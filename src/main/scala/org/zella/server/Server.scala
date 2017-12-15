package org.zella.server

import java.nio.file.Paths

import io.vertx.core.buffer.Buffer
import io.vertx.core.{Handler, Vertx}
import io.vertx.ext.auth.AuthProvider
import io.vertx.ext.web.handler._
import io.vertx.ext.web.sstore.LocalSessionStore
import io.vertx.ext.web.{Router, RoutingContext}
import org.slf4j.LoggerFactory
import org.zella.conf.IPermConfig
import org.zella.permissions.{IFilePermissionChecker, IPermission}
import org.zella.server.Runner.Params
import org.zella.server.handlers.TwirlStaticHandlerImpl

import scala.collection.JavaConverters._

/**
  * @author zella.
  */
class Server(params: Params,
             config: IPermConfig,
             authProvider: AuthProvider,
             permChecker: IFilePermissionChecker,
             vertx: Vertx) {

  private val log = LoggerFactory.getLogger(classOf[Server])

  def start(): Unit = {
    val router = createRouter()
    vertx.createHttpServer.requestHandler(router.accept(_)).listen(params.port)
  }

  private def createRouter(): Router = {

    def toLogin(ctx: RoutingContext): Unit = {
      ctx.session.put("return_url", ctx.request.uri)
      ctx.response.putHeader("location", "/login").setStatusCode(302).end()
    }

    val router = Router.router(vertx)

    router.route.handler(CookieHandler.create)
    router.route.handler(BodyHandler.create
      .setDeleteUploadedFilesOnEnd(true)
      .setUploadsDirectory(params.tmpFolder))
    //TODO session timeout configurable, default - 30min
    router.route.handler(SessionHandler.create(LocalSessionStore.create(vertx)))
    router.route.handler(UserSessionHandler.create(authProvider))
    
    //TODO disable netty and my debug logs

    //TODO LATE, when twirl will be integrated, refresh page after upload with flash scope


    //https://stackoverflow.com/questions/11852689/how-to-turn-off-netty-library-debug-output

    router.route("/").handler(ctx => {
      val firstPub: Option[IPermission] = config.usersHasPerms.toBlocking.value().get("*")
        .flatMap(set => set.headOption)
        .map(first => config.permissions.toBlocking.value().apply(first))

      firstPub match {
        case Some(res) =>
          val url = res.file.toString.replace(params.rootDir, "/files/").replace("//", "/")
          ctx.response.putHeader("location", url).setStatusCode(302).end()
        case None =>
          if (ctx.user() != null) {
            val firstPubU: Option[IPermission] = config.usersHasPerms.toBlocking.value().get(ctx.user().principal().getString("username"))
              .flatMap(set => set.headOption)
              .map(first => config.permissions.toBlocking.value().apply(first))
            firstPubU match {
              case Some(res) =>

                val url = res.file.toString.replace(params.rootDir, "/files/").replace("//", "/")
                ctx.response.putHeader("location", url).setStatusCode(302).end()
              case None => ctx.response.end("No available permissions for you")
            }
          } else {
            toLogin(ctx)
          }
      }

      //Хендлер, который проверяет,
      //если есть публичный ресурс, то выкатываем 1ый
      //если нету, если юзер залогинен, то выкатываем его ресурс(если нет ресурса, то "no permissions")
      //           если не залогинен, то на логин форм
    })


    val adminHanlder = new Handler[RoutingContext] {
      override def handle(ctx: RoutingContext): Unit = {
        if (ctx.user() != null && ctx.user().principal().getString("username").equals(params.admin)) ctx.next()
        else {
          toLogin(ctx)
        }
      }
    }
    router.route("/update").handler(adminHanlder)
    router.route("/do_update").handler(adminHanlder)

    router.get("/login").handler((ctx: RoutingContext) => {
      if (!ctx.session.data.containsKey("return_url"))
        ctx.session.put("return_url", "/")
      ctx.response().putHeader("Content-Type", "text/html").end(html.login.render(ctx).body)
    })

    //ok
    router.get("/update").handler((ctx: RoutingContext) => {

      val html = vertx.fileSystem().readFileBlocking("web/update.html").toString()
      val conf = vertx.fileSystem().readFileBlocking(params.config).toString()

      ctx.response
        .putHeader("Content-Type", "text/html")
        .end(html.replace("{content}", conf))
    })

    //ok
    router.post("/do_update").handler(ctx => {
      val conf = ctx.request().getFormAttribute("conf")
      vertx.fileSystem().writeFileBlocking(params.config, Buffer.buffer(conf))
      config.reload()
      ctx.response().end("Permissions updated")
    })

    //FIXME logout ok text

    // Handles the actual login
    router.post("/authenticate").handler(FormLoginHandler.create(authProvider))

    router.get("/files/*").handler((ctx: RoutingContext) => {

      val requestedFile = ctx.request.path.replaceFirst("/files/", "")

      val requestedPath = Paths.get(params.rootDir, requestedFile)

      if (permChecker.isPermitted(ctx.user(), requestedPath, "r")) {
        val canWrite = permChecker.isPermitted(ctx.user(), requestedPath, "w")
        ctx.put("canWrite", canWrite)
        //".." availability in listing
        val canUp = permChecker.isPermitted(ctx.user(), requestedPath.getParent, "r")
        ctx.put("canUp", canUp)

        ctx.next()
      } else {
        toLogin(ctx)
      }

    })

    router.post("/files/upload").handler((ctx: RoutingContext) => {
      val toDir = Paths.get(params.rootDir, ctx.request.getParam("directory").replaceFirst("/files/", ""))

      if (permChecker.isPermitted(ctx.user(), toDir, "w")) {
        val uploaded = ctx.fileUploads().asScala.toSeq.head
        vertx.fileSystem.move(uploaded.uploadedFileName(),
          Paths.get(toDir.toString, uploaded.fileName()).toString, (isMoved) => {
            if (isMoved.succeeded()) {
              log.debug("File uploaded")
              ctx.response().end("File uploaded")
            }
            else {
              log.error("upload err", isMoved.cause())
              ctx.response().setStatusCode(500).end("Uploading error")
            }
          })
      } else {
        log.debug("toLogin")
        toLogin(ctx)
      }
    })

    router.get("/logout").handler((ctx) => {
      if (ctx.user()!=null) {
        ctx.clearUser()
        //flash message
        ctx.put("msg", "Logout succeed")
      }
      //reroute to login
      ctx.reroute("/login")
    })

    router.get("/files/*").
      handler(new TwirlStaticHandlerImpl().setDirectoryListing(true)
        .setCachingEnabled(false)
        .setAllowRootFileSystemAccess(true)
        .setWebRoot(params.rootDir))

    router
  }

}

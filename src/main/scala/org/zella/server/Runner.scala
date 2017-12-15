package org.zella.server

import java.util.Objects

import io.vertx.core.Vertx
import org.zella.auth.provider.SimpleAuthProvider
import org.zella.conf.impl.ReloadablePermConfig
import org.zella.permissions.impl.FilePermissionCheckerImpl

/**
  * @author zella.
  */
object Runner {


  //FIXME case class instead tuple
  private def parseProps(): Params = {
    try {
      val confFile = System.getProperty("configFile")
      val port = System.getProperty("port").toInt
      val rootDir = System.getProperty("rootDir")
      val reloadInterval = System.getProperty("reloadInterval", "-1").toInt
      val admin = System.getProperty("admin")
      val tmpDir = System.getProperty("tmpDir")

      Objects.requireNonNull(confFile)
      Objects.requireNonNull(rootDir)
      Objects.requireNonNull(admin)

      Params(confFile, port, rootDir, reloadInterval, admin, tmpDir)

    } catch {
      case e: Throwable => throw new IllegalArgumentException("You must specify system paramters." +
        " Example: java -DconfigFile=\"/path/to/config.conf\"" +
        " -Dport=9999 -DrootDir=\"/path/to/dir\" -DreloadInterval=10 tmpDir=/path/to/dir -jar server.jar", e)
    }
  }

  def main(args: Array[String]): Unit = {

    val props = parseProps()

    val vertx = Vertx.vertx()

    val config = new ReloadablePermConfig(props.rootDir, props.config, props.reloadInterval)

    val authProvider = new SimpleAuthProvider(vertx, config.users)

    val permissionChecker = new FilePermissionCheckerImpl(config)

    new Server(props,
      config, authProvider, permissionChecker, vertx).start()
  }

  case class Params(config: String,
                    port: Int,
                    rootDir: String,
                    reloadInterval: Int,
                    admin: String,
                    tmpFolder: String)

}

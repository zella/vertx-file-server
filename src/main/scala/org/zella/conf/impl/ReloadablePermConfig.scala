package org.zella.conf.impl

import java.io.File
import java.nio.file.Paths
import java.util.concurrent.{Executors, TimeUnit}

import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import org.zella.conf.IPermConfig
import org.zella.permissions.IPermission
import org.zella.permissions.impl.PermissionImpl
import rx.Single
import rx.subjects.BehaviorSubject

/**
  * @author zella.
  */
class ReloadablePermConfig(rootDir: String, confPath: String, reloadIntervalSeconds: Int) extends IPermConfig {


  private val log = LoggerFactory.getLogger(classOf[ReloadablePermConfig])

  import java.time.LocalDateTime
  import java.time.format.DateTimeFormatter

  //  val str = "1986-04-08 12:30"
  private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

  private var fileLastModified = new File(confPath).lastModified()

  private val scheduler = Executors.newSingleThreadScheduledExecutor

  private val rxUsers = BehaviorSubject.create[Map[String, String]](initUsers())

  private val rxPermissions = BehaviorSubject.create[Map[String, IPermission]](initPermissions())

  private val rxUsersAndPerms = BehaviorSubject.create[Map[String, Set[String]]](initUsersAndPerms())

  startReloading()

  /**
    * @return Map where keys - permissions ids, values - permissions data
    */
  override def permissions: Single[Map[String, IPermission]] = rxPermissions.take(1).toSingle


  /**
    * @return users
    */
  override def users: Single[Map[String, String]] = rxUsers.take(1).toSingle


  /**
    * @return Map where keys - users, values - set of permissions
    */
  override def usersHasPerms: Single[Map[String, Set[String]]] = rxUsersAndPerms.take(1).toSingle


  //  override val rootDir: String = conf.getString("rootDir")
  //
  //
  //  override val port: Int = conf.getInt("port")

  private def isConfModified: Boolean = {
    log.trace("isConfModified")
    val file = new File(confPath)
    val currentLastModified = file.lastModified
    if (currentLastModified > this.fileLastModified) {
      this.fileLastModified = currentLastModified
      true
    }
    else false
  }

  import scala.collection.JavaConverters._

  private def initPermissions(): Map[String, IPermission] = {
    val conf = ConfigFactory.parseFile(new File(confPath))
    conf.getObjectList("permissions").asScala.map(o => {
      o.get("id").unwrapped().toString ->
        PermissionImpl(
          o.toConfig.getString("id"),
          Paths.get(rootDir, o.toConfig.getString("file")),
          o.toConfig.getString("perm"),
          if (o.containsKey("until"))
            Some(LocalDateTime.parse(o.toConfig.getString("until"), dateFormatter))
          else None
        )
    }).toMap
  }

  private def initUsers(): Map[String, String] = {
    val conf = ConfigFactory.parseFile(new File(confPath))
    conf.getObjectList("users").asScala.map(o =>
      o.toConfig.getString("login") -> o.toConfig.getString("pass")).toMap
  }

  private def initUsersAndPerms(): Map[String, Set[String]] = {
    val conf = ConfigFactory.parseFile(new File(confPath))
    conf.getObjectList("usersHasPermissions").asScala.map(o =>
      o.toConfig.getString("login") ->
        o.toConfig.getStringList("permissions").asScala.toSet).toMap
  }

  def reload(): Unit = {
    log.debug("reloadConfig")
    rxPermissions.onNext(initPermissions())
    rxUsers.onNext(initUsers())
    rxUsersAndPerms.onNext(initUsersAndPerms())
  }

  def startReloading(): Unit = {
    if (reloadIntervalSeconds > 0) {
      this.scheduler.scheduleAtFixedRate(() => {
        if (isConfModified) reload()
      }, reloadIntervalSeconds, reloadIntervalSeconds, TimeUnit.SECONDS)
    }
  }

  def stopReloading(): Unit = {
    this.scheduler.shutdown()
  }
}

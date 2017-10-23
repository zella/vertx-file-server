package org.zella.permissions.impl

import java.nio.file.Path
import java.time.LocalDateTime
import javax.annotation.Nullable

import io.vertx.ext.auth.User
import org.zella.auth.user.SimpleUser
import org.zella.conf.IPermConfig
import org.zella.permissions.IFilePermissionChecker

/**
  * @author zella.
  */
class FilePermissionCheckerImpl(conf: IPermConfig) extends IFilePermissionChecker {

  override def isPermitted(@Nullable user: User, file: Path, perm: String): Boolean = {

    val wildcardPerms = conf.usersHasPerms.toBlocking.value.apply("*").map(conf.permissions.toBlocking.value())

    if (user == null || !conf.usersHasPerms.toBlocking.value.contains(user.asInstanceOf[SimpleUser].username)) {
      wildcardPerms
        .exists(_.isPermitted(file, perm, LocalDateTime.now()))
    } else {
      val userName = user.asInstanceOf[SimpleUser].username
      conf.users.toBlocking.value.contains(userName) &&
        conf.usersHasPerms.toBlocking.value.contains(userName) &&
        (conf.usersHasPerms.toBlocking.value.apply(userName)
          .map(conf.permissions.toBlocking.value()) ++ wildcardPerms)
          .exists(_.isPermitted(file, perm, LocalDateTime.now()))
    }
  }
}

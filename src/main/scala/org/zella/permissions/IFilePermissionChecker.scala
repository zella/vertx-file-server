package org.zella.permissions

import java.nio.file.Path

import io.vertx.ext.auth.User

/**
  * @author zella.
  */
trait IFilePermissionChecker {

  def isPermitted(user: User, file: Path, perm: String): Boolean

}

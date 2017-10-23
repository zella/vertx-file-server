package org.zella.auth.user

import java.lang.Boolean

import io.vertx.core.json.JsonObject
import io.vertx.core.{AsyncResult, Handler}
import io.vertx.ext.auth.{AbstractUser, AuthProvider}
import org.zella.permissions.IFilePermissionChecker

/**
  * @author zella.
  */
class SimpleUser(val username: String) extends AbstractUser {


  override def doIsPermitted(permission: String, resultHandler: Handler[AsyncResult[Boolean]]): Unit = {
    //do nothing
  }

  override val principal: JsonObject = new JsonObject().put("username", username)


  override def setAuthProvider(authProvider: AuthProvider): Unit = {
    //do nothing
  }
}

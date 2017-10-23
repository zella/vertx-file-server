package org.zella.auth.provider

import io.vertx.core._
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.{AuthProvider, User}
import org.zella.auth.user.SimpleUser
import rx.Single

/**
  * @author zella.
  */

class SimpleAuthProvider(vertx: Vertx, users: Single[Map[String, String]]) extends AuthProvider {
  override def authenticate(authInfo: JsonObject,
                            resultHandler: Handler[AsyncResult[User]]): Unit = {
    vertx.executeBlocking((fut: Future[User]) => {
      val user: String = authInfo.getString("username")
      val pass: String = authInfo.getString("password")

      if (!users.toBlocking.value().toSet.contains(user, pass)) {
        throw new VertxException("Invalid (user, pass)")
      }
      fut.complete(new SimpleUser(user))
    }, resultHandler)
  }
}

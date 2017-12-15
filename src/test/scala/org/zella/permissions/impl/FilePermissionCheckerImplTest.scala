package org.zella.permissions.impl

import java.nio.file.Paths
import java.time.LocalDateTime

import org.junit.Test
import org.mockito.Mockito._
import org.scalatest.Matchers
import org.zella.auth.user.SimpleUser
import org.zella.conf.IPermConfig
import org.zella.permissions.IPermission
import rx.Single

/**
  * @author zella.
  */
class FilePermissionCheckerImplTest extends Matchers {

  @Test
  def isPermitted(): Unit = {

    val futureTime = LocalDateTime.now().plusDays(30)

    val conf = mock(classOf[IPermConfig])
    when(conf.users)
      .thenReturn(Single.just(Map("id1" -> "pass1","id2" -> "pass2")))
    when(conf.permissions)
      .thenReturn(Single.just(Map[String, IPermission](
        "p1" -> PermissionImpl("p1", Paths.get("/1/2/"), "rw", Some(futureTime)),
        "p2" -> PermissionImpl("p2", Paths.get("/1/2/3/4/"), "rw", Some(futureTime)),
        "p3" -> PermissionImpl("p2", Paths.get("/1/2/3/"), "rw", Some(futureTime)),
        "p4" -> PermissionImpl("p4", Paths.get("/2"), "rw", Some(futureTime)))))
    when(conf.usersHasPerms)
      .thenReturn(Single.just(Map(
        "id1" -> Set("p2", "p3"),
        "*" -> Set("p4"),
      )))

    val user1 = new SimpleUser("id1")

    val user2 = new SimpleUser("id2")

    val subject = new FilePermissionCheckerImpl(conf)

    subject.isPermitted(user1, Paths.get("/1/2/3/somefile"), "r") shouldBe true
    subject.isPermitted(user1, Paths.get("/1/2/somefile"), "r") shouldBe false
    subject.isPermitted(null, Paths.get("/2/"), "r") shouldBe true
    //to public dir
    subject.isPermitted(user1, Paths.get("/2"), "r") shouldBe true

    //user has no permission should access pub
    subject.isPermitted(user2, Paths.get("/2"), "r") shouldBe true

  }

  @Test
  def issue3PublicPermShouldBeOptional(): Unit = {

    val futureTime = LocalDateTime.now().plusDays(30)

    val conf = mock(classOf[IPermConfig])
    when(conf.users)
      .thenReturn(Single.just(Map("id1" -> "pass1","id2" -> "pass2")))
    when(conf.permissions)
      .thenReturn(Single.just(Map[String, IPermission](
        "p1" -> PermissionImpl("p1", Paths.get("/1/2/"), "rw", Some(futureTime)),
        "p2" -> PermissionImpl("p2", Paths.get("/1/2/3/4/"), "rw", Some(futureTime)),
        "p3" -> PermissionImpl("p2", Paths.get("/1/2/3/"), "rw", Some(futureTime)),
        "p4" -> PermissionImpl("p4", Paths.get("/2"), "rw", Some(futureTime)))))
    when(conf.usersHasPerms)
      .thenReturn(Single.just(Map(
        "id1" -> Set("p2", "p3"),
      )))

    val user1 = new SimpleUser("id1")

    val user2 = new SimpleUser("id2")

    val subject = new FilePermissionCheckerImpl(conf)

    subject.isPermitted(user1, Paths.get("/1/2/3/somefile"), "r") shouldBe true
    subject.isPermitted(user1, Paths.get("/1/2/somefile"), "r") shouldBe false
    subject.isPermitted(null, Paths.get("/2/"), "r") shouldBe false
    subject.isPermitted(user1, Paths.get("/2"), "r") shouldBe false
    subject.isPermitted(user2, Paths.get("/2"), "r") shouldBe false

  }

}

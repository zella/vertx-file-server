package org.zella.conf

import java.io.File
import java.nio.file._
import java.time.LocalDateTime

import org.junit.rules.TemporaryFolder
import org.junit.{Rule, Test}
import org.scalatest.Matchers
import org.zella.conf.impl.ReloadablePermConfig
import org.zella.permissions.impl.PermissionImpl

/**
  * @author zella.
  */
class ReloadableConfigTest extends Matchers {

  val _temporaryFolder = new TemporaryFolder

  @Rule
  def testFolder = _temporaryFolder

  def resourceAsFile(resourceName: String): File = {
    new File(getClass.getClassLoader.getResource(resourceName).getFile)
  }

  private def prepareConfFile(file: String): File = {
    val tempConf = testFolder.newFile(file)
    val fromRes = resourceAsFile(file)
    Files.copy(fromRes.toPath, tempConf.toPath, StandardCopyOption.REPLACE_EXISTING)
    tempConf
  }

  private def replaceContent(file: File, replaceTo: String, replaceWith: String): Unit = {
    val newContent = new String(Files.readAllBytes(file.toPath)).replace(replaceTo, replaceWith)
    Files.write(file.toPath, newContent.getBytes)
  }


  @Test
  def readShouldOk(): Unit = {
    val file = prepareConfFile("read.conf")

    val subject = new ReloadablePermConfig("root", file.toString, 0)


    /*
    users = [
      {"login": "admin", "pass": "1488"},
      {"login": "petooh", "pass": "1555"}
    ]

    permissions = [
      {"id": "p1", "file": "songs", "perm": "rw", "until": "1986-04-08 12:30"},
      {"id": "p2", "file": "songs/5091", "perm": "r"},
      {"id": "p3", "file": "songs/1488", "perm": "r", "until": "2988-04-08 12:30"},
    ]

    usersHasPermissions = [
      {"login": "admin", "permissions": ["p1", "p2"]},
      {"login": "petooh", "permissions": ["p2"]},
      {"login": "*", "permissions": ["p3"]}, #public, no login required
    ]

     */

    subject.permissions.toBlocking.value().toList should contain theSameElementsAs
      Seq(
        ("p1", PermissionImpl("p1", Paths.get("root/songs"), "rw", None)),
        ("p2", PermissionImpl("p2", Paths.get("root/songs/5091"), "r", Some(LocalDateTime.of(1986, 4, 8, 12, 30)))),
        ("p3", PermissionImpl("p3", Paths.get("root/songs/1488"), "r", Some(LocalDateTime.of(2988, 4, 8, 12, 30)))),
      )

    subject.users.toBlocking.value().toList should contain theSameElementsAs
      Seq(
        ("admin", "1488"),
        ("petooh", "1555")
      )
    subject.usersHasPerms.toBlocking.value().toList should contain theSameElementsAs
      Seq(
        ("admin", Set("p1", "p2")),
        ("petooh", Set("p2")),
        ("*", Set("p3"))
      )
  }


  @Test
  def reloadShouldOk(): Unit = {
    val file = prepareConfFile("reload.conf")

    val subject = new ReloadablePermConfig("root", file.toString, 0)
    subject.permissions.toBlocking.value().toList should contain theSameElementsAs
      Seq(("p1", PermissionImpl("p1", Paths.get("root/songs"), "rw", None)))

    replaceContent(file, "songs", "musics")

    subject.reload()

    subject.permissions.toBlocking.value().toSeq should contain theSameElementsAs
      Seq(("p1", PermissionImpl("p1", Paths.get("root/musics"), "rw", None)))

  }

  @Test
  def autoReloadShouldOk(): Unit = {
    val file = prepareConfFile("reload.conf")

    val subject = new ReloadablePermConfig("root", file.toString, 3)

    subject.permissions.toBlocking.value().toList should contain theSameElementsAs
      Seq(("p1", PermissionImpl("p1", Paths.get("root/songs"), "rw", None)))

    Thread.sleep(1000)

    replaceContent(file, "songs", "musics")

    Thread.sleep(1000)

    subject.permissions.toBlocking.value().toList should contain theSameElementsAs
      Seq(("p1", PermissionImpl("p1", Paths.get("root/songs"), "rw", None)))

    Thread.sleep(1500)

    subject.permissions.toBlocking.value().toSeq should contain theSameElementsAs
      Seq(("p1", PermissionImpl("p1", Paths.get("root/musics"), "rw", None)))

    replaceContent(file, "musics", "pizzas")

    Thread.sleep(3000)

    subject.permissions.toBlocking.value().toSeq should contain theSameElementsAs
      Seq(("p1", PermissionImpl("p1", Paths.get("root/pizzas"), "rw", None)))

    subject.stopReloading()

  }


}

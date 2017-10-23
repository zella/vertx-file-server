package org.zella.permissions.impl

import java.nio.file.Paths
import java.time.LocalDate

import org.junit.Test
import org.scalatest.Matchers

/**
  * @author zella.
  */
class PermissionImplTest extends Matchers {


  @Test
  def permittedWithDate(): Unit = {
    val subject = PermissionImpl("p1", Paths.get("/1/2/"), "r", Some(LocalDate.of(2017, 1, 25).atStartOfDay()))

    subject.isPermitted(Paths.get("/1/2/3/superfile"), "r", LocalDate.of(2017, 1, 24).atStartOfDay()) shouldBe true
    subject.isPermitted(Paths.get("/1/2/3/4/"), "r", LocalDate.of(2017, 1, 24).atStartOfDay()) shouldBe true
    subject.isPermitted(Paths.get("/1/25/3/superfile"), "r", LocalDate.of(2017, 1, 24).atStartOfDay()) shouldBe false
    subject.isPermitted(Paths.get("/1/2/3/superfile"), "r", LocalDate.of(2017, 1, 26).atStartOfDay()) shouldBe false
    subject.isPermitted(Paths.get("/1/2/3/superfile"), "w", LocalDate.of(2017, 1, 24).atStartOfDay()) shouldBe false
  }

  @Test
  def permittedWithoutDate(): Unit = {
    val subject = PermissionImpl("p1", Paths.get("/1/2/"), "r", None)

    subject.isPermitted(Paths.get("/1/2/3/superfile"), "r", LocalDate.of(2010, 1, 24).atStartOfDay()) shouldBe true
    subject.isPermitted(Paths.get("/1/2/3/superfile"), "r", LocalDate.of(2317, 1, 24).atStartOfDay()) shouldBe true

  }


}

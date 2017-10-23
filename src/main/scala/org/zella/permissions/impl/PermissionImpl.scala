package org.zella.permissions.impl

import java.nio.file.Path
import java.time.{LocalDate, LocalDateTime}

import org.zella.permissions.IPermission

/**
  * @author zella.
  */
case class PermissionImpl(id: String, file: Path, perm: String, until: Option[LocalDateTime]) extends IPermission {

  override def isPermitted(fileToTest: Path, permToTest: String, currentDate: LocalDateTime): Boolean = {

    //permitted
    val isDateOk = !until.exists(_.isBefore(currentDate))

    val permOk = perm.contains(permToTest) //rw contains r

    val fileOk = isChild(fileToTest, file)

    isDateOk && permOk && fileOk
  }


  private def isChild(child: Path, parent: Path): Boolean = {

    val p = parent.toAbsolutePath.normalize()
    val c = child.toAbsolutePath.normalize()

    child.toAbsolutePath.startsWith(parent.normalize().toAbsolutePath)
  }
}

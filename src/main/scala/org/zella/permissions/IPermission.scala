package org.zella.permissions

import java.nio.file.Path
import java.time.LocalDateTime

/**
  *
  * @author zella.
  */
trait IPermission {

  def id: String

  def file: Path

  def perm: String

  def until: Option[LocalDateTime]

  def isPermitted(file: Path, permToTest: String, currentDate: LocalDateTime): Boolean

}

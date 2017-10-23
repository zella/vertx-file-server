package org.zella.conf


import org.zella.permissions.IPermission
import rx.Single


/**
  * @author zella.
  */
trait IPermConfig {

  /**
    * @return Map where keys - permissions ids, values - permissions data
    */
  def permissions: Single[Map[String, IPermission]]

  /**
    * @return users
    */
  def users: Single[Map[String,String]]

  /**
    * @return Map where keys - users, values - set of permissions
    */
  def usersHasPerms: Single[Map[String, Set[String]]]

  def reload(): Unit

}

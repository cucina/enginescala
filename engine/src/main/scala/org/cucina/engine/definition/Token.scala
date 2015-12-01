package org.cucina.engine.definition

import scala.collection.mutable.Set

/**
  * @author levinev
  */
case class Token(val domainObject: Object, val processDefinition: ProcessDefinition, splitDiscriminator: String = "") {
  require(domainObject != null, "DomainObject cannot be null")
  require(processDefinition != null, "ProcessDefinition cannot be null")
  var stateId: String = _
  var children: Set[Token] = Set()
  var parent: Option[Token] = None

  def hasChildren: Boolean = children.nonEmpty

  override def toString: String = {
    super.toString + ", stateId='" + stateId + "', children=" + children
  }
}  

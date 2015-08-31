package org.cucina.engine.definition

import scala.collection.mutable.Set

/**
 * @author levinev
 */
case class Token(val domainObject: Object, val processDefinition: ProcessDefinition) {
  require(domainObject != null, "DomainObject cannot be null")
  require(processDefinition != null, "ProcessDefinition cannot be null")
  var stateId: String = _
  var children: Set[Token] = Set()
  var parent: Option[Token] = None

  def hasChildren: Boolean = children.nonEmpty
}  

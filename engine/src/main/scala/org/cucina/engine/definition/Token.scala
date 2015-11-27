package org.cucina.engine.definition

import scala.collection.mutable.Set

/**
 * @author levinev
 */
<<<<<<< HEAD:engine/src/main/scala/org/cucina/engine/definition/Token.scala
case class Token(val domainObject: Object, val processDefinition: ProcessDefinition, splitId: String = "") {
=======
case class Token(domainObject: Object, processDefinition: ProcessDefinition, index: Int = -1) {
>>>>>>> origin/master:src/main/scala/org/cucina/engine/definition/Token.scala
  require(domainObject != null, "DomainObject cannot be null")
  require(processDefinition != null, "ProcessDefinition cannot be null")
  var stateId: String = _
  var children: Set[Token] = Set()
  var parent: Option[Token] = None

  def hasChildren: Boolean = children.nonEmpty
}  

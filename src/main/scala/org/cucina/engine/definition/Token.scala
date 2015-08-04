package org.cucina.engine.definition

import scala.collection.mutable.Set

/**
 * @author levinev
 */
class Token(val domainObject: Object, var processDefinition: ProcessDefinition = null) {
  var stateId: String = _
  var children: Set[Token] = Set()

  def hasChildren: Boolean = children.nonEmpty
}  

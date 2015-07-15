package org.cucina.engine.definition

import scala.collection.mutable.Set

/**
 * @author levinev
 */
class Token(domainObject:Object, processDefinition:ProcessDefinition) {
  var stateId:String = _
  var children:Set[Token] = Set()
  
  def hasChildren():Boolean = children.nonEmpty
}  

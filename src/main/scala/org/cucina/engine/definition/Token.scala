package org.cucina.engine.definition

import scala.collection.Set

/**
 * @author levinev
 */
class Token(domainObject:Object, processDefinition:ProcessDefinition) {
  var stateId:String
  val children:Set[Token]
  def hasChildren():Boolean = children.nonEmpty
}  

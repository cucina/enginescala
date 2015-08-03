package org.cucina.engine.definition

import org.cucina.engine.actors.BlankOperationActor

/**
 * @author levinev
 */
case class OperationDescriptor(val name: String, val className: String = classOf[BlankOperationActor].getName,val arguments: List[String] = Nil)
extends StackableElementDescriptor

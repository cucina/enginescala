package org.cucina.engine.definition

import org.cucina.engine.actors.BlankOperationActor

/**
 * @author levinev
 */
case class OperationDescriptor(name: String, className: String = classOf[BlankOperationActor].getName, arguments: List[String] = Nil)
  extends StackableElementDescriptor

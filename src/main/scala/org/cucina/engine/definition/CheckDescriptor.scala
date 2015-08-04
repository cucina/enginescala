package org.cucina.engine.definition

import org.cucina.engine.actors.PassingCheckActor

/**
 * @author levinev
 */
case class CheckDescriptor(name: String, className: String = classOf[PassingCheckActor].getName, arguments: List[String] = Nil)
  extends StackableElementDescriptor
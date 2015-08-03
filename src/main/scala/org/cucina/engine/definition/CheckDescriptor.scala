package org.cucina.engine.definition

import org.cucina.engine.ProcessContext
import org.cucina.engine.actors.PassingCheckActor

/**
 * @author levinev
 */
case class CheckDescriptor(val name: String, val className: String = classOf[PassingCheckActor].getName, val arguments: List[String] = Nil)
  extends StackableElementDescriptor
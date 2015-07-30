package org.cucina.engine.definition

import akka.actor.Props
import org.cucina.engine.actors.TransitionActor


/**
 * @author levinev
 */
class TransitionDescriptor(val name: String, output: String,
                           leaveOperations: Seq[OperationDescriptor],
                           checks: Seq[CheckDescriptor],
                           val className: String = classOf[TransitionActor].getName) extends StackableElementDescriptor {
  def props: Props = Props(Class.forName(className), name, output, leaveOperations, checks)
}

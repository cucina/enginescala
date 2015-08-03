package org.cucina.engine.definition

import akka.actor.Props
import org.cucina.engine.actors.TransitionActor


/**
 * @author levinev
 */
case class TransitionDescriptor(val name: String, output: String,
                           leaveOperations: Seq[OperationDescriptor] = List(),
                           checks: Seq[CheckDescriptor] = List(),
                           val className: String = classOf[TransitionActor].getName) extends StackableElementDescriptor {
  /// Factory method, allows to plugin allows to plugin alternative transition implementations
  def props: Props = Props(Class.forName(className), name, output, leaveOperations, checks)
}

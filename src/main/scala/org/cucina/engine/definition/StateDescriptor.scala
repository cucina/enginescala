package org.cucina.engine.definition

import akka.actor.Props
import org.cucina.engine.actors.StateActor

/**
 * @author levinev
 */
case class StateDescriptor(name: String,
                           transitions: Seq[TransitionDescriptor],
                           enterOperations: Seq[OperationDescriptor]=List(),
                           leaveOperations: Seq[OperationDescriptor]=List(),
                           className: String = classOf[StateActor].getName)
  extends ProcessElementDescriptor {
  override def props:Props = Props(Class.forName(className), name, transitions, enterOperations, leaveOperations)
}


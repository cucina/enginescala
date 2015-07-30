package org.cucina.engine.definition

import akka.actor.Props
import org.cucina.engine.actors.StateActor

/**
 * @author levinev
 */
case class StateDescriptor(val name: String,
                           val transitions: Seq[TransitionDescriptor],
                           val enterOperations: Seq[OperationDescriptor]=Nil,
                           val leaveOperations: Seq[OperationDescriptor]=Nil,
                           val className: String = classOf[StateActor].getName)
  extends ProcessElementDescriptor {
  def props:Props = Props(Class.forName(className), name, transitions, enterOperations, leaveOperations)
}


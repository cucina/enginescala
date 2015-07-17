package org.cucina.engine.actors

import akka.actor.Actor
import org.cucina.engine.ProcessContext
import org.cucina.engine.definition.Transition
import org.cucina.engine.definition.State
import org.cucina.engine.definition.OperationDescriptor
import akka.actor.Props

/**
 * @author levinev
 */

case class EnterState(state: State, transition: Transition, processContext: ProcessContext)
case class EnterStateResult()

class StateEnterer extends Actor {
  def receive = {
    case EnterState(state, transition, processContext) => {
      state.enterInternal(processContext)
      if (state.enterOperations.nonEmpty) {
        val opproc = context.actorOf(Props[OperationProcessor])
        for (od <- state.enterOperations) {
          opproc ! new OperationDescriptorWrap(od, processContext)
        }
      }
      sender ! new EnterStateResult
    }
  }
}


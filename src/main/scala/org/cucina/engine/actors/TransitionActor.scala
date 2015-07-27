package org.cucina.engine.actors

import org.cucina.engine.ProcessContext
import org.cucina.engine.definition.{StateDescriptor, OperationDescriptor}
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.actorRef2Scala
import akka.actor.Props

/**
 * @author levinev
 */
case class Occur(processContext: ProcessContext)

class TransitionActor(id: String, output: StateDescriptor, leaveOperations: Iterable[OperationDescriptor]) extends Actor {
  def receive = {
    case Occur(pc) => {
      pc.token.stateId = null
      fireOperations(leaveOperations, pc)
      publishLeaveEvent(id, pc)
      ///      state ! new EnterState(id, pc)
    }
    case _ =>
  }

  private def fireOperations(ops: Iterable[OperationDescriptor], pc: ProcessContext) = {

  }

  private def publishLeaveEvent(id: String, processontext: ProcessContext) = {

  }

  private def findOutput(): ActorRef = {

  }
}

object TransitionActor {
  def props(id: String, output: StateDescriptor, leaveOperations: Iterable[OperationDescriptor]): Props = {
    Props(classOf[TransitionActor], id, output, leaveOperations)
  }
}
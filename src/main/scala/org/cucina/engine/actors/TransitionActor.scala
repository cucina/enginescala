package org.cucina.engine.actors

import org.cucina.engine.ProcessContext
import org.cucina.engine.definition.{StateDescriptor, OperationDescriptor}
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.actorRef2Scala
import akka.actor.Props
import org.slf4j.LoggerFactory

/**
 * @author levinev
 */
case class Occur(processContext: ProcessContext)

class TransitionActor(id: String, output: StateDescriptor, leaveOperations: Iterable[OperationDescriptor]) extends Actor {
  private val LOG = LoggerFactory.getLogger(getClass)

  def receive = {
    case Occur(pc) => {
      pc.token.stateId = null
      fireOperations(leaveOperations, pc)
      publishLeaveEvent(id, pc)
      findOutput() ! new EnterState(id, pc)
    }
    case e@_ => LOG.debug("Unknown event:" + e)
  }

   def fireOperations(ops: Iterable[OperationDescriptor], pc: ProcessContext) = {
    for (lo <- ops) {
      // TODO
    }
  }

   def publishLeaveEvent(id: String, processontext: ProcessContext) = {

  }

   def findOutput(): ActorRef = {
    null
  }
}

object TransitionActor {
  def props(id: String, output: StateDescriptor, leaveOperations: Iterable[OperationDescriptor]): Props = {
    Props(classOf[TransitionActor], id, output, leaveOperations)
  }
}
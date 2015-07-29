package org.cucina.engine.actors

import org.cucina.engine.ProcessContext
import org.cucina.engine.actors.support.ActorFinder
import org.cucina.engine.definition.{CheckDescriptor, StateDescriptor, OperationDescriptor}
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.actorRef2Scala
import akka.actor.Props
import org.slf4j.LoggerFactory

/**
 * @author levinev
 */
case class Occur(processContext: ProcessContext)

case class CheckPassed(processContext: ProcessContext, remains: Seq[CheckDescriptor])

case class CheckFailed(checkName: String, reason: String)

class TransitionActor(name: String, output: StateDescriptor, leaveOperations: Seq[OperationDescriptor], checks: Seq[CheckDescriptor])
  extends Actor with ActorFinder {
  private val LOG = LoggerFactory.getLogger(getClass)

  def receive = {
    case Occur(pc) => {
      pc.token.stateId = null
      runChecks(pc, checks)
    }
    case CheckPassed(pc, re) => runChecks(pc, re)
    case CheckFailed(cn, reason) =>
      LOG.info("Failed check :" + cn + " due to " + reason)
      // TODO anything else to remain in the current state?

    case e@_ => LOG.debug("Unknown event:" + e)
  }

  def fireOperations(ops: Iterable[OperationDescriptor], pc: ProcessContext) = {
    for (lo <- ops) {
      // TODO pull the trait
    }
  }

  def publishLeaveEvent(id: String, processontext: ProcessContext) = {
    // TODO pull trait
  }

  private def runChecks(pc: ProcessContext, chex: Seq[CheckDescriptor]): Unit = {
    if (chex.isEmpty) {
      fireOperations(leaveOperations, pc)
      publishLeaveEvent(name, pc)
      findActor(output) ! new EnterState(name, pc)
    } else
      findActor(chex.head) ! new CheckRequest(pc, chex.tail)
  }

}

object TransitionActor {
  def props(id: String, output: StateDescriptor, leaveOperations: Iterable[OperationDescriptor]): Props = {
    Props(classOf[TransitionActor], id, output, leaveOperations)
  }
}
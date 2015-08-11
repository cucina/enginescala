package org.cucina.engine.actors

import akka.util.Timeout
import org.cucina.engine.{ExecuteFailed, ProcessContext}
import org.cucina.engine.actors.support.ActorFinder
import org.cucina.engine.definition.{CheckDescriptor, OperationDescriptor}
import akka.actor._
import org.slf4j.LoggerFactory

/**
 * @author levinev
 */
case class CheckPassed(processContext: ProcessContext, remains: Seq[CheckDescriptor])

case class CheckFailed(checkName: String, reason: String)

class TransitionActor(name: String, output: String,
                      leaveOperations: Seq[OperationDescriptor] = List(),
                      checks: Seq[CheckDescriptor] = List())
  extends Actor
  with ActorFinder {
  private val LOG = LoggerFactory.getLogger(getClass)
  lazy val outputState: ActorRef = {
    findActor(output) match {
      case None =>
        throw new IllegalArgumentException("Failed to find output state '" + output + "'")
      case Some(a) => a
    }
  }
  val checkActors: Seq[ActorRef] = {
    checks.map(ch => createActor(ch))
  }
  val leaveOpActors: Seq[ActorRef] = {
    leaveOperations.map((lo => createActor(lo)))
  }

  val staticstack: Seq[ActorRef] = checkActors ++ leaveOpActors

  override def preStart() = {
    try {
      // reason for this call is to have a quickfail
      LOG.info("Located output state:" + outputState)
    } catch {
      case e: IllegalArgumentException => {
        LOG.error("Failed to find output state @ " + "../../" + output)
        self ! PoisonPill
      }
    }
  }

  // this actor should be terminal in a stack therefore it should set a new stack
  // in context terminating it with output state
  def receive = {
    case StackRequest(pc, callerstack) =>
      if (!callerstack.isEmpty) sender ! ExecuteFailed(pc.client, "Transition '" + name + "' should be a terminal actor in the stack")
      else {
        // build stack and execute it
        val stack: Seq[ActorRef] = staticstack :+ outputState
        LOG.info("Stack=" + stack)
        stack.head forward new StackRequest(pc, stack.tail)
      }
    case e@_ =>
      LOG.warn("Unhandled:" + e)
  }
}

object TransitionActor {
  def props(id: String, output: String, leaveOperations: Seq[OperationDescriptor], checks: Seq[CheckDescriptor]): Props = {
    Props(classOf[TransitionActor], id, output, leaveOperations, checks)
  }
}
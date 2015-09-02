package org.cucina.engine.actors

import org.cucina.engine.{ExecuteFailed, ProcessContext}
import org.cucina.engine.actors.support.{ActorCreator, ActorFinder}
import org.cucina.engine.definition.{CheckDescriptor, OperationDescriptor}
import akka.actor._
import org.slf4j.LoggerFactory

/**
 * @author levinev
 */
case class DryCheck(pc: ProcessContext)

class Transition(name: String, output: String,
                 leaveOperations: Seq[OperationDescriptor] = List(),
                 checks: Seq[CheckDescriptor] = List())
  extends Actor
  with ActorFinder
  with ActorCreator {
  private val LOG = LoggerFactory.getLogger(getClass)
  val outputState: ActorRef = {
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

  override def preStart = {
    try {
      // reason for this call is to have a quickfail
      LOG.info("Located output state:" + outputState)
    } catch {
      case e: IllegalArgumentException => {
        LOG.error("Failed to find output state @ " + output)
        self ! PoisonPill
      }
    }
  }

  override def postStop = {
    LOG.info("Been killed " + self)
  }

  // this actor should be terminal in a stack therefore it should set a new stack
  // in context terminating it with output state
  def receive = {
    case StackRequest(pc, callerstack) =>
      LOG.info("StackRequest")
      if (callerstack.nonEmpty) {
        LOG.warn("Transition '" + name + "' should be a terminal actor in the stack, but the stack was " + callerstack)
        sender ! ExecuteFailed(pc.client, "Transition '" + name + "' should be a terminal actor in the stack")
      }
      else {
        // build stack and execute it
        val stack: Seq[ActorRef] = staticstack :+ outputState
        LOG.info("Stack=" + stack)
        stack.head forward new StackRequest(pc, stack.tail)
      }
    case DryCheck(pc) =>
      // only to run through checks
      LOG.info("DryCheck " + this)
      checkActors.head forward new StackRequest(pc, checkActors.tail)
    case e@_ =>
      LOG.warn("Unhandled:" + e)
  }
}

object Transition {
  def props(id: String, output: String, leaveOperations: Seq[OperationDescriptor], checks: Seq[CheckDescriptor]): Props = {
    Props(classOf[Transition], id, output, leaveOperations, checks)
  }
}
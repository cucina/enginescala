package org.cucina.engine.actors

import org.cucina.engine.actors.support.ActorFinder

import org.cucina.engine.{ExecuteFailed, ProcessContext}
import org.cucina.engine.definition._
import org.slf4j.LoggerFactory

import akka.actor.{ActorRef, Terminated, Actor, Props}

/**
 * @author levinev
 */

case class EnterState(transitionName: String, processContext: ProcessContext)

case class LeaveState(transitionName: String, processContext: ProcessContext)

class State(name: String,
                 transitions: Seq[TransitionDescriptor],
                 enterListeners: Seq[String] = List(),
                 leaveListeners: Seq[String] = List(),
                 enterOperations: Seq[OperationDescriptor] = Nil,
                 leaveOperations: Seq[OperationDescriptor] = Nil)
  extends AbstractState(name, transitions, enterListeners, leaveListeners, enterOperations, leaveOperations) {
  private val LOG = LoggerFactory.getLogger(getClass)

  override def receiveLocal = {
    case EnterState(tr, pc) =>
      pc.token.stateId = name
      LOG.info("Entering stateId=" + name + " with transition " + tr)
      LOG.info("Calling " + enterStack.head)
      if (tr == null) enterStack.head forward new StackRequest(pc, enterStack.tail)
      else
        transActors.get(tr) match {
          case Some(t) => enterStack.head forward new StackRequest(pc, enterStack.tail :+ t)
          case None =>
            LOG.warn("Attempted to enter provding invalid exit transition name '" + tr + "'")
            sender ! ExecuteFailed(pc.client, "Attempted to enter providing invalid exit transition name '" + tr + "'")
        }

    case LeaveState(tr, pc) =>
      if (!canLeave(pc)) {
        sender ! ExecuteFailed(pc.client, "Cannot leave current Place '" + name +
          "' since it is not the active place associated with the supplied ProcessContext")
      }

      transActors.get(tr) match {
        case None => sender ! ExecuteFailed(pc.client, "Cannot find transition '" + tr + "' in state '" + name + "'")
        case Some(a) =>
          val stack = leaveStack :+ a
          stack.head forward new StackRequest(pc, stack.tail)
      }

    case e@_ => LOG.warn("Unhandled " + e)
  }

  def processStackRequest(pc:ProcessContext, stack: Seq[ActorRef]) = {
    var lpc = pc
    lpc.token.stateId = name
    LOG.info("Calling " + enterStack.head + " with " + lpc)
    enterStack.head forward new StackRequest(lpc, enterStack.tail)
  }

  private def canLeave(pc: ProcessContext): Boolean = {
    pc.token.stateId == name
  }
}

object State {
  def props(name: String, transitions: Seq[TransitionDescriptor],
            enterPublisher: Seq[String],
            leavePublisher: Seq[String],
            enterOperations: Seq[OperationDescriptor],
            leaveOperations: Seq[OperationDescriptor]): Props = {
    Props(classOf[State], name, transitions, enterPublisher, leavePublisher, enterOperations, leaveOperations)
  }
}

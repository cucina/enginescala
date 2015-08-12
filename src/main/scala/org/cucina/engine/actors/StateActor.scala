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

class StateActor(name: String,
                 transitions: Seq[TransitionDescriptor],
                 enterListeners: Seq[ListenerDescriptor] = List(),
                 leaveListeners: Seq[ListenerDescriptor] = List(),
                 enterOperations: Seq[OperationDescriptor] = Nil,
                 leaveOperations: Seq[OperationDescriptor] = Nil)
  extends Actor with ActorFinder {
  val LOG = LoggerFactory.getLogger(getClass)
  lazy val enterOpActors: Seq[ActorRef] = {
    enterOperations.map(op => createActor(op))
  }
  lazy val leaveOpActors: Seq[ActorRef] = {
    leaveOperations.map(op => createActor(op))
  }
  lazy val transActors: Map[String, ActorRef] = {
    transitions.map(tr => tr.name -> createActor(tr)).toMap
  }
  lazy val enterPubActor: ActorRef = createActor(new EnterPublisherDescriptor(enterListeners))
  lazy val leavePubActor: ActorRef = createActor(new LeavePublisherDescriptor(leaveListeners))
  lazy val enterStack: Seq[ActorRef] = enterOpActors :+ enterPubActor
  lazy val leaveStack: Seq[ActorRef] = leaveOpActors :+ leavePubActor

  def receive = {
    case EnterState(tr, pc) =>
      pc.token.stateId = name
      LOG.info("entering stateId=" + name)
      LOG.info("Calling " + enterStack.head)
      transActors.get(tr) match {
        case Some(t) => enterStack.head forward new StackRequest(pc, enterStack.tail :+ t)
        case None => enterStack.head forward new StackRequest(pc, enterStack.tail)
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
    case StackRequest(pc, callerstack) =>
      if (!callerstack.isEmpty) sender ! ExecuteFailed(pc.client, "State '" + name + "' should be a terminal actor in the stack")
      else {
        LOG.info("Entering state=" + name)
        enterStack.head forward new StackRequest(pc, enterStack.tail)
      }

    case Terminated(child) =>
      LOG.warn("A child is dead:" + child)
    // TODO handle and revive
    case e@_ => LOG.warn("Unhandled " + e)
  }

  private def canLeave(pc: ProcessContext): Boolean = {
    pc.token.stateId == name
  }
}

object StateActor {
  def props(name: String, transitions: Seq[TransitionDescriptor],
            enterPublisher: Seq[ListenerDescriptor],
            leavePublisher: Seq[ListenerDescriptor],
            enterOperations: Seq[OperationDescriptor],
            leaveOperations: Seq[OperationDescriptor]): Props = {
    Props(classOf[StateActor], name, transitions, enterPublisher, leavePublisher, enterOperations, leaveOperations)
  }
}

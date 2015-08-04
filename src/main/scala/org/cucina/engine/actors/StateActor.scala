package org.cucina.engine.actors

import org.cucina.engine.actors.support.ActorFinder

import org.cucina.engine.ProcessContext
import org.cucina.engine.definition._
import org.slf4j.LoggerFactory

import akka.actor.{Terminated, Actor, Props}

/**
 * @author levinev
 */

case class EnterState(transitionName: String, processContext: ProcessContext)

case class LeaveState(transitionName: String, processContext: ProcessContext)

class StateActor(name: String,
                 transitions: Iterable[TransitionDescriptor],
                 enterOperations: Seq[OperationDescriptor] = Nil,
                 leaveOperations: Seq[OperationDescriptor] = Nil)
  extends Actor with ActorFinder {
  val LOG = LoggerFactory.getLogger(getClass)
  // TODO these should be defined globally per process definition
  val enterPublisher = new EnterPublisherDescriptor(List())
  val leavePublisher = new LeavePublisherDescriptor(List())
  val enterStack: Seq[StackableElementDescriptor] = enterOperations :+ enterPublisher
  val leaveStack: Seq[StackableElementDescriptor] = leaveOperations :+ leavePublisher

  override def preStart() = {
    context.actorOf(EnterPublisher.props(List()), enterPublisher.name)
    context.actorOf(LeavePublisher.props(List()), leavePublisher.name)

    transitions.foreach(td => createActor(td))
    enterOperations.foreach(op => createActor(op))
    leaveOperations.foreach(op => createActor(op))
  }

  def receive = {
    case EnterState(tr, pc) =>
      pc.token.stateId = name
      LOG.info("entering stateId=" + name)
      LOG.info("Calling " + enterStack.head)
      findAndSend(enterStack.head, new StackRequest(pc, enterStack.tail))

    case LeaveState(tr, pc) =>
      if (!canLeave(pc)) {
        throw new IllegalArgumentException("Cannot leave current Place '" + name +
          "' since it is not the active place associated with the supplied ExecutionContext")
      }

      // TODO handle None
      val stack = leaveStack :+ transitions.find(_.name == name).get
      findAndSend(stack.head, new StackRequest(pc, stack.tail))

    case Terminated(child) =>
      LOG.warn("A child is dead:" + child)
    // TODO handle and revive
    case _ =>
  }

  private def canLeave(pc: ProcessContext): Boolean = {
    pc.token.stateId == name
  }
}

object StateActor {
  def props(name: String, transitions: Iterable[TransitionDescriptor],
            enterOperations: Seq[OperationDescriptor],
            leaveOperations: Seq[OperationDescriptor]): Props = {
    Props(classOf[StateActor], name, transitions, enterOperations, leaveOperations)
  }
}

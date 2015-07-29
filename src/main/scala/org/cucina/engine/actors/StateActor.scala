package org.cucina.engine.actors

import akka.actor.Actor.Receive
import org.cucina.engine.actors.support.ActorFinder

import scala.collection.mutable.Map

import org.cucina.engine.ProcessContext
import org.cucina.engine.definition.{StackableElementDescriptor, EnterPublisherDescriptor, OperationDescriptor, TransitionDescriptor}
import org.slf4j.LoggerFactory

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props

/**
 * @author levinev
 */

case class EnterState(transitionName: String, processContext: ProcessContext)

case class LeaveState(transitionName: String, processContext: ProcessContext)

class StateActor(name: String,
                 enterOperations: Seq[OperationDescriptor],
                 leaveOperations: Seq[OperationDescriptor],
                 transitions: Iterable[TransitionDescriptor])
  extends Actor with ActorFinder {
  val LOG = LoggerFactory.getLogger(getClass())
  val enterPublisher = new EnterPublisherDescriptor
  // TODO create leavePublisher
  val leavePublisher = new EnterPublisherDescriptor

  val enterStack: Seq[StackableElementDescriptor] = enterOperations :+ enterPublisher
  val leaveStack: Seq[StackableElementDescriptor] = leaveOperations :+ leavePublisher
  val transitionActors: Map[String, ActorRef] = Map[String, ActorRef]()

  def receive = {
    case EnterState(tr, pc) => {
      pc.token.stateId = name
      LOG.info("stateId=" + name)
      LOG.info("Calling " + enterStack.head)
      findActor(enterStack.head) ! new StackRequest(pc, enterStack.tail)
    }

    case LeaveState(tr, pc) => {
      if (!canLeave(pc)) {
        throw new IllegalArgumentException("Cannot leave current Place '" + name +
          "' since it is not the active place associated with the supplied ExecutionContext")
      }

      val trax = transitionActors.getOrElseUpdate(tr, buildTransition(tr))
      if (trax == null) {
        throw new IllegalArgumentException("Transition cannot be null")
      }

      //TODO tr.checkConditions(pc)

      val stack = leaveStack :+ transitions.find(_.name == name).get
      findActor(stack.head) ! new StackRequest(pc, stack.tail)
      trax ! new Occur(pc)
    }

    case _ =>
  }

  private def buildTransition(name: String): ActorRef = {
    val transitionDescriptor = transitions.find(_.name == name).get
    findActor(transitionDescriptor)
  }

  private def canLeave(pc: ProcessContext): Boolean = {
    pc.token.stateId == name
  }
}

object StateActor {
  def props(name: String, enterOperations: Iterable[OperationDescriptor],
            leaveOperations: Iterable[OperationDescriptor], transitions: Iterable[TransitionDescriptor]): Props = {
    Props(classOf[StateActor], name, enterOperations, leaveOperations, transitions)
  }
}

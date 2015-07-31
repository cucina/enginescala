package org.cucina.engine.actors

import akka.actor.Actor.Receive
import org.cucina.engine.actors.support.ActorFinder

import scala.collection.mutable.Map

import org.cucina.engine.ProcessContext
import org.cucina.engine.definition.{StackableElementDescriptor, EnterPublisherDescriptor, OperationDescriptor, TransitionDescriptor}
import org.slf4j.LoggerFactory

import akka.actor.{Terminated, Actor, ActorRef, Props}

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
  val LOG = LoggerFactory.getLogger(getClass())
  val enterPublisher = new EnterPublisherDescriptor
  // TODO create leavePublisher
  val leavePublisher = new EnterPublisherDescriptor

  val enterStack: Seq[StackableElementDescriptor] = enterOperations :+ enterPublisher
  val leaveStack: Seq[StackableElementDescriptor] = leaveOperations :+ leavePublisher
  val transitionActors: Map[String, ActorRef] = Map[String, ActorRef]()

  override def preStart() = {
    for (td <- transitions) {
      createTransition(td)
    }
  }

  def receive = {
    case EnterState(tr, pc) =>
      pc.token.stateId = name
      LOG.info("stateId=" + name)
      LOG.info("Calling " + enterStack.head)
      findActor(enterStack.head) ! new StackRequest(pc, enterStack.tail)

    case LeaveState(tr, pc) =>
      if (!canLeave(pc)) {
        throw new IllegalArgumentException("Cannot leave current Place '" + name +
          "' since it is not the active place associated with the supplied ExecutionContext")
      }

      val trax = transitionActors.getOrElseUpdate(tr, buildTransitionOrElse(tr, createTransition(tr)))
      if (trax == null) {
        throw new IllegalArgumentException("Transition cannot be null")
      }

      //TODO tr.checkConditions(pc)

      val stack = leaveStack :+ transitions.find(_.name == name).get
      findActor(stack.head) ! new StackRequest(pc, stack.tail)
      trax ! new Occur(pc)
    case Terminated(child) =>
      LOG.warn("A child is dead:" + child)
    // TODO handle and revive
    case _ =>
  }

  private def buildTransitionOrElse(name: String, op: => ActorRef): ActorRef = {
    val transitionDescriptor = transitions.find(_.name == name).get
    val a = findActor(transitionDescriptor)
    if (a == null) op
    else a
  }

  private def createTransition(name: String): ActorRef = {
    val td = transitions.find((td) => {
      td.name == name
    }).get
    createTransition(td)
  }

  private def createTransition(td: TransitionDescriptor): ActorRef = {
    // early fail prevention
    require(td.name != null || td.name.length > 0, "transition name should not be null or empty")
    val trax = context.actorOf(td.props, td.name)
    context watch trax
    transitionActors += td.name -> trax
    trax
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

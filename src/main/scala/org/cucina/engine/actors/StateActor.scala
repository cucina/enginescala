package org.cucina.engine.actors

import akka.actor.Actor.Receive
import org.cucina.engine.actors.support.ActorFinder

import scala.collection.mutable.Map

import org.cucina.engine.ProcessContext
import org.cucina.engine.definition._
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
  val LOG = LoggerFactory.getLogger(getClass)
  // TODO these should be defined globally per process definition
  val enterPublisher = new EnterPublisherDescriptor(List())
  val leavePublisher = new LeavePublisherDescriptor(List())

  val enterStack: Seq[StackableElementDescriptor] = enterOperations :+ enterPublisher
  val leaveStack: Seq[StackableElementDescriptor] = leaveOperations :+ leavePublisher
  val transitionActors: Map[String, ActorRef] = Map[String, ActorRef]()

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

      val trax = transitionActors.getOrElseUpdate(tr, buildTransitionOrElse(tr, createTransition(tr)))
      if (trax == null) {
        throw new IllegalArgumentException("Transition cannot be null")
      }

      //TODO tr.checkConditions(pc)

      val stack = leaveStack :+ transitions.find(_.name == name).get
      findAndSend(stack.head, new StackRequest(pc, stack.tail))
      trax ! new Occur(pc)
    case Terminated(child) =>
      LOG.warn("A child is dead:" + child)
    // TODO handle and revive
    case _ =>
  }

  private def buildTransitionOrElse(name: String, op: => ActorRef): ActorRef = {
    val transitionDescriptor = transitions.find(_.name == name).get
    val a = findActor(transitionDescriptor)
    a match {
      case None => op
      case _ => a.get
    }
  }

  private def createTransition(name: String): ActorRef = {
    transitions.find((td) => {
      td.name == name
    }) match {
      case None => throw new IllegalArgumentException("No transition '" + name + "' has been defined for the state " + this.name)
      case Some(td:TransitionDescriptor) =>
        val trax = createActor(td)
        transitionActors += td.name -> trax
        trax
    }
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

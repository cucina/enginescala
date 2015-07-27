package org.cucina.engine.actors

import scala.collection.mutable.Map

import org.cucina.engine.ProcessContext
import org.cucina.engine.definition.OperationDescriptor
import org.cucina.engine.definition.TransitionDescriptor
import org.cucina.engine.definition.TransitionDescriptor
import org.slf4j.LoggerFactory

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props

/**
 * @author levinev
 */

case class EnterState(transitionName: String, processContext: ProcessContext)

case class LeaveState(transitionName: String, processContext: ProcessContext)

class StateActor(id: String, enterOperations: Iterable[OperationDescriptor], leaveOperations: Iterable[OperationDescriptor], transitions: Iterable[TransitionDescriptor])
  extends Actor {
  val LOG = LoggerFactory.getLogger(getClass())
  val transitionActors: Map[String, ActorRef] = Map[String, ActorRef]()

  def receive = {
    case EnterState(tr, pc) => {
      pc.token.stateId = id
      fireOperations(enterOperations, pc)
      publishEnterEvent(id, tr, pc)
    }

    case LeaveState(tr, pc) => {
      if (!canLeave(pc)) {
        throw new IllegalArgumentException("Cannot leave current Place '" + id +
          "' since it is not the active place associated with the supplied ExecutionContext")
      }

      val trax = transitionActors.getOrElseUpdate(tr, buildTransition(tr))
      if (trax == null) {
        throw new IllegalArgumentException("Transition cannot be null")
      }

      //tr.checkConditions(pc)

      fireOperations(leaveOperations, pc)
      trax ! new Occur(pc)
    }

    case _ =>
  }

  private def buildTransition(name: String): ActorRef = {
    val transitionDescriptor = transitions.find(_.id == name).get
    context.system.actorOf(TransitionActor.props(transitionDescriptor.id, transitionDescriptor.output, null))
  }

  private def canLeave(pc: ProcessContext): Boolean = {
    true
  }

  private def fireOperations(ops: Iterable[OperationDescriptor], pc: ProcessContext) = {
    // TODO call to OperationProcessor
  }

  private def publishEnterEvent(id: String, from: String, pc: ProcessContext) = {

  }
}

object StateActor {
  def props(id: String, enterOperations: Iterable[OperationDescriptor], leaveOperations: Iterable[OperationDescriptor], transitions: Iterable[TransitionDescriptor]): Props = {
    Props(classOf[StateActor], id, enterOperations, leaveOperations, transitions)
  }
}

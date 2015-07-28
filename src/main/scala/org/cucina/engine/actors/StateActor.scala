package org.cucina.engine.actors

import org.cucina.engine.actors.support.ActorFinder

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

class StateActor(name: String,
                 enterOperations: Iterable[OperationDescriptor],
                 leaveOperations: Iterable[OperationDescriptor],
                 transitions: Iterable[TransitionDescriptor])
  extends Actor with ActorFinder {
  val LOG = LoggerFactory.getLogger(getClass())
  val transitionActors: Map[String, ActorRef] = Map[String, ActorRef]()

  def receive = {
    case EnterState(tr, pc) => {
      pc.token.stateId = name
      fireOperations(enterOperations, pc)
      publishEnterEvent(name, tr, pc)
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

      //tr.checkConditions(pc)

      fireOperations(leaveOperations, pc)
      trax ! new Occur(pc)
    }

    case _ =>
  }

  private def buildTransition(name: String): ActorRef = {
    val transitionDescriptor = transitions.find(_.name == name).get
    findActor(transitionDescriptor, context)
  }

  private def canLeave(pc: ProcessContext): Boolean = {
    true
  }

  private def fireOperations(ops: Iterable[OperationDescriptor], pc: ProcessContext) = {
    // TODO call to OperationProcessor - create trait for it
  }

  private def publishEnterEvent(id: String, from: String, pc: ProcessContext) = {
    // TODO create trait to publish
  }
}

object StateActor {
  def props(id: String, enterOperations: Iterable[OperationDescriptor], leaveOperations: Iterable[OperationDescriptor], transitions: Iterable[TransitionDescriptor]): Props = {
    Props(classOf[StateActor], id, enterOperations, leaveOperations, transitions)
  }
}

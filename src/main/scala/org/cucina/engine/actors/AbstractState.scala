package org.cucina.engine.actors

import akka.actor.{Terminated, ActorRef, Actor}
import org.cucina.engine.{ProcessContext, ExecuteFailed}
import org.cucina.engine.actors.support.{ActorCreator, ActorFinder}
import org.cucina.engine.definition.{LeavePublisherDescriptor, EnterPublisherDescriptor, TransitionDescriptor, OperationDescriptor}
import org.slf4j.LoggerFactory

/**
 * Created by vagrant on 8/21/15.
 */
abstract class AbstractState(name: String,
                             transitions: Seq[TransitionDescriptor],
                             listeners: Seq[String] = List(),
                             enterOperations: Seq[OperationDescriptor] = Nil,
                             leaveOperations: Seq[OperationDescriptor] = Nil)
  extends Actor with ActorCreator {
  private val LOG = LoggerFactory.getLogger(getClass)
  lazy val enterOpActors: Seq[ActorRef] = {
    enterOperations.map(op => createActor(op))
  }
  lazy val leaveOpActors: Seq[ActorRef] = {
    leaveOperations.map(op => createActor(op))
  }
  val transActors: Map[String, ActorRef] = {
    // TODO preserve order
    transitions.map(tr => tr.name -> createActor(tr)).toMap
  }
  lazy val enterPubActor: ActorRef = createActor(new EnterPublisherDescriptor(listeners))
  lazy val leavePubActor: ActorRef = createActor(new LeavePublisherDescriptor(listeners))
  lazy val enterStack: Seq[ActorRef] = enterOpActors :+ enterPubActor
  lazy val leaveStack: Seq[ActorRef] = leaveOpActors :+ leavePubActor

  def receive = receiveStack orElse receiveLocal

  def receiveLocal: Receive = {
    case a@_ => LOG.info("Not handling " + a + " implementing class should override receiveLocal to handle specific cases")
  }

  def receiveStack: Receive = {
    case StackRequest(pc, callerstack) =>
      if (!callerstack.isEmpty) sender ! ExecuteFailed(pc.client, "State of type " + getClass.getSimpleName
        + " '" + name + "' should be a terminal actor in the stack")
      else {
        LOG.info("Entering state=" + name)
        processStackRequest(pc, callerstack)
      }

    case Terminated(child) =>
      LOG.warn("A child is dead:" + child)
    // TODO handle and revive
  }

  def processStackRequest(pc: ProcessContext, stack: Seq[ActorRef])

  def findTransition(name: String): ActorRef = {
    transActors.get(name) match {
      case Some(t) => t
      case None =>
        LOG.warn("Failed to find transition '" + name + "'")
        throw new IllegalArgumentException("Failed to find transition '" + name + "'")
    }
  }

  protected def canLeave(pc: ProcessContext): Boolean = {
    pc.token.stateId == name  && !pc.token.hasChildren
  }
}

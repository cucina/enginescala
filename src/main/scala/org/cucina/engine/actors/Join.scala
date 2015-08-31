package org.cucina.engine.actors

import org.cucina.engine.{ExecuteFailed, ProcessContext}
import org.cucina.engine.definition._
import org.slf4j.LoggerFactory

import akka.actor.{ActorRef, Actor, Props}

/**
 * @author levinev
 */

class Join(name: String,
           transition: TransitionDescriptor,
           listeners: Seq[String] = List(),
           enterOperations: Seq[OperationDescriptor] = Nil,
           leaveOperations: Seq[OperationDescriptor] = Nil)
  extends AbstractState(name, transition :: Nil, listeners, enterOperations, leaveOperations) {
  private val LOG = LoggerFactory.getLogger(getClass)

  def processStackRequest(pc: ProcessContext, stack: Seq[ActorRef]) = {
    pc.token.parent match {
      case Some(parent) =>
        val joiner = context.actorOf(Props(classOf[Joiner], transition, listeners, leaveOperations))
      // execute enter ops appending a temp actor to handle next step below
      // kill this token
      // if it is the last one, execute leave ops on parent
      // take the only transition
      case None =>
        LOG.warn("Attempted to execute join with a parentless context")
        sender ! ExecuteFailed(pc.client, "Attempted to execute join with a parentless context")
    }
  }
}

object Join {
  def props(name: String, transitions: Seq[TransitionDescriptor],
            listeners: Seq[String] = List(),
            enterOperations: Seq[OperationDescriptor] = List(),
            leaveOperations: Seq[OperationDescriptor] = List()): Props = {
    Props(classOf[State], name, transitions, listeners, enterOperations, leaveOperations)
  }
}

class Joiner(transition: Transition,
             listeners: Seq[String] = List(),
             leaveOperations: Seq[OperationDescriptor] = Nil) extends Actor {
  private val LOG = LoggerFactory.getLogger(getClass)

  def receive = {
    case StackRequest(pc, callerstack) =>
      pc.token.parent match {
        case Some(parent) =>
        // kill this token
        // if it is the last one, execute leave ops on parent
        // take the only transition
        case None =>
          LOG.warn("Attempted to execute join with a parentless context")
          sender ! ExecuteFailed(pc.client, "Attempted to execute join with a parentless context")
      }
  }
}
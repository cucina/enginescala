package org.cucina.engine.actors

import org.cucina.engine.actors.support.ActorFinder

import org.cucina.engine.{ExecuteFailed, ProcessContext}
import org.cucina.engine.definition._
import org.slf4j.LoggerFactory

import akka.actor.{ActorRef, Terminated, Actor, Props}

/**
 * @author levinev
 */

class Decision(name: String,
                 transitions: Seq[TransitionDescriptor],
                 enterListeners: Seq[String] = List(),
                 leaveListeners: Seq[String] = List(),
                 enterOperations: Seq[OperationDescriptor] = Nil,
                 leaveOperations: Seq[OperationDescriptor] = Nil)
  extends AbstractState(name, transitions, enterListeners, leaveListeners, enterOperations, leaveOperations) {
  private val LOG = LoggerFactory.getLogger(getClass)

  def processStackRequest(pc:ProcessContext, stack: Seq[ActorRef]) = {}
}

object Decision {
  def props(name: String, transitions: Seq[TransitionDescriptor],
            enterPublisher: Seq[String],
            leavePublisher: Seq[String],
            enterOperations: Seq[OperationDescriptor],
            leaveOperations: Seq[OperationDescriptor]): Props = {
    Props(classOf[Decision], name, transitions, enterPublisher, leavePublisher, enterOperations, leaveOperations)
  }
}

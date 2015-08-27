package org.cucina.engine.actors

import org.cucina.engine.actors.support.ActorFinder

import org.cucina.engine.{ExecuteFailed, ProcessContext}
import org.cucina.engine.definition._
import org.slf4j.LoggerFactory

import akka.actor.{ActorRef, Terminated, Actor, Props}

/**
 * @author levinev
 */

class Split(name: String,
            transitions: Seq[TransitionDescriptor],
            listeners: Seq[String] = List(),
            enterOperations: Seq[OperationDescriptor] = Nil,
            leaveOperations: Seq[OperationDescriptor] = Nil)
  extends AbstractState(name, transitions, listeners, enterOperations, leaveOperations) {
  private val LOG = LoggerFactory.getLogger(getClass)

  def processStackRequest(pc:ProcessContext, stack: Seq[ActorRef]) = {}
}

object Split {
  def props(name: String, transitions: Seq[TransitionDescriptor],
            listeners: Seq[String] = List(),
            enterOperations: Seq[OperationDescriptor] = List(),
            leaveOperations: Seq[OperationDescriptor] = List()): Props = {
    Props(classOf[State], name, transitions, listeners, enterOperations, leaveOperations)
  }
}

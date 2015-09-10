package org.cucina.engine.actors

import org.cucina.engine.actors.support.ActorFinder

import org.cucina.engine.{ExecuteFailed, ProcessContext}
import org.cucina.engine.definition._
import org.slf4j.LoggerFactory

import akka.actor.{ActorRef, Terminated, Actor, Props}

/**
 * Split execution using all available transitions to run subflows along each one in parallel
 * @author levinev
 */

class Split(name: String,
            transitions: Seq[TransitionDescriptor],
            listeners: Seq[String] = List(),
            enterOperations: Seq[OperationDescriptor] = Nil,
            leaveOperations: Seq[OperationDescriptor] = Nil)
  extends AbstractState(name, transitions, listeners, enterOperations, leaveOperations) {
  private val LOG = LoggerFactory.getLogger(getClass)

  def processStackRequest(pc:ProcessContext, stack: Seq[ActorRef]) = {
    val launcher = context.actorOf(Props(classOf[SplitLauncher], sender, pc))
    LOG.info("Create launcher=" + launcher)
    launcher forward DivisionLaunch(pc.token.domainObject, transActors.values)
  }
}

object Split {
  def props(name: String, transitions: Seq[TransitionDescriptor],
            listeners: Seq[String] = List(),
            enterOperations: Seq[OperationDescriptor] = List(),
            leaveOperations: Seq[OperationDescriptor] = List()): Props = {
    Props(classOf[Split], name, transitions, listeners, enterOperations, leaveOperations)
  }
}

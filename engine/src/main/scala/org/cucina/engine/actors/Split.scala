package org.cucina.engine.actors

import akka.actor.{Props, ActorRef}
import org.cucina.engine.ProcessContext
import org.cucina.engine.definition.{OperationDescriptor, TransitionDescriptor}
import org.slf4j.LoggerFactory

/**
  * Split execution using all available transitions to run subflows along each one in parallel
  * @author levinev
  */

class Split(name: String, join: String,
            transitions: Seq[TransitionDescriptor],
            listeners: Seq[String] = List(),
            enterOperations: Seq[OperationDescriptor] = Nil,
            leaveOperations: Seq[OperationDescriptor] = Nil)
  extends AbstractSplit(name, join, transitions, listeners, enterOperations, leaveOperations) {

  def splitter(pc: ProcessContext): Iterable[(Object, ActorRef, String)] = transActors.values.map { tr => (pc.token.domainObject, tr, tr.hashCode().toString) }
}

object Split {
  def props(name: String, join: String, transitions: Seq[TransitionDescriptor],
            listeners: Seq[String] = List(),
            enterOperations: Seq[OperationDescriptor] = List(),
            leaveOperations: Seq[OperationDescriptor] = List()): Props = {
    Props(classOf[Split], name, join, transitions, listeners, enterOperations, leaveOperations)
  }
}

package org.cucina.engine.actors

import akka.actor.Status.{Failure, Success}
import akka.util.Timeout


import org.cucina.engine.{ExecuteComplete, ExecuteResult, ExecuteFailed, ProcessContext}
import org.cucina.engine.definition._
import org.slf4j.LoggerFactory

import akka.actor.{ActorRef, Props}
import akka.pattern.ask
import scala.concurrent.{Await, Future}
import scala.util.Try
import scala.concurrent.ExecutionContext.Implicits.global


/**
 * @author levinev
 */

class Decision(name: String,
               transitions: Seq[TransitionDescriptor],
               listeners: Seq[String] = List(),
               enterOperations: Seq[OperationDescriptor] = Nil,
               leaveOperations: Seq[OperationDescriptor] = Nil)
  extends AbstractState(name, transitions, listeners, enterOperations, leaveOperations) {
  private val LOG = LoggerFactory.getLogger(getClass)

  def processStackRequest(pc: ProcessContext, stack: Seq[ActorRef]) = {
    // iterate through transition until found one which will work and execute it
    transActors.values.find((p) => isPassable(p, pc)) match {
      case Some(t) => t forward StackRequest(pc, List())
      case None =>
        LOG.warn("Failed to find a single transition in '" + name + "' for " + pc)
        sender ! ExecuteFailed(pc.client, "Failed to find a single transition in '" + name + "'")
    }
  }

  private def isPassable(tr: ActorRef, pc: ProcessContext): Boolean = {
    import scala.concurrent.duration.DurationInt
    implicit val timeout = Timeout(5 seconds) // configure it?
    val testResult: Future[ExecuteResult] = ask(tr, DryCheck(pc)).mapTo[ExecuteResult]
    Await.result(testResult, timeout.duration) match {
      case t: ExecuteComplete => true
      case t: ExecuteFailed => false
    }
  }
}

// TODO stateful shortlived actor finding passable transition and sending it back to Decision


object Decision {
  def props(name: String, transitions: Seq[TransitionDescriptor],
            listeners: Seq[String] = List(),
            enterOperations: Seq[OperationDescriptor] = List(),
            leaveOperations: Seq[OperationDescriptor] = List()): Props = {
    Props(classOf[Decision], name, transitions, listeners, enterOperations, leaveOperations)
  }
}

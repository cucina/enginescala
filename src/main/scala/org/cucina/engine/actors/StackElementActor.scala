package org.cucina.engine.actors

import akka.actor.Actor.Receive
import akka.actor.{ActorRef, ActorContext, Actor}
import org.cucina.engine.{ExecuteFailed, ExecuteComplete, ProcessContext}
import org.cucina.engine.actors.support.ActorFinder
import org.slf4j.LoggerFactory

/**
 * Created by levinev on 28/07/2015.
 *
 * Default behaviour for stack elements such as operations and checks
 */
case class StackRequest(processContext: ProcessContext, stack: Seq[ActorRef])

case class StackElementExecuteResult(success: Boolean, processContext: ProcessContext = null, message: String = null, throwable: Throwable = null)

trait StackElementActor
  extends Actor
  with ActorFinder {
  private[this] val LOG = LoggerFactory.getLogger(getClass)

  def receive = receiveStack orElse receiveLocal

  def receiveLocal: Receive = {
    case a@_ => LOG.info("Not handling " + a + " implementing class should override receiveLocal to handle specific cases")
  }

  def receiveStack(implicit context: ActorContext): Receive = {
    case StackRequest(pc, stack) => {
      execute(pc) match {
        case StackElementExecuteResult(false, pc, message, throwable) => sender ! ExecuteFailed(pc.client, message)
        case _ =>
          if (!stack.isEmpty)
            stack.head forward StackRequest(pc, stack.tail)
          else sender ! ExecuteComplete(pc)
      }
    }
  }

  def execute(processContext: ProcessContext): StackElementExecuteResult
}

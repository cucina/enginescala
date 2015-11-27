package org.cucina.engine.actors

import akka.actor.{ActorContext, Actor, ActorRef}
import org.cucina.engine.{ExecuteComplete, ExecuteFailed, ProcessContext}
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
      LOG.info("Executing:" + pc + " in " + stack)
      execute(pc) match {
        case StackElementExecuteResult(false, pc, message, throwable) => sender ! ExecuteFailed(pc.client, message)
        case _ =>
          if (!stack.isEmpty) {
            LOG.info("Forwarding to " + stack.head)
            stack.head forward StackRequest(pc, stack.tail)
          }
          else {
            LOG.info("Last actor in the stack, sender=" + sender)
            sender ! ExecuteComplete(pc)
          }
      }
    }
  }

  def execute(processContext: ProcessContext): StackElementExecuteResult
}

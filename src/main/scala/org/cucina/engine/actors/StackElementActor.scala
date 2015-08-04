package org.cucina.engine.actors

import akka.actor.Actor.Receive
import akka.actor.{ActorContext, Actor}
import org.cucina.engine.ProcessContext
import org.cucina.engine.actors.support.ActorFinder
import org.cucina.engine.definition.StackableElementDescriptor
import org.slf4j.LoggerFactory

/**
 * Created by levinev on 28/07/2015.
 *
 * Default behaviour for stack elements such as operations and checks
 */
case class StackRequest(processContext: ProcessContext, stack: Seq[StackableElementDescriptor])

case class StackElementExecuteResult(success: Boolean, processContext: ProcessContext = null, message: String = null, trowable: Throwable = null)

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
      execute(pc)
      if (!stack.isEmpty)
        // TODO handle None
        findActor(stack.head.name).get ! new StackRequest(pc, stack.tail)
      else pc.client ! new ExecuteComplete(pc)
    }
    case e@_ => LOG.debug("Unhandled " + e)
  }

  def execute(processContext: ProcessContext): StackElementExecuteResult
}

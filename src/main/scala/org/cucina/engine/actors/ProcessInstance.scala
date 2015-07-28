package org.cucina.engine.actors

import org.cucina.engine.ProcessContext
import org.cucina.engine.actors.support.ActorFinder
import org.cucina.engine.definition.ProcessDefinition
import org.slf4j.LoggerFactory
import akka.actor._
import scala.collection.mutable.Map
import org.cucina.engine.definition.StateDescriptor
import scala.concurrent.Await
import akka.util.Timeout
import scala.concurrent.duration._

/**
 * @author levinev
 */
case class ExecuteStart(processContext: ProcessContext, transitionId: String)
case class ExecuteTransition(processContext: ProcessContext, transitionId: String)
case class ExecuteComplete(processContext: ProcessContext)
case class ExecuteFailed(processContext: ProcessContext, failure:String)

class ProcessInstance(processDefinition: ProcessDefinition)
  extends Actor with ActorFinder {
  private[this] val LOG = LoggerFactory.getLogger(getClass())
  val states = Map[String, ActorRef]()

  def receive = {
    case ExecuteStart(pc, trid) => {
      val sactor = findActor(processDefinition.startState, context)
      sactor forward new EnterState(trid, pc)
    }

    case ExecuteTransition(pc, trid) => {
      val sactor = findActor(processDefinition.findState(pc.token.stateId), context)
      sactor forward new LeaveState(trid, pc)
    }

    case e@_ => LOG.info("Unknown:" + e)
  }
}

object ProcessInstance {
  def props(definition: ProcessDefinition): Props = {
    Props(classOf[ProcessInstance], definition)
  }
}
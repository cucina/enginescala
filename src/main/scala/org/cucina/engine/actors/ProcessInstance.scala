package org.cucina.engine.actors

import org.cucina.engine.ProcessContext
import org.cucina.engine.definition.ProcessDefinition
import org.slf4j.LoggerFactory
import akka.actor._
import scala.collection.mutable.Map

/**
 * @author levinev
 */
case class ExecuteStart(processContext: ProcessContext, transitionId: String)

case class ExecuteTransition(processContext: ProcessContext, transitionId: String)

case class ExecuteComplete(processContext: ProcessContext)

case class ExecuteFailed(processContext: ProcessContext, failure: String)

case object Init

class ProcessInstance(processDefinition: ProcessDefinition)
  extends Actor {
  private[this] val LOG = LoggerFactory.getLogger(getClass())
  val states = Map[String, ActorRef]()

  for (sd <- processDefinition.getAllStates()) {
    val p = sd.props
    LOG.info("Building state from these " + p)
    states += sd.name -> context.actorOf(p, sd.name)
  }

  for (ar <- states.values) {
    ar ! Init
  }

  def receive = {
    case ExecuteStart(pc, trid) => {
      findState(processDefinition.startState) forward new EnterState(trid, pc)
    }

    case ExecuteTransition(pc, trid) => {
      findState(pc.token.stateId) forward new LeaveState(trid, pc)
    }

    case e@_ => LOG.info("Unknown:" + e)
  }

  private def findState(name: String): ActorRef = {
    val ar = states.get(name)
    if (None == ar) {
      LOG.error("Failed to find state '" + name + "'")
      throw new IllegalArgumentException("Failed to find state '" + name + "'")
    }
    ar.get
  }
}

object ProcessInstance {
  def props(definition: ProcessDefinition): Props = {
    Props(classOf[ProcessInstance], definition)
  }
}
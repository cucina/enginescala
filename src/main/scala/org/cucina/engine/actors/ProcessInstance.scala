package org.cucina.engine.actors

import org.cucina.engine.{ExecuteFailed, ProcessContext}
import org.cucina.engine.definition.ProcessDefinition
import org.slf4j.LoggerFactory
import akka.actor._
import scala.collection.mutable.Map

/**
 * @author levinev
 */
class ProcessInstance(processDefinition: ProcessDefinition)
  extends Actor {
  private[this] val LOG = LoggerFactory.getLogger(getClass())
  val states = Map[String, ActorRef]()

  for (sd <- processDefinition.states) {
    val p = sd.props
    LOG.info("Building state from these " + p)
    states += sd.name -> context.actorOf(p, sd.name)
  }

  def receive = {
    case StartInstance(pc, trid) => {
      findState(processDefinition.startState) forward new EnterState(trid, pc)
    }

    case MoveInstance(pc, trid) => {
      findState(pc.token.stateId) forward new LeaveState(trid, pc)
    }

    case e@_ =>
      LOG.info("Unknown:" + e)
      sender ! new ExecuteFailed(null, "Unknown:" + e)
  }

  private def findState(name: String): ActorRef = {
    states.get(name) match {
      case None =>
        LOG.error("Failed to find state '" + name + "'")
        throw new IllegalArgumentException("Failed to find state '" + name + "'")
      case Some(ar) => ar
    }
  }
}

object ProcessInstance {
  def props(definition: ProcessDefinition): Props = {
    Props(classOf[ProcessInstance], definition)
  }
}
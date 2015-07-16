package org.cucina.engine.actors

import akka.actor.Actor
import akka.actor.ActorRef
import org.cucina.engine.ProcessContext
import org.cucina.engine.definition.State
import org.slf4j.LoggerFactory
import org.cucina.engine.definition.ProcessDefinition

/**
 * @author levinev
 */

case class StartProcess(processDefinition: ProcessDefinition, domainObject: Object, transitionId: String, parameters: Map[String, Object], client: ActorRef)


class ProcessStarter(tokenFactory: ActorRef) extends Actor {
  private[this] val LOG = LoggerFactory.getLogger(getClass())

  def receive = {
    case StartProcess(processDefinition, domainObject, transitionId, parameters, client) => {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Object=" + domainObject)
      }

      tokenFactory ! new CreateToken(processDefinition, domainObject)
      
    }
    case TokenResult(token) => {
      val processContext: ProcessContext = new ProcessContext(token, parameters)
  
      // Processing the state
      val start: State = processDefinition.startState

      start.enter(null, processContext)
      start.leave(findTransition(token, transitionId), processContext)

      client ! token
    }
  }
}
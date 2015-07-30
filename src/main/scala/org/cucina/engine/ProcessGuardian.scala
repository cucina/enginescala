package org.cucina.engine

import akka.actor.{Terminated, Props, Actor}
import org.cucina.engine.actors._
import org.slf4j.LoggerFactory

/**
 * Created by levinev on 30/07/2015.
 */
// Transition is optional so is taken only if specified
case class StartProcess(processDefinitionName: String, domainObject: Object, transitionId: String = null, parameters: Map[String, Object])
// the main call for an existing process
case class MakeTransition(processDefinitionName: String, domainObject: Object, transitionId: String, parameters: Map[String, Object])

class ProcessGuardian extends Actor {
  private[this] val LOG = LoggerFactory.getLogger(getClass)
  private val definitionRegistry = context.actorOf(Props[DefinitionRegistry], "definitionRegistry")
  context watch definitionRegistry
  private val processInstanceFactory = context.actorOf(Props(classOf[ProcessInstanceFactory], definitionRegistry), "processInstanceFactory")
  context watch processInstanceFactory
  private val tokenFactory = context.actorOf(Props(classOf[TokenFactory], processInstanceFactory), "tokenFactory")
  context watch tokenFactory

  def receive = {
    case StartProcess(pdn, doj, trid, params) => {
      tokenFactory ! new StartToken(pdn, doj, trid, params, sender())
    }
    case MakeTransition(pdn, doj, trid, params) => {
      tokenFactory ! new StartToken(pdn, doj, trid, params, sender())
    }
    case Terminated(child) =>
      LOG.warn("Actor died " + child)
    case e@_ => LOG.info("Unhandled " + e)
  }


}
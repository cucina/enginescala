package org.cucina.engine

import akka.actor.{ActorRef, Terminated, Props, Actor}
import org.cucina.engine.actors._
import org.cucina.engine.definition.parse.DefinitionParser
import org.cucina.engine.definition.{TransitionDescriptor, StateDescriptor, ProcessDefinition, Token}
import org.cucina.engine.definition.reader.FakeDefinitionReader
import org.slf4j.LoggerFactory

import scala.collection.mutable


/**
 * Created by levinev on 30/07/2015.
 */
// Transition is optional so is taken only if specified
case class StartProcess(processDefinitionName: String, domainObject: Object, transitionId: String = null, parameters: Map[String, Object])

// the main call for an existing process
case class MakeTransition(processDefinitionName: String, domainObject: Object, transitionId: String, parameters: Map[String, Object])

case class ProcessContext(token: Token, parameters: scala.collection.mutable.Map[String, Object], client: ActorRef)

case class AddDefinition(string: String)

class ProcessGuardian(definitionRegistry: ActorRef = null, processInstanceFactory: ActorRef = null, tokenFactory: ActorRef = null) extends Actor with DefinitionParser {
  private[this] val LOG = LoggerFactory.getLogger(getClass)

  lazy val localDefinitionRegistry = {
    if (definitionRegistry == null) {
      context.actorOf(Props[DefinitionRegistry], "definitionRegistry")
    } else {
      definitionRegistry
    }
  }

  lazy val localProcessInstanceFactory = {
    if (processInstanceFactory == null) {
      context.actorOf(ProcessInstanceFactory.props(localDefinitionRegistry), "processInstanceFactory")
    } else {
      processInstanceFactory
    }
  }

  lazy val localTokenFactory = {
    if (tokenFactory == null) {
      context.actorOf(TokenFactory.props(localProcessInstanceFactory), "tokenFactory")
    } else {
      tokenFactory
    }
  }

  override def preStart = {
    context watch localDefinitionRegistry
    context watch localProcessInstanceFactory
    context watch localTokenFactory
  }

  def receive = {
    case StartProcess(pdn, doj, trid, params) =>
      tokenFactory ! new StartToken(pdn, doj, trid, params, sender())

    case MakeTransition(pdn, doj, trid, params) =>
      tokenFactory ! new StartToken(pdn, doj, trid, params, sender())

    case AddDefinition(stri) =>
      definitionRegistry ! new AddProcessDefinition(parseDefinition(stri))

    case Terminated(child) =>
      LOG.warn("Actor died " + child)
    case e@_ => LOG.info("Unhandled " + e)
  }


}

package org.cucina.engine

import akka.actor._
import org.cucina.engine.actors._
import org.cucina.engine.definition.parse.DefinitionParser
import org.cucina.engine.definition.{TransitionDescriptor, StateDescriptor, ProcessDefinition, Token}
import org.cucina.engine.repository.{StoreToken, MapTokenRepository}
import org.slf4j.LoggerFactory

import scala.collection.immutable.HashMap


/**
 * Created by levinev on 30/07/2015.
 */
trait ClientContainer {
  val client: ActorRef
}

// Transition is optional so is taken only if specified
case class StartProcess(processDefinitionName: String, domainObject: Object, transitionId: String = null, parameters: Option[Map[String, Object]] = None)

// the main call for an existing process
case class MakeTransition(processDefinitionName: String, domainObject: Object, transitionId: String, parameters: Option[Map[String, Object]] = None)

case class ProcessContext(token: Token, parameters: scala.collection.mutable.Map[String, Object], client: ActorRef) extends ClientContainer

case class AddDefinition(string: String)

case class ExecuteComplete(processContext: ProcessContext)

case class ExecuteFailed(client: ActorRef, failure: String) extends ClientContainer

case class ProcessDefinitionWrap(processDefinition: ProcessDefinition, nested: NestedTuple)

case class NestedTuple(originalRequest: Object, client: ActorRef) extends ClientContainer

case class ProcessFailure(cause: String)

case class GetAvailableTransitions(domainObject: Object, processDefinitionName: String = null)

class ProcessGuardian(definitionRegistry: ActorRef = null, processInstanceFactory: ActorRef = null, tokenFactory: ActorRef = null)
  extends Actor with DefinitionParser {
  private[this] val LOG = LoggerFactory.getLogger(getClass)

  /*
    val actor = context.system.actorOf(Props[DefaultDeadLetterHandlerActor])
    context.system.eventStream.subscribe(actor, [DeadLetter])
  */

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
      val tokenRepository = context.actorOf(Props[MapTokenRepository], "tokenRepository")
      context.actorOf(TokenFactory.props(tokenRepository), "tokenFactory")
    } else {
      tokenFactory
    }
  }

  override def postStop = {
    LOG.info("Stopped")
  }

  override def preStart = {
    context watch localDefinitionRegistry
    context watch localProcessInstanceFactory
    context watch localTokenFactory
  }

  def receive = {
    case e@StartProcess(pdn, _, _, _) =>
      localDefinitionRegistry ! FindDefinition(pdn, new NestedTuple(e, sender))

    case e@MakeTransition(pdn, _, _, _) =>
      localDefinitionRegistry ! FindDefinition(pdn, new NestedTuple(e, sender))

    case AddDefinition(stri) =>
      localDefinitionRegistry ! AddProcessDefinition(parseDefinition(stri))

    case ProcessDefinitionWrap(definition, cause) =>
      cause.originalRequest match {
        case e: StartProcess =>
          LOG.info("Starting process:" + e)
          LOG.debug("localTokenFactory:" + localTokenFactory)
          localTokenFactory ! StartToken(definition, e.domainObject, e.transitionId, e.parameters.getOrElse(new HashMap[String, Object]), cause.client)
        case e: MakeTransition =>
          LOG.info("Making transition:" + e)
          localTokenFactory ! MoveToken(definition, e.domainObject, e.transitionId, e.parameters.getOrElse(new HashMap[String, Object]), cause.client)
        case e: GetAvailableTransitions =>
          LOG.info("Getting transitions:" + e)
          localTokenFactory ! GetTransitions(definition, e.domainObject, cause.client)
        case _ =>
          LOG.warn("Unknown originalRequest:" + cause.originalRequest)
          cause.client ! "Unknown originalRequest:" + cause.originalRequest
      }
    case e: StartInstance =>
      localProcessInstanceFactory ! e
    case e: MoveInstance =>
      localProcessInstanceFactory ! e
    case ExecuteComplete(pc) =>
      LOG.info("Completed for " + pc)
      localTokenFactory ! StoreToken(pc.token)
      pc.client ! pc.token.domainObject
    case ExecuteFailed(client, error) =>
      client ! ProcessFailure("Whoops:" + error)
    case e@GetAvailableTransitions(domain, pdn) =>
      // TODO implement for a case when an object is a subject of only one process
      if (pdn == null) {}
      else {
        localDefinitionRegistry ! FindDefinition(pdn, new NestedTuple(e, sender))
      }
    case Terminated(child) =>
      // TODO handle by restarting it
      LOG.warn("Actor died " + child)
    case e@_ => LOG.info("Unhandled " + e)
  }
}

object ProcessGuardian {
  def props(definitionRegistry: ActorRef = null, processInstanceFactory: ActorRef = null, tokenFactory: ActorRef = null): Props =
    Props(classOf[ProcessGuardian], definitionRegistry, processInstanceFactory, tokenFactory)
}

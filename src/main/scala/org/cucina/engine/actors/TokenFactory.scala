package org.cucina.engine.actors

import org.cucina.engine.definition.{ProcessDefinition, Token}
import org.cucina.engine.repository._
import org.slf4j.LoggerFactory
import akka.actor.Actor
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.actor.ActorRef
import org.cucina.engine.{ClientContainer, ExecuteFailed, ProcessContext}

trait TokenRequest {
  val processDefinition: ProcessDefinition
  val domainObject: Object
  val transitionId: String
  val parameters: Map[String, Object]
  val client: ActorRef
}

case class StartToken(processDefinition: ProcessDefinition, domainObject: Object, transitionId: String = null, parameters: Map[String, Object], client: ActorRef)
  extends TokenRequest with ClientContainer

case class MoveToken(processDefinition: ProcessDefinition, domainObject: Object, transitionId: String, parameters: Map[String, Object], client: ActorRef)
  extends TokenRequest with ClientContainer

case class TokenNotFound(op: TokenRequest)

case class TokenResult(token: Token, op: TokenRequest)

/**
 * @author levinev
 */
class TokenFactory(tokenRepository: ActorRef) extends Actor {
  private[this] val LOG = LoggerFactory.getLogger(getClass)
  val me = self

  def receive = {
    case st: StartToken =>
      require(st.domainObject != null, "The 'domainObject' cannot be null.")
      // call to tokenRepository to find an existing one for the object
      println(tokenRepository)
      tokenRepository forward FindByDomain(st, me)
    case mt: MoveToken =>
      require(mt.domainObject != null, "The 'domainObject' cannot be null.")
      require(mt.transitionId != null, "The 'transitionId' cannot be null.")
      // call to tokenRepository to find an existing one for the object
      tokenRepository forward FindByDomain(mt, me)

    case TokenResult(token: Token, op: TokenRequest) =>
      op match {
        case st: StartToken =>
          LOG.info("Found existing token:" + token)
          // TODO should it carry on if a token exists or fail here? client policy?
          // startProcess(token, st)
          sender ! ExecuteFailed(st.client, "Cannot start a process, one exists already")
        case st: MoveToken =>
          LOG.info("Found existing token:" + token)
          moveProcess(token, st)
      }

    case TokenNotFound(op: TokenRequest) =>
      op match {
        case st: StartToken =>
          LOG.info("Creating new token")
          startProcess(Token(st.domainObject, st.processDefinition), st)
        case st: MoveToken =>
          LOG.info("Token not found for " + st)
          sender ! ExecuteFailed(st.client, "No token found for " + st)
      }
    case t:StoreToken =>
      tokenRepository ! t

    case e@_ => LOG.info("Not handling:" + e)
  }

  private def moveProcess(token: Token, op: MoveToken) = {
    val processContext: ProcessContext = ProcessContext(token, scala.collection.mutable.Map(op.parameters.toSeq: _*), op.client)
    sender ! MoveInstance(processContext, op.transitionId)
  }


  private def startProcess(token: Token, op: StartToken) = {
    val processContext: ProcessContext = ProcessContext(token, scala.collection.mutable.Map(op.parameters.toSeq: _*), op.client)
    sender ! StartInstance(processContext, op.transitionId)
  }
}

object TokenFactory {
  def props(tokenRepository: ActorRef): Props = Props(classOf[TokenFactory], tokenRepository)
}
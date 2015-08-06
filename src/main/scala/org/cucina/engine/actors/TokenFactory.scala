package org.cucina.engine.actors

import org.cucina.engine.definition.Token
import org.cucina.engine.repository._
import org.slf4j.LoggerFactory
import akka.actor.Actor
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.actor.ActorRef
import org.cucina.engine.{ExecuteFailed, ProcessContext}

trait TokenRequest {
  val processDefinitionName: String
  val domainObject: Object
  val transitionId: String
  val parameters: Map[String, Object]
  val client: ActorRef
}

case class StartToken(processDefinitionName: String, domainObject: Object, transitionId: String = null, parameters: Map[String, Object], client: ActorRef)
  extends TokenRequest

case class MoveToken(processDefinitionName: String, domainObject: Object, transitionId: String, parameters: Map[String, Object], client: ActorRef)
  extends TokenRequest

case class TokenNotFound(op: TokenRequest)

case class TokenResult(token: Token, op: TokenRequest)

/**
 * @author levinev
 */
class TokenFactory(processInstanceFactory: ActorRef, tokenRepository:ActorRef = null) extends Actor {
  private[this] val LOG = LoggerFactory.getLogger(getClass)

  def receive = {
    case st: StartToken =>
      require(st.domainObject != null, "The 'domainObject' cannot be null.")
      // call to tokenRepository to find an existing one for the object
      tokenRepository ! new FindByDomain(st)
    case mt: MoveToken =>
      require(mt.domainObject != null, "The 'domainObject' cannot be null.")
      require(mt.transitionId != null, "The 'transitionId' cannot be null.")
      // call to tokenRepository to find an existing one for the object
      tokenRepository ! new FindByDomain(mt)

    case TokenResult(token: Token, op: TokenRequest) =>
      op match {
        case st: StartToken =>
          LOG.info("Found existing token:" + token)
          // TODO should it carry on if a token exists or fail here? client policy?
          // startProcess(token, st)
          sender ! new ExecuteFailed(failure = "Cannot start a process, one exists already")
        case st: MoveToken =>
          LOG.info("Found existing token:" + token)
          moveProcess(token, st)
      }

    case TokenNotFound(op: TokenRequest) =>
      op match {
        case st: StartToken =>
          LOG.info("Creating new token")
          startProcess(new Token(st.domainObject), st)
        case st: MoveToken =>
          LOG.info("Token not found for " + st)
          sender ! new ExecuteFailed(failure = "No token found for " + st)
      }

    case e@_ => LOG.info("Not handling:" + e)
  }

  private def moveProcess(token: Token, op: MoveToken) = {
    val processContext: ProcessContext = new ProcessContext(token, scala.collection.mutable.Map(op.parameters.toSeq: _*), op.client)
    processInstanceFactory ! new StartInstance(op.processDefinitionName, processContext, op.transitionId)
  }


  private def startProcess(token: Token, op: StartToken) = {
    val processContext: ProcessContext = new ProcessContext(token, scala.collection.mutable.Map(op.parameters.toSeq: _*), op.client)
    processInstanceFactory ! new StartInstance(op.processDefinitionName, processContext, op.transitionId)
  }
}

object TokenFactory {
  def props(processInstanceFactory:ActorRef):Props = Props(classOf[TokenFactory], processInstanceFactory, null)
}
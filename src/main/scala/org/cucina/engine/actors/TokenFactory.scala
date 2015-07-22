package org.cucina.engine.actors

import org.cucina.engine.definition.Token
import org.cucina.engine.definition.ProcessDefinition
import org.cucina.engine.repository._
import org.slf4j.LoggerFactory
import akka.actor.Actor
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.actor.ActorRef
import org.cucina.engine.ProcessContext
import org.cucina.engine.ProcessSession

trait TokenRequest {
  val processDefinition: ProcessDefinition
  val domainObject: Object
  val transitionId: String
  val parameters: Map[String, Object]
  val client: ActorRef
}
case class StartProcess(processDefinition: ProcessDefinition, domainObject: Object, transitionId: String, parameters: Map[String, Object], client: ActorRef) extends TokenRequest
case class TokenNotFound(op: TokenRequest)
case class TokenResult(token: Token, op: TokenRequest)

/**
 * @author levinev
 */
class TokenFactory extends Actor {
  private[this] val LOG = LoggerFactory.getLogger(getClass())

  def receive = {
    case op @ StartProcess(processDefinition, domainObject, transitionId, parameters, client) => {
      require(processDefinition != null, "The 'processDefinition' parameter cannot be null.")
      require(domainObject != null, "The 'domainObject' parameter cannot be null.")

      val tokenRepository = context.actorOf(Props[TokenRepository], name = "tokenRepository")
      // call to tokenRepository to find an existing one for the object
      tokenRepository ! new FindByDomain(op)
    }
    case TokenResult(token: Token, op: TokenRequest) =>
      postProcess(token, op)

    case TokenNotFound(op: TokenRequest) => {
      val token = new Token(op.domainObject, op.processDefinition)
      postProcess(token, op)
    }

  }
  private def postProcess(token: Token, op: TokenRequest) = {
    val processContext: ProcessContext = new ProcessContext(token, scala.collection.mutable.Map(op.parameters.toSeq: _*))

    // Processing the state
    val start = op.processDefinition.startState

    start.enter(null, processContext)
    start.leave(ProcessSession.findTransition(token, op.transitionId), processContext)
    op.client ! new TokenResult(token, op)
  }
}
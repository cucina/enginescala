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

case class StartToken(processDefinitionName: String, domainObject: Object, transitionId: String, parameters: Map[String, Object], client: ActorRef)
  extends TokenRequest
case class TokenNotFound(op: TokenRequest)
case class TokenResult(token: Token, op: TokenRequest)

/**
 * @author levinev
 */
class TokenFactory extends Actor {
  private[this] val LOG = LoggerFactory.getLogger(getClass())
  private[this] val target = context.actorOf(Props[ProcessInstanceFactory], "processInstanceFactory")

  def receive = {
    case st @ StartToken(_, _, _, _, _) => {
      require(st.domainObject != null, "The 'domainObject' parameter cannot be null.")
      val tokenRepository = context.actorOf(Props[TokenRepository], name = "tokenRepository")
      // call to tokenRepository to find an existing one for the object
      tokenRepository ! new FindByDomain(st)
    }
    case TokenResult(token: Token, op: TokenRequest) =>
      op match {
        case st @ StartToken(_, _, _, _, _) => {
          postProcess(token, st)
        }
      }

    case TokenNotFound(op: TokenRequest) => {
      op match {
        case st @ StartToken(_, _, _, _, _) => {
          postProcess(new Token(st.domainObject), st)
        }
      }
    }

  }
  private def postProcess(token: Token, op: StartToken) = {
    val processContext: ProcessContext = new ProcessContext(token, scala.collection.mutable.Map(op.parameters.toSeq: _*), op.client)
    target ! new StartInstance(op.processDefinitionName, processContext, op.transitionId)
  }
}
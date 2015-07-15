package org.cucina.engine.actors

import org.cucina.engine.definition.Token
import org.cucina.engine.definition.ProcessDefinition
import org.cucina.engine.repository._
import org.slf4j.LoggerFactory
import akka.actor.Actor
import akka.actor.Props
import akka.actor.actorRef2Scala

sealed trait TokenMsg
case class TokenResult(token: Token) extends TokenMsg
case class TokenNotFound(processDefinition: ProcessDefinition, domainObject: Object) extends TokenMsg

/**
 * @author levinev
 */
class TokenFactory extends Actor {
  private[this] val LOG = LoggerFactory.getLogger(getClass())

  case class CreateToken(processDefinition: ProcessDefinition, domainObject: Object)

  def receive = {
    case CreateToken(processDefinition, domainObject) =>
      createToken(processDefinition, domainObject)
    case t @ TokenResult(token) =>
      sender ! t
    case TokenNotFound(processDefinition, domainObject) =>
      sender ! new TokenResult(new Token(domainObject, processDefinition))
  }
  
  private def createToken(processDefinition: ProcessDefinition, domainObject: Object) = {
    require(processDefinition != null, "The 'processDefinition' parameter cannot be null.")
    require(domainObject != null, "The 'domainObject' parameter cannot be null.")

    val tokenRepository = context.actorOf(Props[TokenRepository], name = "tokenRepository")
    // call to tokenRepository to find an existing one for the object
    tokenRepository ! new FindByDomain(processDefinition, domainObject)
  }
}
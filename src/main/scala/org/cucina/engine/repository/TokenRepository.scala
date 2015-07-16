package org.cucina.engine.repository

import org.cucina.engine.definition.Token
import akka.actor.Actor
import org.cucina.engine.actors.TokenMsg
import org.cucina.engine.definition.ProcessDefinition
import org.cucina.engine.actors.TokenNotFound
import akka.actor.ActorRef

/**
 * @author levinev
 */

sealed trait TokenMessage
case class FindByDomain(processDefintion: ProcessDefinition, domain: Object, client: ActorRef) extends TokenMessage

class TokenRepository extends Actor {

  def receive = {
    case FindByDomain(defin, domain, client) => {
      // if found return TokenResult
      // client ! TokenResult(token)
      // else
      sender ! TokenNotFound(defin, domain)
    }
  }
}
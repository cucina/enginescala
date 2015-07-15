package org.cucina.engine.repository

import org.cucina.engine.definition.Token
import akka.actor.Actor
import org.cucina.engine.actors.TokenMsg
import org.cucina.engine.definition.ProcessDefinition
import org.cucina.engine.actors.TokenNotFound

/**
 * @author levinev
 */

sealed trait TokenMessage
case class FindByDomain(processDefintion: ProcessDefinition, domain: Object) extends TokenMessage

class TokenRepository extends Actor {

  def receive = {
    case FindByDomain(defin, domain) =>
      sender ! findByDomain(defin, domain)
  }

  def findByDomain(definition: ProcessDefinition, domain: Object): TokenMsg = {
    // if found return TokenResult
    // else
    TokenNotFound(definition, domain)
  }
}
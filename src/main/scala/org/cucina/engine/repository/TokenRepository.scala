package org.cucina.engine.repository

import org.cucina.engine.actors.TokenNotFound
import org.cucina.engine.actors.TokenRequest

import akka.actor.Actor
import akka.actor.actorRef2Scala

/**
 * @author levinev
 */

case class FindByDomain(op:TokenRequest)

class TokenRepository extends Actor {

  def receive = {
    case FindByDomain(op) => {
      // if found return TokenResult
      // client ! TokenResult(token, op)
      // else
      sender ! TokenNotFound(op)
    }
  }
}
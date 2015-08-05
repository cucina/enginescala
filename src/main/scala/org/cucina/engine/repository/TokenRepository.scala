package org.cucina.engine.repository

import org.cucina.engine.actors.{TokenResult, TokenNotFound, TokenRequest}
import akka.actor.Actor
import akka.actor.actorRef2Scala
import org.cucina.engine.definition.Token

/**
 * @author levinev
 */

case class FindByDomain(op:TokenRequest)
case class StoreToken(token:Token)

trait TokenRepository extends Actor {

  def receive = {
    case FindByDomain(op) =>
      findByDomain(op) match {
        case None =>
          sender ! TokenNotFound(op)
        case Some(t) =>
          sender ! TokenResult(t, op)
      }
    case StoreToken(t) =>
      store(t)
  }

  def findByDomain(op:TokenRequest):Option[Token]
  def store(token:Token)
}
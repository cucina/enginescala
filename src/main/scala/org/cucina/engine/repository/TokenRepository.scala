package org.cucina.engine.repository

import org.cucina.engine.actors.{TokenResult, TokenNotFound, TokenRequest}
import akka.actor.{ActorRef, Actor, actorRef2Scala}
import org.cucina.engine.definition.Token

/**
 * @author levinev
 */

case class FindByDomain(op:TokenRequest, requestor:ActorRef)
case class StoreToken(token:Token)

trait TokenRepository extends Actor {

  def receive = {
    case FindByDomain(op, requestor) =>
      findByDomain(op) match {
        case None =>
          requestor forward TokenNotFound(op)
        case Some(t) =>
          requestor forward TokenResult(t, op)
      }
    case StoreToken(t) =>
      store(t)
  }

  def findByDomain(op:TokenRequest):Option[Token]
  def store(token:Token)
}
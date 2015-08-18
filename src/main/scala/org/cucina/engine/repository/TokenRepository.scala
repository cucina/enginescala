package org.cucina.engine.repository

import org.cucina.engine.actors.{TokenResult, TokenNotFound, TokenRequest}
import akka.actor.{ActorRef, Actor, actorRef2Scala}
import org.cucina.engine.definition.Token
import org.slf4j.LoggerFactory

/**
 * @author levinev
 */

case class FindByDomain(op:TokenRequest, requestor:ActorRef)
case class StoreToken(token:Token)

trait TokenRepository extends Actor {
protected val LOG = LoggerFactory.getLogger(getClass)
  def receive = {
    case FindByDomain(op, requestor) =>
      LOG.info("Finding for:" + op)
      findByDomain(op) match {
        case None =>
          LOG.info("Failed to find")
          requestor forward TokenNotFound(op)
        case Some(t) =>
          LOG.info("Found token")
          requestor forward TokenResult(t, op)
      }
    case StoreToken(t) =>
      store(t)
  }

  def findByDomain(op:TokenRequest):Option[Token]
  def store(token:Token)
}
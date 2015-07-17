package org.cucina.engine.actors

import org.cucina.engine.definition.CheckDescriptor
import org.cucina.engine.definition.Token
import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration.Duration
import scala.concurrent.Await

/**
 * @author levinev
 */
case class CheckRequest(token:Token)

class ProcessDriver extends Actor {
  
  def test(token:Token, check:CheckDescriptor):Boolean = {
    val che = context.actorOf(Props.apply(Class.forName(check.className)), check.name)
    implicit val timeout = Timeout(Duration.create(5, "seconds"))
    val future = che ? new CheckRequest(token)
    Await.result(future, timeout.duration).asInstanceOf[Boolean]
  }
  
  def receive = {
    case _ =>
  }
}
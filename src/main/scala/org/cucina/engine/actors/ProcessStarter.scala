package org.cucina.engine.actors

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import org.cucina.engine.ProcessSession
import org.cucina.engine.ProcessContext
import scala.collection.mutable.Map

/**
 * A throwaway actor created for a request self-destructing after use
 * @author levinev
 *
 */
class ProcessStarter extends Actor {
  import context._
  private val target = context.actorOf(Props[TokenFactory], "tokenFactory")

  def receive = start
  private def start: Receive = {
    case sp @ StartProcess => {
      target ! sp
      context become waitForToken(sender)
    }
  }

  private def waitForToken(origin: ActorRef): Receive = {
    case TokenResult(token, op) => {
      val startState = op.processDefinition.startState
      val processContext = new ProcessContext(token, Map(op.parameters.toSeq: _*))

      startState.enter(null, processContext);
      startState.leave(ProcessSession.findTransition(token, op.transitionId), processContext);
//      tokenRepository ! Save(token)
      origin ! token
      stop(self)
    }
    case re @ _ =>
      origin ! re
      stop(self)
  }
}
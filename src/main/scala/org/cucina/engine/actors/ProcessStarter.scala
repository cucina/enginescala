package org.cucina.engine.actors

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import org.cucina.engine.ProcessSession
import org.cucina.engine.ProcessContext
import org.cucina.engine.definition.ProcessDefinition

/**
 * @author levinev
 *
 */
trait TokenRequest {
  val domainObject: Object
  val transitionId: String
  val parameters: Map[String, Object]
  val client: ActorRef
}

case class StartProcess(processDefinitionName: String, domainObject: Object, transitionId: String, parameters: Map[String, Object])
  
class ProcessStarter extends Actor {
  import context._
  private val target = context.actorOf(Props[TokenFactory], "tokenFactory")

  def receive = start
  private def start: Receive = {
    case sp @ StartProcess(_, _, _, _) => {
      target ! new StartToken(sp.processDefinitionName, sp.domainObject, sp.transitionId, sp.parameters, sender())
    }
  }
}
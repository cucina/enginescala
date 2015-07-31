package org.cucina.engine.definition.reader

import akka.actor.{ActorRef, Actor}
import org.cucina.engine.actors.{AddProcessDefinition, DefinitionRegistry}
import org.cucina.engine.definition.ProcessDefinition

/**
 * Created by levinev on 31/07/2015.
 */
class FakeDefinitionReader(registry:ActorRef, processDefinition:ProcessDefinition) extends Actor {
  override def preStart() = {
    registry ! new AddProcessDefinition(processDefinition)
  }

  def receive = Actor.emptyBehavior

}

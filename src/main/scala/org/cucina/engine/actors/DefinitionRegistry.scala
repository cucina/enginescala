package org.cucina.engine.actors

import akka.actor.Actor
import scala.collection.mutable.Map
import org.cucina.engine.definition.ProcessDefinition


/**
 * @author levinev
 */
case class FindDefinition(name: String, nested: Object)

case class AddProcessDefinition(processDefinition: ProcessDefinition)

class DefinitionRegistry extends Actor {
  val definitions: Map[String, ProcessDefinition] = Map[String, ProcessDefinition]()

  def receive = {
    case FindDefinition(name, nested) =>
      sender() ! new ProcessDefinitionWrap(definitions.getOrElse(name, null), nested)
    case AddProcessDefinition(pd) =>
      definitions += (pd.id -> pd)
    case _ =>
  }
}
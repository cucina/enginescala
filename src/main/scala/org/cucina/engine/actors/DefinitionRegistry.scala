package org.cucina.engine.actors

import akka.actor.Actor
import org.cucina.engine.definition.ProcessDefinition
import scala.collection.mutable.Map
import org.cucina.engine.definition.ProcessDefinition


/**
 * @author levinev
 */
case class FindDefinition(name:String, nested:Object)
case class ProcessDefinitionWrap(processDefinition: ProcessDefinition, nested: Object)


class DefinitionRegistry extends Actor {
  val definitions:Map[String, ProcessDefinition] = Map[String, ProcessDefinition]()
  
  def receive = {
    case FindDefinition(name, nested) => {
      sender() ! new ProcessDefinitionWrap(definitions.getOrElse(name, null), nested)
    }
    case _ =>
  }
}
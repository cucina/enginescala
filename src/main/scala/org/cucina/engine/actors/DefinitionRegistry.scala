package org.cucina.engine.actors

import akka.actor.{ActorRef, Actor}
import org.cucina.engine.ProcessDefinitionWrap
import org.slf4j.LoggerFactory
import scala.collection.mutable.Map
import org.cucina.engine.definition.ProcessDefinition


/**
 * @author levinev
 */
case class FindDefinition(name: String, nested: Object)

case class AddProcessDefinition(processDefinition: ProcessDefinition)

class DefinitionRegistry extends Actor {
  private[this] val LOG = LoggerFactory.getLogger(getClass)
  val definitions: Map[String, ProcessDefinition] = Map[String, ProcessDefinition]()

  def receive = {
    case FindDefinition(name, nested) =>
      sender() ! new ProcessDefinitionWrap(definitions.get(name), nested)
    case AddProcessDefinition(pd) =>
      definitions += (pd.id -> pd)
      LOG.info("Added definition " + pd)
    case _ =>
  }
}
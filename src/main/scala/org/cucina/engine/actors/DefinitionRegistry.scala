package org.cucina.engine.actors

import akka.actor.{ActorRef, Actor}
import org.cucina.engine.{NestedTuple, ExecuteFailed, ProcessDefinitionWrap}
import org.slf4j.LoggerFactory
import scala.collection.mutable.Map
import org.cucina.engine.definition.ProcessDefinition


/**
 * @author levinev
 */
case class FindDefinition(name: String, nested: NestedTuple)

case class AddProcessDefinition(processDefinition: ProcessDefinition)

// TODO make it persistable by plugging
class DefinitionRegistry extends Actor {
  private[this] val LOG = LoggerFactory.getLogger(getClass)
  val definitions: Map[String, ProcessDefinition] = Map[String, ProcessDefinition]()

  def receive = {
    case FindDefinition(name, nested) =>
      definitions.get(name) match {
        case Some(d) =>
          sender ! ProcessDefinitionWrap(d, nested)
        case None =>
          sender ! ExecuteFailed(nested.client, "Failed to find definition '" + name + "'")
      }

    case AddProcessDefinition(pd) =>
      definitions += (pd.id -> pd)
      LOG.info("Added definition " + pd)
    case _ =>
  }
}
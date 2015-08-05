package org.cucina.engine.actors

import org.cucina.engine.definition.ProcessDefinition
import org.slf4j.LoggerFactory

import scala.collection.mutable.Map
import org.cucina.engine.ProcessContext
import akka.actor.{Terminated, Actor, ActorRef, Props}


/**
 * @author levinev
 */
case class StartInstance(processname: String, processContext: ProcessContext, transitionId: String = null)

case class MoveInstance(processname: String, processContext: ProcessContext, transitionId: String)

case class ProcessDefinitionWrap(processDefinition: ProcessDefinition, nested: Object)

trait ProcessInstanceProvider {
  def props(definition: ProcessDefinition): Props = ProcessInstance.props(definition)
}

class ProcessInstanceFactory(processRegistry: ActorRef, processInstanceProvider: ProcessInstanceProvider = new ProcessInstanceProvider {}) extends Actor {
  private[this] val LOG = LoggerFactory.getLogger(getClass)
  val instances: Map[String, ActorRef] = Map[String, ActorRef]()


  def receive = {
    case si:StartInstance =>
      LOG.info("StartInstance " + si)
      processRegistry ! FindDefinition(si.processname, si)

    case si:MoveInstance =>
      LOG.info("MoveInstance " + si)
      processRegistry ! FindDefinition(si.processname, si)

    case ProcessDefinitionWrap(pd, si) =>
      if (pd != null)
        si match {
          case StartInstance(_, processContext, transitionId) => {
            LOG.info("Starting instance " + pd)
            // ProcessInstance should be a root of it own actors
            def target: ActorRef = instances.getOrElseUpdate(pd.id, context.actorOf(processInstanceProvider.props(pd)))
            context watch target
            target ! new ExecuteStart(processContext, transitionId)
          }
          case MoveInstance(_, processContext, transitionId) => {
            LOG.info("Existing instance " + pd)
            // ProcessInstance should be a root of it own actors
            def target: ActorRef = instances.getOrElseUpdate(pd.id, context.actorOf(processInstanceProvider.props(pd)))
            context watch target
            target ! new ExecuteTransition(processContext, transitionId)
          }
        }

    case Terminated(child) =>
      // remove dead reference to re-create upon next request
      LOG.warn("ProcessInstance " + child + " had died")
      instances.retain((k, v) => v != child)

    case e@_ => LOG.debug("Not handling " + e)
  }
}

object ProcessInstanceFactory {
  def props(processRegistry: ActorRef): Props = Props(classOf[ProcessInstanceFactory], processRegistry, null)
}
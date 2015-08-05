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

trait ProcessInstanceProvider{
  def props(definition: ProcessDefinition):Props = ProcessInstance.props(definition)
}

class ProcessInstanceFactory(processRegistry: ActorRef) extends Actor {
  this: ProcessInstanceProvider =>

  private[this] val LOG = LoggerFactory.getLogger(getClass)
  val instances: Map[String, ActorRef] = Map[String, ActorRef]()


  def receive = {
    case si@StartInstance(processName, _, _) => processRegistry ! FindDefinition(processName, si)

    case si@MoveInstance(processName, _, _) => processRegistry ! FindDefinition(processName, si)

    case ProcessDefinitionWrap(pd, si) => {
      if (pd != null)
        si match {
          case StartInstance(_, processContext, transitionId) => {
            // ProcessInstance should be a root of it own actors
            def target: ActorRef = instances.getOrElseUpdate(pd.id, context.actorOf(props(pd)))
            context watch target
            target ! new ExecuteStart(processContext, transitionId)
          }
          case MoveInstance(_, processContext, transitionId) => {
            // ProcessInstance should be a root of it own actors
            def target: ActorRef = instances.getOrElseUpdate(pd.id, context.actorOf(props(pd)))
            context watch target
            target ! new ExecuteTransition(processContext, transitionId)
          }
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
  def props(processRegistry: ActorRef):Props = Props(classOf[ProcessInstanceFactory], processRegistry)
}
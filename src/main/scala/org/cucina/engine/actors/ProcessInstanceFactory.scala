package org.cucina.engine.actors

import scala.collection.mutable.Map
import org.cucina.engine.ProcessContext
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import org.cucina.engine.definition.ProcessDefinition

/**
 * @author levinev
 */
case class StartInstance(processname: String, processContext: ProcessContext, transitionId: String)

class ProcessInstanceFactory extends Actor {
  val processRegistry = context.actorOf(Props[DefinitionRegistry])
  val instances: Map[String, ActorRef] = Map[String, ActorRef]()
  def receive = {
    case si @ StartInstance(processName, processContext, transitionId) => processRegistry ! FindDefinition(processName, si)

    case ProcessDefinitionWrap(pd, si) => {
      si match {
        case StartInstance(_, processContext, transitionId) => {
          // ProcessInstance should be a root of it own actors
          def target: ActorRef = instances.getOrElseUpdate(pd.id, context.system.actorOf(ProcessInstance.props(pd)))
          target ! new ExecuteStart(processContext, transitionId)
        }
      }
    }
    case _ =>
  }
}
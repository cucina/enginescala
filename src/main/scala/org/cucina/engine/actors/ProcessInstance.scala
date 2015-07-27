package org.cucina.engine.actors

import org.cucina.engine.ProcessContext
import org.cucina.engine.definition.ProcessDefinition
import org.slf4j.LoggerFactory
import akka.actor._
import scala.collection.mutable.Map
import org.cucina.engine.definition.StateDescriptor
import scala.concurrent.Await
import akka.util.Timeout
import scala.concurrent.duration._

/**
 * @author levinev
 */
case class ExecuteStart(processContext: ProcessContext, transitionId: String)

class ProcessInstance(processDefinition: ProcessDefinition)
  extends Actor {
  private[this] val LOG = LoggerFactory.getLogger(getClass())
  val states = Map[String, ActorRef]()

  private def getState(stateD: StateDescriptor) = states getOrElseUpdate(stateD.id,
    try {
      implicit val resolveTimeout = Timeout(500 millis)
      val actorRef = Await.result(context.actorSelection(stateD.id).resolveOne(), resolveTimeout.duration)
      LOG.info("Located state:" + actorRef)
      actorRef
    } catch {
      case e: ActorNotFound => {
        val c = context actorOf(StateActor.props(stateD.id, stateD.enterOperations, stateD.leaveOperations, stateD.allTransitions), stateD.id)
        context watch c
        LOG.info("created state " + c)
        c
      }
    })


  def receive = {
    case ExecuteStart(pc, trid) => {
      println(self)
      val start = processDefinition.startState
      LOG.info("before")
      val sactor = getState(start)
      sactor ! new EnterState(trid, pc)
      LOG.info("after")
    }

    case _ => LOG.info("Unknown")
  }
}

object ProcessInstance {
  def props(definition: ProcessDefinition): Props = {
    Props(classOf[ProcessInstance], definition)
  }
}
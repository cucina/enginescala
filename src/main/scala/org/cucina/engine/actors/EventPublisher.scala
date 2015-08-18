package org.cucina.engine.actors

import akka.actor.{Props, ActorContext, ActorRef, Actor}
import org.cucina.engine.ProcessContext
import org.slf4j.LoggerFactory

/**
 * Created by levinev on 28/07/2015.
 */

// TODO have a single class using a type of event in constructor

class EventPublisher(val listeners: Seq[String], buildEvent: ProcessContext => ProcessEvent)
  extends StackElementActor {
  private val LOG = LoggerFactory.getLogger(getClass)
  lazy val listActors: Seq[ActorRef] = {
    val seq: Seq[Option[ActorRef]] = listeners.map(l => findActor(l))
    for (Some(ar) <- seq) yield ar
  }

  override def preStart(): Unit = {
    listActors
  }

  def execute(processContext: ProcessContext): StackElementExecuteResult = {
    LOG.info("Logging:" + processContext)
    listActors.foreach(l => l ! buildEvent(processContext))
    new StackElementExecuteResult(true, processContext)
  }
}

object EnterPublisher {
  val buildEvent = (pc:ProcessContext) => new EnterEvent(pc)
  def props(listeners: Seq[String]): Props = Props(classOf[EventPublisher], listeners, buildEvent)
}

object LeavePublisher {
  val buildEvent = (pc:ProcessContext) => new LeaveEvent(pc)
  def props(listeners: Seq[String]): Props = Props(classOf[EventPublisher], listeners, buildEvent)
}

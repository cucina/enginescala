package org.cucina.engine.actors

import akka.actor.{ActorContext, ActorRef, Actor}
import org.cucina.engine.ProcessContext
import org.cucina.engine.actors.support.ActorFinder

/**
 * Created by levinev on 28/07/2015.
 */
case class EnterEvent(processContext: ProcessContext)

abstract class EventPublisher(val listeners: Seq[String]) extends StackElementActor {
  lazy val listActors: Seq[ActorRef] = {
    val seq: Seq[Option[ActorRef]] = listeners.map(l => findActor(l))
    for (Some(ar) <- seq) yield ar
  }
}

class EnterPublisher(listeners: Seq[String])
  extends EventPublisher(listeners) {

  override def preStart(): Unit = {
    listActors
  }

  def execute(processContext: ProcessContext): StackElementExecuteResult = {
    listActors.foreach(l => l ! new EnterEvent(processContext))
    new StackElementExecuteResult(true, processContext)
  }
}

case class LeaveEvent(processContext: ProcessContext)

class LeavePublisher(listeners: Seq[String])
  extends EventPublisher(listeners) {

  override def preStart(): Unit = {
    listActors
  }

  def execute(processContext: ProcessContext): StackElementExecuteResult = {
    listActors.foreach(l => l ! new EnterEvent(processContext))
    new StackElementExecuteResult(true, processContext)
  }
}
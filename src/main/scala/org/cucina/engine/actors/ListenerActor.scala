package org.cucina.engine.actors

import akka.actor.Actor
import org.cucina.engine.ProcessContext
import org.slf4j.LoggerFactory

/**
 * Created by levinev on 11/08/2015.
 */

case class EnterEvent(processContext: ProcessContext)

case class LeaveEvent(processContext: ProcessContext)

trait ListenerActor
  extends Actor {
  def receive = {
    case EnterEvent(pc) =>
    case LeaveEvent(pc) =>
  }

  def processEnter(pc: ProcessContext)

  def processLeave(pc: ProcessContext)
}

class LoggingListenerActor extends ListenerActor {
  private[this] val LOG = LoggerFactory.getLogger(getClass)

  def processEnter(pc: ProcessContext) = {
    LOG.debug("Entered:" + pc)
  }

  def processLeave(pc: ProcessContext) = {
    LOG.debug("Left:" + pc)
  }
}

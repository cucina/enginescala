package org.cucina.engine.actors

import akka.actor.Actor
import org.cucina.engine.ProcessContext
import org.slf4j.LoggerFactory

/**
 * Created by levinev on 03/08/2015.
 */
class BlankOperationActor extends Actor {
  private[this] val LOG = LoggerFactory.getLogger(getClass)
  def receive = {
    case pc:ProcessContext => sender ! new ProcessResult(true, pc)
    case other => LOG.warn("Unknown request " + other)
  }
}

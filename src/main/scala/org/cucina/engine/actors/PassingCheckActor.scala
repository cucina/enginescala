package org.cucina.engine.actors

import akka.actor.Actor
import akka.actor.Actor.Receive
import org.cucina.engine.ProcessContext
import org.cucina.engine.definition.CheckDescriptor

/**
 * Created by levinev on 28/07/2015.
 */
case class CheckRequest(processContext: ProcessContext, remains: Seq[CheckDescriptor])

class PassingCheckActor extends Actor {
  override def receive: Receive = {
    case CheckRequest(pc, re) => {
      sender ! CheckPassed(pc ,re)
    }
    case _ =>
  }
}

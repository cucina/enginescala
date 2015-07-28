package org.cucina.engine.actors

import akka.actor.Actor
import org.cucina.engine.ProcessContext

/**
 * Created by levinev on 28/07/2015.
 */
class EnterPublisher extends StackElementActor {
  def execute(processContext: ProcessContext): StackElementExecuteResult = {
    // TODO figure how to find listeners
    new StackElementExecuteResult(true)
  }
}

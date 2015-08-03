package org.cucina.engine.actors

import akka.actor.Actor

/**
 * Created by levinev on 03/08/2015.
 */
class BlankOperationActor extends Actor {
  def receive = {
    Actor.emptyBehavior
  }
}

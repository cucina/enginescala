package org.cucina.engine.actors.support

import akka.actor.ActorRef
import org.cucina.engine.actors.StackElementActor
import org.cucina.engine.definition.StackableElementDescriptor

/**
 * Allows to wrap an existing ActorRef
 *
 * Created by levinev on 04/08/2015.
 */
class ActorRefStackableElementDescriptor(actorRef: ActorRef, val className:String = classOf[StackElementActor].getName)
  extends StackableElementDescriptor {
  val name = {
    actorRef.path.name
  }
}

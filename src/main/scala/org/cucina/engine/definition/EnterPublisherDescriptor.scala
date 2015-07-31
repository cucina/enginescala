package org.cucina.engine.definition

import akka.actor.Props
import org.cucina.engine.actors.EnterPublisher

/**
 * Created by levinev on 28/07/2015.
 */
class EnterPublisherDescriptor(val className: String = classOf[EnterPublisher].getName,
                               val name: String = "enterPublisher",
                               val params: Seq[Any] = null) extends StackableElementDescriptor {
  def props = Props(Class.forName(className), params)
}


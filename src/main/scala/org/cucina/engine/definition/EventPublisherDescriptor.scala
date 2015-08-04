package org.cucina.engine.definition

import akka.actor.Props
import org.cucina.engine.actors.{LeavePublisher, EnterPublisher}

/**
 * Created by levinev on 28/07/2015.
 */
case class EnterPublisherDescriptor(val listeners: Seq[String],
                                    val name: String = "enterPublisher",
                                    val className: String = classOf[EnterPublisher].getName) extends StackableElementDescriptor {
  override def props = Props(Class.forName(className), listeners)
}

case class LeavePublisherDescriptor(val listeners: Seq[String],
                                    val name: String = "leavePublisher",
                                    val className: String = classOf[LeavePublisher].getName) extends StackableElementDescriptor {
  override  def props = Props(Class.forName(className), listeners)
}

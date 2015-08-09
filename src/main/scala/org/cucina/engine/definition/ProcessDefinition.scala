package org.cucina.engine.definition

import akka.actor.Props
import org.cucina.engine.actors._


/**
 * @author levinev
 */

case class ProcessDefinition(val states: Seq[StateDescriptor], val startState: String, description: String, val id: String)

trait ProcessElementDescriptor {
  val name: String
  val className: Option[String]

  def props: Props = Props(Class.forName(className.get), name)

  override def toString: String = {
    val sb = new StringBuilder()
    sb.append("className='").append(className).append("' name='").append(name).append("'").toString()
  }
}

case class OperationDescriptor(name: String, className: Option[String] = Some(classOf[BlankOperationActor].getName), arguments: Option[List[String]] = None)
  extends ProcessElementDescriptor

case class CheckDescriptor(name: String, className: Option[String] = Some(classOf[PassingCheckActor].getName), arguments: Option[List[String]] = None)
  extends ProcessElementDescriptor

case class TransitionDescriptor(name: String, output: String,
                                leaveOperations: Seq[OperationDescriptor] = List(),
                                checks: Seq[CheckDescriptor] = List(),
                                className: Option[String] = None) extends ProcessElementDescriptor {
  /// Factory method, allows to plugin allows to plugin alternative transition implementations
  override def props: Props = Props(Class.forName(className.getOrElse(classOf[TransitionActor].getName)), name, output, leaveOperations, checks)
}

case class StateDescriptor(name: String,
                           transitions: Seq[TransitionDescriptor],
                           enterPublisher: Option[EnterPublisherDescriptor] = None,
                           leavePublisher: Option[LeavePublisherDescriptor] = None,
                           enterOperations: Option[Seq[OperationDescriptor]] = Some(List()),
                           leaveOperations: Option[Seq[OperationDescriptor]] = Some(List()),
                           className: Option[String] = None)
  extends ProcessElementDescriptor {
  override def props: Props = Props(Class.forName(className.getOrElse(classOf[StateActor].getName)), name, transitions,
    enterPublisher getOrElse (EnterPublisherDescriptor(List())), leavePublisher getOrElse (LeavePublisherDescriptor(List())),
    enterOperations.get, leaveOperations.get)
}

case class EnterPublisherDescriptor(listeners: Seq[String],
                                    val name: String = "enterPublisher",
                                    className: Option[String] = None) extends ProcessElementDescriptor {
  override def props = Props(Class.forName(className.getOrElse(classOf[EnterPublisher].getName)), listeners)
}

case class LeavePublisherDescriptor(listeners: Seq[String],
                                    val name: String = "leavePublisher",
                                    className: Option[String] = None) extends ProcessElementDescriptor {
  override def props = Props(Class.forName(className.getOrElse(classOf[LeavePublisher].getName)), listeners)
}



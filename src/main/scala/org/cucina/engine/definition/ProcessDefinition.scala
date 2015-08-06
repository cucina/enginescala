package org.cucina.engine.definition

import akka.actor.Props
import org.cucina.engine.actors._


/**
 * @author levinev
 */

case class ProcessDefinition(val states: List[StateDescriptor], val startState: String, description: String, val id: String)

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

case class TransitionDescriptor(val name: String, output: String,
                                leaveOperations: Seq[OperationDescriptor] = List(),
                                checks: Seq[CheckDescriptor] = List(),
                                val className: Option[String] = Some(classOf[TransitionActor].getName)) extends ProcessElementDescriptor {
  /// Factory method, allows to plugin allows to plugin alternative transition implementations
  override def props: Props = Props(Class.forName(className.get), name, output, leaveOperations, checks)
}

case class StateDescriptor(name: String,
                           transitions: Seq[TransitionDescriptor],
                           enterPublisher: Option[EnterPublisherDescriptor] = None,
                           leavePublisher: Option[LeavePublisherDescriptor] = None,
                           enterOperations: Option[Seq[OperationDescriptor]] = Some(List()),
                           leaveOperations: Option[Seq[OperationDescriptor]] = Some(List()),
                           className: Option[String] = Some(classOf[StateActor].getName))
  extends ProcessElementDescriptor {
  override def props: Props = Props(Class.forName(className.get), name, transitions, enterPublisher getOrElse(null), leavePublisher getOrElse(null),
    enterOperations.get, leaveOperations.get)
}

case class EnterPublisherDescriptor(val listeners: Seq[String],
                                    val name: String = "enterPublisher",
                                    val className: Option[String] = Some(classOf[EnterPublisher].getName)) extends ProcessElementDescriptor {
  override def props = Props(Class.forName(className.get), 1)
}

case class LeavePublisherDescriptor(val listeners: Seq[String],
                                    val name: String = "leavePublisher",
                                    val className: Option[String] = Some(classOf[LeavePublisher].getName)) extends ProcessElementDescriptor {
  override def props = Props(Class.forName(className.get), listeners)
}



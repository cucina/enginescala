package org.cucina.engine.definition

import akka.actor.Props
import org.cucina.engine.actors._


/**
 * @author levinev
 */

case class ProcessDefinition(val states: Seq[StateDescriptor], val startState: String, description: String, val id: String) {
  def listTransitions(stateId: String): Option[Seq[String]] = {
    states.find(_.name == stateId) match {
      case Some(s) =>
        Some(s.transitions.map((td) => td.name))
      case None =>
        None
    }
  }
}

trait ProcessElementDescriptor {
  val name: String
  val className: Option[String]

  def props: Props = Props(Class.forName(className.get), name)

  override def toString: String = {
    val sb = new StringBuilder()
    sb.append("className='").append(className).append("' name='").append(name).append("'").toString()
  }
}

case class TransitionDescriptor(name: String, output: String,
                                leaveOperations: Seq[OperationDescriptor] = List(),
                                checks: Seq[CheckDescriptor] = List(),
                                className: Option[String] = None) extends ProcessElementDescriptor {
  /// Factory method, allows to plugin allows to plugin alternative transition implementations
  override def props: Props = Props(Class.forName(className.getOrElse(classOf[Transition].getName)), name, output, leaveOperations, checks)
}

case class StateDescriptor(name: String,
                           transitions: Seq[TransitionDescriptor],
                           listeners: Option[Seq[String]] = Some(List()),
                           enterOperations: Option[Seq[OperationDescriptor]] = Some(List()),
                           leaveOperations: Option[Seq[OperationDescriptor]] = Some(List()),
                           className: Option[String] = None,
                            parameters: Option[Map[String, String]] = None)
  extends ProcessElementDescriptor {
  override def props: Props = {
    parameters match {
      case Some(p) => Props(Class.forName(className.getOrElse(classOf[State].getName)), name, transitions,
        listeners.get, enterOperations.get, leaveOperations.get, p)
      case None => Props(Class.forName(className.getOrElse(classOf[State].getName)), name, transitions,
        listeners.get, enterOperations.get, leaveOperations.get)
    }
  }
}

/*
case class DecisionDescriptor(name: String,
                              transitions: Seq[TransitionDescriptor],
                              listeners: Option[Seq[String]] = Some(List()),
                              enterOperations: Option[Seq[OperationDescriptor]] = Some(List()),
                              leaveOperations: Option[Seq[OperationDescriptor]] = Some(List()),
                              className: Option[String] = None)
  extends ProcessElementDescriptor {
  override def props: Props = Props(Class.forName(className.getOrElse(classOf[Decision].getName)), name, transitions,
    listeners.get, enterOperations.get, leaveOperations.get)
}

case class JoinDescriptor(name: String,
                          transition: TransitionDescriptor,
                          listeners: Option[Seq[String]] = Some(List()),
                          enterOperations: Option[Seq[OperationDescriptor]] = Some(List()),
                          leaveOperations: Option[Seq[OperationDescriptor]] = Some(List()),
                          className: Option[String] = None)
  extends ProcessElementDescriptor {
  override def props: Props = Props(Class.forName(className.getOrElse(classOf[Join].getName)), name, transition,
    listeners.get, enterOperations.get, leaveOperations.get)
}

case class SplitDescriptor(name: String,
                           transition: TransitionDescriptor,
                           listeners: Option[Seq[String]] = Some(List()),
                           enterOperations: Option[Seq[OperationDescriptor]] = Some(List()),
                           leaveOperations: Option[Seq[OperationDescriptor]] = Some(List()),
                           className: Option[String] = None)
  extends ProcessElementDescriptor {
  override def props: Props = Props(Class.forName(className.getOrElse(classOf[Split].getName)), name, transition,
    listeners.get, enterOperations.get, leaveOperations.get)
}

case class SplitCollectionDescriptor(name: String,
                                     transition: TransitionDescriptor,
                                     listeners: Option[Seq[String]] = Some(List()),
                                     enterOperations: Option[Seq[OperationDescriptor]] = Some(List()),
                                     leaveOperations: Option[Seq[OperationDescriptor]] = Some(List()),
                                     className: Option[String] = None)
  extends ProcessElementDescriptor {
  override def props: Props = Props(Class.forName(className.getOrElse(classOf[SplitCollection].getName)), name, transition,
    listeners.get, enterOperations.get, leaveOperations.get)
}
*/

object StackElementDescriptor {
  def getStackElementClass(classN: String): Class[_] = {
    val clazz = Class.forName(classN)
    if (!classOf[StackElementActor].isAssignableFrom(clazz)) throw new IllegalArgumentException("className (" + classN
      + ") provided should be of a class extending StackElementActor")
    clazz
  }
}

case class OperationDescriptor(name: String, className: Option[String] = None, parameter: String = null)
  extends ProcessElementDescriptor {
  override def props: Props = Props(StackElementDescriptor.getStackElementClass(
    className.getOrElse(classOf[DelegatingStackActor].getName)), parameter)
}

case class CheckDescriptor(name: String, className: Option[String] = None, parameter: String = null)
  extends ProcessElementDescriptor {
  override def props: Props = Props(StackElementDescriptor.getStackElementClass(
    className.getOrElse(classOf[DelegatingStackActor].getName)), parameter)
}


case class EnterPublisherDescriptor(listeners: Seq[String],
                                    val name: String = "enterPublisher",
                                    className: Option[String] = None) extends ProcessElementDescriptor {
  override def props = EnterPublisher.props(listeners)
}

case class LeavePublisherDescriptor(listeners: Seq[String],
                                    val name: String = "leavePublisher",
                                    className: Option[String] = None) extends ProcessElementDescriptor {
  override def props = LeavePublisher.props(listeners)
}



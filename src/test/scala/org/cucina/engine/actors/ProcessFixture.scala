package org.cucina.engine.actors

import akka.actor.{Props, ActorRef, Actor}
import org.cucina.engine.definition.{ProcessElementDescriptor, TransitionDescriptor, CheckDescriptor, OperationDescriptor}
import org.cucina.engine.{ExecuteFailed, ExecuteComplete, ProcessContext}

/**
 * Created by vagrant on 8/25/15.
 */
class BlankActor extends Actor {
  def receive = Actor.ignoringBehavior
}

class CheckTrue(obj: Object) extends StackElementActor {
  def execute(processContext: ProcessContext): StackElementExecuteResult = {
    println("CheckTrue")
    StackElementExecuteResult(true, processContext)
  }
}

class CheckFalse(obj: Object) extends StackElementActor {
  def execute(processContext: ProcessContext): StackElementExecuteResult = StackElementExecuteResult(false, processContext)
}

class OutState extends Actor {
  def receive = {
    case StackRequest(pc, stack) =>
      println("OutState called with " + pc)
      pc.parameters += ("OutState" -> this.hashCode().toString)
      pc.client ! new ExecuteComplete(pc)
    case e@_ =>
      println(e)
      sender ! "OutState"
  }
}
class FailingTrans(name: String, output: String,
                   leaveOperations: Seq[OperationDescriptor],
                   checks: Seq[CheckDescriptor])
  extends Transition(name, output) {
  override def receive = {
    case StackRequest(pc, callerstack) =>
      sender ! ExecuteFailed(pc.client, "Transition should never be called")
    case DryCheck(pc) =>
      sender ! ExecuteFailed(pc.client, "Expect to fail")
  }
}

class SucceedingTrans(name: String, output: String,
                      leaveOperations: Seq[OperationDescriptor],
                      checks: Seq[CheckDescriptor])
  extends Transition(name, output){
  override def receive = {
    case StackRequest(pc, callerstack) =>
      println("SucceedingTrans: request " + pc + " sender " + sender)
      sender forward ExecuteComplete(pc)
    case DryCheck(pc) =>
      println("SucceedingTrans: dryCheck " + pc)
      sender forward ExecuteComplete(pc)
  }
}

class LocalState(name: String, transitions: Seq[TransitionDescriptor],
                 listeners: Seq[String],
                 enterOperations: Seq[OperationDescriptor],
                 leaveOperations: Seq[OperationDescriptor])
  extends State(name, transitions, listeners, enterOperations, leaveOperations) {
  override def receive: Receive = {
    case EnterState(_, pc) => {
      pc.parameters += ("visited" -> "yes")
      println("pc.parameters:" + pc.parameters)
      sender ! new ExecuteComplete(pc)
    }

    case a@_ => {
      println("Event:" + a)
    }
  }
}

class MockStackActor(name:String) extends StackElementActor {
  def execute(processContext: ProcessContext): StackElementExecuteResult = {
    println("Execute this:" + processContext)
    return StackElementExecuteResult(true, processContext)
  }
}

class MockStackDescritptor(val name: String) extends ProcessElementDescriptor {
  val className: Option[String] = Some(classOf[MockStackActor].getName)
}

class MockTransitionActor(name: String, output: String, ops: Seq[OperationDescriptor], cx: Seq[CheckDescriptor]) extends Actor {
  def receive = {
    case e@_ => println("mocktr:" + e)
  }
}

class MeListener(sink:ActorRef) extends Actor {
  def receive = {
    case LeaveEvent(_) =>
      Thread sleep 100
      sink ! "OK"
  }
}

class Op1 extends StackElementActor {
  def execute(pc: ProcessContext): StackElementExecuteResult = {
    pc.parameters += ("Op1" -> "called")
    new StackElementExecuteResult(true, processContext = pc)
  }
}

class Op1Desc extends OperationDescriptor("op1", className = Some(classOf[Op1].getName)) {
  override def props: Props = Props[Op1]
}

case class ObjectWithSimpleCollection(coll: Seq[String])
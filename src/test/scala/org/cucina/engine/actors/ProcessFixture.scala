package org.cucina.engine.actors

import akka.actor.Actor
import org.cucina.engine.definition.{CheckDescriptor, OperationDescriptor}
import org.cucina.engine.{ExecuteFailed, ExecuteComplete, ProcessContext}

/**
 * Created by vagrant on 8/25/15.
 */
trait ProcessFixture {

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
      println("Called with " + pc)
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
      println("Real request")
      sender ! ExecuteComplete(pc)
    case DryCheck(pc) =>
      println("DryCheck " + pc)
      sender ! ExecuteComplete(pc)
  }
}


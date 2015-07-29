package org.cucina.engine.actors

import akka.actor.{Props, Actor, ActorSystem, actorRef2Scala}
import akka.testkit.{ImplicitSender, TestKit}
import org.cucina.engine.ProcessContext
import org.cucina.engine.definition.{TransitionDescriptor, OperationDescriptor, Token}
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.collection.mutable
import scala.concurrent.duration.DurationInt

/**
 * Created by levinev on 28/07/2015.
 */
class StateActorSpec extends TestKit(ActorSystem("cucina-test"))
with ImplicitSender
with WordSpecLike
with Matchers
with BeforeAndAfterAll
with BeforeAndAfter {
  val processContext: ProcessContext = new ProcessContext(new Token(null, null), new mutable.HashMap[String, Object](), self)

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  val enterOperations: Seq[OperationDescriptor] = List(new Op1Desc)
  val leaveOperations: Seq[OperationDescriptor] = List()
  val transitions: Iterable[TransitionDescriptor] = Set()

  "StateActor" when {
    "received EnterState" should {
      "return " in {
        within(500 millis) {
          val actorRef = system.actorOf(Props(classOf[StateActor], "state", enterOperations, leaveOperations, transitions))
          actorRef ! new EnterState("one", processContext)
          expectMsgPF() {
            case ExecuteComplete(pc) => {
              println(pc.parameters)
              assert("called" == pc.parameters.get("Op1").get)
            }
          }
        }
      }
    }
  }
}

class Op1 extends Actor with StackElementActor {

  def execute(pc: ProcessContext): StackElementExecuteResult = {
    pc.parameters += ("Op1" -> "called")
    new StackElementExecuteResult(true, processContext = pc)
  }
}

class Op1Desc extends OperationDescriptor(className = classOf[Op1].getName, "op1") {

}
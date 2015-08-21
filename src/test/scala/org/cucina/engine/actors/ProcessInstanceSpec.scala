package org.cucina.engine.actors

import akka.actor.{Actor, ActorSystem, actorRef2Scala}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.collection.mutable.HashMap
import scala.concurrent.duration.DurationInt
import org.cucina.engine.definition._
import org.cucina.engine.{ExecuteComplete, ProcessContext}

/**
 * @author levinev
 */
class ProcessInstanceSpec extends TestKit(ActorSystem("cucina-test"))
with ImplicitSender
with WordSpecLike
with Matchers
with BeforeAndAfterAll {
  override def afterAll = {
    TestKit.shutdownActorSystem(system)
  }

  val mocktd = TransitionDescriptor("trand", "land", className = Some(classOf[MockTransitionActor].getName))
  val mockstate = StateDescriptor("start", List(mocktd), className = Some(classOf[LocalState].getName))
  val definition = ProcessDefinition(List(mockstate), "start", "xxx", "xx")
  val processContext: ProcessContext = ProcessContext(Token(new Object, definition), HashMap[String, Object](), self)


  "ProcessInstance actor" when {
    "created" should {
      "work" in {
        val pi = system.actorOf(ProcessInstance.props(definition))
        println("Pi " + pi)
      }
    }

    "receiving ExecuteStart" should {
      "return " in {
        val actorRef = system.actorOf(ProcessInstance.props(definition))
        within(500 millis) {
          actorRef ! new StartInstance(processContext, "one")
          expectMsgPF() {
            case ExecuteComplete(pc) => {
              println(pc)
              assert(pc.parameters.get("visited").get == "yes")
            }
          }
        }
      }
    }

    "receiving two ExecuteStarts" should {
      "return " in {
        val actorRef = system.actorOf(ProcessInstance.props(definition))
        within(500 millis) {
          actorRef ! new StartInstance(processContext, "one")
          actorRef ! new StartInstance(processContext, "two")
          expectMsgPF() {
            case ExecuteComplete(pc) => {
              println(pc)
              assert(pc.parameters.get("visited").get == "yes")
            }
          }
          expectMsgPF() {
            case ExecuteComplete(pc) => {
              println(pc)
              assert(pc.parameters.get("visited").get == "yes")
            }
          }
        }
      }
    }
  }
}

class LocalState(name: String, transitions: Seq[TransitionDescriptor],
                 enterListeners: Seq[String],
                 leaveListeners: Seq[String],
                 enterOperations: Seq[OperationDescriptor],
                 leaveOperations: Seq[OperationDescriptor])
  extends State(name, transitions, enterListeners, leaveListeners, enterOperations, leaveOperations) {
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


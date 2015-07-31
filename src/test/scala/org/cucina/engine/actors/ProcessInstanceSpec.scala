package org.cucina.engine.actors

import akka.actor.{Actor, Props, ActorSystem, actorRef2Scala}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.collection.mutable.HashMap
import scala.concurrent.duration.DurationInt
import org.cucina.engine.definition._
import org.cucina.engine.ProcessContext
import org.mockito.Mockito._

/**
 * @author levinev
 */
class ProcessInstanceSpec extends TestKit(ActorSystem("cucina-test"))
with ImplicitSender
with WordSpecLike
with Matchers
with BeforeAndAfterAll
with BeforeAndAfter
with MockitoSugar {
  var called: Boolean = false
  val mocktd = new TransitionDescriptor("trand", "land", className = classOf[MockTransitionActor].getName)
  val mockstate = new StateDescriptor("start", List(mocktd), className = classOf[LocalState].getName)
  val definition = new ProcessDefinition("start", "xxx", "xx")
  definition.setAllStates(Array(mockstate))
  val processContext: ProcessContext = new ProcessContext(new Token(null, null), new HashMap[String, Object](), self)

  override def afterAll = {
    TestKit.shutdownActorSystem(system)
  }

  "ProcessInstance actor" when {
    "created" should {
      "work" in {
        val pi = system.actorOf(ProcessInstance.props(definition))
        println("Pi " + pi)
      }
    }

    "receiving ExecuteStart" should {
      "return " in {
        within(500 millis) {
          val actorRef = system.actorOf(ProcessInstance.props(definition))
          actorRef ! new ExecuteStart(processContext, "one")
          expectMsgPF() {
            case ExecuteComplete(pc) => {
              println(pc.parameters)
              assert(pc.parameters.get("visited").get == "yes")
            }
          }
        }
      }
    }

    "receiving two ExecuteStarts" should {
      "return " in {
        within(500 millis) {
          val actorRef = system.actorOf(ProcessInstance.props(definition))
          actorRef ! new ExecuteStart(processContext, "one")
          actorRef ! new ExecuteStart(processContext, "two")
          expectMsgPF() {
            case ExecuteComplete(pc) => {
              println(pc.parameters)
              assert(pc.parameters.get("visited").get == "yes")
            }
          }
          expectMsgPF() {
            case ExecuteComplete(pc) => {
              println(pc.parameters)
              assert(pc.parameters.get("visited").get == "yes")
            }
          }
        }
      }
    }
  }
}

class LocalState(name: String, transitions: Iterable[TransitionDescriptor],
                 enterOperations: Seq[OperationDescriptor],
                 leaveOperations: Seq[OperationDescriptor])
  extends StateActor(name, transitions, enterOperations, leaveOperations) {
  override def receive: Receive = {
    case EnterState(_, pc) => {
      pc.parameters += ("visited" -> "yes")
      println("pc.parameters:" + pc.parameters)
      sender ! new ExecuteComplete(pc)
    }

    case a@_ => {
      println("Event:" + a)
      ///called = true
    }
  }
}

class MockTransitionActor(name: String, output: String, ops: Seq[OperationDescriptor], cx: Seq[CheckDescriptor]) extends Actor {
  def receive = {
    case e@_ => println("mocktr:" + e)
  }
}


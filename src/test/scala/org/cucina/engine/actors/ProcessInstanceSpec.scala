package org.cucina.engine.actors

import akka.actor.Actor.Receive

import scala.collection.mutable.HashMap
import scala.concurrent.duration.DurationInt
import org.cucina.engine.ProcessContext
import org.cucina.engine.definition.{Token, ProcessDefinition}
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import akka.actor.{Actor, ActorSystem, Props, actorRef2Scala}
import akka.testkit.ImplicitSender
import akka.testkit.TestKit
import org.cucina.engine.definition.StateDescriptor


/**
 * @author levinev
 */
class ProcessInstanceSpec extends TestKit(ActorSystem("cucina-test"))
with ImplicitSender
with WordSpecLike
with Matchers
with BeforeAndAfterAll
with BeforeAndAfter {
  var called: Boolean = false
  val state = new StateDescriptor(classOf[LocalState].getName, "start", called :: Nil)
  val definition = new ProcessDefinition(state, "xxx", "xx")
  val processContext: ProcessContext = new ProcessContext(new Token(null, null), new HashMap[String, Object](), null)

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "ProcessInstance actor" when {

    "receiving ExecuteStart" should {
      "return " in {
        within(500 millis) {
          val actorRef = system.actorOf(ProcessInstance.props(definition))
          actorRef ! new ExecuteStart(processContext, "one")
        }
      }
    }

    "receiving two ExecuteStarts" should {
      "return " in {
        within(500 millis) {
          val actorRef = system.actorOf(ProcessInstance.props(definition))
          actorRef ! new ExecuteStart(processContext, "one")
          actorRef ! new ExecuteStart(processContext, "two")
        }
      }
    }

    "pre-existing start state" should {
      "tell the state" in {
        within(500 millis) {
          val defin = new ProcessDefinition(state, "aaa", "xx")
          val local = system.actorOf(ProcessInstance.props(defin), "test_local")
          local ! new ExecuteStart(processContext, "one")
          expectMsgPF()  {
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

class LocalState(var called: Boolean) extends Actor {
  override def receive: Receive = {
    case EnterState(_, pc) => {
      pc.parameters += ("visited" -> "yes")
      println("pc.parameters:" + pc.parameters)
      sender ! new ExecuteComplete(pc)
    }

    case a@_ => {
      println("Event:" + a)
      called = true
    }
  }
}

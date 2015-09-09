package org.cucina.engine.actors

import akka.actor.{Props, Actor, ActorSystem, actorRef2Scala}
import akka.testkit.{ImplicitSender, TestKit}
import org.cucina.engine.{ExecuteComplete, ProcessContext}
import org.cucina.engine.definition.{ProcessDefinition, TransitionDescriptor, OperationDescriptor, Token}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.collection.mutable
import scala.concurrent.duration.DurationInt

/**
 * Created by levinev on 28/07/2015.
 */
class StateSpec extends TestKit(ActorSystem("cucina-test"))
with ImplicitSender
with WordSpecLike
with Matchers
with BeforeAndAfterAll
with BeforeAndAfter
with MockitoSugar {
  val processContext: ProcessContext = new ProcessContext(new Token(new Object, mock[ProcessDefinition]), new mutable.HashMap[String, Object](), self)

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  val enterOperations: Seq[OperationDescriptor] = List(new Op1Desc)
  val transitions: Seq[TransitionDescriptor] = List()

  "State" when {
    "received EnterState" should {
      "return " in {
        within(500 millis) {
          val actorRef = system.actorOf(State.props("state", transitions, enterOperations = enterOperations))
          actorRef ! new EnterState("one", processContext)
          expectMsgPF() {
            case ExecuteComplete(pc) =>
              println(pc.parameters)
              assert("called" == pc.parameters.get("Op1").get)
            case a@_ => println("Whopsie:" + a)
          }
        }
      }
    }
  }
}


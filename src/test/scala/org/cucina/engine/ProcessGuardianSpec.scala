package org.cucina.engine

import java.util

import akka.actor.{Actor, Props, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import org.cucina.engine.actors._
import org.cucina.engine.definition.{StateDescriptor, ProcessDefinition, TransitionDescriptor}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}

import org.mockito.Mockito._

/**
 * Created by levinev on 29/07/2015.
 */
class ProcessGuardianSpec extends TestKit(ActorSystem("cucina-test"))
with ImplicitSender
with WordSpecLike
with Matchers
with BeforeAndAfterAll
with BeforeAndAfter
with MockitoSugar {
  val tr1 = new TransitionDescriptor("tr1", "end")
  val definition = new ProcessDefinition(List(new StateDescriptor("start", List(tr1)), new StateDescriptor("end", List())), "start", "fake", "fake")
  val str = """{"states":[{"name":"start","enterOperations":[],"transitions":[{"name":"tr1","checks":[],"className":"org.cucina.engine.actors.TransitionActor","leaveOperations":[],"output":"end"}],"className":"org.cucina.engine.actors.StateActor","leaveOperations":[]},{"name":"end","enterOperations":[],"transitions":[],"className":"org.cucina.engine.actors.StateActor","leaveOperations":[]}],"startState":"start","description":"fake","id":"fake"}"""
  val me = self

  override def afterAll() = {
    TestKit.shutdownActorSystem(system)
  }

  val proccon = mock[ProcessContext]
  when(proccon.client).thenReturn(self)
  when(proccon.token)

  val blankActor = system.actorOf(Props(new BlankActor))
  "ProcessGuardian actor" when {
    "created" should {
      "work" in {
        val pi = system.actorOf(ProcessGuardian.props())
        println("Pi " + pi)
      }
    }

    "received definition" should {
      "add to registry" in {

        val pi = system.actorOf(ProcessGuardian.props(system.actorOf(Props(new Actor {
          def receive = {
            case e: AddProcessDefinition =>
              println("Added definition " + e)
              sender ! ExecuteComplete(proccon)
            case e => println("Whhops:" + e)
          }
        }
        )), blankActor, blankActor))
        pi ! new AddDefinition(str)
        expectMsg("OK")
      }
    }
    "received Start" should {
      "call definitionregistry" in {
        val pi = system.actorOf(ProcessGuardian.props(system.actorOf(Props(new Actor {
          def receive = {
            case t: FindDefinition =>
              println("FindDefinition " + t)
              me ! "OKI"
          }
        })), blankActor, blankActor))
        pi ! StartProcess("fake", new Object, null, None)
        expectMsg("OKI")
      }
    }

    "received Move" should {
      "call definitionregistry" in {
        val pi = system.actorOf(ProcessGuardian.props(system.actorOf(Props(new Actor {
          def receive = {
            case t: FindDefinition =>
              println("FindDefinition " + t)
              me ! "OKI"
          }
        })), blankActor, blankActor))
        pi ! MakeTransition("fake", new Object, null, None)
        expectMsg("OKI")
      }
    }

    "processDefinition" should {
      val obj = new Object()
      "call tokenFactory with start" in {
        val pi = system.actorOf(ProcessGuardian.props(blankActor, blankActor, system.actorOf(Props(new Actor {
          def receive = {
            case t: StartToken =>
              println("StartToken " + t)
              require(t.client == me)
              require(t.processDefinition == definition)
              require(t.domainObject == obj)
              me ! "OKI"
          }
        }))))
        pi ! ProcessDefinitionWrap(Some(definition), NestedTuple(StartProcess("fake", obj, null, None), me))
        expectMsg("OKI")
      }
      "call tokenFactory with move" in {
        val pi = system.actorOf(ProcessGuardian.props(blankActor, blankActor, system.actorOf(Props(new Actor {
          def receive = {
            case t: MoveToken =>
              println("StartToken " + t)
              require(t.client == me)
              require(t.processDefinition == definition)
              require(t.domainObject == obj)
              me ! "OKI"
          }
        }))))
        pi ! ProcessDefinitionWrap(Some(definition), NestedTuple(MakeTransition("fake", obj, null, None), me))
        expectMsg("OKI")
      }
    }

    "received " should {
      val pc = mock[ProcessContext]
      "StartInstance call instanceFactory" in {
        val pi = system.actorOf(ProcessGuardian.props(blankActor, system.actorOf(Props(new Actor {
          def receive = {
            case t: StartInstance =>
              println("StartInstance " + t)
              me ! "OKI"
          }
        })), blankActor))
        pi ! StartInstance(pc, "yahoo")
        expectMsg("OKI")
      }

      "MoveInstance call instanceFactory" in {
        val pi = system.actorOf(ProcessGuardian.props(blankActor, system.actorOf(Props(new Actor {
          def receive = {
            case t: MoveInstance =>
              println("MoveInstance " + t)
              me ! "OKI"
          }
        })), blankActor))
        pi ! MoveInstance(pc, "yahoo")
        expectMsg("OKI")
      }
    }
  }
}

class BlankActor extends Actor {
  def receive = Actor.ignoringBehavior
}

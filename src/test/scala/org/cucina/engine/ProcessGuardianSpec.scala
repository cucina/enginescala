package org.cucina.engine

import java.util

import akka.actor.{Props, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import org.cucina.engine.definition.{StateDescriptor, ProcessDefinition, TransitionDescriptor}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.collection.mutable

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

  override def afterAll() = {
    TestKit.shutdownActorSystem(system)
  }

  "ProcessGuardian actor" when {
    "created" should {
      "work" in {
        val pi = system.actorOf(Props[ProcessGuardian])
        println("Pi " + pi)
      }
    }

    "received definition" should {
      "add to registry" in {
        val pi = system.actorOf(Props[ProcessGuardian])
        pi ! new AddDefinition(str)
      }
    }
    "received Start" should {
      "start process" in {
        val pi = system.actorOf(Props[ProcessGuardian])
        pi ! StartProcess("fake", new Object, null, Map[String, Object]())
        expectMsgPF() {
          case a@_ =>
            println("a " + a)
        }
      }
    }
  }
}

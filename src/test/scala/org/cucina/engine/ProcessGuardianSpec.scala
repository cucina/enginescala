package org.cucina.engine

import java.util

import akka.actor.{Actor, Props, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import org.cucina.engine.actors.{MoveToken, StartToken, AddProcessDefinition}
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
  val me = self
  override def afterAll() = {
    TestKit.shutdownActorSystem(system)
  }

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
            case e:AddProcessDefinition =>
              println("Added definition " + e)
              me ! "OK"
            case e => println("Whhops:" + e)
          }
        }
        )), blankActor, blankActor))
        pi ! new AddDefinition(str)
        expectMsg("OK")
      }
    }
    "received Start" should {
      "start process" in {
        val pi = system.actorOf(ProcessGuardian.props(blankActor, blankActor,
          system.actorOf(Props(new Actor {
            def receive = {
              case t:StartToken =>
                println("Starting " + t)
                me ! "OK"
            }}))))
        pi ! StartProcess("fake", new Object, null, Map[String, Object]())
        expectMsg("OK")
      }
    }

    "received Move" should {
      "move process" in {
        val pi = system.actorOf(ProcessGuardian.props(blankActor, blankActor,
          system.actorOf(Props(new Actor {
            def receive = {
              case t:MoveToken =>
                println("Moving " + t)
                me ! "OK"
            }}))))
        pi ! MakeTransition("fake", new Object, null, Map[String, Object]())
        expectMsg("OK")
      }
    }
  }
}

class BlankActor extends Actor {
  def receive = Actor.ignoringBehavior
}

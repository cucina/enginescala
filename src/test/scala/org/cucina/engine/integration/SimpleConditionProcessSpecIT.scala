package org.cucina.engine.integration

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.cucina.engine._
import org.cucina.engine.actors.PresetBooleanReturnActor
import org.cucina.engine.definition._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}

/**
 * Created by vlevine on 07/08/15.
 */
class SimpleConditionProcessSpecIT extends TestKit(ActorSystem("cucina-test"))
with ImplicitSender
with WordSpecLike
with Matchers
with BeforeAndAfterAll
with BeforeAndAfter
with MockitoSugar {

  override def afterAll() = {
    TestKit.shutdownActorSystem(system)
  }

  val tr1 = TransitionDescriptor("tr1", "end", checks = List(CheckDescriptor("ch1", parameter = "/user/trueCond")))
  val tr2 = TransitionDescriptor("tr2", "end", checks = List(CheckDescriptor("ch2", parameter = "/user/falseCond")))
  val transdesc = List(tr1, tr2)
  val startState = StateDescriptor("start", transdesc)
  val endState = StateDescriptor("end", List())
  val simpleStates = List(startState, endState)
  val domainObject = new Object

  "ProcessGuardian" when {
    import DefinitionProtocol._
    import spray.json._
    val simpleDef = ProcessDefinition(simpleStates, "start", "desc", "simple")

    val json = simpleDef.toJson
    val str = json.compactPrint
    println(str)

    val trueCond = system.actorOf(Props(classOf[PresetBooleanReturnActor], true), "trueCond")
    val falseCond = system.actorOf(Props(classOf[PresetBooleanReturnActor], false), "falseCond")

    "added and ran simple" should {
      val guardian = system.actorOf(ProcessGuardian.props(), "pg")
      guardian ! AddDefinition(str)

      "execute transition tr1" in {
        val obj = new Object
        guardian ! StartProcess("simple", obj, "tr1")
        expectMsg(obj)
        guardian ! GetAvailableTransitions(obj, "simple")
        expectMsg(List())
      }
      "execute transition tr2" in {
        val obj = new Object
        guardian ! StartProcess("simple", obj, "tr2")
        expectMsgPF() {
          case ProcessFailure(_) => // success
          case msg =>
            println(msg)
            fail
        }
        guardian ! GetAvailableTransitions(obj, "simple")
        expectMsgPF() {
          case ProcessFailure(_) =>
          case other => fail
        }
      }
      "fail transition tr3" in {
        val obj = new Object
        guardian ! StartProcess("simple", obj, "tr3")
        expectMsgPF() {
          case ProcessFailure(_) =>
          case other => fail
        }
      }
    }
  }
}
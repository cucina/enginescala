package org.cucina.engine

import akka.actor.{Props, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import org.cucina.engine.actors.{ScriptExecuteActor, BlankOperationActor}
import org.cucina.engine.definition._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}

/**
 * Created by vlevine on 07/08/15.
 */
class SimpleConditionProcessSpec extends TestKit(ActorSystem("cucina-test"))
with ImplicitSender
with WordSpecLike
with Matchers
with BeforeAndAfterAll
with BeforeAndAfter
with MockitoSugar {

  override def afterAll() = {
    TestKit.shutdownActorSystem(system)
  }

  val tr1 = TransitionDescriptor("tr1", "end", checks = List(CheckDescriptor("ch1", parameter = "/user/chx1")))
  val tr2 = TransitionDescriptor("tr2", "end", checks = List(CheckDescriptor("ch2", Some(classOf[ScriptExecuteActor].getName), "false")))
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

    val chx1 = system.actorOf(Props[BlankOperationActor], "chx1")
    println("CHX1 " + chx1)
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
        expectMsg(obj)
        guardian ! GetAvailableTransitions(obj, "simple")
        expectMsg(List())
      }
      "execute transition tr3" in {
        val obj = new Object
        guardian ! StartProcess("simple", obj, "tr3")
        expectMsg(ProcessFailure(_))
      }
    }
  }
}
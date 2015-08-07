package org.cucina.engine

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.cucina.engine.definition.{DefinitionProtocol, TransitionDescriptor, StateDescriptor, ProcessDefinition}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}

/**
 * Created by vlevine on 07/08/15.
 */
class SimpleProcessSpec extends TestKit(ActorSystem("cucina-test"))
with ImplicitSender
with WordSpecLike
with Matchers
with BeforeAndAfterAll
with BeforeAndAfter
with MockitoSugar {

  override def afterAll() = {
    TestKit.shutdownActorSystem(system)
  }


  val startState = StateDescriptor("start", List(TransitionDescriptor("tr1", "end")))
  val endState = StateDescriptor("end", List())
  val simpleStates = List(startState, endState)

  "ProcessGuardian" when {
    "added and ran simple" should {
      "produce complete" in {
        import spray.json._
        import DefinitionProtocol._
        val simpleDef = ProcessDefinition(simpleStates, "start", "desc", "simple")

        val json = simpleDef.toJson
        val str = json.compactPrint
        println(str)

        val guardian = system.actorOf(ProcessGuardian.props())
        guardian ! AddDefinition(str)
        guardian ! StartProcess("simple", new Object)
        expectMsg("OK")
      }
    }
  }
}
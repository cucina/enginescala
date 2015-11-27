package org.cucina.engine.integration

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.cucina.engine._
import org.cucina.engine.definition.{DefinitionProtocol, ProcessDefinition, StateDescriptor, TransitionDescriptor}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}

/**
 * Created by vlevine on 07/08/15.
 */
class TwoStepProcessSpecIT extends TestKit(ActorSystem("cucina-test"))
with ImplicitSender
with WordSpecLike
with Matchers
with BeforeAndAfterAll
with BeforeAndAfter
with MockitoSugar {

  override def afterAll() = {
    TestKit.shutdownActorSystem(system)
  }

  val startState = StateDescriptor("start", List(TransitionDescriptor("tr1", "mid")))
  val midState = StateDescriptor("mid", List(TransitionDescriptor("tr2", "end")))
  val endState = StateDescriptor("end", List())
  val simpleStates = List(startState, midState, endState)
  val domainObject = new Object

  "TwoStep Process" when {
    import DefinitionProtocol._
    import spray.json._
    val simpleDef = ProcessDefinition(simpleStates, "start", "desc", "simple")

    val json = simpleDef.toJson
    val str = json.compactPrint
    println(str)

    "added and ran simple" should {
      val guardian = system.actorOf(ProcessGuardian.props())
      guardian ! AddDefinition(str)
      "produce complete" in {
        guardian ! StartProcess("simple", domainObject)
        expectMsg(domainObject)
      }
      "fail on dupe" in {
        guardian ! StartProcess("simple", domainObject)
        expectMsgPF(){
          case ProcessFailure(_) => // good
          case f@_ => fail("Unexpected result:" + f)
        }
        guardian ! GetAvailableTransitions(domainObject, "simple")
        expectMsg(List("tr1"))
      }
      "execute transition" in {
        val obj = new Object
        guardian ! StartProcess("simple", obj, "tr1")
        expectMsg(obj)
        guardian ! GetAvailableTransitions(obj, "simple")
        expectMsg("tr2"::Nil)
        guardian ! MakeTransition("simple", obj, "tr2")
        expectMsg(obj)
        guardian ! GetAvailableTransitions(obj, "simple")
        expectMsg(List())
      }
    }
  }
}
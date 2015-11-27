package org.cucina.engine.integration

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.cucina.engine._
import org.cucina.engine.actors.{Join, SplitCollection, ObjectWithSimpleCollection}
import org.cucina.engine.definition._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}

/**
 * Created by vlevine on 07/08/15.
 */
class CollectionSplitProcessSpecIT extends TestKit(ActorSystem("cucina-test"))
with ImplicitSender
with WordSpecLike
with Matchers
with BeforeAndAfterAll
with BeforeAndAfter
with MockitoSugar {

  override def afterAll() = {
    TestKit.shutdownActorSystem(system)
  }

  val startState = StateDescriptor("start", List(TransitionDescriptor("tr1", "split")))
  val endState = StateDescriptor("end", List())
  val join = StateDescriptor("join", TransitionDescriptor("trj", "end")::Nil, className=Some(classOf[Join].getName))
  val split = StateDescriptor("split", TransitionDescriptor("trs", "join")::Nil, className = Some(classOf[SplitCollection].getName),
    parameters=Some(Map("collectionExpression"->"token().domainObject().coll()")))
  val simpleStates = List(startState, split, join, endState)

  "Collection Split Process" when {
    import DefinitionProtocol._
    import spray.json._
    val simpleDef = ProcessDefinition(simpleStates, "start", "desc", "simple")

    val json = simpleDef.toJson
    val str = json.prettyPrint
    println(str)

    "added and ran simple" should {
      val domainObject = new Object
      val guardian = system.actorOf(ProcessGuardian.props(), "guardian")
      guardian ! AddDefinition(str)
/*
      "produce complete for start" in {
        guardian ! StartProcess("simple", domainObject)
        expectMsg(domainObject)
      }
      "fail on dupe" in {
        val domainObject = new Object
        guardian ! StartProcess("simple", domainObject)
        expectMsgPF() {
          case ProcessFailure(_) => // good
          case f@_ => fail("Unexpected result:" + f)
        }
        guardian ! GetAvailableTransitions(domainObject, "simple")
        expectMsg(List("tr1"))
      }
*/
      "execute transition" in {
        val domainObject = ObjectWithSimpleCollection("A" :: "B" :: "C" :: Nil)
        guardian ! StartProcess("simple", domainObject, "tr1")
        expectMsg(domainObject)
        print("Received " + domainObject)
        guardian ! GetAvailableTransitions(domainObject, "simple")
        expectMsg(List())
      }
    }
  }
}
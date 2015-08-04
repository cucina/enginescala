package org.cucina.engine.definition

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import spray.json.DefaultJsonProtocol

import scala.collection.mutable

/**
 * Created by levinev on 03/08/2015.
 */
class ProcessDefinitionSpec
  extends TestKit(ActorSystem("cucina-test"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll
  with BeforeAndAfter
  with MockitoSugar {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "ProcessDefinitionSpec" when {
    "serialized" should {
      "produce json" in {
        import spray.json._
/*
        case class Color(a: String)

        object MyJsonProtocol extends DefaultJsonProtocol {
          implicit val colFormat = jsonFormat1(Color)
        }

        import MyJsonProtocol._

        val color = new Color("red")
        val json = color.toJson

        println(json.prettyPrint)
*/

        val tr1 = new TransitionDescriptor("tr1", "end")
        val states = List(new StateDescriptor("start", List(tr1)), new StateDescriptor("end", List()))
        val definition = new ProcessDefinition(states, "start", "fake", "fake")

        import DefinitionProtocol._

        val json = definition.toJson
        println(json.prettyPrint)


      }
    }
  }
}

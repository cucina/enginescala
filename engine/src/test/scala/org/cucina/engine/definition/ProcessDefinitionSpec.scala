package org.cucina.engine.definition

import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpecLike}

/**
 * Created by levinev on 03/08/2015.
 */
class ProcessDefinitionSpec
  extends WordSpecLike
  with Matchers
  with MockitoSugar {

  "ProcessDefinitionSpec" when {
    val tr1 = new TransitionDescriptor("tr1", "end")
    val states = List(new StateDescriptor("start", List(tr1)), new StateDescriptor("end", List()))
    val definition = new ProcessDefinition(states, "start", "fake", "fake")
    "serialized" should {
      "produce json" in {
        import spray.json._
        import DefinitionProtocol._


        val json = definition.toJson
        val str = json.compactPrint
        println(str)
        val pjson = str.parseJson
        val defin = pjson.convertTo[ProcessDefinition]
        println(defin)
        assert("start"== defin.startState)
      }
    }
    "listTransitions" should {
      "return one " in {
        definition.listTransitions("start") match {
          case Some(s) => assert(s.head == "tr1")
          case None => fail
        }
      }
      "return None " in {
        definition.listTransitions("startx") match {
          case Some(_) => fail
          case None =>
        }
      }
      "return empty" in {
        definition.listTransitions("end") match {
          case Some(s) => assert(s.isEmpty)
          case None => fail
        }
      }
    }
  }
}

package org.cucina.engine.actors.support

import javax.script.{ScriptEngine, ScriptEngineManager}

import org.scalatest.{Matchers, WordSpecLike}
import scala.collection.JavaConversions._

/**
 * Created by levinev on 14/08/2015.
 */

class ScriptingInterpreterSpec
  extends WordSpecLike
  with Matchers {
  //settings.usejavacp.value = true
  println(util.Properties.versionString)
  "ScriptingInterpreter" when {
    val screng = new ScriptEngineManager().getEngineFactories
    println(screng)
    for (sce <- screng) {
      println(sce.getNames + " threading=" + sce.getParameter("THREADING"))
    }
    "using scala" should {
      "eval true" in {
        val si = new ScriptingInterpreter
        val start = System.currentTimeMillis()
        si.interpret("println(a);a==\"10\"", Map("a" -> "10")) match {
          case Some(a) => assert(a == true)
          case None => fail
        }
        println("time:" + (System.currentTimeMillis() - start))
      }
      "eval 100" in {
        val si = new ScriptingInterpreter
        val start = System.currentTimeMillis()
        si.interpret("a.asInstanceOf[Int]*b.asInstanceOf[Int]", Map("a" -> Int.box(5), "b" -> Int.box(20))) match {
          case Some(a) => assert(a == 100)
          case None => fail
        }
        println("time:" + (System.currentTimeMillis() - start))
      }
      "get property" in {
        val si = new ScriptingInterpreter
        val start = System.currentTimeMillis()
        si.interpret("import org.cucina.engine.actors.support._;println(a); a.asInstanceOf[My].name==\"Name\"", Map("a" -> new My("Name"))) match {
          case Some(a) => assert(a == true)
          case None => fail
        }
        si.interpret("a.asInstanceOf[My].name", Map("a" -> new My("Name"))) match {
          case Some(a) => assert(a == "Name")
          case None => fail
        }
        println("time:" + (System.currentTimeMillis() - start))
      }
      "set property" in {
        val si = new ScriptingInterpreter
        val start = System.currentTimeMillis()
        si.interpret("import org.cucina.engine.actors.support._;a.asInstanceOf[My].name==\"Name\"", Map("a" -> new My("Name")))  match {
          case Some(a) => assert(a == true)
          case None => fail
        }
        si.interpret("val b=new My(\"New\"); b.name", Map("a" -> new My("Name")))  match {
          case Some(a) => assert(a == "New")
          case None => fail
        }
        val a = new MyR("Name")
        si.interpret("a.asInstanceOf[MyR].name= \"New\"; print(a.asInstanceOf[MyR].name); \"ignore\"", Map("a" -> a))
        assert(a.name == "New")
        println("time:" + (System.currentTimeMillis() - start))
      }
    }
  }
}

case class My(val name: String)

case class MyR(var name: String)

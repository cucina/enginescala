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
    "using ecma" should {
      val si = new ScriptingInterpreter
      "eval true" in {
        assert(si.execute("println(a);a==\"10\"", Map("a" -> "10")) == true)
      }
      "eval 100" in {
        assert(si.execute("a.asInstanceOf[Int]*b.asInstanceOf[Int]", Map("a" -> Int.box(5), "b" -> Int.box(20))) == 100)
      }
      "get property" in {
        assert(si.execute("import org.cucina.engine.actors.support._;println(a); a.asInstanceOf[My].name==\"Name\"", Map("a" -> new My("Name"))) == true)
        assert(si.execute("a.asInstanceOf[My].name", Map("a" -> new My("Name"))) == "Name")
      }
      "set property" in {
        assert(si.execute("a.asInstanceOf[My].name==\"Name\"", Map("a" -> new My("Name"))) == true)
        assert(si.execute("val b=new My(\"New\"); print(b.name); b.name", Map("a" -> new My("Name"))) == "New")
      }
    }
  }
}

case class My(val name: String)

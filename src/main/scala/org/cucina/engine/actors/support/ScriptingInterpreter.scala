package org.cucina.engine.actors.support

import javax.script.ScriptEngineManager

import org.slf4j.LoggerFactory

/**
 * Created by levinev on 14/08/2015.
 */
class ScriptingInterpreter {
  private val LOG = LoggerFactory.getLogger(getClass)
  val engine = new ScriptEngineManager(this.getClass.getClassLoader).getEngineByName("scala")
  // TODO this to be done only once per JVM
  val settings = engine.asInstanceOf[scala.tools.nsc.interpreter.IMain].settings
  settings.embeddedDefaults[ScriptingInterpreter]
  settings.usejavacp.value = true
//  println(engine.ge)

  def execute(expression: String, parameters: Map[String, Object]): Object = {

    LOG.debug("Engine:" + engine)
    parameters.foreach { case (key) =>
      engine.put(key._1, key._2)
    }
    engine.eval(expression)
  }

  //e.put("n", 10)

}

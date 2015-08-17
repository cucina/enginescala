package org.cucina.engine.actors.support

import javax.script.{ScriptContext, ScriptEngineManager}

import org.slf4j.LoggerFactory

import scala.util.Try

/**
 * Created by levinev on 14/08/2015.
 * TODO JDK built-in engine does not have any support for multithreading.
 * TODO There is only one engine which preserves state between calls.
 * TODO Therefore, an alternative should be used such as https://github.com/kostaskougios/scalascriptengine
 *
 */
class ScriptingInterpreter {
  private val LOG = LoggerFactory.getLogger(getClass)
  // TODO optional parameter to identify engine type
  val engine = new ScriptEngineManager(this.getClass.getClassLoader).getEngineByName("scala")
  // TODO this to be done only once per JVM
  val settings = engine.asInstanceOf[scala.tools.nsc.interpreter.IMain].settings
  settings.embeddedDefaults[ScriptingInterpreter]
  settings.usejavacp.value = true

  //  println(engine.ge)

  def interpret(expression: String, parameters: Map[String, Object]): Option[AnyRef] = {
    LOG.debug("Engine:" + engine)
    try {
      val binding = engine.createBindings()

      parameters.foreach { case (key) =>
        binding.put(key._1, key._2)
      }

      engine.setBindings(binding, ScriptContext.ENGINE_SCOPE)
      Some(engine.eval(expression))
    } catch {
      case e:Throwable =>
        LOG.error("Failed executong script:" + e)
        None
    }
  }
}

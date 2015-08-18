package org.cucina.engine.actors

import org.cucina.engine.ProcessContext
import org.cucina.engine.actors.support.ScriptingInterpreter
import org.slf4j.LoggerFactory

/**
 * Created by levinev on 17/08/2015.
 */
// TODO add script engine identifier
class ScriptExecuteActor(script: String) extends StackElementActor {
  private val LOG = LoggerFactory.getLogger(getClass)

  def execute(processContext: ProcessContext): StackElementExecuteResult = {
    LOG.info("Processing " + processContext + "script '" + script + "'")
    val interpreter = new ScriptingInterpreter
    interpreter.interpret(script, Map("processContext" -> processContext)) match {
      case Some(x) => StackElementExecuteResult(x != false, processContext)
      case None => StackElementExecuteResult(false, processContext)
    }
  }
}

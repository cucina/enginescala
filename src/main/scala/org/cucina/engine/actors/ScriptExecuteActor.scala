package org.cucina.engine.actors

import org.cucina.engine.ProcessContext
import org.cucina.engine.actors.support.ScriptingInterpreter

/**
 * Created by levinev on 17/08/2015.
 */
// TODO add script engine identifier
class ScriptExecuteActor(script: String) extends StackElementActor {
  def execute(processContext: ProcessContext): StackElementExecuteResult = {
    val interpreter = new ScriptingInterpreter
    interpreter.interpret(script, Map("processContext" -> processContext)) match {
      case Some(x) => StackElementExecuteResult(x != false, processContext)
      case None => StackElementExecuteResult(false, processContext)
    }
  }
}

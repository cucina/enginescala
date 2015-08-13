package org.cucina.engine.actors

import akka.util.Timeout
import org.cucina.engine.ProcessContext
import akka.pattern.ask
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

/**
 * Created by levinev on 13/08/2015.
 */
case class ProcessResult(result: Boolean, processContext: ProcessContext = null, message: String = null, throwable: Throwable = null)

class DelegatingStackActor(actorName: String) extends StackElementActor {
  val delegate = findActor(actorName).get

  def execute(processContext: ProcessContext): StackElementExecuteResult = {
    implicit val timeout = Timeout(1 second)
    val f = delegate ? processContext
    val result = Await.result(f, timeout.duration).asInstanceOf[ProcessResult]
    StackElementExecuteResult(result.result, result.processContext, result.message, result.throwable)
  }
}
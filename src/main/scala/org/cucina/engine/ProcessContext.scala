package org.cucina.engine

import org.cucina.engine.definition.Token
import scala.collection.mutable.Map
import org.cucina.engine.definition.OperationDescriptor
import org.cucina.engine.definition.ProcessElementDescriptor
import akka.actor.ActorRef

/**
 * @author levinev
 */
class ProcessContext(val token: Token, val parameters: Map[String, Object]) {
  var originalSender: ActorRef = _
  var operationIterator: Iterator[OperationDescriptor] = _

  def nextOperationDescriptor(): OperationDescriptor = {
    if (operationIterator == null) null
    if (operationIterator.hasNext) {
      val od = operationIterator.next()
      if (od == null) {
        operationIterator = null
        null
      } else {
        od
      }
    } else null
  }
}

package org.cucina.engine.actors

import akka.actor.Actor
import org.cucina.engine.definition.OperationDescriptor
import org.cucina.engine.definition.OperationDescriptor
import akka.actor.Props
import org.cucina.engine.ProcessContext
import akka.actor.ActorRef

/**
 * @author levinev
 */

case class OperationDescriptorWrap(operationDescriptor: OperationDescriptor, processContext: ProcessContext)
case class OperationRequest(operationParameters: Map[String, Object], processContext: ProcessContext)
case class OperationResponse(processContext:ProcessContext)

class OperationProcessor extends Actor {
  def receive = {
    case OperationDescriptorWrap(operationDescriptor, processContext) => {
      // operationDescriptor.parameters ++ processContext.parameters
      val op = context.actorOf(Props.apply(Class.forName(operationDescriptor.className)), operationDescriptor.name)
      op forward new OperationRequest(operationDescriptor.parameters, processContext)
    }
  }
}
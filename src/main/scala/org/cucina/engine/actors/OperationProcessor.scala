package org.cucina.engine.actors

import org.cucina.engine.ProcessContext
import org.cucina.engine.definition.OperationDescriptor

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.PoisonPill
import akka.actor.Props
import akka.actor.Status.Failure
import akka.actor.actorRef2Scala

/**
 * @author levinev
 */

case class OperationDescriptorsWrap(operationDescriptors: Iterable[OperationDescriptor], processContext: ProcessContext)
case class OperationRequest(operationParameters: Map[String, Object], processContext: ProcessContext)
case class OperationResponse(processContext: ProcessContext)
case class OperationComplete()
case class OperationFailed(message: String)

class OperationProcessor extends Actor {
  var els: Iterator[OperationDescriptor] = _
  def receive = start

  private def start: Receive = {
    case OperationDescriptorsWrap(operationDescriptors, processContext) => {
      println(sender)
      if (operationDescriptors == null) sender ! new Failure(new IllegalArgumentException("No operationDescriptor"))
      else {
        els = operationDescriptors.iterator
        processEls(processContext)
        context become response(sender)
      }
    }
  }
  private def response(sender: ActorRef): Receive = {
    case OperationResponse(processContext) => processEls(processContext)
    case of @ OperationFailed => {
      sender ! of
      self ! PoisonPill
    }
  }

  private def processEls(processContext: ProcessContext) = {
    if (els.hasNext) {
      val operationDescriptor = els.next
      processNext(operationDescriptor, processContext)
    } else {
      sender ! new OperationComplete()
      self ! PoisonPill
    }
  }
  private def processNext(operationDescriptor: OperationDescriptor, processContext: ProcessContext) = {
    // operationDescriptor.parameters ++ processContext.parameters
    val op = context.actorOf(props(operationDescriptor.className, operationDescriptor.parameters), operationDescriptor.name)
    op ! new OperationRequest(operationDescriptor.parameters, processContext)
  }

  private def props(className: String, parameters: Map[String, Object]): Props = {
    Props.apply(Class.forName(className))
  }
}
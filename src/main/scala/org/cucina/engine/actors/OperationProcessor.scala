package org.cucina.engine.actors

import org.cucina.engine.ProcessContext
import org.cucina.engine.definition.OperationDescriptor
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.PoisonPill
import akka.actor.Props
import akka.actor.Status.Failure
import akka.actor.actorRef2Scala
import org.slf4j.LoggerFactory

/**
 * @author levinev
 */

case class OperationDescriptorsWrap(operationDescriptors: Iterable[OperationDescriptor], processContext: ProcessContext)
case class OperationRequest(operationParameters: Map[String, Object], processContext: ProcessContext)
case class OperationResponse(processContext: ProcessContext)
case class OperationComplete()
case class OperationFailed(message: String)

class OperationProcessor extends Actor {
  private[this] val LOG = LoggerFactory.getLogger(getClass())
  var els: Iterator[OperationDescriptor] = _
  var origSender:ActorRef = _
  def receive = start

  private def start: Receive = {
    case OperationDescriptorsWrap(operationDescriptors, processContext) => {
      LOG.debug("Original sender " + sender)
      LOG.info("Me " + self)
      if (operationDescriptors == null) {
        LOG.debug("No OperationDescriptor")
        sender ! new Failure(new IllegalArgumentException("No operationDescriptor"))
        self ! PoisonPill
      } else {
        els = operationDescriptors.iterator
        origSender = sender()
        processEls(processContext)
        context become response()
      }
    }
  }
  private def response(): Receive = {
    case OperationResponse(processContext) => processEls(processContext)
    case of @ OperationFailed(_) => {
      origSender ! of
      self ! PoisonPill
    }
  }

  private def processEls(processContext: ProcessContext) = {
    if (els.hasNext) {
      val operationDescriptor = els.next
      LOG.debug("operationDescriptor:" + operationDescriptor)
      processNext(operationDescriptor, processContext)
    } else {
      LOG.debug("End of operations " + origSender)
      origSender ! new OperationComplete()
      self ! PoisonPill
    }
  }
  private def processNext(operationDescriptor: OperationDescriptor, processContext: ProcessContext) = {
    // operationDescriptor.parameters ++ processContext.parameters
    val op = if (operationDescriptor.name == null) context.actorOf(props(operationDescriptor.className, operationDescriptor.parameters))
    else context.actorOf(props(operationDescriptor.className, operationDescriptor.parameters), operationDescriptor.name)
    LOG.debug("operation actor=" + op)
    op ! new OperationRequest(operationDescriptor.parameters, processContext)
  }

  private def props(className: String, parameters: Map[String, Object]): Props = {
    Props.apply(Class.forName(className))
  }
}
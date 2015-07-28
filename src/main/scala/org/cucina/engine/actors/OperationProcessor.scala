package org.cucina.engine.actors

import org.cucina.engine.ProcessContext
import org.cucina.engine.actors.support.ActorFinder
import org.cucina.engine.definition.OperationDescriptor
import org.slf4j.LoggerFactory

import akka.actor.Actor
import akka.actor.Props
import akka.actor.Status.Failure
import akka.actor.actorRef2Scala

/**
 * @author levinev
 */

case class OperationDescriptorsWrap(operationDescriptors: Iterable[OperationDescriptor], processContext: ProcessContext) {
  require(processContext != null, "ProcessContext cannot be null")
}
case class OperationRequest(rocessContext: ProcessContext)
case class OperationResponse(processContext: ProcessContext)
trait OriginMessage
case class OperationComplete(processContext: ProcessContext) extends OriginMessage
case class OperationFailed(message: String, processContext: ProcessContext) extends OriginMessage

class OperationProcessor extends Actor with ActorFinder {
  private[this] val LOG = LoggerFactory.getLogger(getClass())

  def receive = {
    case OperationDescriptorsWrap(operationDescriptors, processContext) => {
      LOG.debug("Original sender " + sender)
      LOG.info("Me " + self)
      if (operationDescriptors == null) {
        LOG.debug("No OperationDescriptor")
        sender ! new Failure(new IllegalArgumentException("No operationDescriptor"))
      } else {
        val els: Iterator[OperationDescriptor] = operationDescriptors.iterator
        processContext.operationIterator = els
        processContext.stackCaller = sender()
        processEls(processContext)
      }
    }
    case OperationResponse(processContext) => processEls(processContext)
    case of @ OperationFailed(_, pc) => sendToOrigin(of, pc)
  }

  private def processEls(processContext: ProcessContext) = {
    val operationDescriptor = processContext.nextOperationDescriptor()
    if (operationDescriptor != null) {
      LOG.info("operationDescriptor:" + operationDescriptor)
      processNext(operationDescriptor, processContext)
    } else {
      sendToOrigin(new OperationComplete(processContext), processContext)
    }
  }

  private def sendToOrigin(om: OriginMessage, pc: ProcessContext) = {
    // TODO null check
    pc.stackCaller ! om
  }

  private def processNext(operationDescriptor: OperationDescriptor, processContext: ProcessContext) = {
    // operationDescriptor.parameters ++ processContext.parameters
    val op = findActor(operationDescriptor, context)
    LOG.debug("operation actor=" + op)
    op ! new OperationRequest(processContext)
  }

  private def props(className: String, parameters: Map[String, Object]): Props = {
    // TODO 
    Props.apply(Class.forName(className), parameters)
  }
}
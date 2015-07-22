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

case class OperationDescriptorsWrap(operationDescriptors: Iterable[OperationDescriptor], processContext: ProcessContext) {
  require(processContext != null, "ProcessContext cannot be null")
}
case class OperationRequest(operationParameters: Map[String, Object], processContext: ProcessContext)
case class OperationResponse(processContext: ProcessContext)
trait OriginMessage
case class OperationComplete() extends OriginMessage
case class OperationFailed(message: String, processContext: ProcessContext) extends OriginMessage

class OperationProcessor extends Actor {
  private[this] val LOG = LoggerFactory.getLogger(getClass())
  def receive = {
    case OperationDescriptorsWrap(operationDescriptors, processContext) => {
      LOG.debug("Original sender " + sender)
      LOG.info("Me " + self)
      if (operationDescriptors == null) {
        LOG.debug("No OperationDescriptor")
        sender ! new Failure(new IllegalArgumentException("No operationDescriptor"))
        //        self ! PoisonPill
      } else {
        val els: Iterator[OperationDescriptor] = operationDescriptors.iterator
        processContext.operationIterator = els
        processContext.originalSender = sender()
        processEls(processContext)
      }
    }
    case OperationResponse(processContext) => processEls(processContext)
    case of @ OperationFailed(_, pc) => {
      println(pc)
      sendToOrigin(of, pc)
    }
  }

  private def processEls(processContext: ProcessContext) = {
    val operationDescriptor = processContext.nextOperationDescriptor()
    if (operationDescriptor != null) {
      LOG.debug("operationDescriptor:" + operationDescriptor)
      processNext(operationDescriptor, processContext)
    } else {
      sendToOrigin(new OperationComplete, processContext)
    }
  }

  private def sendToOrigin(om: OriginMessage, pc: ProcessContext) = {
    // TODO null check
    pc.originalSender ! om
    //    self ! PoisonPill
  }

  private def processNext(operationDescriptor: OperationDescriptor, processContext: ProcessContext) = {
    // operationDescriptor.parameters ++ processContext.parameters
    val op = if (operationDescriptor.name == null) context.actorOf(props(operationDescriptor.className, operationDescriptor.parameters))
    else context.actorOf(props(operationDescriptor.className, operationDescriptor.parameters), operationDescriptor.name)
    LOG.debug("operation actor=" + op)
    op ! new OperationRequest(operationDescriptor.parameters, processContext)
  }

  private def props(className: String, parameters: Map[String, Object]): Props = {
    // TODO 
    Props.apply(Class.forName(className), parameters)
  }
}
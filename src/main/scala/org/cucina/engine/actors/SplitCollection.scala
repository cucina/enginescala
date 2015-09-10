package org.cucina.engine.actors

import ognl.Ognl

import org.cucina.engine.{ExecuteComplete, ExecuteFailed, ProcessContext}
import org.cucina.engine.definition._
import org.slf4j.LoggerFactory

import akka.actor._

/**
 * @author levinev
 */

class SplitCollection(name: String,
                      transition: TransitionDescriptor,
                      listeners: Seq[String] = List(),
                      enterOperations: Seq[OperationDescriptor] = Nil,
                      leaveOperations: Seq[OperationDescriptor] = Nil,
                      collectionExpression: String)
  extends AbstractState(name, transition :: Nil, listeners, enterOperations, leaveOperations) {
  private val LOG = LoggerFactory.getLogger(getClass)
  LOG.info("Eval expr=" + collectionExpression)

  def this(name: String,
           transitions: Seq[TransitionDescriptor],
           listeners: Seq[String],
           enterOperations: Seq[OperationDescriptor],
           leaveOperations: Seq[OperationDescriptor],
           parameters: Map[String, String]) = {
    this(name, transitions.head, listeners, enterOperations, leaveOperations, parameters.get("collectionExpression").get)
  }

  def processStackRequest(pc: ProcessContext, stack: Seq[ActorRef]) = {
    try {
      val coll = Ognl.getValue(collectionExpression, pc)

      assert(coll.isInstanceOf[Seq[Object]], "Result of expression '" + collectionExpression + "' should be a Seq but is " + coll)
      val launcher = context.actorOf(Props(classOf[SplitLauncher], sender, pc))
      launcher forward CollectionLaunch(coll.asInstanceOf[Seq[Object]], findTransition(transition.name))
    } catch {
      case e: Throwable => LOG.error("Oops", e)
//        pc.client ! ExecuteFailed()
    }
  }
}

object SplitCollection {
  def props(name: String, transition: TransitionDescriptor,
            listeners: Seq[String] = List(),
            enterOperations: Seq[OperationDescriptor] = List(),
            leaveOperations: Seq[OperationDescriptor] = List(), collectionExpression: String): Props = {
    Props(classOf[SplitCollection], name, transition, listeners, enterOperations, leaveOperations, collectionExpression)
  }
}


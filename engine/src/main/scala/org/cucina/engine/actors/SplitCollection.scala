package org.cucina.engine.actors

import akka.actor.{Props, ActorRef}
import ognl.Ognl
import org.cucina.engine.{ExecuteFailed, ExecuteComplete, ProcessContext}
import org.cucina.engine.definition.{TransitionDescriptor, OperationDescriptor}
import org.slf4j.LoggerFactory

/**
  * @author levinev
  */

class SplitCollection(name: String, join: String,
                      transition: TransitionDescriptor,
                      listeners: Seq[String] = List(),
                      enterOperations: Seq[OperationDescriptor] = Nil,
                      leaveOperations: Seq[OperationDescriptor] = Nil,
                      collectionExpression: String)
  extends AbstractSplit(name, join, transition :: Nil, listeners, enterOperations, leaveOperations) {
  LOG.info("Eval expr=" + collectionExpression)

  def this(name: String, join: String,
           transitions: Seq[TransitionDescriptor],
           listeners: Seq[String],
           enterOperations: Seq[OperationDescriptor],
           leaveOperations: Seq[OperationDescriptor],
           parameters: Map[String, String]) = {
    this(name, join, transitions.head, listeners, enterOperations, leaveOperations, parameters.get("collectionExpression").get)
  }

  def splitter(pc: ProcessContext): Iterable[(Object, ActorRef, String)] = {
    try {
      val coll = Ognl.getValue(collectionExpression, pc)
      assert(coll.isInstanceOf[Seq[String]], "Result of expression '" + collectionExpression + "' should be a Seq but is " + coll)
      coll.asInstanceOf[Seq[Object]].map { obj => (obj, findTransition(transition.name), "") }
    } catch {
      case e: Throwable =>
        LOG.error("Oops", e)
        //pc.client ! ExecuteFailed()
        throw e
    }
  }
}

object SplitCollection {
  def props(name: String, join: String, transition: TransitionDescriptor,
            listeners: Seq[String] = List(),
            enterOperations: Seq[OperationDescriptor] = List(),
            leaveOperations: Seq[OperationDescriptor] = List(), collectionExpression: String): Props = {
    Props(classOf[SplitCollection], name, join, transition, listeners, enterOperations, leaveOperations, collectionExpression)
  }
}


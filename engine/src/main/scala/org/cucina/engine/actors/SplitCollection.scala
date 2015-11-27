package org.cucina.engine.actors

import akka.actor.{Props, ActorRef}
import ognl.Ognl
import org.cucina.engine.{ExecuteComplete, ProcessContext}
import org.cucina.engine.definition.{TransitionDescriptor, OperationDescriptor}
import org.slf4j.LoggerFactory

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
      val launcher = context.actorOf(Props(classOf[SplitLauncher], self, pc))
      launcher forward CollectionLaunch(coll.asInstanceOf[Seq[Object]], findTransition(transition.name))
    } catch {
      case e: Throwable => LOG.error("Oops", e)
//        pc.client ! ExecuteFailed()
    }
  }

  override def receiveLocal: Receive = {
    case a:ExecuteComplete =>
      // find the only transition and do it
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

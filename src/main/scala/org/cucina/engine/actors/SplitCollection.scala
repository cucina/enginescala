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
      val launcher = context.actorOf(Props(classOf[SplitLaunch], sender, pc))
      launcher forward CollectionLaunch(coll.asInstanceOf[Seq[Object]], findTransition(transition.name))
    } catch {
      case e: Throwable => LOG.error("Oops", e)
    }
  }
}

object SplitCollection {
  def props(name: String, transitions: Seq[TransitionDescriptor],
            collectionExpression: String,
            listeners: Seq[String] = List(),
            enterOperations: Seq[OperationDescriptor] = List(),
            leaveOperations: Seq[OperationDescriptor] = List()): Props = {
    require(transitions != null && transitions.nonEmpty, "Transitions is empty")
    require(transitions.size == 1, "Transitions should have exactly one member")
    Props(classOf[SplitCollection], name, transitions.head, collectionExpression, listeners, enterOperations, leaveOperations)
  }
}

case class CollectionLaunch(collection: Seq[Object], transition: ActorRef)

class SplitLaunch(mySender: ActorRef, parentPc: ProcessContext) extends Actor {
  private val LOG = LoggerFactory.getLogger(getClass)
  private var launched: Int = 0

  def receive = {
    case CollectionLaunch(coll, tr) =>
      coll.foreach[Unit](o => createToken(o, tr))
      LOG.info("Launched=" + launched)
    case ec@ExecuteComplete(pc) =>
      LOG.info("Received " + ec)
      launched -= 1
      if (launched == 0) {
        LOG.info("Last complete, notifying sender " + sender)
        mySender ! ExecuteComplete(parentPc)
        self ! PoisonPill
      }

    case ef@ExecuteFailed(c, f) =>
      LOG.info("Received " + ef)
      parentPc.token.children.empty
      mySender ! ExecuteFailed(c, f)
      self ! PoisonPill
  }

  private def createToken(o: Object, tr: ActorRef) = {
    LOG.info("Creating token for " + o)
    val t = Token(o, parentPc.token.processDefinition)
    parentPc.token.children += t
    t.parent = Some(parentPc.token)
    val processContext = ProcessContext(t, parentPc.parameters, parentPc.client)
    tr ! StackRequest(processContext, List())
    launched += 1
  }
}

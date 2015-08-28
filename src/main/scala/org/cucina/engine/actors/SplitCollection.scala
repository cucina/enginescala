package org.cucina.engine.actors

import com.googlecode.scalascriptengine.EvalCode

import org.cucina.engine.{ExecuteComplete, ExecuteFailed, ProcessContext}
import org.cucina.engine.definition._
import org.slf4j.LoggerFactory

import akka.actor._

/**
 * @author levinev
 */

class SplitCollection(name: String,
                      transition: TransitionDescriptor,
                      collectionExpression: String,
                      listeners: Seq[String] = List(),
                      enterOperations: Seq[OperationDescriptor] = Nil,
                      leaveOperations: Seq[OperationDescriptor] = Nil)
  extends AbstractState(name, transition :: Nil, listeners, enterOperations, leaveOperations) {
  private val LOG = LoggerFactory.getLogger(getClass)

  def processStackRequest(pc: ProcessContext, stack: Seq[ActorRef]) = {
    try {
      println("Eval expr=" + collectionExpression)
      val ect = EvalCode.with1Arg[ProcessContext, AnyRef]("pc", collectionExpression)
      val v = ect.newInstance
      val coll = v(pc)
      assert(coll.isInstanceOf[Seq[Object]], "Result of expression '" + collectionExpression + "' should be a Seq but is " + coll)
      val launcher = context.actorOf(Props[SplitLaunch])
      launcher forward CollectionLaunch(coll.asInstanceOf[Seq[Object]], pc, findTransition(transition.name))
    } catch {
      case e:Throwable => LOG.error("Oops", e)
    }
  }
}

object SplitCollection {
  def props(name: String, transition: TransitionDescriptor,
            collectionExpression: String,
            listeners: Seq[String] = List(),
            enterOperations: Seq[OperationDescriptor] = List(),
            leaveOperations: Seq[OperationDescriptor] = List()): Props = {
    Props(classOf[SplitCollection], name, transition, collectionExpression, listeners, enterOperations, leaveOperations)
  }
}

case class CollectionLaunch(collection: Seq[Object], processContext: ProcessContext, transition: ActorRef)

class SplitLaunch extends Actor {
  private val LOG = LoggerFactory.getLogger(getClass)
  private var launched: Int = 0
  private var parentPc: ProcessContext = _

  def receive = {
    case CollectionLaunch(coll, pc, tr) =>
      parentPc = pc
      coll.foreach[Unit](o => createToken(o, pc, tr))
      LOG.info("Launched=" + launched)
    case ec@ExecuteComplete(pc) =>
      LOG.info("Received " + ec)
      launched -= 1
      if (launched == 0) {
        sender ! ExecuteComplete(parentPc)
        self ! PoisonPill
      }

    case ef@ExecuteFailed(c, f) =>
      LOG.info("Received " + ef)
      sender ! ExecuteFailed(c, f)
      self ! PoisonPill
  }

  private def createToken(o: Object, pc: ProcessContext, tr: ActorRef) = {
    LOG.info("Creating token for " + o)
    val t = Token(o, pc.token.processDefinition)
    pc.token.children + t
    val processContext = ProcessContext(t, pc.parameters, pc.client)
    tr ! StackRequest(processContext, List())
    launched += 1
  }

}

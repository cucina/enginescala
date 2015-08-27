package org.cucina.engine.actors

import com.googlecode.scalascriptengine.EvalCode

import org.cucina.engine.{ExecuteFailed, ProcessContext}
import org.cucina.engine.definition._
import org.slf4j.LoggerFactory

import akka.actor.{ActorRef, Terminated, Actor, Props}

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
      println("Eval expr=" + ect)
      val v = ect.newInstance
      val coll = v(pc)
      println("coll=" + coll)
      assert(coll.isInstanceOf[Seq[Object]], "Result of expression '" + collectionExpression + "' should be a Seq")
      val seq = coll.asInstanceOf[Seq[Object]]
      seq.foreach[Unit](o => createToken(o, pc))
    } catch {
      case e => LOG.error("Oops", e)
    }
  }

  private def createToken(o:Object, pc:ProcessContext) = {
    LOG.info("Creating token for " + o)
  }
}

object SplitCollection {
  def props(name: String, transition: TransitionDescriptor,
           collectionExpression:String,
            listeners: Seq[String] = List(),
            enterOperations: Seq[OperationDescriptor] = List(),
            leaveOperations: Seq[OperationDescriptor] = List()): Props = {
    Props(classOf[SplitCollection], name, transition, collectionExpression, listeners, enterOperations, leaveOperations)
  }
}

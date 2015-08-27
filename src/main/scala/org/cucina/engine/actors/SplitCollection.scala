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
    val ect = EvalCode.with1Arg[ProcessContext, AnyRef]("pc", collectionExpression)
    val v= ect.newInstance
    val coll = v(pc)
    assert(coll.isInstanceOf[Seq], "Result of expression '" + collectionExpression + "' should be a Seq")
    val seq = coll.asInstanceOf[Seq[Object]]
    seq.foreach[Unit](o => createToken(o, pc))
  }

  private def createToken(o:Object, pc:ProcessContext) = {
    LOG.info("Creating token for " + o)
  }
}

object SplitCollection {
  def props(name: String, transitions: Seq[TransitionDescriptor],
            listeners: Seq[String] = List(),
            enterOperations: Seq[OperationDescriptor] = List(),
            leaveOperations: Seq[OperationDescriptor] = List()): Props = {
    Props(classOf[State], name, transitions, listeners, enterOperations, leaveOperations)
  }
}

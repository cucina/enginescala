package org.cucina.engine.actors

import akka.actor.ActorRef
import org.cucina.engine.ProcessContext
import org.cucina.engine.actors.support.ActorFinder
import org.cucina.engine.definition.{OperationDescriptor, TransitionDescriptor, Token}
import org.slf4j.LoggerFactory

/**
  * Created by levinev on 30/11/2015.
  */
abstract class AbstractSplit(name: String,
                             join: String,
                             transitions: Seq[TransitionDescriptor],
                             listeners: Seq[String] = List(),
                             enterOperations: Seq[OperationDescriptor] = Nil,
                             leaveOperations: Seq[OperationDescriptor] = Nil)
  extends AbstractState(name, transitions, listeners, enterOperations, leaveOperations) with ActorFinder {

  def processStackRequest(pc: ProcessContext, stack: Seq[ActorRef]) = {
    val joinActor = findActor(join)
    joinActor match {
      case None => throw new IllegalArgumentException("Failed to find join state '" + join + "'")
      case Some(_) =>
    }
    pc.token.stateId = join
    enterStack.head forward new StackRequest(pc, enterStack.tail)
    splitter(pc).foreach(s => createToken(s._1, s._2, s._3, pc, joinActor.get))
  }

  /**
    *
    * @param pc
    * @return collection of tuples containing - Object to use in branch, Transition to invoke on it
    *         and additional token qualifier to help equlsTo to ditinguish between different tokens
    */
  def splitter(pc: ProcessContext): Iterable[(Object, ActorRef, String)]

  private def createToken(o: Object, tr: ActorRef, splitId: String, parentPc: ProcessContext, joinActor: ActorRef) = {
    val t = Token(o, parentPc.token.processDefinition, splitId)
    LOG.info("Created token " + t)
    parentPc.token.children += t
    t.parent = Some(parentPc.token)
    val processContext = ProcessContext(t, parentPc.parameters, parentPc.client)
    tr ! StackRequest(processContext, List())
    joinActor ! SplitToken(processContext)
  }
}

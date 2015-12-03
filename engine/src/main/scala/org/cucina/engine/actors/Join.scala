package org.cucina.engine.actors

import akka.actor.{Props, ActorRef}
import org.cucina.engine.{ExecuteFailed, ProcessContext}
import org.cucina.engine.definition._

import scala.collection.mutable

case class Evt(token: Token, op: String)

case class AddToken(token: Token)

case class RemoveToken(token: Token)

case class TokensInProcess(tokens: mutable.Set[Token] = mutable.Set[Token]()) {
  def add(token: Token): TokensInProcess = copy(tokens += token)

  def find(token: Token): Option[Token] = tokens.find(_ == token)

  def size: Int = tokens.size

  override def toString: String = tokens.toString
}

/**
  * This process element joins previously created flows back into a single stream.
  * Note that discards subflows upon successful entry, so if it desirable to save some states, for example dealing
  * with subflows upon members of a collection, an operation should be provided in the enter operation stack.
  *
  * Once all subflows have joined, the only transition will be taken for the parent object.
  *
  * @param name
  * @param transition
  * @param listeners
  * @param enterOperations
  * @param leaveOperations
  *
  * @author levinev
  */
class Join(name: String,
           transition: TransitionDescriptor,
           listeners: Seq[String] = List(),
           enterOperations: Seq[OperationDescriptor] = Nil,
           leaveOperations: Seq[OperationDescriptor] = Nil)
  extends AbstractState(name, transition :: Nil, listeners, enterOperations, leaveOperations) {

  var parents = TokensInProcess()

  def this(name: String,
           transitions: Seq[TransitionDescriptor],
           listeners: Seq[String],
           enterOperations: Seq[OperationDescriptor],
           leaveOperations: Seq[OperationDescriptor]) = {
    this(name, transitions.head, listeners, enterOperations, leaveOperations)
  }

  def processStackRequest(pc: ProcessContext, stack: Seq[ActorRef]) = {
    if (stack.nonEmpty) {
      LOG.warn("Join '" + name + "' should be a terminal actor in the stack, but the stack was " + stack)
      sender ! ExecuteFailed(pc.client, "Join '" + name + "' should be a terminal actor in the stack")
    }
    LOG.info("Came to join:" + pc.token)
    val parent = pc.token.parent

    parent match {
      case None => LOG.warn("Execution came to join without parent:" + pc)
      case Some(pt) =>
        parents.find(pt) match {
          case Some(t) =>
            t.children -= pc.token
            pc.token.parent = Some(t)
          case None =>
            LOG.warn("Token's parent is not in the list of already existing, adding " + pt)
            parents.add(pt)
        }

        val ptt = parent.get
        if (ptt.stateId != this.name) LOG.warn("Execution came to join '" + name + "' but parent is here '" + ptt.stateId + "'")

        if (ptt.children isEmpty) {
          // TODO inject join strategy
          LOG.info("all tokens arrived")

          // TODO make it persistent so the mapping could get restored
          val leaves = leaveStack :+ findTransition(transition.name)
          leaves.head forward new StackRequest(new ProcessContext(pt, pc.parameters, pc.client), leaves.tail)
        }
    }
  }
}

object Join {
  def props(name: String, transitions: Seq[TransitionDescriptor],
            listeners: Seq[String] = List(),
            enterOperations: Seq[OperationDescriptor] = List(),
            leaveOperations: Seq[OperationDescriptor] = List()): Props = {
    require(transitions != null && transitions.nonEmpty, "Transitions is empty")
    require(transitions.size == 1, "Transitions should have exactly one member")
    Props(classOf[Join], name, transitions.head, listeners, enterOperations, leaveOperations)
  }
}
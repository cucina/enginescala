package org.cucina.engine.actors

import org.cucina.engine.{ExecuteComplete, ExecuteFailed, ProcessContext}
import org.cucina.engine.definition._
import org.slf4j.LoggerFactory

import akka.actor.{PoisonPill, ActorRef, Actor, Props}

import scala.collection.mutable

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
  private val LOG = LoggerFactory.getLogger(getClass)

  def this(name: String,
           transitions: Seq[TransitionDescriptor],
           listeners: Seq[String],
           enterOperations: Seq[OperationDescriptor],
           leaveOperations: Seq[OperationDescriptor]) = {
    this(name, transitions.head, listeners, enterOperations, leaveOperations)
  }

  // a bit of a state
  val joiners:mutable.HashMap[Token, ActorRef] = new mutable.HashMap[Token, ActorRef]

  def processStackRequest(pc: ProcessContext, stack: Seq[ActorRef]) = {
    if (stack.nonEmpty) {
      LOG.warn("Join '" + name + "' should be a terminal actor in the stack, but the stack was " + stack)
      sender ! ExecuteFailed(pc.client, "Join '" + name + "' should be a terminal actor in the stack")
    }
    LOG.info("Came to join:"+ pc.token)
    pc.token.parent match {
      case Some(parent) =>
        // TODO make it persistent so the mapping could get restored
        val leaves = leaveStack :+ findTransition(transition.name)
        val joiner = joiners.getOrElse(parent, context.actorOf(Props(classOf[Joiner], leaves)))
        // create Joiner mapped to the parent
        joiners += (parent -> joiner)
        // execute enter ops appending a temp actor to handle next step below
        val mestack: Seq[ActorRef] = enterStack :+ joiner
        LOG.info("Stack=" + mestack)
        mestack.head forward new StackRequest(pc, mestack.tail)
      case None =>
        LOG.warn("Attempted to execute join with a parentless context")
        sender ! ExecuteFailed(pc.client, "Attempted to execute join with a parentless context")
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

/**
 * A quickie actor to handle end of stack execution
 *
 * @param leaveStack
 */
class Joiner(leaveStack: Seq[ActorRef] = Nil) extends Actor {
  private val LOG = LoggerFactory.getLogger(getClass)
  // TODO collection of al children visited this
  // TODO make it persistent so the mapping could get restored

  def receive = {
    case StackRequest(pc, callerstack) =>
      pc.token.parent match {
        case Some(parent) =>
          pc.token.stateId = null
          sender ! ExecuteComplete(pc)
          assert(parent.hasChildren, "Cannot process this as parent does not have children")
          // TODO compare this list of children to the parent ones
          // and remove all of them at once
          parent.children.remove(pc.token)
          // if it is the last one, execute leave ops on parent
          if (parent.children.isEmpty) {
            // take the only transition
            val ppc = new ProcessContext(parent, pc.parameters, pc.client)
            leaveStack.head forward new StackRequest(ppc, leaveStack.tail)
            self ! PoisonPill
          }
        case None =>
          LOG.warn("Attempted to execute joiner with a parentless context")
          sender ! ExecuteFailed(pc.client, "Attempted to execute joiner with a parentless context")
      }
  }
}
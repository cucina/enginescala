package org.cucina.engine

import scala.collection.immutable.HashSet
import scala.collection.mutable.Map

import org.cucina.engine.definition._
import org.slf4j.LoggerFactory

import akka.actor.ActorRef

/**
 * @author vlevine
 */
class ProcessSession(tokenFactory: ActorRef) {
  private[this] val LOG = LoggerFactory.getLogger(getClass())
  private[this] val ERROR_CONTEXT_IS_REQUIRED = s"Context is required."

  /**
   * Gets all available {@link org.cucina.engine.definition.Transition
   * Transitions} out of the current state of the workflow instance identified
   * by the {@link WorkflowInstanceHandle} in the supplied
   * {@link ExecutionContext}.
   */
  def getAvailableTransitions(processContext: ProcessContext): Set[TransitionDescriptor] = {
    val token = processContext.token

    if (token.hasChildren()) {
      // this token does not have life of its own until all children are
      // dead.
      new HashSet
    } else {
      val currentState = token.processDefinition.findState(token.stateId)
      //currentState.getEnabledTransitions(processContext)
      //currentState.allTransitions
      null
    }
  }

  /**
   * Delegates to {@link #doSignal}.
   */
  def signal(processContext: ProcessContext, transition: TransitionDescriptor): Unit = {
    require(processContext != null, ERROR_CONTEXT_IS_REQUIRED)
    require(transition != null, "Cannot move to a null transition")

    doSignal(processContext, transition)
  }

  /**
   * Delegates to {@link #doSignal}.
   */
  @throws(classOf[SignalFailedException])
  @throws(classOf[TransitionNotFoundException])
  def signal(processContext: ProcessContext, transitionId: String): Unit = {
    require(processContext != null, ERROR_CONTEXT_IS_REQUIRED)

    signal(processContext, ProcessSession.findTransition(processContext.token, transitionId))
  }

  /**
   * Starts a new workflow instance for the supplied
   * <code>PersistableObject</code>. The <code>WorkflowInstanceHandle</code>
   * associated with the newly created instance can be accessed from the
   * supplied <code>ExecutionContext</code>.
   * <p/>
   */
  /* @throws(classOf[SignalFailedException])
  @throws(classOf[TransitionNotFoundException])
  def startProcessInstance(processDefinition: ProcessDefinition, domainObject: Object, transitionId: String,
    parameters: Map[String, Object]): Token = {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Object=" + domainObject)
    }
    tokenFactory ! new StartProcess(processDefinition, domainObject, transitionId, parameters, self)
    
  }*/

  /**
   * Leaves the input {@link State} of the specified {@link Transition}
   * provided that {@link State} is a valid state of the current workflow
   * instance.
   * <p/>
   * Delegates to the {@link WorkflowInstanceHandleDao} to persist state
   * changes to the {@link WorkflowInstanceHandle}.
   */
  @throws(classOf[CheckNotMetException])
  @throws(classOf[SignalFailedException])
  private def doSignal(processContext: ProcessContext, transition: TransitionDescriptor) = {
    val token: Token = processContext.token

    require(token != null, "Null token in the executionContext")

    if (LOG.isDebugEnabled()) {
      LOG.debug("token = " + token)
    }

    val currentstate: StateDescriptor = token.processDefinition.findState(token.stateId)

    try {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Before leaving place " + token.stateId + " transition.id=" +
          transition.name)
      }

      //currentstate.leave(transition, processContext)

      if (LOG.isDebugEnabled()) {
        LOG.debug("After leaving place id=" + token.stateId)
      }
    } catch {
      case ex: CheckNotMetException => throw ex
      case e: Exception =>
        var id = "unknown"

        if (currentstate != null) {
          id = token.stateId
        }

        throw new SignalFailedException("Unable to signal end of state [" + id + "]. See nested exception for more details", Some(e))
    }
  }
}

object ProcessSession {
  /**
   * Finds a transition corresponding to the specified ID using the state of
   * the supplied {@link Token}. If the {@link Token} has no children then the
   * transition will be resolved against the current {@link State} of the
   * {@link Token} itself. If the {@link Token} does have children then this
   * method will search for the transition across the {@link State places}
   * associated with the child {@link Token tokens}.
   */
  @throws(classOf[SignalFailedException])
  @throws(classOf[TransitionNotFoundException])
  def findTransition(token: Token, transitionId: String): TransitionDescriptor = {
    //token.processDefinition.findState(token.stateId).getTransition(transitionId)
    null
  }
}
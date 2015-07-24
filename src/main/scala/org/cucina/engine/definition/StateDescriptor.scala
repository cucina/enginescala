package org.cucina.engine.definition

import org.cucina.engine.ProcessContext
import scala.collection.immutable.Set
import java.lang.Boolean

/**
 * @author levinev
 */
class StateDescriptor(
    val id: String,
    val allTransitions: Set[TransitionDescriptor],
    val enterOperations: Set[OperationDescriptor],
    val leaveOperations: Set[OperationDescriptor]) {

  /*  def enter(from: TransitionDescriptor, processContext: ProcessContext)
  def leave(transition: TransitionDescriptor, processContext: ProcessContext)
  def getTransition(transitionId: String): TransitionDescriptor
  def getEnabledTransitions(processContext: ProcessContext): Set[TransitionDescriptor] = {
    allTransitions.filter { _.isEnabled(processContext) }
  }

  */
  /**
   * Indicates whether, given the supplied {@link ProcessContext} this
   * <code>State</code> can be left. Does not take into consideration whether
   * the {@link Transition} transitions associated with this
   * <code>State</code> are active or not.
   * <p/>
   * Default implementation simply checks to see whether the {@link Token} for
   * the supplied {@link ProcessContext} sits at this <code>State</code>.
   *
   */ /*
  def canLeave(processContext: ProcessContext): Boolean = {
    id.equals(processContext.token.stateId)
  }

  def enterInternal(processContext: ProcessContext) {
    require(processContext != null, "ExecutionContext cannot be null")

    val token = processContext.token

    require(token != null, "Token cannot be null")

    token.stateId = id
  }
*/
}
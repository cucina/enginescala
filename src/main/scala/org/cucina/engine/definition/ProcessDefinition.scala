package org.cucina.engine.definition

import org.cucina.engine.SignalFailedException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import scala.collection.mutable.HashMap
import scala.collection.mutable.Map

/**
 * Describes an entire workflow as network-representation based on Petri-nets.
 * Each state in the workflow is represented as a {@link State} and paths
 * between states are represented as {@link Transition Transitions}.
 *
 * @see State
 * @see Transition
 */
class ProcessDefinition(val startState:String, description:String, val id:String) {
  private[this] val states: Map[String, StateDescriptor] = new HashMap[String, StateDescriptor]

  /**
   * JAVADOC.
   *
   */
  def setAllStates(statesArray: Array[StateDescriptor]) = {
    for (s: StateDescriptor <- statesArray) {
      registerState(s)
    }
  }

  /**
   * Retrieves all the {@link State} that are part of this
   * <code>ProcessDefinition</code>.
   */
  def getAllStates(): Iterable[StateDescriptor] = {
    states.values
  }

  /**
   * Finds a {@link State} that is part of this
   * <code>WorkflowDefinition</code> by ID.
   */
  def findState(stateId: String): StateDescriptor = {
    val place: StateDescriptor = states.get(stateId).get

    if (place == null) {
      throw new SignalFailedException("Failed to find state named '" + stateId + "' in workflow '" + id + "'")
    }

    place
  }

  /**
   * Registers a {@link State} as part of the <code>WorkflowDefinition</code>.
   */
  private def registerState(state: StateDescriptor) = {
    states.put(state.name, state)
  }
}

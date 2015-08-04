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
case class ProcessDefinition(val states: List[StateDescriptor], val startState: String, description: String, val id: String) {

  /**
   * Finds a {@link State} that is part of this <code>ProcessDefinition</code> by id.
   */
  def findState(stateId: String): StateDescriptor = {
    val place = states.filter(_.name == stateId).head
    if (place == null) {
      throw new SignalFailedException("Failed to find state named '" + stateId + "' in workflow '" + id + "'")
    }

    place
  }
}

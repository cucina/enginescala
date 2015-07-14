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
class ProcessDefinition {
  private[this] val states: Map[String, State] = new HashMap[String, State]
  var startState: State
  var description: String
  var id: String

  /**
   * JAVADOC.
   *
   * @param places
   *            JAVADOC.
   */
  def setAllStates(statesArray: Array[State]) = {
    for (s: State <- statesArray) {
      registerState(s)
    }
  }

  /**
   * Retrieves all the {@link State} that are part of this
   * <code>ProcessDefinition</code>.
   */
  def getAllPlaces(): Iterable[State] = {
    states.values
  }

  /**
   * Finds a {@link State} that is part of this
   * <code>WorkflowDefinition</code> by ID.
   */
  def findState(stateId: String): State = {
    val place: State = states.get(stateId).get

    if (place == null) {
      throw new SignalFailedException("Failed to find state named '" + stateId + "' in workflow '" + id + "'")
    }

    place
  }

  /**
   * Registers a {@link State} as part of the <code>WorkflowDefinition</code>.
   */
  private def registerState(state: State) = {
    states.put(state.id, state)
  }

  /*private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        id = (String) in.readObject()

        ProcessDefinitionRegistry registry = DefaultProcessEnvironment.instance()
                                                                      .getDefinitionRegistry()

        Assert.notNull(registry, "Failed to find workflowDefinitionRegistry in application context")

        ProcessDefinition wfd = registry.findWorkflowDefinition(id)

        Assert.notNull(wfd, "Failed to find workflow with id:'" + id + "'")
        startState = wfd.getStartState()
        setAllPlaces(wfd.getAllPlaces())
    }

    private void writeObject(ObjectOutputStream os)
        throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Serializing...")
        }

        os.writeObject(id)

        if (LOG.isDebugEnabled()) {
            LOG.debug("Finished.")
        }
    }*/
}

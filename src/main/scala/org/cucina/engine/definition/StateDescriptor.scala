package org.cucina.engine.definition

import org.cucina.engine.ProcessContext
import org.cucina.engine.actors.StateActor
import scala.collection.immutable.Set
import java.lang.Boolean

/**
 * @author levinev
 */
class StateDescriptor(val className: String = classOf[StateActor].getName, val name: String, val arguments: Seq[Any])
  extends ProcessElementDescriptor

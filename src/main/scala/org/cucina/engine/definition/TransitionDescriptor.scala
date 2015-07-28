package org.cucina.engine.definition

import org.cucina.engine.actors.TransitionActor


/**
 * @author levinev
 */
class TransitionDescriptor(val className: String = classOf[TransitionActor].getName,
                           val name: String,
                           val arguments: Seq[Any]) extends ProcessElementDescriptor

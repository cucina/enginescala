package org.cucina.engine.definition

import org.cucina.engine.actors.EnterPublisher

/**
 * Created by levinev on 28/07/2015.
 */
class EnterPublisherDescriptor(val className: String = classOf[EnterPublisher].getName,
                               val name: String = null,
                               val arguments: Seq[Any] = null) extends StackableElementDescriptor

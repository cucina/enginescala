package org.cucina.engine.definition

import org.cucina.engine.ProcessContext

/**
 * @author levinev
 */
class CheckDescriptor(val className: String, val name: String = null, val arguments: Seq[Any] = Nil)
extends ProcessElementDescriptor 
package org.cucina.engine.definition

/**
 * @author levinev
 */
class OperationDescriptor(val className: String, val name: String = null, val arguments: Seq[Any] = Nil)
extends StackableElementDescriptor

package org.cucina.engine.definition

/**
 * @author levinev
 */
class OperationDescriptor(val className: String, val name: String = null, val parameters: Map[String, Object] = null) 
extends ProcessElementDescriptor 

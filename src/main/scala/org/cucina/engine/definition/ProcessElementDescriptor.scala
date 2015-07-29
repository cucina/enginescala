package org.cucina.engine.definition

/**
 * @author levinev
 */
trait ProcessElementDescriptor {
  val name: String
  val className: String

  override def toString: String = {
    val sb = new StringBuilder()
    sb.append("className='").append(className).append("' name='").append(name).append("'").toString()
  }
}
package org.cucina.engine.definition

/**
 * @author levinev
 */
trait ProcessElementDescriptor {
  val className: String
  val name: String
  val arguments: Seq[Any]

  override def toString: String = {
    val sb = new StringBuilder()
    sb.append("className='").append(className).append("' name='").append(name).append("' arguments='").append(arguments).append("'").toString()
  }
}
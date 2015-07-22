package org.cucina.engine.definition

/**
 * @author levinev
 */
trait ProcessElementDescriptor {
  def className:String
  def name:String
  def parameters: Map[String, Object]
  override def toString: String = {
    val sb = new StringBuilder()
    sb.append("className='").append(className).append("' name='").append(name).append("' parameters='").append(parameters).append("'").toString()
  }
}
package org.cucina.engine.definition

import akka.actor.Props

/**
 * @author levinev
 */
trait ProcessElementDescriptor {
  val name: String
  val className: String

  def props:Props = Props(Class.forName(className), name)

  override def toString: String = {
    val sb = new StringBuilder()
    sb.append("className='").append(className).append("' name='").append(name).append("'").toString()
  }
}
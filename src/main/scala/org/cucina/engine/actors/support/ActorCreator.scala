package org.cucina.engine.actors.support

import java.util.concurrent.TimeoutException

import akka.actor._
import org.cucina.engine.definition.ProcessElementDescriptor
import org.slf4j.LoggerFactory

/**
 * Created by levinev on 27/07/2015.
 *
 * Finds an existing actor from elemntDescriptor or creates a new one
 */
trait ActorCreator {
  private val LOG = LoggerFactory.getLogger(classOf[ActorCreator])

  def createActor(elementDescriptor: ProcessElementDescriptor)(implicit context: ActorContext): ActorRef = {
    val props = elementDescriptor.props
    LOG.info("Props:" + props)
    val c = if (elementDescriptor.name == null) context actorOf props else context actorOf(props, elementDescriptor.name)
    require(c != null, "Failed to create actor from " + elementDescriptor)
    context watch c
    LOG.info("Created actor:" + c)
    c
  }
}
package org.cucina.engine.actors.support

import akka.actor._
import akka.util.Timeout
import org.cucina.engine.definition.ProcessElementDescriptor
import org.slf4j.LoggerFactory
import scala.concurrent.duration._
import scala.concurrent.Await

/**
 * Created by levinev on 27/07/2015.
 *
 * Finds an existing actor from elemntDescriptor or creates a new one
 */
trait ActorFinder {
  private val LOG = LoggerFactory.getLogger(getClass)

  def findActor(elementDescriptor: ProcessElementDescriptor, context: ActorContext): ActorRef = {
    if (elementDescriptor.name == null) create(elementDescriptor, context)
    else {
      try {
        implicit val resolveTimeout = Timeout(500 millis)
        val actorRef = Await.result(context.actorSelection(elementDescriptor.name).resolveOne(), resolveTimeout.duration)
        LOG.info("Located actor:" + actorRef)
        actorRef
      } catch {
        case e: ActorNotFound => {
          create(elementDescriptor, context)
        }
      }
    }
  }

  private def create(elementDescriptor: ProcessElementDescriptor, context: ActorContext): ActorRef = {
    val props = propsBuild(elementDescriptor)
    LOG.info("Props:" + props)
    val c = if (elementDescriptor.name == null) context actorOf props else context actorOf(props, elementDescriptor.name)
    require(c != null, "ActorRef cannot be null")
    context watch c
    c
  }

  private def propsBuild(elementDescriptor: ProcessElementDescriptor): Props = {
    try {
      def clazz = Class.forName(elementDescriptor.className)
      LOG.info("Clazz:" + clazz)
      val args = elementDescriptor.arguments.toArray
      LOG.info("args:" + args.array)
      Props(clazz, args: _*)
    } catch {
      case e: Throwable => {
        LOG.warn("Failed to create an actor:", e)
        throw e
      }
    }
  }
}
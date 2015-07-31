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

  /// recursive search in this context and in its parent/grandparent
  def findActor(name: String)(implicit context: ActorContext): ActorRef = {
    val searchPaths = List(name, "../" + name, "../../" + name)
    search(searchPaths)
  }

  def search(paths: List[String])(implicit context: ActorContext): ActorRef = {
    paths match {
      case List() => null
      case path :: xs =>
        val ac = searchPath(path)
        if (ac == null) search(xs)
        else ac
    }
  }

  private def searchPath(path: String)(implicit context: ActorContext): ActorRef = {
    LOG.info("Path:" + path)
    try {
      // TODO parameter for timeout?
      implicit val resolveTimeout = Timeout(500 millis)
      val ac = context.actorSelection(path)
      val ro = ac.resolveOne()
      val actorRef = Await.result(ro, resolveTimeout.duration)
      LOG.info("Located actor:" + actorRef)
      actorRef
    } catch {
      case e: ActorNotFound => {
        LOG.warn("Failed to find actor by name '" + path + "'")
        null
      }
    }
  }

  def findActor(elementDescriptor: ProcessElementDescriptor)(implicit context: ActorContext): ActorRef = {
    if (elementDescriptor.name == null) {
      LOG.warn("Cannot find an actor without a name")
      null
    } else {
      findActor(elementDescriptor.name)
    }
  }

  def createActor(elementDescriptor: ProcessElementDescriptor)(implicit context: ActorContext): ActorRef = {
    val props = propsBuild(elementDescriptor)
    LOG.info("Props:" + props)
    val c = if (elementDescriptor.name == null) context actorOf props else context actorOf(props, elementDescriptor.name)
    require(c != null, "ActorRef cannot be null")
    context watch c
    LOG.info("Create actor:" + c)
    c
  }

  private def propsBuild(elementDescriptor: ProcessElementDescriptor): Props = {
    try {
      def clazz = Class.forName(elementDescriptor.className)
      LOG.info("Clazz:" + clazz)
      /*
            if (elementDescriptor.arguments != null) {
              LOG.info("args:" + elementDescriptor.arguments)
              Props(clazz, elementDescriptor.arguments: _*)
            } else {
      */
      if (elementDescriptor.name != null)
        Props(clazz, elementDescriptor.name)
      else
        Props(clazz)
      //      }
    } catch {
      case e: Throwable => {
        LOG.warn("Failed to create an actor:", e)
        throw e
      }
    }
  }
}
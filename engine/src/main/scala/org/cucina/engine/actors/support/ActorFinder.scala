package org.cucina.engine.actors.support

import java.util.concurrent.TimeoutException

import akka.actor.{ActorNotFound, ActorRefFactory, ActorContext, ActorRef}
import akka.util.Timeout
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Created by levinev on 27/07/2015.
 *
 * Finds an existing actor from elemntDescriptor or creates a new one
 */
trait ActorFinder {
  private val LOG = LoggerFactory.getLogger(classOf[ActorFinder])

  def findAndSend(name: String, event: Any)(implicit context: ActorContext): Unit = {
    findActor(name) match {
      case None => LOG.info("Failed to find actor named '" + name + "'")
      case Some(a) => a ! event
    }
  }

  /// recursive search in this context and in its parent/grandparent
  def findActor(name: String)(implicit context: ActorContext): Option[ActorRef] = {
    // TODO cache actors
    name match {
      case x if x startsWith "/" => search(List(x))(context.system)
      case y => search(List(y, "../" + y, "../../" + y))
    }
  }

  private def search(paths: List[String])(implicit context: ActorRefFactory): Option[ActorRef] = {
    paths match {
      case List() =>
        None
      case path :: xs =>
        searchPath(path) match {
          case None => search(xs)
          case opt => opt
        }
    }
  }

  private[this] val cache = new mutable.HashMap[String, ActorRef]()

  private def searchPath(path: String)(implicit context: ActorRefFactory): Option[ActorRef] = {
    cache.get(path) match {
      case s@Some(_) => s
      case None =>
        LOG.info("Path:" + path)
        try {
          // TODO parameter for timeout?
          implicit val resolveTimeout = Timeout(500 millis)
          val ac = context.actorSelection(path)

          val ro = ac.resolveOne()
          val actorRef = Await.result(ro, resolveTimeout.duration)
          LOG.info("Located actor:" + actorRef)
          actorRef match {
            case null => None
            case _ =>
              cache.put(path, actorRef)
              Some(actorRef)
          }
        } catch {
          case e: ActorNotFound =>
            LOG.warn("Failed to find actor by name '" + path + "'")
            None
          case e:TimeoutException =>
            LOG.warn("Timedout finding actor by name '" + path + "'", e)
            None
        }
    }
  }
}
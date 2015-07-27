package org.cucina.engine.actors.support

import akka.actor._
import akka.util.Timeout
import org.cucina.engine.actors.StateActor
import org.cucina.engine.definition.ProcessElementDescriptor
import org.slf4j.LoggerFactory
import scala.concurrent.duration._
import scala.concurrent.Await

/**
 * Created by levinev on 27/07/2015.
 */
trait ActorFinder {
  private val LOG = LoggerFactory.getLogger(getClass)

  def findActor(elementDescriptor: ProcessElementDescriptor, context: ActorContext): ActorRef = {
    try {
      implicit val resolveTimeout = Timeout(500 millis)
      val actorRef = Await.result(context.actorSelection(elementDescriptor.name).resolveOne(), resolveTimeout.duration)
      LOG.info("Located state:" + actorRef)
      actorRef
    } catch {
      case e: ActorNotFound => {
        val props = produce(elementDescriptor)
        LOG.info("Props:" + props)
        val c = context actorOf (props, elementDescriptor.name)
        require(c != null, "ActorRef cannot be null")
        context watch c
        c
      }
    }
  }

  private def produce(elementDescriptor: ProcessElementDescriptor): Props = {
    def clazz = Class.forName(elementDescriptor.className)

    LOG.info("Clazz:" + clazz)
/*
    def instantiate[T](clazz: java.lang.Class[T])(args: AnyRef*): T = {
      val constructor = clazz.getConstructors()(0)
      LOG.info("constructor:" + constructor)
      LOG.info("arguments:" + args)
      constructor.newInstance(args: _*).asInstanceOf[T]
    }
*/

    try {
      val args = elementDescriptor.arguments.toArray
      LOG.info("args:" + args.array)
      //      val instance = instantiate(clazz)(args.array).asInstanceOf[Actor]
      //      LOG.info("instance:" + instance)
      //      Props(instance)
      Props(clazz, args:_*)
    } catch {
      case e: Throwable => {
        LOG.warn("Failed to create an actor:", e)
        throw e
      }
    }
  }
}
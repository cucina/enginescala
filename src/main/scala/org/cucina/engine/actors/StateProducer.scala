package org.cucina.engine.actors

import akka.actor.{Actor, IndirectActorProducer}

/**
 * Created by levinev on 24/07/2015.
 */
class StateProducer(clazz: Class[_ <: Actor], args: AnyRef*) extends IndirectActorProducer {

  override def produce(): Actor = {
    def instantiate[T](clazz: java.lang.Class[T])(args: AnyRef*): T = {
      val constructor = clazz.getConstructors()(0)
      return constructor.newInstance(args: _*).asInstanceOf[T]
    }

    instantiate(clazz)(args)
  }

  override def actorClass: Class[_ <: Actor] = clazz
}

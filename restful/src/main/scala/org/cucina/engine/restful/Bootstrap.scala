/*
 * Copyright 2003-2015 Monitise Group Limited. All Rights Reserved.
 *
 * Save to the extent permitted by law, you may not use, copy, modify,
 * distribute or create derivative works of this material or any part
 * of it without the prior written consent of Monitise Group Limited.
 * Any reproduction of this material must contain this notice.
 */
package org.cucina.engine.restful

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import spray.can.Http

import scala.concurrent.duration._

object Bootstrap extends App {
  //private val config = ConfigBuilder("application.conf").build()

  implicit val system = ActorSystem("process-system")
  val service = system.actorOf(Props[WorkflowService], "rest-service")
//  val Interface = config.getString("mcp.sync-acceptor.listen-interface")
//  val Port = config.getInt("mcp.sync-acceptor.listen-port")

  implicit val timeout = Timeout(5.seconds)

  // Boot all components
  IO(Http) ? Http.Bind(service, interface = "localhost", port = 9999)
}

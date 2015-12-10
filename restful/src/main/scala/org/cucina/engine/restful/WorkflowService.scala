package org.cucina.engine.restful

import org.cucina.engine._
import spray.routing._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._


/**
  * Created by levinev on 07/12/2015.
  */
class WorkflowService extends HttpServiceActor with Routes {
  def receive = runRoute(allRoutes)

  def guardian = context.actorOf(ProcessGuardian.props())

  def startProcessOperation(body: StartProcessJson): Route = {
    requestResponse(body)
  }

  def makeTransitionOperation(body: MakeTransitionJson): Route = {
    requestResponse(body)
  }

  def getTransitionsOperation(body: GetAvailableTransitionsJson): Route = {
    requestResponse(body)
  }

  def addDefinitionOperation(body: AddDefinition): Route = {
    requestResponse(body)
  }

  def requestResponse(body: AnyRef): Route = {
    implicit val timeout = Timeout(30 seconds)
    val future = ask(guardian, body)
    complete(future)
  }
}

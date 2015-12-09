package org.cucina.engine.restful

import akka.pattern.ask
import org.cucina.engine._
import spray.routing._

/**
  * Created by levinev on 07/12/2015.
  */
class WorkflowService extends HttpServiceActor with Routes {
  def receive = runRoute(allRoutes)

  def guardian = context.actorOf(ProcessGuardian.props())

  def startProcessOperation(body: StartProcess): Route = {
    requestResponse(body)
  }

  def makeTransitionOperation(body: MakeTransition): Route = {
    requestResponse(body)
  }

  def getTransitionsOperation(body: GetAvailableTransitions): Route = {
    requestResponse(body)
  }

  def addDefinitionOperation(body: AddDefinition): Route = {
    requestResponse(body)
  }

  def requestResponse(body: AnyRef): Route = {
    // TODO sync to async
    val future = ask(guardian, body)
    future onSuccess {
      case posts => for (post <- posts) println(post)
    }
    complete()
  }
}

package org.cucina.engine.restful

import org.cucina.engine._
import spray.http.{StatusCodes, StatusCode}
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
    import spray.json._
    implicit val timeout = Timeout(30 seconds)
    val future = ask(guardian, body)

    future onSuccess {
      case pf: ProcessFailure => complete(StatusCodes.ClientError, pf.cause)
      case po@ _ => complete(po.toJson.compactPrint)
    }

    future onFailure {
      case e@_ => complete(StatusCodes.ServerError, e.toJson.compactPrint)
    }

  }
}

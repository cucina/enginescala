package org.cucina.engine.restful


import org.cucina.engine.{AddDefinition, GetAvailableTransitions, MakeTransition, StartProcess}
import spray.httpx.SprayJsonSupport._
import spray.json.DefaultJsonProtocol
import spray.routing.{HttpService, Route}

/**
  * Binding business operations to the HTTP paths
  *
  * @author vlevine
  */
trait Routes
  extends HttpService with DefaultJsonProtocol {

  lazy val allRoutes = startProcess ~ makeTransition ~ availableTransitions ~ addDefinition

  val startProcess = path("workflow" / "start") {
    post {
      // authenticate()
      // authorize()
      entity(as[StartProcess]) { body =>
        startProcessOperation(body)
      }
    }
  }

  def startProcessOperation(body: StartProcess): Route

  val makeTransition = path("workflow" / "transition") {
    post {
      entity(as[MakeTransition]) { body =>
        makeTransitionOperation(body)
      }
    }
  }

  def makeTransitionOperation(body: MakeTransition): Route

  val availableTransitions = path("workflow" / "transitions") {
    get {
      entity(as[GetAvailableTransitions]) { body =>
        getTransitionsOperation(body)
      }
    }
  }

  def getTransitionsOperation(body: GetAvailableTransitions): Route

  val addDefinition = path("workflow" / "definition") {
    post {
      entity(as[AddDefinition]) { body =>
        addDefinitionOperation(body)
      }
    }
  }

  def addDefinitionOperation(body: AddDefinition): Route

}

//object StartProcessProtocol extends DefaultJsonProtocol with SprayJsonSupport {
//  implicit val spFormat = jsonFormat4(StartProcess)
//  implicit val mtFormat = jsonFormat4(MakeTransition)
//  implicit val gaFormat = jsonFormat2(GetAvailableTransitions)
//  implicit val adFormat = jsonFormat1(AddDefinition)
//}
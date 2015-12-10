package org.cucina.engine.restful


import org.cucina.engine.{AddDefinition}
import spray.routing.{HttpService, Route}


case class StartProcessJson(processDefinitionName: String, domainObject: String, transitionId: String = null, parameters: Option[Map[String, String]] = None)

case class MakeTransitionJson(processDefinitionName: String, domainObject: String, transitionId: String, parameters: Option[Map[String, String]] = None)

case class GetAvailableTransitionsJson(domainObject: String, processDefinitionName: String = null)

/**
  * Binding business operations to the HTTP paths
  *
  * @author vlevine
  */
trait Routes
  extends HttpService {

  import ProcessProtocol._

  lazy val allRoutes = startProcess ~ makeTransition ~ availableTransitions ~ addDefinition

  val startProcess = path("workflow" / "start") {
    post {
      // authenticate()
      // authorize()
      entity(as[StartProcessJson]) { body =>
        startProcessOperation(body)
      }
    }
  }

  def startProcessOperation(body: StartProcessJson): Route

  val makeTransition = path("workflow" / "transition") {
    post {
      entity(as[MakeTransitionJson]) { body =>
        makeTransitionOperation(body)
      }
    }
  }

  def makeTransitionOperation(body: MakeTransitionJson): Route

  val availableTransitions = path("workflow" / "transitions") {
    get {
      entity(as[GetAvailableTransitionsJson]) { body =>
        getTransitionsOperation(body)
      }
    }
  }

  def getTransitionsOperation(body: GetAvailableTransitionsJson): Route

  val addDefinition = path("workflow" / "definition") {
    post {
      entity(as[AddDefinition]) { body =>
        addDefinitionOperation(body)
      }
    }
  }

  def addDefinitionOperation(body: AddDefinition): Route


}

package org.cucina.engine

import spray.http.HttpRequest
import spray.httpx.unmarshalling._
import spray.json.DefaultJsonProtocol
import spray.json._
/**
 * @author vlevine
 */
package object restful {
  object ProcessProtocol extends DefaultJsonProtocol {
    implicit val spFormat = jsonFormat4(StartProcessJson.apply)
    implicit val spUnmarshaller = new FromRequestUnmarshaller[StartProcessJson] {
      override def apply(request: HttpRequest): Deserialized[StartProcessJson] = try {
        Right(request.entity.asString.parseJson.convertTo[StartProcessJson])
      } catch {
        case x: Throwable =>
          Left(MalformedContent("Could not unmarshal StartProcessJson", x))
      }
    }
    implicit val mtFormat = jsonFormat4(MakeTransitionJson.apply)
    implicit val mtUnmarshaller = new FromRequestUnmarshaller[MakeTransitionJson] {
      override def apply(request: HttpRequest): Deserialized[MakeTransitionJson] = try {
        Right(request.entity.asString.parseJson.convertTo[MakeTransitionJson])
      } catch {
        case x: Throwable =>
          Left(MalformedContent("Could not unmarshal MakeTransitionJson", x))
      }
    }
    implicit val gatFormat = jsonFormat2(GetAvailableTransitionsJson.apply)
    implicit val gatUnmarshaller = new FromRequestUnmarshaller[GetAvailableTransitionsJson] {
      override def apply(request: HttpRequest): Deserialized[GetAvailableTransitionsJson] = try {
        Right(request.entity.asString.parseJson.convertTo[GetAvailableTransitionsJson])
      } catch {
        case x: Throwable =>
          Left(MalformedContent("Could not unmarshal GetAvailableTransitionsJson", x))
      }
    }
    implicit val adFormat = jsonFormat1(AddDefinition)
    implicit val adUnmarshaller = new FromRequestUnmarshaller[AddDefinition] {
      override def apply(request: HttpRequest): Deserialized[AddDefinition] = try {
        Right(request.entity.asString.parseJson.convertTo[AddDefinition])
      } catch {
        case x: Throwable =>
          Left(MalformedContent("Could not unmarshal AddDefinition", x))
      }
    }
  }
}

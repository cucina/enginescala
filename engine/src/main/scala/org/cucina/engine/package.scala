package org.cucina

import spray.json.DefaultJsonProtocol

/**
 * @author vlevine
 */
package object engine {
  object ProcessProtocol extends DefaultJsonProtocol {
    implicit val chFormat = jsonFormat4(StartProcess)
    implicit val opFormat = jsonFormat4(MakeTransition)
    implicit val traFormat = jsonFormat2(GetAvailableTransitions)
    implicit val epdFormat = jsonFormat1(AddDefinition)
  }
}

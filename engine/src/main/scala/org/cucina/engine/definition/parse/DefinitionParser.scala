package org.cucina.engine.definition.parse

import org.cucina.engine.definition.{DefinitionProtocol, ProcessDefinition}
import spray.json._

/**
 * Created by vlevine on 04/08/15.
 */
trait DefinitionParser {
  def parseDefinition(string: String): ProcessDefinition = {
    import DefinitionProtocol._
    val pjson = string.parseJson
    pjson.convertTo[ProcessDefinition]
  }
}

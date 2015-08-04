package org.cucina.engine

import spray.json.DefaultJsonProtocol

/**
 * Created by levinev on 04/08/2015.
 */
package object definition {
  object DefinitionProtocol extends DefaultJsonProtocol {
    implicit val chFormat = jsonFormat3(CheckDescriptor)
    implicit val opFormat = jsonFormat3(OperationDescriptor)
    implicit val traFormat = jsonFormat5(TransitionDescriptor)
    implicit val staFormat = jsonFormat5(StateDescriptor)
    implicit val defFormat = jsonFormat4(ProcessDefinition)
  }
}

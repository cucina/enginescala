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
    implicit val lisFormat = jsonFormat3(ListenerDescriptor)
    implicit val epdFormat = jsonFormat3(EnterPublisherDescriptor)
    implicit val lpdFormat = jsonFormat3(LeavePublisherDescriptor)
    implicit val staFormat = jsonFormat7(StateDescriptor)
    implicit val defFormat = jsonFormat4(ProcessDefinition)
  }
}

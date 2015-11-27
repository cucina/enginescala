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
    implicit val epdFormat = jsonFormat3(EnterPublisherDescriptor)
    implicit val lpdFormat = jsonFormat3(LeavePublisherDescriptor)
    implicit val staFormat = jsonFormat7(StateDescriptor)
/*
    implicit val decFormat = jsonFormat6(DecisionDescriptor)
    implicit val joiFormat = jsonFormat6(JoinDescriptor)
    implicit val splFormat = jsonFormat6(SplitDescriptor)
    implicit val spcFormat = jsonFormat6(SplitCollectionDescriptor)
*/
    implicit val defFormat = jsonFormat4(ProcessDefinition)
  }
}

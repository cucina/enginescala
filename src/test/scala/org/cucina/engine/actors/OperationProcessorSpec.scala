package org.cucina.engine.actors

import scala.concurrent.duration.DurationInt
import org.scalatest.WordSpecLike
import akka.actor.ActorSystem
import akka.actor.Status.Failure
import akka.testkit.TestActorRef
import akka.testkit.TestKit
import akka.testkit.ImplicitSender
import org.scalatest.Matchers

/**
 * @author levinev
 */
class OperationProcessorSpec
    extends TestKit(ActorSystem("OperationProcessorSpec"))
    with WordSpecLike
    with ImplicitSender
    with Matchers {

  "OperationProcessor actor" when {
    "receiving null OperationDescriptor" should {
      "return Failure" in {
        val actorRef = TestActorRef[OperationProcessor]
        //implicit val timeout: Timeout = 1.second
        within(1 second) {
          actorRef ! new OperationDescriptorsWrap(null, null)
          expectMsgPF() {
            case Failure(ex: Exception) => ex.getMessage == "No operationDescriptor"
          }
        }
      }
    }
  }
}
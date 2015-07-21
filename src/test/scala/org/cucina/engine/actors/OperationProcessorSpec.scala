package org.cucina.engine.actors

import scala.concurrent.duration.DurationInt
import org.scalatest.BeforeAndAfterAll
import org.scalatest.Matchers
import org.scalatest.WordSpecLike
import akka.actor._
import akka.testkit.ImplicitSender
import akka.testkit.TestKit
import akka.testkit.TestActorRef
import org.cucina.engine.definition.OperationDescriptor
import akka.actor.Status.Failure
import scala.collection.mutable.Set

/**
 * @author levinev
 */
class OperationProcessorSpec
    extends TestKit(ActorSystem("OperationProcessorSpec"))
    with WordSpecLike
    with ImplicitSender
    with Matchers
    with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "OperationProcessor actor" when {

    "receiving null OperationDescriptor" should {
      "return Failure" in {
        val actorRef = TestActorRef[OperationProcessor]
        within(500 millis) {
          actorRef ! new OperationDescriptorsWrap(null, null)
          expectMsgPF() {
            case Failure(ex: Exception) => assert("No operationDescriptor" == ex.getMessage)
          }
        }
      }
    }

    "receiving single OperationDescriptor" should {
      "return OperationFailed" in {
        val opds = Set[OperationDescriptor]()
        opds += new OperationDescriptor(Predef.classOf[FailedStub].getName, "failee")
        val actorRef = TestActorRef[OperationProcessor]
        within(500 millis) {
          actorRef ! new OperationDescriptorsWrap(opds, null)
          expectMsgPF() {
            case OperationFailed(msg: String) => assert("Whoops" == msg)
          }
        }
      }
    }

    "receiving duplicate OperationDescriptor" should {
      "return OperationComplete" in {
        val opds = Set[OperationDescriptor]()
        opds += new OperationDescriptor(Predef.classOf[GoodStub].getName)
        opds += new OperationDescriptor(Predef.classOf[GoodStub].getName)
        val actorRef = TestActorRef[OperationProcessor]
        within(500 millis) {
          actorRef ! new OperationDescriptorsWrap(opds, null)
          expectMsg(OperationComplete())
        }
      }
    }
  }
}

class FailedStub extends Actor {
  def receive = {
    case OperationRequest(parameters @ _, processContext @ _) => {
      sender ! new OperationFailed("Whoops")
    }
    case r @ _ => println("Unknown " + r)

  }
}

class GoodStub extends Actor {
  def receive = {
    case OperationRequest(parameters @ _, processContext @ _) => {
      sender ! new OperationResponse(processContext)
      println(self)
    }
    case r @ _ => println("Unknown " + r)

  }
}

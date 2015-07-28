package org.cucina.engine.actors

import scala.collection.mutable.HashMap
import scala.collection.mutable.Map
import scala.collection.mutable.Set
import scala.concurrent.duration.DurationInt

import org.cucina.engine.ProcessContext
import org.cucina.engine.definition.OperationDescriptor
import org.cucina.engine.definition.Token
import org.scalatest.BeforeAndAfterAll
import org.scalatest.Matchers
import org.scalatest.WordSpecLike

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.Status.Failure
import akka.actor.actorRef2Scala
import akka.testkit.ImplicitSender
import akka.testkit.TestKit

/**
 * @author levinev
 */
class OperationProcessorSpec
    extends TestKit(ActorSystem("cucina-test"))
    with WordSpecLike
    with ImplicitSender
    with Matchers
    with BeforeAndAfterAll {
  val actorRef = system.actorOf(Props[OperationProcessor], "opproc")
  val processContext: ProcessContext = new ProcessContext(new Token(null, null), new HashMap[String, Object](), null)

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "OperationProcessor actor" when {

    "receiving null OperationDescriptor" should {
      "return Failure" in {
        within(500 millis) {
          actorRef ! new OperationDescriptorsWrap(null, processContext)
          expectMsgPF() {
            case Failure(ex: Exception) => assert("No operationDescriptor" == ex.getMessage)
          }
        }
      }
    }

    "receiving single OperationDescriptor" should {
      "return OperationFailed" in {
        val opds = Set[OperationDescriptor]()
        opds += new OperationDescriptor(classOf[FailedStub].getName, "failee")
        within(500 millis) {
          actorRef ! new OperationDescriptorsWrap(opds, processContext)
          expectMsgPF() {
            case OperationFailed(msg: String, _) => assert("Whoops" == msg)
          }
        }
      }
    }

    "receiving duplicate OperationDescriptor" should {
      "return OperationComplete" in {
        val opds = Set[OperationDescriptor]()
        opds += new OperationDescriptor(classOf[GoodStub].getName)
        opds += new OperationDescriptor(classOf[GoodStub].getName)
        within(500 millis) {
          actorRef ! new OperationDescriptorsWrap(opds, processContext)
          expectMsg(OperationComplete(processContext))
        }
      }
    }
  }
}

class FailedStub extends Actor {
  def receive = {
    case OperationRequest(processContext @ _) => {
      sender ! new OperationFailed("Whoops", processContext)
    }
    case r @ _ => println("Unknown " + r)

  }
}

class GoodStub extends Actor {
  def receive = {
    case OperationRequest(processContext @ _) => {
      sender ! new OperationResponse(processContext)
      println(self)
    }
    case r @ _ => println("Unknown " + r)

  }
}

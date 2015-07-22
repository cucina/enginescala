package org.cucina.engine.actors

import scala.collection.mutable.Map
import scala.collection.mutable.Set
import scala.concurrent.duration.DurationInt
import org.cucina.engine.ProcessContext
import org.cucina.engine.definition.OperationDescriptor
import org.cucina.engine.definition.Token
import org.scalatest.BeforeAndAfterAll
import org.scalatest.Matchers
import org.scalatest.WordSpecLike
import akka.actor._
import akka.actor.Status.Failure
import akka.testkit.ImplicitSender
import akka.testkit.TestKit
import scala.collection.mutable.HashMap

/**
 * @author levinev
 */
class OperationProcessorSpec
    extends TestKit(ActorSystem("OperationProcessorSpec"))
    with WordSpecLike
    with ImplicitSender
    with Matchers
    with BeforeAndAfterAll {
  //  val actorRef = TestActorRef[OperationProcessor]
  val actorRef = system.actorOf(Props[OperationProcessor], "opproc")
  val processContext: ProcessContext = new ProcessContext(new Token(null, null), new HashMap[String, Object]())
  /*override def afterAll {
    TestKit.shutdownActorSystem(system)
  }
*/
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
        opds += new OperationDescriptor(Predef.classOf[FailedStub].getName, "failee")
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
        opds += new OperationDescriptor(Predef.classOf[GoodStub].getName)
        opds += new OperationDescriptor(Predef.classOf[GoodStub].getName)
        within(500 millis) {
          actorRef ! new OperationDescriptorsWrap(opds, processContext)
          expectMsg(OperationComplete())
        }
      }
    }
  }
}

class FailedStub(val parameters:Map[String, Object]) extends Actor {
  def receive = {
    case OperationRequest(parameters @ _, processContext @ _) => {
      sender ! new OperationFailed("Whoops", processContext)
    }
    case r @ _ => println("Unknown " + r)

  }
}

class GoodStub(val parameters:Map[String, Object]) extends Actor {
  def receive = {
    case OperationRequest(parameters @ _, processContext @ _) => {
      sender ! new OperationResponse(processContext)
      println(self)
    }
    case r @ _ => println("Unknown " + r)

  }
}

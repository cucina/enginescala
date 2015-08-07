package org.cucina.engine.actors

import akka.actor.{Actor, Props, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import org.cucina.engine.{ProcessDefinitionWrap, ExecuteComplete, ProcessContext}
import org.cucina.engine.definition.{Token, ProcessDefinition}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.collection.mutable
import scala.concurrent.duration.DurationInt
import org.mockito.Mockito._


/**
 * Created by levinev on 05/08/2015.
 */
class ProcessInstanceFactorySpec extends TestKit(ActorSystem("cucina-test"))
with ImplicitSender
with WordSpecLike
with Matchers
with BeforeAndAfterAll
with BeforeAndAfter
with MockitoSugar {
  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  val defin = mock[ProcessDefinition]
  when(defin.id).thenReturn("mock")

  val processContext: ProcessContext = new ProcessContext(new Token(new Object, defin), new mutable.HashMap[String, Object](), self)

  val procReg = system.actorOf(Props(new Actor
    with MockitoSugar {

    def receive = {
      case e: FindDefinition =>
        println("Fake Registry " + e + " sender " + sender)
        sender ! new ProcessDefinitionWrap(Some(defin), e.nested)
    }
  }))

  println("Me " + self)

  val actorRef = system.actorOf(Props(classOf[ProcessInstanceFactory], procReg, new TestProcessInstanceProvider))

  "ProcessInstanceFactory" when {
    "received StartInstance" should {
      "receive ProcessDefinitionWrap" in {
        within(500 millis) {
          actorRef ! new StartInstance(processContext)
          expectMsgPF() {
            case ExecuteComplete(pc) =>
              assert(processContext == pc)
            case a@_ =>
              fail("Unexpected response" + a)
          }
        }
      }
    }
  }
}

class TestProcessInstanceProvider extends ProcessInstanceProvider {
  override def props(processDefinition: ProcessDefinition): Props = {
    Props(new Actor {
      def receive = {
        case e: StartInstance =>
          println("Got start " + e.processContext + " sender " + sender)
          sender ! new ExecuteComplete(e.processContext)
        case e@_ => println("TPE:" + e)
      }
    })
  }
}


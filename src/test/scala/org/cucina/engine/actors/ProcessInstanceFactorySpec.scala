package org.cucina.engine.actors

import akka.actor.{Actor, Props, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import org.cucina.engine.ProcessContext
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

  val processContext: ProcessContext = new ProcessContext(new Token(null, null), new mutable.HashMap[String, Object](), self)

  val procReg = system.actorOf(Props(new Actor
    with MockitoSugar {
    val defin = mock[ProcessDefinition]
    when(defin.id).thenReturn("mock")

    def receive = {
      case e: FindDefinition => sender ! new ProcessDefinitionWrap(defin, e.nested)
    }
  }))

  val actorRef = system.actorOf(Props(classOf[ProcessInstanceFactory], procReg, new TestProcessInstanceProvider))

  "ProcessInstanceFactory" when {
    "received StartInstance" should {
      "receive ProcessDefinitionWrap" in {
        within(500 millis) {
          actorRef ! new StartInstance("mock", processContext)
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
        case e: ExecuteStart => e.processContext.client ! new ExecuteComplete(e.processContext)
        case e@_ => println("TPE:" + e)
      }
    })
  }
}


package org.cucina.engine.actors

import akka.actor.{Props, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import org.cucina.engine.{NestedTuple, ProcessDefinitionWrap}
import org.cucina.engine.definition.ProcessDefinition
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration.DurationInt
import org.mockito.Mockito._

/**
 * Created by levinev on 05/08/2015.
 */
class DefinitionRegistrySpec extends TestKit(ActorSystem("cucina-test"))
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

  "DefinitionRegistry" when {
    "received AddProcessDefinition" should {
      "return new" in {
        within(500 millis) {
          val actorRef = system.actorOf(Props[DefinitionRegistry])
          actorRef ! new AddProcessDefinition(defin)
          val obj: Object = new Object
          actorRef ! new FindDefinition("mock", NestedTuple(obj, self))
          expectMsgPF() {
            case ProcessDefinitionWrap(d, o) =>
              assert(obj == o.originalRequest)
              assert(d == defin)
            case a@_ => println("Whoopsie:" + a)
          }
        }
      }
    }
  }
}

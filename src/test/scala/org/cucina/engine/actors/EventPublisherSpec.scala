package org.cucina.engine.actors

import akka.actor.{ActorRef, Actor, Props, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import org.cucina.engine.{ExecuteComplete, ProcessContext}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration.DurationInt
import org.mockito.Mockito._

/**
 * Created by levinev on 12/08/2015.
 */
class EventPublisherSpec extends TestKit(ActorSystem("cucina-test"))
with ImplicitSender
with WordSpecLike
with Matchers
with BeforeAndAfterAll
with MockitoSugar {
  override def afterAll = {
    TestKit.shutdownActorSystem(system)
  }

  "EventPublisher" when {
    "created" should {
      "work" in {
        val pi = system.actorOf(Props(classOf[EventPublisher], List(), (pc: ProcessContext) => new EnterEvent(pc)))
        println("Pi " + pi)
      }
    }
    "called no listeners" should {
      "complete" in {
        val pi = system.actorOf(Props(classOf[EventPublisher], List(), (pc: ProcessContext) => new EnterEvent(pc)))
        val pc = mock[ProcessContext]
        when(pc.client).thenReturn(self)
        pi ! StackRequest(pc, List())
        expectMsg(ExecuteComplete(pc))
      }
    }
    "called 1 listener" should {
      "complete" in {
        system.actorOf(Props(classOf[MeListener], self) , "mel")
        val pi = system.actorOf(Props(classOf[EventPublisher], "mel"::Nil, (pc: ProcessContext) => new LeaveEvent(pc)))
        val pc = mock[ProcessContext]
        when(pc.client).thenReturn(self)
        pi ! StackRequest(pc, List())
        expectMsg(ExecuteComplete(pc))
        expectMsg("OK")
      }
    }
  }
}

class MeListener(sink:ActorRef) extends Actor {
  def receive = {
    case LeaveEvent(_) =>
      Thread sleep 100
      sink ! "OK"
  }
}

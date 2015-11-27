package org.cucina.engine.actors

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.cucina.engine.definition.{ProcessDefinition, Token, TransitionDescriptor}
import org.cucina.engine.{ExecuteComplete, ExecuteFailed, ProcessContext}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.collection.mutable
import scala.concurrent.duration.DurationInt

/**
  * @author vlevine
  */
class SplitSpec extends TestKit(ActorSystem("cucina-test"))
with ImplicitSender
with WordSpecLike
with Matchers
with BeforeAndAfterAll
with BeforeAndAfter
with MockitoSugar {
  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  println(system.actorOf(Props[OutState], "state1"))
  println(system.actorOf(Props[OutState], "state2"))

  "received StackRequest" should {
    val processContext: ProcessContext = new ProcessContext(new Token(new Object, mock[ProcessDefinition]),
      new mutable.HashMap[String, Object](), testActor)

    "success for simple " in {
      val actorRef = system.actorOf(Split.props("sc",
        TransitionDescriptor("str1", "/user/state1", className = Some(classOf[SucceedingTrans].getName)) ::
          TransitionDescriptor("str2", "/user/state2", className = Some(classOf[SucceedingTrans].getName)) :: Nil,
        List(), List(), List()))
      actorRef !new StackRequest(processContext, List())
      expectMsgPF(1 second) {
        case ExecuteComplete(pc) =>
          assert(pc.token.hasChildren)
          assert(pc.token.children.size == 2)
          val ch = pc.token.children.head
          assert(ch.parent.get == pc.token)
      }
    }
    "fail for simple " in {
      val actorRef = system.actorOf(Split.props("sc",
        TransitionDescriptor("str1", "/user/state1", className = Some(classOf[SucceedingTrans].getName)) ::
          TransitionDescriptor("str2", "/user/state2", className = Some(classOf[FailingTrans].getName)) :: Nil,
        List(), List(), List()))
      actorRef !new StackRequest(processContext, List())
      expectMsgPF(500 millis) {
        case ExecuteFailed(c, f) =>
          println("client " + c)
          println("error " + f)
      }
    }
  }
}
package org.cucina.engine.actors

import akka.actor.{Props, Actor, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import org.cucina.engine.{ExecuteComplete, ExecuteFailed, ProcessContext}
import org.cucina.engine.definition._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration.DurationInt
import scala.collection.mutable

/**
 * Created by vlevine on 8/25/15.
 */
class DecisionSpec extends TestKit(ActorSystem("cucina-test"))
with ImplicitSender
with WordSpecLike
with Matchers
with BeforeAndAfterAll
with BeforeAndAfter
with MockitoSugar {
  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  println("outstate=" + system.actorOf(Props[OutState], "state"))

  "received StackRequest" should {
    "fail for empty " in {
      val actorRef = system.actorOf(Decision.props("dec", List(),
        List(), List(), List()))
      val processContext: ProcessContext = new ProcessContext(new Token(new Object, mock[ProcessDefinition]), new mutable.HashMap[String, Object](), testActor)
      actorRef ! new StackRequest(processContext, List())
      expectMsgPF(500 millis) {
        case msg => println("DecisionSpec fe msg " + msg)
      }
    }
    "success for first " in {
      val actorRef = system.actorOf(Decision.props("dec", TransitionDescriptor("str", "/user/state", className = Some(classOf[SucceedingTrans].getName)) :: Nil,
        List(), List(), List()))
      val processContext: ProcessContext = new ProcessContext(new Token(new Object, mock[ProcessDefinition]), new mutable.HashMap[String, Object](), testActor)
      actorRef ! new StackRequest(processContext, List())
      expectMsgPF(500 millis) {
        case msg:ExecuteComplete => println("DecisionSpec sf msg " + msg)
      }
    }
    "fail for first " in {
      val actorRef = system.actorOf(Decision.props("dec", TransitionDescriptor("str", "/user/state", className = Some(classOf[FailingTrans].getName)) :: Nil,
        List(), List(), List()))
      val processContext: ProcessContext = new ProcessContext(new Token(new Object, mock[ProcessDefinition]), new mutable.HashMap[String, Object](), testActor)
      actorRef ! new StackRequest(processContext, List())
      expectMsgPF(500 millis) {
        case msg:ExecuteFailed => println("DecisionSpec ff msg " + msg)
      }
    }
    "success for second " in {
      val actorRef = system.actorOf(Decision.props("dec",
        TransitionDescriptor("ftr", "/user/state", className = Some(classOf[FailingTrans].getName))
          :: TransitionDescriptor("str", "/user/state", className = Some(classOf[SucceedingTrans].getName)) :: Nil,
        List(), List(), List()))
      val processContext: ProcessContext = new ProcessContext(new Token(new Object, mock[ProcessDefinition]), new mutable.HashMap[String, Object](), testActor)
      actorRef ! new StackRequest(processContext, List())
      expectMsgPF(500 millis) {
        case msg:ExecuteComplete => println("DecisionSpec ss msg " + msg)
      }
    }
  }
}


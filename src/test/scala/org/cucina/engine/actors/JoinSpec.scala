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
 * Created by vlevine on 8/27/15.
 */
class JoinSpec extends TestKit(ActorSystem("cucina-test"))
with ImplicitSender
with WordSpecLike
with Matchers
with BeforeAndAfterAll
with BeforeAndAfter
with MockitoSugar {
  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  println(system.actorOf(Props[OutState], "state"))

  "received StackRequest" should {
    "succeeded for single " in {
      val actorRef = system.actorOf(Join.props("sc", TransitionDescriptor("str", "/user/state", className = Some(classOf[SucceedingTrans].getName)) :: Nil,
        List(), List(), List()))
      val parent = new Token(ObjectWithSimpleCollection("AA" :: Nil), mock[ProcessDefinition])
      val cha = new Token("AA", mock[ProcessDefinition])
      parent.children += cha
      cha.parent = Some(parent)
      val processContext: ProcessContext = new ProcessContext(cha, new mutable.HashMap[String, Object](), testActor)

      actorRef ! new StackRequest(processContext, List())
      expectMsgPF(500 millis) {
        case ExecuteComplete(pc) =>
          assert("AA" == pc.token.domainObject)
      }
      expectMsgPF(500 millis) {
        case ExecuteComplete(pc) =>
          assert(pc.token.domainObject.isInstanceOf[ObjectWithSimpleCollection])
          assert(!pc.token.hasChildren)
      }
    }
    "succeeded for double " in {
      val actorRef = system.actorOf(Join.props("sc", TransitionDescriptor("str", "/user/state", className = Some(classOf[SucceedingTrans].getName)) :: Nil,
        List(), List(), List()))
      val parent = new Token(ObjectWithSimpleCollection("A" :: "B" :: Nil), mock[ProcessDefinition])
      val chA = new Token("A", mock[ProcessDefinition])
      parent.children += chA
      chA.parent = Some(parent)
      val pcA: ProcessContext = new ProcessContext(chA, new mutable.HashMap[String, Object](), testActor)
      val chB = new Token("B", mock[ProcessDefinition])
      parent.children += chB
      chB.parent = Some(parent)
      val pcB: ProcessContext = new ProcessContext(chB, new mutable.HashMap[String, Object](), testActor)

      actorRef ! new StackRequest(pcA, List())
      expectMsgPF(500 millis) {
        case ExecuteComplete(pc) =>
          assert("A" == pc.token.domainObject)
      }
      actorRef ! new StackRequest(pcB, List())
      expectMsgPF(500 millis) {
        case ExecuteComplete(pc) =>
          assert("B" == pc.token.domainObject)
      }
      expectMsgPF(500 millis) {
        case ExecuteComplete(pc) =>
          assert(pc.token.domainObject.isInstanceOf[ObjectWithSimpleCollection])
          assert(!pc.token.hasChildren)
      }
    }
    "fail for parentless " in {
      val actorRef = system.actorOf(Join.props("sc", TransitionDescriptor("str", "/user/state", className = Some(classOf[SucceedingTrans].getName)) :: Nil,
        List(), List(), List()))
      val processContext: ProcessContext = new ProcessContext(new Token("C", mock[ProcessDefinition]),
        new mutable.HashMap[String, Object](), testActor)

      actorRef ! new StackRequest(processContext, List())
      expectMsgPF(500 millis) {
        case ExecuteFailed(c, f) =>
          println("client " + c)
          println("error " + f)
      }
    }
  }
}
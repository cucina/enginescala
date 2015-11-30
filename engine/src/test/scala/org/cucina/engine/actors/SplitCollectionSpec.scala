package org.cucina.engine.actors

import akka.actor.{Props, ActorSystem}
import akka.testkit.{TestProbe, ImplicitSender, TestKit}
import akka.util.Timeout
import org.cucina.engine.{ExecuteFailed, ExecuteComplete, ProcessContext}
import org.cucina.engine.definition.{TransitionDescriptor, ProcessDefinition, Token}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.collection.mutable

/**
  * Created by vlevine on 8/27/15.
  */
class SplitCollectionSpec extends TestKit(ActorSystem("cucina-test"))
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
  //println(system.actorOf(Props[OutState], "join"))
  val join = TestProbe("join")
  println(join.ref)

  val statement = "token().domainObject().coll()"

  "received StackRequest" should {
    "success for simple " in {
      val actorRef = system.actorOf(SplitCollection.props("sc", "system/" + join.ref.path.name,
        TransitionDescriptor("str", "/user/state", className = Some(classOf[SucceedingTrans].getName)),
        List(), List(), List(), statement))
      val processContext: ProcessContext = new ProcessContext(new Token(ObjectWithSimpleCollection("a" :: "b" :: "c" :: Nil), mock[ProcessDefinition]),
        new mutable.HashMap[String, Object](), testActor)

      actorRef ! new StackRequest(processContext, List())
      expectMsgPF(1 second) {
        case ec@ExecuteComplete(`processContext`) =>
          print("aha:" + ec.processContext)
        //          assert(processContext.token.hasChildren)
        //          assert(processContext.token.children.size == 3)
        //          val ch = processContext.token.children.head
        //          assert(ch.parent.get == processContext.token)
      }
      join.expectMsgPF(1 second) {
        case SplitToken(pc) =>
          println(pc)
          assert(pc.token.domainObject=="a")
      }
      join.expectMsgPF(1 second) {
        case SplitToken(pc) =>
          println(pc)
          assert(pc.token.domainObject=="b")
      }
      join.expectMsgPF(1 second) {
        case SplitToken(pc) =>
          println(pc)
          assert(pc.token.domainObject=="c")
      }
      assert(processContext.token.hasChildren)
      println(processContext.token.children)
    }

    "fail for simple " in {
      val actorRef = system.actorOf(SplitCollection.props("sc", "system/" + join.ref.path.name,
        TransitionDescriptor("str", "/user/state", className = Some(classOf[FailingTrans].getName)),
        List(), List(), List(), statement))
      val processContext: ProcessContext = new ProcessContext(new Token(ObjectWithSimpleCollection("a" :: "b" :: "c" :: Nil), mock[ProcessDefinition]),
        new mutable.HashMap[String, Object](), testActor)

      actorRef ! new StackRequest(processContext, List())
      expectMsgPF(500 millis) {
        case ec@ExecuteComplete(`processContext`) =>
          print("aha:" + ec.processContext)
      }

      join.expectMsgPF(1 second) {
        case SplitToken(pc) =>
          println(pc)
          assert(pc.token.domainObject=="a")
      }
      join.expectMsgPF(1 second) {
        case SplitToken(pc) =>
          println(pc)
          assert(pc.token.domainObject=="b")
      }
      join.expectMsgPF(1 second) {
        case SplitToken(pc) =>
          println(pc)
          assert(pc.token.domainObject=="c")
      }
      /*case ExecuteFailed(c, f) =>
          println("client " + c)
          println("error " + f)
      }*/

    }
  }
}
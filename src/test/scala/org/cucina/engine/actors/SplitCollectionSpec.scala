package org.cucina.engine.actors

import akka.actor.{Props, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
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

  val statement = "token().domainObject().coll()"

  "received StackRequest" should {
    "success for simple " in {
      val actorRef = system.actorOf(SplitCollection.props("sc", TransitionDescriptor("str", "/user/state", className = Some(classOf[SucceedingTrans].getName)),
        statement, List(), List(), List()))
      val processContext: ProcessContext = new ProcessContext(new Token(SimpleColl("a" :: "b" :: "c" :: Nil), mock[ProcessDefinition]),
        new mutable.HashMap[String, Object](), testActor)

      actorRef ! new StackRequest(processContext, List())
      expectMsgPF(1 second) {
        case ExecuteComplete(pc) =>
          assert(pc.token.hasChildren)
          assert(pc.token.children.size == 3)
          val ch = pc.token.children.head
          assert(ch.parent.get == pc.token)
      }
    }
    "fail for simple " in {
      val actorRef = system.actorOf(SplitCollection.props("sc", TransitionDescriptor("str", "/user/state", className = Some(classOf[FailingTrans].getName)),
        statement, List(), List(), List()))
      val processContext: ProcessContext = new ProcessContext(new Token(SimpleColl("a" :: "b" :: "c" :: Nil), mock[ProcessDefinition]),
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

case class SimpleColl(coll: Seq[String])

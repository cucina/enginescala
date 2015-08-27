package org.cucina.engine.actors

import akka.actor.{Props, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import org.cucina.engine.ProcessContext
import org.cucina.engine.definition.{TransitionDescriptor, ProcessDefinition, Token}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
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

  println("outstate=" + system.actorOf(Props[OutState], "state"))

  "received StackRequest" should {
    "execute for simple " in {
      case class SimpleColl(coll: Seq[String])
      val actorRef = system.actorOf(SplitCollection.props("sc", TransitionDescriptor("str", "/user/state", className = Some(classOf[SucceedingTrans].getName)),
        "pc.token.domainObject.coll",
        List(), List(), List()))
      val processContext: ProcessContext = new ProcessContext(new Token(SimpleColl("a"::"b"::"c"::Nil), mock[ProcessDefinition]),
        new mutable.HashMap[String, Object](), testActor)
      actorRef ! new StackRequest(processContext, List())
      expectMsgPF(500 millis) {
        case msg => println("msg " + msg)
      }
    }
  }
}



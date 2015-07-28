package org.cucina.engine.actors

import akka.actor.{Props, Actor, ActorSystem, actorRef2Scala}
import akka.testkit.{ImplicitSender, TestKit}
import org.cucina.engine.ProcessContext
import org.cucina.engine.definition.Token
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.collection.mutable
import scala.concurrent.duration.DurationInt

/**
 * Created by levinev on 28/07/2015.
 */
class StateActorSpec extends TestKit(ActorSystem("cucina-test"))
with ImplicitSender
with WordSpecLike
with Matchers
with BeforeAndAfterAll
with BeforeAndAfter {
  val processContext: ProcessContext = new ProcessContext(new Token(null, null), new mutable.HashMap[String, Object](), null)

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "StateActor" when {

    "received EnterState" should {
      "return " in {
        within(500 millis) {
          val actorRef = system.actorOf(Props(classOf[StateActor], null))
          actorRef ! new EnterState("one", processContext)
        }
      }
    }
  }
}

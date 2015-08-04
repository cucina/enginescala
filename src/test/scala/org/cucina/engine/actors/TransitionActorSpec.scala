package org.cucina.engine.actors

import akka.actor.{Actor, Props, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import org.cucina.engine.ProcessContext
import org.cucina.engine.definition.{CheckDescriptor, TransitionDescriptor, OperationDescriptor, Token}
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.collection.mutable
import scala.concurrent.duration.DurationInt

/**
 * Created by levinev on 04/08/2015.
 */
class TransitionActorSpec extends TestKit(ActorSystem("cucina-test"))
with ImplicitSender
with WordSpecLike
with Matchers
with BeforeAndAfterAll
with BeforeAndAfter {
  val processContext: ProcessContext = new ProcessContext(new Token(null, null), new mutable.HashMap[String, Object](), self)
  val leaveOperations: Seq[OperationDescriptor] = List()
  val checks: Seq[CheckDescriptor] = List()

  system.actorOf(Props[OutState], "state")

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "TransitionActor" when {
    "received StackRequest" should {
      "return " in {
        within(500 millis) {
          val actorRef = system.actorOf(TransitionActor.props("transition", "state", leaveOperations, checks))
          actorRef ! new StackRequest(processContext, List())
          processContext.parameters += ("OutState" -> "")
          actorRef ! new StackRequest(processContext, List())
          var oldin = ""
          expectMsgPF() {
            case ExecuteComplete(pc) =>
              println(pc.parameters)
              pc.parameters.get("OutState") match {
                case Some(in:String) =>
                  println("Hashcode=" + in)
                  if (oldin == "") {
                    oldin = in
                  } else {
                    assert(in == oldin)
                  }
              }

            case a@_ => println("Whopsie:" + a)
          }
        }
      }
    }
  }
}

class OutState extends Actor {
  def receive = {
    case StackRequest(pc, stack) =>
      println("Called with " + pc)
      pc.parameters += ("OutState" -> this.hashCode().toString)
      pc.client ! new ExecuteComplete(pc)
    case e@_ =>
      println(e)
      sender ! "OutState"
  }
}

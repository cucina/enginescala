package org.cucina.engine.actors

import akka.actor.{Actor, Props, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import org.cucina.engine.{ExecuteFailed, ExecuteComplete, ProcessContext}
import org.cucina.engine.definition._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.collection.mutable
import scala.concurrent.duration.DurationInt

/**
 * Created by levinev on 04/08/2015.
 */
class TransitionSpec extends TestKit(ActorSystem("cucina-test"))
with ImplicitSender
with WordSpecLike
with Matchers
with BeforeAndAfterAll
with BeforeAndAfter
with MockitoSugar {

  system.actorOf(Props[OutState], "state")

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }


  "received StackRequest" should {
    "return " in {
      within(1500 millis) {
        val actorRef = system.actorOf(Transition.props("transition", "state", List(), List()), "tr")
        val processContext: ProcessContext = new ProcessContext(new Token(new Object, mock[ProcessDefinition]), new mutable.HashMap[String, Object](), testActor)
        actorRef ! new StackRequest(processContext, List())
        expectMsgPF() {
          case ExecuteComplete(pc) =>
            println("Not yet " + pc.parameters)
        }
        processContext.parameters += ("OutState" -> "")
        actorRef ! new StackRequest(processContext, List())
        expectMsgPF() {
          case ExecuteComplete(pc) =>
            pc.parameters.get("OutState") match {
              case Some(in: String) =>
                println("Hashcode=" + in)
                assert(in != "")

              case e@_ =>
                fail("Unexpected:" + e)
            }

          case a@_ => println("Whopsie:" + a)
        }
      }
    }
  }

  "received DryCheck" should {
    "execute checks only" in {
      within(3 seconds) {
        val leaveOperations: Seq[OperationDescriptor] = OperationDescriptor("opf", Some(classOf[CheckFalse].getName)) :: Nil
        val checks: Seq[CheckDescriptor] = CheckDescriptor("cht", Some(classOf[CheckTrue].getName)) :: Nil
        val actorRef = system.actorOf(Transition.props("transition1", "state", leaveOperations, checks), "tr1")
        println("dr1 " + actorRef)
        val processContext: ProcessContext = new ProcessContext(new Token(new Object, mock[ProcessDefinition]), new mutable.HashMap[String, Object](), testActor)
        actorRef ! new DryCheck(processContext)
        expectMsgPF() {
          case ec@ExecuteComplete(processContext) =>
            println("Completed dr1 " + processContext)
          case ehh =>
            fail("Expected ExecuteComplete instead of " + ehh)
        }

        println("Phase 1")
      }
      val leaveOperations: Seq[OperationDescriptor] = OperationDescriptor("opf2", Some(classOf[CheckFalse].getName)) :: Nil
      val checks: Seq[CheckDescriptor] = CheckDescriptor("cht2", Some(classOf[CheckTrue].getName)) ::
        CheckDescriptor("chf2", Some(classOf[CheckFalse].getName)) :: Nil
      val actorRef = system.actorOf(Transition.props("transition2", "state", leaveOperations, checks), "tr2")
      println("dr2 " + actorRef)
      val processContext: ProcessContext = new ProcessContext(new Token(new Object, mock[ProcessDefinition]), new mutable.HashMap[String, Object](), testActor)
      actorRef ! new DryCheck(processContext)
      expectMsgPF() {
        case f@ExecuteFailed(c, s) =>
          println("expected fail=" + f)
        case ehh =>
          fail("Expected ExecuteFailed instead of " + ehh)
      }
    }
  }
  /*
    "received DryCheck2" should {
      "execute failing checks only" in {
        val leaveOperations: Seq[OperationDescriptor] = OperationDescriptor("opf2", Some(classOf[CheckFalse].getName)) :: Nil
        val checks: Seq[CheckDescriptor] = CheckDescriptor("cht2", Some(classOf[CheckTrue].getName)) ::
          CheckDescriptor("chf2", Some(classOf[CheckFalse].getName)) :: Nil
        val actorRef = system.actorOf(Transition.props("transition2", "state", leaveOperations, checks), "tr2")
        println("dr2 " + actorRef)
        val processContext: ProcessContext = new ProcessContext(new Token(new Object, mock[ProcessDefinition]), new mutable.HashMap[String, Object](), testActor)
        actorRef ! new DryCheck(processContext)
        expectMsgPF() {
          case ExecuteFailed(c, s) =>
            println("c=" + c)
          case ehh =>
            println(ehh)
            fail("Expected ExecuteFailed")
        }
      }

    }
  */
}




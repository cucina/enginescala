package org.cucina.engine.actors

import akka.actor.{Props, ActorSystem}
import akka.testkit.{TestKit, ImplicitSender}
import org.cucina.engine.{ProcessFailure, ExecuteFailed}
import org.cucina.engine.definition.{Token, ProcessDefinition}
import org.cucina.engine.repository.{StoreToken, MapTokenRepository}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{WordSpecLike, Matchers, BeforeAndAfter, BeforeAndAfterAll}
import scala.collection.immutable.HashMap
import org.mockito.Mockito._

/**
 * Created by levinev on 07/08/2015.
 */
class TokenFactorySpec  extends TestKit(ActorSystem("cucina-test"))
with ImplicitSender
with WordSpecLike
with Matchers
with BeforeAndAfterAll
with BeforeAndAfter
with MockitoSugar {
  override def afterAll = {
    TestKit.shutdownActorSystem(system)
  }

  val tokenRepo = system.actorOf(Props[MapTokenRepository])
  val defi = mock[ProcessDefinition]
  when(defi.listTransitions("start")).thenReturn(Some(List("t1")))
  val obj = new Object
  val token = Token(obj, defi)
  token.stateId = "start"
  tokenRepo ! StoreToken(token)

  "TokenFactory actor" when {
    "created" should {
      "work" in {
        val pi = system.actorOf(TokenFactory.props(tokenRepo))
        println("Pi " + pi)
      }
    }
    "received StartToken" should {
      "respond with StartInstance" in {
        val pi = system.actorOf(TokenFactory.props(tokenRepo))
        pi ! StartToken(defi, new Object, null, new HashMap[String, Object], self)
        expectMsgPF() {
          case st:StartInstance =>
            println(st)
            assert(st.processContext.token.processDefinition == defi)
        }
      }
      "respond with ExecuteFailed" in {
        val pi = system.actorOf(TokenFactory.props(tokenRepo))
        pi ! StartToken(defi, obj, null, new HashMap[String, Object], self)
        expectMsgPF() {
          case st:ExecuteFailed =>
            println(st)
        }
      }
    }

    "received MoveToken" should {
      "respond with MoveInstance" in {
        val pi = system.actorOf(TokenFactory.props(tokenRepo))
        pi ! MoveToken(defi, obj, "tr1", new HashMap[String, Object], self)
        expectMsgPF() {
          case st:MoveInstance =>
            println(st)
            assert(st.processContext.token.processDefinition == defi)
        }
      }
      "respond with ExecuteFailed" in {
        val pi = system.actorOf(TokenFactory.props(tokenRepo))
        pi ! MoveToken(defi, new Object, "tr1", new HashMap[String, Object], self)
        expectMsgPF() {
          case st:ExecuteFailed =>
            println(st)
        }
      }
    }

    "received GetTransitions" should {
      "respond with list" in {
        val pi = system.actorOf(TokenFactory.props(tokenRepo))
        pi ! GetTransitions(defi, obj, self)
        expectMsgPF() {
          case st:Seq[String] =>
            println(st)
            assert(st.head == "t1")
        }
      }
      "respond with ExecuteFailed" in {
        val pi = system.actorOf(TokenFactory.props(tokenRepo))
        pi ! GetTransitions(defi, new Object, self)
        expectMsgPF() {
          case st:ExecuteFailed =>
            println(st)
        }
      }
    }
  }
}

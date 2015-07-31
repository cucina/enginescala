package org.cucina.engine.actors.support

import akka.actor.Actor.Receive
import akka.actor._
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import org.cucina.engine.definition.ProcessElementDescriptor
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import org.mockito.Mockito._
import org.mockito.Matchers._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/**
 * Created by levinev on 27/07/2015.
 */
class ActorFinderSpec extends TestKit(ActorSystem("cucina-test"))
with WordSpecLike
with Matchers
with BeforeAndAfterAll
with BeforeAndAfter
with MockitoSugar
with ImplicitSender {

  class Wrap extends ActorFinder

  "ActorFinder" when {
    "finding EmptyActor" should {
      "return null" in {
        val wrap = new Wrap
        implicit val context = mock[ActorContext]
        when(context.actorSelection("empty")).thenThrow(new ActorNotFound(null))
        when(context.actorSelection("../empty")).thenThrow(new ActorNotFound(null))
        when(context.actorSelection("../../empty")).thenThrow(new ActorNotFound(null))
        val ar = mock[ActorRef]
        when(context.actorOf(anyObject[Props], anyString())).thenReturn(ar)
        val cre = wrap.findActor(new EmptyDescriptor)
        assert(cre == null)
      }

      "return actor as a sibling" in {
        val act = system.actorOf(Props(new Actor {
          val locala: ActorRef = context.actorOf(Props(classOf[EmptyActor], "haha", 1), "empty")
          val finder = context.actorOf(Props[SimpleFinder])

          def receive = {
            case e@_ =>
              println("Got " + e + " from sender " + sender)
              finder forward e

          }
        }))

        act ! "empty"
        expectMsgPF() {
          case a: ActorRef =>
            println("sibling:" + a)
            assert(a != null)
        }
      }
      "return actor as an uncle" in {
        val locala: ActorRef = system.actorOf(Props(classOf[EmptyActor], "haha", 1), "empty")
        val act = system.actorOf(Props(new Actor {
          val finder = context.actorOf(Props[SimpleFinder])

          def receive = {
            case e@_ =>
              println("Got '" + e + "' from sender " + sender)
              finder forward e

          }
        }))

        act ! "empty"
        expectMsgPF() {
          case a: ActorRef =>
            println("uncle:" + a)
            assert(a != null)
        }
      }
    }
  }

  "Reflector" when {
    import scala.reflect.runtime.universe._
    import scala.reflect.runtime._
    "creating EmptyActor" should {
      "create one" in {
        val a = Props(classOf[EmptyActor], Array[Any]("name", 1): _*)
        println("Actor:" + a)
      }
    }
  }

  class EmptyDescriptor extends ProcessElementDescriptor {
    override val className: String = classOf[EmptyActor].getName
    val arguments: Seq[Any] = Array[Any]("ana", 2)
    override val name: String = "empty"
  }

}

class SimpleFinder extends Actor with ActorFinder {
  def receive = {
    case s: String =>
      sender ! findActor(s)
  }
}

class EmptyActor(name: String, index: Int) extends Actor {
  def receive = Actor.emptyBehavior
}


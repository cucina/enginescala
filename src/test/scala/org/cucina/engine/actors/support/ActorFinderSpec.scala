package org.cucina.engine.actors.support

import akka.actor._
import org.cucina.engine.definition.ProcessElementDescriptor
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import org.mockito.Mockito._
import org.mockito.Matchers._

/**
 * Created by levinev on 27/07/2015.
 */
class ActorFinderSpec extends WordSpecLike
with Matchers
with BeforeAndAfterAll
with BeforeAndAfter
with MockitoSugar {

  class Wrap extends ActorFinder

  "ActorFinder" when {
    "creating EmptyActor" should {
      "return reference" in {
        val wrap = new Wrap
        implicit val context = mock[ActorContext]
        when(context.actorSelection("empty")).thenThrow(new ActorNotFound(null))
        val ar = mock[ActorRef]
        when(context.actorOf(anyObject[Props], anyString())).thenReturn(ar)
        val cre = wrap.findActor(new EmptyDescriptor)
        assert(cre != null)
      }
      "fail for invalid arguments" in {

      }
    }
  }

  "Reflector" when {
    import scala.reflect.runtime.universe._
    import scala.reflect.runtime._
    "creating EmptyActor" should {
      "create one" in {
        val a = Props(classOf[EmptyActor], Array[Any]("name", 1):_*)
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

class EmptyActor(name:String, index:Int) extends Actor {
  def receive = Actor.emptyBehavior
}


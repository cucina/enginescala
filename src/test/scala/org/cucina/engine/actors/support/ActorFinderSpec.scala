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
        val context = mock[ActorContext]
        when(context.actorSelection("empty")).thenThrow(new ActorNotFound(null))
        val ar = mock[ActorRef]
        when(context.actorOf(anyObject[Props], anyString())).thenReturn(ar)
        val cre = wrap.findActor(new EmptyDescriptor, context)
        assert(cre != null)
      }
    }
  }

  "Reflector" when {
    import scala.reflect.runtime.universe._
    import scala.reflect.runtime._
    "creating EmptyActor" should {
      "create one" in {
/*
        val ru = universe
        val m = ru.runtimeMirror(classOf[EmptyActor].getClassLoader)
        val clazz = ru.typeOf[EmptyActor].typeSymbol.asClass
        val cm = m.reflectClass(clazz)
        println("cm=" + cm)
        val ctorC = ru.typeOf[EmptyActor].decl(ru.termNames.CONSTRUCTOR).asMethod
        println("ctorC=" + ctorC)
        val ctorm = cm.reflectConstructor(ctorC)
        println("ctorm=" + ctorm)
*/
        val a = Props(classOf[EmptyActor], Array[Any]("name", 1):_*)
        println("Actor:" + a)
      }
    }
  }

  class EmptyDescriptor extends ProcessElementDescriptor {
    override val className: String = classOf[EmptyActor].getName
    override val arguments: Seq[Any] = Array[Any]("ana", 2)
    override val name: String = "empty"
  }

}

class EmptyActor(name:String, index:Int) extends Actor {
  def receive = Actor.emptyBehavior
}


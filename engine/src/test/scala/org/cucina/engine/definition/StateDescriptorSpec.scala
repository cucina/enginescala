package org.cucina.engine.definition

import org.scalatest.BeforeAndAfter
import org.scalatest.BeforeAndAfterAll
import org.scalatest.Matchers
import org.scalatest.WordSpecLike
import akka.actor.{ ActorSystem}
import akka.testkit.ImplicitSender
import akka.testkit.TestKit
import org.scalatest.mock.MockitoSugar

/**
 * Created by levinev on 29/07/2015.
 */
class StateDescriptorSpec
    extends TestKit(ActorSystem("cucina-test"))
    with ImplicitSender
    with WordSpecLike
    with Matchers
    with BeforeAndAfterAll
    with BeforeAndAfter
    with MockitoSugar {
  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "StateDescriptor" when {
    "used to build" should {
      "create actor" in {
        val tr = mock[TransitionDescriptor]
        val sd = new StateDescriptor("sa", List(tr))
        val ac = system.actorOf(sd.props)
        println(ac)
      }
    }
  }
}

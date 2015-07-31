package org.cucina.engine

import java.util

import akka.actor.{Props, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.collection.mutable

/**
 * Created by levinev on 29/07/2015.
 */
class ProcessGuardianSpec extends TestKit(ActorSystem("cucina-test"))
with ImplicitSender
with WordSpecLike
with Matchers
with BeforeAndAfterAll
with BeforeAndAfter
with MockitoSugar{

  override def afterAll() = {
    TestKit.shutdownActorSystem(system)
  }

  "ProcessGuardian actor" when {
    "created" should {
      "work" in {
        val pi = system.actorOf(Props[ProcessGuardian])
        println("Pi " + pi)
      }
    }

    "received Start" should {
      "start process" in {
        val pi = system.actorOf(Props[ProcessGuardian])
        pi ! StartProcess("fake", new Object, null, Map[String, Object]())
        expectMsgPF() {
          case a@_ =>
            println("a " + a)
        }
      }
    }
  }
}

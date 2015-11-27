package org.cucina.engine.actors

import akka.actor.{PoisonPill, ActorRef, Actor}
import org.cucina.engine.{ExecuteFailed, ExecuteComplete, ProcessContext}
import org.cucina.engine.definition.Token
import org.slf4j.LoggerFactory

/**
 * A stateful actor created only for duration of a particular split.
 *
 * Created by vlevine
 *
 */
case class CollectionLaunch(collection: Seq[Object], transition: ActorRef)
case class DivisionLaunch(obj: Object, transitions: Iterable[ActorRef])

class SplitLauncher(mySender: ActorRef, parentPc: ProcessContext) extends Actor {
  private val LOG = LoggerFactory.getLogger(getClass)
  private var launched: Int = 0

  def receive = {
    case CollectionLaunch(coll, tr) =>
<<<<<<< HEAD:engine/src/main/scala/org/cucina/engine/actors/SplitLauncher.scala
      coll.foreach[Unit](o => createToken(o, tr, ""))
      LOG.info("Launched=" + launched)
    case DivisionLaunch(obj, trs) =>
      trs.foreach[Unit](tr => createToken(obj, tr, tr.hashCode().toString))
=======
      coll.foreach[Unit](o => launchToken(o, tr))
      LOG.info("Launched=" + launched)
    case DivisionLaunch(obj, trs) =>
      trs.foreach[Unit](tr => launchToken(obj, tr))
>>>>>>> origin/master:src/main/scala/org/cucina/engine/actors/SplitLauncher.scala
      LOG.info("Launched=" + launched)
    case ec@ExecuteComplete(pc) =>
      LOG.info("Received " + ec)
      launched -= 1
      if (launched == 0) {
        LOG.info("Last complete, notifying sender " + mySender)
        mySender ! ExecuteComplete(parentPc)
        self ! PoisonPill
      }

    case ef@ExecuteFailed(c, f) =>
      LOG.info("Received " + ef)
      parentPc.token.children.clear
      mySender ! ExecuteFailed(c, f)
      self ! PoisonPill
  }

<<<<<<< HEAD:engine/src/main/scala/org/cucina/engine/actors/SplitLauncher.scala
  private def createToken(o: Object, tr: ActorRef, splitId: String) = {
    val t = Token(o, parentPc.token.processDefinition, splitId)
    LOG.info("Created token " + t)
=======
  private def launchToken(o: Object, tr: ActorRef) = {
    LOG.info("Creating token for " + o)
    val t = Token(o, parentPc.token.processDefinition, launched)
>>>>>>> origin/master:src/main/scala/org/cucina/engine/actors/SplitLauncher.scala
    parentPc.token.children += t
    t.parent = Some(parentPc.token)
    val processContext = ProcessContext(t, parentPc.parameters, parentPc.client)
    tr ! StackRequest(processContext, List())
    launched += 1
  }
}
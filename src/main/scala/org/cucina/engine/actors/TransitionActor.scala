package org.cucina.engine.actors

import akka.util.Timeout
import org.cucina.engine.ProcessContext
import org.cucina.engine.definition.{CheckDescriptor, StateDescriptor, OperationDescriptor}
import akka.actor._
import org.slf4j.LoggerFactory
import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * @author levinev
 */
case class Occur(processContext: ProcessContext)

case class CheckPassed(processContext: ProcessContext, remains: Seq[CheckDescriptor])

case class CheckFailed(checkName: String, reason: String)

class TransitionActor(name: String, output: String,
                      leaveOperations: Seq[OperationDescriptor]= List(),
                      checks: Seq[CheckDescriptor] = List())
  extends Actor {
  private val LOG = LoggerFactory.getLogger(getClass)
  var outputState: ActorRef = _

  override def preStart() = {
    try {
      implicit val resolveTimeout = Timeout(500 millis)
      outputState = Await.result(context.actorSelection("../../" + output).resolveOne(), resolveTimeout.duration)
      LOG.info("Located output state:" + outputState)
    } catch {
      case e: ActorNotFound => {
        LOG.error("Failed to find output state @ " + "../../" + output)
        self ! PoisonPill
      }
    }
  }

  def receive = {
    case Occur(pc) => {
      // TODO build stack and execute it
      pc.token.stateId = null
    }

    case e@_ => LOG.debug("Unknown event:" + e)
  }
}

object TransitionActor {
  def props(id: String, output: String, leaveOperations: Seq[OperationDescriptor] , checks: Seq[CheckDescriptor]): Props = {
    Props(classOf[TransitionActor], id, output, leaveOperations, checks)
  }
}
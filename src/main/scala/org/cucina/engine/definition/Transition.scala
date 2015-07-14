package org.cucina.engine.definition

import org.cucina.engine.ProcessContext
import org.slf4j.LoggerFactory

/**
 * @author levinev
 */
class Transition {
  private[this] val LOG = LoggerFactory.getLogger(getClass())
  val id: String
  val output: State
  val input: State
  val checks: Array[Check]
  def isEnabled(processContext: ProcessContext) = {
    input.canLeave(processContext) && (findFirstFailingCondition(processContext) == null)
  }

  private[this] def findFirstFailingCondition(processContext: ProcessContext): Check = {
    if (checks != null) {
      for (check: Check <- checks) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Check is " + check)
        }

        if (!processContext.test(check)) {
          LOG.debug("Check failed!")

          return check
        }
      }
    }
    return null
  }
}
package org.cucina.engine.definition

import org.cucina.engine.ProcessContext
import org.slf4j.LoggerFactory

/**
 * @author levinev
 */
class TransitionDescriptor(val id: String, val output: StateDescriptor, checks: Iterable[CheckDescriptor]) {
  private[this] val LOG = LoggerFactory.getLogger(getClass())
  def isEnabled(processContext: ProcessContext) = {
    //input.canLeave(processContext) && (findFirstFailingCondition(processContext) == null)
  }

  private[this] def findFirstFailingCondition(processContext: ProcessContext): CheckDescriptor = {
    if (checks != null) {
      for (check: CheckDescriptor <- checks) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Check is " + check)
        }

/*        if (!processContext.test(check)) {
          LOG.debug("Check failed!")

          return check
        }
*/      }
    }
    return null
  }
}
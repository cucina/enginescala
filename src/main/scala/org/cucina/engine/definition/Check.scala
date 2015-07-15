package org.cucina.engine.definition

import org.cucina.engine.ProcessContext

/**
 * @author levinev
 */
trait Check {
  def test(processContext: ProcessContext): Boolean
}

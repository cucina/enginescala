package org.cucina.engine

import org.cucina.engine.definition.{Token, Check}

/**
 * @author levinev
 */
class ProcessContext(val token: Token, val parameters: Map[String, Object]) {
   def test(check:Check) :Boolean = {
      false
     // need to call to ProcessDriver
   }
}
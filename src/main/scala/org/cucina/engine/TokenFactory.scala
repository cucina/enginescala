package org.cucina.engine

import org.cucina.engine.definition.Token
import org.cucina.engine.definition.ProcessDefinition
import org.slf4j.LoggerFactory

/**
 * @author levinev
 */
class TokenFactory {
  private[this] val LOG = LoggerFactory.getLogger(getClass())
  def createToken(definition: ProcessDefinition, domainObject: Object): Token = {
    require(definition != null, "The 'definition' parameter cannot be null.")
    require(domainObject != null, "The 'domainObject' parameter cannot be null.")

    // call to tokenRepository to find an existing one for the object
    val token: Token = tokenRepository.findByDomain(domainObject)

    if (token != null) {
      LOG.debug("Found existing token for the object :" + token)
      token
    } else {
      new Token(domainObject, definition)
    }
  }
}
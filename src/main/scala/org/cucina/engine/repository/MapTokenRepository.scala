package org.cucina.engine.repository

import org.cucina.engine.actors.TokenRequest
import org.cucina.engine.definition.Token

import scala.collection.mutable

/**
 * Created by levinev on 05/08/2015.
 */
class MapTokenRepository extends TokenRepository {
  val map = new mutable.HashMap[(Object, String), Token]()

  def findByDomain(op: TokenRequest): Option[Token] = {
    map.get((op.domainObject, op.processDefinitionName))
  }

  def store(token:Token) = {
    map += (token.domainObject, token.processDefinition.id) -> token
  }
}

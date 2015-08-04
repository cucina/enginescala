package org.cucina.engine.actors.support

import org.cucina.engine.actors.{EnterEvent, LeaveEvent}

/**
 * Created by levinev on 04/08/2015.
 */

trait EnterEventListener {
  def process(enterEvent: EnterEvent)
}

trait LeaveEventListener {
  def process(leaveEvent: LeaveEvent)
}
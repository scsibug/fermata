package code.lib

import net.liftweb.actor._
import code.model.Message
import code.comet.{NewMessage}

abstract trait MessageIndexService extends LiftActor {

  def search(querystr: String, max: Int) : List[Message]
  def indexMessage(msg: Message)
  def indexMessageQuickly(msg: Message)
  def doIndex() : Int
  def indexedMailCount() : Int
  override def messageHandler : PartialFunction[Any, Unit] = {
    case NewMessage(msg: Message) => indexMessage(msg)
    case DoIndex => reply(this.doIndex())
  }

}

case class DoIndex()

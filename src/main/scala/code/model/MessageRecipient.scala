package code.model

import _root_.net.liftweb.mapper._
import _root_.net.liftweb.util._
import _root_.net.liftweb.common._

class MessageRecipient extends LongKeyedMapper[MessageRecipient] with IdPK {
  def getSingleton = MessageRecipient
  object message extends MappedLongForeignKey(this, Message)
  object recipient extends MappedLongForeignKey(this, Recipient)
}

object MessageRecipient extends MessageRecipient with LongKeyedMetaMapper[MessageRecipient] {
  def join (rpt : Recipient, msg : Message) =
    this.create.recipient(rpt).message(msg).save
}

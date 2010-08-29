package code.model

import _root_.net.liftweb.mapper._
import _root_.net.liftweb.util._
import _root_.net.liftweb.common._

class Message extends LongKeyedMapper[Message] with IdPK {
  def getSingleton = Message
  object msgBody extends MappedText(this)
  object subject extends MappedText(this)
  object sender extends MappedEmail(this, 256)
}

object Message extends Message with LongKeyedMetaMapper[Message] {
  override def fieldOrder = List(sender,msgBody)
}

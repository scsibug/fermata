package code.snippet

import _root_.scala.xml.{NodeSeq, Text}
import _root_.net.liftweb.util._
import _root_.net.liftweb.mapper._
import _root_.net.liftweb.common._
import _root_.net.liftweb.http.S 
import _root_.java.util.Date
import code.lib._
import code.model.Message
import Helpers._

class Messages {

  def list(xhtml: NodeSeq) = {
    bind("message", xhtml,
         "header" -> Message.findAll().flatMap(
           b => <li><a href={"/msg/" + b.primaryKeyField}>{b.sender}: {b.subject}</a></li>)
       )
  }

  def detail(xhtml: NodeSeq) = {
    val msgid = S.param("msgId") getOrElse {"0"}
    val message : Box[Message] = Message.getMessageById(msgid.toLong)
    message match {
      case Full(m) => bind("message", xhtml, 
                           "sender" -> m.sender,
                           "subject" -> m.subject,
                           "sentDate" -> m.sentDate,
                           "textContent" -> m.textContent)
      case _ => <strong>Could not find message</strong>
    }
  }
}

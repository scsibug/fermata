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

  def detail(xhtml: NodeSeq) = S.param("msgId") match {
    case Full(msgid) => val msgbox = Message.getMessageById(msgid.toLong)
                        val msgcontent : String = msgbox.run("No Message Found or No Text Content") 
                              { (s: String, m: Message) => m.textContent }
                        <pre>{msgcontent}</pre>
    case _ => <pre>Error processing message ID</pre>
  }

}

package code.snippet

import _root_.scala.xml.{NodeSeq, Text}
import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import _root_.java.util.Date
import code.lib._
import code.model.Message
import Helpers._

class Messages {
  def list(xhtml: NodeSeq) = {
    bind("message", xhtml,
         "header" -> Message.findAll().flatMap(
           b => <li>{b.sender}: {b.subject}</li>)
       )
  }
}

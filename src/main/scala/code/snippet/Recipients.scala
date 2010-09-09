package code.snippet

import _root_.scala.xml.{NodeSeq, Text}
import _root_.net.liftweb.util._
import _root_.net.liftweb.mapper._
import _root_.net.liftweb.common._
import _root_.net.liftweb.http.S 
import code.lib._
import code.model.Recipient
import Helpers._

class Recipients {
  def list(xhtml: NodeSeq) = {
    val count = S.attr("count", _.toInt) openOr 20
    bind("recipients", xhtml,
         "latest" -> Recipient.findAll(MaxRows(count)).flatMap(
           r => <li>{r.addressIndex}</li>
         )
       )
  }
}

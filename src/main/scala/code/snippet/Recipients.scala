package code.snippet

import _root_.scala.xml.{NodeSeq, Text}
import _root_.net.liftweb.util._
import _root_.net.liftweb.mapper._
import _root_.net.liftweb.common._
import net.liftweb.http.{S,DispatchSnippet,Paginator,PaginatorSnippet,  
  SortedPaginator,SortedPaginatorSnippet}
import net.liftweb.mapper.view.{SortedMapperPaginatorSnippet,SortedMapperPaginator}
import code.lib._
import code.model.Recipient
import code.model.Message
import Helpers._
import S.?

class Recipients extends DispatchSnippet {

  override def dispatch = {
    case "listMessages" =>  listMessages _
    case "list" =>  list _
    case "address" => address _
  }

  def list(xhtml: NodeSeq) = {
    val count = S.attr("count", _.toInt) openOr 20
    bind("recipients", xhtml,
         "latest" -> Recipient.findAll(MaxRows(count)).flatMap(
           r => <li><a href={"/recipient/" + r.primaryKeyField}>{r.addressIndex}</a></li>
         )
    )
  }

  def listMessages(xhtml: NodeSeq) = {
    val rcptid = S.param("rcptId") getOrElse {"0"}
    val messages : List[Message] = Message.getMessagesByRecipient(rcptid.toLong)
    val count = S.attr("count", _.toInt) openOr 100
    bind("recipient", xhtml,
         "messages" -> messages.flatMap(
           m => <li><a href={"/msg/" + m.primaryKeyField}>{m.sender}: {m.subject}</a></li>
         )
    )
  }

  def address(xhtml: NodeSeq) = {
    val rcptid = S.param("rcptId") getOrElse {"0"}
    val recipient = Recipient.findByKey(rcptid.toLong)
    recipient match {
      case Full(r) => Text(r.addressIndex)
      case _ => Nil
    }
  }
}

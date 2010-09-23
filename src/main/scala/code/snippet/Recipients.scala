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
import code.model.MessageRecipient
import code.model.Message
import Helpers._
import S.?

class Recipients extends DispatchSnippet {

  override def dispatch = {
    case "listMessages" =>  listMessages _
    case "address" => address _

    case "all" => all _
    case "top" => top _
    case "paginate" => paginator.paginate _
  }

  val paginator = new SortedMapperPaginatorSnippet(Recipient,Recipient.id, "ID" -> Recipient.id){
    override def itemsPerPage = 20
    _sort = (0,false)
    override def prevXml: NodeSeq = Text(?("Prev"))
    override def nextXml: NodeSeq = Text(?("Next"))
    override def firstXml: NodeSeq = Text(?("First"))
    override def lastXml: NodeSeq = Text(?("Last"))
  }

  protected def many(recipients: List[Recipient], xhtml: NodeSeq): NodeSeq = 
    recipients.flatMap(a => single(a,xhtml))

  protected def single(r: Recipient, xhtml: NodeSeq): NodeSeq =
    bind("a", xhtml,
         "linkedaddress" -> <a href={"/recipient/" + r.primaryKeyField}>{r.addressIndex}</a>
       )
    
  // Display all entries the paginator returns
  def all(xhtml: NodeSeq): NodeSeq = many(paginator.page,xhtml)

  // Show pagination links
  def paginate(xhtml: NodeSeq) {
    paginator.paginate(xhtml)
  }

  // Show most recent, no pagination offsets
  def top(xhtml: NodeSeq) = {
    val count = S.attr("count", _.toInt) openOr 20
    many(Recipient.findAll(MaxRows(count), OrderBy(Recipient.id, Descending)),xhtml)
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

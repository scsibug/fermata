package code.snippet

import _root_.scala.xml.{NodeSeq, Text}
import _root_.net.liftweb.util._
import _root_.net.liftweb.mapper._
import _root_.net.liftweb.common._
import _root_.net.liftweb.http.S
import net.liftweb.http.{S,DispatchSnippet,Paginator,PaginatorSnippet,  
  SortedPaginator,SortedPaginatorSnippet}
import net.liftweb.mapper.view.{SortedMapperPaginatorSnippet,SortedMapperPaginator}
import _root_.java.util.Date
import code.lib._
import code.model.Message
import code.model.Recipient
import Helpers._
import S.?

class Messages extends DispatchSnippet {

  override def dispatch = {
    case "all" => all _
    case "top" => top _ 
    case "paginate" => paginator.paginate _

    case "detail" => detail _
  }

  val paginator = new SortedMapperPaginatorSnippet(Message,Message.id, "ID" -> Message.id){
    override def itemsPerPage = 20
    _sort = (0,false)
    override def prevXml: NodeSeq = Text(?("Prev"))
    override def nextXml: NodeSeq = Text(?("Next"))
    override def firstXml: NodeSeq = Text(?("First"))
    override def lastXml: NodeSeq = Text(?("Last"))
  }

  protected def many(messages: List[Message], xhtml: NodeSeq): NodeSeq = 
    messages.flatMap(a => single(a,xhtml))

  protected def single(m: Message, xhtml: NodeSeq): NodeSeq =
    bind("a", xhtml,
      "sender" -> m.sender,
      "subject" -> m.subject,
      "sent" -> <abbr class="timeago" title={m.atomSentDate}>{m.sentDate}</abbr>,
      "linkedsubject" -%> <a href={"/msg/"+m.primaryKeyField}>{m.subject}</a>
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
    many(Message.findAll(MaxRows(count), OrderBy(Message.id, Descending)),xhtml)
  }

  def detail(xhtml: NodeSeq) = {
    val msgid = S.param("msgId") getOrElse {"0"}
    val message : Box[Message] = Message.getMessageById(msgid.toLong)

    message match {
      case Full(m) =>
        bind("message", xhtml, 
          "sender" -> m.sender,
          "subject" -> m.subject,
          "sent" -> <abbr class="timeago" title={m.atomSentDate}>{m.sentDate}</abbr>,
          "textContent" -> m.textContent,
          "recipients" -> m.recipientsPrintable,
          "headers" -> m.getHeaders(),
          "attachments" -> m.getAttachments().map{x => <li>{x}</li>})
      case _ => <strong>{?("Could not find message")}</strong>
    }
  }
}

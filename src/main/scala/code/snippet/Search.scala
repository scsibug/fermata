package code.snippet

import _root_.scala.xml.{NodeSeq, Text}
import _root_.net.liftweb.util._
import _root_.net.liftweb.mapper._
import _root_.net.liftweb.common._
import net.liftweb.http.{S,DispatchSnippet,SHtml,RequestVar}
import code.lib._
import code.model.Message
import Helpers._

class Search extends DispatchSnippet {

  override def dispatch = {
    case "search" => search _
    case "results" => results _
  }

  object query extends RequestVar[String]("")

  def search(xhtml: NodeSeq): NodeSeq = {

    def doSearch () = {
      println("query is " + query)
      //val msgs = MessageIndex.search(query,10)
    }

    bind("search", xhtml,
         "query" -> SHtml.text(query.is, query(_)),
         "submit" -> SHtml.submit("Search", doSearch)
       )
  }

  def results(xhtml: NodeSeq): NodeSeq = {
      if (query.is=="") {
        return Text("")
      }
      val msgs = MessageIndex.search(query,10)
      many(msgs,xhtml)
  }

  def many(messages: List[Message], xhtml: NodeSeq): NodeSeq = 
    messages.flatMap(a => single(a,xhtml))

  def single(msg: Message, xhtml: NodeSeq): NodeSeq =
    bind("a", xhtml,
      "sender" -> msg.sender,
      "subject" -> msg.subject,
      "date" -> msg.sentDate,
      "linkedsubject" -%> <a href={"/msg/"+msg.primaryKeyField}>{msg.subject}</a>
    )
}

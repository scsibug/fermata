package code.api

import scala.xml.{Node, NodeSeq}

import net.liftweb.common.{Box,Full,Logger}
import net.liftweb.http.{AtomResponse,BadResponse,CreatedResponse,GetRequest,LiftResponse,LiftRules,NotFoundResponse,ParsePath,PutRequest,Req,RewriteRequest}
import net.liftweb.http.rest.XMLApiHelper
import net.liftweb.mapper.By

import code.model.{Message,MessageRecipient}

object RestAPI extends XMLApiHelper {

  def dispatch: LiftRules.DispatchPF = {
    case Req("api" :: "msg" :: "atom" :: Nil, "", GetRequest) => () => showRecentMessagesAtom()
    case Req("api" :: "recipient" :: recipient :: "atom" :: Nil, "", GetRequest) => () => showRecentMessagesForRecipientAtom(recipient)
    case Req("api" :: x :: Nil, "", _) => failure _
  }

  def failure(): LiftResponse = {
    val ret: Box[NodeSeq] = Full(<op id="FAILURE"></op>)
    NotFoundResponse()
  }

  def createTag(in: NodeSeq) = {
    <fermata_api>in</fermata_api>
  }

  def showRecentMessagesAtom(): AtomResponse = {
    AtomResponse(Message.toAtomFeed(Message.getLatestMessages(20)))
  }

  def showRecentMessagesForRecipientAtom(rcpt: String): AtomResponse = {
    AtomResponse(Message.toAtomFeed(MessageRecipient.recentMessagesForRecipient(rcpt.toLong, 20)))
//      MessageRecipient.findAll(By(MessageRecipient.id,rcpt.toLong),MaxRows(20), OrderBy(MessageRecipient.id, Descending)).map(_.message.obj.open_!)))
  }

}

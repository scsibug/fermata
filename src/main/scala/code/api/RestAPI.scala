package code.api

import scala.xml.{Node, NodeSeq}
import _root_.net.liftweb.http.S
import net.liftweb.common.{Box,Full,Logger}
import net.liftweb.http.{AtomResponse,BadResponse,CreatedResponse,GetRequest,LiftResponse,LiftRules,NotFoundResponse,ParsePath,PutRequest,Req,RewriteRequest}
import net.liftweb.http.rest.XMLApiHelper
import net.liftweb.mapper.By

import code.model.{Message,MessageRecipient,Recipient}

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
    AtomResponse(Message.toAtomFeed(
      "Fermata Recently Received Messages",
      currentUri,
      Message.getLatestMessages(20)))
  }

  def showRecentMessagesForRecipientAtom(rcpt: String): AtomResponse = {
    val recipientB = Recipient.find(By(Recipient.id,rcpt.toLong))
    val recipientAddr = (recipientB.map(_.addressIndex)) openOr rcpt
    AtomResponse(Message.toAtomFeed(
      "Recent Messages for "+recipientAddr,
      currentUri,
      MessageRecipient.recentMessagesForRecipient(rcpt.toLong, 20)))
  }

  // The URI for the current request.
  def currentUri = {
    val uri = S.request.map(_.uri) openOr ("")
    S.hostAndPath+uri
  }

}

package org.texart.fermata

import scala.collection.mutable.HashMap

import org.subethamail.smtp.server.SMTPServer
import org.subethamail.smtp.MessageHandlerFactory
import org.subethamail.smtp.MessageHandler
import org.subethamail.smtp.RejectException
import org.subethamail.smtp.MessageContext

import com.sun.mail.smtp.SMTPMessage
import com.sun.mail.smtp.SMTPMessage
import javax.mail.{Part, Multipart}

import net.liftweb.common.Logger


import code.model.Message

import java.io.{IOException, InputStream, ByteArrayInputStream}
import java.util.Date

import org.apache.commons.io.IOUtils

object MailServerManager {
  var serverMap = new HashMap[String, SMTPServer]
 
  def startServer(name: String, port: Int) {
    var server = new SMTPServer(new LoggingMessageHandlerFactory())
    server.setPort(port)
    serverMap += name -> server
    server.start()
  }

  def stopServer(name: String) = serverMap.remove(name) match {
      case None => false
      case Some(x) => {x.stop() ; true}
    }

  def portsInUse():Iterable[Int] = 
    serverMap.values.map{i => i.getPort}
}

class LoggingMessageHandlerFactory extends MessageHandlerFactory {
  def create(ctx:MessageContext) = new Handler(ctx)
}

class Handler(ctx: MessageContext) extends MessageHandler with Logger {
  var from = ""
  var recipients = List[String]()
  var msg : SMTPMessage = _

  debug("Creating new Handler")

  @throws(classOf[RejectException])
  def from(f: String) {
    from = f
  }

  @throws(classOf[RejectException])
  def recipient(recipient: String) {
    recipients = recipient :: recipients
  }

  @throws(classOf[IOException])
  def data(data: InputStream) {
    val base = IOUtils.toByteArray(data)
    msg = new SMTPMessage(null, new ByteArrayInputStream(base))
    info("Message received.")
    debug("All recipients: " ++ (recipients mkString " "))

    val msg_entity = Message.create
    msg_entity sender from
    msg_entity subject msg.getSubject()
    msg_entity sentDate (new Date())
    msg_entity messageId msg.getMessageID()
    msg_entity msgBody base
    msg_entity textContent (getText(msg) getOrElse null)
    msg_entity save
  }

  def getText(p:Part):Option[String] = {
    var txtbox : Option[String] = None
    if (p.isMimeType("text/plain")) {
      val txt = p.getContent().asInstanceOf[String]
      if (txt != null) txtbox = Some(txt)
    } else if (p.isMimeType("multipart/*")) {
      txtbox = Some("blah")
      val mp : Multipart = p.getContent().asInstanceOf[Multipart]
      val range = 0.until(mp.getCount())
      for (i <- range) {
        val bp:Part = mp.getBodyPart(i)
        if (bp.isMimeType("text/plain")) {
          txtbox = Some(bp.getContent().asInstanceOf[String])
        } else if (bp.isMimeType("multipart/*")) {
          txtbox = getText(bp)
        }
      }
    }
  txtbox
  }

  def done {}
}



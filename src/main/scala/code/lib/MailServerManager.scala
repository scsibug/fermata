package code.lib

import scala.collection.mutable.HashMap

import org.subethamail.smtp.server.SMTPServer
import org.subethamail.smtp.MessageHandlerFactory
import org.subethamail.smtp.MessageHandler
import org.subethamail.smtp.RejectException
import org.subethamail.smtp.MessageContext

import com.sun.mail.smtp.SMTPMessage
import javax.mail.{Part, Multipart}

import net.liftweb.common.Logger
import net.liftweb.actor._
import code.comet.{MostRecentMail, NewMessage}

import com.google.inject._

import code.model.Message
import code.model.Recipient
import code.model.MessageRecipient

import java.io.{IOException, InputStream, ByteArrayInputStream}
import java.util.Date

import org.apache.commons.io.IOUtils

<<<<<<< HEAD:src/main/scala/org/texart/fermata/MailServerManager.scala
@Singleton
class MailServerManager extends MailServerManagerService with Logger {
  val serverMap = new HashMap[String, SMTPServer]
  @Inject var msgIdx: MessageIndexService = _

  def startServer(name: String, port: Int) {
    info("Starting mail server "+name+" on port "+port.toString)
    var server = new SMTPServer(new LoggingMessageHandlerFactory(msgIdx))
    server.setPort(port)
    serverMap += name -> server
    server.start()
  }

  def stopServer(name: String) = {
    info("Shutting down mailserver "+name)
    serverMap.remove(name) match {
      case None => false
      case Some(x) => {x.stop() ; true}
    }
  }

  def portsInUse():Iterable[Int] = 
    serverMap.values.map{i => i.getPort}
}

class LoggingMessageHandlerFactory(msgIdx: MessageIndexService) extends MessageHandlerFactory {
  def create(ctx:MessageContext) = new Handler(ctx,msgIdx)
}

class Handler(ctx: MessageContext, msgIdx: MessageIndexService) extends MessageHandler with Logger {
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
    var recipient_entities = recipients.map({x:String => Recipient.recipientFindOrNew(x)})
    recipient_entities.map({x:Recipient => x save; MessageRecipient.join(x,msg_entity)})
    //MostRecentMail ! NewMessage(msg_entity)
    msgIdx ! NewMessage(msg_entity)
  }

  def getText(p:Part):Option[String] = {
    var txtbox : Option[String] = None
    if (p.isMimeType("text/plain")) {
      val txt = p.getContent().asInstanceOf[String]
      if (txt != null) txtbox = Some(txt)
    } else if (p.isMimeType("multipart/*")) {
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



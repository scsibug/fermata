package org.texart.fermata

import scala.collection.mutable.HashMap

import org.subethamail.smtp.server.SMTPServer
import org.subethamail.smtp.MessageHandlerFactory
import org.subethamail.smtp.MessageHandler
import org.subethamail.smtp.RejectException
import org.subethamail.smtp.MessageContext

import com.sun.mail.smtp.SMTPMessage

import java.io.{IOException, InputStream}

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

class Handler(ctx: MessageContext) extends MessageHandler {
  var from = ""
  var recipients = List[String]()
  var msg : SMTPMessage = _

  println("Creating new Handler")

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
    //var datastr = io.Source.fromInputStream(data).mkString
    //println ("Data:\n" ++ datastr)
    msg = new SMTPMessage(null, data)
    println ("Message done.")
    println ("Message from: " ++ from)
    println ("All recipients: " ++ (recipients mkString " "))
    println ("MessageID: " ++ msg.getMessageID())
    println ("Subject: " ++ msg.getSubject())
    println ("Body Size: " ++ (msg.getSize().toString))
  }

  def done {}
}



package org.texart.fermata

import scala.collection.mutable.HashMap

import org.subethamail.smtp.server.SMTPServer
import org.subethamail.smtp.MessageHandlerFactory
import org.subethamail.smtp.MessageHandler
import org.subethamail.smtp.RejectException
import org.subethamail.smtp.MessageContext
import java.io.IOException
import java.io.InputStream


object MailServerManager {
  var serverMap = new HashMap[String, SMTPServer]
 
  def startNewServer(name: String, port: Int) {
    var server = new SMTPServer(new LoggingMessageHandlerFactory())
    server.setPort(port)
    serverMap += name -> server
    server.start()
  }
}

class LoggingMessageHandlerFactory extends MessageHandlerFactory {
  def create(ctx:MessageContext) = new Handler(ctx)
}

class Handler(ctx: MessageContext) extends MessageHandler {

  @throws(classOf[RejectException])
  def from(from: String) {
    
  }

  @throws(classOf[RejectException])
  def recipient(recipient: String) {
    
  }

  @throws(classOf[IOException])
  def data(data: InputStream) {

  }
  
  def done {

  }
}



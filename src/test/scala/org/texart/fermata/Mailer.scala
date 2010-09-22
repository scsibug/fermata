package org.texart.fermata

import com.sun.mail.smtp.SMTPMessage
import javax.mail._
import javax.mail.internet._
import java.util.{Properties}

class Mailer (port: Int) {
  val props : Properties = new Properties()
  props.put("mail.smtp.host", "localhost")
  props.put("mail.smtp.port", port.toString)
  val smtpSession : Session = Session.getDefaultInstance(props, null)

  def sendMsg(body : String,
              subject : String,
              recipients : List[String],
              from: String) {
    val msg:Message = new MimeMessage(smtpSession)
    val rcpts : Array[Address] = new Array(recipients.length)
    recipients.map(new InternetAddress(_)).copyToArray(rcpts)
    msg.setFrom(new InternetAddress(from))
    msg.setRecipients(Message.RecipientType.TO, rcpts)
    msg.setSubject(subject)
    msg.setContent(body, "text/plain")
    Transport.send(msg)
  }
}

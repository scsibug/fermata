import com.sun.mail.smtp.SMTPMessage
import javax.mail._
import javax.mail.internet._
import java.util._

// Quick script for generating a large number of test emails for basic
// UI/index testing.

// Run like...
// scala -cp lib_managed/scala_2.8.0/compile/mail-1.4.1.jar genemail.sh 1000

object GenerateEmail {
  def main(args: Array[String]) {
    if (args.size < 1) {
      println("Usage: scala generate_emails.scala [number-of-emails]")
    } else {
      val sendCount = args(0).toInt
      println("Generating " + sendCount.toString + " emails")
      val props : Properties = new Properties()
      props.put("mail.smtp.host", "localhost")
      props.put("mail.smtp.port", "2500")
      val smtpSession : Session = Session.getDefaultInstance(props, null)
      val rcpts : Array[Address] = Array(new InternetAddress("aoeu@aoeu.com"))
      println("Sending messages...")
      for (i <- 1 to sendCount)
        sendMsg(smtpSession, "HW Subj", "Hello World", rcpts, "aoeuFROM@example.com")
    }
  }

  def sendMsg(session : Session, body : String, subject : String, recipients : Array[Address], from: String) {
      val msg:Message = new MimeMessage(session)
      msg.setFrom(new InternetAddress(from))
      msg.setRecipients(Message.RecipientType.TO, recipients)
      msg.setSubject(subject)
      msg.setContent(body, "text/plain")
      print(".")
      Transport.send(msg)
  }
}

GenerateEmail.main(args)
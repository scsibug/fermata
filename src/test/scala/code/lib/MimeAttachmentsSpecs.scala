package code.lib

import org.specs.Specification
import org.specs.runner.JUnit4

import javax.mail.internet.{MimeBodyPart,MimeMessage}
import javax.mail.Session

import javax.mail.internet.InternetAddress

class MimeAttachmentsSpecsAsTest extends JUnit4(MessageIndexSpecs)

object MimeAttachmentsSpecs extends Specification {

  "MimeAttachments" should {

    "Be instantiable from MimeMessage" in {
      MimeAttachments(new MimeMessage(null.asInstanceOf[Session])) must notBeNull
    }

    "Calculate the number of attachments" in {}

    "Find the best plain/text representation" in {
      val bodyText = "hello world"
      val msg = new MimeMessage(null.asInstanceOf[Session])
      msg.addFrom(Array(new InternetAddress("joe@example.com")))
      msg.setText(bodyText)
      val m = MimeAttachments(msg)
      m.getText must be equalTo(Some(bodyText))
    }

    "Return attachment MIME type" in {}

    "Flatten multipart hierarchy into list" in {}
  }
}

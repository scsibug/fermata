package code.lib

import com.sun.mail.smtp.SMTPMessage
import javax.mail.internet.MimeMessage
import javax.mail.{Part, Multipart}
import javax.mail.Session

import net.liftweb.common.Logger

import java.io.{IOException, InputStream, ByteArrayInputStream}
import org.apache.commons.io.IOUtils

class MimeAttachments(m: MimeMessage) {

  def getText:Option[String] = getText(m)

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
}

object MimeAttachments {

  def apply(msg: MimeMessage) = {
    new MimeAttachments(msg)
  }

  def apply(data: InputStream) = {
    val m = new MimeMessage(null, new ByteArrayInputStream(IOUtils.toByteArray(data)))
    new MimeAttachments(m)
  }
}

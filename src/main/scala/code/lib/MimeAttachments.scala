package code.lib

import com.sun.mail.smtp.SMTPMessage
import javax.mail.internet.MimeMessage
import javax.mail.{Part, Multipart}
import javax.mail.Session
import _root_.scala.xml.{NodeSeq, Text}

import net.liftweb.common.Logger

import java.io.{IOException, InputStream, ByteArrayInputStream}
import org.apache.commons.io.{IOUtils}

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

  def allAttachments(msgUrl: String): Seq[NodeSeq] = allAttachments(msgUrl,m,0)

  def allAttachments(msgUrl: String, p:Part, id:Int): Seq[NodeSeq] = {
    if (p.isMimeType("multipart/*")) {
      val mp : Multipart = p.getContent().asInstanceOf[Multipart]
      val range = 0.until(mp.getCount())
      return range.flatMap{x:Int => allAttachments(msgUrl,mp.getBodyPart(x),(id+1))}
    } else {
      val ct: String = p.getContentType().takeWhile(_!=';')
      var fn: String = p.getFileName()
      if (fn == null || fn == "") {fn = "Unnamed"}
      val attachmentLink = <a href={msgUrl+"/attachments/"+id.toString()}>{fn}</a>
      if (ct=="text/html") {
        return List(<li class="mime-text-html" title={ct}>{fn}</li>)
      } else if (ct.startsWith("text")) {
        return List(<li class="mime-text-x-generic" title={ct}>{fn}</li>)
      } else if (ct.startsWith("image")) {
        return List(<li class="mime-image-x-generic" title={ct}>{attachmentLink}</li>)
      } else {
        return List(<li class="mime-package-x-generic" title={ct}>{fn}</li>)
      }
    }
  }
  
  def attachmentsList = attachments(m)

  def attachments(p:Part): Seq[Part] = {
    if (p.isMimeType("multipart/*")) {
      val mp : Multipart = p.getContent().asInstanceOf[Multipart]
      val range = 0.until(mp.getCount())
      range.flatMap{x:Int => attachments(mp.getBodyPart(x))}
    } else {
        return List(p)
    }
  }

  def getAttachment(id:Int): Pair[String,InputStream] = {
    val ais = attachmentsList(id-1).getInputStream()
    return Pair("text/plain", ais);
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

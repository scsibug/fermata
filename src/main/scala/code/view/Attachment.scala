package code.view

import net.liftweb.common.{Box,Empty,Full}
import net.liftweb.http.{ForbiddenResponse,InMemoryResponse,LiftResponse,OutputStreamResponse}
import java.io.{OutputStream,ByteArrayOutputStream}
import net.liftweb.common.Logger
import code.model.Message
import org.apache.commons.io.{IOUtils}

object Attachment extends Logger {
  def view (msgid: String, attId: String): Box[LiftResponse] = {
    val message : Box[Message] = Message.getMessageById(msgid.toLong)
    val response = message match {
      case Full(msg) => {
        val (contentType, attachmentStream) = msg.getAttachment(attId.toInt)
        val os :OutputStream = new ByteArrayOutputStream()
        def writer(os:OutputStream): Unit = {
          IOUtils.copy(attachmentStream,os)
          os.close()
        }
        val osr = OutputStreamResponse(writer _, List(("Content-Type", contentType)))
        Full(osr)
      }
      case _ => Empty
    }
    response
  }
}


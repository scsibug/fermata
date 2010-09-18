package code.model

import _root_.net.liftweb.mapper._
import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import _root_.scala.xml.{NodeSeq, Text}
import org.apache.lucene.document.{Document,Field}
import com.sun.mail.smtp.SMTPMessage

import java.io.{IOException, InputStream, ByteArrayInputStream}


class Message extends LongKeyedMapper[Message] with IdPK {
  def getSingleton = Message
  object msgBody extends MappedBinary(this)
  object subject extends MappedText(this)
  object sender extends MappedEmail(this, 256)
  object sentDate extends MappedDateTime(this)
  object messageId extends MappedString(this, 256)
  object textContent extends MappedText(this)
  object lazy_recipients extends HasManyThrough(this, Recipient,
    MessageRecipient, MessageRecipient.recipient, MessageRecipient.message)

  def recipients = MessageRecipient.findAll(By(MessageRecipient.message, this.id)).map(_.recipient.obj.open_!)

  def recipientsPrintable() = {
    val rlinks = recipients.map({x => val el = <a href={"/recipient/" + x.primaryKeyField}>{x.addressIndex}</a>
                                      el.asInstanceOf[NodeSeq]
                                })
    rlinks.reduceLeft((x,y) => x ++ Text(", ") ++ y)
  }

  def getHeaders() : String = {
    val msg = new SMTPMessage(null, new ByteArrayInputStream(msgBody))
    val headers = msg.getAllHeaderLines()
    val headertext = new StringBuilder()
    while(headers.hasMoreElements()) {
      headertext.append(headers.nextElement()).append("\n")
    }
    headertext.toString()
  }

  // Convert to a Lucene Document for indexing
  def toDocument(): Document = {
    val doc = new Document
    doc.add(mkField(id,id.toString,true,false))
    doc.add(mkField(textContent,textContent,true,true))
    doc.add(mkField(subject,subject,true,true))
    doc.add(mkField(sender,sender,true,true))
    def mkField[A,B <: net.liftweb.mapper.Mapper[B]](
        name: MappedField[A,B],
        value: String,
        store: Boolean,
        searchable: Boolean): Field = {
      val fieldStore = if (store) Field.Store.YES else Field.Store.NO
      val indexed = if (searchable) Field.Index.ANALYZED else Field.Index.NO
      val valNoNull = if (value==null) "" else value
      new Field(name.dbColumnName,valNoNull,fieldStore,indexed)
    }
    doc
  }

}

object Message extends Message with LongKeyedMetaMapper[Message] {
  override def dbTableName = "messages"
  override def fieldOrder = List(sender,subject,sentDate,msgBody)
  // A descending index on the 'id' column significantly improves
  // performance on the H2 database with >100,000 messages.  Mapper
  // doesn't appear to support making those kinds of indexes though.
  // A workaround is to drop the existing index, and run:
  // create index messages on messages(id desc);

  def getMessageById(id : Long) : Box[Message] = {
    val msg : List[Message] = Message.findAll(By(Message.primaryKeyField, id))
    val msgbox = msg match {
      case Nil => Empty
      case m :: _ => Full(m)
    }
    msgbox
  }

  def getLatestMessage() : Box[Message] = {
    val msg : List[Message] = Message.findAll(OrderBy(Message.primaryKeyField, Descending), MaxRows(1))
    val msgbox = msg match {
      case Nil => Empty
      case m :: _ => Full(m)
    }
    msgbox
  }

  def getMessagesByRecipient(id : Long) : List[Message] = {
    MessageRecipient.findAll(By(MessageRecipient.recipient, id)).map(_.message.obj.open_!)
  }

}

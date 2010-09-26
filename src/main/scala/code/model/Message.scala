package code.model

import _root_.net.liftweb.mapper._
import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import _root_.scala.xml.{NodeSeq, Text}
import _root_.net.liftweb.http.S
import org.apache.lucene.document.{Document,Field}
import com.sun.mail.smtp.SMTPMessage
import java.text.{SimpleDateFormat}
import java.io.{IOException, InputStream, ByteArrayInputStream}
import java.util.{Date}


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

  def recipientsPrintable = {
    val rlinks = recipients.map({x => val el = <a href={"/recipient/" + x.primaryKeyField}>{x.addressIndex}</a>
                                 el.asInstanceOf[NodeSeq]})
    if (rlinks isEmpty) Text("") else
      rlinks.reduceLeft((x,y) => x ++ Text(", ") ++ y)
  }

  def getHeaders() : String = {
    val headertext = new StringBuilder()    
    if (msgBody.get != null) {
      val msg = new SMTPMessage(null, new ByteArrayInputStream(msgBody))
      val headers = msg.getAllHeaderLines()
      while(headers.hasMoreElements()) {
        headertext.append(headers.nextElement()).append("\n")
      }
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
    doc.add(new Field("all",allFieldsToText,Field.Store.NO,Field.Index.ANALYZED))
    doc
  }

  def atomDateFormatter(d: Date) = {
    val formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    formatter.format(d)
  }

  def toAtomEntry = {
    val uri = S.request.map(_.uri) openOr ("")
    val atomUrl = S.hostAndPath+"/msg/"+id
    <entry>
	<title>{subject.is}</title>
	<link href={atomUrl} />
	<id>{atomUrl}</id>
	<updated>{atomDateFormatter(sentDate.is)}</updated>
        <content>{textContent}</content>
    </entry>
  }

  def allFieldsToText(): String = {
    val all = new StringBuffer()
    all.append(textContent).append("\n")
    all.append(subject).append("\n")
    all.append(sender).append("\n")
    all.append(getHeaders).append("\n")
    recipients.map({r =>
      all.append(r).append("\n")
                  })
    all.toString()
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

  def toAtomFeed(title: String, feedUri: String, msgs: List[Message]) = {
    val entries = msgs.map(_.toAtomEntry)
    val updatedDate = if (msgs isEmpty) new Date() else msgs.head.sentDate.is
<feed xmlns="http://www.w3.org/2005/Atom">
  <author><name>Fermata</name></author>
  <id>{feedUri}</id>
  <link href={feedUri} />
  <title>{title}</title>
  <updated>{atomDateFormatter(updatedDate)}</updated>
  {entries}
</feed>
}

  def getMessageById(id : Long) : Box[Message] = {
    val msg : List[Message] = Message.findAll(By(Message.primaryKeyField, id))
    val msgbox = msg match {
      case Nil => Empty
      case m :: _ => Full(m)
    }
    msgbox
  }

  def getLatestMessages(count: Int) : List[Message] = {
    Message.findAll(OrderBy(Message.primaryKeyField, Descending), MaxRows(count))
  }

  def getLatestMessage() : Box[Message] = getLatestMessages(1) headOption

  def getMessagesByRecipient(id : Long) : List[Message] = {
    MessageRecipient.findAll(By(MessageRecipient.recipient, id)).map(_.message.obj.open_!)
  }

}

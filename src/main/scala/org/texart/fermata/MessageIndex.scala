package org.texart.fermata
import org.apache.lucene.index.{IndexWriter}
import org.apache.lucene.store.{SimpleFSDirectory}
import org.apache.lucene.analysis.standard.{StandardAnalyzer}
import org.apache.lucene.util.Version.{LUCENE_30}
import org.apache.lucene.document.{Document,Field}
import net.liftweb.actor._
import code.comet.{NewMessage}
import code.model.Message
import java.io.File

object MessageIndex extends LiftActor {
  var index : IndexWriter = {
    val index_dir = new File("fermata_index")
    index_dir mkdir
    val dir = new SimpleFSDirectory(index_dir)
    val analyzer = new StandardAnalyzer(LUCENE_30)
    new IndexWriter(dir, analyzer, IndexWriter.MaxFieldLength.UNLIMITED)
  }

  override def messageHandler : PartialFunction[Any, Unit] = {
    case NewMessage(msg: Message) => {
      println("got new msg")
    }
  }

  def indexMessage(msg: Message) = {
    val doc : Document = new Document()
    val idField : Field =
      new Field("id",
                msg.id.toString,
                Field.Store.YES,
                Field.Index.NO)
    val textContentField : Field =
      new Field("textcontent",
                msg.textContent,
                Field.Store.YES,
                Field.Index.ANALYZED)
    doc.add(idField)
    doc.add(textContentField)
    index addDocument doc
  }

}


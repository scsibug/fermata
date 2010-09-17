package org.texart.fermata
import org.apache.lucene.index.{IndexWriter}
import org.apache.lucene.store.{SimpleFSDirectory,RAMDirectory}
import org.apache.lucene.analysis.snowball.{SnowballAnalyzer}
import org.apache.lucene.util.Version.{LUCENE_30}
import org.apache.lucene.queryParser.{QueryParser}
import org.apache.lucene.search.{IndexSearcher}
import org.apache.lucene.document.{Document,Field}
import net.liftweb.actor._
import net.liftweb.common.Logger
import code.comet.{NewMessage}
import code.model.Message
import java.io.File

class MessageIndex extends LiftActor with Logger {
  var analyzer = new SnowballAnalyzer(LUCENE_30, "English")
  var index : IndexWriter = {
    val dir = new RAMDirectory()
    new IndexWriter(dir, analyzer, IndexWriter.MaxFieldLength.UNLIMITED)
  }

  override def messageHandler : PartialFunction[Any, Unit] = {
    case NewMessage(msg: Message) => {
      println("got new msg")
    }
  }

  def search(querystr: String, max: Int) : List[Message] = {
    val searcher = new IndexSearcher(index.getReader())
    val parser = new QueryParser(LUCENE_30, "textcontent", analyzer)
    val query = parser.parse(querystr)
    val hits = searcher.search(query, null, max).scoreDocs;
    info("Query found " + hits.length + " hits")
    val documents = hits.map({r => searcher.doc(r.doc)})
    searcher.close

    val msgsbox = documents.map({d => Message.getMessageById(d.get("id").toLong)})
    msgsbox.iterator.filter(!_.isEmpty).map(_.open_!).toList
  }

  def indexMessage(msg: Message) = {
    index addDocument (msg.toDocument)
    index commit
  }

  def reindex() {
    info("Starting reindex")
    val msgs : List[Message] = Message.findAll()
    msgs.map({indexMessage(_)})
    info("Reindex completed")
    info("Total documents indexed = "+index.numDocs)
  }

}

object MessageIndex extends MessageIndex {
  reindex
}

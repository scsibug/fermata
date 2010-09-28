package code.lib

import org.apache.lucene.index.{IndexWriter}
import org.apache.lucene.store.{SimpleFSDirectory,RAMDirectory}
import org.apache.lucene.analysis.snowball.{SnowballAnalyzer}
import org.apache.lucene.util.Version.{LUCENE_30}
import org.apache.lucene.queryParser.{QueryParser,ParseException}
import org.apache.lucene.search.{IndexSearcher,Query}
import org.apache.lucene.document.{Document,Field}
import net.liftweb.actor._
import net.liftweb.common.Logger
import code.comet.{NewMessage}
import code.model.Message
import java.io.File
import com.google.inject._

@Singleton
class MessageIndex extends MessageIndexService with Logger {
  var analyzer = new SnowballAnalyzer(LUCENE_30, "English")

  var indexw : IndexWriter = {
    val dir = new RAMDirectory()
    new IndexWriter(dir, analyzer, IndexWriter.MaxFieldLength.UNLIMITED)
  }

  def search(querystr: String, max: Int) : List[Message] = {
    val searcher = new IndexSearcher(indexw.getReader())
    val parser = new QueryParser(LUCENE_30, "all", analyzer)
    var query = null.asInstanceOf[Query]
    try {
      query = parser.parse(querystr)
    } catch {
      case e:ParseException => query = parser.parse(QueryParser.escape(querystr))
    }
    val hits = searcher.search(query, null, max).scoreDocs;
    info("Query found " + hits.length + " hits")
    val documents = hits.map({r => searcher.doc(r.doc)})
    val msgsbox = documents.map({d => Message.getMessageById(d.get("id").toLong)})
    val results = msgsbox.iterator.filter(!_.isEmpty).map(_.open_!).toList
    searcher.close
    results
  }

  def indexMessage(msg: Message) = {
    info("Indexing new message")
    indexMessageQuickly(msg)
    indexw commit
  }

  // Don't commit these documents immediately after indexing
  def indexMessageQuickly(msg: Message) = {
    indexw addDocument (msg.toDocument)
  }

  def doIndex() : Int = {
    info("Starting index of all Messages")
    val msgs : List[Message] = Message.findAll()
    msgs.map({indexMessageQuickly(_)})
    indexw.commit
    info("index built")
    indexw.optimize()
    info("index optimized")
    info("Total messages indexed = "+indexw.numDocs)
    indexw.numDocs
  }

  def indexedMailCount : Int = indexw.numDocs

}

object MessageIndex extends MessageIndex {
  //ensure that an index is built (asynchronously)
  this ! DoIndex
}

package org.texart.fermata

import net.liftweb.mapper.{Schemifier}
import net.liftweb.util.{Log}

import code.util.DBUtil

import org.specs.Specification
import org.specs.runner.JUnit4
import code.model.{Message}
class MessageIndexSpecsAsTest extends JUnit4(MessageIndexSpecs)

object MessageIndexSpecs extends Specification{

  val idx : MessageIndex = new MessageIndex()

  "MessageIndex" should {
    doFirst {
      DBUtil.initialize
      Schemifier.schemify(true, Schemifier.infoF _, Message)
      DBUtil.setupDB("dbunit/search_messages.xml")
    }

    doBefore {
      idx !? DoIndex
    }

    "find a string only when it appears" in {
      var results = idx search("quick", 2)
      (results.length == 1) must beTrue
      results.head.id.get must be equalTo(3)
    }

    "find a message in the index" in {
      var results = idx search("content", 10)
      (results.length >= 1) must beTrue
      results.head.id.get must be equalTo(1)
    }

    "return no more results than requested" in {
      var results = idx search("content", 10)
      (results.length == 2) must beTrue
    }
    
    "find nothing for keywords that are absent in corpus" in {
      var results = idx search("absent",10)
      (results.length == 0) must beTrue
    }
    
    "perform derivation to find more results" in {
      var results = idx search("searched", 10)
      (results.length >= 1) must beTrue
      results.head.id.get must be equalTo(4)
    }

    "perform stemming to find more results" in {
      var results = idx search("fish", 10)
      (results.length >= 1) must beTrue
      results.head.id.get must be equalTo(4)
    }

    "search case-insensitively" in {
      var results = idx search("MESSAGE", 10)
      (results.length >= 1) must beTrue
    }

    "reindex correct number of documents" in {
      val mi = new MessageIndex()
      (mi !? DoIndex) must be equalTo(5)
    }

  }	
}

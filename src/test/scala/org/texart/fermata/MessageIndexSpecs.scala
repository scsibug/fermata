package org.texart.fermata

import net.liftweb.mapper.{Schemifier}
import net.liftweb.util.{Log}

import code.util.DBUtil

import org.specs.Specification
import org.specs.runner.JUnit4
import code.model.{Message}
class MessageIndexSpecsAsTest extends JUnit4(MessageIndexSpecs)

object MessageIndexSpecs extends Specification{
  "MessageIndex" should {
    doFirst {
      DBUtil.initialize
      Schemifier.schemify(true, Schemifier.infoF _, Message)
      DBUtil.setupDB("dbunit/simple_message.xml")
    }
    "search the index for message" in {
      var results = MessageIndex search "content"
        (results.length == 1) must beTrue
    }
  }	
}

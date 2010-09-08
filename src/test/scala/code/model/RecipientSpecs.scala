package code.model

import net.liftweb.mapper.{Schemifier}
import net.liftweb.util.{Log}
import _root_.net.liftweb.common._

import code.util.DBUtil

import org.specs.Specification
import org.specs.runner.JUnit4

class RecipientSpecsAsTest extends JUnit4(MessageSpecs)

object RecipientSpecs extends Specification {
    "Recipient" should {
        doFirst {
            DBUtil.initialize
            Schemifier.schemify(true, Schemifier.infoF _, Message, Recipient, MessageRecipient)
            DBUtil.setupDB("dbunit/multiple_recipients.xml")
        }

        "allow multiple recipients per message" in {
          val msg = Message.getMessageById(1)
          msg.isDefined must be(true)
          msg match {
            case Full(m) => {(m.recipients().size == 3) must beTrue}
            case _ => ()
          }
          
        }

        doLast {
            DBUtil.shutdownDB
            Schemifier.destroyTables_!!(Log.infoF _, Message)
        }
    }
}

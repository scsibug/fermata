package code.model

import net.liftweb.mapper.{Schemifier}
import net.liftweb.util.{Log}

import code.util.DBUtil

import org.specs.Specification
import org.specs.runner.JUnit4

class MessageSpecsAsTest extends JUnit4(MessageSpecs)

object MessageSpecs extends Specification {
    "Message" should {
        doFirst {
            DBUtil.initialize
            Schemifier.schemify(true, Schemifier.infoF _, Message)
            DBUtil.setupDB("dbunit/simple_message.xml")
        }

        "save without problem" in {
            val msg = new Message

            msg.save must beTrue
            (Message.findAll.length == 2) must beTrue
        }

        "find by ID" in {
            val msg = Message.getMessageById(1)
            msg.isDefined must be(true)
        }

        "delete without problem" in {
            val msg = Message.getMessageById(1)

            msg.isDefined must beTrue
            msg.map(x => x.delete_!)

            val msg_gone = Message.getMessageById(1)
            msg_gone.isDefined must beFalse
        }

        doLast {
            DBUtil.shutdownDB
            Schemifier.destroyTables_!!(Log.infoF _, Message)
        }
    }
}

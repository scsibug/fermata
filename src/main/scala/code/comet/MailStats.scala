package code.comet

import net.liftweb.util._
import net.liftweb.http._
import net.liftweb.common._
import net.liftweb.http.js.JsCmds.SetHtml
import scala.xml.Text

import scala.collection.mutable.{HashMap, HashSet}
import net.liftweb.actor._

import code.model.{Message,Recipient}

class MailStats extends CometActor {
  override def defaultPrefix = Full("mailStats")

  // initial render
  def render = bind("subject" -> subject,
                    "msgCount" -> msgCount,
                    "rcptCount" -> rcptCount)

  def msgCount = (<span id="msgCount">{Message.count.toString}</span>)
  def rcptCount = (<span id="rcptCount">{Recipient.count.toString}</span>)

  def subject = {
    val msgbox = Message.getLatestMessage()
    val s = msgbox.run("No messages received yet.") {(s:String,m:Message) => m.subject}
    (<span id="subject">{s}</span>)
  }

  // With every new message, this is called to update stats for connected clients
  override def lowPriority : PartialFunction[Any, Unit] = {
    case NewMessage(msg: Message) => {
      val msgbox = Message.getLatestMessage()
      partialUpdate(SetHtml("subject", Text(msg.subject)))
      msgbox.run() {
        (s:Unit, m:Message) => partialUpdate(SetHtml("subject",Text(m.subject)));
        partialUpdate(SetHtml("msgCount", msgCount));
        partialUpdate(SetHtml("rcptCount", rcptCount));
        Unit
      }
    }
  }

  override def localSetup {
    MailStats ! AddMessageListener(this)
  }

  override def localShutdown {
    MailStats ! RemoveMessageListener(this)
  }

}

case class NewMessage(msg: Message)
case class AddMessageListener(listener: LiftActor)
case class RemoveMessageListener(listener: LiftActor)

object MailStats extends LiftActor {
  val listeners = new HashSet[LiftActor]

  def notifyListeners(msg: Message) = {
    listeners.foreach(_ ! NewMessage(msg))
  }

  protected def messageHandler = {
        case NewMessage(msg: Message) =>
          notifyListeners(msg)
        case AddMessageListener(listener: LiftActor) =>
          listeners += listener
        case RemoveMessageListener(listener: LiftActor) =>
          listeners -= listener
  }
}

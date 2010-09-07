package code.comet

import net.liftweb.util._
import net.liftweb.http._
import net.liftweb.common._
import net.liftweb.http.js.JsCmds.SetHtml
import scala.xml.Text

import scala.collection.mutable.{HashMap, HashSet}
import net.liftweb.actor._

import code.model.Message

class MostRecentMail extends CometActor {
  override def defaultPrefix = Full("recentMail")

  def render = bind("subject" -> subject)

  def subject = (<span id="subject">Original</span>)

  override def lowPriority : PartialFunction[Any, Unit] = {
    case NewMessage(msg: Message) => {
      println("got new msg")
//      val msgbox = Message.getLatestMessage()
      partialUpdate(SetHtml("subject", Text(msg.subject)))
//      msgbox.run() {(s:Unit, m:Message) => partialUpdate(SetHtml("subject", Text(m.subject)));print(m.subject);Unit}
    }
  }

  override def localSetup {
    MostRecentMail ! AddMessageListener(this)
  }

  override def localShutdown {
    MostRecentMail ! RemoveMessageListener(this)
  }

}

case class NewMessage(msg: Message)
case class AddMessageListener(listener: LiftActor)
case class RemoveMessageListener(listener: LiftActor)

object MostRecentMail extends LiftActor {
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

package code.comet

import net.liftweb.util._
import net.liftweb.http._
import net.liftweb.common._
import net.liftweb.http.js.JsCmds.SetHtml
import scala.xml.Text

import code.model.Message

class MostRecentMail extends CometActor {
  override def defaultPrefix = Full("recentMail")

  def render = bind("subject" -> subject)

  def subject = (<span id="subject">Original</span>)

  ActorPing.schedule(this, Tick, 5000L)

  override def lowPriority : PartialFunction[Any, Unit] = {
    case Tick => {
      println("got tick")
      val msgbox = Message.getLatestMessage()
      msgbox.run() {(s:Unit, m:Message) => partialUpdate(SetHtml("subject", Text(m.subject)));print(m.subject);Unit}
      ActorPing.schedule(this, Tick, 5000L)
    }
  }
}
case object Tick

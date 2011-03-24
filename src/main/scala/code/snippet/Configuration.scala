package code.snippet

import _root_.scala.xml.{NodeSeq, Text}
import code.lib._

class Configuration {
  def smtpPort(xhtml: NodeSeq): NodeSeq =
    Text(MailServerManager.portsInUse().mkString(","))
}

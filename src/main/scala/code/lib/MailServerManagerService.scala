package code.lib

abstract class MailServerManagerService {
  def startServer(name: String, port: Int)
  def stopServer(name: String)
  def portsInUse():Iterable[Int]
}

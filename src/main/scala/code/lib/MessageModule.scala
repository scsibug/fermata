package code.lib

import com.google.inject._
import com.google._

class MessageModule extends AbstractModule {
  override protected def configure = {
    bind(classOf[MessageIndexService]).to(classOf[MessageIndex])
    bind(classOf[MailServerManagerService]).to(classOf[MailServerManager])
  }
}

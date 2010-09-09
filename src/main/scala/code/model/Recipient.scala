package code.model

import _root_.net.liftweb.mapper._
import _root_.net.liftweb.util._
import _root_.net.liftweb.common._

class Recipient extends LongKeyedMapper[Recipient] with IdPK {
  def getSingleton = Recipient
  object addressIndex extends MappedEmail(this, 256)
  
}

object Recipient extends Recipient with LongKeyedMetaMapper[Recipient] {
  override def dbTableName = "recipients"
  override def fieldOrder = List(addressIndex)
  override def dbIndexes = UniqueIndex(addressIndex) :: super.dbIndexes
  // FIXME: race condition if address is created elsewhere between find & create...
  def recipientFindOrNew(rcpt : String) = {
    find(By(addressIndex,rcpt)) openOr (create.addressIndex(rcpt))
  }
}


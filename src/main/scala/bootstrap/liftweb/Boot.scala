package bootstrap.liftweb

import net.liftweb._
import util._
import Helpers._

import common._
import http._
import sitemap._
import Loc._
import mapper._

import code.model._
import code.api._
import code.view._

import code.lib.{MessageIndex,MailServerManager}

import java.lang.{Integer => JInteger}

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  def boot {
    if (!DB.jndiJdbcConnAvailable_?) {
      val vendor = 
	new StandardDBVendor(Props.get("db.driver") openOr "org.h2.Driver",
                             Props.get("db.url") openOr 
                             "jdbc:h2:fermata.db;AUTO_SERVER=TRUE",
                             Props.get("db.user"), Props.get("db.password"))

      LiftRules.unloadHooks.append(vendor.closeAllConnections_! _)

      DB.defineConnectionManager(DefaultConnectionIdentifier, vendor)
    }

    LiftRules.unloadHooks.append(mailShutdown _)
    // Use Lift's Mapper ORM to populate the database
    // you don't need to use Mapper to use Lift... use
    // any ORM you want
    Schemifier.schemify(true, Schemifier.infoF _, User, Message, Recipient,
                        MessageRecipient)

    // Start a default mail server
    MailServerManager.startServer("default",
                                  JInteger.getInteger("fermataSmtpPort", 2500).intValue())
    // Ping the indexer, so that it starts up immediately
    MessageIndex ! None
    // Shutdown mail server at Lift unload
    def mailShutdown = {
      MailServerManager.stopServer("default")
      ()
    }

    // where to search snippet
    LiftRules.addToPackages("code")

    LiftRules.dispatch.prepend(RestAPI.dispatch)

    // Send attachments directly
    LiftRules.dispatch.append {
      case Req(List("msg",msgId,"attachments",attId),_,_) =>
        () => Attachment.view(msgId,attId)
    }

    // Rewrites for messages
    LiftRules.statelessRewrite.append {
      // Send requests for /msg/<id> to messages template
      case RewriteRequest (
        ParsePath(List("msg",msgId),_,_,_),_,_) => {
          RewriteResponse("message_detail" :: Nil, Map("msgId" -> msgId))
        }
    }

    // Rewrites for recipients
    LiftRules.statelessRewrite.append {
      // Send requests for /recipient/<id> to messages template
      case RewriteRequest (
        ParsePath(List("recipient",rcptId),_,_,_),_,_) => {
          RewriteResponse("recipient_detail" :: Nil, Map("rcptId" -> rcptId))
        }
    }

    // Build TopLevel SiteMap
    val entries = List(
      Menu.i("Home") / "index", // the simple way to declare a menu
      Menu(Loc("Messages", List("messages"), "Messages")),
      Menu(Loc("Msg", List("message_detail") -> true, "Message Detail", Hidden)),
      Menu.i("Recipients") / "recipients",
      Menu(Loc("Search", List("search"), "Search")),
      Menu(Loc("Rcpts", List("recipient_detail") -> true, "Recipient Detail", Hidden))
      //Menu.i("Addresses") / "addresses",
      //Menu.i("Configuration") / "config",
    )

    // set the sitemap.  Note if you don't want access control for
    // each page, just comment this line out.
    LiftRules.setSiteMap(SiteMap(entries:_*))


    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
    
    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // What is the function to test if a user is logged in?
    LiftRules.loggedInTest = Full(() => User.loggedIn_?)

    // Make a transaction span the whole HTTP request
    S.addAround(DB.buildLoanWrapper)
  }
}

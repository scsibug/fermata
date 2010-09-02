// From http://jgoday.wordpress.com/2009/12/25/lift-testing-with-dbunit-and-specs/
package code.util

import net.liftweb.mapper.{DB,
                          ConnectionIdentifier, DefaultConnectionIdentifier,
                          StandardDBVendor}
import net.liftweb.util.{Log, Props}                                        

import org.dbunit.dataset.{IDataSet}
import org.dbunit.dataset.xml.{XmlDataSet}
import org.dbunit.database.{DatabaseConfig, DatabaseConnection}
import org.dbunit.operation.{DatabaseOperation}                

object DBUtil {
    private var dataset: IDataSet      = null
    private var dbunitConnection: DatabaseConnection = null

    lazy val connectionIdentifier: () => ConnectionIdentifier = {
        () => DefaultConnectionIdentifier
    }                                                            

    def initialize = {
        DB.defineConnectionManager(connectionIdentifier(),
                                  new StandardDBVendor(Props.get("db.driver").openOr(""),
                                                       Props.get("db.url").openOr(""),
                                                       Props.get("db.user"),
                                                       Props.get("db.password")))
    }                                                                                    

    def setupDB(filename: String) = {
        this.dataset = new XmlDataSet(this.getClass().getClassLoader().getResourceAsStream(filename))

        DB.use(connectionIdentifier()) {
            conn => {
                this.dbunitConnection = new DatabaseConnection(conn)
                DatabaseOperation.CLEAN_INSERT.execute(this.dbunitConnection, this.dataset)
            }
        }
    }

    def shutdownDB = {
        DB.use(connectionIdentifier()) {
            conn => {
                try {
                    DatabaseOperation.DELETE.execute(this.dbunitConnection, this.dataset)
                }
                catch {
                    case e: Exception => Log.error(e.getMessage)
                }
            }
        }

    }
}

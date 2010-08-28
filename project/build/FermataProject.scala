import sbt._

class FermataProject(info: ProjectInfo) extends DefaultWebProject(info) {
  val liftVersion = "2.1-SNAPSHOT"
  val scalaToolsSnapshots = "Scala-Tools Maven2 Snapshots Repository" at "http://scala-tools.org/repo-snapshots/"
  val mavenLocal = "Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository"

  override def libraryDependencies = Set(
    "net.liftweb" %% "lift-webkit" % liftVersion % "compile->default",
    "net.liftweb" %% "lift-mapper" % liftVersion % "compile->default",
    "org.mortbay.jetty" % "jetty" % "6.1.22" % "test->default",
    "junit" % "junit" % "4.5" % "test->default",
    "org.scala-tools.testing" % "specs" % "1.6.2.1" % "test->default",
    "com.h2database" % "h2" % "1.2.138",
    "org.subethamail" % "subethasmtp" % "3.1.4"
  ) ++ super.libraryDependencies
}

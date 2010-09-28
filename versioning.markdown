Releasing new versions
======================

 * Remove "SNAPSHOT" from project/build.properties
 * git commit -am 'Bumped version number.'
 * git tag v0.X -s -m 'Version 0.X'
 * git push --tags
 * Increment project/build.properties version, suffix with "-SNAPSHOT"
 * git commit -am 'Bumped version number.'

Creating a binary release
=========================

 * sbt package
 * rename WAR as "fermata-0.X.war"
 * move into standalone-war
 * gradle -PinWar=fermata-0.X.war
 * upload fermata-0.X.war and build/fermata-0.X-standalone.war to S3.
  * hs3 so ghsoftware fermata-0.X-standalone.war < build/fermata-0.X-standalone.war
  * hs3 so ghsoftware fermata-0.X.war < fermata-0.X.war
 * edit http://github.com/scsibug/fermata/wiki to reference new release.

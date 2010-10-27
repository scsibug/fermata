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
 * rename standalone WAR to fermata.war
 * mkdir fermata-0.X
  * add fermata.war, README.markdown, LICENSE, fermata
 * tar -czvf fermata-0.6.tar.gz fermata-0.6
 * upload fermata-0.X.war and build/fermata-0.X-standalone.war to S3.
  * hs3 so ghsoftware fermata-0.X.tar.gz < fermata-0.X.tar.gz
 * edit http://github.com/scsibug/fermata/wiki to reference new release.

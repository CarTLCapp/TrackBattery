name := "play"

import com.typesafe.config._

val conf = ConfigFactory.parseFile(new File("conf/application.conf")).resolve()

version := conf.getString("app.version")

scalaVersion := "2.11.11"

libraryDependencies += jdbc
libraryDependencies += "com.adrianhurt" %% "play-bootstrap" % "1.2-P26-B3"

libraryDependencies += javaJdbc
libraryDependencies += ehcache
libraryDependencies += javaWs

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.45"

libraryDependencies += evolutions
libraryDependencies += guice

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-java-sdk-core" % "1.11.253",
  "com.amazonaws" % "aws-java-sdk-s3" % "1.11.253"
)

libraryDependencies += "org.apache.commons" % "commons-text" % "1.9"

javacOptions ++= Seq(
  "-Xlint:deprecation",
  "-Xlint:unchecked"
)

enablePlugins(AndroidApp)

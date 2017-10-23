
name := "vertx-file-server"
version := "0.1-SNAPSHOT"
scalaVersion := "2.12.3"

lazy val root = (project in file(".")).enablePlugins(SbtTwirl)

parallelExecution in Test := false

mainClass in assembly := Some("org.zella.server.Runner")
mainClass in Compile := Some("org.zella.server.Runner")

// META-INF discarding
assemblyMergeStrategy in assembly := {
  case PathList(ps@_*) if ps.last endsWith
    "io.netty.versions.properties" => MergeStrategy.first
  case "io.netty.versions.properties" => MergeStrategy.last
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  //http://stackoverflow.com/a/30713280/1996639
  case PathList("reference.conf") => MergeStrategy.concat
  case x => MergeStrategy.last
}


libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
// https://mvnrepository.com/artifact/io.vertx/vertx-core
libraryDependencies += "io.vertx" % "vertx-core" % "3.4.2"
// https://mvnrepository.com/artifact/io.vertx/vertx-web
libraryDependencies += "io.vertx" % "vertx-web" % "3.4.2"
//// https://mvnrepository.com/artifact/io.vertx/vertx-rx-java
libraryDependencies += "io.vertx" % "vertx-rx-java" % "3.4.2"
// https://mvnrepository.com/artifact/com.typesafe/config
libraryDependencies += "com.typesafe" % "config" % "1.3.2"
// https://mvnrepository.com/artifact/com.google.code.findbugs/jsr305
libraryDependencies += "com.google.code.findbugs" % "jsr305" % "3.0.2"
// https://mvnrepository.com/artifact/org.scalatest/scalatest_2.12
libraryDependencies += "org.scalatest" % "scalatest_2.12" % "3.0.4" % "test"

libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"
// https://mvnrepository.com/artifact/org.mockito/mockito-all
libraryDependencies += "org.mockito" % "mockito-all" % "1.10.19" % "test"
// https://mvnrepository.com/artifact/io.vertx/vertx-web-client
libraryDependencies += "io.vertx" % "vertx-web-client" % "3.4.2" % "test"



enablePlugins(JettyPlugin)

org.ensime.EnsimeCoursierKeys.ensimeServerVersion in ThisBuild := "2.0.0-M1"
name := "cms"
scalaVersion := "2.12.2"
version := "0.20170501"

val sbtcp = taskKey[Unit]("sbt-classpath")

sbtcp := {
  val files: Seq[File] = (fullClasspath in Compile).value.files
  val sbtClasspath : String = files.map(x => x.getAbsolutePath).mkString(":")
  println("Set SBT classpath to 'sbt-classpath' environment variable")
  System.setProperty("sbt-classpath", sbtClasspath)
}

run <<= (run in Compile).dependsOn(sbtcp)

libraryDependencies += "org.scala-lang" % "scala-compiler" % "2.12.2" // http://mvnrepository.com/artifact/org.scala-lang/scala-compiler
libraryDependencies += "org.flywaydb" % "flyway-core" % "4.1.2" // http://mvnrepository.com/artifact/org.flywaydb/flyway-core
libraryDependencies += "commons-io" % "commons-io" % "2.5" // http://mvnrepository.com/artifact/commons-io/commons-io
libraryDependencies += "joda-time" % "joda-time" % "2.9.9"  // http://mvnrepository.com/artifact/joda-time/joda-time
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3" // http://mvnrepository.com/artifact/ch.qos.logback/logback-classic
libraryDependencies += "com.typesafe.scala-logging" % "scala-logging_2.12" % "3.5.0" // https://mvnrepository.com/artifact/com.typesafe.scala-logging/scala-logging_2.12
libraryDependencies += "org.slf4j" % "slf4j-jdk14" % "1.7.25" // for tomcat: https://mvnrepository.com/artifact/org.slf4j/slf4j-jdk14
libraryDependencies += "org.jsoup" % "jsoup" % "1.10.2"  // http://mvnrepository.com/artifact/org.jsoup/jsoup
libraryDependencies += "com.hubspot.jinjava" % "jinjava" % "2.1.19" // https://mvnrepository.com/artifact/com.hubspot.jinjava/jinjava
libraryDependencies += "com.vladsch.flexmark" % "flexmark-all" % "0.19.0" // https://mvnrepository.com/artifact/com.vladsch.flexmark/flexmark-java
libraryDependencies += "edu.gatech.gtri.typesafeconfig-extensions" % "typesafeconfig-for-webapps" % "1.1" // https://mvnrepository.com/artifact/edu.gatech.gtri.typesafeconfig-extensions/typesafeconfig-for-webapps
libraryDependencies += "com.github.nscala-time" % "nscala-time_2.12" % "2.16.0" // http://mvnrepository.com/artifact/com.github.nscala-time/nscala-time_2.11
libraryDependencies += "com.github.pathikrit" % "better-files_2.12" % "3.0.0" // http://mvnrepository.com/artifact/com.github.pathikrit/better-files_2.11
libraryDependencies += "com.h2database" % "h2" % "1.4.195" // http://mvnrepository.com/artifact/com.h2database/h2

libraryDependencies ++= Seq(
  "scalikejdbc_2.12","scalikejdbc-syntax-support-macro_2.12" // http://mvnrepository.com/artifact/org.scalikejdbc/scalikejdbc_2.12
).map("org.scalikejdbc" % _ % "2.5.1")

libraryDependencies ++= Seq(
  "scalatra_2.12", "scalatra-json_2.12"
).map("org.scalatra" % _ % "2.5.0") // http://mvnrepository.com/artifact/org.scalatra/scalatra_2.12

libraryDependencies ++= Seq(
  "json4s-jackson_2.12", "json4s-ext_2.12"
).map("org.json4s" % _ % "3.5.1") // http://mvnrepository.com/artifact/org.json4s/json4s-jackson_2.12

libraryDependencies ++= Seq(
  "jetty-webapp","jetty-plus","jetty-annotations","jetty-servlets"
).map("org.eclipse.jetty" % _ % "9.4.4.v20170414") // http://mvnrepository.com/artifact/org.eclipse.jetty/jetty-webapp

libraryDependencies += "org.scalatest" % "scalatest_2.12" % "3.0.0" % Test // http://mvnrepository.com/artifact/org.scalatest/scalatest_2.12
libraryDependencies ++= Seq(
  "scalatra-test_2.12", "scalatra-scalatest_2.12"
).map("org.scalatra" % _ % "2.5.0" % Test) // http://mvnrepository.com/artifact/org.scalatra/scalatra-scalatest_2.12

libraryDependencies ++= Seq(
  "selenium-support", // http://mvnrepository.com/artifact/org.seleniumhq.selenium/selenium-support
  "selenium-chrome-driver"  // http://mvnrepository.com/artifact/org.seleniumhq.selenium/selenium-chrome-driver
).map("org.seleniumhq.selenium" % _ % "3.4.0" % Test)

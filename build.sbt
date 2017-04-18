name := "genschema"

organization := "net.metadrift"
sonatypeProfileName := "net.metadrift"

scalaVersion := "2.12.1"

version := "0.3.2"

scalacOptions := Seq("-unchecked",
                     "-deprecation",
                     "-encoding",
                     "utf8",
                     "-feature",
                     "-Xfatal-warnings",
                     "-Xcheckinit",
                     "-Xlint")
scalacOptions in Test ++= Seq("-Yrangepos")

resolvers += Resolver.sonatypeRepo("public")

val circeVersion = "0.7.1"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "com.novocode" % "junit-interface" % "0.11" % "test"
)

libraryDependencies ++= Seq(
  "com.chuusai" %% "shapeless" % "2.3.2"
)

addCommandAlias("format", "; scalafmt --in-place")

/**
  * Test configuration
  */
libraryDependencies += "com.github.fge" % "json-schema-validator" % "2.2.6" % "test"

/**
  *  Publishing information
  */
publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

useGpg := true

pomIncludeRepository := { _ =>
  false
}

pomExtra := <url>https://github.com/metadrift/genschema</url>
  <licenses>
    <license>
      <name>Apache License, Version 2.0</name>
      <url>https://opensource.org/licenses/Apache-2.0</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:metadrift/genschema.git</url>
    <connection>scm:git:git@github.com:metadrift/genschema.git</connection>
  </scm>
  <developers>
    <developer>
      <id>coursera</id>
      <name>Coursera</name>
    </developer>
    <developer>
      <id>sauldhernandez</id>
      <name>Saul Hernandez</name>
      <url>http://github.com/sauldhernandez</url>
    </developer>
    <developer>
      <id>ericbmerritt</id>
      <name>Eric B Merritt</name>
      <url>http://github.com/ericbmerritt</url>
    </developer>
  </developers>

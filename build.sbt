val ZioVersion      = "1.0.0-RC11-1"
val EmbKafkaVersion = "2.3.0"
val Specs2Version   = "4.7.0"
val ArrowVersion    = "0.14.1"

resolvers += Resolver.sonatypeRepo("releases")
resolvers += Resolver.sonatypeRepo("snapshots")

lazy val commonLibs =
  libraryDependencies ++= Seq(
    "dev.zio"                 %% "zio"            % ZioVersion,
    "io.github.embeddedkafka" %% "embedded-kafka" % EmbKafkaVersion % "test",
    "org.specs2"              %% "specs2-core"    % Specs2Version % "test",
    "org.apache.arrow"        % "arrow-vector"    % ArrowVersion
  )

maintainer in Docker := "Clover Group"
dockerUsername in Docker := Some("clovergrp")
dockerUpdateLatest := true

lazy val commonSettings = Seq(
  organization := "CloverGroup",
  //name := "zio-tsp",
  version := "0.0.1",
  scalaVersion := "2.12.8",
  maxErrors := 3,
  parallelExecution in Test := true,
  commonLibs,
  // do not release sub-projects
  skip in publish := true
)

lazy val front = (project in file("zio_front"))
  .settings(
    name := "front",
    commonSettings
  )

lazy val kafka = (project in file("zio-kafka"))
  .settings(
    name := "kafka",
    commonSettings
  )

//lazy val parquet = (project in file("zio-parquet"))
//  .settings(
//    name := "parquet",
//    commonSettings
//  )

//lazy val serdes = (project in file("zio-serdes"))
//  .settings(
//    name := "serdes",
//    commonSettings
//  )

lazy val top = (project in file("."))
  .settings(
    name := "tsp",
    commonSettings
  )
  .settings(
    inTask(assembly)(assemblySettings),
    skip in publish := false,
  )
  .enablePlugins(JavaAppPackaging, UniversalPlugin)
  .dependsOn(kafka, front)
//.aggregate(kafka, parquet)

scalacOptions --= Seq(
  "-Xfatal-warnings"
)

lazy val assemblySettings = Seq(
  assemblyJarName := s"TSP_v${version.value}.jar",
  javaOptions += "--add-modules=java.xml.bind",
  assemblyMergeStrategy := { _ => MergeStrategy.discard } // TODO: Check
)

scriptClasspath := Seq((assemblyJarName in (assembly in top)).value)

// make native packager use only the fat jar
mappings in Universal := {
  // universalMappings: Seq[(File,String)]
  val universalMappings = (mappings in Universal).value
  val fatJar = (assembly in top).value
  // removing means filtering
  val filtered = universalMappings filter {
    case (file, name) => !name.contains(".jar")
  }
  // add the fat jar
  filtered :+ (fatJar -> ("lib/" + fatJar.getName))
}

mappings in Docker := {
  // universalMappings: Seq[(File,String)]
  val universalMappings = (mappings in Universal).value
  val fatJar = (assembly in top).value
  // removing means filtering
  val filtered = universalMappings filter {
    case (file, name) => !name.contains(".jar")
  }
  // add the fat jar
  filtered :+ (fatJar -> ("lib/" + fatJar.getName))
}

// clear the existing docker commands
dockerCommands := Seq()

import com.typesafe.sbt.packager.docker._
dockerCommands := Seq(
  Cmd("FROM", "openjdk:12.0.1-jdk-oracle"),
  Cmd("LABEL", s"""MAINTAINER="${(maintainer in Docker).value}""""),
  // TODO: Add artefacts here
)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)

addCommandAlias("fmt", "all scalafmtSbt scalafmtAll test:scalafmtAll")
addCommandAlias("chk", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")

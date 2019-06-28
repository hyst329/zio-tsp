val ZioVersion      = "1.0.0-RC9"
val EmbKafkaVersion = "2.2.0"

resolvers += Resolver.sonatypeRepo("releases")
resolvers += Resolver.sonatypeRepo("snapshots")

lazy val libs =
  libraryDependencies ++= Seq(
    "dev.zio" %% "zio" % ZioVersion
    //"io.github.embeddedkafka" %% "embedded-kafka" % EmbKafkaVersion % test
  )

lazy val commonSettings = Seq(
  organization := "CloverGroup",
  name := "zio-tsp",
  version := "0.0.1",
  scalaVersion := "2.12.8",
  maxErrors := 3,
  libs
)

lazy val kafka = (project in file("zio-kafka"))
  .settings(
    commonSettings,
    libs
  )

lazy val root = (project in file("."))
  .settings(commonSettings)
  .dependsOn(kafka)

scalacOptions := Seq(
  "-Xsource:2.13",
  "-Xlint",
  "-Xverify",
  "-feature",
  "-deprecation",
  "-explaintypes",
  "-unchecked",
  "-Xfuture",
  "-encoding",
  "UTF-8",
  "-Yrangepos",
  "-Xlint:_,-type-parameter-shadow",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused",
  "-Ywarn-value-discard",
  "-language:higherKinds",
  "-language:existentials",
  "-Yno-adapted-args",
  "-Ypartial-unification",
  //"-Xfatal-warnings",
  "-Xlint:-infer-any,_",
  "-Ywarn-value-discard",
  "-Ywarn-numeric-widen",
  "-Ywarn-extra-implicit",
  "-Ywarn-unused:_",
  "-Ywarn-inaccessible",
  "-Ywarn-nullary-override",
  "-Ywarn-nullary-unit",
  "-opt-inline-from:<source>",
  "-opt-warnings",
  "-opt:l:inline"
)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("chk", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")

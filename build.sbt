name := "MoneyTransfer"

version := "1.0"

scalaVersion := "2.12.2"

resolvers ++= Seq("Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/releases",
    "Maven Central" at "http://central.maven.org/maven2/"
)

val circeVersion = "0.9.3"
val finchVersion = "0.21.0"
val scalaTestVersion = "3.0.5"
val twitterServerVersion = "18.6.0"

libraryDependencies ++= Seq(
    "org.scalactic" %% "scalactic" % scalaTestVersion,
    "org.scalatest" %% "scalatest" % scalaTestVersion % "test",

    "com.github.finagle" %% "finch-core" % finchVersion,
    "com.github.finagle" %% "finch-circe" % finchVersion,

    "io.circe" %% "circe-core" % circeVersion,
    "io.circe" %% "circe-generic" % circeVersion,

    "org.scala-stm" %% "scala-stm" % "0.8",

    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
    "ch.qos.logback" % "logback-classic" % "1.2.3",

    "org.apache.commons" % "commons-lang3" % "3.1"
)

assemblyMergeStrategy in assembly := {
    case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
    case _ => MergeStrategy.first
}

// parallel causes problems with cleaning up the state
parallelExecution in Test := false

scalacOptions += "-deprecation"


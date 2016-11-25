val Version = "0.3-SNAPSHOT"

lazy val root = (project in file(".")).settings(
  name := "cloudfront-report",
  version := Version,
	organization := "cn.amazonaws",
  scalaVersion := "2.11.6",
  libraryDependencies ++= Seq(
    "com.amazonaws" % "aws-lambda-java-core" % "1.1.0",
    ("com.amazonaws" % "aws-lambda-java-events" % "1.3.0"),
    "com.maxmind.geoip" % "geoip-api" % "1.3.1",
    "org.scalaj" %% "scalaj-http" % "2.3.0",
		"org.scalatest" %% "scalatest" % "2.2.4" % "test"
  ),
  assemblyJarName in assembly := "cloudfront-report-%s.jar" format(Version),
  dependencyOverrides += "joda-time" % "joda-time" % "2.8.2"
)

assemblyMergeStrategy in assembly := {
  case PathList("org", "joda", "time", "base", "BaseDateTime.class") => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}


version := "0.1"

scalaVersion := "2.13.4"


lazy val Cat = "org.typelevel" %% "cats-core" % "2.1.1"
lazy val CatEffects = "org.typelevel" %% "cats-effect" % "2.3.1"
lazy val Fs2 = "co.fs2" %% "fs2-core" % "3.0.0-M9"
lazy val Fs2IO = "co.fs2" %% "fs2-io" % "3.0.0-M9"
lazy val PureConfig = "com.github.pureconfig" %% "pureconfig" % "0.14.0"
lazy val ScalaTest = "org.scalatest" %% "scalatest" % "3.2.2" % Test
lazy val Scalatic = "org.scalactic" %% "scalactic" % "3.2.2"
lazy val BouncyCastle ="org.bouncycastle" % "bcprov-jdk15on" % "1.68"

lazy val root = (project in file(".")).settings(
  name := "cryptography",
  version := "0.0.1",
  scalaVersion := "2.13.4",
  fork in run := true,
  javaOptions in run ++= Seq(
//    "-Xms256M", "-Xmx3G", "-XX:MaxPermSize=1024M", "-XX:+UseConcMarkSweepGC"),
  "-Xms1G", "-Xmx3G", "-XX:MaxPermSize=1024M", "-XX:+UseConcMarkSweepGC"),
//  javaOptions in ThisBuild ++= Seq("-Xmx1g"),

  libraryDependencies ++= Seq(
    Fs2,
    Fs2IO,
    PureConfig,
    BouncyCastle,
    ScalaTest,
    Scalatic
  ),
  addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")

)
enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)
dockerRepository := Some("nachocode")
packageName in Docker := "cinvestav-is-lab01"
version in Docker := "latest"
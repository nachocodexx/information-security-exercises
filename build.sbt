
version := "0.1"

scalaVersion := "2.13.4"


lazy val Cat = "org.typelevel" %% "cats-core" % "2.1.1"
lazy val CatEffects = "org.typelevel" %% "cats-effect" % "2.3.1"
lazy val Fs2 = "co.fs2" %% "fs2-core" % "2.5.0"
//lazy val Fs2IO = "co.fs2" %% "fs2-io" % "3.0-5795280"
lazy val Fs2IO = "co.fs2" %% "fs2-io" % "2.5.0"
lazy val PureConfig = "com.github.pureconfig" %% "pureconfig" % "0.14.0"
lazy val Slf4j ="io.chrisdavenport" %% "log4cats-slf4j" % "1.1.1"
lazy val LogbackClassic= "ch.qos.logback" % "logback-classic" % "1.2.3"
lazy val ScalaMeter= "com.storm-enroute" %% "scalameter" % "0.20"
lazy val ScalaTest = "org.scalatest" %% "scalatest" % "3.2.2" % Test
lazy val Scalatic = "org.scalactic" %% "scalactic" % "3.2.2"
lazy val BouncyCastle ="org.bouncycastle" % "bcprov-jdk15on" % "1.68"

lazy val root = (project in file(".")).settings(
  name := "cryptography",
  version := "0.0.1",
  scalaVersion := "2.13.4",
  resolvers ++= Seq(
    "Artima Maven Repository" at "https://repo.artima.com/releases",
    "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/releases",
  ),
//testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework"),
//  parallelExecution in Test := false,
//  fork := true,
//  javaOptions += "-Xmx4G",
  testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest,"-o"),
  libraryDependencies ++= Seq(
    Cat,
    CatEffects,
    Fs2,
    Fs2IO,
    PureConfig,
    Slf4j,
    LogbackClassic,
    BouncyCastle,
//    ScalaMeter,
    ScalaTest,
    Scalatic
  )

)
enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)
dockerRepository := Some("nachocode")
packageName in Docker := "cinvestav-is-lab01"
version in Docker := "latest"
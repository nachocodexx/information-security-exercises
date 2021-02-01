
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



lazy val root = (project in file(".")).settings(
  name := "cryptography",
  version := "0.0.1",
  scalaVersion := "2.13.4",
  resolvers += "Sonatype OSS Snapshots" at
    "https://oss.sonatype.org/content/repositories/releases",
  testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework"),
  parallelExecution in Test := false,
  fork := true,
  useCoursier:= false,
  semanticdbEnabled := false,
  outputStrategy := Some(StdoutOutput),
//  connectInput := true,
  libraryDependencies ++= Seq(
    Cat,
    CatEffects,
    Fs2,
    Fs2IO,
    PureConfig,
    Slf4j,
    LogbackClassic,
    ScalaMeter
  )

)
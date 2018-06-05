val nm = "sgxtools"
val ver = "1.0.7"


// Basic info
name := nm
version := ver
scalaVersion := "2.12.0"

// sbt assembly to generate fat jar
assemblyJarName in assembly := nm + "-" + ver + ".jar"

// Main class
mainClass in (Compile,run) := Some("commandline.CommandLineEngine")
mainClass in assembly := Some("commandline.CommandLineEngine")

// Dependencies
unmanagedJars in Compile += file("lib/sgxlib-1.0.5.jar")
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"
libraryDependencies += "org.rogach" %% "scallop" % "2.1.0"

// Testing
testOptions in Test += Tests.Argument("-oF")

// Make build info accessible programmatically from object buildinfo.BuildInfo
lazy val root = (project in file(".")).
  enablePlugins(BuildInfoPlugin).
  settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion),
    buildInfoPackage := "buildinfo"
  )


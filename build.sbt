val nm = "sgxtools"
val ver = "1.0"

// Basic info
name := nm
version := ver
scalaVersion := "2.11.8"

// sbt assembly to generate fat jar
assemblyJarName in assembly := nm + "-" + ver + ".jar"

// Main class
mainClass in (Compile,run) := Some("commandline.CommandLineEngine")
mainClass in assembly := Some("commandline.CommandLineEngine")

// Dependencies
unmanagedJars in Compile += file("lib/sgxlib-1.0.jar")
libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"
libraryDependencies += "org.rogach" %% "scallop" % "2.1.0"

// Testing
testOptions in Test += Tests.Argument("-oF")


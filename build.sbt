name := "sgxtools"

version := "1.0"

scalaVersion := "2.11.8"

unmanagedJars in Compile += file("lib/sgxlib-assembly-1.0.jar")

mainClass in (Compile,run) := Some("commandline.CommandLineEngine")

mainClass in assembly := Some("commandline.CommandLineEngine")

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"

testOptions in Test += Tests.Argument("-oF")
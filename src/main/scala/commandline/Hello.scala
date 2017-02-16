package commandline

object Hello extends CommandLineProgram {
  override lazy val toolName: String = "Hello"
}

final case class Hello(name: String) {
  println("Hello " + name)
}


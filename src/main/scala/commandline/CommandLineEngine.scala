package commandline

import org.rogach.scallop.{ScallopConf, ScallopOption, Subcommand}

trait CommandLineProgram {
  /**
    * Name of tool to be passed as subcommand on command line
    */
  val toolName: String
}

/**
  * The main program that manages all subcommands
  */
object CommandLineEngine extends App {

  // Scallop configuration
  private class Config(arguments: Seq[String]) extends ScallopConf(arguments) {

    // Text for help menu
    version("\nsgxtools 1.0\n")
    banner(
      """Usage: java -jar sgxtools-1.0.jar [subcommand] [options]
        |
        |Options:
      """.stripMargin)
    footer("\nDocumentation: https://github.com/pamelarussell/sgxtools\n")

    // The subcommands
    val hello = new Subcommand(Hello.toolName) {
      val name: ScallopOption[String] = opt[String](
        required = true,
        descr = "Your name")
    }

    // Add the subcommands to the configuration
    addSubcommand(hello)

    // Verify the configuration
    verify()

  }

  override def main(args: Array[String]) {

    // Make the scallop configuration from the args
    val conf = new Config(args)

    // Match the subcommand
    conf.subcommand match {
      case Some(conf.hello) =>
        val nm = conf.hello.name.getOrElse(throw new IllegalArgumentException("Invalid option"))
        Hello(nm)
      case _ =>
        conf.printHelp()
        sys.exit(0)
    }
  }

}



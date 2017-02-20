package commandline

import java.io.File

import buildinfo.BuildInfo
import feature.Orientation
import org.rogach.scallop.{ScallopConf, ScallopOption, Subcommand}

trait CommandLineProgram {

  /** Name of tool to be passed as subcommand on command line */
  val toolName: String

  /** One line tool description */
  val descr: String

}

/**
  * The main program that manages all subcommands
  */
object CommandLineEngine extends App {

  // Scallop configuration
  private class Config(arguments: Seq[String]) extends ScallopConf(arguments) {

    // The subcommands
    val featureCounts = new Subcommand(FeatureCounts.toolName) {
      descr(FeatureCounts.descr)
      val bam: ScallopOption[String] = opt[String](required = true, descr = "Bam file")
      val gtf: ScallopOption[String] = opt[String](required = true, descr = "GTF2.2 file")
      val fpstrand: ScallopOption[String] = opt[String](required = true, descr = s"First of pair orientation " +
        s"with respect to transcript (${Orientation.commaSepList})")
      val out: ScallopOption[String] = opt[String](required = true, descr = "Output table")
    }

    // Add the subcommands to the configuration
    addSubcommand(featureCounts)

    // Text for help menu
    version(s"\n${BuildInfo.name} ${BuildInfo.version}\n")
    banner(
      s"""Usage: java -jar ${BuildInfo.name}-${BuildInfo.version}.jar [subcommand] [options]
        |
        |Options:
      """.stripMargin)
    footer(s"\nDocumentation: https://github.com/pamelarussell/${BuildInfo.name}\n")


    // Verify the configuration
    verify()

  }

  override def main(args: Array[String]) {

    // Make the scallop configuration from the args
    val conf = new Config(args)

    // Match the subcommand
    conf.subcommand match {

      case Some(conf.featureCounts) =>
        val bam = new File(conf.featureCounts.bam.getOrElse(throw new IllegalArgumentException("Invalid option")))
        val gtf = new File(conf.featureCounts.gtf.getOrElse(throw new IllegalArgumentException("Invalid option")))
        val fpstrand = Orientation.fromString(conf.featureCounts.fpstrand.getOrElse(throw new IllegalArgumentException("Invalid option")))
        val out = new File(conf.featureCounts.out.getOrElse(throw new IllegalArgumentException("Invalid option")))
        FeatureCounts(bam, gtf, fpstrand, out)

      case _ =>
        conf.printHelp()
        sys.exit(0)

    }
  }

}



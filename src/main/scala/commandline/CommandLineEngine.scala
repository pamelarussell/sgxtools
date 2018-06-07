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

    val nearestFeature = new Subcommand(NearestFeature.toolName) {
      descr(NearestFeature.descr)
      val intervalList: ScallopOption[String] = opt[String](required = true,
        descr = "Interval file (line format: <id> <chr> <start_inclusive> <end_exclusive>)")
      val gtf: ScallopOption[String] = opt[String](required = true, descr = "GTF2.2 file")
      val out: ScallopOption[String] = opt[String](required = true, descr = "Output table")
    }

    val nearbyFeatures = new Subcommand(NearbyFeatures.toolName) {
      descr(NearbyFeatures.descr)
      val posList: ScallopOption[String] = opt[String](required = true,
        descr = "Position file (line format: <id> <chr> <pos>")
      val gtf: ScallopOption[String] = opt[String](required = true, descr = "GTF2.2 file")
      val dist: ScallopOption[Int] = opt[Int](required = true, descr = "Distance to go out from position")
      val out: ScallopOption[String] = opt[String](required = true, descr = "Output table")
    }

    val bamMergeBlockOverlappers = new Subcommand(BamMergeBlockOverlappers.toolName) {
      descr(BamMergeBlockOverlappers.descr)
      val bam: ScallopOption[String] = opt[String](required = true, descr = "Bam file")
      val intervals: ScallopOption[String] = opt[String](required = true, descr = s"Intervals e.g. chr5:1000-2000:+ separated by '${BamMergeBlockOverlappers.blkSep}'")
      val fpstrand: ScallopOption[String] = opt[String](required = true, descr = s"First of pair orientation " +
        s"with respect to transcript (${Orientation.commaSepList})")
      val out: ScallopOption[String] = opt[String](required = true, descr = "Output BED file")
    }

    val vcfRemoveAlleles = new Subcommand(VcfRemoveAlleles.toolName) {
      descr(VcfRemoveAlleles.descr)
      val vcf: ScallopOption[String] = opt[String](required = true, descr = "Input VCF file")
      val alleles: ScallopOption[String] = opt[String](required = true, descr = "File of alleles to remove, one line per allele, " +
        "format = variant_id  allele (can have multiple lines per variant)")
      val output: ScallopOption[String] = opt[String](required = true, descr = "Output VCF file")
    }

    // Add the subcommands to the configuration
    addSubcommand(featureCounts)
    addSubcommand(nearestFeature)
    addSubcommand(nearbyFeatures)
    addSubcommand(bamMergeBlockOverlappers)
    addSubcommand(vcfRemoveAlleles)

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

      case Some(conf.nearestFeature) =>
        val intervalList = new File(conf.nearestFeature.intervalList
          .getOrElse(throw new IllegalArgumentException("Invalid option")))
        val gtf = new File(conf.nearestFeature.gtf
          .getOrElse(throw new IllegalArgumentException("Invalid option")))
        val out = new File(conf.nearestFeature.out
          .getOrElse(throw new IllegalArgumentException("Invalid option")))
        NearestFeature(intervalList, gtf, out)

      case Some(conf.nearbyFeatures) =>
        val posList = new File(conf.nearbyFeatures.posList
          .getOrElse(throw new IllegalArgumentException("Invalid option")))
        val gtf = new File(conf.nearbyFeatures.gtf
          .getOrElse(throw new IllegalArgumentException("Invalid option")))
        val dist = conf.nearbyFeatures.dist
          .getOrElse(throw new IllegalArgumentException("Invalid option"))
        val out = new File(conf.nearbyFeatures.out
          .getOrElse(throw new IllegalArgumentException("Invalid option")))
        NearbyFeatures(posList, gtf, dist, out)

      case Some(conf.bamMergeBlockOverlappers) =>
        val bam = new File(conf.bamMergeBlockOverlappers.bam.getOrElse(throw new IllegalArgumentException("Invalid option")))
        val intervals = BamMergeBlockOverlappers.blocksFromString(conf.bamMergeBlockOverlappers.intervals.getOrElse(throw new IllegalArgumentException("Invalid option")))
        val fpStrand = Orientation.fromString(conf.bamMergeBlockOverlappers.fpstrand.getOrElse(throw new IllegalArgumentException("Invalid option")))
        val output = new File(conf.bamMergeBlockOverlappers.out.getOrElse(throw new IllegalArgumentException("Invalid option")))
        BamMergeBlockOverlappers(bam, intervals, fpStrand, output)

      case Some(conf.vcfRemoveAlleles) =>
        val vcf = new File(conf.vcfRemoveAlleles.vcf.getOrElse(throw new IllegalArgumentException("Invalid option")))
        val alleleFile = new File(conf.vcfRemoveAlleles.alleles.getOrElse(throw new IllegalArgumentException("Invalid option")))
        val alleles = VcfRemoveAlleles.readAlleles(alleleFile)
        val output = new File(conf.vcfRemoveAlleles.output.getOrElse(throw new IllegalArgumentException("Invalid option")))
        VcfRemoveAlleles(vcf, alleles, output)

      case _ =>
        conf.printHelp()
        sys.exit(0)

    }
  }

}



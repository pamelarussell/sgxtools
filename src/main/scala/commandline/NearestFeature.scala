package commandline

import java.io._

import collection.GTF22FeatureSet
import feature.{Block, GenericFeature, Transcript, Unstranded}

/**
  * [[NearestFeature]] program parameters
  */
object NearestFeature extends CommandLineProgram {
  override lazy val toolName: String = "NearestFeature"
  override lazy val descr: String = "takes a list of genomic intervals and a GTF2.2 file, and finds " +
    "the nearest feature(s) to each interval"
}

/** Finds the nearest features to a list of intervals in a GTF2.2 file and writes the results to a table.
  *
  * @param intervalList File of genomic intervals (line format: [id] [chr] [start_inclusive] [end_exclusive])
  * @param gtf22 GTF2.2 file of features
  * @param output Output table to write
  */
final case class NearestFeature(intervalList: File, gtf22: File, output: File) {

  println("\nNearestFeature...\n")

  println(s"Reading features from ${gtf22.getAbsolutePath}")
  val features = new GTF22FeatureSet(gtf22)
  println(s"Reading intervals from ${intervalList.getAbsolutePath}")
  println(s"Writing nearest features to ${output.getAbsolutePath}")
  val reader = new BufferedReader(new FileReader(intervalList))
  val writer = new FileWriter(output)
  writer.write("id\tchr\tstart\tend\tnearest_features\tnearest_genes\tdistance_to_features\n")

  while(reader.ready()) {
    val line = reader.readLine()
    val tokens = line.split("\\s+")
    if(tokens.length != 4) throw new IllegalArgumentException("Line format: <id> <chr> <start_inclusive> <end_exclusive>")
    val id = tokens(0)
    val chr = tokens(1).replaceAll("^chr", "")
    val start = tokens(2).toInt
    val end = tokens(3).toInt
    val nearest = features.nearest(chr, start, end).toList
    val featNames = nearest.map(_.name.get).mkString(",")
    val genes: Set[String] = nearest.map {
      case t: Transcript => t.geneId
      case _ => None
    }
      .filter(g => g.isDefined)
      .map(g => g.get).toSet
    val geneNames: String = if(genes.isEmpty) "NA" else genes.mkString(",")
    val distances = nearest.map(_.distance(new GenericFeature(Block(chr, start, end, Unstranded), None)))
    val distance: Int = distances.foldLeft[Int](distances.head)((a, b) => {
      if(a != b) throw new IllegalStateException(s"There are unequal distances: $a, $b")
      a
    })
    writer.write(s"$id\t$chr\t$start\t$end\t$featNames\t$geneNames\t$distance\n")
  }

  reader.close()
  writer.close()

  println("\nAll done.\n")

}

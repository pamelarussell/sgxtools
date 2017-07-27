package commandline

import java.io._

import collection.GTF22FeatureSet
import feature.{Block, GenericFeature, Transcript, Unstranded}

/**
  * [[NearestFeature]] program parameters
  */
object NearestFeature extends CommandLineProgram {
  override lazy val toolName: String = "NearestFeature"
  override lazy val descr: String = "takes a list of genomic positions and a GTF2.2 file, and finds " +
    "the nearest feature(s) to each position"
}

/** Finds the nearest features to a list of positions in a GTF2.2 file and writes the results to a table.
  *
  * @param posList File of genomic positions (line format: [id] [chr] [pos])
  * @param gtf22 GTF2.2 file of features
  * @param output Output table to write
  */
final case class NearestFeature(posList: File, gtf22: File, output: File) {

  println("\nNearestFeature...\n")

  println(s"Reading features from ${gtf22.getAbsolutePath}")
  val features = new GTF22FeatureSet(gtf22)
  println(s"Reading positions from ${posList.getAbsolutePath}")
  println(s"Writing nearest features to ${output.getAbsolutePath}")
  val reader = new BufferedReader(new FileReader(posList))
  val writer = new FileWriter(output)
  writer.write("id\tchr\tpos\tnearest_features\tnearest_genes\tdistance_to_features\n")

  while(reader.ready()) {
    val line = reader.readLine()
    val tokens = line.split("\\s+")
    if(tokens.length != 3) throw new IllegalArgumentException("Line format: <id> <chr> <pos>")
    val id = tokens(0)
    val chr = tokens(1).replaceAll("^chr", "")
    val pos = tokens(2).toInt
    val nearest = features.nearest(chr, pos, pos+1).toList
    val featNames = nearest.map(_.name.get).mkString(",")
    val genes: Set[String] = nearest.map {
      case t: Transcript => t.geneId
      case _ => None
    }
      .filter(g => g.isDefined)
      .map(g => g.get).toSet
    val geneNames: String = if(genes.isEmpty) "NA" else genes.mkString(",")
    val distances = nearest.map(_.distance(new GenericFeature(Block(chr, pos, pos+1, Unstranded), None)))
    val distance: Int = distances.foldLeft[Int](distances.head)((a, b) => {
      if(a != b) throw new IllegalStateException(s"There are unequal distances: $a, $b")
      a
    })
    writer.write(s"$id\t$chr\t$pos\t$featNames\t$geneNames\t$distance\n")
  }

  reader.close()
  writer.close()

  println("\nAll done.\n")

}

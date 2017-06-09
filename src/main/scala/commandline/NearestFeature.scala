package commandline

import java.io._

import collection.GTF22FeatureSet
import feature.{Block, GenericFeature, Unstranded}

/**
  * [[NearestFeature]] program parameters
  */
object NearestFeature extends CommandLineProgram {
  override lazy val toolName: String = "NearestFeature"
  override lazy val descr: String = "takes a list of genomic positions and a GTF2.2 file, and finds " +
    "the nearest feature(s) to each position"
}


final case class NearestFeature(posList: File, gtf22: File, output: File) {

  println("\nNearestFeature...\n")

  println(s"Reading features from ${gtf22.getAbsolutePath}")
  val features = new GTF22FeatureSet(gtf22)
  println(s"Reading positions from ${posList.getAbsolutePath}")
  println(s"Writing nearest features to ${output.getAbsolutePath}")
  val reader = new BufferedReader(new FileReader(posList))
  val writer = new FileWriter(output)
  writer.write("id\tchr\tpos\tnearest_features\tdistances\n")

  while(reader.ready()) {
    val line = reader.readLine()
    val tokens = line.split("\\s+")
    if(tokens.length != 3) throw new IllegalArgumentException("Line format: <id> <chr> <pos>")
    val id = tokens(0)
    val chr = tokens(1).replaceAll("^chr", "")
    val pos = tokens(2).toInt
    val nearest = features.nearest(chr, pos, pos+1).toList
    val names = nearest.map(_.name.get).mkString(",")
    val distances = nearest
      .map(_.distance(new GenericFeature(Block(chr, pos, pos+1, Unstranded), None)))
      .mkString(",")
    writer.write(s"$id\t$chr\t$pos\t$names\t$distances\n")
  }

  reader.close()
  writer.close()

  println("\nAll done.\n")

}

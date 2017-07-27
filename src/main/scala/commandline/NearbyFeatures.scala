package commandline

import java.io._

import collection.GTF22FeatureSet
import feature._

/**
  * [[NearbyFeatures]] program parameters
  */
object NearbyFeatures extends CommandLineProgram {
  override lazy val toolName: String = "NearbyFeatures"
  override lazy val descr: String = "takes a list of genomic positions and a GTF2.2 file, and finds " +
    "nearby feature(s) to each position within a specified distance"

  // String representation of relative direction of position with respect to a feature
  private def relDir(pos: Int, feat: Feature): String = {
    val start = feat.getStart
    val end = feat.getEnd
    val strand = feat.getOrientation
    strand match {
      case Plus =>
        if(pos < start) "upstream"
        else if(pos >= end) "downstream"
        else "in_span"
      case Minus =>
        if(pos < start) "downstream"
        else if(pos >= end) "upstream"
        else "in_span"
      case Unstranded =>
        if(pos >= start && pos < end) "in_span"
        else "unstranded"
    }
  }

}

/** Finds nearby features to a list of positions in a GTF2.2 file and writes the results to a table.
  *
  * @param posList File of genomic positions (line format: [id] [chr] [pos])
  * @param gtf22 GTF2.2 file of features
  * @param dist Distance from position within which to report features
  * @param output Output table to write
  */
final case class NearbyFeatures(posList: File, gtf22: File, dist: Int, output: File) {

  println("\nNearbyFeatures...\n")

  println(s"Reading features from ${gtf22.getAbsolutePath}")
  val features = new GTF22FeatureSet(gtf22)
  println(s"Reading positions from ${posList.getAbsolutePath}")
  println(s"Writing nearby features to ${output.getAbsolutePath}")
  val reader = new BufferedReader(new FileReader(posList))
  val writer = new FileWriter(output)
  writer.write("id\tchr\tpos\tnearby_features\tnearby_genes\toverlapping_blocks\tdistance_to_features\t" +
    "distance_to_genes\tposition_direction_wrt_features\tposition_direction_wrt_genes\n")

  while(reader.ready()) {
    val line = reader.readLine()
    val tokens = line.split("\\s+")
    if(tokens.length != 3) throw new IllegalArgumentException("Line format: <id> <chr> <pos>")
    val id = tokens(0)
    val chr = tokens(1).replaceAll("^chr", "")
    val pos = tokens(2).toInt
    val nearby = features.overlappers(chr, Math.max(0, pos-dist), pos + dist + 1, Unstranded).toList

    val overlappingBlks = nearby.
      flatMap(_.getBlocks).
      filter(_.overlaps(Block(chr, pos, pos+1, Unstranded))).
      map(_.toString())
    val overlappingBlkStr = if(overlappingBlks.isEmpty) "NA" else overlappingBlks.mkString(",")

    val featNameDistDirGene: List[(Feature, String, Int, String, Option[String])] = nearby.map(feat => (
      feat,
      feat.name.getOrElse("NA"),
      feat.distance(new GenericFeature(Block(chr, pos, pos+1, Unstranded), None)),
      NearbyFeatures.relDir(pos, feat),
      feat match {
        case t: Transcript => t.geneId
        case _ => None
      }))

    val featNames = if(featNameDistDirGene.isEmpty) "NA" else featNameDistDirGene.map(x => x._2).mkString(",")
    val featDist = if(featNameDistDirGene.isEmpty) "NA" else featNameDistDirGene.map(x => x._3).mkString(",")
    val featDir = if(featNameDistDirGene.isEmpty) "NA" else featNameDistDirGene.map(x => x._4).mkString(",")

    val geneNameDistDir: Map[String, (Int, String)] = featNameDistDirGene.
      filter(x => x._5.isDefined).
      groupBy(x => x._5.get).
      mapValues(x => (x.minBy(y => y._3)._3,
        x.map(y => y._4).distinct match {
          case List(y) => y
          case List("in_span", _) => "in_span"
          case List(_, "in_span") => "in_span"
          case _ => "various"
        }
//        if(x.forall(_._4 == x.head._4)) x.head._4
//        else "various"
      ))

    val geneNames = if(geneNameDistDir.isEmpty) "NA" else geneNameDistDir.keys.mkString(",")
    val geneDist = if(geneNameDistDir.isEmpty) "NA" else geneNameDistDir.values.map(x => x._1).mkString(",")
    val geneDir = if(geneNameDistDir.isEmpty) "NA" else geneNameDistDir.values.map(x => x._2).mkString(",")

    writer.write(s"$id\t$chr\t$pos\t$featNames\t$geneNames\t$overlappingBlkStr\t$featDist\t" +
      s"$geneDist\t$featDir\t$geneDir\n")
  }

  reader.close()
  writer.close()

  println("\nAll done.\n")

}

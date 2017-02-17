package commandline

import java.io.File

import collection.GTF22FeatureSet
import feature.{Feature, Orientation}
import sequencing.SamReader

object FeatureCounts extends CommandLineProgram {
  override lazy val toolName: String = "FeatureCounts"
  override lazy val descr: String = "generates a fragment count table for features in a GTF2.2 file"
}

final case class FeatureCounts(bam: File, gtf22: File, firstOfPairStrandOnTranscript: Orientation, output: File) {
  val fs = new GTF22FeatureSet(gtf22)
  val sr = new SamReader(bam)
  val it: Iterator[Feature] = fs.iterator
  it.foreach(feat => println(s"${feat.name.getOrElse("*")}\t${sr.countCompatibleFragments(feat, firstOfPairStrandOnTranscript)}"))
}

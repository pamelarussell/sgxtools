package commandline

import java.io.{BufferedWriter, File, FileWriter}

import collection.GTF22FeatureSet
import feature._
import sequencing.SamReader

/**
  * [[FeatureCounts]] program parameters
  */
object FeatureCounts extends CommandLineProgram {
  override lazy val toolName: String = "FeatureCounts"
  override lazy val descr: String = "generates a fragment count table for features in a GTF2.2 file"
}

/**
  * Generates counts of reads or fragments compatible with features in a GTF2.2 file and writes to a table.
  *
  * Counts are at the transcript level and/or non-transcribed feature level, not the gene level. If library is
  * strand specific, only reads on the correct strand relative to the feature are counted. If reads are
  * paired, counts are fragment counts, where a fragment consists of two mates both compatible with the feature.
  * If reads are unpaired, counts are read counts.
  *
  * @param bam Bam file, paired or unpaired, stranded or unstranded
  * @param gtf22 GTF2.2 file of features
  * @param firstOfPairStrandOnTranscript Strand relative to transcription strand of read 1 (if reads are paired) or
  *                                      all reads (if unpaired). If read 1 maps to the transcription strand, this parameter
  *                                      should be [[Plus]]. If read 1 maps to the opposite of the transcription strand,
  *                                      this parameter should be [[Minus]]. If reads are not strand-specific, this parameter
  *                                      should be [[Unstranded]].
  * @param output Output table to write
  */
final case class FeatureCounts(bam: File, gtf22: File, firstOfPairStrandOnTranscript: Orientation, output: File) {
  val fs = new GTF22FeatureSet(gtf22)
  val sr = new SamReader(bam)
  val it: Iterator[Feature] = fs.iterator
  val bw = new BufferedWriter(new FileWriter(output))
  bw.write("feature_name\tcompatible_fragments\n")
  it.foreach(feat =>
      bw.write(s"${feat.name.getOrElse("*")}\t${sr.countCompatibleFragments(feat, firstOfPairStrandOnTranscript)}\n")
    )
  bw.close()
}

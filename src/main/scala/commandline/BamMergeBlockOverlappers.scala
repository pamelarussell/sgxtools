package commandline

import java.io.{BufferedWriter, FileWriter, File}
import feature._
import sequencing.{SamReader, SamMapping}
import htsjdk.samtools.SAMRecord

object BamMergeBlockOverlappers extends CommandLineProgram {

  override lazy val toolName: String = "BamMergeBlockOverlappers"
  override lazy val descr: String = "generates a single BED record representing the union of all alignments " +
    "in a given bam file that overlap all of a given list of intervals"

  /**
    * Separator for blocks passed on command line
    */
  val blkSep = ","

  // Get collection of blocks from list passed on command line
  def blocksFromString(blksString: String): Iterable[Block] = {
    blksString.split(blkSep).toIterable.map(Block.fromString(_))
  }

}

/**
  * Generates a single merged feature from the union of all records in a bam file that overlap all of a given
  * list of intervals. In other words, iterates through the bam file and keeps each record such that the record
  * simultaneously overlaps all the given intervals. Then takes the union of all kept records. Writes the single
  * merged record to a BED file.
  *
  * @param bam Bam file
  * @param intervals List of intervals e.g. chr6:1000-2000:+ or chr5:100-300:unstranded, separated by [[BamMergeBlockOverlappers.blkSep]]
  * @param fpStrand Strand relative to transcription strand of read 1 (if reads are paired) or all reads (if unpaired). If read 1 maps to
  *                 the transcription strand, this parameter should be [[Plus]]. If read 1 maps to the opposite of the transcription strand,
  *                 this parameter should be [[Minus]]. If reads are not strand-specific, this parameter should be [[Unstranded]].
  * @param output BED file to write the single record to
  */
final case class BamMergeBlockOverlappers(bam: File, intervals: Iterable[Block], fpStrand: Orientation, output: File) {

  println("\nBamMergeBlockOverlappers...\n")

  println(s"Reading alignments from ${bam.getAbsolutePath}")
  val sr = new SamReader(bam)
  val it: Iterator[SAMRecord] = sr.iterator
  var nOverlap = 0
  val mergedOverlappers: Region = it.foldLeft[Region](Empty)((region, rec) => {
    val feat2: SamMapping = SamMapping(rec, fpStrand)
    if(intervals.forall(in => feat2.blocks.overlaps(in))) {
      nOverlap = nOverlap + 1
      feat2.blocks.union(region)
    }
    else region
  })
  println(s"There were $nOverlap records overlapping intervals ${intervals.mkString(";")}")

  println(s"Writing output to ${output.getAbsolutePath}")
  val bw = new BufferedWriter(new FileWriter(output))
  bw.write(new GenericFeature(mergedOverlappers, Some(s"Merge_${nOverlap}_overlappers")).toBED() + "\n")
  bw.close()

  println("\nAll done.\n")

}
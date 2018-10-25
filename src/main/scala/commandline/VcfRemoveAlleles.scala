package commandline
import java.io.{File, FileWriter, BufferedWriter}

import scala.collection.immutable.HashSet
import scala.io.Source

import htsjdk.variant.variantcontext.{Allele, VariantContext}
import htsjdk.variant.variantcontext.writer.{VariantContextWriter, VariantContextWriterBuilder}
import htsjdk.variant.vcf.VCFFileReader
import htsjdk.variant.vcf.VCFEncoder

import variant.VariantContextMutations._

object VcfRemoveAlleles extends CommandLineProgram {

  /** Name of tool to be passed as subcommand on command line */
  override val toolName: String = "VcfRemoveAlleles"

  /** One line tool description */
  override val descr: String = "transforms a VCF file by removing specified alleles from specified variants," +
    " setting individuals with those alleles to no genotype call"

  /**
    * Reads the alleles to remove from a file
    * Each line format: variant_id  allele
    * Can have multiple lines per variant if removing multiple alleles
    * @param file File
    * @return Map of variant chr, pos, id to set of alleles to remove from the variant
    */
  def readAlleles(file: File): Map[(String, Int, String), Set[Allele]] = {
    val rtrn = new scala.collection.mutable.HashMap[(String, Int, String), Set[Allele]]
    for (line <- Source.fromFile(file).getLines) {
      val tokens = line.split("\\s+")
      if(tokens.length != 4) throw new IllegalArgumentException(s"Invalid line: $line. Correct line format: <chr> <pos> <id> <allele>")
      val chr = tokens(0)
      val pos = tokens(1).toInt
      val id = tokens(2)
      val allele = Allele.create(tokens(3))
      val variant = (chr, pos, id)
      if(!rtrn.contains(variant)) {
        rtrn += ((variant, new HashSet[Allele]()))
      }
      rtrn += ((variant, rtrn.get(variant).get + allele))
    }
    rtrn.toMap
  }

  /**
    * Transforms a VCF record by removing specified alleles if the variant ID is in the map,
    * otherwise just returns the original record
    * @param vc VariantContext
    * @param toRemove Map of variant chr, pos, ID to set of alleles to remove
    * @param errorIfEncountered Set of variants (chr, pos, ID) such that this function will
    *                           throw an exception if the variant context matches something
    *                           in the set
    * @return The VCF record modified accordingly, and the set that causes an error which has
    *         been updated to include the passed variant
    */
  private def transformVariantContext(vc: VariantContext,
                                      toRemove: Map[(String, Int, String), Set[Allele]],
                                      errorIfEncountered: Set[(String, Int, String)]
                                     ) : (VariantContext, Set[(String, Int, String)]) = {
    val variant = (vc.getContig(), vc.getStart(), vc.getID())
    if (errorIfEncountered contains variant) throw new IllegalArgumentException(s"Invalid variant encountered. Maybe it has been seen already? $variant")
    else {
      val updatedErrorIfEncountered = errorIfEncountered + variant
      if (toRemove.contains(variant)) {
        val alleles = toRemove.get(variant).get
        (alleles.foldLeft[VariantContext](vc)((vc, allele) => removeAllele(vc, allele)), updatedErrorIfEncountered)
      } else {
        (vc, updatedErrorIfEncountered)
      }
    }
    }

}

/**
  * Modifies a VCF file, setting individual genotypes to missing for specified variants and alleles.
  * Input is a list of chromosome, position, variant ID, and allele to delete. If an individual has at
  * least one copy of the allele for the variant, their whole genotype will be set to missing. The
  * program will crash if a variant in the VCF file cannot be uniquely determined from the combination
  * of chromosome, position, and ID.
  *
  * @param vcf VCF file
  * @param toRemove Map of (chr, pos, id) -> alleles to remove
  * @param output VCF file to write
  */
final case class VcfRemoveAlleles(vcf: File, toRemove: Map[(String, Int, String), Set[Allele]], output: File) {

  println("\nVcfRemoveAlleles...\n")

  // Print some info
  val nVariants = toRemove.size
  val nAlleles = toRemove.values.flatten.size
  println(s"Removing $nAlleles alleles from $nVariants variants")

  // Set up VCF reader
  val vcfReader = new VCFFileReader(vcf)
  val header = vcfReader.getFileHeader()
  val vcfIter = vcfReader.iterator()

  // Build a VCF writer
  val vcfWriter : VariantContextWriter = new VariantContextWriterBuilder().
    setOutputFile(output).
    setReferenceDictionary(header.getSequenceDictionary()).
    build()
  vcfWriter.writeHeader(header)

  // Keep track of variants that have been seen and throw error if something appears more than once in VCF file
  var errorIfEncountered = Set[(String, Int, String)]()

  // Iterate through the VCF and process each record
  while(vcfIter.hasNext()) {
    val vc = vcfIter.next()
    val transformed = VcfRemoveAlleles.transformVariantContext(vc, toRemove, errorIfEncountered)
    errorIfEncountered = transformed._2
    vcfWriter.add(transformed._1)
  }

  vcfReader.close()
  vcfWriter.close()

  println("\nAll done.\n")

}


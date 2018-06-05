# sgxtools
Command line applications for genomics written in Scala

## Overview

sgxtools provides command line genomics applications using [sgxlib](https://github.com/pamelarussell/sgxlib) under the hood. sgxtools is platform independent and runs on the Java Virtual Machine (JVM).

## Requirements

- Java 8 or higher

## Download

Go to [latest release](https://github.com/pamelarussell/sgxtools/releases/latest) for source code downloads.

## Building from source

To build a monolithic .jar file including all dependencies, [sbt](http://www.scala-sbt.org/) is required.

```
sbt assembly
```

generates `target/scala-[version]/sgxtools-[release].jar`.

## Running sgxtools

```
java -jar sgxtools-[version].jar
```

prints the help menu with list of subcommands and detailed instructions for each.

## Available tools

### FeatureCounts

`FeatureCounts` generates counts of reads or fragments in a BAM file that are compatible with features in a GTF2.2 file, and writes the counts to a table. 

BAM records are considered to be compatible with a given genomic feature (i.e. transcript) if:
- All aligned blocks of the record are fully contained within blocks of the feature
- If strand specific, the read strand matches transcription strand. For example, in a paired end library, if the library is such that read 1 always maps to the transcription strand, then for an RNA transcribed from the plus strand, read 1 should map to the plus strand and read 2 should map to the minus strand. If the reads are single end, the same idea holds but there is no read 2.
- The splice junctions match. In other words, there can be no splice junction of the read mapping that falls strictly within a block of the feature, and vice versa.

If the library is unpaired, the program generates counts of individual reads compatible with each feature.

If the library is paired end, the program generates counts of full fragments compatible with each feature. Full fragments are pairs of reads from the same fragment that:
- Are both compatible with the same feature as defined above
- Are both primary alignments

If the library is paired end, only full fragments (both mates mapped) are counted. The generated counts are for fragments. In other words, both mate pairs of a fragment being compatible with a given feature will count as one mapped fragment.

The program writes a table with one line per feature, with the count in the second column. Counts are at the transcript level and/or non-transcribed feature level, not the gene level.

To run:
```
java -jar sgxtools-[version].jar FeatureCounts --help
```

Options:
```
  -b, --bam  <arg>        Bam file
  -f, --fpstrand  <arg>   First of pair orientation with respect to transcript
                          (+, -, unstranded)
  -g, --gtf  <arg>        GTF2.2 file
  -o, --out  <arg>        Output table
      --help              Show help message
```

Explanation:
- Bam file: sorted, indexed BAM file. Can be paired or unpaired, strand specific or not.
- First of pair orientation with respect to transcript: if strand specific, if first of pair always maps to transcript strand, this is '+'. If first of pair always maps to opposite of transcript strand, this is '-'. If not strand specific, 'unstranded'.
- GTF2.2 file: file of features in [GTF2.2](http://mblab.wustl.edu/GTF22.html) format.
- Output table: file to write output to

### NearestFeature

`NearestFeature` identifies the nearest feature(s) and gene(s) in a GTF2.2 file to each of a list of genomic intervals. GTF2.2 format is strictly enforced.

The distance between a genomic interval and a feature is defined as zero if the interval overlaps the span of the feature (including introns), or the positive distance to the closest position in the feature otherwise. If multiple features lie at the same distance, all features tied for the minimum distance are reported. Gene IDs corresponding to the nearest features are also reported.

The program writes a table with one line per position, reporting the lists of nearest features and genes to each position as well as their common distance from the interval.

To run:
```
java -jar sgxtools-[version].jar NearestFeature --help
```

Options:
```
  -g, --gtf  <arg>        GTF2.2 file
  -o, --out  <arg>        Output table
  -p, --interval-list  <arg>   Interval file (line format: <id> <chr> <start_inclusive> <end_exclusive>)
      --help              Show help message
```

Explanation:
- GTF2.2 file: file of features in [GTF2.2](http://mblab.wustl.edu/GTF22.html) format.
- Interval file: file of genomic intervals. Each line has format: \<id> \<chr> \<start_inclusive> \<end_exclusive>
- Output table: file to write output to

### NearbyFeatures

`NearbyFeatures` identifies the feature(s) and gene(s) in a GTF2.2 file within a specified distance of each of a list of genomic positions. GTF2.2 format is strictly enforced.

The program writes a table with one line per position, reporting the lists of features and genes within the specified distance of each position, their distances from the position, and the direction of the position relative to the features and genes. There is also a column of output listing coordinates of exons that overlap the position. Distance is defined as in `NearestFeature` above.

To run:
```
java -jar sgxtools-[version].jar NearbyFeatures --help
```

Options:
```
  -d, --dist  <arg>       Distance to go out from position
  -g, --gtf  <arg>        GTF2.2 file
  -o, --out  <arg>        Output table
  -p, --pos-list  <arg>   Position file (line format: <id> <chr> <pos>
      --help              Show help message
```

Explanation:
- GTF2.2 file: file of features in [GTF2.2](http://mblab.wustl.edu/GTF22.html) format.
- Position file: file of genomic positions. Each line has format: \<id> \<chr> \<pos>
- Distance: distance to look in either direction from each position
- Output table: file to write output to

### BamMergeBlockOverlappers

`BamMergeBlockOverlappers` generates a single merged feature from the union of all records in a bam file that overlap all of a given list of intervals. In other words, it iterates through the bam file and keeps each record such that the record simultaneously overlaps all the given intervals. Then it takes the union of all kept records and writes the single merged record to a BED file.

To run:
```
java -jar sgxtools-[version].jar BamMergeBlockOverlappers --help
```

Options:
```
  -b, --bam  <arg>         Bam file
  -f, --fpstrand  <arg>    First of pair orientation with respect to transcript
                           (+, -, unstranded)
  -i, --intervals  <arg>   Intervals e.g. chr5:1000-2000:+ separated by ','
  -o, --out  <arg>         Output BED file
      --help               Show help message
```

Explanation:
- Bam file: Bam file
- First of pair orientation with respect to transcript: if strand specific, if first of pair always maps to transcript strand, this is '+'. If first of pair always maps to opposite of transcript strand, this is '-'. If not strand specific, 'unstranded'.
- Intervals: Comma-separated list of intervals, e.g. "chr5:1000-2000:+,chr5:2100-2200:+"
- Output BED file: BED file to write the single merged record to

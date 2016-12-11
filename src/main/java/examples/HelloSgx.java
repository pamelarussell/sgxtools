package examples;

import feature.*;
import java.util.Arrays;
import scala.collection.JavaConverters;

// Example usage of sgxlib from Java 8
public class HelloSgx {

    public static void main(String[] args) {

        // Create two Blocks on chr1
        Block blk1 = new Block("chr1", 1000, 2000, Plus$.MODULE$);
        Block blk2 = new Block("chr1", 3000, 4000, Plus$.MODULE$);
        System.out.println("The Blocks are " + blk1 + " and " + blk2);

        // Create a Region consisting of the two blocks
        scala.collection.immutable.List<Block> bss =
                JavaConverters.asScalaBufferConverter(
                Arrays.asList(blk1, blk2))
                .asScala()
                .toList();
        BlockSet bs = new BlockSet(bss);
        System.out.println("The BlockSet is " + bs);

        // Create an mRNA from the Region
        scala.Option<String> noName = scala.Option.apply(null);
        scala.Option<String> geneName = scala.Option.apply("gene");
        MessengerRNA mrna = new MessengerRNA(bs, 1500, 3400, noName, geneName);
        System.out.println("The MessengerRNA is " + mrna);

        // Get the CDS
        Region cds = mrna.getCDS();
        System.out.println("The CDS is " + cds);

    }

}

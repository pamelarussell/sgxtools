package commandline.program

import commandline.CommandLineProgram
import feature.{Block, Unstranded}

object HelloWorld extends CommandLineProgram {

  println("Hello " + args(0))

  val block = Block("chr1", 500, 1000, Unstranded)
  println(s"This is a block from sgxlib:\t$block")

}

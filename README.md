# sgxtools
Command line tools for genomics written in Scala

## Overview

sgxtools provides command line genomics applications using [sgxlib](https://github.com/pamelarussell/sgxlib) under the hood. sgxtools is platform independent and runs on the Java Virtual Machine (JVM).

## Requirements

- Java 8

## Download precompiled .jar file and source code

The easiest way to use sgxtools is to use the precompiled .jar file. Go to [latest release](https://github.com/pamelarussell/sgxtools/releases/latest) for precompiled .jar file and source code downloads.

## Building from source

To build a .jar file from source, [sbt](http://www.scala-sbt.org/) is required.

```
sbt assembly
```

generates `target/scala-[version]/sgxtools-[release].jar`.

## Running sgxtools




package com.augustnagro.utf8;

import org.openjdk.jmh.annotations.*;

import java.io.IOException;

@BenchmarkMode(Mode.Throughput)
@State(Scope.Benchmark)
@Measurement(time = 1, iterations = 5)
@Fork(
  value = 1,
  jvmArgsPrepend = {
    "--enable-preview",
    "--add-modules=jdk.incubator.vector",
  }
)
public class Bench512 {
  private static final LookupTables LUTS = new LookupTables512();

  @Param({"/twitter.json", "/utf8-demo.txt", "/utf8-demo-invalid.txt", "/20k.txt"})
  String testFile;

  byte[] buf;

  @Setup
  public void setup() throws IOException {
    buf = getClass().getResourceAsStream(testFile).readAllBytes();
  }

  @Benchmark
  public boolean vector_512() {
    return Utf8.validate(buf, LUTS);
  }

}

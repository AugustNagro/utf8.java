package com.augustnagro.utf8;

import jdk.incubator.vector.ByteVector;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
public class Bench {

  @Param({"/twitter.json", "/utf8-demo.txt", "/utf8-demo-invalid.txt", "/20k.txt"})
  String testFile;

  byte[] buf;

  @Setup
  public void setup() throws IOException {
    buf = getClass().getResourceAsStream(testFile).readAllBytes();
  }

  @Benchmark
  public String jdk() {
    return new String(buf, StandardCharsets.UTF_8);
  }

  private static final LookupTables LOOKUP_TABLES_128 =
      new LookupTables(ByteVector.SPECIES_128);

  @Benchmark
  public boolean vector_128() {
    return Utf8.validate(buf, ByteVector.SPECIES_128, LOOKUP_TABLES_128);
  }

  private static final LookupTables LOOKUP_TABLES_PREFERRED =
      new LookupTables(ByteVector.SPECIES_PREFERRED);

  @Benchmark
  public boolean vector_preferred() {
    return Utf8.validate(buf, ByteVector.SPECIES_PREFERRED, LOOKUP_TABLES_PREFERRED);
  }
}

package com.augustnagro.utf8;

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
public class BenchJDK {

  private static final LookupTables LUTS_128 = new LookupTables128();
  private static final LookupTables LUTS_256 = new LookupTables256();
  private static final LookupTables LUTS_512 = new LookupTables512();

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


  @Benchmark
  public boolean vector_512() {
    return Utf8.validate(buf, LUTS_512);
  }

  @Benchmark
  public boolean vector_256() {
    return Utf8.validate(buf, LUTS_256);
  }

  @Benchmark
  public boolean vector_128() {
    return Utf8.validate(buf, LUTS_128);
  }

}

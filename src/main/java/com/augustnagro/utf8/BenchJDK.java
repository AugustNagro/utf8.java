package com.augustnagro.utf8;

import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@BenchmarkMode(Mode.Throughput)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 5)
@Measurement(time = 1, iterations = 1)
@Fork(
  value = 1, warmups = 1,
  jvmArgsPrepend = {
    "--enable-preview",
    "--add-modules=jdk.incubator.vector",
  }
)
public class BenchJDK {

  private static final LookupTables LUTS_128 = new LookupTables128();
  private static final LookupTables LUTS_256 = new LookupTables256();
  private static final LookupTables LUTS_512 = new LookupTables512();

  @Param({"/twitter.json"}) // the following could be added to the list: {"/utf8-demo.txt", "/utf8-demo-invalid.txt", "/20k.txt"}
  String testFile;

  byte[] buf;

  @Setup
  public void setup() throws IOException {
    buf = getClass().getResourceAsStream(testFile).readAllBytes();
  }

  @Benchmark
  public boolean jdk() {
    try {
      new String(buf, StandardCharsets.UTF_8);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  @Benchmark
  public boolean scalar() {
    return Utf8.scalarValidUtf8(0, buf);
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

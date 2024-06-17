package com.augustnagro.utf8;

import java.lang.foreign.MemorySegment;
import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.VectorSpecies;
import org.openjdk.jmh.annotations.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Random;

import static jdk.incubator.vector.VectorOperators.*;

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
public class OneBranchTooMany {

  private static final long SEED = 4691838106802987135L;
  private static final VectorSpecies<Byte> SPECIES = ByteVector.SPECIES_128;
  private static final ByteOrder BYTE_ORDER = ByteOrder.nativeOrder();

  @Param({"ZEROS", "TRIVIAL", "HARDER", "HARDEST"})
  String branchPrediction;

  byte[] buf;

  @Setup(Level.Trial)
  public void setup() {
    // 5_000 128 bit vectors
    buf = new byte[640_000];

    // 0/100 chance the bytes of a vector are negative
    if ("TRIVIAL".equals(branchPrediction)) {
      var r = new Random(SEED);
      for (int i = 0; i < buf.length; i += 128) {
        byte b = (byte) r.nextInt(128);
        Arrays.fill(buf, i, i + 128, b);
      }

    // 1/100 chance the bytes of a vector are negative
    // harder to predict than all 0's
    } else if ("HARDER".equals(branchPrediction)) {
      var r = new Random(SEED);
      for (int i = 0; i < buf.length; i += 128) {
        byte b = (byte) r.nextInt(128);
        if (r.nextInt(100) == 0) b = (byte) -b;
        Arrays.fill(buf, i, i + 128, b);
      }

    // 1/2 chance the bytes of a vector are negative
    // hardest / impossible to predict.
    } else if ("HARDEST".equals(branchPrediction)) {
      var r = new Random(SEED);
      for (int i = 0; i < buf.length; i += 128) {
        byte b = (byte) r.nextInt(128);
        if (r.nextBoolean()) b = (byte) -b;
        Arrays.fill(buf, i, i + 128, b);
      }
    }
  }

  @Benchmark
  public boolean scalar() {
    boolean isAllPositive = false;

    for (int i = 0; i < 5_000; i += 128) {
      boolean vectorHasNegatives = false;
      for (int j = 0; j < 128; j++) {
        if (buf[i + j] < 0) vectorHasNegatives = true;
      }
      isAllPositive = !vectorHasNegatives;
    }

    return isAllPositive;
  }

  @Benchmark
  public boolean vectorBranchFree() {
    var res = ByteVector.zero(SPECIES);
    boolean isAllPositive = false;

    int i = 0;
    for (; i < SPECIES.loopBound(buf.length); i += SPECIES.length()) {
      var input = ByteVector.fromArray(SPECIES, buf, i);
      isAllPositive = !input.test(IS_NEGATIVE).anyTrue();
      res = res.lanewise(OR, input);
    }

    return isAllPositive && res.test(IS_DEFAULT).allTrue();
  }

  @Benchmark
  public boolean vectorBranchy() {
    var res = ByteVector.zero(SPECIES);
    boolean isAllPositive = false;

    int i = 0;
    for (; i < SPECIES.loopBound(buf.length); i += SPECIES.length()) {
      var input = ByteVector.fromArray(SPECIES, buf, i);
      isAllPositive = !input.test(IS_NEGATIVE).anyTrue();
      if (isAllPositive) {
        res = res.lanewise(OR, input);
      }
    }

    return isAllPositive && res.test(IS_DEFAULT).allTrue();
  }

}

# utf8.java

Vectorized UTF-8 validation & benchmarks, written in Java. 

Based on the [paper by John Keiser and Daniel Lemire](https://arxiv.org/abs/2010.03090),
with minor modifications.

## Verify Correctness
Make sure to have Java 22 installed. Then execute:

```bash
mvn compile assembly:single && \
java --enable-preview --add-modules jdk.incubator.vector \
-jar target/utf8.java-1.0-SNAPSHOT-jar-with-dependencies.jar [optional list of space-delineated file paths]
```

With no arguments, this will run the UTF-8 validator on 4 source files:
  - twitter.json: 631_515 bytes, frequent multi-byte characters.
    Taken from [here](https://raw.githubusercontent.com/simdjson/simdjson/master/jsonexamples/twitter.json).
  - utf8-demo.txt: 13_459 bytes, many special / tricky utf8 characters. From
    [w3.org](https://www.w3.org/2001/06/utf-8-test/UTF-8-demo.html).
  - utf8-demo-invalid.txt: same as utf8-demo.txt, but with one error.
  - 20k.txt: 3.8 MB, all ascii.

## Running Benchmarks

```bash
mvn verify && java -jar target/benchmarks.jar
```

The JMH benchmarks use the same 4 test files mentioned above, at 3 vector lengths: 128 bit, 256, and 512. Most likely your hardware does not support 512 bit vectors, so these benchmarks fallback to the slow array-based implementation.`jdk_decode` uses the JDK's `new String(buf, UTF_8)`. This constructor produces a new String in addition to validation, but is good enough for a baseline.

## Performance

Throughput for `twitter.json` as of 2024-06-19:

| `new String(buf, UTF_8)` | `Utf8.validate(buf, new LookupTables256())` | `simdjson::validate_utf8(str, len)`         |
|--------------------------|---------------------------------------------|---------------------------------------------|
| .96 GB/sec               | 11.44 GB/sec                                | 24 GB/sec (from paper, not recently tested) |

* The JDK algorithm is very optimized, and uses intrinsics to check negatives (for the ASCII shortcut) and to elide array bound checks.

* In the vectorized algorithm, 256 bit vectors currently perform best. We cannot go smaller than 128 bit, since nibbles (4 bits) are used to select from the lookup tables.

## Conclusion

  - The Vector api is expressive and a pleasure to use. Performance is getting better.
  - Abstracting over ISA and even vector Shape is *incredible* for
    portability, given how fragmented vector instruction sets are.
  - The dissonance between Vector<Byte> and ByteVector is a little annoying.
  - If I had benchmarked iteratively while developing, I could've discovered the causes
    of slowdown sooner.
  - I wish there was a (documented) debug/logging flag.
  - Vector::selectInto is awesome for lookup tables.
  - The project's [JavaDoc](https://docs.oracle.com/en/java/javase/17/docs/api/jdk.incubator.vector/jdk/incubator/vector/package-summary.html) 
    is one of the best introductions to vectorization on the internet.
  - Debugging works great! I do wish we could make Vector::toString print
    hex instead of base 10 by default.
  - Would have been nice if performance was a success story, but failure is educational.
  - I think someone could implement simd-json in Java if they wanted to. Would it be fast? At least not for now.


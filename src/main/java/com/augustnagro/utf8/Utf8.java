package com.augustnagro.utf8;

import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.Vector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorSpecies;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static jdk.incubator.vector.VectorOperators.*;
import static com.augustnagro.utf8.LookupTables.*;

public class Utf8 {

  private static final VectorSpecies<Byte> species = ByteVector.SPECIES_PREFERRED;
  /**
   * Returns true if buf is valid UTF-8.
   */
  public static boolean validate(byte[] buf) {
    /*
    These three vectors are the local state.
    -> error: if ever non-zero, there has been a UTF-8 error.
    -> prevIncomplete: whether the last vector we processed was incomplete,
    like when a 4-byte char started in the last two lanes. This is used for the
    ascii fast path.
    -> prevInputBlock: the last vector processed.
     */
    ByteVector error = ByteVector.zero(species);
    Vector<Byte> prevIncomplete = ByteVector.zero(species);
    ByteVector prevInputBlock = ByteVector.zero(species);

    int i = 0;
    for (; i < species.loopBound(buf.length); i += species.length()) {
      ByteVector input = ByteVector.fromArray(species, buf, i);

      /*
      Short-circuit if the entire vector is Ascii; we know since the MSB of an
      ascii byte is always 0. Java bytes are signed using two's complement,
      so if there is any negative byte found, it cannot be an ascii character.
       */
      boolean isAscii = !input.test(IS_NEGATIVE).anyTrue();
      if (isAscii) {
        // if last vector had an incomplete multi-byte char, put it in error vector.
        error = error.or(prevIncomplete);
      } else {
        error = error.or(testUtf8(input, prevInputBlock));
        /*
        There are three cases we must consider at the end of our vector:
        1. Last byte is like 11______ (starting byte for a 2-byte char)
        2. 2nd to last byte is like 111_____ (starting byte for a 3-byte char)
        3. 3rd to last byte is like 1111____ (starting byte for a 4-byte char)

        For example, if our vector is
        <..., 11100000, 10100100>
        , which ends with the first two bytes of char '¢', then prevIncomplete =
        <0, 0, ..., 11111111, 0>
         */
        prevIncomplete = input.and(isIncompleteAnd).eq(isIncompleteEq).toVector();
        prevInputBlock = input;
      }
    }

    /*
    Final run for the remaining bytes.
     */
    VectorMask<Byte> m = species.indexInRange(i, buf.length);
    ByteVector input = ByteVector.fromArray(species, buf, i, m);
    boolean notAscii = input.test(IS_NEGATIVE).anyTrue();
    if (notAscii) {
      error = error.or(testUtf8(input, prevInputBlock));
      prevIncomplete = input.and(isIncompleteAnd).eq(isIncompleteEq).toVector();
    }

    /*
    Check end of file.. needed for both ascii and utf8 paths, since
    the specialCases in validateUtf8() only checks for bytes
    too large in the first of two bytes.
     */
    error = error.or(prevIncomplete);

    /*
    If no lanes in error are set, the bytes are valid UTF-8.
     */
    return error.test(IS_DEFAULT).allTrue();
  }

  /**
   * Returns the error vector
   */
  private static Vector<Byte> testUtf8(ByteVector input, ByteVector prevInputBlock) {

    /*
    Check multi byte lengths.

    Given
    input = <i_0, i_1, ..., i_n-1>,
    prevInputBlock = <p_0, p_1, ..., p_n-2, p_n-1>,
    it holds that
    prev2 = <p_n-2, p_n-1, i_0, i_1, ..., i_n-3>
     */
    ByteVector prev2 = prevInputBlock.slice(species.length() - 2, input);
    ByteVector prev3 = prevInputBlock.slice(species.length() - 3, input);
      /*
      Here we check each lane if this lane begins a 3 or 4-byte continuation.
      So, we select a lane if it's byte starts with 111_____ or 1111____.
      Keiser & Lemire use saturating subtraction here, which isn't available in the incubator.
      Instead we use bitwise ops (one more instruction)
       */
    VectorMask<Byte> is3ByteCont = prev2.and((byte) 0b11100000).eq((byte) 0b11100000);
    VectorMask<Byte> is4ByteCont = prev3.and((byte) 0b11110000).eq((byte) 0b11110000);
    // we only wish to mark the first bit of a continuation, so AND with 0x80.
    Vector<Byte> markedContinuations =
        is3ByteCont.or(is4ByteCont).toVector().lanewise(AND, (byte) 0x80);

    /*
    Check 2 and 3 byte special cases.
    The paper linked in README does excelent job explaining the lookup tables.
     */
    ByteVector prev1 = prevInputBlock.slice(species.length() - 1, input);
    ByteVector byte1High = prev1.lanewise(LSHR, 4).selectFrom(byte1HighLookup);
    ByteVector byte1Low = prev1.and((byte) 0x0f).selectFrom(byte1LowLookup);
    ByteVector byte2High = input.lanewise(LSHR, 4).selectFrom(byte2HighLookup);
    ByteVector specialCases = byte1High.and(byte1Low).and(byte2High);

    // any markers not 'knocked-out' with XOR represent error.
    return markedContinuations.lanewise(XOR, specialCases);
  }

  public static void main(String[] args) throws IOException {
    byte[] buf;

    if (args.length == 0) {
      String[] resources = {
          "twitter.json",
          "utf8-demo.txt",
          "utf8-demo-invalid.txt",
          "20k.txt"
      };
      for (String resource : resources) {
        buf = Utf8.class.getResourceAsStream("/" + resource).readAllBytes();
        System.out.println(resource + ": " + validate(buf));
      }

    } else {
      for (String path : args) {
        buf = Files.readAllBytes(Path.of(path));
        System.out.println(path + ": " + validate(buf));
      }
    }
  }

}

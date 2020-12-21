package com.augustnagro.utf8;

import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.VectorSpecies;

import java.util.Arrays;

public class LookupTables {
  /*
  Lookup table definitions
   */
  private static final byte TOO_SHORT = 1 << 0;
  private static final byte TOO_LONG = 1 << 1;
  private static final byte OVERLONG_3 = 1 << 2;
  private static final byte SURROGATE = 1 << 4;
  private static final byte OVERLONG_2 = 1 << 5;
  private static final byte TWO_CONTS = (byte) (1 << 7);
  private static final byte TOO_LARGE = 1 << 3;
  private static final byte TOO_LARGE_1000 = 1 << 6;
  private static final byte OVERLONG_4 = 1 << 6;
  private static final byte CARRY = TOO_SHORT | TOO_LONG | TWO_CONTS;

  final ByteVector byte1HighLookup;
  final ByteVector byte1LowLookup;
  final ByteVector byte2HighLookup;
  final ByteVector isIncompleteAnd;
  final ByteVector isIncompleteEq;
  
  LookupTables(VectorSpecies<Byte> species) {
    byte[] byte1HighTable = new byte[species.length()];
    byte1HighTable[0] = TOO_LONG;
    byte1HighTable[1] = TOO_LONG;
    byte1HighTable[2] = TOO_LONG;
    byte1HighTable[3] = TOO_LONG;

    byte1HighTable[4] = TOO_LONG;
    byte1HighTable[5] = TOO_LONG;
    byte1HighTable[6] = TOO_LONG;
    byte1HighTable[7] = TOO_LONG;

    byte1HighTable[8] = TWO_CONTS;
    byte1HighTable[9] = TWO_CONTS;
    byte1HighTable[10] = TWO_CONTS;
    byte1HighTable[11] = TWO_CONTS;

    byte1HighTable[12] = TOO_SHORT | OVERLONG_2;
    byte1HighTable[13] = TOO_SHORT;
    byte1HighTable[14] = TOO_SHORT | OVERLONG_3 | SURROGATE;
    byte1HighTable[15] = TOO_SHORT | TOO_LARGE | TOO_LARGE_1000 | OVERLONG_4;
    byte1HighLookup = ByteVector.fromArray(species, byte1HighTable, 0);


    byte[] byte1LowTable = new byte[species.length()];
    byte1LowTable[0] = CARRY | OVERLONG_3 | OVERLONG_2 | OVERLONG_4;
    byte1LowTable[1] = CARRY | OVERLONG_2;
    byte1LowTable[2] = CARRY;
    byte1LowTable[3] = CARRY;

    byte1LowTable[4] = CARRY | TOO_LARGE;
    byte1LowTable[5] = CARRY | TOO_LARGE | TOO_LARGE_1000;
    byte1LowTable[6] = CARRY | TOO_LARGE | TOO_LARGE_1000;
    byte1LowTable[7] = CARRY | TOO_LARGE | TOO_LARGE_1000;

    byte1LowTable[8] = CARRY | TOO_LARGE | TOO_LARGE_1000;
    byte1LowTable[9] = CARRY | TOO_LARGE | TOO_LARGE_1000;
    byte1LowTable[10] = CARRY | TOO_LARGE | TOO_LARGE_1000;
    byte1LowTable[11] = CARRY | TOO_LARGE | TOO_LARGE_1000;

    byte1LowTable[12] = CARRY | TOO_LARGE | TOO_LARGE_1000;
    byte1LowTable[13] = CARRY | TOO_LARGE | TOO_LARGE_1000 | SURROGATE;
    byte1LowTable[14] = CARRY | TOO_LARGE | TOO_LARGE_1000;
    byte1LowTable[15] = CARRY | TOO_LARGE | TOO_LARGE_1000;
    byte1LowLookup = ByteVector.fromArray(species, byte1LowTable, 0);


    byte[] byte2HighTable = new byte[species.length()];
    byte2HighTable[0] = TOO_SHORT;
    byte2HighTable[1] = TOO_SHORT;
    byte2HighTable[2] = TOO_SHORT;
    byte2HighTable[3] = TOO_SHORT;

    byte2HighTable[4] = TOO_SHORT;
    byte2HighTable[5] = TOO_SHORT;
    byte2HighTable[6] = TOO_SHORT;
    byte2HighTable[7] = TOO_SHORT;

    byte2HighTable[8] = TOO_LONG | OVERLONG_2 | TWO_CONTS | OVERLONG_3 | TOO_LARGE_1000 | OVERLONG_4;
    byte2HighTable[9] = TOO_LONG | OVERLONG_2 | TWO_CONTS | OVERLONG_3 | TOO_LARGE;
    byte2HighTable[10] = TOO_LONG | OVERLONG_2 | TWO_CONTS | SURROGATE | TOO_LARGE;
    byte2HighTable[11] = TOO_LONG | OVERLONG_2 | TWO_CONTS | SURROGATE | TOO_LARGE;

    byte2HighTable[12] = TOO_SHORT;
    byte2HighTable[13] = TOO_SHORT;
    byte2HighTable[14] = TOO_SHORT;
    byte2HighTable[15] = TOO_SHORT;
    byte2HighLookup = ByteVector.fromArray(species, byte2HighTable, 0);


    byte[] incompleteTable = new byte[species.length()];
    Arrays.fill(incompleteTable, 0, incompleteTable.length - 3, (byte) 0);
    incompleteTable[incompleteTable.length - 3] = (byte) 0b11110000;
    incompleteTable[incompleteTable.length - 2] = (byte) 0b11100000;
    incompleteTable[incompleteTable.length - 1] = (byte) 0b11000000;
    isIncompleteAnd = ByteVector.fromArray(species, incompleteTable, 0);

    byte[] incompleteEq = new byte[species.length()];
    Arrays.fill(incompleteEq, 0, incompleteEq.length - 3, Byte.MIN_VALUE);
    incompleteEq[incompleteEq.length - 3] = (byte) 0b11110000;
    incompleteEq[incompleteEq.length - 2] = (byte) 0b11100000;
    incompleteEq[incompleteEq.length - 1] = (byte) 0b11000000;
    isIncompleteEq = ByteVector.fromArray(species, incompleteEq, 0);
  }
}

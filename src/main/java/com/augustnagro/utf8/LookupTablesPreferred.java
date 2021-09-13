package com.augustnagro.utf8;

import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.VectorSpecies;

public final class LookupTablesPreferred implements LookupTables {
  private static final VectorSpecies<Byte> SPECIES = ByteVector.SPECIES_PREFERRED;
  private static final ByteVector BYTE1_HIGH_LOOKUP = Util.buildByte1HighLookup(SPECIES);
  private static final ByteVector BYTE1_LOW_LOOKUP = Util.buildByte1LowLookup(SPECIES);
  private static final ByteVector BYTE2_HIGH_LOOKUP = Util.buildByte2HighLookup(SPECIES);
  private static final ByteVector IS_INCOMPLETE_AND = Util.buildIsIncompleteAnd(SPECIES);
  private static final ByteVector IS_INCOMPLETE_EQ = Util.buildIsIncompleteEq(SPECIES);

  @Override
  public VectorSpecies<Byte> species() {
    return SPECIES;
  }

  @Override
  public ByteVector byte1HighLookup() {
    return BYTE1_HIGH_LOOKUP;
  }

  @Override
  public ByteVector byte1LowLookup() {
    return BYTE1_LOW_LOOKUP;
  }

  @Override
  public ByteVector byte2HighLookup() {
    return BYTE2_HIGH_LOOKUP;
  }

  @Override
  public ByteVector isIncompleteAnd() {
    return IS_INCOMPLETE_AND;
  }

  @Override
  public ByteVector isIncompleteEq() {
    return IS_INCOMPLETE_EQ;
  }
}

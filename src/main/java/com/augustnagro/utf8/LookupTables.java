package com.augustnagro.utf8;

import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.VectorSpecies;

public sealed interface LookupTables permits
  LookupTables128, LookupTables256, LookupTables512, LookupTablesMax, LookupTablesPreferred {

  VectorSpecies<Byte> species();
  ByteVector byte1HighLookup();
  ByteVector byte1LowLookup();
  ByteVector byte2HighLookup();
  ByteVector isIncompleteAnd();
  ByteVector isIncompleteEq();
}

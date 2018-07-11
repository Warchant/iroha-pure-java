package jp.co.soramitsu.iroha.java.detail.mapping;

import iroha.protocol.Primitive;
import java.math.BigInteger;

public class Uint256Mapper {

  private Uint256Mapper() {
  }

  private static final int SHIFT_SIZE = 64;
  private static final BigInteger TWO_COMPL_REF = BigInteger.ONE.shiftLeft(64);

  // https://stackoverflow.com/questions/10886962/interpret-a-negative-number-as-unsigned-with-biginteger-java#10887788
  private static BigInteger makeUint(BigInteger b) {
    if (b.compareTo(BigInteger.ZERO) < 0) {
      b = b.add(TWO_COMPL_REF);
    }
    return b;
  }


  public static BigInteger toDomainValue(Primitive.uint256 uint) {
    BigInteger big = BigInteger.ZERO;

    return big
        .or(makeUint(BigInteger.valueOf(uint.getFirst())))
        .shiftLeft(SHIFT_SIZE)
        .or(makeUint(BigInteger.valueOf(uint.getSecond())))
        .shiftLeft(SHIFT_SIZE)
        .or(makeUint(BigInteger.valueOf(uint.getThird())))
        .shiftLeft(SHIFT_SIZE)
        .or(makeUint(BigInteger.valueOf(uint.getFourth())));

  }

  public static Primitive.uint256 toProtobufValue(BigInteger big) {
    if (big.bitLength() > 256) {
      throw new IllegalArgumentException("BigInteger does not fit into uint256");
    }

    if (big.signum() < 0) {
      throw new IllegalArgumentException("BigInteger must be positive");
    }

    long fourth = big.longValue();
    big = big.shiftRight(SHIFT_SIZE);

    long third = big.longValue();
    big = big.shiftRight(SHIFT_SIZE);

    long second = big.longValue();
    big = big.shiftRight(SHIFT_SIZE);

    long first = big.longValue();

    return Primitive.uint256.newBuilder()
        .setFirst(first)
        .setSecond(second)
        .setThird(third)
        .setFourth(fourth)
        .build();
  }
}

package jp.co.soramitsu.iroha.java.detail.mapping;

import static org.junit.Assert.*;

import iroha.protocol.Primitive;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.Test;
import org.junit.experimental.categories.Categories.IncludeCategory;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;


@RunWith(Parameterized.class)
public class Uint256MapperTest {

  private final BigInteger inputBig;

  public Uint256MapperTest(BigInteger input) {
    this.inputBig = input;
  }

  @Parameterized.Parameters
  public static List<BigInteger> bigIntegers() {
    final int AMOUNT = 256;

    List<BigInteger> list = new ArrayList<>(AMOUNT);
    Random rnd = new SecureRandom();

    for (int bits = 0; bits < AMOUNT; bits++) {
      list.add(new BigInteger(bits, rnd));
    }

    return list;
  }


  @Test
  public void givenDomainWhenMapToProtoAndMapToDomainThenEqual() {
    // given inputBig

    // when
    Primitive.uint256 proto = Uint256Mapper.toProtobufValue(inputBig);
    BigInteger newBig = Uint256Mapper.toDomainValue(proto);

    // then
    assertEquals(inputBig, newBig);
  }

  @Test(expected = IllegalArgumentException.class)
  public void givenTooBigBigIntWhenMapToProtoAndMapToDomainThenThrows(){
    // given
    Random rnd = new SecureRandom();
    // between 2**256 + 1 and 2**512 + 1
    BigInteger tooBig = new BigInteger(256, rnd).add(BigInteger.ONE).shiftLeft(256);

    // when
    Primitive.uint256 proto = Uint256Mapper.toProtobufValue(tooBig);
    BigInteger newBig = Uint256Mapper.toDomainValue(proto);

    // then throws
  }

}

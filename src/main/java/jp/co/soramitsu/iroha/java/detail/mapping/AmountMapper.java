package jp.co.soramitsu.iroha.java.detail.mapping;

import iroha.protocol.Primitive.Amount;
import iroha.protocol.Primitive.uint256;
import java.math.BigDecimal;
import java.math.BigInteger;

public class AmountMapper {

  private AmountMapper() {
  }

  public static BigDecimal toDomainValue(Amount instance) {
    int precision = instance.getPrecision();
    uint256 value = instance.getValue();
    BigInteger big = Uint256Mapper.toDomainValue(value);

    return new BigDecimal(big, precision);
  }

  public static Amount toProtobufValue(BigDecimal instance) {
    int precision = instance.scale();
    uint256 value = Uint256Mapper.toProtobufValue(instance.unscaledValue());

    return Amount.newBuilder()
        .setValue(value)
        .setPrecision(precision)
        .build();
  }

  public static Amount toProtobufValue(String instance) {
    return toProtobufValue(new BigDecimal(instance));
  }
}

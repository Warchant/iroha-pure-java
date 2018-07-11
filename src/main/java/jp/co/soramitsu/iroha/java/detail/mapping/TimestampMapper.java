package jp.co.soramitsu.iroha.java.detail.mapping;

import java.time.Instant;

public class TimestampMapper {

  private TimestampMapper() {
  }

  public static Instant toDomainValue(Long instance) {
    return Instant.ofEpochMilli(instance);
  }

  public static Long toProtobufValue(Instant instance) {
    return instance.toEpochMilli();
  }
}

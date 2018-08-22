package jp.co.soramitsu.iroha.java;

import static jp.co.soramitsu.iroha.java.ValidationException.Type.AMOUNT;
import static jp.co.soramitsu.iroha.java.ValidationException.Type.QUORUM;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.NonNull;

public class FieldValidator {

  public void checkAmount(@NonNull BigDecimal amount) {
    if (amount.signum() < 0) {
      throw new ValidationException(AMOUNT, "BigInteger must be positive");
    }

    if (amount.unscaledValue().bitLength() > 256) {
      throw new ValidationException(AMOUNT, "BigInteger does not fit into uint256");
    }
  }

  public void checkAccount(@NonNull String account) {
  }

  public void checkDomain(@NonNull String domain) {
  }

  public void checkAccountId(@NonNull String accountId) {
  }

  public void checkDomainId(@NonNull String domainId) {
  }

  public void checkTimestamp(@NonNull Instant time) {
  }

  public void checkQuorum(int quorum) {
    if (quorum < 1) {
      throw new ValidationException(QUORUM, "Quorum must be positive");
    }
  }

  public void checkAssetId(@NonNull String assetId) {
  }

  public void checkAccountDetailsKey(@NonNull String key) {
  }

  public void checkAccountDetailsValue(@NonNull String value) {
  }
}

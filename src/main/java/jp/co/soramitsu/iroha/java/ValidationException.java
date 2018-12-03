package jp.co.soramitsu.iroha.java;

import lombok.Getter;

@Getter
public class ValidationException extends IllegalArgumentException {

  public ValidationException(Type type, String format, Object... args) {
    super(
        String.format("%s: %s", type.toString(), String.format(format, args))
    );
    this.type = type;
  }

  private Type type;


  public enum Type {
    AMOUNT,
    ACCOUNT,
    ACCOUNT_ID,
    PUBKEY,
    PEER_ADDRESS,
    QUORUM,
    PRECISION,
    ROLE_NAME,
    DETAILS_KEY
  }
}

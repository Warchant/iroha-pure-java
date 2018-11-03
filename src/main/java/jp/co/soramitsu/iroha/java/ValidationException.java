package jp.co.soramitsu.iroha.java;

import lombok.Getter;

public class ValidationException extends IllegalArgumentException {

  public ValidationException(Type type, String msg) {
    super(
        String.format("%s: %s", type.toString(), msg)
    );
  }


  @Getter
  public enum Type {
    AMOUNT("amount"),
    ACCOUNT("account"),
    ACCOUNT_ID("account_id"),
    PUBKEY("public_key"),
    PEER_ADDRESS("peer_address"),
    QUORUM("quorum"),
    ROLE_NAME("role_name");


    private String type;

    Type(String s) {
      this.type = s;
    }
  }
}

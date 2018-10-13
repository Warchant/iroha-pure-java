package jp.co.soramitsu.iroha.java;

import static jp.co.soramitsu.iroha.java.ValidationException.Type.AMOUNT;
import static jp.co.soramitsu.iroha.java.ValidationException.Type.PEER_ADDRESS;
import static jp.co.soramitsu.iroha.java.ValidationException.Type.PUBKEY;
import static jp.co.soramitsu.iroha.java.ValidationException.Type.QUORUM;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
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
    // TODO
  }

  public void checkDomain(@NonNull String domain) {
    // TODO
  }

  public void checkAccountId(@NonNull String accountId) {
    // TODO
  }

  public void checkDomainId(@NonNull String domainId) {
    // TODO
  }

  public void checkTimestamp(@NonNull Instant time) {
    // TODO
  }

  public void checkQuorum(int quorum) {
    if (quorum < 1) {
      throw new ValidationException(QUORUM, "Quorum must be positive");
    }
  }

  public void checkAssetId(@NonNull String assetId) {
    // TODO
  }

  public void checkAccountDetailsKey(@NonNull String key) {
    // TODO
  }

  public void checkAccountDetailsValue(@NonNull String value) {
    // TODO
  }

  public void checkPeerAddress(String address) {
    try {
      URI uri = new URI(address);
    } catch (URISyntaxException e) {
      throw new ValidationException(PEER_ADDRESS, "Invalid format in peer address");
    }
  }

  public void checkPublicKey(byte[] peerKey) {
    if (peerKey.length != 32) {
      throw new ValidationException(PUBKEY, "Public key must be 32 bytes length");
    }
  }
}

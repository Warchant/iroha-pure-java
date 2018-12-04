package jp.co.soramitsu.iroha.java;

import static jp.co.soramitsu.iroha.java.ValidationException.Type.ACCOUNT_ID;
import static jp.co.soramitsu.iroha.java.ValidationException.Type.ACCOUNT_NAME;
import static jp.co.soramitsu.iroha.java.ValidationException.Type.AMOUNT;
import static jp.co.soramitsu.iroha.java.ValidationException.Type.ASSET_ID;
import static jp.co.soramitsu.iroha.java.ValidationException.Type.ASSET_NAME;
import static jp.co.soramitsu.iroha.java.ValidationException.Type.DETAILS_KEY;
import static jp.co.soramitsu.iroha.java.ValidationException.Type.DETAILS_VALUE;
import static jp.co.soramitsu.iroha.java.ValidationException.Type.DOMAIN;
import static jp.co.soramitsu.iroha.java.ValidationException.Type.PEER_ADDRESS;
import static jp.co.soramitsu.iroha.java.ValidationException.Type.PRECISION;
import static jp.co.soramitsu.iroha.java.ValidationException.Type.PUBKEY;
import static jp.co.soramitsu.iroha.java.ValidationException.Type.QUORUM;
import static jp.co.soramitsu.iroha.java.ValidationException.Type.ROLE_NAME;
import static jp.co.soramitsu.iroha.java.ValidationException.Type.TIMESTAMP;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.NonNull;
import lombok.val;

public class FieldValidator {

  public void checkAmount(@NonNull String amount) {
    BigDecimal am;

    try {
      am = new BigDecimal(amount);
    } catch (Exception e) {
      throw new ValidationException(AMOUNT, e.toString());
    }

    if (am.signum() < 0) {
      throw new ValidationException(AMOUNT, "BigInteger must be positive");
    }

    if (am.unscaledValue().bitLength() > 256) {
      throw new ValidationException(AMOUNT, "BigInteger does not fit into uint256");
    }
  }

  private static final Pattern accountPattern = Pattern.compile("[a-z_0-9]{1,32}");

  public void checkAccount(@NonNull String account) {
    val m = accountPattern.matcher(account);
    if (!m.matches()) {
      throw new ValidationException(
          ACCOUNT_NAME,
          "Invalid account. Expected '%s', got '%s'",
          accountPattern.pattern(),
          account
      );
    }
  }

  public void checkDomain(@NonNull String domain) {
    try {
      URI uri = new URI(null, null, domain, 0, null, null, null);
    } catch (URISyntaxException e) {
      throw new ValidationException(DOMAIN, "Domain name is invalid: '%s'", domain);
    }
  }

  private static final String accountDomainSplitToken = "@";

  public void checkAccountId(@NonNull String accountId) {
    val t = accountId.split(accountDomainSplitToken);
    if (t.length != 2) {
      throw new ValidationException(ACCOUNT_ID, "Valid format is account@domain, got '%s'",
          accountId);
    }

    try {
      this.checkAccount(t[0]);
      this.checkDomain(t[1]);
    } catch (ValidationException e) {
      throw new ValidationException(
          ACCOUNT_ID,
          "Valid format is account@domain, got '%s'. Details: '%s'.",
          accountId,
          e.getMessage()
      );
    }

  }

  public void checkQuorum(int quorum) {
    if (quorum < 1) {
      throw new ValidationException(QUORUM, "Quorum must be positive");
    }
  }

  private static final String assetDomainSplitToken = "#";

  public void checkAssetId(@NonNull String assetId) {
    val t = assetId.split(assetDomainSplitToken);
    if (t.length != 2) {
      throw new ValidationException(
          ASSET_ID,
          "Valid format is asset#domain, got '%s'",
          assetId);
    }

    try {
      this.checkAssetName(t[0]);
      this.checkDomain(t[1]);
    } catch (ValidationException e) {
      throw new ValidationException(
          ASSET_ID,
          "Valid format is asset#domain, got '%s'. Details: '%s'.",
          assetId,
          e.getMessage()
      );
    }
  }

  private static final Pattern accountDetailsKeyPattern = Pattern.compile("[A-Za-z0-9_]{1,64}");

  public void checkAccountDetailsKey(@NonNull String key) {
    val m = accountDetailsKeyPattern.matcher(key);
    if (!m.matches()) {
      throw new ValidationException(
          DETAILS_KEY,
          "Invalid key. Expected '%s', got '%s'",
          accountDetailsKeyPattern.pattern(),
          key
      );
    }
  }

  public void checkAccountDetailsValue(@NonNull String value) {
    int len = 4096;
    if (!(value.length() <= len)) {
      throw new ValidationException(DETAILS_VALUE,
          "Invalid details value, exceeded maximum length in '%d'. Got '%d'", len, value.length());
    }
  }

  public void checkPeerAddress(@NonNull String address) {
    val m = String.format("Expected ip:port, got '%s'", address);
    val t = address.split(":");
    if (t.length != 2) {
      throw new ValidationException(PEER_ADDRESS, m);
    }

    try {
      String host = t[0];
      int port = Integer.parseInt(t[1]);
      URI uri = new URI(null, null, host, port, null, null, null);
    } catch (Exception e) {
      throw new ValidationException(PEER_ADDRESS, "%s. %s.", m, e.toString());
    }
  }

  public void checkPublicKey(@NonNull byte[] peerKey) {
    if (peerKey.length != 32) {
      throw new ValidationException(PUBKEY, "Public key must be 32 bytes length, got '%d'",
          peerKey.length);
    }
  }


  private static Pattern roleNamePattern = Pattern.compile("[a-z_0-9]{1,32}");

  public void checkRoleName(@NonNull String roleName) {
    Matcher m = roleNamePattern.matcher(roleName);
    if (!m.find()) {
      throw new ValidationException(ROLE_NAME,
          "Role name is invalid, should match: '%s'", roleNamePattern.pattern());
    }
  }

  private static final Pattern assetNamePattern = Pattern.compile("[a-z_0-9]{1,32}");

  public void checkAssetName(@NonNull String assetName) {
    val m = assetNamePattern.matcher(assetName);
    if (!m.matches()) {
      throw new ValidationException(ASSET_NAME,
          "Invalid asset name. Expected '%s', got '%s'",
          assetNamePattern.pattern(),
          assetName);
    }
  }

  public void checkPrecision(@NonNull Integer precision) {
    if (precision < 0 || precision > 255) {
      throw new ValidationException(PRECISION,
          String.format("Invalid precision: '%d'. Should be 0<=precision<=255", precision));
    }
  }

  public void checkTimestamp(@NonNull Long time) {
    if (time < 0) {
      throw new ValidationException(TIMESTAMP, "Time must be positive");
    }
  }
}

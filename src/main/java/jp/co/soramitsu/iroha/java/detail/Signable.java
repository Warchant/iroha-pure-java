package jp.co.soramitsu.iroha.java.detail;

import java.security.KeyPair;
import jp.co.soramitsu.iroha.java.detail.Ed25519Sha3.CryptoException;

public interface Signable extends ProtobufBuildable {
  Signable sign(KeyPair keyPair) throws CryptoException;
}

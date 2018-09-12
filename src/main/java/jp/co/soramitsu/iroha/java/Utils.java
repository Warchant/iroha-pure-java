package jp.co.soramitsu.iroha.java;

import static javax.xml.bind.DatatypeConverter.parseHexBinary;
import static jp.co.soramitsu.crypto.ed25519.Ed25519Sha3.privateKeyFromBytes;
import static jp.co.soramitsu.crypto.ed25519.Ed25519Sha3.publicKeyFromBytes;

import java.security.KeyPair;

public class Utils {

  public static KeyPair keyPair(String hexPublicKey, String hexPrivateKey) {
    return new KeyPair(
        publicKeyFromBytes(parseHexBinary(hexPublicKey)),
        privateKeyFromBytes(parseHexBinary(hexPrivateKey)));
  }
}

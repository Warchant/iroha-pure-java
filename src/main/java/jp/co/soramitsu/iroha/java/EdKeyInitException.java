package jp.co.soramitsu.iroha.java;

public class EdKeyInitException extends IllegalArgumentException {
  EdKeyInitException(String format, int size){
    super("ed25519 " + format + " public key should be " + size + "  bytes long");
  }
}

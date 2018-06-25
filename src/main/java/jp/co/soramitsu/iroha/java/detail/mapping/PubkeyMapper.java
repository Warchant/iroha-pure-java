package jp.co.soramitsu.iroha.java.detail.mapping;

import com.google.protobuf.ByteString;
import java.security.PublicKey;
import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3;
import jp.co.soramitsu.crypto.ed25519.spec.EdDSANamedCurveTable;
import jp.co.soramitsu.crypto.ed25519.spec.EdDSAParameterSpec;

public class PubkeyMapper {

  private static final EdDSAParameterSpec spec = EdDSANamedCurveTable
      .getByName(EdDSANamedCurveTable.ED_25519);

  public static PublicKey toDomainValue(ByteString instance) {
    return Ed25519Sha3.publicKeyFromBytes(instance.toByteArray());
  }

  public static ByteString toProtobufValue(PublicKey instance) {
    return ByteString.copyFrom(Ed25519Sha3.publicKeyToBytes(instance));
  }
}

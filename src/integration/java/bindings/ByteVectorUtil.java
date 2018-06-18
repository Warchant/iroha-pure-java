package bindings;

import jp.co.soramitsu.iroha.Blob;
import jp.co.soramitsu.iroha.ByteVector;

public class ByteVectorUtil {
  public static Blob bytes2Blob(byte[] b) {
    ByteVector bv = new ByteVector(b.length);
    for (int i = 0; i < b.length; i++) {
      bv.set(i, b[i]);
    }

    return new Blob(bv);
  }

  public static byte[] byteVector2bytes(ByteVector blob) {
    byte[] array = new byte[(int) blob.size()];
    for (int i = 0; i < array.length; i++) {
      array[i] = (byte) blob.get(i);
    }

    return array;
  }
}

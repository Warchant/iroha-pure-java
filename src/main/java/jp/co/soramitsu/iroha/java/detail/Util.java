package jp.co.soramitsu.iroha.java.detail;

public class Util {

  // this method is here only because some old versions of Android do not have Objects.nonNull
  public static boolean nonNull(Object obj) {
    return obj != null;
  }
}

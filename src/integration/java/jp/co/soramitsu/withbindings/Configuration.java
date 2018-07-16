package jp.co.soramitsu.withbindings;

public class Configuration {

  private Configuration() {
  }

  public static void init() {
    try {
      System.loadLibrary("irohajava");
    } catch (UnsatisfiedLinkError e) {
      System.err.println("Native code library failed to load. \n" + e);
      System.err.println(System.getProperty("java.library.path"));
      System.exit(1);
    }
  }

}

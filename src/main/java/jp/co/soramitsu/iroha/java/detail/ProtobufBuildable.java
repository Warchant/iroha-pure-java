package jp.co.soramitsu.iroha.java.detail;

import iroha.protocol.BlockOuterClass;

public interface ProtobufBuildable {
  BlockOuterClass.Transaction build();
}

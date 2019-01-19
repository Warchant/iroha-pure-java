package jp.co.soramitsu.iroha.java.subscription;

import io.reactivex.Observable;
import iroha.protocol.Endpoint.ToriiResponse;
import jp.co.soramitsu.iroha.java.IrohaAPI;

/**
 * Wait until observable calls onComplete
 */
public class WaitUntilCompleted implements SubscriptionStrategy {

  @Override
  public Observable<ToriiResponse> subscribe(IrohaAPI api, byte[] txhash) {
    return api.txStatus(txhash);
  }
}

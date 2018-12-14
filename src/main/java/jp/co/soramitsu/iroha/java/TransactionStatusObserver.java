package jp.co.soramitsu.iroha.java;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import iroha.protocol.Endpoint.ToriiResponse;
import jp.co.soramitsu.iroha.java.detail.InlineTransactionStatusObserver;
import jp.co.soramitsu.iroha.java.detail.InlineTransactionStatusObserver.InlineTransactionStatusObserverBuilder;
import jp.co.soramitsu.iroha.java.detail.TransactionStatusObserverFace;

/**
 * Subscriber for Iroha Transaction Statuses.
 *
 * Iroha returns status stream, which can be read by any subscriber as onNext -> onComplete. To
 * enhance mobile UX, this class extends transaction status lifecycle with couple non standard
 * statuses.
 *
 * <p>
 * 1. When transaction is valid and can be committed:
 * <ol>
 * <li>onTransactionSent</li>
 * <li>onStatelessValidationSuccess</li>
 * <li>onStatefulValidationSuccess</li>
 * <li>onTransactionCommitted</li>
 * <li>onComplete</li>
 * </ol>
 * </p>
 *
 * <p>
 * 2. When transaction is invalid:
 * <ol>
 * <li>onTransactionFailed - transaction did not pass stateful or stateless validation</li>
 * <li>onNotReceived - peer does not know about current transaction</li>
 * <li>onMstExpired - any Multi Signature Transaction failure</li>
 * <li>onUnrecognizedStatus - iroha returns some unrecognized status, most probably bug</li>
 * </ol>
 * </p>
 *
 * @see io.reactivex.Observer
 */
public abstract class TransactionStatusObserver implements Observer<ToriiResponse>,
    TransactionStatusObserverFace {

  /**
   * Main router, which parses standard status stream and routes statuses over current transaction
   * lifecycle.
   */
  @Override
  public final void onNext(ToriiResponse t) {
    switch (t.getTxStatus()) {
      case STATELESS_VALIDATION_FAILED:
      case STATEFUL_VALIDATION_FAILED:
        this.onTransactionFailed(t);
        break;
      case STATELESS_VALIDATION_SUCCESS:
        this.onStatelessValidationSuccess(t);
        break;
      case STATEFUL_VALIDATION_SUCCESS:
        this.onStatefulValidationSuccess(t);
        break;
      case COMMITTED:
        this.onTransactionCommited(t);
        break;
      case MST_EXPIRED:
        this.onMstExpired(t);
        break;
      case MST_PENDING:
        this.onMstPending(t);
        break;
      case ENOUGH_SIGNATURES_COLLECTED:
        this.onEnoughSignaturesCollected(t);
        break;
      case NOT_RECEIVED:
        this.onNotReceived(t);
        break;
      case REJECTED:
        this.onRejected(t);
        break;
      default:
        this.onUnrecognizedStatus(t);
        break;
    }
  }

  /**
   * Helper to implement onTransactionSent. Can be overridden by clients who need {@link
   * Disposable}
   */
  @Override
  public void onSubscribe(Disposable d) {
    this.onTransactionSent();
  }

  /**
   * Builder can be used to conveniently build inline observer. If handler is not specified, then
   * default (do-nothing) handler is used.
   */
  public static InlineTransactionStatusObserverBuilder builder() {
    return InlineTransactionStatusObserver.builder();
  }
}

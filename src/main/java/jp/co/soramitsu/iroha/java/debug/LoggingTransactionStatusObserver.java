package jp.co.soramitsu.iroha.java.debug;

import iroha.protocol.Endpoint.ToriiResponse;
import java.util.function.Consumer;
import jp.co.soramitsu.iroha.java.TransactionStatusObserver;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;


/**
 * Logging observer, useful for debugging or logging.
 *
 * Just pass lambda, which will log string line to the constructor.
 */
@EqualsAndHashCode(callSuper = true)
@Value
@AllArgsConstructor
public class LoggingTransactionStatusObserver extends TransactionStatusObserver {

  @NonNull
  private Consumer<String> logger;

  public LoggingTransactionStatusObserver() {
    this.logger = System.out::println;
  }

  @Override
  public void onTransactionSent() {
    logger.accept("onTransactionSent");
  }

  @Override
  public void onTransactionFailed(ToriiResponse t) {
    logger.accept("onTransactionFailed: " + t);
  }

  @Override
  public void onStatelessValidationSuccess(ToriiResponse t) {
    logger.accept("onStatelessValidationSuccess: " + t);
  }

  @Override
  public void onStatefulValidationSuccess(ToriiResponse t) {
    logger.accept("onStatefulValidationSuccess: " + t);
  }

  @Override
  public void onTransactionCommited(ToriiResponse t) {
    logger.accept("onTransactionCommited: " + t);
  }

  @Override
  public void onNotReceived(ToriiResponse t) {
    logger.accept("onNotReceived: " + t);

  }

  @Override
  public void onMstFailed(ToriiResponse t) {
    logger.accept("onMstFailed: " + t);

  }

  @Override
  public void onUnrecognizedStatus(ToriiResponse t) {
    logger.accept("onUnrecognizedStatus: " + t);
  }

  @Override
  public void onError(Throwable e) {
    logger.accept("onError: " + e);
  }

  @Override
  public void onComplete() {
    logger.accept("onComplete");
  }
}

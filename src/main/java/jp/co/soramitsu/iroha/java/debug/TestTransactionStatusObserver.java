package jp.co.soramitsu.iroha.java.debug;

import io.reactivex.observers.TestObserver;
import iroha.protocol.Endpoint.ToriiResponse;
import java.util.concurrent.atomic.AtomicInteger;
import jp.co.soramitsu.iroha.java.detail.TransactionStatusObserverFace;

public class TestTransactionStatusObserver extends TestObserver<ToriiResponse> implements
    TransactionStatusObserverFace {

  private RuntimeException fail(String format, Object... args) {
    return new RuntimeException(String.format(format, args));
  }

  private AtomicInteger sent = new AtomicInteger();
  private AtomicInteger committed = new AtomicInteger();
  private AtomicInteger failed = new AtomicInteger();

  public TestTransactionStatusObserver assertNTransactionSent(int n) {
    if (sent.get() != n) {
      throw fail("assertNTransactionSent: sent %d, expected %d", sent.get(), n);
    }

    return this;
  }

  public TestTransactionStatusObserver assertNTransactionsCommitted(int n) {
    if (committed.get() != n) {
      throw fail("assertNTransactionsCommitted: committed %d, expected %d", committed.get(), n);
    }

    return this;
  }

  public TestTransactionStatusObserver assertNTransactionsFailed(int n) {
    if (failed.get() != n) {
      throw fail("assertNTransactionsFailed: failed %d, expected %d", failed.get(), n);
    }

    return this;
  }

  public TestTransactionStatusObserver assertNoTransactionFailed() {
    return assertNTransactionsFailed(0);
  }

  public TestTransactionStatusObserver assertNoTransactionCommitted() {
    if (sent.get() == 0) {
      throw fail("No transactions have been sent");
    }
    return assertNTransactionsCommitted(0);
  }

  public TestTransactionStatusObserver assertAllTransactionsFailed() {
    if (sent.get() == 0) {
      throw fail("No transactions have been sent");
    }
    return assertNTransactionsFailed(sent.get());
  }

  public TestTransactionStatusObserver assertAllTransactionsCommitted() {
    if (sent.get() == 0) {
      throw fail("No transactions have been sent");
    }
    return assertNTransactionsCommitted(sent.get());
  }


  @Override
  public void onTransactionSent() {
    sent.incrementAndGet();
  }

  @Override
  public void onTransactionFailed(ToriiResponse t) {
    failed.incrementAndGet();
  }

  @Override
  public void onTransactionCommited(ToriiResponse t) {
    committed.incrementAndGet();
  }
}

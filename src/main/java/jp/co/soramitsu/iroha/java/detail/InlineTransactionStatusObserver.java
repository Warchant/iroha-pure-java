package jp.co.soramitsu.iroha.java.detail;

import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.internal.functions.Functions;
import iroha.protocol.Endpoint.ToriiResponse;
import jp.co.soramitsu.iroha.java.TransactionStatusObserver;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InlineTransactionStatusObserver extends TransactionStatusObserver {

  @Default
  private Action onTransactionSent = Functions.EMPTY_ACTION;

  @Default
  private Action onComplete = Functions.EMPTY_ACTION;

  @Default
  private Consumer<? super ToriiResponse> onTransactionFailed = Functions.emptyConsumer();

  @Default
  private Consumer<? super ToriiResponse> onTransactionCommited = Functions.emptyConsumer();

  @Default
  private Consumer<? super ToriiResponse> onStatelessValidationSuccess = Functions.emptyConsumer();

  @Default
  private Consumer<? super ToriiResponse> onStatefulValidationSuccess = Functions.emptyConsumer();

  @Default
  private Consumer<? super ToriiResponse> onNotReceived = Functions.emptyConsumer();

  @Default
  private Consumer<? super ToriiResponse> onUnrecognizedStatus = Functions.emptyConsumer();

  @Default
  private Consumer<? super ToriiResponse> onMstFailed = Functions.emptyConsumer();

  @Default
  private Consumer<? super Throwable> onError = Functions.emptyConsumer();

  @Override
  public void onTransactionSent() {
    try {
      this.onTransactionSent.run();
    } catch (Exception e) {
      Exceptions.propagate(e);
    }
  }

  @Override
  public void onTransactionFailed(ToriiResponse t) {
    try {
      this.onTransactionFailed.accept(t);
    } catch (Exception e) {
      Exceptions.propagate(e);
    }
  }

  @Override
  public void onStatelessValidationSuccess(ToriiResponse t) {
    try {
      this.onStatelessValidationSuccess.accept(t);
    } catch (Exception e) {
      Exceptions.propagate(e);
    }
  }

  @Override
  public void onStatefulValidationSuccess(ToriiResponse t) {
    try {
      this.onStatefulValidationSuccess.accept(t);
    } catch (Exception e) {
      Exceptions.propagate(e);
    }
  }

  @Override
  public void onTransactionCommited(ToriiResponse t) {
    try {
      this.onTransactionCommited.accept(t);
    } catch (Exception e) {
      Exceptions.propagate(e);
    }
  }

  @Override
  public void onNotReceived(ToriiResponse t) {
    try {
      this.onNotReceived.accept(t);
    } catch (Exception e) {
      Exceptions.propagate(e);
    }
  }

  @Override
  public void onMstFailed(ToriiResponse t) {
    try {
      this.onMstFailed.accept(t);
    } catch (Exception e) {
      Exceptions.propagate(e);
    }
  }

  @Override
  public void onUnrecognizedStatus(ToriiResponse t) {
    try {
      this.onUnrecognizedStatus.accept(t);
    } catch (Exception e) {
      Exceptions.propagate(e);
    }
  }

  @Override
  public void onError(Throwable e) {
    try {
      this.onError.accept(e);
    } catch (Exception e1) {
      Exceptions.propagate(e);
    }
  }

  @Override
  public void onComplete() {
    try {
      this.onComplete.run();
    } catch (Exception e) {
      Exceptions.propagate(e);
    }
  }
}

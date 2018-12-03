package jp.co.soramitsu.iroha.java.detail;

import io.grpc.stub.StreamObserver;
import io.reactivex.Emitter;
import lombok.Value;

@Value
public class StreamObserverToSubject<T> implements StreamObserver<T> {

  private Emitter<T> emitter;

  @Override
  public void onNext(T value) {
    emitter.onNext(value);
  }

  @Override
  public void onError(Throwable t) {
    emitter.onError(t);
  }

  @Override
  public void onCompleted() {
    emitter.onComplete();
  }
}

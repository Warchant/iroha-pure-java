package jp.co.soramitsu.iroha.java.detail;

import io.grpc.stub.StreamObserver;
import io.reactivex.subjects.Subject;
import lombok.Value;

@Value
public class StreamObserverToSubject<T> implements StreamObserver<T> {

  private Subject<T> subject;

  @Override
  public void onNext(T value) {
    subject.onNext(value);
  }

  @Override
  public void onError(Throwable t) {
    subject.onError(t);
  }

  @Override
  public void onCompleted() {
    subject.onComplete();
  }
}

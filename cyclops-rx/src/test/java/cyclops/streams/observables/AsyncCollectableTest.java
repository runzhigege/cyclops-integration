package cyclops.streams.observables;


import cyclops.companion.rx.Observables;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;


public class AsyncCollectableTest extends CollectableTest {


    public <T> ReactiveSeq<T> of(T... values){

        ReactiveSeq<T> seq = Spouts.<T>async(s->{
            Thread t = new Thread(()-> {
                for (T next : values) {
                    s.onNext(next);
                }
                s.onComplete();
            });
            t.start();
        });

        return Observables.reactiveSeq(Observables.observableFrom(seq));
    }

}

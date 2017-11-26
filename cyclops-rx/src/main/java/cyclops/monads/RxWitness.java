package cyclops.monads;



import com.oath.cyclops.rx.adapter.ObservableAdapter;
import com.oath.cyclops.rx.adapter.ObservableReactiveSeq;
import com.oath.cyclops.types.extensability.FunctionalAdapter;
import rx.Observable;


public interface RxWitness {

    public static <T> Observable<T> observable(AnyM<observable,? extends T> anyM){
        ObservableReactiveSeq<T> obs = anyM.unwrap();
        return obs.getObservable();
    }

    static interface ObservableWitness<W extends RxWitness.ObservableWitness<W>>  extends WitnessType<W> {

    }
    public static enum observable implements ObservableWitness<observable> {
        INSTANCE;

        @Override
        public FunctionalAdapter<observable> adapter() {
            return new ObservableAdapter();
        }

    }

}

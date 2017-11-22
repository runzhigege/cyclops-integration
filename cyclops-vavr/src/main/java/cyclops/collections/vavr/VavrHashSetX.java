package cyclops.collections.vavr;


import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import com.oath.cyclops.data.collections.extensions.CollectionX;
import com.oath.cyclops.data.collections.extensions.lazy.immutable.LazyPSetX;
import com.oath.cyclops.types.Unwrapable;
import com.oath.cyclops.types.foldable.Evaluation;
import com.oath.cyclops.types.persistent.PersistentSet;
import cyclops.collections.immutable.LinkedListX;
import cyclops.collections.immutable.OrderedSetX;
import cyclops.collections.immutable.PersistentSetX;
import cyclops.companion.Monoids;
import cyclops.control.Option;
import cyclops.function.Reducer;
import cyclops.reactive.ReactiveSeq;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;
import cyclops.data.tuple.Tuple2;




import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class VavrHashSetX<T>  implements PersistentSet<T>, Unwrapable {

    public static <T> PersistentSetX<T> listX(ReactiveSeq<T> stream){
        return fromStream(stream);
    }
    public static <T> PersistentSetX<T> copyFromCollection(CollectionX<? extends T> vec) {
        PersistentSetX<T> res = VavrHashSetX.<T>empty()
                .plusAll(vec);
        return res;
    }
    @Override
    public <R> R unwrap() {
        return (R)set;
    }
    /**
     * Create a LazyPSetX from a Stream
     *
     * @param stream to construct a LazyQueueX from
     * @return LazyPSetX
     */
    public static <T> LazyPSetX<T> fromStream(Stream<T> stream) {
        return new LazyPSetX<T>(null, ReactiveSeq.fromStream(stream),toPSet(), Evaluation.LAZY);
    }

    /**
     * Create a LazyPSetX that contains the Integers between start and end
     *
     * @param start
     *            Number of range to start from
     * @param end
     *            Number for range to end at
     * @return Range ListX
     */
    public static LazyPSetX<Integer> range(int start, int end) {
        return fromStream(ReactiveSeq.range(start, end));
    }

    /**
     * Create a LazyPSetX that contains the Longs between start and end
     *
     * @param start
     *            Number of range to start from
     * @param end
     *            Number for range to end at
     * @return Range ListX
     */
    public static LazyPSetX<Long> rangeLong(long start, long end) {
        return fromStream(ReactiveSeq.rangeLong(start, end));
    }

    /**
     * Unfold a function into a ListX
     *
     * <pre>
     * {@code
     *  LazyPSetX.unfold(1,i->i<=6 ? Option.some(Tuple.tuple(i,i+1)) : Option.none());
     *
     * //(1,2,3,4,5)
     *
     * }</pre>
     *
     * @param seed Initial value
     * @param unfolder Iteratively applied function, terminated by an empty Optional
     * @return ListX generated by unfolder function
     */
    public static <U, T> LazyPSetX<T> unfold(U seed, Function<? super U, Option<Tuple2<T, U>>> unfolder) {
        return fromStream(ReactiveSeq.unfold(seed, unfolder));
    }

    /**
     * Generate a LazyPSetX from the provided Supplier up to the provided limit number of times
     *
     * @param limit Max number of elements to generate
     * @param s Supplier to generate ListX elements
     * @return ListX generated from the provided Supplier
     */
    public static <T> LazyPSetX<T> generate(long limit, Supplier<T> s) {

        return fromStream(ReactiveSeq.generate(s)
                                      .limit(limit));
    }

    /**
     * Create a LazyPSetX by iterative application of a function to an initial element up to the supplied limit number of times
     *
     * @param limit Max number of elements to generate
     * @param seed Initial element
     * @param f Iteratively applied to each element to generate the next element
     * @return ListX generated by iterative application
     */
    public static <T> LazyPSetX<T> iterate(long limit, final T seed, final UnaryOperator<T> f) {
        return fromStream(ReactiveSeq.iterate(seed, f)
                                      .limit(limit));
    }


    public static <T> Reducer<PersistentSet<T>,T> toPSet() {
      return Reducer.fromMonoid(Monoids.pcollectionConcat(cyclops.data.HashSet.empty()), a -> VavrHashSetX.singleton(a));
    }

    public static <T> LazyPSetX<T> PSet(Set<T> q) {
        return fromPSet(new VavrHashSetX<>(q), toPSet());
    }
    public static <T> VavrHashSetX<T> emptyPSet(){
        return  new VavrHashSetX<>(HashSet.empty());
    }
    public static <T> LazyPSetX<T> empty(){
        return fromPSet( new VavrHashSetX<>(HashSet.empty()), toPSet());
    }
    private static <T> LazyPSetX<T> fromPSet(PersistentSet<T> ts, Reducer<PersistentSet<T>,T> pSetReducer) {
        return new LazyPSetX<T>(ts,null,pSetReducer, Evaluation.LAZY);
    }
    public static <T> LazyPSetX<T> singleton(T t){
        return fromPSet(new VavrHashSetX<>(HashSet.of(t)), toPSet());
    }
    public static <T> LazyPSetX<T> of(T... t){
        return fromPSet( new VavrHashSetX<>(HashSet.of(t)), toPSet());
    }
    public static <T> LazyPSetX<T> ofAll(Set<T> q) {
        return fromPSet(new VavrHashSetX<>(q), toPSet());
    }
    @SafeVarargs
    public static <T> LazyPSetX<T> PSet(T... elements){
        return  of(elements);
    }
    @Wither
    private final Set<T> set;

    @Override
    public VavrHashSetX<T> plus(T e) {
        return withSet(set.add(e));
    }

  @Override
  public VavrHashSetX<T> plusAll(Iterable<? extends T> list) {
    return withSet(set.addAll(list));
  }

  @Override
  public VavrHashSetX<T> removeValue(T e) {
    return withSet(set.remove(e));
  }

  @Override
  public VavrHashSetX<T> removeAll(Iterable<? extends T> list) {
    return withSet(set.removeAll(list));
  }


  @Override
    public int size() {
        return set.size();
    }

    @Override
    public Iterator<T> iterator() {
        return set.iterator();
    }




}

package cyclops.collections.vavr;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import com.oath.cyclops.data.collections.extensions.CollectionX;
import com.oath.cyclops.data.collections.extensions.lazy.immutable.FoldToList;
import com.oath.cyclops.data.collections.extensions.lazy.immutable.LazyLinkedListX;
import com.oath.cyclops.types.Unwrapable;
import com.oath.cyclops.types.foldable.Evaluation;
import com.oath.cyclops.types.persistent.PersistentList;
import cyclops.collections.immutable.LinkedListX;
import cyclops.collections.immutable.PersistentQueueX;
import cyclops.control.Option;
import cyclops.function.Reducer;
import cyclops.reactive.ReactiveSeq;
import cyclops.data.tuple.Tuple2;
;


import io.vavr.collection.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class VavrListX<T>  implements PersistentList<T>, Unwrapable {
    public static <T> LinkedListX<T> listX(ReactiveSeq<T> stream){
        return fromStream(stream);
    }
    @Override
    public <R> R unwrap() {
        return (R)list;
    }

    static final FoldToList gen = (it,i)-> VavrListX.from(from(it,i));

    public static <T> LinkedListX<T> copyFromCollection(CollectionX<T> vec) {
        List<T> list = from(vec.iterator(),0);
        return from(list);

    }

    private static <E> List<E> from(final Iterator<E> i, int depth) {

        if(!i.hasNext())
            return List.empty();
        E e = i.next();
        return  from(i,depth++).prepend(e);
    }
    /**
     * Create a LazyLinkedListX from a Stream
     *
     * @param stream to construct a LazyQueueX from
     * @return LazyLinkedListX
     */
    public static <T> LazyLinkedListX<T> fromStream(Stream<T> stream) {
        Reducer<PersistentList<T>,T> p = toPersistentList();
        return new LazyLinkedListX<T>(null, ReactiveSeq.fromStream(stream),p, gen,Evaluation.LAZY);
    }

    /**
     * Create a LazyLinkedListX that contains the Integers between start and end
     *
     * @param start
     *            Number of range to start from
     * @param end
     *            Number for range to end at
     * @return Range ListX
     */
    public static LazyLinkedListX<Integer> range(int start, int end) {
        return fromStream(ReactiveSeq.range(start, end));
    }

    /**
     * Create a LazyLinkedListX that contains the Longs between start and end
     *
     * @param start
     *            Number of range to start from
     * @param end
     *            Number for range to end at
     * @return Range ListX
     */
    public static LazyLinkedListX<Long> rangeLong(long start, long end) {
        return fromStream(ReactiveSeq.rangeLong(start, end));
    }

    /**
     * Unfold a function into a ListX
     *
     * <pre>
     * {@code
     *  LazyLinkedListX.unfold(1,i->i<=6 ? Optional.of(Tuple.tuple(i,i+1)) : Optional.empty());
     *
     * //(1,2,3,4,5)
     *
     * }</pre>
     *
     * @param seed Initial value
     * @param unfolder Iteratively applied function, terminated by an empty Optional
     * @return ListX generated by unfolder function
     */
    public static <U, T> LazyLinkedListX<T> unfold(U seed, Function<? super U, Option<Tuple2<T, U>>> unfolder) {
        return fromStream(ReactiveSeq.unfold(seed, unfolder));
    }

    /**
     * Generate a LazyLinkedListX from the provided Supplier up to the provided limit number of times
     *
     * @param limit Max number of elements to generate
     * @param s Supplier to generate ListX elements
     * @return ListX generated from the provided Supplier
     */
    public static <T> LazyLinkedListX<T> generate(long limit, Supplier<T> s) {

        return fromStream(ReactiveSeq.generate(s)
                                      .limit(limit));
    }

    /**
     * Create a LazyLinkedListX by iterative application of a function to an initial element up to the supplied limit number of times
     *
     * @param limit Max number of elements to generate
     * @param seed Initial element
     * @param f Iteratively applied to each element to generate the next element
     * @return ListX generated by iterative application
     */
    public static <T> LazyLinkedListX<T> iterate(long limit, final T seed, final UnaryOperator<T> f) {
        return fromStream(ReactiveSeq.iterate(seed, f)
                                      .limit(limit));
    }

    /**
     * <pre>
     * {@code
     * PersistentList<Integer> q = JSPersistentList.<Integer>toPersistentList()
                                     .mapReduce(Stream.of(1,2,3,4));
     *
     * }
     * </pre>
     * @return Reducer for PersistentList
     */
    public static <T> Reducer<PersistentList<T>,T> toPersistentList() {
        return Reducer.<PersistentList<T>,T> of(VavrListX.emptyPersistentList(), (final PersistentList<T> a) -> b -> a.plusAll(b), (final T x) -> VavrListX.singleton(x));
    }

    public static <T> VavrListX<T> emptyPersistentList(){
        return new VavrListX<T>(List.empty());
    }
    public static <T> LazyLinkedListX<T> empty(){
        return fromPersistentList(new VavrListX<T>(List.empty()), toPersistentList());
    }
    private static <T> LazyLinkedListX<T> fromPersistentList(PersistentList<T> s, Reducer<PersistentList<T>,T> pStackReducer) {
        return new LazyLinkedListX<T>(s,null, pStackReducer, gen, Evaluation.LAZY);
    }
    public static <T> LazyLinkedListX<T> singleton(T t){
        return fromPersistentList(new VavrListX<T>(List.of(t)), toPersistentList());
    }
    public static <T> LazyLinkedListX<T> of(T... t){
        return fromPersistentList(new VavrListX<T>(List.of(t)), toPersistentList());
    }
    public static <T> LazyLinkedListX<T> ofAll(List<T> q){
        return fromPersistentList(new VavrListX<T>(q), toPersistentList());
    }
    public static <T> LazyLinkedListX<T> PersistentList(List<T> q) {
        return fromPersistentList(new VavrListX<>(q), toPersistentList());
    }
    public static <T> LazyLinkedListX<T> from(List<T> q) {
        return fromPersistentList(new VavrListX<>(q), toPersistentList());
    }
    @SafeVarargs
    public static <T> LazyLinkedListX<T> PersistentList(T... elements){
        return fromPersistentList(of(elements),toPersistentList());
    }
    @Wither
    private final List<T> list;

    @Override
    public VavrListX<T> plus(T e) {
        return withList(list.prepend(e));
    }

    @Override
    public VavrListX<T> plusAll(Iterable<? extends T> l) {
        List<T> use = list;
        for(T next :  l)
            use = use.prepend(next);
        return withList(use);
    }

  @Override
  public VavrListX<T> updateAt(int i, T e) {
    return withList(list.update(i,e));
  }

  @Override
  public VavrListX<T> insertAt(int i, T e) {
    return withList(list.insert(i,e));
  }

  @Override
  public VavrListX<T> insertAt(int i, Iterable<? extends T> it) {
    return withList(list.insertAll(i,it));
  }

  @Override
  public VavrListX<T> removeValue(T e) {
    return withList(list.remove(e));
  }

  @Override
  public VavrListX<T> removeAll(Iterable<? extends T> it) {
    return withList(list.removeAll(it));
  }

  @Override
  public VavrListX<T> removeAt(int i) {
    return withList(list.removeAt(i));
  }



    @Override
    public Option<T> get(int index) {
      if(index>=0 && index<list.size())
        return Option.some(list.get(index));
      return Option.none();
    }

  @Override
  public T getOrElse(int index, T alt) {
    return get(index).orElse(alt);
  }

  @Override
  public Iterator<T> iterator() {
    return list.iterator();
  }

  @Override
  public T getOrElseGet(int index, Supplier<? extends T> alt) {
    return get(index).orElseGet(alt);
  }

  @Override
    public int size() {
        return list.size();
    }


}

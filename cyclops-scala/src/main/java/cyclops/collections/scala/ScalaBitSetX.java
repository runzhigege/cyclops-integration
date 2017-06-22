package cyclops.collections.scala;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import com.aol.cyclops.scala.collections.HasScalaCollection;
import com.aol.cyclops2.data.collections.extensions.CollectionX;
import com.aol.cyclops2.data.collections.extensions.lazy.immutable.LazyPOrderedSetX;
import com.aol.cyclops2.types.Unwrapable;
import com.aol.cyclops2.types.foldable.Evaluation;
import cyclops.collections.immutable.OrderedSetX;
import cyclops.function.Reducer;
import cyclops.stream.ReactiveSeq;
import org.jooq.lambda.tuple.Tuple2;
import org.pcollections.POrderedSet;



import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import scala.collection.GenTraversableOnce;
import scala.collection.JavaConversions;
import scala.collection.generic.CanBuildFrom;
import scala.collection.immutable.BitSet;
import scala.collection.immutable.BitSet$;
import scala.collection.mutable.Builder;

/*
 * BitSet is experimental / not ready for prime time
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ScalaBitSetX extends AbstractSet<Integer>implements POrderedSet<Integer>, HasScalaCollection, Unwrapable {
    public static OrderedSetX<Integer> bitSetX(ReactiveSeq<Integer> stream){
        return fromStream(stream);
    }
    @Override
    public <R> R unwrap() {
        return (R)set;
    }

    /**
     * Create a LazyPOrderedSetX from a Stream
     * 
     * @param stream to construct a LazyQueueX from
     * @return LazyPOrderedSetX
     */
    public static <T extends Comparable<? super T>> LazyPOrderedSetX<Integer> fromStream(Stream<Integer> stream) {
        Reducer<POrderedSet<Integer>> reducer = ScalaBitSetX.toPOrderedSet();
        return new LazyPOrderedSetX( null, ReactiveSeq.<Integer>fromStream(stream),
                                  reducer,
                Evaluation.LAZY);
    }

    /**
     * Create a LazyPOrderedSetX that contains the Integers between start and end
     * 
     * @param start
     *            Number of range to start from
     * @param end
     *            Number for range to end at
     * @return Range SetX
     */
    public static LazyPOrderedSetX<Integer> range(int start, int end) {
        return fromStream(ReactiveSeq.range(start, end));
    }

   

    /**
     * Unfold a function into a SetX
     * 
     * <pre>
     * {@code 
     *  LazyPOrderedSetX.unfold(1,i->i<=6 ? Optional.of(Tuple.tuple(i,i+1)) : Optional.empty());
     * 
     * //(1,2,3,4,5)
     * 
     * }</pre>
     * 
     * @param seed Initial value 
     * @param unfolder Iteratively applied function, terminated by an empty Optional
     * @return SetX generated by unfolder function
     */
    public static  LazyPOrderedSetX<Integer> unfold(Integer seed, Function<? super Integer, Optional<Tuple2<Integer, Integer>>> unfolder) {
        return fromStream(ReactiveSeq.unfold(seed, unfolder));
    }

    /**
     * Generate a LazyPOrderedSetX from the provided Supplier up to the provided limit number of times
     * 
     * @param limit Max number of elements to generate
     * @param s Supplier to generate SetX elements
     * @return SetX generated from the provided Supplier
     */
    public static <T extends Comparable<? super T>> LazyPOrderedSetX generate(long limit, Supplier s) {

        return fromStream(ReactiveSeq.generate(s)
                                     .limit(limit));
    }

    /**
     * Create a LazyPOrderedSetX by iterative application of a function to an initial element up to the supplied limit number of times
     * 
     * @param limit Max number of elements to generate
     * @param seed Initial element
     * @param f Iteratively applied to each element to generate the next element
     * @return SetX generated by iterative application
     */
    public static <T extends Comparable<? super T>> LazyPOrderedSetX iterate(long limit, final T seed, final UnaryOperator f) {
        return fromStream(ReactiveSeq.iterate(seed, f)
                                     .limit(limit));
    }

    /**
     * <pre>
     * {@code 
     * POrderedSet<Integer> q = JSPOrderedSet.<Integer>toPOrderedSet()
                                     .mapReduce(Stream.of(1,2,3,4));
     * 
     * }
     * </pre>
     * @return Reducer for POrderedSet
     */
    public static  Reducer<POrderedSet<Integer>> toPOrderedSet() {
        return Reducer.<POrderedSet<Integer>> of(ScalaBitSetX.emptyPOrderedSet(),
                                                 (final POrderedSet<Integer> a) -> b -> a.plusAll(b),
                                      (final Integer x) -> ScalaBitSetX.singleton(x));
    }
    
  

    public static ScalaBitSetX fromSet(BitSet set) {
        return new ScalaBitSetX(
                                 set);
    }

    
    public static ScalaBitSetX emptyPOrderedSet() {
        return new ScalaBitSetX(
                                 BitSet$.MODULE$.empty());
    }
    
    
    public static LazyPOrderedSetX<Integer> empty() {
        
        
        return fromPOrderedSet(new ScalaBitSetX(BitSet$.MODULE$.empty()),
                                                toPOrderedSet());
    }

    public static  LazyPOrderedSetX<Integer> singleton(Integer t) {
        return of(t);
    }
   
    public static  LazyPOrderedSetX<Integer> of(Integer... t) {

        Builder<Integer, BitSet> lb = (Builder)BitSet$.MODULE$.newBuilder();
       for (Integer next : t)
           lb.$plus$eq(next);
       BitSet vec = lb.result();
       return fromPOrderedSet(new ScalaBitSetX(
                                                       vec),
                                     toPOrderedSet());
   }



    private static <T> LazyPOrderedSetX<T> fromPOrderedSet(POrderedSet<T> ordered, Reducer<POrderedSet<T>> reducer) {
        return  new LazyPOrderedSetX<T>(ordered,null,reducer,Evaluation.LAZY);
    }
    public static  LazyPOrderedSetX<Integer> POrderedSet(BitSet q) {
        return fromPOrderedSet(new ScalaBitSetX(
                                                         q),
                                      toPOrderedSet());
    }

    @SafeVarargs
    public static  LazyPOrderedSetX<Integer> POrderedSet(Integer... elements) {
        return fromPOrderedSet(of(elements), toPOrderedSet());
    }

    @Wither
    private final BitSet set;

    @Override
    public ScalaBitSetX plus(Integer e) {
       
        return withSet(set.$plus((int)e));
    }

    @Override
    public ScalaBitSetX plusAll(Collection<? extends Integer> l) {
        
        
        BitSet vec = set;
        if(l instanceof HasScalaCollection){
            HasScalaCollection<Integer> sc = (HasScalaCollection)l;
            return withSet((BitSet)vec.$plus$plus(sc.traversable(), sc.canBuildFrom()));
        }
        for (Integer next : l) {
              vec = vec.$plus((int)next);
        }

        return withSet(vec);
       
    }

   

    
  

    @Override
    public POrderedSet<Integer> minus(Object e) {
        if(e instanceof Integer){
            Integer i =(Integer)e;
            return withSet((BitSet)set.$minus$minus(BitSet$.MODULE$.empty().$plus((int)i)));
        }
        else
            return this;
                
    }

    @Override
    public POrderedSet<Integer> minusAll(Collection<?> s) {
        GenTraversableOnce gen =  HasScalaCollection.traversable(s);
        return withSet((BitSet)set.$minus$minus(gen));        
    }

  
   

    @Override
    public int size() {
        return set.size();
    }

    @Override
    public Iterator<Integer> iterator() {
        return (Iterator)JavaConversions.asJavaIterator(set.iterator());
    }

    @Override
    public Integer get(int index) {
        return (Integer) set.toIndexedSeq().toVector().apply(index);
    }

    @Override
    public int indexOf(Object o) {
        return set.toIndexedSeq().toVector().indexOf(o);
    }

    @Override
    public GenTraversableOnce traversable() {
        
        return this.set;
    }

    @Override
    public CanBuildFrom canBuildFrom() {
       return BitSet.canBuildFrom();
    }

    public static OrderedSetX<Integer> copyFromCollection(CollectionX<Integer> vec) {
        OrderedSetX<Integer> res = ScalaBitSetX.empty()
                .plusAll(vec);
        return res;
    }

}
package com.aol.cyclops.reactor;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.jooq.lambda.tuple.Tuple;
import org.reactivestreams.Publisher;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.For;
import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.Publishers;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.reactor.transformer.FluxT;
import com.aol.cyclops.reactor.transformer.FluxTSeq;
import com.aol.cyclops.reactor.transformer.MonoT;
import com.aol.cyclops.reactor.transformer.MonoTSeq;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.anyM.AnyMValue;
import com.aol.cyclops.util.function.QuadFunction;
import com.aol.cyclops.util.function.TriFunction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * For Comprehensions for FluxT and MonoT
 * 
 * AnyM creational methods for FluxT and MonoT
 * 
 * @author johnmcclean
 *
 */
public interface Reactor {

    /**
     * Construct an AnyM type from a Flux. This allows the Flux to be manipulated according to a standard interface
     * along with a vast array of other Java Monad implementations
     * 
     * <pre>
     * {@code 
     *    
     *    AnyMSeq<Integer> fluxT = Reactor.fromFluxT(fluxT);
     *    AnyMSeq<Integer> transformedFluxT = myGenericOperation(flux);
     *    
     *    public AnyMSeq<Integer> myGenericOperation(AnyMSeq<Integer> monad);
     * }
     * </pre>
     * 
     * @param fluxT To wrap inside an AnyM
     * @return AnyMSeq wrapping a fluxT
     */
    public static <T> AnyMSeq<T> fromFluxT(FluxT<T> flux) {
        return AnyM.ofSeq(flux);
    }

   
    /**
     * Construct a FluxT from a Publisher containing Fluxes.
     * 
     * @param nested Publisher of Fluxes
     * @return Flux Transformer for manipulating nested Fluxes
     */
    public static <T> FluxTSeq<T> fluxT(Publisher<Flux<T>> nested) {
        return FluxT.fromPublisher(nested);
    }

    
    /**
     * Construct a MonoT from a Publisher containing Monos.
     * 
     * @param nested Publisher of Monos
     * @return Mono Transformer for manipulating nested Monos
     */
    public static <T> MonoTSeq<T> monoT(Publisher<Mono<T>> nested) {
        return MonoT.fromPublisher(Flux.from(nested));
    }
    /**
     * Construct an AnyM type from a Mono. This allows the Mono to be manipulated according to a standard interface
     * along with a vast array of other Java Monad implementations
     * 
     * <pre>
     * {@code 
     *    
     *    AnyMSeq<Integer> monoT = Reactor.fromFluxT(fluxT);
     *    AnyMSeq<Integer> transformedMonoT = myGenericOperation(mono);
     *    
     *    public AnyMSeq<Integer> myGenericOperation(AnyMSeq<Integer> monad);
     * }
     * </pre>
     * 
     * @param fluxT To wrap inside an AnyM
     * @return AnyMSeq wrapping a fluxT
     */
    public static <T> AnyMSeq<T> fromMonoT(MonoT<T> mono) {
        return AnyM.ofSeq(mono);
    }

   
    public interface ForFluxTransformer {
         /**
          * Perform a For Comprehension over a FluxT, accepting 3 generating functions. 
          * This results in a four level nested internal iteration over the provided Publishers.
          * 
          *  <pre>
          * {@code
          *    
          *   import static com.aol.cyclops.reactor.FluxTs.forEach;
          *   
          *    FluxT<Integer> fluxT = FluxT.fromIterable(Arrays.asList(Flux.range(10,2),Flux.range(100,2)));
          *    
              forEach(fluxT, 
                       a-> ReactiveSeq.iterate(a,i->i+1).limit(2),
                       (a,b)-> ReactiveSeq.iterate(a,i->i+1).limit(2),
                       (a,b,c)-> ReactiveSeq.iterate(a,i->i+1).limit(2),
                    Tuple::tuple)
                    .forEach(System.out::println);
                    
          //(10, 10, 10, 10)
            (11, 11, 11, 11)
            (100, 100, 100, 100)
            (101, 101, 101, 101)        
         * 
         * }
         * </pre>
         * 
         * @param value1 top level FluxT
         * @param value2 Nested publisher
         * @param value3 Nested publisher
         * @param value4 Nested publisher
         * @param yieldingFunction  Generates a result per combination
         * @return FluxTSeq with an element per combination of nested publishers generated by the yielding function
         */
        public static <T1, T2, T3, R1, R2, R3, R> FluxT<R> forEach(FluxT<? extends T1> value1,
                Function<? super T1, ? extends Publisher<R1>> value2,
                BiFunction<? super T1, ? super R1, ? extends Publisher<R2>> value3,
                TriFunction<? super T1, ? super R1, ? super R2, ? extends Publisher<R3>> value4,
                QuadFunction<? super T1, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

            return AnyM.ofSeq(For.anyM(fromFluxT(value1))
                                 .anyM(a -> Publishers.anyM(value2.apply(a)))
                                 .anyM(a -> b -> Publishers.anyM(value3.apply(a, b)))
                                 .anyM(a -> b -> c -> Publishers.anyM(value4.apply(a, b, c)))
                                 .yield4(yieldingFunction)
                                 .unwrap())
                       .unwrap();

        }
       

        /**
         * Perform a For Comprehension over a FluxT, accepting 3 generating functions. 
         * This results in a four level nested internal iteration over the provided Publishers.
         * 
         *  <pre>
         * {@code
         *    
         *   import static com.aol.cyclops.reactor.FluxTs.forEach;
         *   
         *    FluxT<Integer> fluxT = FluxT.fromIterable(Arrays.asList(Flux.range(10,2),Flux.range(100,2)));
         *    
             forEach(fluxT, 
                      a-> ReactiveSeq.iterate(a,i->i+1).limit(2),
                      (a,b)-> ReactiveSeq.iterate(a,i->i+1).limit(2),
                      (a,b,c)-> ReactiveSeq.iterate(a,i->i+1).limit(2),
                      (a,b,c,d)->a+b+c+d<102,
                   Tuple::tuple)
                   .forEach(System.out::println);
                   
         //(10, 10, 10, 10)
           (11, 11, 11, 11)       
        * 
        * }
        * </pre>
         * @param value1 top level FluxT
         * @param value2 Nested publisher
         * @param value3 Nested publisher
         * @param value4 Nested publisher
         * @param filterFunction A filtering function, keeps values where the predicate holds
         * @param yieldingFunction Generates a result per combination
         * @return
         */
        public static <T1, T2, T3, R1, R2, R3, R> FluxT<R> forEach(FluxT<? extends T1> value1,
                Function<? super T1, ? extends Publisher<R1>> value2,
                BiFunction<? super T1, ? super R1, ? extends Publisher<R2>> value3,
                TriFunction<? super T1, ? super R1, ? super R2, ? extends Publisher<R3>> value4,
                QuadFunction<? super T1, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                QuadFunction<? super T1, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

            return AnyM.ofSeq(For.anyM(fromFluxT(value1))
                                 .anyM(a -> Publishers.anyM(value2.apply(a)))
                                 .anyM(a -> b -> Publishers.anyM(value3.apply(a, b)))
                                 .anyM(a -> b -> c -> Publishers.anyM(value4.apply(a, b, c)))
                                 .filter(a -> b -> c -> d -> filterFunction.apply(a, b, c, d))
                                 .yield4(yieldingFunction)
                                 .unwrap())
                       .unwrap();

        }
        /**
         * Perform a For Comprehension over a FluxT, accepting 2 generating functions. 
         * This results in three level nested internal iteration over the provided Publishers.
         * 
         *  <pre>
         * {@code
         *    
         *   import static com.aol.cyclops.reactor.FluxTs.forEach;
         *   
         *    FluxT<Integer> fluxT = FluxT.fromIterable(Arrays.asList(Flux.range(10,2),Flux.range(100,2)));
         *    
             forEach(fluxT, 
                      a-> ReactiveSeq.iterate(a,i->i+1).limit(2),
                      (a,b)-> ReactiveSeq.iterate(a,i->i+1).limit(2),
                   Tuple::tuple)
                   .forEach(System.out::println);
                   
              
        * 
        * }
        * </pre>
        * 
        * @param value1 top level FluxT
        * @param value2 Nested publisher
        * @param value3 Nested publisher
        * @param yieldingFunction  Generates a result per combination
        * @return FluxTSeq with an element per combination of nested publishers generated by the yielding function
        */
        public static <T1, T2, R1, R2, R> FluxT<R> forEach(FluxT<? extends T1> value1,
                Function<? super T1, ? extends Publisher<R1>> value2,
                BiFunction<? super T1, ? super R1, ? extends Publisher<R2>> value3,
                TriFunction<? super T1, ? super R1, ? super R2, ? extends R> yieldingFunction) {

            return AnyM.ofSeq(For.anyM(fromFluxT(value1))
                                 .anyM(a -> Publishers.anyM(value2.apply(a)))
                                 .anyM(a -> b -> Publishers.anyM(value3.apply(a, b)))
                                 .yield3(yieldingFunction)
                                 .unwrap())
                       .unwrap();

        }
        /**
         * Perform a For Comprehension over a FluxT, accepting 2 generating functions. 
         * This results in a three level nested internal iteration over the provided Publishers.
         * 
         *  <pre>
         * {@code
         *    
         *   import static com.aol.cyclops.reactor.FluxTs.forEach;
         *   
         *    FluxT<Integer> fluxT = FluxT.fromIterable(Arrays.asList(Flux.range(10,2),Flux.range(100,2)));
         *    
             forEach(fluxT, 
                      a-> ReactiveSeq.iterate(a,i->i+1).limit(2),
                      (a,b)-> ReactiveSeq.iterate(a,i->i+1).limit(2),
                      (a,b,c,d)->a+b+c+d<102,
                   Tuple::tuple)
                   .forEach(System.out::println);
     
        * 
        * }
        * </pre>
         * @param value1 top level FluxT
         * @param value2 Nested publisher
         * @param value3 Nested publisher
         * @param filterFunction A filtering function, keeps values where the predicate holds
         * @param yieldingFunction Generates a result per combination
         * @return FluxTSeq with an element per combination of nested publishers generated by the yielding function
         */
       public  static <T1, T2, R1, R2, R> FluxT<R> forEach(FluxT<? extends T1> value1,
                Function<? super T1, ? extends Publisher<R1>> value2,
                BiFunction<? super T1, ? super R1, ? extends Publisher<R2>> value3,
                TriFunction<? super T1, ? super R1, ? super R2, Boolean> filterFunction,
                TriFunction<? super T1, ? super R1, ? super R2, ? extends R> yieldingFunction) {

            return AnyM.ofSeq(For.anyM(fromFluxT(value1))
                                 .anyM(a -> Publishers.anyM(value2.apply(a)))
                                 .anyM(a -> b -> Publishers.anyM(value3.apply(a, b)))
                                 .filter(a -> b -> c -> filterFunction.apply(a, b, c))
                                 .yield3(yieldingFunction)
                                 .unwrap())
                       .unwrap();

        }

       /**
        * Perform a For Comprehension over a FluxT, accepting a generating function. 
        * This results in two level nested internal iteration over the provided Publishers.
        * 
        *  <pre>
        * {@code
        *    
        *   import static com.aol.cyclops.reactor.FluxTs.forEach;
        *   
        *    FluxT<Integer> fluxT = FluxT.fromIterable(Arrays.asList(Flux.range(10,2),Flux.range(100,2)));
        *    
            forEach(fluxT, 
                     a-> ReactiveSeq.iterate(a,i->i+1).limit(2),
                  Tuple::tuple)
                  .forEach(System.out::println);
                  
             
       * 
       * }
       * </pre>
       * 
       * @param value1 top level FluxT
       * @param value2 Nested publisher
       * @param value3 Nested publisher
       * @param yieldingFunction  Generates a result per combination
       * @return FluxTSeq with an element per combination of nested publishers generated by the yielding function
       */
        public static <T, R1, R> FluxT<R> forEach(FluxT<? extends T> value1, Function<? super T, Publisher<R1>> value2,
                BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

            return AnyM.ofSeq(For.anyM(fromFluxT(value1))
                                 .anyM(a -> Publishers.anyM(value2.apply(a)))
                                 .yield2(yieldingFunction)
                                 .unwrap())
                       .unwrap();

        }
        /**
         * Perform a For Comprehension over a FluxT, accepting a generating function. 
         * This results in a two level nested internal iteration over the provided Publishers.
         * 
         *  <pre>
         * {@code
         *    
         *   import static com.aol.cyclops.reactor.FluxTs.forEach;
         *   
         *   FluxT<Integer> fluxT = FluxT.fromIterable(Arrays.asList(Flux.range(10,2),Flux.range(100,2)));
         *    
             forEach(fluxT, 
                      a-> ReactiveSeq.iterate(a,i->i+1).limit(2),
                      (a,b,c,d)->a+b+c+d<102,
                   Tuple::tuple)
                   .forEach(System.out::println);
     
        * 
        * }
        * </pre>
         * @param value1 top level FluxT
         * @param value2 Nested publisher
         * @param value3 Nested publisher
         * @param filterFunction A filtering function, keeps values where the predicate holds
         * @param yieldingFunction Generates a result per combination
         * @return FluxTSeq with an element per combination of nested publishers generated by the yielding function
         */
        public static <T, R1, R> FluxT<R> forEach(FluxT<? extends T> value1, Function<? super T, ? extends Publisher<R1>> value2,
                BiFunction<? super T, ? super R1, Boolean> filterFunction,
                BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

            return AnyM.ofSeq(For.anyM(fromFluxT(value1))
                                 .anyM(a -> Publishers.anyM(value2.apply(a)))
                                 .filter(a -> b -> filterFunction.apply(a, b))
                                 .yield2(yieldingFunction)
                                 .unwrap())
                       .unwrap();

        }
    }

   

}

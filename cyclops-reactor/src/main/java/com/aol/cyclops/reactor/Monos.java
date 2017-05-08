package com.aol.cyclops.reactor;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.aol.cyclops.reactor.ReactorWitness.mono;
import com.aol.cyclops.reactor.hkt.MonoKind;
import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.types.Value;
import com.aol.cyclops2.types.anyM.AnyMValue;
import cyclops.async.Future;
import cyclops.function.Fn1;
import cyclops.function.Fn3;
import cyclops.function.Fn4;
import cyclops.function.Monoid;
import cyclops.monads.AnyM;
import cyclops.typeclasses.Pure;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.instances.General;
import cyclops.typeclasses.monad.*;
import org.reactivestreams.Publisher;



import lombok.experimental.UtilityClass;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Companion class for working with Reactor Mono types
 * 
 * @author johnmcclean
 *
 */
@UtilityClass
public class Monos {

    
    /**
     * Construct an AnyM type from a Mono. This allows the Mono to be manipulated according to a standard interface
     * along with a vast array of other Java Monad implementations
     * 
     * <pre>
     * {@code 
     *    
     *    AnyMSeq<Integer> mono = Fluxs.anyM(Mono.just(1,2,3));
     *    AnyMSeq<Integer> transformedMono = myGenericOperation(mono);
     *    
     *    public AnyMSeq<Integer> myGenericOperation(AnyMSeq<Integer> monad);
     * }
     * </pre>
     * 
     * @param mono To wrap inside an AnyM
     * @return AnyMSeq wrapping a Mono
     */
    public static <T> AnyMValue<mono,T> anyM(Mono<T> mono) {
        return AnyM.ofValue(mono, ReactorWitness.mono.INSTANCE);
    }

    /**
     * Perform a For Comprehension over a Mono, accepting 3 generating functions. 
     * This results in a four level nested internal iteration over the provided Monos.
     * 
     *  <pre>
     * {@code
     *    
     *   import static com.aol.cyclops.reactor.Monos.forEach4;
     *    
          forEach4(Mono.just(1), 
                  a-> Mono.just(a+1),
                  (a,b) -> Mono.<Integer>just(a+b),
                  (a,b,c) -> Mono.<Integer>just(a+b+c),
                  Tuple::tuple)
     * 
     * }
     * </pre>
     * 
     * @param value1 top level Mono
     * @param value2 Nested Mono
     * @param value3 Nested Mono
     * @param value4 Nested Mono
     * @param yieldingFunction Generates a result per combination
     * @return Mono with a combined value generated by the yielding function
     */
    public static <T1, T2, T3, R1, R2, R3, R> Mono<R> forEach4(Mono<? extends T1> value1,
            Function<? super T1, ? extends Mono<R1>> value2,
            BiFunction<? super T1, ? super R1, ? extends Mono<R2>> value3,
            Fn3<? super T1, ? super R1, ? super R2, ? extends Mono<R3>> value4,
            Fn4<? super T1, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {


        Future<? extends R> res = Future.fromPublisher(value1).flatMap(in -> {

            Future<R1> a = Future.fromPublisher(value2.apply(in));
            return a.flatMap(ina -> {
                Future<R2> b = Future.fromPublisher(value3.apply(in, ina));
                return b.flatMap(inb -> {
                    Future<R3> c = Future.fromPublisher(value4.apply(in, ina, inb));
                    return c.map(in2 -> yieldingFunction.apply(in, ina, inb, in2));
                });

            });

        });
        return Mono.from(res);
    }


    /**
     * Perform a For Comprehension over a Mono, accepting 2 generating functions. 
     * This results in a three level nested internal iteration over the provided Monos.
     * 
     *  <pre>
     * {@code
     *    
     *   import static com.aol.cyclops.reactor.Monos.forEach3;
     *    
          forEach3(Mono.just(1), 
                  a-> Mono.just(a+1),
                  (a,b) -> Mono.<Integer>just(a+b),
                  Tuple::tuple)
     * 
     * }
     * </pre>
     * 
     * @param value1 top level Mono
     * @param value2 Nested Mono
     * @param value3 Nested Mono
     * @param yieldingFunction Generates a result per combination
     * @return Mono with a combined value generated by the yielding function
     */
    public static <T1, T2, R1, R2, R> Mono<R> forEach3(Mono<? extends T1> value1,
            Function<? super T1, ? extends Mono<R1>> value2,
            BiFunction<? super T1, ? super R1, ? extends Mono<R2>> value3,
            Fn3<? super T1, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        Future<? extends R> res = Future.fromPublisher(value1).flatMap(in -> {

            Future<R1> a = Future.fromPublisher(value2.apply(in));
            return a.flatMap(ina -> {
                Future<R2> b = Future.fromPublisher(value3.apply(in, ina));


                return b.map(in2 -> yieldingFunction.apply(in, ina, in2));


            });

        });
        return Mono.from(res);

    }



    /**
     * Perform a For Comprehension over a Mono, accepting a generating function. 
     * This results in a two level nested internal iteration over the provided Monos.
     * 
     *  <pre>
     * {@code
     *    
     *   import static com.aol.cyclops.reactor.Monos.forEach;
     *    
          forEach(Mono.just(1), 
                  a-> Mono.just(a+1),
                  Tuple::tuple)
     * 
     * }
     * </pre>
     * 
     * @param value1 top level Mono
     * @param value2 Nested Mono
     * @param yieldingFunction Generates a result per combination
     * @return Mono with a combined value generated by the yielding function
     */
    public static <T, R1, R> Mono<R> forEach(Mono<? extends T> value1,
                                             Function<? super T, Mono<R1>> value2,
                                             BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        Future<R> res = Future.fromPublisher(value1).flatMap(in -> {

            Future<R1> a = Future.fromPublisher(value2.apply(in));
            return a.map(ina -> yieldingFunction.apply(in, ina));


        });


        return Mono.from(res);

    }



    /**
     * Lazily combine this Mono with the supplied value via the supplied BiFunction
     * 
     * @param mono Mono to combine with another value
     * @param app Value to combine with supplied mono
     * @param fn Combiner function
     * @return Combined Mono
     */
    public static <T1, T2, R> Mono<R> combine(Mono<? extends T1> mono, Value<? extends T2> app,
            BiFunction<? super T1, ? super T2, ? extends R> fn) {
        return Mono.from(Future.of(mono.toFuture())
                                .combine(app, fn));
    }

    /**
     * Lazily combine this Mono with the supplied Mono via the supplied BiFunction
     * 
     * @param mono Mono to combine with another value
     * @param app Mono to combine with supplied mono
     * @param fn Combiner function
     * @return Combined Mono
     */
    public static <T1, T2, R> Mono<R> combine(Mono<? extends T1> mono, Mono<? extends T2> app,
            BiFunction<? super T1, ? super T2, ? extends R> fn) {
        return Mono.from(Future.of(mono.toFuture())
                                .combine(Future.of(app.toFuture()), fn));
    }

    /**
     * Combine the provided Mono with the first element (if present) in the provided Iterable using the provided BiFunction
     * 
     * @param mono Mono to combine with an Iterable
     * @param app Iterable to combine with a Mono
     * @param fn Combining function
     * @return Combined Mono
     */
    public static <T1, T2, R> Mono<R> zip(Mono<? extends T1> mono, Iterable<? extends T2> app,
            BiFunction<? super T1, ? super T2, ? extends R> fn) {
        return Mono.from(Future.of(mono.toFuture())
                                .zip(app, fn));
    }

    /**
     * Combine the provided Mono with the first element (if present) in the provided Publisher using the provided BiFunction
     * 
     * @param mono  Mono to combine with a Publisher
     * @param fn Publisher to combine with a Mono
     * @param app Combining function
     * @return Combined Mono
     */
    public static <T1, T2, R> Mono<R> zip(Mono<? extends T1> mono, BiFunction<? super T1, ? super T2, ? extends R> fn,
            Publisher<? extends T2> app) {
        Mono<R> res = Mono.from(Future.of(mono.toFuture()).zipP(app, fn));
        return res;
    }

    /**
     * Test if value is equal to the value inside this Mono
     * 
     * @param mono Mono to test
     * @param test Value to test
     * @return true if equal
     */
    public static <T> boolean test(Mono<T> mono, T test) {
        return Future.of(mono.toFuture())
                      .test(test);
    }

    /**
     * Construct a Mono from Iterable by taking the first value from Iterable
     * 
     * @param t Iterable to populate Mono from
     * @return Mono containing first element from Iterable (or empty Mono)
     */
    public static <T> Mono<T> fromIterable(Iterable<T> t) {
        return Mono.from(Flux.fromIterable(t));
    }

    /**
     * Get an Iterator for the value (if any) in the provided Mono
     * 
     * @param pub Mono to get Iterator for
     * @return Iterator over Mono value
     */
    public static <T> Iterator<T> iterator(Mono<T> pub) {
        return Future.fromPublisher(pub).iterator();

    }

    /**
     * Companion class for creating Type Class instances for working with Monos
     * @author johnmcclean
     *
     */
    @UtilityClass
    public static class Instances {


        /**
         *
         * Transform a Mono, mulitplying every element by 2
         *
         * <pre>
         * {@code
         *  MonoKind<Integer> future = Monos.functor().map(i->i*2, MonoKind.widen(Mono.just(3));
         *
         *  //[6]
         *
         *
         * }
         * </pre>
         *
         * An example fluent api working with Monos
         * <pre>
         * {@code
         *   MonoKind<Integer> ft = Monos.unit()
        .unit("hello")
        .then(h->Monos.functor().map((String v) ->v.length(), h))
        .convert(MonoKind::narrowK);
         *
         * }
         * </pre>
         *
         *
         * @return A functor for Monos
         */
        public static <T,R>Functor<MonoKind.µ> functor(){
            BiFunction<MonoKind<T>,Function<? super T, ? extends R>,MonoKind<R>> map = Instances::map;
            return General.functor(map);
        }
        /**
         * <pre>
         * {@code
         * MonoKind<String> ft = Monos.unit()
        .unit("hello")
        .convert(MonoKind::narrowK);

        //Mono["hello"]
         *
         * }
         * </pre>
         *
         *
         * @return A factory for Monos
         */
        public static <T> Pure<MonoKind.µ> unit(){
            return General.<MonoKind.µ,T>unit(Instances::of);
        }
        /**
         *
         * <pre>
         * {@code
         * import static com.aol.cyclops.hkt.jdk.MonoKind.widen;
         * import static com.aol.cyclops.util.function.Lambda.l1;
         * import static java.util.Arrays.asMono;
         *
        Monos.applicative()
        .ap(widen(asMono(l1(this::multiplyByTwo))),widen(Mono.just(3)));
         *
         * //[6]
         * }
         * </pre>
         *
         *
         * Example fluent API
         * <pre>
         * {@code
         * MonoKind<Function<Integer,Integer>> ftFn =Monos.unit()
         *                                                  .unit(Lambda.l1((Integer i) ->i*2))
         *                                                  .convert(MonoKind::narrowK);

        MonoKind<Integer> ft = Monos.unit()
        .unit("hello")
        .then(h->Monos.functor().map((String v) ->v.length(), h))
        .then(h->Monos.applicative().ap(ftFn, h))
        .convert(MonoKind::narrowK);

        //Mono.just("hello".length()*2))
         *
         * }
         * </pre>
         *
         *
         * @return A zipper for Monos
         */
        public static <T,R> Applicative<MonoKind.µ> applicative(){
            BiFunction<MonoKind< Function<T, R>>,MonoKind<T>,MonoKind<R>> ap = Instances::ap;
            return General.applicative(functor(), unit(), ap);
        }
        /**
         *
         * <pre>
         * {@code
         * import static com.aol.cyclops.hkt.jdk.MonoKind.widen;
         * MonoKind<Integer> ft  = Monos.monad()
        .flatMap(i->widen(Mono.just(i)), widen(Mono.just(3)))
        .convert(MonoKind::narrowK);
         * }
         * </pre>
         *
         * Example fluent API
         * <pre>
         * {@code
         *    MonoKind<Integer> ft = Monos.unit()
        .unit("hello")
        .then(h->Monos.monad().flatMap((String v) ->Monos.unit().unit(v.length()), h))
        .convert(MonoKind::narrowK);

        //Mono.just("hello".length())
         *
         * }
         * </pre>
         *
         * @return Type class with monad functions for Monos
         */
        public static <T,R> Monad<MonoKind.µ> monad(){

            BiFunction<Higher<MonoKind.µ,T>,Function<? super T, ? extends Higher<MonoKind.µ,R>>,Higher<MonoKind.µ,R>> flatMap = Instances::flatMap;
            return General.monad(applicative(), flatMap);
        }
        /**
         *
         * <pre>
         * {@code
         *  MonoKind<String> ft = Monos.unit()
        .unit("hello")
        .then(h->Monos.monadZero().filter((String t)->t.startsWith("he"), h))
        .convert(MonoKind::narrowK);

        //Mono.just("hello"));
         *
         * }
         * </pre>
         *
         *
         * @return A filterable monad (with default value)
         */
        public static <T,R> MonadZero<MonoKind.µ> monadZero(){

            return General.monadZero(monad(), MonoKind.empty());
        }
        /**
         * Combines Monos by selecting the first result returned
         *
         * <pre>
         * {@code
         *  MonoKind<Integer> ft = Monos.<Integer>monadPlus()
        .plus(MonoKind.widen(Mono.empty()), MonoKind.widen(Mono.just(10)))
        .convert(MonoKind::narrowK);
        //Mono.empty()
         *
         * }
         * </pre>
         * @return Type class for combining Monos by concatenation
         */
        public static <T> MonadPlus<MonoKind.µ> monadPlus(){


            Monoid<MonoKind<T>> m = Monoid.of(MonoKind.<T>widen(Mono.empty()),
                    (f,g)-> MonoKind.widen(Mono.first(f.narrow(),g.narrow())));

            Monoid<Higher<MonoKind.µ,T>> m2= (Monoid)m;
            return General.monadPlus(monadZero(),m2);
        }
        /**
         *
         * <pre>
         * {@code
         *  Monoid<MonoKind<Integer>> m = Monoid.of(MonoKind.widen(Arrays.asMono()), (a,b)->a.isEmpty() ? b : a);
        MonoKind<Integer> ft = Monos.<Integer>monadPlus(m)
        .plus(MonoKind.widen(Arrays.asMono(5)), MonoKind.widen(Arrays.asMono(10)))
        .convert(MonoKind::narrowK);
        //Arrays.asMono(5))
         *
         * }
         * </pre>
         *
         * @param m Monoid to use for combining Monos
         * @return Type class for combining Monos
         */
        public static <T> MonadPlus<MonoKind.µ> monadPlus(Monoid<MonoKind<T>> m){
            Monoid<Higher<MonoKind.µ,T>> m2= (Monoid)m;
            return General.monadPlus(monadZero(),m2);
        }

        /**
         * @return Type class for traversables with traverse / sequence operations
         */
        public static <C2,T> Traverse<MonoKind.µ> traverse(){

            return General.traverseByTraverse(applicative(), Instances::traverseA);
        }

        /**
         *
         * <pre>
         * {@code
         * int sum  = Monos.foldable()
        .foldLeft(0, (a,b)->a+b, MonoKind.widen(Arrays.asMono(1,2,3,4)));

        //10
         *
         * }
         * </pre>
         *
         *
         * @return Type class for folding / reduction operations
         */
        public static <T> Foldable<MonoKind.µ> foldable(){
            BiFunction<Monoid<T>,Higher<MonoKind.µ,T>,T> foldRightFn =  (m, l)-> m.apply(m.zero(), MonoKind.narrow(l).block());
            BiFunction<Monoid<T>,Higher<MonoKind.µ,T>,T> foldLeftFn = (m, l)->  m.apply(m.zero(), MonoKind.narrow(l).block());
            return General.foldable(foldRightFn, foldLeftFn);
        }
        public static <T> Comonad<MonoKind.µ> comonad(){
            Function<? super Higher<MonoKind.µ, T>, ? extends T> extractFn = maybe -> maybe.convert(MonoKind::narrow).block();
            return General.comonad(functor(), unit(), extractFn);
        }

        private static <T> MonoKind<T> of(T value){
            return MonoKind.widen(Mono.just(value));
        }
        private static <T,R> MonoKind<R> ap(MonoKind<Function< T, R>> lt, MonoKind<T> list){


            return MonoKind.widen(Monos.combine(lt.narrow(),list.narrow(), (a, b)->a.apply(b)));

        }
        private static <T,R> Higher<MonoKind.µ,R> flatMap(Higher<MonoKind.µ,T> lt, Function<? super T, ? extends  Higher<MonoKind.µ,R>> fn){
            return MonoKind.widen(MonoKind.narrow(lt).flatMap(fn.andThen(MonoKind::narrow)));
        }
        private static <T,R> MonoKind<R> map(MonoKind<T> lt, Function<? super T, ? extends R> fn){
            return MonoKind.widen(lt.narrow().map(fn));
        }


        private static <C2,T,R> Higher<C2, Higher<MonoKind.µ, R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn,
                                                                            Higher<MonoKind.µ, T> ds){
            Mono<T> future = MonoKind.narrow(ds);
            return applicative.map(MonoKind::just, fn.apply(future.block()));
        }

    }

}

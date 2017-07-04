package cyclops.companion.vavr;


import com.aol.cyclops.vavr.hkt.LazyKind;
import cyclops.VavrConverters;
import cyclops.control.Maybe;
import cyclops.conversion.vavr.FromCyclopsReact;
import cyclops.conversion.vavr.FromJDK;
import cyclops.conversion.vavr.FromJooqLambda;
import cyclops.monads.VavrWitness;
import cyclops.monads.VavrWitness.array;
import com.aol.cyclops.vavr.hkt.ArrayKind;
import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.types.anyM.AnyMSeq;
import cyclops.function.Fn3;
import cyclops.function.Fn4;
import cyclops.function.Monoid;
import cyclops.monads.AnyM;

import cyclops.stream.ReactiveSeq;
import cyclops.typeclasses.Active;
import cyclops.typeclasses.InstanceDefinitions;
import cyclops.typeclasses.Nested;
import cyclops.typeclasses.Pure;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.instances.General;
import cyclops.typeclasses.monad.*;
import io.vavr.Lazy;
import io.vavr.collection.Array;
import lombok.experimental.UtilityClass;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;


public class Arrays {


   
    public static <T> AnyMSeq<array,T> anyM(Array<T> option) {
        return AnyM.ofSeq(option, array.INSTANCE);
    }
    /**
     * Perform a For Comprehension over a Array, accepting 3 generating functions.
     * This results in a four level nested internal iteration over the provided Publishers.
     *
     *  <pre>
     * {@code
     *
     *   import static cyclops.Arrays.forEach4;
     *
    forEach4(IntArray.range(1,10).boxed(),
    a-> Array.iterate(a,i->i+1).limit(10),
    (a,b) -> Array.<Integer>of(a+b),
    (a,b,c) -> Array.<Integer>just(a+b+c),
    Tuple::tuple)
     *
     * }
     * </pre>
     *
     * @param value1 top level Array
     * @param value2 Nested Array
     * @param value3 Nested Array
     * @param value4 Nested Array
     * @param yieldingFunction  Generates a result per combination
     * @return Array with an element per combination of nested publishers generated by the yielding function
     */
    public static <T1, T2, T3, R1, R2, R3, R> Array<R> forEach4(Array<? extends T1> value1,
                                                               Function<? super T1, ? extends Array<R1>> value2,
                                                               BiFunction<? super T1, ? super R1, ? extends Array<R2>> value3,
                                                               Fn3<? super T1, ? super R1, ? super R2, ? extends Array<R3>> value4,
                                                               Fn4<? super T1, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {


        return value1.flatMap(in -> {

            Array<R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                Array<R2> b = value3.apply(in,ina);
                return b.flatMap(inb -> {
                    Array<R3> c = value4.apply(in,ina,inb);
                    return c.map(in2 -> yieldingFunction.apply(in, ina, inb, in2));
                });

            });

        });

    }

    /**
     * Perform a For Comprehension over a Array, accepting 3 generating function.
     * This results in a four level nested internal iteration over the provided Publishers.
     * <pre>
     * {@code
     *
     *  import static com.aol.cyclops2.reactor.Arrayes.forEach4;
     *
     *  forEach4(IntArray.range(1,10).boxed(),
    a-> Array.iterate(a,i->i+1).limit(10),
    (a,b) -> Array.<Integer>just(a+b),
    (a,b,c) -> Array.<Integer>just(a+b+c),
    (a,b,c,d) -> a+b+c+d <100,
    Tuple::tuple);
     *
     * }
     * </pre>
     *
     * @param value1 top level Array
     * @param value2 Nested Array
     * @param value3 Nested Array
     * @param value4 Nested Array
     * @param filterFunction A filtering function, keeps values where the predicate holds
     * @param yieldingFunction Generates a result per combination
     * @return Array with an element per combination of nested publishers generated by the yielding function
     */
    public static <T1, T2, T3, R1, R2, R3, R> Array<R> forEach4(Array<? extends T1> value1,
                                                                 Function<? super T1, ? extends Array<R1>> value2,
                                                                 BiFunction<? super T1, ? super R1, ? extends Array<R2>> value3,
                                                                 Fn3<? super T1, ? super R1, ? super R2, ? extends Array<R3>> value4,
                                                                 Fn4<? super T1, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                                 Fn4<? super T1, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {


        return value1.flatMap(in -> {

            Array<R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                Array<R2> b = value3.apply(in,ina);
                return b.flatMap(inb -> {
                    Array<R3> c = value4.apply(in,ina,inb);
                    return c.filter(in2->filterFunction.apply(in,ina,inb,in2))
                            .map(in2 -> yieldingFunction.apply(in, ina, inb, in2));
                });

            });

        });
    }

    /**
     * Perform a For Comprehension over a Array, accepting 2 generating function.
     * This results in a three level nested internal iteration over the provided Publishers.
     *
     * <pre>
     * {@code
     *
     * import static Arrays.forEach3;
     *
     * forEach(IntArray.range(1,10).boxed(),
    a-> Array.iterate(a,i->i+1).limit(10),
    (a,b) -> Array.<Integer>of(a+b),
    Tuple::tuple);
     *
     * }
     * </pre>
     *
     *
     * @param value1 top level Array
     * @param value2 Nested Array
     * @param value3 Nested Array
     * @param yieldingFunction Generates a result per combination
     * @return Array with an element per combination of nested publishers generated by the yielding function
     */
    public static <T1, T2, R1, R2, R> Array<R> forEach3(Array<? extends T1> value1,
                                                         Function<? super T1, ? extends Array<R1>> value2,
                                                         BiFunction<? super T1, ? super R1, ? extends Array<R2>> value3,
                                                         Fn3<? super T1, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return value1.flatMap(in -> {

            Array<R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                Array<R2> b = value3.apply(in,ina);
                return b.map(in2 -> yieldingFunction.apply(in, ina, in2));
            });


        });


    }

    /**
     * Perform a For Comprehension over a Array, accepting 2 generating function.
     * This results in a three level nested internal iteration over the provided Publishers.
     * <pre>
     * {@code
     *
     * import static Arrays.forEach;
     *
     * forEach(IntArray.range(1,10).boxed(),
    a-> Array.iterate(a,i->i+1).limit(10),
    (a,b) -> Array.<Integer>of(a+b),
    (a,b,c) ->a+b+c<10,
    Tuple::tuple)
    .toArrayX();
     * }
     * </pre>
     *
     * @param value1 top level Array
     * @param value2 Nested publisher
     * @param value3 Nested publisher
     * @param filterFunction A filtering function, keeps values where the predicate holds
     * @param yieldingFunction Generates a result per combination
     * @return
     */
    public static <T1, T2, R1, R2, R> Array<R> forEach3(Array<? extends T1> value1,
                                                         Function<? super T1, ? extends Array<R1>> value2,
                                                         BiFunction<? super T1, ? super R1, ? extends Array<R2>> value3,
                                                         Fn3<? super T1, ? super R1, ? super R2, Boolean> filterFunction,
                                                         Fn3<? super T1, ? super R1, ? super R2, ? extends R> yieldingFunction) {


        return value1.flatMap(in -> {

            Array<R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                Array<R2> b = value3.apply(in,ina);
                return b.filter(in2->filterFunction.apply(in,ina,in2))
                        .map(in2 -> yieldingFunction.apply(in, ina, in2));
            });



        });
    }

    /**
     * Perform a For Comprehension over a Array, accepting an additonal generating function.
     * This results in a two level nested internal iteration over the provided Publishers.
     *
     * <pre>
     * {@code
     *
     *  import static Arrays.forEach2;
     *  forEach(IntArray.range(1, 10).boxed(),
     *          i -> Array.range(i, 10), Tuple::tuple)
    .forEach(System.out::println);

    //(1, 1)
    (1, 2)
    (1, 3)
    (1, 4)
    ...
     *
     * }</pre>
     *
     * @param value1 top level Array
     * @param value2 Nested publisher
     * @param yieldingFunction Generates a result per combination
     * @return
     */
    public static <T, R1, R> Array<R> forEach2(Array<? extends T> value1,
                                                Function<? super T, Array<R1>> value2,
                                                BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {


        return value1.flatMap(in -> {

            Array<R1> a = value2.apply(in);
            return a.map(in2 -> yieldingFunction.apply(in,  in2));
        });

    }

    /**
     *
     * <pre>
     * {@code
     *
     *   import static Arrays.forEach2;
     *
     *   forEach(IntArray.range(1, 10).boxed(),
     *           i -> Array.range(i, 10),
     *           (a,b) -> a>2 && b<10,
     *           Tuple::tuple)
    .forEach(System.out::println);

    //(3, 3)
    (3, 4)
    (3, 5)
    (3, 6)
    (3, 7)
    (3, 8)
    (3, 9)
    ...

     *
     * }</pre>
     *
     *
     * @param value1 top level Array
     * @param value2 Nested publisher
     * @param filterFunction A filtering function, keeps values where the predicate holds
     * @param yieldingFunction Generates a result per combination
     * @return
     */
    public static <T, R1, R> Array<R> forEach2(Array<? extends T> value1,
                                                Function<? super T, ? extends Array<R1>> value2,
                                                BiFunction<? super T, ? super R1, Boolean> filterFunction,
                                                BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {


        return value1.flatMap(in -> {

            Array<R1> a = value2.apply(in);
            return a.filter(in2->filterFunction.apply(in,in2))
                    .map(in2 -> yieldingFunction.apply(in,  in2));
        });
    }
    public static <T> Active<array,T> allTypeclasses(Array<T> array){
        return Active.of(ArrayKind.widen(array), Instances.definitions());
    }
    public static <T,W2,R> Nested<array,W2,R> mapM(Array<T> array, Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
        Array<Higher<W2, R>> e = array.map(fn);
        ArrayKind<Higher<W2, R>> lk = ArrayKind.widen(e);
        return Nested.of(lk, Arrays.Instances.definitions(), defs);
    }
    /**
     * Companion class for creating Type Class instances for working with Arrays*
     */
    @UtilityClass
    public static class Instances {
        public static InstanceDefinitions<array> definitions() {
            return new InstanceDefinitions<array>() {

                @Override
                public <T, R> Functor<array> functor() {
                    return Instances.functor();
                }

                @Override
                public <T> Pure<array> unit() {
                    return Instances.unit();
                }

                @Override
                public <T, R> Applicative<array> applicative() {
                    return Instances.zippingApplicative();
                }

                @Override
                public <T, R> Monad<array> monad() {
                    return Instances.monad();
                }

                @Override
                public <T, R> Maybe<MonadZero<array>> monadZero() {
                    return Maybe.just(Instances.monadZero());
                }

                @Override
                public <T> Maybe<MonadPlus<array>> monadPlus() {
                    return Maybe.just(Instances.monadPlus());
                }

                @Override
                public <T> Maybe<MonadPlus<array>> monadPlus(Monoid<Higher<array, T>> m) {
                    return Maybe.just(Instances.monadPlus(m));
                }

                @Override
                public <C2, T> Maybe<Traverse<array>> traverse() {
                    return Maybe.just(Instances.traverse());
                }

                @Override
                public <T> Maybe<Foldable<array>> foldable() {
                    return Maybe.just(Instances.foldable());
                }

                @Override
                public <T> Maybe<Comonad<array>> comonad() {
                    return Maybe.none();
                }

                @Override
                public <T> Maybe<Unfoldable<array>> unfoldable() {
                    return Maybe.just(Instances.unfoldable());
                }
            };

        }
        /**
         *
         * Transform a list, mulitplying every element by 2
         *
         * <pre>
         * {@code
         *  ArrayKind<Integer> list = Arrays.functor().map(i->i*2, ArrayKind.widen(Arrays.asArray(1,2,3));
         *
         *  //[2,4,6]
         *
         *
         * }
         * </pre>
         *
         * An example fluent api working with Arrays
         * <pre>
         * {@code
         *   ArrayKind<Integer> list = Arrays.unit()
        .unit("hello")
        .then(h->Arrays.functor().map((String v) ->v.length(), h))
        .convert(ArrayKind::narrowK);
         *
         * }
         * </pre>
         *
         *
         * @return A functor for Arrays
         */
        public static <T,R>Functor<array> functor(){
            BiFunction<ArrayKind<T>,Function<? super T, ? extends R>,ArrayKind<R>> map = Instances::map;
            return General.functor(map);
        }
        /**
         * <pre>
         * {@code
         * ArrayKind<String> list = Arrays.unit()
        .unit("hello")
        .convert(ArrayKind::narrowK);

        //Arrays.asArray("hello"))
         *
         * }
         * </pre>
         *
         *
         * @return A factory for Arrays
         */
        public static <T> Pure<array> unit(){
            return General.<array,T>unit(ArrayKind::of);
        }
        /**
         *
         * <pre>
         * {@code
         * import static com.aol.cyclops.hkt.jdk.ArrayKind.widen;
         * import static com.aol.cyclops.util.function.Lambda.l1;
         * import static java.util.Arrays.asArray;
         *
        Arrays.zippingApplicative()
        .ap(widen(asArray(l1(this::multiplyByTwo))),widen(asArray(1,2,3)));
         *
         * //[2,4,6]
         * }
         * </pre>
         *
         *
         * Example fluent API
         * <pre>
         * {@code
         * ArrayKind<Function<Integer,Integer>> listFn =Arrays.unit()
         *                                                  .unit(Lambda.l1((Integer i) ->i*2))
         *                                                  .convert(ArrayKind::narrowK);

        ArrayKind<Integer> list = Arrays.unit()
        .unit("hello")
        .then(h->Arrays.functor().map((String v) ->v.length(), h))
        .then(h->Arrays.zippingApplicative().ap(listFn, h))
        .convert(ArrayKind::narrowK);

        //Arrays.asArray("hello".length()*2))
         *
         * }
         * </pre>
         *
         *
         * @return A zipper for Arrays
         */
        public static <T,R> Applicative<array> zippingApplicative(){
            BiFunction<ArrayKind< Function<T, R>>,ArrayKind<T>,ArrayKind<R>> ap = Instances::ap;
            return General.applicative(functor(), unit(), ap);
        }
        /**
         *
         * <pre>
         * {@code
         * import static com.aol.cyclops.hkt.jdk.ArrayKind.widen;
         * ArrayKind<Integer> list  = Arrays.monad()
        .flatMap(i->widen(ArrayX.range(0,i)), widen(Arrays.asArray(1,2,3)))
        .convert(ArrayKind::narrowK);
         * }
         * </pre>
         *
         * Example fluent API
         * <pre>
         * {@code
         *    ArrayKind<Integer> list = Arrays.unit()
        .unit("hello")
        .then(h->Arrays.monad().flatMap((String v) ->Arrays.unit().unit(v.length()), h))
        .convert(ArrayKind::narrowK);

        //Arrays.asArray("hello".length())
         *
         * }
         * </pre>
         *
         * @return Type class with monad functions for Arrays
         */
        public static <T,R> Monad<array> monad(){

            BiFunction<Higher<array,T>,Function<? super T, ? extends Higher<array,R>>,Higher<array,R>> flatMap = Instances::flatMap;
            return General.monad(zippingApplicative(), flatMap);
        }
        /**
         *
         * <pre>
         * {@code
         *  ArrayKind<String> list = Arrays.unit()
        .unit("hello")
        .then(h->Arrays.monadZero().filter((String t)->t.startsWith("he"), h))
        .convert(ArrayKind::narrowK);

        //Arrays.asArray("hello"));
         *
         * }
         * </pre>
         *
         *
         * @return A filterable monad (with default value)
         */
        public static <T,R> MonadZero<array> monadZero(){

            return General.monadZero(monad(), ArrayKind.widen(Array.empty()));
        }
        /**
         * <pre>
         * {@code
         *  ArrayKind<Integer> list = Arrays.<Integer>monadPlus()
        .plus(ArrayKind.widen(Arrays.asArray()), ArrayKind.widen(Arrays.asArray(10)))
        .convert(ArrayKind::narrowK);
        //Arrays.asArray(10))
         *
         * }
         * </pre>
         * @return Type class for combining Arrays by concatenation
         */
        public static <T> MonadPlus<array> monadPlus(){
            Monoid<ArrayKind<T>> m = Monoid.of(ArrayKind.widen(Array.empty()), Instances::concat);
            Monoid<Higher<array,T>> m2= (Monoid)m;
            return General.monadPlus(monadZero(),m2);
        }
        /**
         *
         * <pre>
         * {@code
         *  Monoid<ArrayKind<Integer>> m = Monoid.of(ArrayKind.widen(Arrays.asArray()), (a,b)->a.isEmpty() ? b : a);
        ArrayKind<Integer> list = Arrays.<Integer>monadPlus(m)
        .plus(ArrayKind.widen(Arrays.asArray(5)), ArrayKind.widen(Arrays.asArray(10)))
        .convert(ArrayKind::narrowK);
        //Arrays.asArray(5))
         *
         * }
         * </pre>
         *
         * @param m Monoid to use for combining Arrays
         * @return Type class for combining Arrays
         */
        public static <T> MonadPlus<array> monadPlus(Monoid<Higher<array, T>> m){
            Monoid<Higher<array,T>> m2= (Monoid)m;
            return General.monadPlus(monadZero(),m2);
        }
        public static <T> MonadPlus<array> monadPlusK(Monoid<ArrayKind<T>> m){
            Monoid<Higher<array,T>> m2= (Monoid)m;
            return General.monadPlus(monadZero(),m2);
        }
        public static Unfoldable<array> unfoldable(){
            return new Unfoldable<array>() {
                @Override
                public <R, T> Higher<array, R> unfold(T b, Function<? super T, Optional<Tuple2<R, T>>> fn) {
                    return ArrayKind.widen(ReactiveSeq.unfold(b,fn).collect(Array.collector()));

                }
            };
        }
        /**
         * @return Type class for traversables with traverse / sequence operations
         */
        public static <C2,T> Traverse<array> traverse(){

            BiFunction<Applicative<C2>,ArrayKind<Higher<C2, T>>,Higher<C2, ArrayKind<T>>> sequenceFn = (ap, list) -> {

                Higher<C2,ArrayKind<T>> identity = ap.unit(ArrayKind.widen(Array.empty()));

                BiFunction<Higher<C2,ArrayKind<T>>,Higher<C2,T>,Higher<C2,ArrayKind<T>>> combineToArray =   (acc, next) -> ap.apBiFn(ap.unit((a, b) -> ArrayKind.widen(ArrayKind.narrow(a).append(b))),
                        acc,next);

                BinaryOperator<Higher<C2,ArrayKind<T>>> combineArrays = (a, b)-> ap.apBiFn(ap.unit((l1, l2)-> ArrayKind.widen(ArrayKind.narrow(l1).appendAll(l2.narrow()))),a,b); ;

                return ReactiveSeq.fromIterable(ArrayKind.narrow(list))
                        .reduce(identity,
                                combineToArray,
                                combineArrays);


            };
            BiFunction<Applicative<C2>,Higher<array,Higher<C2, T>>,Higher<C2, Higher<array,T>>> sequenceNarrow  =
                    (a,b) -> ArrayKind.widen2(sequenceFn.apply(a, ArrayKind.narrowK(b)));
            return General.traverse(zippingApplicative(), sequenceNarrow);
        }

        /**
         *
         * <pre>
         * {@code
         * int sum  = Arrays.foldable()
        .foldLeft(0, (a,b)->a+b, ArrayKind.widen(Arrays.asArray(1,2,3,4)));

        //10
         *
         * }
         * </pre>
         *
         *
         * @return Type class for folding / reduction operations
         */
        public static <T> Foldable<array> foldable(){
            BiFunction<Monoid<T>,Higher<array,T>,T> foldRightFn =  (m, l)-> ReactiveSeq.fromIterable(ArrayKind.narrow(l)).foldRight(m);
            BiFunction<Monoid<T>,Higher<array,T>,T> foldLeftFn = (m, l)-> ReactiveSeq.fromIterable(ArrayKind.narrow(l)).reduce(m);
            return General.foldable(foldRightFn, foldLeftFn);
        }

        private static  <T> ArrayKind<T> concat(ArrayKind<T> l1, ArrayKind<T> l2){

            return ArrayKind.widen(l1.appendAll(ArrayKind.narrow(l2)));

        }

        private static <T,R> ArrayKind<R> ap(ArrayKind<Function< T, R>> lt, ArrayKind<T> list){
            return ArrayKind.widen(FromCyclopsReact.fromStream(ReactiveSeq.fromIterable(lt.narrow()).zip(list.narrow(), (a, b)->a.apply(b))).toArray());
        }
        private static <T,R> Higher<array,R> flatMap(Higher<array,T> lt, Function<? super T, ? extends  Higher<array,R>> fn){
            return ArrayKind.widen(ArrayKind.narrow(lt).flatMap(fn.andThen(ArrayKind::narrow)));
        }
        private static <T,R> ArrayKind<R> map(ArrayKind<T> lt, Function<? super T, ? extends R> fn){
            return ArrayKind.widen(ArrayKind.narrow(lt).map(in->fn.apply(in)));
        }
    }



}

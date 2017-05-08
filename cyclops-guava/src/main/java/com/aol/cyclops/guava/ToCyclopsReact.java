
package com.aol.cyclops.guava;

import java.util.function.BiFunction;
import java.util.function.Function;


import com.aol.cyclops.guava.GuavaWitness.optional;
import com.aol.cyclops2.types.anyM.AnyMSeq;
import com.aol.cyclops2.types.anyM.AnyMValue;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import cyclops.control.Maybe;
import cyclops.monads.AnyM;
import cyclops.stream.ReactiveSeq;

public class ToCyclopsReact {

    public static <T> Maybe<T> maybe(Optional<T> option) {
        return option.isPresent() ? Maybe.just(option.get()) : Maybe.none();
    }

    public static <T> ReactiveSeq<T> reactiveSeq(Iterable<T> it) {
        return ReactiveSeq.fromIterable(it);
    }








}

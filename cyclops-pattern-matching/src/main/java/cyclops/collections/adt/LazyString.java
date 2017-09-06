package cyclops.collections.adt;


import com.aol.cyclops2.types.Filters;
import cyclops.control.Maybe;
import cyclops.patterns.Sealed2;
import cyclops.stream.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class LazyString implements ImmutableList<Character> {
    private final LazySeq<Character> string;

    private static final LazyString Nil = fromLazyList(LazySeq.empty());
    public static LazyString fromLazyList(LazySeq<Character> string){
        return new LazyString(string);
    }
    public static LazyString of(CharSequence seq){
        return fromLazyList(LazySeq.fromStream( seq.chars().mapToObj(i -> (char) i)));
    }

    public static LazyString empty(){
        return Nil;
    }

    public LazyString op(Function<? super LazySeq<Character>, ? extends LazySeq<Character>> custom){
        return fromLazyList(custom.apply(string));
    }

    public LazyString substring(int start){
        return drop(start);
    }
    public LazyString substring(int start, int end){
        return drop(start).take(end-start);
    }
    public LazyString toUpperCase(){
        return fromLazyList(string.map(c->c.toString().toUpperCase().charAt(0)));
    }
    public LazyString toLowerCase(){
        return fromLazyList(string.map(c->c.toString().toLowerCase().charAt(0)));
    }
    public LazySeq<LazyString> words() {
        return string.split(t -> t.equals(' ')).map(l->fromLazyList(l));
    }
    public LazySeq<LazyString> lines() {
        return string.split(t -> t.equals('\n')).map(l->fromLazyList(l));
    }
    public LazyString mapChar(Function<Character,Character> fn){
        return fromLazyList(string.map(fn));
    }
    public LazyString flatMapChar(Function<Character,LazyString> fn){
        return fromLazyList(string.flatMap(fn.andThen(s->s.string)));
    }
    @Override
    public LazyString filter(Predicate<? super Character> predicate) {
        return fromLazyList(string.filter(predicate));
    }

    @Override
    public <R> ImmutableList<R> map(Function<? super Character, ? extends R> fn) {
        return string.map(fn);
    }

    @Override
    public <R> ImmutableList<R> flatMap(Function<? super Character, ? extends ImmutableList<? extends R>> fn) {
        return  string.flatMap(fn);
    }

    @Override
    public <R> ImmutableList<R> flatMapI(Function<? super Character, ? extends Iterable<? extends R>> fn) {
        return  string.flatMapI(fn);
    }

    @Override
    public <R> R match(Function<? super Some<Character>, ? extends R> fn1, Function<? super None<Character>, ? extends R> fn2) {
        return string.match(fn1,fn2);
    }

    @Override
    public ImmutableList<Character> onEmpty(ImmutableList<Character> value) {
        return string.onEmpty(value);
    }

    @Override
    public ImmutableList<Character> onEmptyGet(Supplier<? extends ImmutableList<Character>> supplier) {
        return string.onEmptyGet(supplier);
    }

    @Override
    public <X extends Throwable> ImmutableList<Character> onEmptyThrow(Supplier<? extends X> supplier) {
        return string.onEmptyThrow(supplier);
    }

    @Override
    public ImmutableList<Character> onEmptySwitch(Supplier<? extends ImmutableList<Character>> supplier) {
        return string.onEmptySwitch(supplier);
    }

    public ReactiveSeq<Character> stream(){
        return string.stream();
    }
    public LazyString take(final int n) {
        return fromLazyList(string.take(n));

    }
    public LazyString  drop(final int num) {
        return fromLazyList(string.drop(num));
    }
    public LazyString  reverse() {
        return fromLazyList(string.reverse());
    }
    public Maybe<Character> get(int pos){
        return string.get(pos);
    }

    @Override
    public Character getOrElse(int pos, Character alt) {
        return string.getOrElse(pos,alt);
    }

    @Override
    public Character getOrElseGet(int pos, Supplier<Character> alt) {
        return string.getOrElseGet(pos,alt);
    }

    public LazyString prepend(Character value){
        return fromLazyList(string.prepend(value));
    }

    @Override
    public LazyString prependAll(Iterable<Character> value) {
        return fromLazyList(string.prependAll(value)) ;
    }

    @Override
    public LazyString append(Character value) {
        return fromLazyList(string.append(value)) ;
    }

    @Override
    public LazyString appendAll(Iterable<Character> value) {
        return fromLazyList(string.appendAll(value)) ;
    }

    public LazyString prependAll(LazyString value){
        return fromLazyList(string.prependAll(value.string));
    }
    public LazyString append(String s){
        return fromLazyList(string.appendAll(LazySeq.fromStream( s.chars().mapToObj(i -> (char) i))));
    }
    public int size(){
        return length();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public int length(){
        return string.size();
    }
    public String toString(){
        return string.stream().join("");
    }

}
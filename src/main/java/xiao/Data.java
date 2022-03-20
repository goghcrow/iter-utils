package xiao;

import lombok.Value;
import xiao.Data.Option.None;
import xiao.Data.Option.Some;

import java.util.*;

import static xiao.Funs.*;
import static xiao.Iterators.*;

/**
 * Data & Data Factory
 * @author chuxiaofeng
 */
@SuppressWarnings("unused")
public interface Data {

    static <A> Ref<A>  Ref(A v)               { return new Ref<>(v);             }
    static <A> Lazy<A> Lazy(A v)              { return new Lazy<>(() -> v);      }
    static <A> Lazy<A> Lazy(Fun0<A> t)        { return new Lazy<>(t);            }

    static <A> None<A>         None()         { return new Option.None<>();      }
    static <A> Some<A>         Some(A value)  { return new Option.Some<>(value); }
    static <L, R> Either<L, R> Left(L value)  { return new Either.Left<>(value); }
    static <L, R> Either<L, R> Right(R value) { return new Either.Right<>(value);}

    static <A>          Monuple  <A>          Tuple(A _1)                   { return Monuple.of(_1);               }
    static <A, B>       Pair     <A, B>       Tuple(A _1, B _2)             { return Pair(_1, _2);                 }
    static <A, B, C>    Triple   <A, B, C>    Tuple(A _1, B _2, C _3)       { return Triple.of(_1, _2, _3);        }
    static <A, B, C, D> Quadruple<A, B, C, D> Tuple(A _1, B _2, C _3, D _4) { return Quadruple.of(_1, _2, _3, _4); }

    static <A, B> Pair<A, B> Pair(A _1, B _2)         { return Pair.of(_1, _2);                  }
    static <A, B> Pair<A, B> Pair(Map.Entry<A, B> it) { return Pair(it.getKey(), it.getValue()); }
    static <A, B, C> PairIter<A, B> PairIter(Map<A, B> m) {
        Iterator<Map.Entry<A, B>> i = m.entrySet().iterator();
        return new PairIter<A, B>() {
            @Override public boolean hasNext() { return i.hasNext(); }
            @Override public Pair<A, B> next() { return Pair(i.next()); }
        };
    }
    @SafeVarargs static <A> Iter<A> Iter(A... xs) {
        if (xs.length == 0) {
            return EmptyIter.get();
        } else if (xs.length == 1) {
            return new SingleIter<>(xs[0]);
        } else {
            return new ArrayIter<>(xs);
        }
    }
    @SafeVarargs static <A> List<A> List(A... xs) {
        if (xs.length == 0) {
            return Collections.emptyList();
        } else if (xs.length == 1) {
            return Collections.singletonList(xs[0]);
        } else {
            return Arrays.asList(xs);
        }
    }
    @SafeVarargs static <A> Set<A> Set(A... xs) {
        if (xs.length == 0) {
            return Collections.emptySet();
        } else {
            return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(xs)));
        }
    }
    @SafeVarargs static <A extends Comparable<? super A>> SortedSet<A> SortedSet(A... xs) {
        return Collections.unmodifiableSortedSet(new TreeSet<>(Arrays.asList(xs)));
    }
    @SafeVarargs static <A, B> Map<A, B> Map(Pair<A, B>... xs) { return Map(Iter(xs)); }
    static <A, B> Map<A, B> Map(Iterable<Pair<A, B>> i) { return Map(i.iterator()); }
    static <A, B> Map<A, B> Map(Iterator<Pair<A, B>> i) {
        Map<A, B> m = new HashMap<>();
        while (i.hasNext()) {
            Pair<A, B> it = i.next();
            m.put(it._1, it._2);
        }
        return Collections.unmodifiableMap(m);
    }
    @SafeVarargs static <A extends Comparable<? super A>, B> SortedMap<A, B> SortedMap(Pair<A, B>... xs) {
        return SortedMap(Iter(xs));
    }
    static <A extends Comparable<? super A>, B> SortedMap<A, B> SortedMap(Iterator<Pair<A, B>> i) {
        SortedMap<A, B> m = new TreeMap<>();
        while (i.hasNext()) {
            Pair<A, B> p = i.next();
            m.put(p._1, p._2);
        }
        return Collections.unmodifiableSortedMap(m);
    }
    static <A extends Comparable<? super A>, B> SortedMap<A, B> SortedMap(Iterable<Pair<A, B>> i) { return SortedMap(i.iterator()); }
    static <K, V> SortedMap<K, V> SortedMap(Iterator<Pair<K, V>> i, Comparator<? super K> cmp) {
        SortedMap<K, V> m = new TreeMap<>(cmp);
        while (i.hasNext()) {
            Pair<K, V> p = i.next();
            m.put(p._1, p._2);
        }
        return Collections.unmodifiableSortedMap(m);
    }
    static <K, V> SortedMap<K, V> SortedMap(Iterable<Pair<K, V>> i, Comparator<? super K> cmp) { return SortedMap(i.iterator(), cmp); }

    // 🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶

    // 也可以直接使用 Atomic*, AtomicReference
    final class Ref<T> {
        T val;
        public Ref(T val) { this.val = val; }
        public T get() { return val; } // unRef
        public void set(T val) { this.val = val; }
    }

    final class Lazy<T> implements Fun0<T> {
        private T val;
        private Fun0<T> thunk;
        private Lazy(Fun0<T> thunk) { this.thunk = Objects.requireNonNull(thunk); }
        @Override public T call() {
            if (thunk != null) {
                val = thunk.call();
                thunk = null;
            }
            return val;
        }
    }

    @Value class Monuple<A> { // tuple1
        public A _1;
        public static <A> Monuple<A> of(A _1) { return new Monuple<>(_1); }
        @Override public String toString() { return "(" + _1 + ")"; }
    }

    // 用 pair 因为字母少... (其实是因为最用用了 Pair, 懒得改了... 改成Tuple1234简单点)
    @Value class Pair<A, B> { // tuple2, couple
        public A _1;
        public B _2;
        public static <A, B> Pair<A, B> of(A _1, B _2) { return new Pair<>(_1, _2); }
        @Override public String toString() { return "(" + _1 + ", " +  _2 + ")"; }
    }

    @Value class Triple<A, B, C> { // tuple3
        public A _1;
        public B _2;
        public C _3;
        public static <A, B, C> Triple<A, B, C> of(A _1, B _2, C _3) { return new Triple<>(_1, _2, _3); }
        @Override public String toString() { return "(" + _1 + ", " +  _2 + ", " + _3 + ")"; }
    }

    @Value class Quadruple<A, B, C, D> { // tuple4
        public A _1;
        public B _2;
        public C _3;
        public D _4;
        public static <A, B, C, D> Quadruple<A, B, C, D> of(A _1, B _2, C _3, D _4) { return new Quadruple<>(_1, _2, _3, _4); }
        @Override public String toString() { return "(" + _1 + ", " +  _2 + ", " + _3 + ", " + _4 + ")"; }
    }

    abstract class Option<A> implements Iterable<A> {
        private Option() { }
        @SuppressWarnings("UnusedReturnValue")
        public <X> X match() throws NoneToken, SomeToken { if (this instanceof None) throw nt; else throw st; }
        private static final NoneToken nt = new NoneToken();
        private static final SomeToken st = new SomeToken();
        public static class NoneToken extends Throwable { private NoneToken() { super(null, null, false, false); } }
        public static class SomeToken extends Throwable { private SomeToken() { super(null, null, false, false); } }
        @SuppressWarnings("Lombok") @Value public static class Some<T> extends Option<T> { public T value; }
        @SuppressWarnings("Lombok") @Value public static class None<T> extends Option<T> { }
        public A get() { if (isEmpty()) throw new NoSuchElementException(); else return get(st); }
        public A get(SomeToken t) { return ((Some<A>)this).value; }
        public boolean isEmpty() { return equals(None()); }
        public<U> Option<U> map(Fun1<A, U> f) { return isEmpty() ? None() : Some(f.call(get(st))); }
        public <B> B fold(Fun0<B> ifEmpty, Fun1<A, B> f) { return map(f).getOrElse(ifEmpty); }
        public A getOrElse(Fun0<A> def) { return isEmpty() ? def.call() : get(st); }
        public boolean isDefined() { return !isEmpty(); }
        @Override public Iterator<A> iterator() { return isEmpty() ? Iter.empty() : Iter(get(st)); }
    }

    abstract class Either<L, R> implements Show {
        private Either() { }
        public <X> X match() throws LeftToken, RightToken { if (this instanceof Left) throw lt; throw rt; }
        private static final LeftToken lt = new LeftToken();
        private static final RightToken rt = new RightToken();
        public static class LeftToken extends Throwable { private LeftToken() { super(null, null, false, false); } }
        public static class RightToken extends Throwable { private RightToken() { super(null, null, false, false); } }
        @SuppressWarnings("Lombok") @Value public static class Left<L, R> extends Either<L, R> { public L value; }
        @SuppressWarnings("Lombok") @Value public static class Right<L, R> extends Either<L, R> { public R value; }
        public L get(LeftToken t) { return ((Left<L, R>)this).value; }
        public R get(RightToken t) { return ((Right<L, R>)this).value; }
        public R getOrElse(R defVal) {
            try { return match(); }
            catch (LeftToken t) { return defVal;    }
            catch (RightToken t) { return get(t);   }
        }
        @Override public String show() {
            try { return match(); }
            catch (LeftToken t) { return Show.show(get(t)); }
            catch (RightToken t) { return Show.show(get(t)); }
        }
    }

    // 绕过 @Value 重写 toString
    interface Show {
        String show();
        static String show(Object o) { return o instanceof Show ? ((Show) o).show() : Objects.toString(o); }
    }
}
package xiao;

import xiao.Funs.*;

import java.util.Comparator;
import java.util.Iterator;

/**
 * @author chuxiaofeng
 */
@SuppressWarnings({"unused", "BooleanMethodIsAlwaysInverted"})
public interface Ord<A> {

//    boolean le(A x, A y);
//    default boolean ge(A x, A y) { return le(y, x); }
//    default boolean lt(A x, A y) { return le(x, y) && !eq(x, y); }
//    default boolean gt(A x, A y) { return ge(x, y) && !eq(x, y); }
//    default boolean eq(A x, A y) { return le(x, y) && le(y, x); }
//    default Comparator<A> toComparator() {
//        return (x, y) -> {
//            if (eq(x, y)) return 0;
//            if (lt(x, y)) return -1;
//            return 1;
//        };
//    }


    static <A extends Comparable<? super A>> boolean le(A x, A y) { return x.compareTo(y) <= 0; }
    static <A extends Comparable<? super A>> boolean ge(A x, A y) { return x.compareTo(y) >= 0; }
    static <A extends Comparable<? super A>> boolean lt(A x, A y) { return x.compareTo(y) < 0; }
    static <A extends Comparable<? super A>> boolean gt(A x, A y) { return x.compareTo(y) > 0; }
    static <A extends Comparable<? super A>> boolean eq(A x, A y) { return x.compareTo(y) == 0; }

    static <A extends Comparable<? super A>, B extends A> B max(B x, B y) { return ge(x, y) ? x : y; }
    static <A extends Comparable<? super A>, B extends A> B min(B x, B y) { return le(x, y) ? x : y; }

    static <A extends Comparable<? super A>> int compare(Iterator<A> a, Iterator<A> b) {
        while (a.hasNext() && b.hasNext()) {
            int r = a.next().compareTo(b.next());
            if (r != 0) return r;
        }
        return Boolean.compare(a.hasNext(), b.hasNext());
    }


//    static <T> Fun2<? super T, ? super T, Boolean> le(Comparator<? super T> cmp) { return (x, y) -> cmp.compare(x, y) <= 0; }
//    static <T> Fun2<? super T, ? super T, Boolean> ge(Comparator<? super T> cmp) { return (x, y) -> cmp.compare(x, y) >= 0; }
//    static <T> Fun2<? super T, ? super T, Boolean> lt(Comparator<? super T> cmp) { return (x, y) -> cmp.compare(x, y) < 0; }
//    static <T> Fun2<? super T, ? super T, Boolean> gt(Comparator<? super T> cmp) { return (x, y) -> cmp.compare(x, y) > 0; }
//    static <T> Fun2<? super T, ? super T, Boolean> eq(Comparator<? super T> cmp) { return (x, y) -> cmp.compare(x, y) == 0; }
//    static <T> Fun2<? super T, ? super T, ? super T> max(Comparator<T> cmp) { return (x, y) -> ge(cmp).call(x, y) ? x : y; }
//    static <T> Fun2<? super T, ? super T, ? super T> min(Comparator<T> cmp) { return (x, y) -> le(cmp).call(x, y) ? x : y; }
//
//    static <T> boolean le(T x, T y, Comparator<? super T> cmp) { return cmp.compare(x, y) <= 0; }
//    static <T> boolean ge(T x, T y, Comparator<? super T> cmp) { return cmp.compare(x, y) >= 0; }
//    static <T> boolean lt(T x, T y, Comparator<? super T> cmp) { return cmp.compare(x, y) < 0; }
//    static <T> boolean gt(T x, T y, Comparator<? super T> cmp) { return cmp.compare(x, y) > 0; }
//    static <T> boolean eq(T x, T y, Comparator<? super T> cmp) { return cmp.compare(x, y) == 0; }
//    static <T, U extends T> U max(U x, U y, Comparator<? super U> cmp) { return ge(x, y, cmp) ? x : y; }
//    static <T, U extends T> U min(U x, U y, Comparator<? super U> cmp) { return le(x, y, cmp) ? x : y; }
//    static <A> int compare(Iterator<A> a, Iterator<A> b, Comparator<A> cmp) {
//        while (a.hasNext() && b.hasNext()) {
//            int r = cmp.compare(a.next(), b.next());
//            if (r != 0) return r;
//        }
//        return Boolean.compare(a.hasNext(), b.hasNext());
//    }
}

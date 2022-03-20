package xiao;

import java.util.Comparator;
import java.util.Iterator;

/**
 * @author chuxiaofeng
 */
@SuppressWarnings("unused")
public interface Ord<A> extends Comparator<A> {
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

    static <T> boolean le(T x, T y, Comparator<T> cmp) { return cmp.compare(x, y) <= 0; }
    static <T> boolean ge(T x, T y, Comparator<T> cmp) { return cmp.compare(x, y) >= 0; }
    static <T> boolean lt(T x, T y, Comparator<T> cmp) { return cmp.compare(x, y) < 0; }
    static <T> boolean gt(T x, T y, Comparator<T> cmp) { return cmp.compare(x, y) > 0; }
    static <T> boolean eq(T x, T y, Comparator<T> cmp) { return cmp.compare(x, y) == 0; }
    static <T, U extends T> U max(U x, U y, Comparator<U> cmp) { return ge(x, y, cmp) ? x : y; }
    static <T, U extends T> U min(U x, U y, Comparator<U> cmp) { return le(x, y, cmp) ? x : y; }
    static <A> int compare(Iterator<A> a, Iterator<A> b, Comparator<A> cmp) {
        while (a.hasNext() && b.hasNext()) {
            int r = cmp.compare(a.next(), b.next());
            if (r != 0) return r;
        }
        return Boolean.compare(a.hasNext(), b.hasNext());
    }
}

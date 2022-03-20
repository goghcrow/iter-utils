package xiao;

import java.util.*;

/**
 * @author chuxiaofeng
 */
public interface Legacy {

    @SuppressWarnings({"unchecked", "rawtypes"})
    static <A> A _fromSpecific(Object a) {
        if (a instanceof LinkedList)         return (A) new LinkedList<>();
        else if (a instanceof List)          return (A) new ArrayList();
        else if (a instanceof LinkedHashSet) return (A) new LinkedHashSet();
        else if (a instanceof SortedSet)     return (A) new TreeSet(((SortedSet) a).comparator());
        else if (a instanceof Set)           return (A) new HashSet();
        else if (a instanceof LinkedHashMap) return (A) new LinkedHashMap();
        else if (a instanceof SortedMap)     return (A) new TreeMap(((SortedMap) a).comparator());
        else if (a instanceof Map)           return (A) new HashMap();
        else throw new UnsupportedOperationException();
    }

    // C[A] -> C[B] where C <: Collection
    static <A extends Collection<B>, B, C, T>
    T _map(A a, Funs.Fun1<? super B, ? extends C> f) {
        Collection<C> a1 = _fromSpecific(a);
        Iterator<C> i = IterFuns.map(a.iterator(), f);
        while (i.hasNext()) a1.add(i.next());
        //noinspection unchecked
        return ((T) a1);
    }

    static <A extends Collection<B>, C extends Collection<D>, B, D, E, T>
    T _map(A a, C b, Funs.Fun2<? super B, ? super D, ? extends E> f) {
        Collection<E> a1 = _fromSpecific(a);
        Iterator<E> i = IterFuns.map(a.iterator(), b.iterator(), f);
        while (i.hasNext()) a1.add(i.next());
        //noinspection unchecked
        return ((T) a1);
    }

}

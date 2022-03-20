package xiao;

import xiao.Data.Pair;

import java.util.*;

import static xiao.Helper.*;

/**
 * @author chuxiaofeng
 */
public interface PairIter<A, B> extends Iter<Pair<A, B>> {

    default Map<A, B> toMap() {
        return IterFuns.putAll(new HashMap<>(), this);
    }

    default SortedMap<A, B> toSortedMap(Comparator<? super A> cmp) {
        return IterFuns.putAll(new TreeMap<>(cmp), this);
    }

    static <A extends Comparable<? super A>, B> SortedMap<A, B> toSortedMap(PairIter<A, B> i) {
        return IterFuns.putAll(new TreeMap<>(), i);
    }

}

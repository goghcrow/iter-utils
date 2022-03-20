package xiao;

import java.util.SortedSet;
import java.util.TreeSet;

import static xiao.Helper.*;

/**
 * @author chuxiaofeng
 */
public interface SortedIter<A extends Comparable<? super A>> extends Iter<A> {

    default SortedSet<A> toSortedSet() {
        return IterFuns.addAll(new TreeSet<>(), this);
    }

}

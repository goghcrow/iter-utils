package xiao;

import xiao.Helper.*;
import xiao.Iterators.GroupedIter;

import java.util.*;
import java.util.stream.Stream;

import static xiao.Data.*;
import static xiao.Funs.*;

/**
 * Lazy Once Iterator
 * @author chuxiaofeng
 */
@SuppressWarnings({"BooleanMethodIsAlwaysInverted", "unused"})
public interface Iter<A> extends Iterator<A> {

    // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 Factory 🔴🍇🍉🍌🍋🍊🍐🍎🍏
    static <A> Iter<A>             empty()     { return Iter(); }
    static <A> Iter<A>             single(A a) { return Iter(a); }
    @SafeVarargs
    static <A> Iter<A>             concat(Iterator<? extends A>...xss) { return IterFuns.flatten(xss); }
    @SafeVarargs
    static <A> Iter<A>             concat(Iterable<? extends A>...xss) { return IterFuns.flatten(xss); }
    static <A> Iter<A>             fill(int len, Fun0<? extends A> elem) {
        return new Iter<A>() {
            int i = 0;
            @Override public int knownSize() { return Math.max(len - i, 0); }
            @Override public boolean hasNext() { return i < len; }
            @Override public A next() { if (hasNext()) { i++; return elem.call(); } else { return IterFuns.noSuchElement(); } }
        };
    }
    static <A> Iter<Iter<A>>       fill(int n1, int n2, Fun0<? extends A> elem)         { return fill(n1, () -> fill(n2, elem));     }
    static <A> Iter<Iter<Iter<A>>> fill(int n1, int n2, int n3, Fun0<? extends A> elem) { return fill(n1, () -> fill(n2, n3, elem)); }
    static <A> Iter<A>             tabulate(int end, Fun1<Integer, ? extends A> f) {
        return new Iter<A>() {
            int i = 0;
            @Override public int knownSize() { return Math.max(end - i, 0); }
            @Override public boolean hasNext() { return i < end; }
            @Override public A next() { return hasNext() ? f.call(i++) : IterFuns.noSuchElement(); }
        };
    }
    static <A> Iter<Iter<A>>       tabulate(int n1, int n2, Fun2<Integer, Integer, ? extends A> f) {
        return tabulate(n1, i1 -> tabulate(n2, i2 -> f.call(i1, i2)));
    }
    static <A> Iter<Iter<Iter<A>>> tabulate(int n1, int n2, int n3, Fun3<Integer, Integer, Integer, ? extends A> f) {
        return tabulate(n1, i1 -> tabulate(n2, n3, (i2, i3) -> f.call(i1, i2, i3)));
    }
    static Iter<Integer> from(int start) { return from(start, 1); }
    static Iter<Integer> from(int start, int step) {
        return new Iter<Integer>() {
            int i = start;
            @Override public boolean hasNext() { return true; }
            @Override public Integer next() { int r = i; i += step; return r; }
        };
    }
    static Iter<Long>    from(long start) { return from(start, 1); }
    static Iter<Long>    from(long start, long step) {
        return new Iter<Long>() {
            long i = start;
            @Override public boolean hasNext() { return true; }
            @Override public Long next() { long r = i; i += step; return r; }
        };
    }
    static Iter<Integer> range(int start, int end)              { return range(start, end, 1);                     }
    static Iter<Integer> range(int start, int end, int step)    { return new Iterators.IntRangeIter(start, end, step);   }
    static Iter<Long>    range(long start, long end)            { return range(start, end, 1);                     }
    static Iter<Long>    range(long start, long end, long step) { return new Iterators.LongRangeIter(start, end, step);  }
    static <A> Iter<A>   iterate(A start, int len, Fun1<? super A, ? extends A> f) { return iterate(start, f).take(len); }
    static <A> Iter<A>   iterate(A start, Fun1<? super A, ? extends A> f) {
        // start, f(start), f(f(start)) ...
        return new Iter<A>() {
            boolean first = true;
            A acc = start;
            @Override public boolean hasNext() { return true; }
            @Override public A next() {
                if (first) first = false;
                else acc = f.call(acc);
                return acc;
            }
        };
    }
    static <A, S> Iter<A> unfold(S init, Fun1<? super S, Option<Pair<A, S>>> f) { return new Iterators.UnfoldIter<>(init, f); }
    /**
     * 示例
     * <pre>
     * try (val in = new ByteArrayInputStream("Hello".getBytes())) {
     *      try (val buf = new BufferedInputStream(in)) {
     *          continually(sneaky(() -> buf.read())).takeWhile(i -> i != -1);
     *      }
     * }
     * <pre/>
     */
    static <A> Iter<A> continually(Fun0<A> elem) {
        return new Iter<A>() {
            @Override public boolean hasNext() { return true; }
            @Override public A next() { return elem.call(); }
        };
    }

    // 🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶

    // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 basic 🔴🍇🍉🍌🍋🍊🍐🍎🍏
    default int         knownSize() { return -1;                                }
    default int         size()      { return IterFuns.size(this);            }
    default boolean     isEmpty()   { return IterFuns.isEmpty(this);         }
    default boolean     nonEmpty()  { return IterFuns.nonEmpty(this);        }
    default Option<A>   nextOption(){ return IterFuns.nextOption(this);      }
    default A           head()      { return IterFuns.head(this);            }
    default Option<A>   headOption(){ return IterFuns.headOption(this);      }
    default A           last()      { return IterFuns.last(this);            }
    default Option<A>   lastOption(){ return IterFuns.lastOption(this);      }
    default Iter<A>     init()      { return IterFuns.init(this);            }
    default Iter<A>     tail()      { return IterFuns.tail(this);            }
    default Iter<Iter<A>> inits()   { return IterFuns.inits(this);           }
    default Iter<Iter<A>> tails()   { return IterFuns.tails(this);           }

    // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 翻转 & 去重 🔴🍇🍉🍌🍋🍊🍐🍎🍏
    default Iter<A>     reverse()  { return IterFuns.reverse(this);  }
    default Iter<A>     distinct() { return IterFuns.distinct(this); }
    default <B> Iter<A> distinctBy (Fun1<? super A, ? extends B> f) { return IterFuns.distinctBy(this, f);  }
    default <B> Iter<A> distinctBy1(Fun1<? super A, ? extends B> f) { return IterFuns.distinctBy1(this, f); }

    // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 查找 & predicate 🔴🍇🍉🍌🍋🍊🍐🍎🍏
    default Option<A>   find(Fun1<? super A, Boolean> p)                 { return IterFuns.find(this, p);                }
    default int         indexWhere(Fun1<? super A, Boolean> p, int from) { return IterFuns.indexWhere(this, p, from);    }
    default int         indexOf(A elem)                                  { return IterFuns.indexOf(this, elem);          }
    default int         indexOf(A elem, int from)                        { return IterFuns.indexOf(this, elem, from);    }
    default boolean     forall(Fun1<? super A, Boolean> p)               { return IterFuns.forall(this, p);              }
    default boolean     exists(Fun1<? super A, Boolean> p)               { return IterFuns.exists(this, p);              }
    default boolean     contains(Object elem)                            { return IterFuns.contains(this, elem);         }
    default boolean     sameElements(Iterator<? extends A> those)        { return IterFuns.sameElements(this, those); }
    default boolean     sameElements(Iterable<? extends A> those)        { return IterFuns.sameElements(this, those); }
    default <B> boolean corresponds (Iterator<? extends B> those, Fun2<? super A, ? super B, Boolean> p) { return IterFuns.corresponds(this, those, p); }
    default <B> boolean corresponds (Iterable<? extends B> those, Fun2<? super A, ? super B, Boolean> p) { return IterFuns.corresponds(this, those, p);    }

    // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 映射 & 过滤 🔴🍇🍉🍌🍋🍊🍐🍎🍏
    default <B> Iter<B> map(Fun1<? super A, ? extends B> f)   { return IterFuns.map(this, f);       }
    default <B> Iter<B> flatMap(Fun1<? super A, Iterator<? extends B>> f)  { return IterFuns.flatMap(this, f);  }
    default <B> Iter<B> flatMap1(Fun1<? super A, Iterator<? extends B>> f) { return IterFuns.flatMap1(this, f); }
    default <B> Iter<B> flatMap2(Fun1<? super A, Iterator<? extends B>> f) { return IterFuns.flatMap2(this, f); }
    default Iter<A>     filter(Fun1<? super A, Boolean> p)    { return IterFuns.filter(this, p);    }
    default Iter<A>     filter1(Fun1<? super A, Boolean> p)   { return IterFuns.filter1(this, p);   }
    default Iter<A>     filter2(Fun1<? super A, Boolean> p)   { return IterFuns.filter2(this, p);   }
    default Iter<A>     filterNot(Fun1<? super A, Boolean> p) { return IterFuns.filterNot(this, p); }
    default <B> Iter<B> collect(PartialFun<? super A, ? extends B> pf) { return IterFuns.collect(this, pf);             }
    default <B> Option<B> collectFirst(PartialFun<? super A, ? extends B> pf) { return IterFuns.collectFirst(this, pf); }

    // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 更新 & 替换 🔴🍇🍉🍌🍋🍊🍐🍎🍏
    default Iter<A>     update(int index, A elem) { return IterFuns.update(this, index, elem); }
    default Iter<A>     patch(int from, Iterator<? extends A> patchElems, int replaced) { return IterFuns.patch(this, from, patchElems, replaced); }
    default Iter<A>     patch(int from, Iterable<? extends A> patchElems, int replaced) { return IterFuns.patch(this, from, patchElems, replaced); }

    // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 [条件/范围]裁剪 🔴🍇🍉🍌🍋🍊🍐🍎🍏
    default Iter<A>     remove(A el)                    { return IterFuns.remove(this, el);         }
    default Iter<A>     removeAll(Iterator<A> b)        { return IterFuns.removeAll(this, b);       }
    default Iter<A>     removeAll(Iterable<A> b)        { return IterFuns.removeAll(this, b);       }
    default Iter<A>     slice(int from, int until)      { return IterFuns.slice(this, from, until); }
    default Iter<A>     take(int n)                           { return IterFuns.take(this, n);      }
    default Iter<A>     takeRight(int n)                      { return IterFuns.takeRight(this, n); }
    default Iter<A>     takeWhile(Fun1<? super A, Boolean> p) { return IterFuns.takeWhile(this, p); }
    default Iter<A>     drop(int n)                           { return IterFuns.drop(this, n);      }
    default Iter<A>     dropRight(int n)                      { return IterFuns.dropRight(this, n); }
    default Iter<A>     dropWhile(Fun1<? super A, Boolean> p) { return IterFuns.dropWhile(this, p); }

    // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 拼接 & 补齐 🔴🍇🍉🍌🍋🍊🍐🍎🍏
    default Iter<A>     concat(Iterator<? extends A> suffix) { return IterFuns.concat(this, suffix);    }
    default Iter<A>     concat1(Iterator<? extends A> suffix){ return IterFuns.concat1(this, suffix);   }
    default Iter<A>     concat(Iterable<? extends A> suffix) { return IterFuns.concat(this, suffix);    }
    default Iter<A>     append(A elem)                       { return IterFuns.append(this, elem);      }
    default Iter<A>     prepend(A elem)                      { return IterFuns.prepend(this, elem);     }
    default Iter<A>     padTo(int len, A elem)               { return IterFuns.padTo(this, len, elem); }

    // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 zip & unzip & transpose 🔴🍇🍉🍌🍋🍊🍐🍎🍏
    default <B> Iter<Pair<A, B>>       zip(Iterator<B> that) { return IterFuns.zip(this, that);    }
    default <B> Iter<Pair<A, B>>       zip(Iterable<B> that) { return IterFuns.zip(this, that);    }
    default     Iter<Pair<A, Integer>> zipWithIndex()        { return IterFuns.zipWithIndex(this); }
    default     Iter<Pair<A, A>>       zipAll(Iterator<? extends A> that, A thisElem, A thatElem) { return IterFuns.zipAll(this, that, thisElem, thatElem); }
    default     Iter<Pair<A, A>>       zipAll(Iterable<? extends A> that, A thisElem, A thatElem) { return IterFuns.zipAll(this, that, thisElem, thatElem); }
    default <A1, A2> Pair<Iter<A1>, Iter<A2>> unzip(Fun1<? super A, Pair<A1, A2>> asPair) { return IterFuns.unzip(this, asPair); }
    default <A1, A2, A3> Triple<Iter<A1>, Iter<A2>, Iter<A3>> unzip3(Fun1<? super A, Triple<A1, A2, A3>> asTriple) { return IterFuns.unzip3(this, asTriple); }

    // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 分组[映射/聚合] 🔴🍇🍉🍌🍋🍊🍐🍎🍏
    default GroupedIter<A> grouped(int sz)           { return IterFuns.grouped(this, sz);       }
    default GroupedIter<A> sliding(int sz)           { return IterFuns.sliding(this, sz);       }
    default GroupedIter<A> sliding(int sz, int step) { return IterFuns.sliding(this, sz, step); }
    default <K>    Map<K, List<A>>  groupBy(Fun1<? super A, ? extends K> f) { return IterFuns.groupBy(this, f); }
    default <K, B> Map<K, List<B>>  groupMap(Fun1<? super A, ? extends K> key, Fun1<? super A, ? extends B> val) { return IterFuns.groupMap(this, key, val); }
    default <K, B> Map<K, B>        groupMapReduce(Fun1<? super A, ? extends K> key,
                                                   Fun1<? super A, ? extends B> val,
                                                   Fun2<? super B, ? super B, ? extends B> reduce) { return IterFuns.groupMapReduce(this, key, val, reduce); }

    // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 [条件|Either|下标]分区 🔴🍇🍉🍌🍋🍊🍐🍎🍏
    default <A1, A2> Pair<Iter<A1>, Iter<A2>> partitionMap(Fun1<? super A, Either<A1, A2>> f) { return IterFuns.partitionMap(this, f); }
    default Pair<Iter<A>, Iter<A>>            partition(Fun1<? super A, Boolean> p)   { return IterFuns.partition(this, p);         }
    default Pair<Iter<A>, Iter<A>>            span(Fun1<? super A, Boolean> p)        { return IterFuns.span(this, p);              }
    default Pair<Iter<A>, Iter<A>>            span1(Fun1<? super A, Boolean> p)       { return IterFuns.span1(this, p);             }
    default Pair<Iter<A>, Iter<A>>            splitAt(int n)                          { return IterFuns.splitAt(this, n);           }

    // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 聚合: scan 🔴🍇🍉🍌🍋🍊🍐🍎🍏
    default <B> Iter<B> scan(B z, Fun2<? super B, ? super A, ? extends B> op)         { return IterFuns.scan(this, z, op);          }
    default <B> Iter<B> scanLeft(B z, Fun2<? super B, ? super A, ? extends B> op)     { return IterFuns.scanLeft(this, z, op);      }
    default <B> Iter<B> scanRight(B z, Fun2<? super A, ? super B, ? extends B> op)    { return IterFuns.scanRight(this, z, op);     }

    // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 聚合: fold 🔴🍇🍉🍌🍋🍊🍐🍎🍏
    default <B> B       aggregate(B z, Fun2<? super B, ? super A, ? extends B> op)    { return IterFuns.aggregate(this, z, op);     }
    default <B> B       foldLeft(B z, Fun2<? super B, ? super A, ? extends B> op)     { return IterFuns.foldLeft(this, z, op);      }
    default <B> B       foldRight(B z, Fun2<? super A, ? super B, ? extends B> op)    { return IterFuns.foldRight(this, z, op);     }
    default <B> B foldRight1(B z, Fun2<? super A, ? super B, ? extends B> op)         { return IterFuns.foldRight1(this, z, op);    }
    default <B> B       fold(B z, Fun2<? super B, ? super A, ? extends B> op)         { return IterFuns.fold(this, z, op);          }

    // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 聚合: reduce 🔴🍇🍉🍌🍋🍊🍐🍎🍏
    default A           reduce(Fun2<? super A, ? super A, ? extends A> op)            { return IterFuns.reduce(this, op);           }
    default Option<A>   reduceOption(Fun2<? super A, ? super A, ? extends A> op)      { return IterFuns.reduceOption(this, op);     }
    default A           reduceLeft(Fun2<? super A, ? super A, ? extends A> op)        { return IterFuns.reduceLeft(this, op);       }
    default A           reduceRight(Fun2<? super A, ? super A, ? extends A> op)       { return IterFuns.reduceRight(this, op);      }
    default Option<A>   reduceLeftOption(Fun2<? super A, ? super A, ? extends A> op)  { return IterFuns.reduceLeftOption(this, op); }
    default Option<A>   reduceRightOption(Fun2<? super A, ? super A, ? extends A> op) { return IterFuns.reduceRightOption(this, op);}

    // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 聚合: join 🔴🍇🍉🍌🍋🍊🍐🍎🍏
    default StringBuilder addString(StringBuilder b, CharSequence pre, CharSequence del, CharSequence suf) { return IterFuns.addString(this, b, pre, del, suf); }
    default StringBuilder addString(StringBuilder b, CharSequence del) { return IterFuns.addString(this, b, del); }
    default StringBuilder addString(StringBuilder b) { return IterFuns.addString(this, b); }
    default String        mkString(CharSequence pre, CharSequence del, CharSequence suf) { return IterFuns.mkString(this, pre, del, suf); }
    default String        mkString(CharSequence del) { return IterFuns.mkString(this, del); }
    default String        mkString() { return IterFuns.mkString(this); }

    // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 聚合: count & sum 🔴🍇🍉🍌🍋🍊🍐🍎🍏
    default int         count(Fun1<? super A, Boolean> p) { return IterFuns.count(this, p); }

    // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 聚合: compare 🔴🍇🍉🍌🍋🍊🍐🍎🍏
    default int           sizeCompare(int otherSize)              { return IterFuns.sizeCompare(this, otherSize); }
    default int           sizeCompare(Iter<? extends A> that)     { return IterFuns.sizeCompare(this, that);      }
    default int           sizeCompare(Iterable<? extends A> that) { return IterFuns.sizeCompare(this, that);      }
    default A             min(Comparator<A> ord)                  { return IterFuns.min(this, ord);               }
    default Option<A>     minOption(Comparator<A> ord)            { return IterFuns.minOption(this, ord);         }
    default A             max(Comparator<A> ord)                  { return IterFuns.max(this, ord);               }
    default Option<A>     maxOption(Comparator<A> ord)            { return IterFuns.maxOption(this, ord);         }
    default <B> A         minBy(Fun1<? super A, ? extends B> f, Comparator<B> ord)       { return IterFuns.minBy(this, f, ord);       }
    default <B> Option<A> minByOption(Fun1<? super A, ? extends B> f, Comparator<B> ord) { return IterFuns.minByOption(this, f, ord); }
    default <B> A         maxBy(Fun1<? super A, ? extends B> f, Comparator<B> ord)       { return IterFuns.maxBy(this, f, ord);       }
    default <B> Option<A> maxByOption(Fun1<? super A, ? extends B> f, Comparator<B> ord) { return IterFuns.maxByOption(this, f, ord); }

    // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 特殊: 复制 🔴🍇🍉🍌🍋🍊🍐🍎🍏
    default Pair<Iter<A>, Iter<A>> duplicate() { return IterFuns.duplicate(this); }

    // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 sideEffect 🔴🍇🍉🍌🍋🍊🍐🍎🍏
    // default <U> Iter<A> tapEach(Fun1<? super A, ? extends U> f) { return IterFuns.tapEach(this, f); }
    default Iter<A>     tapEach(Act1<? super A> f)    { return IterFuns.tapEach(this, f); }
    default void        foreach(Act1<? super A> f)    { IterFuns.foreach(this, f);        }
    default <U> void    foreach(Fun1<? super A, U> f) { IterFuns.foreach(this, f);        }
    default void        println()                     { Helper.println(toList());            }

    // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 toXXX 🔴🍇🍉🍌🍋🍊🍐🍎🍏
    // 注意: 之所以不返回 () -> this; 是因为 iterable.iterator() 每次返回一个新的迭代器
    default Iterable<A> toIterable()    { return IterFuns.toIterable(this); }
    default List<A>     toList()        { return IterFuns.toList(this);     }
    default A[]         toArray(A[] a)  { return IterFuns.toArray(this, a); }
    default Set<A>      toSet()         { return IterFuns.toSet(this);      }
    default SortedSet<A>toSortedSet(Comparator<? super A> cmp) { return IterFuns.toSortedSet(this, cmp); }
    default Stream<A>   toStream()      { return IterFuns.toStream(this);   }
    default BufferedIter<A> buffered()  { return IterFuns.buffered(this);   }
}

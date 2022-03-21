package xiao;

import lombok.val;
import xiao.Iterators.*;

import java.util.*;
import java.util.stream.Stream;

import static xiao.Data.*;
import static xiao.Funs.*;

/**
 * @author chuxiaofeng
 */
@SuppressWarnings("unused")
public interface IterFuns {

    static <A> Iter<A> iter(Iterator<? extends A> i) { return map(i, a -> a); }
    static <A> Iter<A> iter(Iterable<? extends A> i) { return iter(i.iterator()); }

    static <A> A noSuchElement() { return Data.<A>Iter().next(); }

    // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 basic 🔴🍇🍉🍌🍋🍊🍐🍎🍏
    static int knownSize(Iterator<?> i) { return i instanceof Iter ? ((Iter<?>) i).knownSize() : -1; }
    static <A> int size(Iterator<? extends A> i) {
        int l = 0;
        while (i.hasNext()) {
            l++; i.next();
        }
        return l;
    }
    static <A> int size(Iterable<? extends A> i) { return size(i.iterator()); }
    static <A> boolean isEmpty(Iterator<? extends A> i) { return !i.hasNext(); }
    static <A> boolean isEmpty(Iterable<? extends A> i) { return isEmpty(i.iterator()); }
    static <A> boolean nonEmpty(Iterator<? extends A> i) { return !isEmpty(i); }
    static <A> boolean nonEmpty(Iterable<? extends A> i) { return nonEmpty(i.iterator()); }
    static <A> Option<A> nextOption(Iterator<? extends A> i) { return i.hasNext() ? Some(i.next()) : None(); }
    static <A> Option<A> nextOption(Iterable<? extends A> i) { return nextOption(i.iterator()); }
    static <A> A head(Iterator<? extends A> i) { return i.next(); }
    static <A> A head(Iterable<? extends A> i) { return head(i.iterator()); }
    static <A> Option<A> headOption(Iterator<? extends A> i) { return isEmpty(i) ? None() : Some(i.next()); }
    static <A> Option<A> headOption(Iterable<? extends A> i) { return headOption(i.iterator()); }
    static <A> A last(Iterator<? extends A> i) {
        A a = i.next();
        while (i.hasNext()) a = i.next();
        return a;
    }
    static <A> A last(Iterable<? extends A> i) { return last(i.iterator()); }
    static <A> Option<A> lastOption(Iterator<? extends A> i) { return isEmpty(i) ? None() : Some(last(i)); }
    static <A> Option<A> lastOption(Iterable<? extends A> i) { return lastOption(i.iterator()); }
    static <A> Iter<A> init(Iterator<? extends A> i) {
        if (isEmpty(i)) throw new UnsupportedOperationException();
        return dropRight(i, 1);
    }
    static <A> Iter<A> init(Iterable<? extends A> i) { return init(i.iterator()); }
    static <A> Iter<A> tail(Iterator<? extends A> i) {
        if (isEmpty(i)) throw new UnsupportedOperationException();
        return drop(i, 1);
    }
    static <A> Iter<A> tail(Iterable<? extends A> i) { return tail(i.iterator()); }
    static <A> Iter<Iter<A>> inits(Iterator<? extends A> i) {
        // iterate(iter(i), Iter::init).takeWhile(Iter::nonEmpty).concat(Iter(Iter.empty()));
        // start, f(start), f(f(start)) ...
        Iter<A> start = iter(i);
        return new Iter<Iter<A>>() {
            boolean first = true;
            Iter<A> acc = start;
            @Override public boolean hasNext() { return !acc.isEmpty(); }
            @Override public Iter<A> next() {
                if (first) first = false;
                Pair<Iter<A>, Iter<A>> dup = IterFuns.duplicate(acc);
                acc = dup._1.init();
                return dup._2;
            }
        }.concat(Iter(Iter.empty()));
    }
    static <A> Iter<Iter<A>> inits(Iterable<? extends A> i) { return inits(i.iterator()); }
    static <A> Iter<Iter<A>> tails(Iterator<? extends A> i) {
        // iterate(iter(i), Iter::tail).takeWhile(Iter::nonEmpty).concat(Iter(Iter.empty()));
        // start, f(start), f(f(start)) ...
        Iter<A> start = iter(i);
        return new Iter<Iter<A>>() {
            boolean first = true;
            Iter<A> acc = start;
            @Override public boolean hasNext() { return !acc.isEmpty(); }
            @Override public Iter<A> next() {
                if (first) first = false;
                Pair<Iter<A>, Iter<A>> dup = IterFuns.duplicate(acc);
                acc = dup._1.tail();
                return dup._2;
            }
        }.concat(Iter(Iter.empty()));
    }
    static <A> Iter<Iter<A>> tails(Iterable<? extends A> i) { return tails(i.iterator()); }

    // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 翻转 & 去重 🔴🍇🍉🍌🍋🍊🍐🍎🍏
    static <A> Iter<A> reverse(Iterator<? extends A> i) { return new ReverseIter<>(i); }
    static <A> Iter<A> reverse(Iterable<? extends A> i) { return reverse(i.iterator()); }
    static <A> Iter<A> distinct(Iterator<? extends A> a) { return new DistinctByIter<>(a, it -> it); }
    static <A> Iter<A> distinct(Iterable<? extends A> a) { return distinct(a.iterator()); }
    static <A, B> Iter<A> distinctBy(Iterator<? extends A> a, Fun1<? super A, ? extends B> f) { return new DistinctByIter<>(a, f); }
    static <A, B> Iter<A> distinctBy(Iterable<? extends A> a, Fun1<? super A, ? extends B> f) { return distinctBy(a.iterator(), f); }
    static <A, B> Iter<A> distinctBy1(Iterator<? extends A> a, Fun1<? super A, ? extends B> f) { return new DistinctByIter1<>(a, f); }

    // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 查找 & predicate 🔴🍇🍉🍌🍋🍊🍐🍎🍏
    static <A> Option<A> find(Iterator<? extends A> i, Fun1<? super A, Boolean> p) {
        while (i.hasNext()) {
            A a = i.next();
            if (p.call(a)) return Some(a);
        }
        return None();
    }
    static <A> Option<A> find(Iterable<? extends A> i, Fun1<? super A, Boolean> p) { return find(i.iterator(), p); }
    static <A> int indexWhere(Iterator<? extends A> i, Fun1<? super A, Boolean> p, int from) {
        int idx = Math.max(0, from);
        i = drop(i, from);
        while (i.hasNext()) {
            if (p.call(i.next())) return idx;
            idx += 1;
        }
        return -1;
    }
    static <A> int indexWhere(Iterable<? extends A> i, Fun1<? super A, Boolean> p, int from) { return indexWhere(i.iterator(), p, from); }
    static <A> int indexOf(Iterator<? extends A> i, A elem) { return indexOf(i, elem, 0); }
    static <A> int indexOf(Iterable<? extends A> i, A elem) { return indexOf(i.iterator(), elem); }
    static <A> int indexOf(Iterator<? extends A> i, A elem, int from) { return indexWhere(i, a -> Objects.equals(a, elem), from); }
    static <A> int indexOf(Iterable<? extends A> i, A elem, int from) { return indexOf(i.iterator(), elem, from); }
    static <A> boolean forall(Iterator<? extends A> i, Fun1<? super A, Boolean> p) {
        boolean r = true;
        while (r && i.hasNext()) r = p.call(i.next());
        return r;
    }
    static <A> boolean forall(Iterable<? extends A> i, Fun1<? super A, Boolean> p) { return forall(i.iterator(), p); }
    static <A> boolean exists(Iterator<? extends A> i, Fun1<? super A, Boolean> p) {
        boolean r = false;
        while (!r && i.hasNext()) {
            r = p.call(i.next());
        }
        return r;
    }
    static <A> boolean exists(Iterable<? extends A> i, Fun1<? super A, Boolean> p) { return exists(i.iterator(), p); }
    static <A> boolean contains(Iterator<? extends A> i, Object elem) { return exists(i, a -> Objects.equals(a, elem)); }
    static <A> boolean contains(Iterable<? extends A> i, Object elem) { return contains(i.iterator(), elem); }
    static <A> boolean sameElements(Iterator<? extends A> these, Iterator<? extends A> those) { return corresponds(these, those, Objects::equals); }
    static <A> boolean sameElements(Iterable<? extends A> these, Iterator<? extends A> those) { return sameElements(these.iterator(), those); }
    static <A> boolean sameElements(Iterator<? extends A> these, Iterable<? extends A> those) { return sameElements(these, those.iterator()); }
    static <A> boolean sameElements(Iterable<? extends A> these, Iterable<? extends A> those) { return sameElements(these.iterator(), those.iterator()); }
    static <A, B> boolean corresponds(Iterator<? extends A> these, Iterator<? extends B> those, Fun2<? super A, ? super B, Boolean> p) {
        while (these.hasNext() && those.hasNext()) {
            if (!p.call(these.next(), those.next())) return false;
        }
        return these.hasNext() == those.hasNext();
    }
    static <A, B> boolean corresponds(Iterator<? extends A> a, Iterable<? extends B> b, Fun2<? super A, ? super B, Boolean> p) { return corresponds(a, b.iterator(), p); }
    static <A, B> boolean corresponds(Iterable<? extends A> a, Iterator<? extends B> b, Fun2<? super A, ? super B, Boolean> p) { return corresponds(a.iterator(), b, p); }
    static <A, B> boolean corresponds(Iterable<? extends A> a, Iterable<? extends B> b, Fun2<? super A, ? super B, Boolean> p) { return corresponds(a.iterator(), b.iterator(), p); }

    // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 映射 & 过滤 🔴🍇🍉🍌🍋🍊🍐🍎🍏
    static <A, B> Iter<B> map(Iterator<? extends A> i, Fun1<? super A, ? extends B> f) {
        return new Iter<B>() {
            @SuppressWarnings("rawtypes")
            @Override public int knownSize() { return i instanceof Iter ? ((Iter) i).knownSize() : -1; }
            @Override public boolean hasNext() { return i.hasNext(); }
            @Override public B next() { return f.call(i.next()); }
        };
    }
    static <A, B, C> Iter<C> map(Iterator<? extends A> a, Iterator<? extends B> b, Fun2<? super A, ? super B, ? extends C> f) {
        return new Iter<C>() {
            @SuppressWarnings("rawtypes")
            @Override public int knownSize() {
                if (a instanceof Iter && b instanceof Iter) {
                    int sz1 = ((Iter) a).knownSize();
                    int sz2 = ((Iter) b).knownSize();
                    return Math.min(sz1, sz2);
                } else {
                    return -1;
                }
            }
            @Override  public boolean hasNext() { return a.hasNext() && b.hasNext(); }
            @Override public C next() { return f.call(a.next(), b.next()); }
        };
    }
    static <A, B> Iter<B> map(Iterable<? extends A> i, Fun1<? super A, ? extends B> f) { return map(i.iterator(), f); }
    static <A, B, C> Iter<C> map(Iterable<? extends A> a, Iterable<? extends B> b, Fun2<? super A, ? super B, ? extends C> f) { return map(a.iterator(), b.iterator(), f); }
    static <A, B, C> Iter<C> map(Iterator<? extends A> a, Iterable<? extends B> b, Fun2<? super A, ? super B, ? extends C> f) { return map(a, b.iterator(), f); }
    static <A, B, C> Iter<C> map(Iterable<? extends A> a, Iterator<? extends B> b, Fun2<? super A, ? super B, ? extends C> f) { return map(a.iterator(), b, f); }
    // todo
    // static <A, B> Iter<B> flatten(Iterator<? extends A> i, Fun1<? super A, Iterator<? extends B>> asIter) { }
    // static <A, B> Iter<B> flatten(Iterable<? extends A> i, Fun1<? super A, Iterator<? extends B>> asIter) { return flatten(i.iterator(), asIter); }
    @SafeVarargs static <A> Iter<A> flatten(Iterator<? extends A>... is) { return Iter(is).foldLeft(Iter(), IterFuns::concat); }
    @SafeVarargs static <A> Iter<A> flatten1(Iterator<? extends A>... is) { return flatten(Iter(is)); }
    @SafeVarargs static <A> Iter<A> flatten2(Iterator<? extends A>... is) { return flatten1(Iter(is)); }
    @SafeVarargs  static <A> Iter<A> flatten(Iterable<? extends A>... is) { return flatten(map(Iter(is), Iterable::iterator)); }
    static <A> Iter<A> flatten(Iterator<? extends Iterator<? extends A>> is) { return new FlattenIter<>(is); }
    static <A> Iter<A> flatten1(Iterator<? extends Iterator<? extends A>> is) { return new FlattenIter1<>(is); }
    static <A, B> Iter<B> flatMap(Iterator<? extends A> i, Fun1<? super A, Iterator<? extends B>> f) { return flatten(map(i, f)); }
    static <A, B> Iter<B> flatMap(Iterable<? extends A> i, Fun1<? super A, Iterable<? extends B>> f) { return flatten(map(i, a -> f.call(a).iterator())); }
    static <A, B> Iter<B> flatMap1(Iterator<? extends A> i, Fun1<? super A, Iterator<? extends B>> f) { return flatten1(map(i, f)); }
    static <A, B> Iter<B> flatMap2(Iterator<? extends A> i, Fun1<? super A, Iterator<? extends B>> f) { return flatten(map(i, f)); }
    static <A> Iter<A> filter(Iterator<? extends A> i, Fun1<? super A, Boolean> p) {
        /*if (i instanceof FilterIter) {
            FilterIter<A> fi = (FilterIter<A>) i;
            if (!fi.isFlipped) {
                return new FilterIter<>(fi.i, a -> fi.p.call(a) && p.call(a), false);
            }
        }*/
        return new FilterIter<>(i, p, false);
    }
    static <A> Iter<A> filter(Iterable<? extends A> i, Fun1<? super A, Boolean> p) { return filter(i.iterator(), p); }
    static <A> Iter<A> filter1(Iterator<? extends A> i, Fun1<? super A, Boolean> p) { return new FilterIter1<>(i, p, false); }
    static <A> Iter<A> filter2(Iterator<? extends A> i, Fun1<? super A, Boolean> p) { return new FilterIter2<>(i, p, false); }
    static <A> Iter<A> filterNot(Iterator<? extends A> i, Fun1<? super A, Boolean> p) { return new FilterIter<>(i, p, true); }
    static <A> Iter<A> filterNot(Iterable<? extends A> i, Fun1<? super A, Boolean> p) { return filterNot(i.iterator(), p); }
    static <A, B> Iter<B> collect(Iterator<? extends A> i, PartialFun<? super A, ? extends B> pf) {
        return new CollectIter<>(i, pf);
    }
    static <A, B> Iter<B> collect(Iterable<? extends A> i, PartialFun<? super A, ? extends B> pf) { return collect(i.iterator(), pf); }
    static <A, B> Option<B> collectFirst(Iterator<? extends A> i, PartialFun<? super A, ? extends B> pf) {
        while (i.hasNext()) {
            B x = pf.callOrElse(i.next(), PartialFun.checkFallback());
            if (!PartialFun.fallbackOccurred(x)) {
                return Some(x);
            }
        }
        return None();
    }
    static <A, B> Option<B> collectFirst(Iterable<? extends A> i, PartialFun<? super A, ? extends B> pf) { return collectFirst(i.iterator(), pf); }

    // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 更新 & 替换 🔴🍇🍉🍌🍋🍊🍐🍎🍏
    static <A> Iter<A> update(Iterator<? extends A> it, int index, A elem) {
        return new Iter<A>() {
            int i = 0;
            @Override public int knownSize() { return IterFuns.knownSize(it);}
            @Override public boolean hasNext() {
                if (it.hasNext()) return true;
                else if (index >= i) throw new IndexOutOfBoundsException("" + index);
                else return false;
            }
            @Override public A next() {
                A v = it.next();
                return i++ == index ? elem : v;
            }
        };
    }
    static <A> Iter<A> update(Iterable<? extends A> i, int index, A elem) { return update(i.iterator(), index, elem); }
    static <A> Iter<A> patch(Iterator<? extends A> i, int from, Iterator<? extends A> patchElems, int replaced) {
        return new PatchIter<>(i, from, patchElems, replaced);
    }
    static <A> Iter<A> patch(Iterable<? extends A> i, int from, Iterator<? extends A> patchElems, int replaced) {
        return patch(i.iterator(), from, patchElems, replaced);
    }
    static <A> Iter<A> patch(Iterator<? extends A> i, int from, Iterable<? extends A> patchElems, int replaced) {
        return patch(i, from, patchElems.iterator(), replaced);
    }
    static <A> Iter<A> patch(Iterable<? extends A> i, int from, Iterable<? extends A> patchElems, int replaced) {
        return patch(i.iterator(), from, patchElems, replaced);
    }

    // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 [条件/范围]裁剪 🔴🍇🍉🍌🍋🍊🍐🍎🍏
    static <A> Iter<A> remove(Iterator<? extends A> i, A el) { return filter(i, a -> !Objects.equals(a, el)); }
    static <A> Iter<A> remove(Iterable<? extends A> i, A el) { return remove(i.iterator(), el); }
    static <A> Iter<A> removeAll(Iterator<A> a, Iterator<A> b) { return iter(foldLeft(b, a, IterFuns::remove)); }
    static <A> Iter<A> removeAll(Iterator<A> a, Iterable<A> b) { return removeAll(a, b.iterator()); }
    static <A> Iter<A> removeAll(Iterable<A> a, Iterator<A> b) { return removeAll(a.iterator(), b); }
    static <A> Iter<A> slice(Iterator<? extends A> i, int from, int until) {
        int lo = Math.max(from, 0);
        int rest;
        if (until < 0) {
            rest = -1; // unbounded
        } else if (until <= lo) {
            rest = 0; // empty
        } else {
            rest = until - lo; // finite
        }
        if (rest == 0) {
            return Iter.empty();
        } else {
            return new SliceIter<>(i, lo, rest);
        }
    }
    static <A> Iter<A> slice(Iterable<? extends A> i, int from, int until) { return slice(i.iterator(), from, until); }
    static <A> Iter<A> take(Iterator<? extends A> i, int n) { return slice(i, 0, Math.max(n, 0)); }
    static <A> Iter<A> take(Iterable<? extends A> i, int n) { return take(i.iterator(), n); }
    static <A> Iter<A> takeRight(Iterator<? extends A> i, int n) {
        int k = knownSize(i);
        if (k == 0 || n <= 0) {
            return Iter.empty();
        } else if (n == Integer.MAX_VALUE) {
            return new Iter<A>() {
                @Override public boolean hasNext() { return i.hasNext(); }
                @Override public A next() { return i.next(); }
            };
        } else if (k > 0) {
            return drop(i, Math.max(k - n, 0));
        } else {
            return new TakeRightIter<>(i, n);
        }
    }
    static <A> Iter<A> takeRight(Iterable<? extends A> i, int n) { return takeRight(i.iterator(), n); }
    static <A> Iter<A> takeWhile(Iterator<? extends A> i, Fun1<? super A, Boolean> p) { return new TakeWhileFilter<>(i, p); }
    static <A> Iter<A> takeWhile(Iterable<? extends A> i, Fun1<? super A, Boolean> p) { return takeWhile(i.iterator(), p); }
    static <A> Iter<A> drop(Iterator<? extends A> i, int n) { return slice(i, n, -1); }
    static <A> Iter<A> drop(Iterable<? extends A> i, int n) { return drop(i.iterator(), n); }
    static <A> Iter<A> dropRight(Iterator<? extends A> i, int n) {
        if (n <= 0) {
            return new Iter<A>() {
                @Override public boolean hasNext() { return i.hasNext(); }
                @Override public A next() { return i.next(); }
            };
        } else {
            int k = knownSize(i);
            if (k >= 0) {
                return take(i, k - n);
            } else {
                return new DropRightIter<>(i, n);
            }
        }
    }
    static <A> Iter<A> dropRight(Iterable<? extends A> i, int n) { return dropRight(i.iterator(), n); }
    static <A> Iter<A> dropWhile(Iterator<? extends A> i, Fun1<? super A, Boolean> p) { return new DropWhileFilter<>(i, p); }
    static <A> Iter<A> dropWhile(Iterable<? extends A> i, Fun1<? super A, Boolean> p) { return dropWhile(i.iterator(), p); }

    // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 拼接 & 补齐 🔴🍇🍉🍌🍋🍊🍐🍎🍏
    static <A> Iter<A> concat(Iterator<? extends A> i, Iterator<? extends A> suffix) { return new ConcatIter<A>(i).concat(suffix); }
    static <A> Iter<A> concat1(Iterator<? extends A> i, Iterator<? extends A> suffix) { return flatten(i, suffix); }
    static <A> Iter<A> concat(Iterable<? extends A> i, Iterator<? extends A> suffix) { return concat(i.iterator(), suffix); }
    static <A> Iter<A> concat(Iterator<? extends A> i, Iterable<? extends A> suffix) { return concat(i, suffix.iterator()); }
    static <A> Iter<A> concat(Iterable<? extends A> i, Iterable<? extends A> suffix) { return concat(i.iterator(), suffix); }
    static <A> Iter<A> append(Iterator<? extends A> i, A elem) { return concat(i, Iter.single(elem)); }
    static <A> Iter<A> append(Iterable<? extends A> i, A elem) { return append(i.iterator(), elem); }
    static <A> Iter<A> prepend(Iterator<? extends A> i, A elem) { return concat(Iter.single(elem), i); }
    static <A> Iter<A> prepend(Iterable<? extends A> i, A elem) { return prepend(i.iterator(), elem); }
    static <A> Iter<A> padTo(Iterator<? extends A> it, int len, A elem) {
        return new Iter<A>() {
            int i = 0;
            @Override public int knownSize() {
                int k = IterFuns.knownSize(it);
                return k >= 0 ? Math.max(k, len - this.i) : k;
            }
            @Override public boolean hasNext() { return it.hasNext() || i < len; }
            @Override public A next() {
                if (it.hasNext()) {
                    i += 1;
                    return it.next();
                } else if (i < len) {
                    i += 1;
                    return elem;
                } else {
                    return noSuchElement();
                }
            }
        };
    }
    static <A> Iter<A> padTo(Iterable<? extends A> it, int len, A elem) { return padTo(it.iterator(), len, elem); }

    // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 zip & unzip & transpose 🔴🍇🍉🍌🍋🍊🍐🍎🍏
    static <A, B> Iter<Pair<A, B>> zip(Iterator<? extends A> i, Iterator<B> that) {
        return new Iter<Pair<A, B>>() {
            @Override public int knownSize() { return Math.min(IterFuns.knownSize(i), IterFuns.knownSize(that)); }
            @Override public boolean hasNext() { return i.hasNext() && that.hasNext(); }
            @Override public Pair<A, B> next() { return Pair(i.next(), that.next()); }
        };
    }
    static <A, B> Iter<Pair<A, B>> zip(Iterable<? extends A> i, Iterator<B> that) { return zip(i.iterator(), that); }
    static <A, B> Iter<Pair<A, B>> zip(Iterator<? extends A> i, Iterable<B> that) { return zip(i, that.iterator()); }
    static <A, B> Iter<Pair<A, B>> zip(Iterable<? extends A> i, Iterable<B> that) { return zip(i.iterator(), that); }
    static <A> Iter<Pair<A, Integer>> zipWithIndex(Iterator<? extends A> i) {
        return new Iter<Pair<A, Integer>>() {
            int idx = 0;
            @Override public int knownSize() { return IterFuns.knownSize(i); }
            @Override public boolean hasNext() { return i.hasNext(); }
            @Override public Pair<A, Integer> next() { return Pair(i.next(), idx++); }
        };
    }
    static <A> Iter<Pair<A, Integer>> zipWithIndex(Iterable<? extends A> i) { return zipWithIndex(i.iterator()); }
    static <A, B> Iter<Pair<A, B>> zipAll(Iterator<? extends A> these, Iterator<? extends B> those, A thisElem, B thatElem) {
        return new Iter<Pair<A, B>>() {
            @Override public int knownSize() {
                int k1 = IterFuns.knownSize(these);
                int k2 = IterFuns.knownSize(those);
                if (k1 < 0 || k2 < 0) return -1;
                else return Math.max(k1, k2);
            }
            @Override public boolean hasNext() { return these.hasNext() || those.hasNext(); }
            @Override public Pair<A, B> next() {
                if (hasNext()) {
                    return Pair(
                            these.hasNext() ? these.next() : thisElem,
                            those.hasNext() ? those.next() : thatElem
                    );
                } else {
                    return noSuchElement();
                }
            }
        };
    }
    static <A, B> Iter<Pair<A, B>> zipAll(Iterable<? extends A> these, Iterator<? extends B> those, A thisElem, B thatElem) { return zipAll(these.iterator(), those, thisElem, thatElem); }
    static <A, B> Iter<Pair<A, B>> zipAll(Iterator<? extends A> these, Iterable<? extends B> those, A thisElem, B thatElem) { return zipAll(these, those.iterator(), thisElem, thatElem); }
    static <A, B> Iter<Pair<A, B>> zipAll(Iterable<? extends A> these, Iterable<? extends B> those, A thisElem, B thatElem) { return zipAll(these.iterator(), those, thisElem, thatElem); }
    static <A, A1, A2> Pair<Iter<A1>, Iter<A2>> unzip(Iterator<? extends A> i, Fun1<? super A, Pair<A1, A2>> asPair) {
        Pair<Iter<A>, Iter<A>> dup = duplicate(i);
        Iter<A1> _1 = map(dup._1, __ -> asPair.call(__)._1);
        Iter<A2> _2 = map(dup._2, __ -> asPair.call(__)._2);
        return Tuple(_1, _2);
    }
    static <A, A1, A2> Pair<Iter<A1>, Iter<A2>> unzip(Iterable<? extends A> i, Fun1<? super A, Pair<A1, A2>> asPair) { return unzip(i.iterator(), asPair); }
    static <A1, A2> Pair<Iter<A1>, Iter<A2>> unzip(Iterator<Pair<A1, A2>> i) { return unzip(i, a -> a); }
    static <A1, A2> Pair<Iter<A1>, Iter<A2>> unzip(Iterable<Pair<A1, A2>> i) { return unzip(i.iterator(), a -> a); }
    static <A, A1, A2, A3> Triple<Iter<A1>, Iter<A2>, Iter<A3>> unzip3(Iterator<? extends A> i, Fun1<? super A, Triple<A1, A2, A3>> asTriple) {
        Pair<Iter<A>, Iter<A>> dup = duplicate(i);
        Pair<Iter<A>, Iter<A>> dup1 = duplicate(dup._2);
        Iter<A1> _1 = map(dup._1, __ -> asTriple.call(__)._1);
        Iter<A2> _2 = map(dup1._1, __ -> asTriple.call(__)._2);
        Iter<A3> _3 = map(dup1._2, __ -> asTriple.call(__)._3);
        return Tuple(_1, _2, _3);
    }
    static <A, A1, A2, A3> Triple<Iter<A1>, Iter<A2>, Iter<A3>> unzip3(Iterable<? extends A> i, Fun1<? super A, Triple<A1, A2, A3>> asTriple) { return unzip3(i.iterator(), asTriple); }
    static <A1, A2, A3> Triple<Iter<A1>, Iter<A2>, Iter<A3>> unzip3(Iterator<Triple<A1, A2, A3>> i) { return unzip3(i, a -> a); }
    static <A1, A2, A3> Triple<Iter<A1>, Iter<A2>, Iter<A3>> unzip3(Iterable<Triple<A1, A2, A3>> i) { return unzip3(i.iterator(), a -> a); }
    static <A, B> Iter<Iter<B>> transpose(Iterator<? extends Iterator<? extends B>> it) {
        if (!it.hasNext()) return Iter.empty();

        List<List<B>> bs = null;
        int headSz = -1;
        while (it.hasNext()) {
            Iterator<? extends B> xs = it.next();
            if (headSz == -1) {
                val d = duplicate(xs);
                headSz = size(d._1);
                bs = new ArrayList<>(headSz);
                for (int i = 0; i < headSz; i++) {
                    bs.add(new ArrayList<>());
                }
                xs = d._2;
            }
            int i = 0;
            while (xs.hasNext()) {
                B x = xs.next();
                if (i >= headSz) throw new IllegalArgumentException("iter 必须 sz 相等");
                bs.get(i).add(x);
                i += 1;
            }
            if (i != headSz) throw new IllegalArgumentException("iter 必须 sz 相等");
        }
        return map(bs, IterFuns::iter);
    }
    static <A, B> Iter<Iter<B>> transpose(Iterable<? extends Iterable<? extends B>> i) { return transpose(map(i, IterFuns::iter)); }
    // static <A, B> Iter<Iter<B>> transpose(Iterator<? extends Iterable<? extends B>> i) { }
    // static <A, B> Iter<Iter<B>> transpose(Iterable<? extends Iterable<? extends B>> i) { }

    // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 分组[映射/聚合] 🔴🍇🍉🍌🍋🍊🍐🍎🍏
    static <A> GroupedIter<A> grouped(Iterator<? extends A> i, int sz) { return new GroupedIter<>(i, sz, sz); }
    static <A> GroupedIter<A> grouped(Iterable<? extends A> i, int sz) { return grouped(i.iterator(), sz); }
    static <A> GroupedIter<A> sliding(Iterator<? extends A> i, int sz) { return new GroupedIter<>(i, sz, 1); }
    static <A> GroupedIter<A> sliding(Iterable<? extends A> i, int sz) { return sliding(i.iterator(), sz); }
    static <A> GroupedIter<A> sliding(Iterator<? extends A> i, int sz, int step) { return new GroupedIter<>(i, sz, step); }
    static <A> GroupedIter<A> sliding(Iterable<? extends A> i, int sz, int step) { return sliding(i.iterator(), sz, step); }
    static <A, K> Map<K, List<A>> groupBy(Iterator<? extends A> i, Fun1<? super A, ? extends K> key) {
        Map<K, List<A>> m = new HashMap<>();
        while (i.hasNext()) {
            A v = i.next();
            K k = key.call(v);
            m.computeIfAbsent(k, __ -> new ArrayList<>()).add(v);
        }
        return m;
    }
    static <A, K> Map<K, List<A>> groupBy(Iterable<? extends A> i, Fun1<? super A, ? extends K> f) { return groupBy(i.iterator(), f); }
    static <A, K, B> Map<K, List<B>> groupMap(Iterator<? extends A> i, Fun1<? super A, ? extends K> key, Fun1<? super A, ? extends B> f) {
        Map<K, List<B>> m = new HashMap<>();
        while (i.hasNext()) {
            A el = i.next();
            K k = key.call(el);
            B v = f.call(el);
            m.computeIfAbsent(k, __ -> new ArrayList<>()).add(v);
        }
        return m;
    }
    static <A, K, B> Map<K, List<B>> groupMap(Iterable<? extends A> i,
                                              Fun1<? super A, ? extends K> key,
                                              Fun1<? super A, ? extends B> f) {return groupMap(i.iterator(), key, f); }
    static <K, A, B> Map<K, B> groupMapReduce(Iterator<? extends A> i,
                                              Fun1<? super A, ? extends K> key,
                                              Fun1<? super A, ? extends B> f,
                                              Fun2<? super B, ? super B, ? extends B> reduce) {
        Map<K, B> m = new HashMap<>();
        while (i.hasNext()) {
            A el = i.next();
            K k = key.call(el);
            B v = f.call(el);
            if (m.containsKey(k)) {
                B curry = m.get(k);
                v = reduce.call(curry, v);
            }
            m.put(k, v);
        }
        return Collections.unmodifiableMap(m);
    }
    static <K, A, B> Map<K, B> groupMapReduce(Iterable<? extends A> i,
                                              Fun1<? super A, ? extends K> key,
                                              Fun1<? super A, ? extends B> val,
                                              Fun2<? super B, ? super B, ? extends B> reduce) {
        return groupMapReduce(i.iterator(), key, val, reduce);
    }

    // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 [条件|Either|下标]分区 🔴🍇🍉🍌🍋🍊🍐🍎🍏
    static <A, A1, A2> Pair<Iter<A1>, Iter<A2>> partitionMap(Iterator<? extends A> i, Fun1<? super A, Either<A1, A2>> f) {
        val dup = duplicate(i);
        Iter<A1> left = new LeftPartitionIter<>(dup._1, f);
        Iter<A2> right = new RightPartitionIter<>(dup._2, f);
        return Tuple(left, right);
    }
    static <A, A1, A2> Pair<Iter<A1>, Iter<A2>> partitionMap(Iterable<? extends A> i, Fun1<? super A, Either<A1, A2>> f) {
        return partitionMap(i.iterator(), f);
    }
    static <A> Pair<Iter<A>, Iter<A>> partition(Iterator<? extends A> i, Fun1<? super A, Boolean> p) {
        val dup = duplicate(i);
        Iter<A> fst = new FilterIter<>(dup._1, p, false);
        Iter<A> snd = new FilterIter<>(dup._2, p, true);
        return Tuple(fst, snd);
    }
    static <A> Pair<Iter<A>, Iter<A>> partition(Iterable<? extends A> i, Fun1<A, Boolean> p) { return partition(i.iterator(), p); }
    // 调用完 span 不应该使用原来旧的 i
    // 用 predicate 把 iter 分成两部分
    static <A> Pair<Iter<A>, Iter<A>> span(Iterator<? extends A> i, Fun1<? super A, Boolean> p) {
        // 更高效的实现
        class Leading implements Iter<A> {
            // 因为 Trailing 被 split 之后可能先被使用, 所以需要一个 buf 来存下来 leading 的元素
            Queue<A> lookAhead = null;
            // Leading 遇到 predicate = false 就停
            // predicate 要判断, 就会多读取一个 Trailing 的元素, 这里表示 Trailing 的 header
            A hd;
            // 1 下一个元素已经保存在 hd, 仍旧继续读
            // 0 继续读, 没有发现下一个元素
            // -1 已经迭代完, 只能依赖 lookAhead
            // -2 已经迭代完, 且为另一个迭代器保存 hd 作为其第一个元素
            int status = 0;
            void store(A a) {
                if (lookAhead == null) lookAhead = new ArrayDeque<>();
                lookAhead.add(a);
            }
            A trailer() { return hd; }
            // Trailing 有没有 hd 可以用, 可不可以调用 trailer
            boolean finish() {
                switch (status) {
                    case -2:
                        // Leading 已经启动, 已经把持续 predicate = true 消费完, 且给 Trailing 的 hd 已经存储
                        status = -1; // 标记 Leading 结束, 只能依赖 lookAhead
                        return true;
                    case -1: // i 已经迭代完, 没 hd
                        return false;
                    case 1:
                        store(hd);
                        status = 0; // 0 表示中间状态, 需要继续处理
                        return finish();
                    case 0:
                        // 这个分支表示表示
                        // 1. Leading 已经启动, 但是没迭代到 predicate = false
                        // 2. Leading 没启动
                        // 不管 1 还是 2, Trailing 启动时, 都需要把 predicate = true 的 Leading 的元素存起来跳过
                        // 找到 Trailing 的 hd 暂停
                        status = -1;
                        while (i.hasNext()) {
                            A a = i.next();
                            if (p.call(a)) {
                                store(a);
                            } else {
                                hd = a;
                                return true;
                            }
                        }
                        // predicate 全部 = true, Trailing 没元素
                        return false;
                    default:
                        return false;
                }
            }
            @Override public boolean hasNext() {
                if (status < 0) {
                    return lookAhead != null && !lookAhead.isEmpty();
                } else if (status > 0) {
                    // 1, 继续迭代
                    return true;
                } else { // == 0
                    if (i.hasNext()) {
                        hd = i.next();
                        status = p.call(hd) ? 1 : -2;
                    } else {
                        status = -1;
                    }
                    return status > 0;
                }
            }
            @Override public A next() {
                if (hasNext()) {
                    if (status == 1) {
                        status = 0;
                        return hd;
                    } else {
                        return lookAhead.remove();
                    }
                } else {
                    return noSuchElement();
                }
            }
        }
        Leading leading = new Leading();

        class Trailing implements Iter<A> {
            Leading myLeading = leading;
            // -1 初始状态
            // 0 leading 中有一个元素可用
            // 1 使用 i
            // 2 i.hasNext 已经准备好
            // 3 已经迭代完
            int status = -1;
            // -1 是 初始状态, hasNext 会将初始状态确定为：要么是使用 leading的 hd(0), 要么是 i 的状态(1)
            @Override public boolean hasNext() {
                // 0,2,3 都是幂等状态
                switch (status) {
                    case 3: return false;
                    case 2: return true;
                    case 1: // 1 是中间状态, 不知道结果, 要需要继续处理
                        if (i.hasNext()) {
                            status = 2;
                            return true;
                        } else {
                            status = 3;
                            return false;
                        }
                    case 0: return true;
                    default: // -1
                        // finish 一定要 hd 可用
                        if (myLeading.finish()) {
                            // finish 不是幂等的, 状态改成 0
                            status = 0;
                            return true;
                        } else {
                            status = 1;
                            myLeading = null;
                            return hasNext();
                        }
                }
            }
            @Override public A next() {
                if (hasNext()) {
                    // 只有两种情况,
                    if (status == 0) { // 0 表示有 hd 可以用
                        status = 1; // hd 用完, 使用 i 继续迭代
                        A r = myLeading.trailer();
                        myLeading = null; // 只用 hd, 用完拉到, gc
                        return r;
                    } else {
                        status = 1; // 使用 i 继续迭代
                        return i.next();
                    }
                } else {
                    return noSuchElement();
                }
            }
        }
        Trailing trailing = new Trailing();

        return Pair(leading, trailing);
    }
    static <A> Pair<Iter<A>, Iter<A>> span1(Iterator<? extends A> i, Fun1<? super A, Boolean> p) {
        val dup = duplicate(i);
        return Pair(takeWhile(dup._1, p), dropWhile(dup._2, p));
    }
    static <A> Pair<Iter<A>, Iter<A>> span(Iterable<? extends A> i, Fun1<? super A, Boolean> p) { return span(i.iterator(), p); }
    static <A> Pair<Iter<A>, Iter<A>> splitAt(Iterator<? extends A> i, int n) {
        val d = duplicate(i);
        return Tuple(take(d._1, n), drop(d._2, n));
    }
    static <A> Pair<Iter<A>, Iter<A>> splitAt(Iterable<? extends A> i, int n) { return splitAt(i.iterator(), n); }

    // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 聚合: scan 🔴🍇🍉🍌🍋🍊🍐🍎🍏
    static <A, B> Iter<B> scan(Iterator<? extends A> i, B z, Fun2<? super B, ? super A, ? extends B> op) { return scanLeft(i, z, op); }
    static <A, B> Iter<B> scan(Iterable<? extends A> i, B z, Fun2<? super B, ? super A, ? extends B> op) { return scan(i.iterator(), z, op); }
    static <A, B> Iter<B> scanLeft(Iterator<? extends A> i, B z, Fun2<? super B, ? super A, ? extends B> op) { return new ScanLeftIter<>(i, z, op); }
    static <A, B> Iter<B> scanLeft(Iterable<? extends A> i, B z, Fun2<? super B, ? super A, ? extends B> op) { return scanLeft(i.iterator(), z, op); }
    static <A, B> Iter<B> scanRight(Iterator<? extends A> i, B z, Fun2<? super A, ? super B, ? extends B> op) { return scanLeft(reverse(i), z, (b, a) -> op.call(a, b)); }
    static <A, B> Iter<B> scanRight(Iterable<? extends A> i, B z, Fun2<? super A, ? super B, ? extends B> op) { return scanRight(i.iterator(), z, op); }

    // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 聚合: fold 🔴🍇🍉🍌🍋🍊🍐🍎🍏
    // (1, 2, 3)  -->  op(op(op(z, 1), 2), 3)
    static <A, B> B aggregate(Iterator<? extends A> i, B z, Fun2<? super B, ? super A, ? extends B> op) { return foldLeft(i, z, op); }
    static <A, B> B aggregate(Iterable<? extends A> i, B z, Fun2<? super B, ? super A, ? extends B> op) { return foldLeft(i, z, op); }
    static <A, B> B foldLeft(Iterator<? extends A> i, B z, Fun2<? super B, ? super A, ? extends B> op) {
        B result = z;
        while (i.hasNext()) {
            result = op.call(result, i.next());
        }
        return result;
    }
    static <A, B> B foldLeft(Iterable<? extends A> i, B z, Fun2<? super B, ? super A, ? extends B> op) { return foldLeft(i.iterator(), z, op); }
    // op 参数顺序与 foldLeft 不一样参考展开形式
    // foldLeft  (1, 2, 3)  -->  op(op(op(z, 1), 2), 3)
    // foldRight (1, 2, 3)  -->  op(1, op(2, op(3, z)))
    static <A, B> B foldRight(Iterator<? extends A> i, B z, Fun2<? super A, ? super B, ? extends B> op) {
        return reverse(i).foldLeft(z, (b, a) -> op.call(a, b));
    }
    static <A, B> B foldRight(Iterable<? extends A> i, B z, Fun2<? super A, ? super B, ? extends B> op) { return foldRight(i.iterator(), z, op); }
    static <A, B> B foldRight1(Iterator<? extends A> i, B z, Fun2<? super A, ? super B, ? extends B> op) {
        return new Object() { B go() { return i.hasNext() ? op.call(i.next(), go()) : z; } }.go();
    }
    static <A, B> B foldRightRec(Iterable<? extends A> i, B z, Fun2<? super A, ? super B, ? extends B> op) { return foldRight1(i.iterator(), z, op); }
    static <A, B> B fold(Iterator<? extends A> i, B z, Fun2<? super B, ? super A, ? extends B> op) { return foldLeft(i, z, op); }
    static <A, B> B fold(Iterable<? extends A> i, B z, Fun2<? super B, ? super A, ? extends B> op) { return foldLeft(i, z, op); }

    // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 聚合: reduce 🔴🍇🍉🍌🍋🍊🍐🍎🍏
    static <A> A reduce(Iterator<? extends A> i, Fun2<? super A, ? super A, ? extends A> op) { return reduceLeft(i, op); }
    static <A> A reduce(Iterable<? extends A> i, Fun2<? super A, ? super A, ? extends A> op) { return reduce(i.iterator(), op); }
    static <A> Option<A> reduceOption(Iterator<? extends A> i, Fun2<? super A, ? super A, ? extends A> op) { return reduceLeftOption(i, op); }
    static <A> Option<A> reduceOption(Iterable<? extends A> i, Fun2<? super A, ? super A, ? extends A> op) { return reduceOption(i.iterator(), op); }
    static <A> A reduceLeft(Iterator<? extends A> i, Fun2<? super A, ? super A, ? extends A> op) {
        if (isEmpty(i)) throw new UnsupportedOperationException("empty.reduceLeft");
        return foldLeft(i, i.next(), op);
    }
    static <A> A reduceLeft(Iterable<? extends A> i, Fun2<? super A, ? super A, ? extends A> op) { return reduceLeft(i.iterator(), op); }
    static <A> A reduceRight(Iterator<? extends A> i, Fun2<? super A, ? super A, ? extends A> op) {
        if (isEmpty(i)) throw new UnsupportedOperationException("empty.reduceRight");
        // return reverse(i).reduceLeft((a, b) -> op.call(b, a));
        return foldRight(i, i.next(), op);
    }
    static <A> A reduceRight(Iterable<? extends A> i, Fun2<? super A, ? super A, ? extends A> op) { return reduceRight(i.iterator(), op); }
    static <A> Option<A> reduceLeftOption(Iterator<? extends A> i, Fun2<? super A, ? super A, ? extends A> op) {
        if (isEmpty(i)) {
            return None();
        } else {
            return Some(reduceLeft(i, op));
        }
    }
    static <A> Option<A> reduceLeftOption(Iterable<? extends A> i, Fun2<? super A, ? super A, ? extends A> op) { return reduceLeftOption(i.iterator(), op); }
    static <A> Option<A> reduceRightOption(Iterator<? extends A> i, Fun2<? super A, ? super A, ? extends A> op) {
        if (isEmpty(i)) {
            return None();
        } else {
            return Some(reduceRight(i, op));
        }
    }
    static <A> Option<A> reduceRightOption(Iterable<? extends A> i, Fun2<? super A, ? super A, ? extends A> op) { return reduceRightOption(i.iterator(), op); }

    // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 聚合: join 🔴🍇🍉🍌🍋🍊🍐🍎🍏
    static <A> StringBuilder addString(Iterator<? extends A> i, StringBuilder b, CharSequence pre, CharSequence del, CharSequence suf) {
        if (pre.length() != 0) b.append(pre);
        if (i.hasNext()) {
            b.append(i.next());
            while (i.hasNext()) b.append(del).append(i.next());
        }
        if (suf.length() != 0) b.append(suf);
        return b;
    }
    static <A> StringBuilder addString(Iterator<? extends A> i, StringBuilder b, CharSequence del) {
        return addString(i, b, "", del, "");
    }
    static <A> StringBuilder addString(Iterator<? extends A> i, StringBuilder b) { return addString(i, b, ""); }
    static <A> StringBuilder addString(Iterable<? extends A> i, StringBuilder b, CharSequence pre, CharSequence del, CharSequence suf) { return addString(i.iterator(), b, pre, del, suf); }
    static <A> StringBuilder addString(Iterable<? extends A> i, StringBuilder b, CharSequence del) { return addString(i, b, "", del, "");}
    static <A> StringBuilder addString(Iterable<? extends A> i, StringBuilder b) { return addString(i, b, ""); }
    static <A> String mkString(Iterator<? extends A> i, CharSequence pre, CharSequence del, CharSequence suf) {
        StringBuilder b = new StringBuilder();
        if (isEmpty(i)) return b.append(pre).append(suf).toString();
        return addString(i, b, pre, del, suf).toString();
    }
    static <A> String mkString(Iterable<? extends A> i, CharSequence pre, CharSequence del, CharSequence suf) { return mkString(i.iterator(), pre, del, suf); }
    static <A> String mkString(Iterable<? extends A> i, CharSequence del) { return mkString(i, "", del, ""); }
    static <A> String mkString(Iterator<? extends A> i, CharSequence del) { return mkString(i, "", del, ""); }
    static <A> String mkString(Iterable<? extends A> i) { return mkString(i, ""); }
    static <A> String mkString(Iterator<? extends A> i) { return mkString(i, ""); }

    // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 聚合: count & sum 🔴🍇🍉🍌🍋🍊🍐🍎🍏
    static <A> int count(Iterator<? extends A> i, Fun1<? super A, Boolean> p) {
        int c = 0;
        while (i.hasNext()) {
            if (p.call(i.next())) c++;
        }
        return c;
    }
    static <A> int count(Iterable<? extends A> i, Fun1<? super A, Boolean> p) { return count(i.iterator(), p); }
    static Integer sumInt(Iterator<Integer> i) { return reduceOption(i, Integer::sum).getOrElse(() -> 0); }
    static Integer sumInt(Iterable<Integer> i) { return sumInt(i.iterator()); }
    static Double  sumDouble(Iterator<Double> i) { return reduceOption(i, Double::sum).getOrElse(() -> 0.0); }
    static Double  sumDouble(Iterable<Double> i) { return sumDouble(i.iterator()); }
    static Integer productInt(Iterator<Integer> i) { return reduceOption(i, (r, a) -> r * a).getOrElse(() -> 0); }
    static Integer productInt(Iterable<Integer> i) { return productInt(i.iterator()); }
    static Double  productDouble(Iterator<Double> i) { return reduceOption(i, (r, a) -> r * a).getOrElse(() -> 0.0); }
    static Double  productDouble(Iterable<Double> i) { return productDouble(i.iterator()); }

    // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 聚合: compare 🔴🍇🍉🍌🍋🍊🍐🍎🍏
    static <A> int     sizeCompare(Iterator<? extends A> i, int otherSize) {
        if (otherSize < 0) return 1;
        else {
            int k = knownSize(i);
            if (k >= 0) return Integer.compare(k, otherSize);
            else {
                int sz = 0;
                while (i.hasNext()) {
                    if (sz == otherSize) return 1;
                    i.next();
                    sz += 1;
                }
                return sz - otherSize;
            }
        }
    }
    static <A> int     sizeCompare(Iterable<? extends A> i, int otherSize) { return sizeCompare(i.iterator(), otherSize); }
    static <A, B> int  sizeCompare(Iterator<? extends A> i, Iterator<? extends A> that) {
        int thatK = knownSize(that);
        if (thatK >= 0) return sizeCompare(i, thatK);
        else {
            int k = knownSize(i);
            if (k >= 0) {
                int r = sizeCompare(that, k);
                // -Integer.MIN_VALUE = Integer.MIN_VALUE
                return r == Integer.MIN_VALUE ? 1 : -r;
            } else {
                while (i.hasNext() && that.hasNext()) {
                    i.next();
                    that.next();
                }
                return Boolean.compare(i.hasNext(), that.hasNext());
            }
        }
    }
    static <A, B> int  sizeCompare(Iterator<? extends A> i, Iterable<? extends A> that) { return sizeCompare(i, that.iterator()); }
    static <A, B> int  sizeCompare(Iterable<? extends A> i, Iterator<? extends A> that) { return sizeCompare(i.iterator(), that); }
    static <A, B> int  sizeCompare(Iterable<? extends A> i, Iterable<? extends A> that) { return sizeCompare(i.iterator(), that); }
    static <A extends B, B> A           min(Iterator<? extends A> i, Comparator<B> ord) {
        if (isEmpty(i)) throw new UnsupportedOperationException("empty.min");
        return reduceLeft(i, (a, b) -> ord.compare(a, b) < 0 ? a : b);
    }
    static <A extends B, B> A           min(Iterable<? extends A> i, Comparator<B> ord) { return min(i.iterator(), ord); }
    static <A extends B, B> Option<A>   minOption(Iterator<? extends A> i, Comparator<B> ord) { if (isEmpty(i)) return None(); else return Some(min(i, ord)); }
    static <A extends B, B> Option<A>   minOption(Iterable<? extends A> i, Comparator<B> ord) { return minOption(i.iterator(), ord); }
    static <A extends B, B> A           max(Iterator<? extends A> i, Comparator<B> ord) {
        if (isEmpty(i)) throw new UnsupportedOperationException("empty.max");
        return reduceLeft(i, (a, b) -> ord.compare(a, b) > 0 ? a : b);
    }
    static <A extends B, B> A           max(Iterable<? extends A> i, Comparator<B> ord) { return max(i.iterator(), ord); }
    static <A extends B, B> Option<A>   maxOption(Iterator<? extends A> i, Comparator<B> ord) { if (isEmpty(i)) return None(); else return Some(max(i, ord)); }
    static <A extends B, B> Option<A>   maxOption(Iterable<? extends A> i, Comparator<B> ord) { return maxOption(i.iterator(), ord); }
    static <A, B> A           minBy(Iterator<? extends A> i, Fun1<? super A, ? extends B> f, Comparator<B> ord) {
        if (isEmpty(i)) throw new UnsupportedOperationException("empty.minBy");
        B minF = null;
        A minElem = null;
        boolean first = true;
        while (i.hasNext()) {
            A elem = i.next();
            B fx = f.call(elem);
            if (first || ord.compare(fx, minF) < 0) {
                minElem = elem;
                minF = fx;
                first = false;
            }
        }
        return minElem;
    }
    static <A, B> A           minBy(Iterable<? extends A> i, Fun1<? super A, ? extends B> f, Comparator<B> ord) { return minBy(i.iterator(), f, ord); }
    static <A, B> Option<A>   minByOption(Iterator<? extends A> i, Fun1<? super A, ? extends B> f, Comparator<B> ord) { if (isEmpty(i)) return None(); else return Some(minBy(i, f, ord)); }
    static <A, B> Option<A>   minByOption(Iterable<? extends A> i, Fun1<? super A, ? extends B> f, Comparator<B> ord) { return minByOption(i.iterator(), f, ord); }
    static <A, B> A           maxBy(Iterator<? extends A> i, Fun1<? super A, ? extends B> f, Comparator<B> ord) {
        if (isEmpty(i)) throw new UnsupportedOperationException("empty.maxBy");
        B maxF = null;
        A maxElem = null;
        boolean first = true;
        while (i.hasNext()) {
            A elem = i.next();
            B fx = f.call(elem);
            if (first || ord.compare(fx, maxF) > 0) {
                maxElem = elem;
                maxF = fx;
                first = false;
            }
        }
        return maxElem;
    }
    static <A, B> A           maxBy(Iterable<? extends A> i, Fun1<? super A, ? extends B> f, Comparator<B> ord) { return maxBy(i.iterator(), f, ord); }
    static <A, B> Option<A>   maxByOption(Iterator<? extends A> i, Fun1<? super A, ? extends B> f, Comparator<B> ord) { if (isEmpty(i)) return None(); else return Some(maxBy(i, f, ord)); }
    static <A, B> Option<A>   maxByOption(Iterable<? extends A> i, Fun1<? super A, ? extends B> f, Comparator<B> ord) { return  maxByOption(i.iterator(), f, ord); }

    static <A extends Comparable<? super A>> A           min(Iterator<? extends A> i) {
        if (isEmpty(i)) throw new UnsupportedOperationException("empty.min");
        return reduceLeft(i, Ord::min);
    }
    static <A extends Comparable<? super A>> A           min(Iterable<? extends A> i) { return min(i.iterator()); }
    static <A extends Comparable<? super A>> Option<A>   minOption(Iterator<? extends A> i) { if (isEmpty(i)) return None(); else return Some(min(i)); }
    static <A extends Comparable<? super A>> Option<A>   minOption(Iterable<? extends A> i) { return minOption(i.iterator()); }
    static <A extends Comparable<? super A>> A           max(Iterator<? extends A> i) {
        if (isEmpty(i)) throw new UnsupportedOperationException("empty.max");
        return reduceLeft(i, Ord::max);
    }
    static <A extends Comparable<? super A>> A           max(Iterable<? extends A> i) { return max(i.iterator()); }
    static <A extends Comparable<? super A>> Option<A>   maxOption(Iterator<? extends A> i) { if (isEmpty(i)) return None(); else return Some(max(i)); }
    static <A extends Comparable<? super A>> Option<A>   maxOption(Iterable<? extends A> i) { return maxOption(i.iterator()); }
    static <A extends Comparable<? super A>, B extends Comparable<? super B>> A           minBy(Iterator<? extends A> i, Fun1<? super A, ? extends B> f) {
        if (isEmpty(i)) throw new UnsupportedOperationException("empty.minBy");
        B minF = null;
        A minElem = null;
        boolean first = true;
        while (i.hasNext()) {
            A elem = i.next();
            B fx = f.call(elem);
            if (first || fx.compareTo(minF) < 0) {
                minElem = elem;
                minF = fx;
                first = false;
            }
        }
        return minElem;
    }
    static <A extends Comparable<? super A>, B extends Comparable<? super B>> A           minBy(Iterable<? extends A> i, Fun1<? super A, ? extends B> f) { return minBy(i.iterator(), f); }
    static <A extends Comparable<? super A>, B extends Comparable<? super B>> Option<A>   minByOption(Iterator<? extends A> i, Fun1<? super A, ? extends B> f) { if (isEmpty(i)) return None(); else return Some(minBy(i, f)); }
    static <A extends Comparable<? super A>, B extends Comparable<? super B>> Option<A>   minByOption(Iterable<? extends A> i, Fun1<? super A, ? extends B> f) { return minByOption(i.iterator(), f); }
    static <A extends Comparable<? super A>, B extends Comparable<? super B>> A           maxBy(Iterator<? extends A> i, Fun1<? super A, ? extends B> f) {
        if (isEmpty(i)) throw new UnsupportedOperationException("empty.maxBy");
        B maxF = null;
        A maxElem = null;
        boolean first = true;
        while (i.hasNext()) {
            A elem = i.next();
            B fx = f.call(elem);
            if (first || fx.compareTo(maxF) > 0) {
                maxElem = elem;
                maxF = fx;
                first = false;
            }
        }
        return maxElem;
    }
    static <A extends Comparable<? super A>, B extends Comparable<? super B>> A           maxBy(Iterable<? extends A> i, Fun1<? super A, ? extends B> f) { return maxBy(i.iterator(), f); }
    static <A extends Comparable<? super A>, B extends Comparable<? super B>> Option<A>   maxByOption(Iterator<? extends A> i, Fun1<? super A, ? extends B> f) { if (isEmpty(i)) return None(); else return Some(maxBy(i, f)); }
    static <A extends Comparable<? super A>, B extends Comparable<? super B>> Option<A>   maxByOption(Iterable<? extends A> i, Fun1<? super A, ? extends B> f) { return maxByOption(i.iterator(), f); }

    // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 特殊: 复制 🔴🍇🍉🍌🍋🍊🍐🍎🍏
    static <A> Pair<Iter<A>, Iter<A>> duplicate(Iterator<? extends A> i) {
        Queue<A> gap = new ArrayDeque<>();
        Ref<Iter<A>/*Partner*/> ahead = Ref(null); // 先迭代的迭代器

        class Partner implements Iter<A> {
            @Override synchronized public boolean hasNext() {
                // 如果是 ahead, 只判断 i.hasNext
                // 如果不是 ahead, gap 没有消费完, 一定可以继续迭代, gap 如果已经消费完,
                // 不一定不能继续, 需要看原始的迭代器能不能继续
                // return this != ahead.get() && !gap.isEmpty() || i.hasNext();
                if (this == ahead.get()) {
                    return i.hasNext();
                } else {
                    return !gap.isEmpty() || i.hasNext();
                }
            }
            @Override synchronized public A next() {
                // q 为空, 把当前迭代器设置为 ahead
                if (gap.isEmpty()) {
                    ahead.set(this);
                }
                // ahead 迭代器还需要为后迭代的填充 q 缓存, 后迭代的直接从 q 取
                if (this == ahead.get()) {
                    A e = i.next();
                    gap.add(e);
                    return e;
                } else {
                    return gap.remove();
                }
            }
        }

        return Pair(new Partner(), new Partner());
    }
    static <A> Pair<Iter<A>, Iter<A>> duplicate(Iterable<? extends A> i) { return duplicate(i.iterator()); }



    // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 sideEffect 🔴🍇🍉🍌🍋🍊🍐🍎🍏
    static <A> Iter<A> tapEach(Iterator<? extends A> i, Act1<? super A> f) {
        return new Iter<A>() {
            @Override public int knownSize() { return IterFuns.knownSize(i); }
            @Override public boolean hasNext() { return i.hasNext(); }
            @Override public A next() { A nxt = i.next(); f.call(nxt); return nxt; }
        };
    }
    static <A> Iter<A> tapEach(Iterable<? extends A> i, Act1<? super A> f) { return tapEach(i.iterator(), f); }
    // static <A, U> Iter<A> tapEach(Iterator<? extends A> i, Fun1<? super A, ? extends U> f) { return tapEach(i, (Act1<A>) f::call); }
    // static <A, U> Iter<A> tapEach(Iterable<? extends A> i, Fun1<? super A, ? extends U> f) { return tapEach(i.iterator(), f); }
    static <A> void foreach(Iterator<? extends A> i, Act1<? super A> f) { while (i.hasNext()) f.call(i.next()); }
    static <A> void foreach(Iterable<? extends A> i, Act1<? super A> f) { foreach(i.iterator(), f); }
    static <A, U> void foreach(Iterator<? extends A> i, Fun1<? super A, U> f) { while (i.hasNext()) f.call(i.next()); }
    static <A, U> void foreach(Iterable<? extends A> i, Fun1<? super A, U> f) { foreach(i.iterator(), f); }

    // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 toXXX 🔴🍇🍉🍌🍋🍊🍐🍎🍏
    static <A extends Collection<B>, B> A addAll(A a, Iterator<? extends B> i) {
        while (i.hasNext()) {
            a.add(i.next());
        }
        return a;
    }
    static <A extends Map<B, C>, B, C> A putAll(A m, Iterator<Pair<B, C>> i) {
        while (i.hasNext()) {
            Pair<B, C> it = i.next();
            m.put(it._1, it._2);
        }
        return m;
    }
    static <A> Iterable<A> toIterable(Iterator<? extends A> i)    { return toList(i);                     }
    static <A> List<A>     toList(Iterator<? extends A> i)        { return addAll(new ArrayList<>(), i);  }
    static <A> A[]         toArray(Iterator<? extends A> i, A[] a){ return toList(i).toArray(a);          }
    static <A> Set<A>      toSet(Iterator<? extends A> i)         { return addAll(new HashSet<>(), i);    }
    static <A> SortedSet<A>toSortedSet(Iterator<? extends A> i, Comparator<? super A> cmp)
                                                                  { return addAll(new TreeSet<>(cmp), i); }
    static <A> Stream<A>   toStream(Iterator<? extends A> i)      { return IterFuns.<A>toList(i).stream();}
    static <A> BufferedIter<A> buffered(Iterator<? extends A> i) {
        return new BufferedIter<A>() {
            A nxt;
            boolean nxtDefined = false;
            @Override public int knownSize() {
                int k = IterFuns.knownSize(i);
                if (k >= 0 && nxtDefined) return k + 1;
                else return k;
            }
            @Override public A head() {
                if (!nxtDefined) {
                    nxt = i.next();
                    nxtDefined = true;
                }
                return nxt;
            }
            @Override public boolean hasNext() { return nxtDefined || i.hasNext(); }
            @Override public A next() {
                if (nxtDefined) {
                    nxtDefined = false;
                    return nxt;
                } else {
                    return i.next();
                }
            }
        };
    }
    static <A> BufferedIter<A> buffered(Iterable<? extends A> i) { return buffered(i.iterator()); }



    // 应该放到 Iterators, BUT 编译不过 ...
    final class CollectIter<A, B> implements Iter<B>, Fun1<A, B> {
        private final static Object marker = new Object();
        final Iterator<? extends A> i;
        final PartialFun<A, B> pf;
        B hd;
        int status = 0; // Seek = 0; Found = 1; Empty = -1;
        public CollectIter(Iterator<? extends A> i, PartialFun<? super A, ? extends B> pf) {
            this.i = i;
            this.pf = Funs.narrow(pf);
        }
        @SuppressWarnings("unchecked")
        @Override public B call(A a) { return ((B) marker); }
        @Override public boolean hasNext() {
            while (status == 0) {
                if (i.hasNext()) {
                    A a = i.next();
                    B b = pf.callOrElse(a, this);
                    if (b != marker) {
                        hd = b;
                        status = 1;
                    }
                } else {
                    status = -1;
                }
            }
            return status == 1;
        }
        @Override public B next() {
            if (hasNext()) {
                status = 0;
                return hd;
            } else {
                return noSuchElement();
            }
        }
    }
}
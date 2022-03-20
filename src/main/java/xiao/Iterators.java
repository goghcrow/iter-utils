package xiao;

import org.jetbrains.annotations.Nullable;
import xiao.Helper.*;

import java.util.*;

import static xiao.Data.*;
import static xiao.Funs.*;
import static xiao.Data.Either.*;
import static xiao.IterFuns.noSuchElement;

/**
 * @author chuxiaofeng
 */
public interface Iterators {

    @SuppressWarnings({"unchecked", "rawtypes"})
    final class EmptyIter<A> implements Iter<A> {
        private final static Iter empty = new EmptyIter<>();
        public static <A> Iter<A> get() { return empty; }
        @Override public int knownSize() { return 0; }
        @Override public boolean hasNext() { return false; }
        @Override public A next() { throw new NoSuchElementException("next on empty iterator"); }
    }

    final class SingleIter<A> implements Iter<A> {
        boolean consumed = false;
        final A a;
        public SingleIter(A a) { this.a = a; }
        @Override public int knownSize() { return consumed ? 0 : 1; }
        @Override public boolean hasNext() { return !consumed; }
        @Override public A next() { if (consumed) return noSuchElement(); else { consumed = true; return a; } }
    }

    final class /*Elems*/ArrayIter<A> implements Iter<A> {
        private final A[] a;
        private int i = 0;
        public ArrayIter(A[] a) { this.a = a; }
        @Override public int knownSize() { return Math.max(a.length - i, 0); }
        @Override  public boolean hasNext() { return i < a.length; }
        @Override  public A next() { return hasNext() ? a[i++] : noSuchElement(); }
    }

    final class FlattenIter<A> implements Iter<A> {
        final Iterator<? extends Iterator<? extends A>> i;
        Iterator<? extends A> cur = Iter.empty();
        int hasNext = -1; // -1 = unknown, 0 = false, 1 = true
        public FlattenIter(Iterator<? extends Iterator<? extends A>> i) { this.i = i; }
        @Override public int knownSize() { return IterFuns.knownSize(i); }
        @Override public boolean hasNext() {
            if (hasNext == -1) {
                while (!cur.hasNext()) {
                    if (i.hasNext()) {
                        cur = i.next();
                        hasNext = -1;
                    } else {
                        hasNext = 0;
                        cur = Iter.empty(); // gc
                        return false;
                    }
                }
                hasNext = 1;
                return true;
            } else {
                return hasNext == 1;
            }
        }
        @Override public A next() {
            if (hasNext()) hasNext = -1;
            return cur.next();
        }
    }
    final class FlattenIter1<A> implements Iter<A> {
        @SuppressWarnings("rawtypes")
        private final static Iterator end = Iter.empty();
        final Iterator<? extends Iterator<? extends A>> i;
        Iterator<? extends A> cur;
        public FlattenIter1(Iterator<? extends Iterator<? extends A>> i) {
            this.i = i;
            this.cur = nextIter();
        }
        @SuppressWarnings("unchecked")
        Iterator<? extends A> nextIter() { return i.hasNext() ? i.next() : end; }
        @Override public boolean hasNext() {
            if (cur == end) return false;
            if (cur.hasNext()) {
                return true;
            } else {
                cur = nextIter();
                return hasNext();
            }
        }
        @Override public A next() { return hasNext() ? cur.next() : noSuchElement(); }
    }

    final class FilterIter<A> implements Iter<A> {
        final Iterator<? extends A> i;
        final Fun1<? super A, Boolean> p;
        final boolean isFlipped;
        boolean nxtDefined = false;
        A nxt;
        public FilterIter(Iterator<? extends A> i, Fun1<? super A, Boolean> p, boolean isFlipped) {
            this.i = i;
            this.p = p;
            this.isFlipped = isFlipped;
        }
        @Override public boolean hasNext() {
            if (nxtDefined) return true;
            if (!i.hasNext()) return false;
            nxt = i.next();
            while (p.call(nxt) == isFlipped) {
                if (!i.hasNext()) return false;
                nxt = i.next();
            }
            nxtDefined = true;
            return true;
        }
        @Override public A next() {
            if (hasNext()) {
                nxtDefined = false;
                return nxt;
            } else {
                return noSuchElement();
            }
        }
    }
    final class FilterIter1<A> implements Iter<A> {
        final Iterator<? extends A> i;
        final Fun1<? super A, Boolean> p;
        final boolean isFlipped;
        A[] nxt; // 装箱区分 null 还是 hasNext = false
        public FilterIter1(Iterator<? extends A> i, Fun1<? super A, Boolean> p, boolean isFlipped) {
            this.i = i;
            this.p = p;
            this.isFlipped = isFlipped;
        }
        @SuppressWarnings("unchecked")
        boolean found(A a) { nxt = ((A[]) new Object[] { a }); return true; }
        A get() { A v = nxt[0]; nxt = null; return v; }
        @Override public A next() { return hasNext() ? get() : noSuchElement(); }
        @Override public boolean hasNext() {
            if (nxt != null) return true;
            if (!i.hasNext()) return false;
            A v = i.next();
            return p.call(v) != isFlipped ? found(v) : hasNext();
        }
    }
    final class FilterIter2<A> implements Iter<A> {
        final Iterator<? extends A> i;
        final Fun1<? super A, Boolean> p;
        final boolean isFlipped;
        int status = 0; // seek = 0, found = 1, empty = -1
        A nxt;
        public FilterIter2(Iterator<? extends A> i, Fun1<? super A, Boolean> p, boolean isFlipped) {
            this.i = i;
            this.p = p;
            this.isFlipped = isFlipped;
        }
        @Override public boolean hasNext() {
            while (status == 0) {
                if (i.hasNext()) {
                    A v = i.next();
                    if (p.call(v) != isFlipped) {
                        nxt = v;
                        status = 1; // found
                    }
                } else {
                    status = -1; // empty
                }
            }
            return status == 1; // found
        }
        @Override public A next() {
            if (hasNext()) {
                status = 0;
                return nxt;
            } else {
                return noSuchElement();
            }
        }
    }

    final class LeftPartitionIter<A, A1, A2> implements Iter<A1> {
        final Iterator<? extends A> i;
        final Fun1<? super A, Either<A1, A2>> f;
        boolean nxtDefined = false;
        A1 nxt;
        public LeftPartitionIter(Iterator<? extends A> i, Fun1<? super A, Either<A1, A2>> f) {
            this.i = i;
            this.f = f;
        }
        @Override public boolean hasNext() {
            if (nxtDefined) return true;
            if (!i.hasNext()) return false;
            Either<A1, A2> ei = f.call(i.next());
            try { return ei.match(); }
            catch (LeftToken t) {
                nxt = ei.get(t);
                nxtDefined = true;
                return true;
            }
            catch (RightToken t) { return hasNext(); }
        }
        @Override public A1 next() {
            if (hasNext()) {
                nxtDefined = false;
                return nxt;
            } else {
                return noSuchElement();
            }
        }
    }

    final class RightPartitionIter<A, A1, A2> implements Iter<A2> {
        final Iterator<? extends A> i;
        final Fun1<? super A, Either<A1, A2>> f;
        boolean nxtDefined = false;
        A2 nxt;
        public RightPartitionIter(Iterator<? extends A> i, Fun1<? super A, Either<A1, A2>> f) {
            this.i = i;
            this.f = f;
        }
        @Override public boolean hasNext() {
            if (nxtDefined) return true;
            if (!i.hasNext()) return false;
            Either<A1, A2> ei = f.call(i.next());
            try { return ei.match(); }
            catch (LeftToken t) { return hasNext(); }
            catch (RightToken t) {
                nxt = ei.get(t);
                nxtDefined = true;
                return true;
            }
        }
        @Override public A2 next() {
            if (hasNext()) {
                nxtDefined = false;
                return nxt;
            } else {
                return noSuchElement();
            }
        }
    }

    final class DistinctByIter1<A, B> implements Iter<A> {
        FilterIter<A> filter;
        final Set<B> set = new HashSet<>();
        public DistinctByIter1(Iterator<? extends A> i, final Fun1<? super A, ? extends B> f) {
            filter = new FilterIter<>(i, a -> set.add(f.call(a)), false);
        }
        @Override public boolean hasNext() { return filter.hasNext(); }
        @Override public A next() { return filter.next(); }
    }
    final class DistinctByIter<A, B> implements Iter<A> {
        final Iterator<? extends A> i;
        final Fun1<? super A, ? extends B> f;
        final Set<B> traversedValues = new HashSet<>();
        boolean nextElementDefined = false;
        A nextElement;
        public DistinctByIter(Iterator<? extends A> i, Fun1<? super A, ? extends B> f) {
            this.i = i;
            this.f = f;
        }
        @Override public boolean hasNext() {
            if (nextElementDefined) return true;
            if (!i.hasNext()) return false;
            A a = i.next();
            if (traversedValues.add(f.call(a))) {
                nextElement = a;
                nextElementDefined = true;
                return true;
            } else {
                return hasNext();
            }
        }
        @Override public A next() {
            if (hasNext()) {
                nextElementDefined = false;
                return nextElement;
            } else {
                return noSuchElement();
            }
        }
    }

    final class ReverseIter<A> implements Iter<A> {
        final Iterator<A> i;
        public ReverseIter(Iterator<? extends A> i) {
            LinkedList<A> l = new LinkedList<>();
            while (i.hasNext()) {
                l.addFirst(i.next());
            }
            this.i = l.iterator();
        }
        @Override public boolean hasNext() { return i.hasNext(); }
        @Override public A next() { return i.next(); }
    }

    final class PatchIter<A> implements Iter<A> {
        Iterator<? extends A> origElems;
        final int from;
        final Iterator<? extends A> patchElems;
        final int replaced;
        // > 0 => 继续 orig
        //   0 => 从 orig drop, 切换到 path 开始
        //  -1 => 已经 drop orig, 把 path 迭代完, 然后切回 drop 完的 ori
        int state;
        public PatchIter(Iterator<? extends A> origElems, int from, Iterator<? extends A> patchElems, int replaced) {
            this.origElems = origElems;
            this.from = from;
            this.patchElems = patchElems;
            this.replaced = replaced;
            this.state = Math.max(from, 0);
        }
        void switchToPatchIfNeeded() {
            if (state == 0) {
                origElems = IterFuns.drop(origElems, replaced);
                state = -1;
            }
        }
        @Override public boolean hasNext() {
            switchToPatchIfNeeded();
            // 不管 < 0 还是 > 0 两个都要尝试迭代, 任意一个能迭代都可以
            return origElems.hasNext() || patchElems.hasNext();
        }
        @Override public A next() {
            switchToPatchIfNeeded();
            if (state < 0) {
                // -1 先 patch 然后 orig
                if (patchElems.hasNext()) {
                    return patchElems.next();
                } else {
                    return origElems.next();
                }
            } else {
                // > 0 orig, 没有切换到 patch
                if (origElems.hasNext()) {
                    state -= 1;
                    return origElems.next();
                } else {
                    state = -1;
                    return patchElems.next();
                }
            }
        }
    }

    final class TakeWhileFilter<A> implements Iter<A> {
        /*final*/ Iterator<? extends A> i;
        final Fun1<? super A, Boolean> p;
        boolean nxtDefined = false;
        A nxt;
        public TakeWhileFilter(Iterator<? extends A> i, Fun1<? super A, Boolean> p) {
            this.i = i;
            this.p = p;
        }
        @SuppressWarnings("CommentedOutCode")
        @Override public boolean hasNext() {
            if (nxtDefined) return true;
            if (!i.hasNext()) return false;
            nxt = i.next();
            if (p.call(nxt)) {
                nxtDefined = true;
            } else {
                // BUG: 这里需要清空 i, 否则hasNext 不是幂等, 因为内部会不断调用 i 的 next
                    /*
                    Iter<Integer> it = takeWhile(Iter(0, 4, 1), i -> i < 2);
                    println(it.hasNext());// true
                    println(it.next()); // 0
                    println(it.hasNext()); // false
                    println(it.hasNext()); // BUG: 1 < 2 true
                    */
                i = Iter.empty();
            }
            return nxtDefined;
        }
        @Override public A next() {
            if (hasNext()) {
                nxtDefined = false;
                return nxt;
            } else {
                return noSuchElement();
            }
        }
    }

    final class DropWhileFilter<A> implements Iter<A> {
        final Iterator<? extends A> i;
        final Fun1<? super A, Boolean> p;
        int status = -1; // -1 = 还没 drop, 0 = drop 完, 1 = 委托给 i
        A fst;
        public DropWhileFilter(Iterator<? extends A> i, Fun1<? super A, Boolean> p) {
            this.i = i;
            this.p = p;
        }
        @Override public boolean hasNext() {
            if (status == 1) return i.hasNext();
            else if (status == 0) return true;
            else {
                while (i.hasNext()) {
                    A a = i.next();
                    if (!p.call(a)) {
                        fst = a;
                        status = 0;
                        return true;
                    }
                }
                status = 1;
                return false;
            }
        }
        @Override public A next() {
            if (hasNext()) {
                if (status == 1) {
                    return i.next();
                } else {
                    assert status == 0;
                    status = 1;
                    return fst;
                }
            } else {
                return noSuchElement();
            }
        }
    }

    final class DropRightIter<A> implements Iter<A> {
        final Iterator<? extends A> i;
        final int maxLen;

        int len = -1; // 已知 size 或者 -1 未知 size
        int pos = 0; // 读写游标
        A[] buf; // 循环数组, 长度为 min(实际可迭代长度, maxLen), 相当于保留一个drop的窗口往右边移动
        public DropRightIter(Iterator<? extends A> i, int maxLen) {
            this.i = i;
            this.maxLen = maxLen;
        }
        void extendBuf() {
            int newLen = Math.min(buf.length * 2, maxLen);
            //noinspection unchecked
            A[] newBuf = ((A[]) new Object[newLen]);
            System.arraycopy(buf, 0, newBuf, 0, buf.length);
            buf = newBuf;
        }
        void initialize () {
            if (buf == null) {
                // 避免实际可迭代长度远小于 maxLen, 白白浪费空间
                //noinspection unchecked
                buf = ((A[]) new Object[Math.min(maxLen, 256)]);
                // drop的窗口buf 填满或者不够 drop
                while (pos < maxLen && i.hasNext()) {
                    if (pos >= buf.length) {
                        extendBuf();
                    }
                    buf[pos++] = i.next();
                }
                // 总长度还不够 drop 的
                if (!i.hasNext()) {
                    len = 0;
                }
                pos = 0; // reset 读游标
            }
        }
        @Override public int knownSize() { return len; }
        @Override public boolean hasNext() { initialize(); return len != 0; }
        @Override public A next() {
            if (!hasNext()) return noSuchElement();
            A x = buf[pos];
            if (len == -1) {
                // 消费一个, 缓存一个, 往右移动窗口
                buf[pos] = i.next();
                if (!i.hasNext()) {
                    len = 0;
                }
            } else {
                len -= 1; // 这个分支应该没用
            }
            pos += 1; // 读游标往右移
            if (pos == maxLen) { // 循环数组回绕
                pos = 0;
            }
            return x;
        }
    }

    final class TakeRightIter<A> implements Iter<A> {
        Iterator<? extends A> i;
        final int maxLen;

        int len = -1; // take 剩余未读长度
        int pos = 0; // 读写游标
        A[] buf; // 循环数组, size = min(实际可迭代长度, maxLen)
        public TakeRightIter(Iterator<? extends A> i, int maxLen) {
            this.i = i;
            this.maxLen = maxLen;
        }
        void extendBuf() {
            int newLen = Math.min(buf.length * 2, maxLen);
            //noinspection unchecked
            A[] newBuf = ((A[]) new Object[newLen]);
            System.arraycopy(buf, 0, newBuf, 0, buf.length);
            buf = newBuf;
        }
        void initialize () {
            if (buf == null) {
                // 避免实际可迭代长度远小于 maxLen, 白白浪费空间
                //noinspection unchecked
                buf = ((A[]) new Object[Math.min(maxLen, 256)]);
                // 把 i 迭代完, 保留 min(实际可迭代 len, maxLen) 数据, 其余舍弃
                len = 0; // 实际 size
                while (i.hasNext()) {
                    A n = i.next();
                    // 实际可迭代 size 大于 256, 扩容
                    if (pos >= buf.length) {
                        extendBuf();
                    }
                    buf[pos++] = n;
                    // 缓存满, 回绕, 因为左边的数据需要舍弃, 所处直接覆盖原来数据
                    if (pos == maxLen) {
                        pos = 0;
                    }
                    len += 1;
                }
                i = null; // gc
                // 出现覆盖情况, 实际len = maxLen
                if (len > maxLen) {
                    len = maxLen;
                }
                // fix 循环数组游标到起始读位置
                pos = pos - len;
                if (pos < 0) {
                    pos += maxLen;
                }
            }
        }
        @Override public int knownSize() { return len; }
        @Override public boolean hasNext() { initialize(); return len > 0; }
        @Override public A next() {
            initialize();
            if (len == 0) {
                return noSuchElement();
            } else {
                A x = buf[pos];
                pos += 1;
                // 循环数据回绕
                if (pos == maxLen) {
                    pos = 0;
                }
                len -= 1;
                return x;
            }
        }
    }

    final class ScanLeftIter<A, B> implements Iter<B> {
        final Iterator<? extends A> i;
        final B z;
        final Fun2<? super B, ? super A, ? extends B> op;
        Iter<B> current;
        public ScanLeftIter(Iterator<? extends A> i, B z, Fun2<? super B, ? super A, ? extends B> op) {
            this.i = i;
            this.z = z;
            this.op = op;
            // 中间迭代器统一处理 z
            current = new Iter<B>() {
                @Override public int knownSize() {
                    int k = IterFuns.knownSize(i);
                    return k < 0 ? -1 : k + 1;
                }
                // 只用一次, next 是 z, 所以 return true
                @Override public boolean hasNext() { return true; }
                @Override public B next() {
                    // 返回 z, 把 current 桥接回 i
                    current = new Iter<B>() {
                        B acc = z;
                        @Override public int knownSize() { return IterFuns.knownSize(i); }
                        @Override public boolean hasNext() { return i.hasNext(); }
                        @Override public B next() {
                            acc = op.call(acc, i.next());
                            return acc;
                        }
                    };
                    return z;
                }
            };
        }
        @Override public int knownSize() { return current.knownSize(); }
        @Override public boolean hasNext() { return current.hasNext(); }
        @Override public B next() { return current.next(); }
    }

    final class GroupedIter<A> implements Iter<List<A>> {
        final Iterator<? extends A> i;
        final int size; // 分组 大小
        final int step; // 下一个分组起始偏移

        List<A> buffer = new ArrayList<>(); // 之所以有 buffer, 是因为 step 可能小于 size, 每次 yield 数据有重叠
        boolean filled = false;             // buffer 是否 ready
        boolean partial = true;             // 当最后分组长度不够时, 是否返回分组
        Option<Fun0<A>> pad = None();       // 当最后分组长度不够可选 pad 函数

        public GroupedIter(Iterator<? extends A> i, int size, int step) {
            if (size < 1 || step < 1) {
                String msg = String.format("size=%d and step=%d, but both must be positive", size, step);
                throw new IllegalArgumentException(msg);
            }
            this.i = i;
            this.size = size;
            this.step = step;
        }
        // 配置迭代器, 最后分组不够用 x 补齐, 所有分组长度一样
        // 注意: 1. 方法会修改迭代器 2. 方法与 withPartial(true) 互斥
        public Iter<List<A>> withPadding(Fun0<A> x) {
            pad = Some(x);
            return this;
        }
        // 配置迭代器. 当最后分组长度不够时,
        // withPartial(false), 多余部分丢弃, withPartial(true) 多余部分保留并清除 pad
        public Iter<List<A>> withPartial(boolean x) {
            partial = x;
            // fill 中 pad.isDefined() 的逻辑在 partial 之前, 所以这里要清除
            if (partial) pad = None();
            return this;
        }
        List<A> takeDestructively(int size) {
            // return IterFuns.take(i, size).toList();
            List<A> buf = new ArrayList<>();
            int n = 0;
            while (n < size && i.hasNext()) {
                buf.add(i.next());
                n++;
            }
            return buf;
        }
        List<A> padding(int x) { return Iter.fill(x, pad.get()).toList(); }
        int gap() { return Math.max(step - size, 0); } // step 如果 大于 size, 中间跳过的部分
        boolean fill() {
            // 第一次取 size, 之后每次取 step即可满足下次迭代需要的 size, e.g.
            // 1 2 3 4 ...
            // size = 2, step = 1 : buf=[1 2], buf=[...2 3], buf=[...3 4]
            // size = 2, step = 2 : buf=[1 2], buf=[...3 4], buf=[...5 6]
            // size = 2, step = 3 : buf=[1 2], buf=[...4 5], buf=[...7 8]
            if (!i.hasNext()) return false;
            else if (buffer.isEmpty()) return fill(size);
            else return fill(step);
        }
        boolean fill(int cnt) {
            // prevSize 除了第一次是0, 和最后不足(不会进入 fill 函数不用考虑), 永远是 size
            int prevSize = buffer.size();
            boolean isFirst = prevSize == 0; // 因为每次 next 返回, 没有清空 buffer, 所以可以用 prevSize 判断 isFirst

            // 不够且需要 pad, 就先 pad, 这里是个跟后面没关系的逻辑, 简化后续处理
            List<A> xs;
            List<A> took = takeDestructively(cnt); // 第一次获取 size, 第二次获取 gap + size
            int shortBy = cnt - took.size();
            if (shortBy > 0 && pad.isDefined()) {
                List<A> padding = padding(shortBy);
                xs = IterFuns.concat(took, padding).toList();
            } else {
                xs = took;
            }

            // len 实际取到的长度 cnt 期望取到的长度
            int len = xs.size();
            // 如果 incomplete = true, 则说明到了最后一组, 没有经过 pad, 且长度不够
            boolean incomplete = len < cnt;

            class Deliver {
                // howMany 参数含义: 在原来 buffer 基础上 追加 takeRight(xs, N), N 最大不超过 howMany
                // 只有三种情况：
                // 1. partial = true, len < size, 且分组获取不足时, 最大不超过实际大小 len
                // 2. step < size 时, 最大不超过 step
                // 3. 其他都是最大不超过 size的正常情况
                boolean go(int howMany) {
                    // 这里 howMany 应该永远 > 0, scala 实现不大对
                    if (howMany <= 0) return false;

                    // partial 分支 和 isFirst 分支都会存在 isFirst,
                    // 覆盖 partial=true|false 情况下, 第一个分组是否有足够元素
                    // 第一次 fill(size) 而不是 fill(step), 且 buffer.isEmpty, 不管够不够, 直接填充返回
                    if (isFirst) {
                        buffer.addAll(xs);
                        filled = true;
                        return true;
                    }

                    if (len > gap()) {
                        // 根据是否需要重合的数据计算保留元素后的 buffer：
                        // 不是 first, 填充的是 step, 如果 step < size, 说明数据有重合, 要跳过 gap
                        // 如果 step 大于等于 siz, 说明没有数据重合, 之前 buffer 没什么用, 直接清空
                        if (step < size) {
                            // arrayList subList 之后 add 还会引用原来的空间, 这里为了释放原来空间复制一份
                            // 否则会浪费空间一直保存迭代过的元素
                            buffer = new ArrayList<>(buffer.subList(step, buffer.size()));
                        } else {
                            // buffer = buffer.subList(prevSize, buffer.size());
                            assert prevSize == size;
                            buffer = new ArrayList<>();
                        }

                        // 跳过 gap 后实际buffer 实际应该填充的大小
                        // available 一定不超过 size, 填充满 available = size, 填充不满, available < size
                        int available = len - gap(); // <= size


                        // 因为 howMany 有三种情况, 考虑到 buffer 可能有剩余元素, 实际获取的大小不是 len-gap()
                        // 而是限制在不超过 howMany 情况
                        // howMany 三种情况
                        // 第三种情况：howMany 不超过 size, available 一定 <= howMany
                        // 第一种情况：partial = true, len < size, 且分组获取不足时, howMany 最大不超过实际大小 len
                        //      howMany < len < size && available < size
                        // 第二种情况：step < size 时, howMany 最大不超过 step
                        //      howMany < step < size && available < size
                        // 一、二两种情况都需要裁切
                        if (howMany < available) {
                            System.out.println();
                        }
                        available = Math.min(howMany, available);
                        buffer.addAll(IterFuns.takeRight(xs, available).toList());
                        filled = true;
                        return true;
                    } else {
                        // 实际获取的长度还没 gap 长(gap 是需要被跳过的), 则说明已经没元素了, return false
                        return false;
                    }
                }
            }

            // 没取到, 已经迭代完成
            if (xs.isEmpty()) return false;
                // xs 不为空, 且可以取部分, 实际需要 takeRight 大小是(最长不超过 size)：
                // 1. len < size: 不管是不是第一次, len 可能是最后分组小于分组大小, takeRight(实际大小 len), 早大不超过 len
                // 2. len > size: isFirst = false,fill(step), step > size, takeRight(size), 最大不超过 size
            else if (partial) return new Deliver().go(Math.min(len, size)/*>0*/);
                // 这这个分支是 partial = false, 且实际获取小组分组大小, 直接舍弃剩余元素
            else if (incomplete) return false;

                // 下面两个分支 incomplete = false, partial = false, fill 填充慢

                // 第一次 fill的 size, 不用截取, 所以直接取实际大小 len 就行, 所以这里 len 一定等于 size, 最大不超过 size
            else if (isFirst) return new Deliver().go(len/*>0*/);
                // normal 分支, 不是第一次, 且当前分组数据不缺
                // fill 的一定是 step 不是 size
                // step > size, 取 size 大小, takeRight(size), drop 掉 gap, 和上次 buffer 没有重叠, 最大不超过 size
                // step < size, 合并上次buffer, takeRight, 包括重叠部分, 最大不超过 step
            else return new Deliver().go(Math.min(step, size)/*>0*/);
        }
        @Override public boolean hasNext() { return filled || fill(); }
        @Override public List<A> next() {
            if (hasNext()) {
                filled = false;
                return buffer;
            } else {
                return noSuchElement();
            }
//                if (!filled) fill();
//                if (!filled) return noSuchElement();
//                filled = false;
//                return new ArrayList<>(buffer);
        }
    }

    // !!!可以用 FlattenIter 替代, but 这个效率更高一些
    final class ConcatIter<A> implements Iter<A> {
        private final static class ConcatIterCell<A> {
            final Iterator<? extends A> headIter; // first
            ConcatIterCell<A> tail; // rest
            private ConcatIterCell(Iterator<? extends A> headIter, @Nullable ConcatIterCell<A> tail) {
                this.headIter = headIter;
                this.tail = tail;
            }
        }
        Iterator<? extends A> current; // 当前遍历的迭代器
        ConcatIterCell<A> tail; // 接下来遍历的迭代器
        ConcatIterCell<A> last; // 用来快速concat
        boolean currentHasNextChecked = false;
        public ConcatIter(Iterator<? extends A> current) { this.current = current; }
        // 遇到 ConcatIter, 递归展开, 插入当前位置
        private void merge() {
            if (current instanceof ConcatIter) {
                // iter 是只读的, 所以这么转型没问题
                //noinspection unchecked
                ConcatIter<A> c = (ConcatIter<A>) current;
                // 合并要更新 current, tail, last, currentHasNextChecked
                // 被合并迭代器当前可能不为空
                current = c.current;
                currentHasNextChecked = c.currentHasNextChecked;
                // 被合并迭代器后继如果为空不处理, 不为空进行连接
                if (c.tail != null) {
                    // 当前迭代器节点后继为空时, 需要把 last 变更为插入的节点的 last
                    // 不为空的话因为是插入, last不变
                    if (last == null) {
                        assert tail == null;
                        last = c.last;
                    }
                    // 把当前节点后面的迭代器链表接到被被展开的迭代器后面
                    c.last.tail = tail;
                    tail = c.tail;
                }
                merge();
            }
        }
        // current 设置成下一个非空迭代器
        // 当所有迭代器都遍历完, current 设置成 null
        private boolean advance() {
            if (tail == null) {
                // current 已经迭代完, 后续也没有需要迭代的, 结束
                current = null;
                last = null;
                return false;
            } else {
                // 当前迭代游标往前移, 抛弃 current
                current = tail.headIter;
                if (last == tail) {
                    assert last.tail == null;
                    last = last.tail;
                }
                tail = tail.tail;
                merge();
                if (currentHasNextChecked) {
                    return true;
                } else if (current != null && current.hasNext()) {
                    currentHasNextChecked = true;
                    return true;
                } else {
                    return advance();
                }
            }
        }
        // todo View.scala Concat
        // @Override public int knownSize() { }
        @Override public boolean hasNext() {
            if (currentHasNextChecked) return true;
            else if (current == null) return false;
            else if (current.hasNext()) { currentHasNextChecked = true; return true; }
            else return advance();
        }
        @Override public A next() {
            if (hasNext()) {
                currentHasNextChecked = false;
                return current.next();
            } else {
                return noSuchElement();
            }
        }
        // def concat[B >: A](that: => IterableOnce[B]): Iterator[B]
        @Override public Iter<A> concat(Iterator<? extends A> that) {
            ConcatIterCell<A> c = new ConcatIterCell<>(that, null);
            //noinspection IfStatementWithIdenticalBranches
            if (tail == null) {
                tail = c;
                last = c;
            } else {
                // 链表尾部添加节点
                last.tail = c;
                last = c;
            }
            // 如果之前已经迭代完, 再添加时候需要再次初始化 current, 简化处理
            if (current == null) current = Iter.empty();
            return this;
        }
    }

    final class SliceIter<A> implements Iter<A> {
        final Iterator<? extends A> i;
        int remaining;
        int dropping;
        final boolean unbounded;
        public SliceIter(Iterator<? extends A> i, int start, int limit) {
            this.i = i;
            remaining = limit;
            dropping = start;
            this.unbounded = remaining < 0;
        }
        void skip() {
            while (dropping > 0) {
                if (i.hasNext()) {
                    i.next();
                    dropping -= 1;
                } else {
                    dropping = 0;
                }
            }
        }
        @Override public int knownSize() {
            int k = IterFuns.knownSize(i);
            if (k < 0) {
                return k;
            } else {
                int dropSz = Math.max(k - dropping, 0);
                return unbounded ? dropSz : Math.min(remaining, dropSz);
            }
        }
        @Override public boolean hasNext() { skip(); return remaining != 0 && i.hasNext(); }
        @Override public A next() {
            skip();
            if (remaining > 0) {
                remaining -= 1;
                return i.next();
            } else if (unbounded) {
                return i.next();
            } else {
                return noSuchElement();
            }
        }
        @Override public Iter<A> slice(int from, int until) {
            int lo = Math.max(from, 0);
            int adjustedBound = unbounded ? -1 : Math.max(0, remaining - lo);
            int rest;
            if (until < 0) {
                rest = adjustedBound; // respect current bound, if any
            } else if (until <= lo) {
                rest = 0; // empty
            } else if (unbounded) {
                rest = until - lo; // now finite
            } else {
                rest = Math.min(adjustedBound, until - lo); // keep lesser bound
            }
            if (rest == 0) {
                return Iter.empty();
            } else {
                dropping += lo;
                remaining = rest;
                return this;
            }
        }
    }

    final class UnfoldIter<A, S> implements Iter<A> {
        S state;
        final Fun1<? super S, Option<Pair<A, S>>> f;
        Option<Pair<A, S>> nextResult = null;
        public UnfoldIter(S init, Fun1<? super S, Option<Pair<A, S>>> f) {
            this.state = init;
            this.f = f;
        }
        @Override public boolean hasNext() {
            if (nextResult == null) {
                nextResult = Objects.requireNonNull(f.call(state));
                state = null; // gc
            }
            return nextResult.isDefined();
        }
        @Override public A next() {
            if (hasNext()) {
                Pair<A, S> p = nextResult.get();
                state = p._2;
                nextResult = null;
                return p._1;
            } else {
                return noSuchElement();
            }
        }
    }

    final class IntRangeIter implements Iter<Integer> {
        int i;
        final int end;
        final int step;
        boolean hasOverFlowed = false;
        public IntRangeIter(int start, int end, int step) {
            if (step == 0) throw new IllegalArgumentException("zero step");
            this.i = start;
            this.end = end;
            this.step = step;
        }
        @Override public int knownSize() {
            double size = Math.ceil(((long) end) - ((long) i) / ((double) step));
            if (size < 0) return 0;
            else if (size > Integer.MAX_VALUE) return -1;
            return ((int) size);
        }
        @Override public boolean hasNext() {
            if (hasOverFlowed) return false;
            return step < 0 ? i > end : i < end;
        }
        @Override public Integer next() {
            if (hasNext()) {
                int r = i;
                int nxt = i + step;
                // 处理溢出
                hasOverFlowed = (step > 0) == nxt < i;
                i = nxt;
                return r;
            } else {
                return noSuchElement();
            }
        }
    }

    final class LongRangeIter implements Iter<Long> {
        long i;
        final long end;
        final long step;
        boolean hasOverFlowed = false;
        public LongRangeIter(long start, long end, long step) {
            if (step == 0) throw new IllegalArgumentException("zero step");
            this.i = start;
            this.end = end;
            this.step = step;
        }
        @Override public int knownSize() {
            double size = Math.ceil(end - i / ((double) step));
            if (size < 0) return 0;
            else if (size > Integer.MAX_VALUE) return -1;
            return ((int) size);
        }
        @Override public boolean hasNext() {
            if (hasOverFlowed) return false;
            return step < 0 ? i > end : i < end;
        }
        @Override public Long next() {
            if (hasNext()) {
                long r = i;
                long nxt = i + step;
                hasOverFlowed = (step > 0) == nxt < i;
                i = nxt;
                return r;
            } else {
                return noSuchElement();
            }
        }
    }

}

package xiao;

import xiao.Data.Option;
import xiao.Data.Ref;
import xiao.Helper.*;

import static xiao.Funs.*;
import static xiao.Data.*;
import static xiao.PatternMatching.*;

/**
 * @author chuxiaofeng
 *
 * 调用 call 之前调用 isDefinedAt 是调用者的责任, 因为如果 isDefinedAt 返回 false,
 * 不保证 call 会抛异常标识调用了一个错误的分支, 如果没有跑异常, 会返回错误的执行结果
 * 最佳实践是调用 callOrElse, 这样比调用 isDefinedAt + call 更高效
 *
 * === === === === === === === === === === === === === === === === === ===
 *
 * Fun1<Integer, Boolean> isEven = n -> n % 2 == 0;
 * PartialFun<Integer, String> eveningNews = Case(isEven, i -> i + " is even");
 * 等同于 filter + map
 * Iter<String> evenNumbers = range(1, 10).collect(eveningNews);
 * System.out.println(evenNumbers.toList());
 *      [2 is even, 4 is even, 6 is even, 8 is even]
 *
 * === === === === === === === === === === === === === === === === === ===
 *
 * PartialFun<Integer, String> oddlyEnough = Case(i -> !isEven.call(i), i1 -> i1 + " is odd");
 * pf 可以链接到一起
 * System.out.println(range(1, 10).map(eveningNews.orElse(oddlyEnough)).toList());
 *      [1 is odd, 2 is even, 3 is odd, 4 is even, 5 is odd, 6 is even, 7 is odd, 8 is even, 9 is odd]
 *
 * 等同于
 * System.out.println(range(1, 10).map(i1 -> eveningNews.callOrElse(i1, oddlyEnough)).toList());
 *      [1 is odd, 2 is even, 3 is odd, 4 is even, 5 is odd, 6 is even, 7 is odd, 8 is even, 9 is odd]
 *
 * === === === === === === === === === === === === === === === === === ===
 *
 * PartialFun<Integer, Integer> half = Case(isEven, x -> x / 2);
 * 组合计算是昂贵的
 * PartialFun<Integer, String> oddByHalf = half.andThen(oddlyEnough);
 * System.out.println(range(1, 10).collect(oddByHalf).toList());
 *      1-10 =isEven=> 2 4 6 8 =x/2=> 1 2 3 4 =odd=> 1 3
 *      [1 is odd, 3 is odd]
 *
 * Iter<Integer> oddBalls1 = range(1, 10).filter(oddByHalf::isDefinedAt);
 * System.out.println(oddBalls1.toList());
 *      1-10 =isEven=> 2 4 6 8 =x/2=> 1 2 3 4 =odd=> 1 3
 *      1 3 map 之前是 1-10 里头 的 2 6
 *
 * 提供默认值
 * Iter<String> oddsAndEnds = range(1, 10).map(n -> oddByHalf.callOrElse(n, i -> "[" + i + "]"));
 * System.out.println(oddsAndEnds.toList());
 *      [[1], 1 is odd, [3], [4], [5], 3 is odd, [7], [8], [9]]
 *
 * === === === === === === === === === === === === === === === === === ===
 *
 * 模拟 if (pf.isDefinedAt) { pf.call() }, 导致 复计算
 *
 * Fun1<Integer, Boolean> isEven = n -> n % 2 == 0;
 * PartialFun<Integer, String> oddlyEnough = Case(i -> !isEven.call(i), i1 -> {
 *     System.out.println(i1 + " is odd");
 *     return i1 + " is odd";
 * });
 * PartialFun<Integer, Integer> half = Case(isEven, x -> {
 *     System.out.println(x + " / 2");
 *     return x / 2;
 * });
 * PartialFun<Integer, String> oddByHalf = half.andThen(oddlyEnough);
 *
 * System.out.println(range(1, 10).filter(a -> {
 *     boolean definedAt = oddByHalf.isDefinedAt(a);
 *     if (definedAt) {
 *         System.out.print("isDefinedAt " + a + " " + definedAt + " call again ");
 *     } else {
 *         System.out.println("isDefinedAt " + a + " " + definedAt);
 *     }
 *     return definedAt;
 * }).map(oddByHalf).toList());
 *
 *      isDefinedAt 1 false
 *      2 / 2
 *      isDefinedAt 2 true call again 2 / 2
 *      1 is odd
 *      isDefinedAt 3 false
 *      4 / 2
 *      isDefinedAt 4 false
 *      isDefinedAt 5 false
 *      6 / 2
 *      isDefinedAt 6 true call again 6 / 2
 *      3 is odd
 *      isDefinedAt 7 false
 *      8 / 2
 *      isDefinedAt 8 false
 *      isDefinedAt 9 false
 *      [1 is odd, 3 is odd]
 *
 * 比 filter+map 快
 * System.out.println(range(1, 10).collect(oddByHalf).toList());
 *
 *      2 / 2
 *      1 is odd
 *      4 / 2
 *      6 / 2
 *      3 is odd
 *      8 / 2
 *      [1 is odd, 3 is odd]
 */
@SuppressWarnings({"unchecked", "unused"})
public interface PartialFun<A, B> extends Fun1<A, B> {
    static <A, B> Fun1<A, B> checkFallback() { return PartialFunObject.fallback_fn; }
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    static <B> boolean fallbackOccurred(B x) { return x == PartialFunObject.fallback_fn; }

    boolean isDefinedAt(A a);
    default PartialFun<A, B> orElse(PartialFun<? super A, ? extends B> that) { return new PartialFunObject.OrElse<>(this, that); }
    default <C> PartialFun<A, C> andThen(Fun1<? super B, ? extends C> after) {
        if (after instanceof PartialFun) {
            return andThen(after);
        } else {
            return new PartialFunObject.AndThen<>(this, after);
        }
    }
    default <C> PartialFun<A, C> andThen(PartialFun<? super B, ? extends C> after) { return new PartialFunObject.Combined<>(this, after); }
    default <C> PartialFun<C, B> compose(PartialFun<? super C, ? extends A> before) { return new PartialFunObject.Combined<>(before, this); }
    // PartialFun<A, B> -> Fun1<A, Option<B>>
    default Fun1<A, Option<B>> lift() { return new PartialFunObject.Lifted<>(this); }
    default B callOrElse(A a, Fun1<? super A, ? extends B> defVal) { return isDefinedAt(a) ? call(a) : defVal.call(a); }
    default <U> Fun1<A, Boolean> runWith(Fun1<? super B, ? extends U> action) {
        return a -> {
            B z = callOrElse(a, checkFallback());
            if (!fallbackOccurred(z)) {
                action.call(z);
                return true;
            } else {
                return false;
            }
        };
    }

    // Fun1<A, Option<B>> -> PartialFun<A, B>
    static <A, B> PartialFun<A, B> unlifted(Fun1<? super A, Option<B>> f) {
        if (f instanceof PartialFunObject.Lifted) {
            return Funs.narrow(((PartialFunObject.Lifted<A, B>) f).pf);
        } else {
            return new PartialFunObject.Unlifted<>(f);
        }
    }
    static <A, B> PartialFun<A, B> fromFun(Fun1<? super A, ? extends B> f) {
        return new PartialFun<A, B>() {
            @Override public boolean isDefinedAt(A a) { return true; }
            @Override public B call(A a) { return f.call(a); }
        };
    }
    static <A, B> PartialFun<A, B> empty() { return PartialFunObject.empty_pf; }
    // _ => false
    static <A> boolean cond(A a, PartialFun<? super A, Boolean> pf) { return pf.callOrElse(a, PartialFunObject.constFalse); }
    static <A, B> Option<B> condOpt(A a, PartialFun<? super A, ? extends B> pf) {
        B z = pf.callOrElse(a, checkFallback());
        if (!fallbackOccurred(z)) return Some(z); else return None();
    }


    @SuppressWarnings({"rawtypes", "unchecked", "BooleanMethodIsAlwaysInverted"})
    class PartialFunObject {
        private static final Ref<Fun1> ref = new Ref<>(null);
        private static final Fun1 fallback_fn = a -> ref.get();
        static { ref.set(fallback_fn); }

        private static final Fun1 constFalse = __ -> false;
        private static final PartialFun empty_pf = new Empty();

        private static class OrElse<A, B> implements PartialFun<A, B> {
            final PartialFun<A, B> f1;
            final PartialFun<A, B> f2;
            private OrElse(PartialFun<? super A, ? extends B> f1, PartialFun<? super A, ? extends B> f2) {
                this.f1 = Funs.narrow(f1);
                this.f2 = Funs.narrow(f2);
            }
            @Override public boolean isDefinedAt(A a) { return f1.isDefinedAt(a) || f2.isDefinedAt(a); }
            @Override public B call(A a) { return f1.callOrElse(a, f2); }
            @Override public B callOrElse(A a, Fun1<? super A, ? extends B> defVal) {
                B z = f1.callOrElse(a, checkFallback());
                if (!fallbackOccurred(z)) return z; else return f2.callOrElse(a, defVal);
            }
            @Override public PartialFun<A, B> orElse(PartialFun<? super A, ? extends B> that) {
                return new OrElse<>(f1, f2.orElse(that));
            }
            @Override public <C> PartialFun<A, C> andThen(Fun1<? super B, ? extends C> after) {
                return new OrElse<>(f1.andThen(after), f2.andThen(after));
            }
        }
        private static class AndThen<A, B, C> implements PartialFun<A, C> {
            final PartialFun<A, B> pf;
            final Fun1<? super B, ? extends C> k;
            private AndThen(PartialFun<A, B> pf, Fun1<? super B, ? extends C> k) { this.pf = pf; this.k = k; }
            @Override public boolean isDefinedAt(A a) { return pf.isDefinedAt(a); }
            @Override public C call(A a) { return k.call(pf.call(a)); }
            @Override public C callOrElse(A a, Fun1<? super A, ? extends C> defVal) {
                B z = pf.callOrElse(a, checkFallback());
                if (!fallbackOccurred(z)) return k.call(z); else return defVal.call(a);
            }
        }
        private static class Combined<A, B, C> implements PartialFun<A, C> {
            final PartialFun<A, B> pf;
            final PartialFun<B, C> k;
            private Combined(PartialFun<? super A, ? extends B> pf, PartialFun<? super B, ? extends C> k) {
                this.pf = Funs.narrow(pf);
                this.k = Funs.narrow(k);
            }
            // 注意 combine isDefinedAt 方法会触发 pf 的副作用
            @Override public boolean isDefinedAt(A a) {
                B b = pf.callOrElse(a, checkFallback());
                if (!fallbackOccurred(b)) return k.isDefinedAt(b); else return false;
            }
            @Override public C call(A a) { return k.call(pf.call(a)); }
            @Override public C callOrElse(A a, Fun1<? super A, ? extends C> defVal) {
                B pfv = pf.callOrElse(a, checkFallback());
                if (!fallbackOccurred(pfv)) return k.callOrElse(pfv, __ -> defVal.call(a)); else return defVal.call(a);
            }
        }
        private static class Lifted<A, B> implements Fun1<A, Option<B>> {
            final PartialFun<? super A, ? extends B> pf;
            private Lifted(PartialFun<? super A, ? extends B> pf) { this.pf = pf; }
            @Override public Option<B> call(A a) {
                B z = pf.callOrElse(a, checkFallback());
                if (!fallbackOccurred(z)) return Some(z); else return None();
            }
        }
        private static class Unlifted<A, B> implements PartialFun<A, B> {
            final Fun1<? super A, Option<B>> f;
            private Unlifted(Fun1<? super A, Option<B>> f) { this.f = f; }
            @Override public boolean isDefinedAt(A a) { return f.call(a).isDefined(); }
            @Override public B call(A a) { return callOrElse(a, PartialFun.empty()); }
            @Override public B callOrElse(A a, Fun1<? super A, ? extends B> defVal) { return f.call(a).getOrElse(() -> defVal.call(a)); }
            @Override public Fun1<A, Option<B>> lift() { return Funs.narrow(f); }
        }
        private static class Empty<A, B> implements PartialFun<A, B> {
            @Override public boolean isDefinedAt(A a) { return false; }
            @Override public B call(A a) { throw new MatchError(a); }
            @Override public PartialFun<A, B> orElse(PartialFun<? super A, ? extends B> that) { return ((PartialFun) that); }
            @Override public <C> PartialFun<A, C> andThen(Fun1<? super B, ? extends C> after) { return ((PartialFun<A, C>) this); }
            @Override public Fun1<A, Option<B>> lift() { return a -> None(); }
            @Override public <U> Fun1<A, Boolean> runWith(Fun1<? super B, ? extends U> action) { return ((Fun1) constFalse); }
        }
    }
}
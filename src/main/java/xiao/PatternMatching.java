package xiao;

import java.util.NoSuchElementException;

import static xiao.Helper.*;
import static xiao.Data.*;
import static xiao.Funs.*;

/**
 * @author chuxiaofeng
 */
@SuppressWarnings("unused")
public interface PatternMatching {
    class MatchError extends NoSuchElementException {  public MatchError(Object o) { super("no match: " + o); } }
    // 不要自己 new Case, 因为要处理一堆默认方法, callOrElse 需要特殊处理, 去继承 CaseImpl, 或者使用 Case 方法 创建 case
    interface Case<A, B> extends PartialFun<A, B> { }
    // interface Pattern<A, B> extends PartialFun<A, B> { }

    class CaseImpl<A, B> implements Case<A, B> {
        final PartialFun<A, B> pf;
        public CaseImpl(PartialFun<? super A, ? extends B> pf) {
            this.pf = Funs.narrow(pf);
        }
        public CaseImpl(Fun1<? super A, Boolean> p, Fun1<? super A, ? extends B> f) {
            this.pf = new PartialFun<A, B>() {
                // pf.pf.isDefinedAt(a) 处理, 这里还需要处理, 是因为 PartialFun.callOrElse 也调用 isDefinedAt
                // 方法会分派到父类, 所以 pf 这里也要处理
                @Override public boolean isDefinedAt(A a) {
                    try {
                        return p.call(a);
                    } catch (ClassCastException e) {
                        return false;
                    }
                }
                @Override public B call(A a) { return f.call(a); }
            };
        }
        // 这里之所以要处理 ClassCastException, 因为 Match 的 of 方法中 cases 参数的签名为了 case 后面的 call 类型自动推导,
        // e.g. Match.of(
        //      Case(Instance(Integer.class), i -> i + 1) // i 被推导成 int
        //      Case(Instance(Double.class), d -> d + 1) // d 被推导成 double
        // )
        // 处理成了 extends A, 而不是 super A
        // <B> B of(Case<? extends A, ? extends B>... cases) {
        // 这样导致实际的 case 不一定有能力处理 Match(v) 的实际参数 v, 导致触发 classCastException
        // e.g.
        // Fun1<Number, Number> plusOne = o -> Match(o).of(
        //      // 写成这样没问题, 会触发短路, 后面 lambda 实际参数类型是 Object, 应该是在 MethodHandle 自动生成的代码中触发类型检查失败
        //      Case(All(Instance(Integer.class), i -> i > 100), i -> i + 1)
        //      // 这样就会出问题...
        //      Case(All(i -> i > 100, Instance(Integer.class)), i -> i + 1)
        // );
        // plusOne.call(1L);
        @Override public boolean isDefinedAt(A a) {
            try {
                return pf.isDefinedAt(a);
            } catch (ClassCastException e) {
                return false;
            }
        }
        @Override public B call(A a)               { return pf.call(a); }
        @Override public Fun1<A, Option<B>> lift() { return pf.lift();  }
        @Override public Case<A, B> orElse(PartialFun<? super A, ? extends B> that)         { return new CaseImpl<>(pf.orElse(that));    }
        @Override public <C> Case<A, C> andThen(Fun1<? super B, ? extends C> after)         { return new CaseImpl<>(pf.andThen(after));  }
        @Override public <C> Case<A, C> andThen(PartialFun<? super B, ? extends C> after)   { return new CaseImpl<>(pf.andThen(after));  }
        @Override public <C> Case<C, B> compose(PartialFun<? super C, ? extends A> before)  { return new CaseImpl<>(pf.compose(before)); }
        @Override public B callOrElse(A a, Fun1<? super A, ? extends B> defVal)             { return pf.callOrElse(a, defVal);           }
        @Override public <U> Fun1<A, Boolean> runWith(Fun1<? super B, ? extends U> action)  { return pf.runWith(action);                 }
    }

    final class Match<A> {
        final A v;
        Match(A v) { this.v = v; }
        @SuppressWarnings({"CommentedOutCode", "unchecked"})
        @SafeVarargs public final <B> B of(Case<? extends/*注意这里!!!*/ A, ? extends B>... cases) {
            // 会重复计算
                /*for (Case<? extends A, ? extends B> it : cases) {
                    Case<A, B> c = ((Case<A, B>) it);
                    if (c.isDefinedAt(v)) return c.call(v);
                }
                throw new MatchError(v);*/
            if (cases.length == 0) throw new MatchError(v);
            else if (cases.length == 1) return ((Case<A, B>) cases[0]).callOrElse(v, a -> Throw(new MatchError(v)));
            else return Cases(cases).callOrElse(v, a -> Throw(new MatchError(v)));
        }
        @SuppressWarnings({"CommentedOutCode", "unchecked"})
        @SafeVarargs public final <B> Option<B> option(Case<? extends A, ? extends B>... cases) {
            // 会重复计算
                /*for (Case<? super A, ? extends B> it : cases) {
                    Case<A, B> c = ((Case<A, B>) it);
                    if (c.isDefinedAt(v)) return Some(c.call(v));
                }
                return None();*/
            if (cases.length == 0) return None();
            B z;
            if (cases.length == 1) {
                z = ((Case<A, B>) cases[0]).callOrElse(v, PartialFun.checkFallback());
            } else {
                z = Cases(cases).callOrElse(v, PartialFun.checkFallback());
            }
            if (!PartialFun.fallbackOccurred(z)) {
                return Some(z);
            } else {
                return None();
            }
        }
    }

    static <A> Match<A> Match(A a) { return new Match<>(a); }
    static <A, B> Case<A, B> Case(PartialFun<? super A, ? extends B> pf)                      { return new CaseImpl<>(pf); }
    static <A, B> Case<A, B> Case(Fun1<? super A, Boolean> p, Fun1<? super A, ? extends B> f) { return new CaseImpl<>(p, f); }
    static <A, B> Case<A, B> Case(Fun1<? super A, Boolean> p, Fun0<? extends B> f)            { return Case(p, (Fun1<? super A, ? extends B>) a -> f.call()); }
    // Act* 会和 Fun* 产生方法分派歧义 e.g. Case(Instance(Integer.class), i -> Left(i))
    // static <A, B> Case<A, B> Case(Fun1<? super A, Boolean> p, Act0 f)                         { return Case(p, a -> { f.call(); return null; });              }
    // static <A, B> Case<A, B> Case(Fun1<? super A, Boolean> p, Act1<? super A> f)              { return Case(p, a -> { f.call(a); return null; });             }
    static <A, B> Case<A, B> Case(Fun1<? super A, Boolean> p, B r)                            { return Case(p, a -> r);                                       }

    @SafeVarargs
    static <A, B> Case<A, B> Cases(Case<? extends A, ? extends B>... cases) {
        //noinspection unchecked
        return Iter(cases)
                .map(it -> ((Case<A, B>) it)) // 这里强制转型类型不安全, Case 中 处理 ClassCastException
                .reduce((a, b) -> Case(a.orElse(b)));
    }
}

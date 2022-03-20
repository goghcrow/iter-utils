package xiao;

import xiao.Helper.*;

import java.util.Objects;

import static xiao.Data.*;
import static xiao.Funs.*;

/**
 * 断言
 * for 模式匹配和偏函数
 * @author chuxiaofeng
 */
@SuppressWarnings("unused")
public interface Predicates {
    Fun1<Object, Boolean> __ = True(); // Const True
    @SafeVarargs
    static <A> Fun1<A, Boolean> All(Fun1<? super A, Boolean> ...ps) { return x -> Iter(ps).forall(p -> p.call(x)); }
    @SafeVarargs
    static <A> Fun1<A, Boolean> Any(Fun1<? super A, Boolean> ...ps) { return x -> Iter(ps).exists(p -> p.call(x)); }
    // @SafeVarargs static <A> Fun1<A, Boolean> None(Fun1<? super A, Boolean> ...ps) { return Not(Any(ps)); } // 和 Option.None 冲突 干掉
    static <A> Fun1<Iter<A>, Boolean> Exists(Fun1<? super A, Boolean> p) { return i -> i.exists(p); }
    static <A> Fun1<Iter<A>, Boolean> Forall(Fun1<? super A, Boolean> p) { return i -> i.forall(p); }
    static <A> Fun1<A, Boolean> Not(Fun1<? super A, Boolean> p) { return x -> !p.call(x); }
    // extends A原因举例: Instance(Number.class) 需要被声明称 Fun1<Object, Boolean>, 否则只能 test number 是否 instanceof number
    static <A> Fun1<A, Boolean> Instance(Class<? extends A> c)  { return c::isInstance; }
    static <A> Fun1<A, Boolean> Is(Object a)   { return x -> Objects.equals(a, x); }
    @SafeVarargs
    static <A> Fun1<A, Boolean> In(A ...xs)    { return it -> Iter(xs).exists(x -> Objects.equals(x, it)); }
    static <A> Fun1<A, Boolean> Null()         { return Objects::isNull;  }
    static <A> Fun1<A, Boolean> Null(Object a) { return Objects::isNull;  }
    static <A> Fun1<A, Boolean> True()         { return x -> true;  }
    static <A> Fun1<A, Boolean> True(Object a) { return x -> true;  }
    static <A> Fun1<A, Boolean> False()        { return x -> false; }
    static <A> Fun1<A, Boolean> False(Object a){ return x -> false; }
}
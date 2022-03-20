package xiao;

import xiao.Data.Option;
import xiao.Helper.*;

/**
 * @author chuxiaofeng
 */
@SuppressWarnings({"unchecked", "unused"})
public interface Funs {
    static <A, B> PartialFun<A, B> narrow(PartialFun<? super A, ? extends B> f) { return ((PartialFun<A, B>) f); }
    static <A, B> Fun1<A, B>       narrow(Fun1<? super A, ? extends B> f)       { return ((Fun1<A, B>) f);       }
    static <A, B> PartialFun<A, B> unlifted(Fun1<? super A, Option<B>> f)       { return PartialFun.unlifted(f); }

    interface Fun0<R>               { R call();                 }
    interface Fun1<A, R>            { R call(A a);              }
    interface Fun2<A, B, R>         { R call(A a, B b);         }
    interface Fun3<A, B, C, R>      { R call(A a, B b, C c);    }

    interface Act0                  { void call();              }
    interface Act1<A>               { void call(A a);           }
    interface Act2<A, B>            { void call(A a, B b);      }
    interface Act3<A, B, C>         { void call(A a, B b, C c); }

    interface JSneaky {
        interface CheckedRunnable            { void run() throws Throwable;             }
        interface CheckedSupplier<A>         { A get() throws Throwable;                }
        interface CheckedConsumer<A>         { void accept(A a) throws Throwable;       }
        interface CheckedBiConsumer<A, B>    { void accept(A a, B b) throws Throwable;  }
        interface CheckedFunction<A, R>      { R apply(A a) throws Throwable;           }
        interface CheckedBiFunction<A, B, R> { R apply(A a, B b) throws Throwable;      }
        interface CheckedPredicate<A>        { boolean test(A a) throws Throwable;      }
        interface CheckedBiPredicate<A, B>   { boolean test(A a, B b) throws Throwable; }

        static java.lang.Runnable                               sneak(CheckedRunnable runnable)            { return ()     -> { try { runnable.run();              } catch (Throwable t) { throw Sneaky.asRuntime(t); } }; }
        static <A> java.util.function.Supplier<A>               sneak(CheckedSupplier<A> supplier)         { return ()     -> { try { return supplier.get();       } catch (Throwable t) { throw Sneaky.asRuntime(t); } }; }
        static <A> java.util.function.Consumer<A>               sneak(CheckedConsumer<A> consumer)         { return a      -> { try { consumer.accept(a);          } catch (Throwable t) { throw Sneaky.asRuntime(t); } }; }
        static <A, B> java.util.function.BiConsumer<A, B>       sneak(CheckedBiConsumer<A, B> consumer)    { return (a, b) -> { try { consumer.accept(a, b);       } catch (Throwable t) { throw Sneaky.asRuntime(t); } }; }
        static <A, R> java.util.function.Function<A, R>         sneak(CheckedFunction<A, R> function)      { return a      -> { try { return function.apply(a);    } catch (Throwable t) { throw Sneaky.asRuntime(t); } }; }
        static <A, B, R> java.util.function.BiFunction<A, B, R> sneak(CheckedBiFunction<A, B, R> function) { return (a, b) -> { try { return function.apply(a, b); } catch (Throwable t) { throw Sneaky.asRuntime(t); } }; }
        static <A> java.util.function.Predicate<A>              sneak(CheckedPredicate<A> predicate)       { return a      -> { try { return predicate.test(a);    } catch (Throwable t) { throw Sneaky.asRuntime(t); } }; }
        static <A, B> java.util.function.BiPredicate<A, B>      sneak(CheckedBiPredicate<A, B> predicate)  { return (a, b) -> { try { return predicate.test(a, b); } catch (Throwable t) { throw Sneaky.asRuntime(t); } }; }
    }

    interface Sneaky {
        interface CFun0<R>              { R call() throws Throwable;                 }
        interface CFun1<A, R>           { R call(A a) throws Throwable;              }
        interface CFun2<A, B, R>        { R call(A a, B b) throws Throwable;         }
        interface CFun3<A, B, C, R>     { R call(A a, B b, C c) throws Throwable;    }
        interface CAct0                 { void call() throws Throwable;              }
        interface CAct1<A>              { void call(A a) throws Throwable;           }
        interface CAct2<A, B>           { void call(A a, B b) throws Throwable;      }
        interface CAct3<A, B, C>        { void call(A a, B b, C c) throws Throwable; }

        static <R> Fun0<R>                   sneak(CFun0<R> f)          { return ()        -> { try { return f.call();        } catch (Throwable t) { throw asRuntime(t); } }; }
        static <A, R> Fun1<A, R>             sneak(CFun1<A, R> f)       { return a         -> { try { return f.call(a);       } catch (Throwable t) { throw asRuntime(t); } }; }
        static <A, B, R> Fun2<A, B, R>       sneak(CFun2<A, B, R> f)    { return (a, b)    -> { try { return f.call(a,b);     } catch (Throwable t) { throw asRuntime(t); } }; }
        static <A, B, C, R> Fun3<A, B, C, R> sneak(CFun3<A, B, C, R> f) { return (a, b, c) -> { try { return f.call(a, b, c); } catch (Throwable t) { throw asRuntime(t); } }; }
        static Act0                          sneak(CAct0 f)             { return ()        -> { try { f.call();               } catch (Throwable t) { throw asRuntime(t); } }; }
        static <A> Act1<A>                   sneak(CAct1<A> f)          { return a         -> { try { f.call(a);              } catch (Throwable t) { throw asRuntime(t); } }; }
        static <A, B> Act2<A, B>             sneak(CAct2<A, B> f)       { return (a, b)    -> { try { f.call(a,b);            } catch (Throwable t) { throw asRuntime(t); } }; }
        static <A, B, C> Act3<A, B, C>       sneak(CAct3<A, B, C> f)    { return (a, b, c) -> { try { f.call(a, b, c);        } catch (Throwable t) { throw asRuntime(t); } }; }

        static RuntimeException asRuntime(Throwable t) {
            if (t instanceof RuntimeException) {
                return (RuntimeException) t;
            } else {
                return new RuntimeException(t);
            }
        }
    }
}

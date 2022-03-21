### 介绍
- Iter 一次性的迭代器
    - 各种构造 Iter 的工厂方法, range,from,iterate,tabulate...
    - 给迭代器加了大量方法, 全部转发 IterFuns, 用来写链式
    - 其他 Iter: SortedIter/PairIter/BufferedIter
    - 注意
        - 通用性 > 性能, 全部基于 hasNext/next, 没有 list等的特化处理
        - 迭代器都是一次性的, 并且大部分迭代器方法会导致原来的迭代器不能继续使用
        ```java
        iter2 = f(iter1)
        // 得到 iter2 之后如果需要继续使用原来的 iter1, 需要使用 duplicate 方法
        dup = duplicate(iter1)
        iter1 = dup._2
        iter2 = f(dup._1)
        // 或者 .toIterable() .toList() 之后, 得到 Iterable 来进行后续操作
        ```
- IterFuns 处理 Iterator/Iterable 的静态方法
    - 之所以搞了一堆静态实现, 是为了可以直接静态导入, 当函数用, 而不用 Stream.of 之类 "起手式" 的操作
    - 迭代器大部分方法抄袭 scala
- Iterators 各种迭代器, 私有, 内部实现细节, 也是抄袭 scala
- Funs 函数接口
    - 函数接口
        - 没有用 java.util.function.* 下的接口, 嫌弃名字长
        - 常规函数接口 Fun0~3 Act0~3, 没有添加 andThen compose 等方法
        - 没有Predicate, 用 Fun1<A, Boolean> 代替, and/or/negative 用 Predicates 中的 All/Any/Not 等替代
    - Sneaky
      - 加入了签名抛出受检异常的 CFun0-3 CAct0-3, 用来在 lambda 中使用抛受检异常的方法,
      - 并在 Sneaky 中 提供 sneak 方法将签名抛出受检异常的函数接口转换成对应正常的函数接口
      - 并提供了 java.util.function.* 的版本 JSneaky
    - narrow, 用来绕过 java 类型系统的限制
- Predicates 常用谓词函数组合
- Data 常用数据结构 和 数据构造器
    - 数据构造器(工厂方法)大写开头：Left/Right/None/Some/Tuple1~4/Iter/List/Set/SortedSet/Map/SortedMap
    - 数据结构
        - Ref: 就是 Ref, 最常见用法, 绕过 java lambda upValue 的 final 语义, 或者延迟构造递归结构
            - 也可是使用 array, 或者 Atomic*, e.g. AtomicReference
        - Lazy: 就是 Lazy
        - Monuple/Pair/Triple/Quadruple: 就是 Tuple1~4 之前只有 Pair, 懒得改名了
        - Option, Either: 最常用的两个 SumType, 用 CE 做的, 处理了exhaustiveness checking, 保证类型安全
    - 注意: 不考虑性能, immutable 粗暴的 unmodifiableSet 或者复制
- PartialFun 偏函数
    - 抄袭 scala 的偏函数, 处理了重复计算的问题
    - 所有需要 Fun1 的场景都可以使用 Case 或者 Cases 构造偏函数, 多个参数可以用 Tuple 桥接
- PatternMatching 模式匹配
    - 参考了 vavr, 用抄袭 scala 的偏函数做的, 没 vavr case0-n pattern0-n $ 之类零碎的东西
- Ord
- Contract 就是 Contract
- Helper

🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶
### 建议
- 静态导入使用
```java
import static xiao.Iter.*;
import static xiao.IterFuns.*;
import static xiao.Funs.*;
import static xiao.Predicates.*;
import static xiao.Data.*;
import static xiao.Helper.*;
```

🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶

### Example

```java
package xiao;

import lombok.Value;
import lombok.val;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static xiao.Data.*;
import static xiao.Funs.*;
import static xiao.Iter.*;
import static xiao.IterFuns.*;
import static xiao.PatternMatching.*;
import static xiao.Predicates.*;
import static xiao.Helper.*;
import static xiao.Test.*;

/**
 * @author chuxiaofeng
 */
public interface Example {

    static void main(String[] args) {
        data_constructor();
        iter_factory();
        iter_funs();
        predicates();
        partial_fun();
        pattern_matching();
        combining();
    }

    static void data_constructor() {
        test("Ref", () -> {
            Data.Ref<Integer> ref = Ref(1);
            assertEquals(ref.get(), 1);
            ref.set(2);
            assertEquals(ref.get(), 2);

            Ref<Integer> cnt = Ref(0);
            Data.Lazy<Integer> lazy = Lazy(() -> { cnt.set(cnt.get() + 1); return 1; });
            assertEquals(cnt.get(), 0);
            assertEquals(lazy.call(), 1);
            assertEquals(cnt.get(), 1);
            assertEquals(lazy.call(), 1);
            assertEquals(cnt.get(), 1);
        });

        test("Tuple", () -> {
            Tuple(1);
            Tuple(1, 2);
            Tuple(1, 2, 3);
            Tuple(1, 2, 3, 4);

            assertEquals(
                    Tuple(1, 2),
                    Pair(1, 2)
            );
        });

        test("Iter", () -> {
            assertEquals(
                    Iter(1, 2, 3, 4),
                    range(1, 5)
            );
            assertEquals(
                    Iter(1, 2, 3, 4).toList(),
                    List(1, 2, 3, 4)
            );
        });

        test("List", () -> {
            List();
            List(1, 2, 3);
        });

        test("Set", () -> {
            Set(1, 2, 3, 4);
            SortedSet(1, 2, 3, 4);
        });

        test("Map", () -> {
            Map<Integer, String> m = Map(Pair(1, "a"), Pair(2, "b"));
            assertEquals(
                    map(m.entrySet(), Data::Pair),
                    PairIter(m)
            );
            assertEquals(
                    Map(Pair(1, "a"), Pair(2, "b")),
                    Map(Iter(Pair(1, "a"), Pair(2, "b")))
            );
            assertEquals(
                    SortedMap(Pair(1, "a"), Pair(2, "b")),
                    SortedMap(Iter(Pair(1, "a"), Pair(2, "b")))
            );
        });
    }

    static void iter_factory() {
        assertEquals(
                empty(),
                Iter()
        );

        assertEquals(
                single(1),
                Iter(1)
        );

        test("fill", () -> {
            assertEquals(
                    Iter.fill(5, () -> 42),
                    Iter(42, 42, 42, 42, 42)
            );
            assertEquals(
                    Iter.fill(5, 3, () -> "A").map(Iter::toList),
                    Iter(
                            List("A", "A", "A"),
                            List("A", "A", "A"),
                            List("A", "A", "A"),
                            List("A", "A", "A"),
                            List("A", "A", "A")
                    )
            );
            assertEquals(
                    Iter.fill(5, 3, 2, () -> "A").map(it -> it.map(Iter::toList).toList()),
                    Iter(
                            List(
                                    List("A", "A"),
                                    List("A", "A"),
                                    List("A", "A")
                            ),
                            List(
                                    List("A", "A"),
                                    List("A", "A"),
                                    List("A", "A")
                            ),
                            List(
                                    List("A", "A"),
                                    List("A", "A"),
                                    List("A", "A")
                            ),
                            List(
                                    List("A", "A"),
                                    List("A", "A"),
                                    List("A", "A")
                            ),
                            List(
                                    List("A", "A"),
                                    List("A", "A"),
                                    List("A", "A")
                            )
                    )
            );

            assertEquals(
                    fill(2, () -> 1),
                    Iter(1, 1)
            );
            assertEquals(
                    fill(5, () -> "hello"),
                    Iter("hello", "hello", "hello", "hello", "hello")
            );
        });

        test("tabulate", () -> {
            assertEquals(
                    tabulate(3, Object::toString),
                    range(0, 3).map(Object::toString)
            );
            assertEquals(
                    tabulate(5, i -> "hello " + i),
                    Iter("hello 0", "hello 1", "hello 2", "hello 3", "hello 4")
            );
            assertEquals(
                    tabulate(5, i -> i),
                    Iter(0, 1, 2, 3, 4))
            ;
            assertEquals(
                    tabulate(5, 3, Data::Pair).map(Iter::toList),
                    Iter(
                            List(Tuple(0, 0), Tuple(0, 1), Tuple(0, 2)),
                            List(Tuple(1, 0), Tuple(1, 1), Tuple(1, 2)),
                            List(Tuple(2, 0), Tuple(2, 1), Tuple(2, 2)),
                            List(Tuple(3, 0), Tuple(3, 1), Tuple(3, 2)),
                            List(Tuple(4, 0), Tuple(4, 1), Tuple(4, 2))
                    )
            );
            assertEquals(
                    tabulate(5, 3, 2, Data::Tuple).map(it1 -> it1.map(Iter::toList).toList()),
                    Iter(
                            List(List(Tuple(0, 0, 0), Tuple(0, 0, 1)), List(Tuple(0, 1, 0), Tuple(0, 1, 1)), List(Tuple(0, 2, 0), Tuple(0, 2, 1))),
                            List(List(Tuple(1, 0, 0), Tuple(1, 0, 1)), List(Tuple(1, 1, 0), Tuple(1, 1, 1)), List(Tuple(1, 2, 0), Tuple(1, 2, 1))),
                            List(List(Tuple(2, 0, 0), Tuple(2, 0, 1)), List(Tuple(2, 1, 0), Tuple(2, 1, 1)), List(Tuple(2, 2, 0), Tuple(2, 2, 1))),
                            List(List(Tuple(3, 0, 0), Tuple(3, 0, 1)), List(Tuple(3, 1, 0), Tuple(3, 1, 1)), List(Tuple(3, 2, 0), Tuple(3, 2, 1))),
                            List(List(Tuple(4, 0, 0), Tuple(4, 0, 1)), List(Tuple(4, 1, 0), Tuple(4, 1, 1)), List(Tuple(4, 2, 0), Tuple(4, 2, 1))))
                    );
        });

        test("range", () -> {
            assertEquals(
                    range(1, 7),
                    concat(List(1, 2), List(3, 4), List(5, 6))
            );
            assertEquals(
                    range(1, 4),
                    Iter(1, 2, 3)
            );
            assertEquals(
                    range(3, 0, -1),
                    Iter(3, 2, 1)
            );
            assertEquals(
                    range(1, 10, 2),
                    Iter(1, 3, 5, 7, 9)
            );
            assertEquals(range(Integer.MAX_VALUE - 3, Integer.MAX_VALUE, 2), Iter(2147483644, 2147483646));
            assertEquals(range(1, 5, 2), Iter(1, 3));
            assertEquals(range(1, -5, -2), Iter(1, -1, -3));
        });

        test("from", () -> {
            assertEquals(
                    from(1).take(3),
                    Iter(1, 2, 3)
            );
            assertEquals(
                    from(1, 2).take(3),
                    Iter(1, 3, 5)
            );
            assertEquals(from(1, 2).take(3), Iter(1, 3, 5));
            assertEquals(from(1, -2).take(3), Iter(1, -1, -3));
        });

        test("iterate", () -> {
            // iterate : f(init), f(f(init)), f(f(f(init))) ...
            assertEquals(
                    iterate(5, i -> i + 5).take(3),
                    Iter(5, 10, 15)
            );
            assertEquals(
                    iterate(5, 3, i -> i + 5),
                    Iter(5, 10, 15)
            );
            assertEquals(iterate(0, 5, i -> i + 1), Iter(0, 1, 2, 3, 4));
        });

        test("unfold", () -> {
            assertEquals(
                    // 初始状态 i = 1
                    unfold(1, i -> {
                        if (i > 3) return None();
                            // 通过 pair 返回值同时更新状态
                        else return Some(Pair(i.toString(), i + 1));
                    }),
                    range(1, 4).map(Object::toString)
            );
            assertEquals(unfold("!", s -> Some(Pair(s, s + "!"))).take(5), Iter("!", "!!", "!!!", "!!!!", "!!!!!"));
            assertEquals(unfold("!", s -> s.length() > 5 ? None() : Some(Pair(s, s + "!"))), Iter("!", "!!", "!!!", "!!!!", "!!!!!"));
        });

        test("continually", () -> {
            try (val in = new ByteArrayInputStream("Hello".getBytes(StandardCharsets.UTF_8))) {
                try (val buf = new BufferedInputStream(in)) {
                    assertEquals(
                            continually(Funs.Sneaky.sneak(() -> buf.read())).takeWhile(i -> i != -1),
                            Iter('H', 'e', 'l', 'l', 'o').map(it -> ((int) it))
                    );
                }
            } catch (IOException ignored) {}
        });
    }

    static void iter_funs() {
        // 所有迭代器方法, 都有两个版本
        // 1. 静态方法
        //      func(Iterator<?> i, ...)
        //      func(Iterable<?> i, ...)
        // 2. 实例方法
        //      iterObj.func(...)
        // e.g.
        //      1. map(List(...), it -> ...)
        //      2. Iter(...).map(it -> ...)


        // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 basic 🔴🍇🍉🍌🍋🍊🍐🍎🍏

        test("size", () -> {
            assertEquals(Iter().size(), 0);
            assertEquals(Iter(1, 2, 3).size(), 3);
            assertEquals(size(List()), 0);
            assertEquals(size(List(1, 2, 3)), 3);
            assertEquals(size(Set(1, 2, 3)), 3);
        });

        test("isEmpty", () -> {
            assertTrue(Iter().isEmpty());
            assertFalse(Iter(1).isEmpty());
            assertTrue(isEmpty(List()));
            assertFalse(isEmpty(List(1)));
        });

        test("nonEmpty", () -> {
            assertFalse(Iter().nonEmpty());
            assertTrue(Iter(1).nonEmpty());
            assertFalse(nonEmpty(List()));
            assertTrue(nonEmpty(List(1)));
        });

        test("nextOption", () -> {
            assertEquals(Iter().nextOption(), None());
            assertEquals(Iter(1).nextOption(), Some(1));
            assertEquals(nextOption(List()), None());
            assertEquals(nextOption(List(1)), Some(1));
            assertEquals(nextOption(Set(1)), Some(1));
        });

        test("head", () -> {
            assertEquals(Iter(1, 2).head(), 1);
            assertEquals(head(List(1, 2)), 1);
        });

        test("headOption", () -> {
            assertEquals(Iter().headOption(), None());
            assertEquals(headOption(List()), None());
            assertEquals(Iter(1, 2).headOption(), Some(1));
            assertEquals(headOption(List(1, 2)), Some(1));
        });

        test("last", () -> {
            assertEquals(Iter(1, 2).last(), 2);
            assertEquals(last(List(1, 2)), 2);
        });

        test("lastOption", () -> {
            assertEquals(Iter().lastOption(), None());
            assertEquals(lastOption(List()), None());
            assertEquals(Iter(1, 2).lastOption(), Some(2));
            assertEquals(lastOption(List(1, 2)), Some(2));
        });

        test("init", () -> {
            assertEquals(range(1, 5).init(), range(1, 4));
            assertEquals(init(range(1, 5)), range(1, 4));
        });

        test("tail", () -> {
            assertEquals(range(1, 5).tail(), range(2, 5));
            assertEquals(tail(range(1, 5)), range(2, 5));
        });

        test("inits", () -> {
            assertEquals(
                    Iter(1, 2, 3).inits().map(Iter::toList),
                    Iter(
                            List(1, 2, 3), List(1, 2), List(1), List()
                    )
            );
        });

        test("tails", () -> {
            assertEquals(
                    Iter(1, 2, 3).tails().map(Iter::toList),
                    Iter(
                            List(1, 2, 3), List(2, 3), List(3), List()
                    )
            );
        });

        // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 翻转 & 去重 🔴🍇🍉🍌🍋🍊🍐🍎🍏

        test("reverse", () -> {
            assertEquals(
                    range(0, 10).reverse(),
                    range(9, -1, -1)
            );
        });

        test("distinct", () -> {
            assertEquals(
                    Iter(1, 2, 3, 4, 5, 4, 3, 2, 1, 2, 3, 4, 5, 6, 7, 8).distinct(),
                    range(1, 9)
            );
            assertEquals(
                    concat(
                            range(0, 5),
                            range(0, 5)
                    ).distinct(),
                    range(0, 5)
            );
        });

        @Value class Entity { int id; String name; }
        List<Entity> its = List(
                new Entity(1, "foo"),
                new Entity(2, "bar"),
                new Entity(1, "hello")
        );

        test("distinctBy", () -> {
            assertEquals(
                    range(0, 10).distinctBy(i -> i % 2),
                    Iter(0, 1)
            );
            assertEquals(
                    distinctBy(its, a -> a.id),
                    Iter(new Entity(1, "foo"), new Entity(2, "bar"))
            );

            assertEquals(Iter().distinctBy(a -> a), Iter());
            assertEquals(Iter().distinctBy1(a -> a), Iter());
        });

        // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 查找 & predicate 🔴🍇🍉🍌🍋🍊🍐🍎🍏

        test("find", () -> {
            assertEquals(
                    range(0, 10).find(i -> i > 9),
                    None()
            );
            // first
            assertEquals(
                    find(its, it -> it.id == 1),
                    Some(its.get(0))
            );
            assertEquals(
                    range(1, 8).find(i -> i % 2 == 0 && i > 4),
                    Some(6)
            );
            assertEquals(
                    range(1, 8).find(i -> i % 2 == 0 && i > 10),
                    None()
            );
        });

        test("indexWhere", () -> {
            assertEquals(
                    indexWhere(its, it -> it.id == 2, 0),
                    1
            );
            assertEquals(
                    indexWhere(its, it -> it.id == 1, 0),
                    0
            );
            assertEquals(
                    indexWhere(its, it -> it.id == 1, 1),
                    2
            );
        });

        test("indexOf", () -> {
            assertEquals(
                    indexOf(its, new Entity(2, "bar")),
                    1
            );
            assertEquals(
                    indexOf(its, new Entity(1, "hello"), 1),
                    2
            );
        });

        test("forall", () -> {
            assertFalse(forall(range(0, 10), i -> i % 2 == 0));
            assertTrue(forall(range(0, 10, 2), i -> i % 2 == 0));
        });

        test("exists", () -> {
            assertFalse(exists(range(0, 10), it -> it > 9));
            assertTrue(exists(range(0, 10), it -> it >= 9));
            assertTrue(range(1, 8).exists(i -> i > 1));
            assertFalse(range(1, 8).exists(i -> i < 0));
        });

        test("contains", () -> {
            assertFalse(contains(range(0, 10), 10));
            assertTrue(contains(range(0, 10), 9));
        });

        test("sameElements", () -> {
            assertTrue(sameElements(Iter(1, 2, 3, 4), range(1, 5)));
        });

        test("corresponds", () -> {
            assertTrue(corresponds(Iter(1, 2, 3, 4), range(0, 4), (l, r) -> l == r + 1));
        });

        // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 映射 & 过滤 🔴🍇🍉🍌🍋🍊🍐🍎🍏

        test("map", () -> {
            assertEquals(
                    range(1, 6).map(i -> i * 2),
                    Iter(2, 4, 6, 8, 10)
            );
            assertEquals(
                    map(range(0, 5), i -> i + 1),
                    range(1, 6)
            );
            assertEquals(
                    map(List(1, 2, 3), List("a", "b", "c"), (i, a) -> i + a),
                    Iter("1a", "2b", "3c")
            );
        });

        test("flatten", () -> {
            assertEquals(
                    flatten(List(1, 2, 3), List(4, 5, 6)),
                    range(1, 7)
            );
        });

        test("flatMap", () -> {
            assertEquals(
                    // map then flatten
                    flatMap(Iter(1, 2, 3), i -> range(0, i)),
                    // ...range(0, 1), ...range(0, 2), ...range(0, 3)
                    Iter(0, 0, 1, 0, 1, 2)
            );
            assertEquals(Iter("HELLO", "WORLD", "!").flatMap(a -> Iter(a.split(""))), Iter("H", "E", "L", "L", "O", "W", "O", "R", "L", "D", "!"));
            assertEquals(Data.<String>Iter().flatMap(a -> Iter(a.split(""))), Iter());

            assertEquals(Iter("HELLO", "WORLD", "!").flatMap1(a -> Iter(a.split(""))), Iter("H", "E", "L", "L", "O", "W", "O", "R", "L", "D", "!"));
            assertEquals(Data.<String>Iter().flatMap1(a -> Iter(a.split(""))), Iter());

            assertEquals(Iter("HELLO", "WORLD", "!").flatMap2(a -> Iter(a.split(""))), Iter("H", "E", "L", "L", "O", "W", "O", "R", "L", "D", "!"));
            assertEquals(Data.<String>Iter().flatMap2(a -> Iter(a.split(""))), Iter());
        });

        test("filter", () -> {
            assertEquals(
                    range(1, 6).filter(i -> i % 2 == 1),
                    Iter(1, 3, 5)
            );
            assertEquals(
                    range(0, 10).filter(i -> i % 2 == 0),
                    range(0, 10, 2)
            );
            assertEquals(
                    range(0, 10).filterNot(i -> i % 2 == 0),
                    range(1, 10, 2)
            );

            assertEquals(range(0, 10).filter(it -> false), Iter());
            assertEquals(range(0, 10).filter(it -> it % 2 == 0), Iter(0, 2, 4, 6, 8));

            assertEquals(range(0, 10).filter1(it -> false), Iter());
            assertEquals(range(0, 10).filter1(it -> it % 2 == 0), Iter(0, 2, 4, 6, 8));

            assertEquals(range(0, 10).filter2(it -> false), Iter());
            assertEquals(range(0, 10).filter2(it -> it % 2 == 0), Iter(0, 2, 4, 6, 8));
        });

        test("collect", () -> {
            assertEquals(
                    range(0, 10).collect(
                            Cases(
                                    Case(i -> i % 2 == 0, i -> "even: " + i),
                                    Case(i -> i % 2 == 1, i -> "odd: " + i)
                            )
                    ),
                    range(0, 10).map(i -> (i % 2 == 0 ? "even: " : "odd: ") + i)
            );
            assertEquals(
                    range(0, 10).collect(
                            Case(i -> i % 2 == 0, i -> "even: " + i)
                    ),
                    range(0, 10).filter(i -> i % 2 == 0).map(i -> "even: " + i)
            );
        });

        test("collectFirst", () -> {
            assertEquals(
                    range(0, 10).collectFirst(
                            Cases(
                                    Case(i -> i % 2 == 0, i -> "even: " + i),
                                    Case(i -> i % 2 == 1, i -> "odd: " + i)
                            )
                    ),
                    Some("even: 0")
            );
            assertEquals(
                    range(1, 10).collectFirst(
                            Cases(
                                    Case(i -> i % 2 == 0, i -> "even: " + i),
                                    Case(i -> i % 2 == 1, i -> "odd: " + i)
                            )
                    ),
                    Some("odd: 1")
            );
            assertEquals(
                    range(1, 10).collectFirst(Case(a -> false, a -> a)),
                    None()
            );
            {
                Case pf = Case(Instance(Integer.class), i -> i * 10);
                Option a = Iter("a", 1, 5L).collectFirst(pf);
                assertTrue(a.isDefined());
                assertEquals(a.get(), 10);
            }
        });

        // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 更新 & 替换 🔴🍇🍉🍌🍋🍊🍐🍎🍏

        test("update", () -> {
            assertEquals(
                    range(0, 5).update(0, 42),
                    Iter(42, 1, 2, 3, 4)
            );
            assertEquals(
                    range(0, 5).update(4, 42),
                    Iter(0, 1, 2, 3, 42)
            );
        });

        test("patch", () -> {
            assertEquals(
                    range(1, 7).patch(2, Iter(), 2),
                    Iter(1, 2, 5, 6)
            );
            assertEquals(
                    range(1, 7).patch(2, Iter(1), 2),
                    Iter(1, 2, 1, 5, 6)
            );
            assertEquals(
                    range(1, 7).patch(2, Iter(1, 2, 3), 2),
                    Iter(1, 2, 1, 2, 3, 5, 6)
            );
            assertEquals(
                    range(1, 7).patch(0, Iter(42), 2),
                    Iter(42, 3, 4, 5, 6)
            );
            assertEquals(range(1, 7).patch(-1, Iter(42), 2), Iter(42, 3, 4, 5, 6));
            assertEquals(range(1, 7).patch(100, Iter(42), 2), Iter(1, 2, 3, 4, 5, 6, 42));
        });

        // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 [条件/范围]裁剪 🔴🍇🍉🍌🍋🍊🍐🍎🍏

        test("remove", () -> {
            assertEquals(
                    Iter(1, 2, 3, 4, 5).remove(3),
                    Iter(1, 2, 4, 5)
            );
        });

        test("removeAll", () -> {
            assertEquals(
                    Iter(1, 2, 3, 4, 5).removeAll(Iter(1,3,5)),
                    Iter(2, 4)
            );
        });

        test("slice", () -> {
            assertEquals(
                    range(1, 6).slice(1, 4),
                    Iter(2, 3, 4)
            );
            assertEquals(
                    Iter().slice(0, 1),
                    Iter()
            );
            assertEquals(
                    Iter(1, 2, 3, 4, 5).slice(3, 2),
                    Iter()
            );
            assertEquals(
                    Iter(1, 2, 3, 4, 5).slice(0, -1),
                    Iter(1, 2, 3, 4, 5)
            );
            assertEquals(
                    Iter(1, 2, 3, 4, 5).slice(1, -1),
                    Iter(2, 3, 4, 5)
            );
            assertEquals(
                    Iter(1, 2, 3, 4, 5).slice(4, 5),
                    Iter(5)
            );
        });

        test("take", () -> {
            assertEquals(
                    range(1, 6).take(2),
                    Iter(1, 2)
            );
        });

        test("takeRight", () -> {
            assertEquals(Iter().takeRight(2), Iter());
            assertEquals(Iter(1).takeRight(2), Iter(1));
            assertEquals(Iter(1, 2).takeRight(2), Iter(1, 2));
            assertEquals(Iter(1, 2, 3).takeRight(2), Iter(2, 3));
            assertEquals(Iter(1, 2, 3).takeRight(0), Iter());
        });

        test("takeWhile", () -> {
            assertEquals(Iter(1, 2 ,3, 4, 5).takeWhile(a -> a < 1), Iter());
            assertEquals(Iter(1, 2 ,3, 4, 5).takeWhile(a -> a < 5), Iter(1, 2, 3, 4));
        });

        test("drop", () -> {
            assertEquals(
                    range(1, 6).drop(2),
                    Iter(3, 4, 5)
            );
        });

        test("dropRight", () -> {
            assertEquals(Iter().dropRight(2), Iter());
            assertEquals(Iter(1).dropRight(2), Iter());
            assertEquals(range(1, 4).dropRight(0), Iter(1, 2, 3));
            assertEquals(range(1, 4).dropRight(2), Iter(1));
        });

        test("dropWhile", () -> {
            assertEquals(Iter().takeWhile(a -> false), Iter());
            assertEquals(Iter().takeWhile(a -> true), Iter());
            assertEquals(Iter(1, 2 ,3, 4, 5).dropWhile(a -> a < 1), Iter(1, 2, 3, 4, 5));
            assertEquals(Iter(1, 2 ,3, 4, 5).dropWhile(a -> a < 4), Iter(4, 5));
        });

        // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 拼接 & 补齐 🔴🍇🍉🍌🍋🍊🍐🍎🍏

        test("concat", () -> {
            assertEquals(Iter().concat(Iter()).concat(Iter()), Iter());
            assertEquals(Iter().concat(Iter()).concat(Iter(1)), Iter(1));
            assertEquals(Iter().concat(Iter(1)).concat(Iter()), Iter(1));
            assertEquals(Iter(1).concat(Iter()).concat(Iter(1)), Iter(1, 1));
            assertEquals(Iter(1, 2 ,3, 4, 5).concat(Iter()).concat(Iter(6, 7)), Iter(1, 2, 3, 4, 5, 6, 7));
            assertEquals(flatten(Iter(1, 2, 3, 4, 5), Iter(6, 7)), Iter(1, 2, 3, 4, 5, 6, 7));

            assertEquals(Iter().concat1(Iter()).concat1(Iter()), Iter());
            assertEquals(Iter().concat1(Iter()).concat1(Iter(1)), Iter(1));
            assertEquals(Iter().concat1(Iter(1)).concat1(Iter()), Iter(1));
            assertEquals(Iter(1).concat1(Iter()).concat1(Iter(1)), Iter(1, 1));
            assertEquals(Iter(1, 2 ,3, 4, 5).concat1(Iter()).concat1(Iter(6, 7)), Iter(1, 2, 3, 4, 5, 6, 7));
            assertEquals(flatten(Iter(1, 2, 3, 4, 5), Iter(6, 7)), Iter(1, 2, 3, 4, 5, 6, 7));
        });

        test("append", () -> {});

        test("prepend", () -> {});

        test("padTo", () -> {
            assertEquals(Iter().padTo(2, 42), Iter(42, 42));
            assertEquals(Iter(1, 2).padTo(2, 42), Iter(1, 2));
        });

        // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 zip & unzip & transpose 🔴🍇🍉🍌🍋🍊🍐🍎🍏

        test("zip", () -> {
            assertEquals(range(0, 5).zip(range(10, 14)), Iter(Tuple(0, 10), Tuple(1, 11), Tuple(2, 12), Tuple(3, 13)));
        });

        test("zipWithIndex", () -> {
            assertEquals(range(10, 15).zipWithIndex(),
                    Iter(Tuple(10, 0), Tuple(11, 1), Tuple(12, 2), Tuple(13, 3), Tuple(14, 4)));
        });

        test("zipAll", () -> {
            assertEquals(range(0, 5).zipAll(range(10, 14), 0, 42),
                    Iter(Tuple(0, 10), Tuple(1, 11), Tuple(2, 12), Tuple(3, 13), Tuple(4, 42)));
            assertEquals(range(10, 14).zipAll(range(0, 5), 0, 42),
                    Iter(Tuple(10, 0), Tuple(11, 1), Tuple(12, 2), Tuple(13, 3), Tuple(0, 4)));
        });

        test("unzip", () -> {
            Pair<Iter<Integer>, Iter<String>> p = unzip(Iter(
                    Tuple(1, "one"),
                    Tuple(2, "two"),
                    Tuple(3, "three")
            ));

            assertEquals(p._1, Iter(1, 2, 3));
            assertEquals(p._2, Iter("one", "two", "three"));

            Triple<Iter<Integer>, Iter<String>, Iter<Character>> t = unzip3(Iter(
                    Tuple(1, "one", '1'),
                    Tuple(2, "two", '2'),
                    Tuple(3, "three", '3')
            ));
            assertEquals(t._1, Iter(1, 2, 3));
            assertEquals(t._2, Iter("one", "two", "three"));
            assertEquals(t._3, Iter('1', '2', '3'));
        });

        test("unzip3", () -> {});

        test("transpose", () -> {
            assertEquals(transpose(Iter(
                    Iter(1, 2, 3),
                    Iter(4, 5, 6))).map(Iter::toList), Iter(List(1, 4), List(2, 5), List(3, 6)));
            assertEquals(transpose(List(
                    Set(1, 2, 3),
                    Set(4, 5, 6))).map(Iter::toList), Iter(List(1, 4), List(2, 5), List(3, 6)));
            assertEquals(transpose(List(
                    List(1, 2, 3),
                    List(4, 5, 6))).map(Iter::toList), Iter(List(1, 4), List(2, 5), List(3, 6)));
        });

        // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 分组[映射/聚合] 🔴🍇🍉🍌🍋🍊🍐🍎🍏

        test("grouped", () -> {
            assertEquals(sliding(range(1, 7), 2, 3), Iter(
                    List(1, 2), List(4, 5)
            ));

            {
                assertEquals(grouped(Iter(), 3).withPartial(true), Iter());
                assertEquals(grouped(Iter(), 3).withPartial(false), Iter());
                Iter<Integer> pad = iterate(20, a -> a + 5);
                assertEquals(grouped(Iter(), 3).withPadding(pad::next), Iter());
            }
            {
                assertEquals(grouped(range(0, 1), 3).withPartial(true), Iter(List(0)));
                assertEquals(grouped(range(0, 1), 3).withPartial(false), Iter());
                Iter<Integer> pad = iterate(20, a -> a + 5);
                assertEquals(grouped(range(0, 1), 3).withPadding(pad::next), Iter(List(0, 20, 25)));
            }

            assertEquals(grouped(range(1, 8), 3).withPartial(true), Iter(
                    List(1, 2, 3), List(4, 5, 6), List(7)
            ));
            assertEquals(grouped(range(1, 8), 3).withPartial(false), Iter(
                    List(1, 2, 3), List(4, 5, 6)
            ));
            Iter<Integer> pad = iterate(20, a -> a + 5);
            assertEquals(grouped(range(1, 8), 3).withPadding(pad::next), Iter(
                    List(1, 2, 3), List(4, 5, 6), List(7, 20, 25)
            ));
        });

        test("sliding", () -> {
            {
                assertEquals(sliding(Iter(), 3).withPartial(true), Iter());
                assertEquals(sliding(Iter(), 3).withPartial(false), Iter());
                Iter<Integer> pad = iterate(20, a -> a + 5);
                assertEquals(sliding(Iter(), 3).withPadding(pad::next), Iter());
            }
            {
                assertEquals(sliding(range(0, 1), 3).withPartial(true), Iter(List(0)));
                assertEquals(sliding(range(0, 1), 3).withPartial(false), Iter());
                Iter<Integer> pad = iterate(20, a -> a + 5);
                assertEquals(sliding(range(0, 1), 3).withPadding(pad::next), Iter(List(0, 20, 25)));
            }

            assertEquals(sliding(range(1, 6), 3).withPartial(true), Iter(
                    List(1, 2, 3), List(2, 3, 4), List(3, 4, 5)
            ));
            assertEquals(sliding(range(1, 6), 3, 1).withPartial(true), Iter(
                    List(1, 2, 3), List(2, 3, 4), List(3, 4, 5)
            ));
            assertEquals(sliding(range(1, 6), 3, 2).withPartial(true), Iter(
                    List(1, 2, 3), List(3, 4, 5)
            ));
            assertEquals(sliding(range(1, 6), 3, 3).withPartial(true), Iter(
                    List(1, 2, 3), List(4, 5)
            ));
            assertEquals(sliding(range(1, 6), 3, 3).withPartial(false), Iter(
                    List(1, 2, 3)
            ));
            assertEquals(sliding(range(1, 6), 3, 4).withPartial(true), Iter(
                    List(1, 2, 3), List(5)
            ));
            assertEquals(sliding(range(1, 6), 4, 3).withPartial(true), Iter(
                    List(1, 2, 3, 4), List(4, 5)
            ));
            assertEquals(sliding(range(1, 6), 4, 3).withPartial(false), Iter(
                    List(1, 2, 3, 4)
            ));
            Iter<Integer> pad = iterate(20, a -> a + 5);
            assertEquals(sliding(range(1, 6), 4, 3).withPadding(pad::next), Iter(
                    List(1, 2, 3, 4), List(4, 5, 20, 25)
            ));

            assertEquals(sliding(Iter(), 2), Iter());
            assertEquals(sliding(Iter(1), 2), Iter(List(1)));
            assertEquals(sliding(Iter(1, 2), 2), Iter(List(1, 2)));
            assertEquals(sliding(Iter(1, 2, 3), 2), Iter(List(1, 2), List(2, 3)));

            assertEquals(sliding(range(1, 6), 2, 2), Iter(List(1, 2), List(3, 4), List(5)));
            assertEquals(sliding(range(1, 7), 2, 3), Iter(List(1, 2), List(4, 5)));
        });

        test("groupBy", () -> {
            Map<Integer, List<Integer>> m = range(1, 8).groupBy(i -> i % 2);
            assertEquals(m.get(0), List(2, 4, 6));
            assertEquals(m.get(1), List(1, 3, 5, 7));

            // (xs groupBy f)(k) = xs filter (x => f(x) == k)
            boolean k = true;
            Fun1<Integer, Boolean> isEven = i -> i % 2 == 0;
            assertEquals(
                    range(0, 10).groupBy(isEven).get(k),
                    range(0, 10).filter(i -> Objects.equals(isEven.call(i), k)).toList()
            );
        });

        test("groupMap", () -> {
            @Value class User { String name; int age; }
            class NamesByAge {
                Map<Integer, List<String>> go(List<User> users) { return groupMap(users, u -> u.age, u -> u.name); }
            }
            assertEquals(new NamesByAge().go(
                    List(
                            new User("a", 1),
                            new User("b", 2),
                            new User("c", 1),
                            new User("d", 2))
            ).get(2), List("b", "d"));
        });

        test("groupMapReduce", () -> {
            @Value class User { String name; int age; }
            class NamesByAge {
                Map<Integer, String> go(List<User> users) {
                    return groupMapReduce(users, u -> u.age, u -> u.name, (c, it) -> c + "_" + it);
                }
            }
            Map<Integer, String> m = new NamesByAge().go(
                    List(
                            new User("a", 1),
                            new User("b", 2),
                            new User("c", 1),
                            new User("d", 2))
            );
            assertEquals(m.get(1), "a_c");
            assertEquals(m.get(2), "b_d");
        });

        // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 [条件|Either|下标]分区 🔴🍇🍉🍌🍋🍊🍐🍎🍏

        test("partitionMap", () -> {
            Pair<Iter<Integer>, Iter<String>> p = Iter(1, "one", 2, "two", 3, "three")
                    .partitionMap(Cases(
                            Case(Instance(Integer.class), Data::Left),
                            Case(Instance(String.class), Data::Right)
                    ));
            assertEquals(p._1, Iter(1, 2, 3));
            assertEquals(p._2, Iter("one", "two", "three"));
        });

        test("partition", () -> {
            Pair<Iter<Integer>, Iter<Integer>> p = range(0, 5).partition(it -> it >= 2);
            assertEquals(p._1, Iter(2, 3, 4));
            assertEquals(p._2, Iter(0, 1));

            p = Iter(0, 4, 1, 3, 2).partition(it -> it >= 2);
            assertEquals(p._1, Iter(4, 3, 2));
            assertEquals(p._2, Iter(0, 1));
        });

        test("span", () -> {
            Pair<Iter<Integer>, Iter<Integer>> p;
            p = range(0, 5).span(it -> it < 2);
            assertEquals(p._1, Iter(0, 1));
            assertEquals(p._2, Iter(2, 3, 4));
            {
                p = range(0, 5).span(it -> it < 2);
                assertEquals(p._2, Iter(2, 3, 4));
                assertEquals(p._1, Iter(0, 1));
            }
            {
                p = range(0, 5).span1(it -> it < 2);
                assertEquals(p._2, Iter(2, 3, 4));
                assertEquals(p._1, Iter(0, 1));
            }

            p = range(0, 5).span(it -> it < 5);
            assertEquals(p._1, Iter(0, 1, 2, 3, 4));
            assertEquals(p._2, Iter());
            {
                p = range(0, 5).span(it -> it < 5);
                assertEquals(p._2, Iter());
                assertEquals(p._1, Iter(0, 1, 2, 3, 4));
            }
            {
                p = range(0, 5).span1(it -> it < 5);
                assertEquals(p._2, Iter());
                assertEquals(p._1, Iter(0, 1, 2, 3, 4));
            }

            p = range(0, 5).span(it -> it >= 5);
            assertEquals(p._1, Iter());
            assertEquals(p._2, Iter(0, 1, 2, 3, 4));
            {
                p = range(0, 5).span(it -> it >= 5);
                assertEquals(p._2, Iter(0, 1, 2, 3, 4));
                assertEquals(p._1, Iter());
            }
            {
                p = range(0, 5).span1(it -> it >= 5);
                assertEquals(p._2, Iter(0, 1, 2, 3, 4));
                assertEquals(p._1, Iter());
            }

            p = Iter(0, 4, 1, 3, 2).span(it -> it < 2);
            assertEquals(p._1, Iter(0));
            assertEquals(p._2, Iter(4, 1, 3, 2));
            {
                p = Iter(0, 4, 1, 3, 2).span(it -> it < 2);
                assertEquals(p._2, Iter(4, 1, 3, 2));
                assertEquals(p._1, Iter(0));
            }
            {
                p = Iter(0, 4, 1, 3, 2).span1(it -> it < 2);
                assertEquals(p._2, Iter(4, 1, 3, 2));
                assertEquals(p._1, Iter(0));
            }
        });

        test("splitAt", () -> {
            val it = range(0, 10).splitAt(5);
            assertEquals(it._1, range(0, 5));
            assertEquals(it._2, range(5, 10));
        });

        // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 聚合: scan 🔴🍇🍉🍌🍋🍊🍐🍎🍏

        test("scan", () -> {});

        test("scanLeft", () -> {
            assertEquals(Iter(1, 2, 3, 4).scanLeft(0, (acc, i) -> acc + i), Iter(0, 1, 3, 6, 10));
        });

        test("scanRight", () -> {
            assertEquals(Iter(1, 2, 3, 4).scanRight(0, (i, acc) -> acc + i), Iter(0, 4, 7, 9, 10));
        });

        test("aggregate", () -> {});

        test("foldLeft", () -> {
            assertEquals(
                    range(1, 8).foldLeft(0, Integer::sum),
                    28
            );
            assertEquals(
                    range(1, 8).foldLeft(1, (x, y) -> x * y),
                    5040
            );
            assertEquals(Iter("abcde".split("")).foldLeft("", (r, s) -> r + s), "abcde");
        });

        test("foldRight", () -> {
            assertEquals(Iter("abcde".split("")).foldRight("", (s, r) -> r + s), "edcba");
            assertEquals(Iter("abcde".split("")).foldRight1("", (s, r) -> r + s), "edcba");
        });

        test("fold", () -> {

        });

        // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 聚合: reduce 🔴🍇🍉🍌🍋🍊🍐🍎🍏

        test("reduce", () -> {});

        test("reduceOption", () -> {});

        test("reduceLeft", () -> {});

        test("reduceRight", () -> {});

        test("reduceLeftOption", () -> {});

        test("reduceRightOption", () -> {});

        // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 聚合: join 🔴🍇🍉🍌🍋🍊🍐🍎🍏

        test("addString", () -> {});

        test("mkString", () -> {
            assertEquals(
                    range(1, 8).mkString(","),
                    "1,2,3,4,5,6,7"
            );
            assertEquals(
                    range(1, 8).mkString("[", ",", "]"),
                    "[1,2,3,4,5,6,7]"
            );
        });

        // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 聚合: count & sum 🔴🍇🍉🍌🍋🍊🍐🍎🍏

        test("count", () -> {});

        test("sumInt", () -> {});

        test("sumDouble", () -> {});

        test("productInt", () -> {});

        test("productDouble", () -> {});

        // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 聚合: compare 🔴🍇🍉🍌🍋🍊🍐🍎🍏

        test("sizeCompare", () -> {
            assertTrue(range(1, 4).sizeCompare(1) > 0);
            assertTrue(range(1, 4).sizeCompare(3) == 0);
            assertTrue(range(1, 4).sizeCompare(4) < 0);

            assertTrue(range(1, 4).sizeCompare(Iter()) > 0);
            assertTrue(range(1, 4).sizeCompare(Iter(2, 3, 4)) == 0);
            assertTrue(range(1, 4).sizeCompare(range(1, 5)) < 0);
        });

        test("min max", () -> {
            {
                Funs.Fun0<Iter<Integer>> i = () -> Iter(3, 5, 2, 4, 1);
                assertEquals(min(i.call()), 1);
                assertEquals(max(i.call()), 5);
                assertEquals(minBy(i.call(), a -> 10 - a), 5);
                assertEquals(maxBy(i.call(), a -> 10 - a), 1);
            }

            {
                @Value class Int { Integer i; }
                Funs.Fun0<Iter<Int>> i = () -> Iter(3, 5, 2, 4, 1).map(Int::new);
                Comparator<Int> ord = Comparator.comparingInt(a -> a.i);
                assertEquals(i.call().min(ord).i, 1);
                assertEquals(i.call().max(ord).i, 5);
                assertEquals(i.call().minBy(a -> new Int(10 - a.i), ord).i, 5);
                assertEquals(i.call().maxBy(a -> new Int(10 - a.i), ord).i, 1);
            }
            {
                @Value class Int { Integer i; }
                Funs.Fun0<Iter<Int>> i = () -> Iter(3, 5, 2, 4, 1).map(Int::new);
                Comparator<Int> ord = Comparator.comparingInt(a -> a.i);
                assertEquals(min(i.call(), ord).i, 1);
                assertEquals(max(i.call(), ord).i, 5);
                assertEquals(minBy(i.call(), a -> new Int(10 - a.i), ord).i, 5);
                assertEquals(maxBy(i.call(), a -> new Int(10 - a.i), ord).i, 1);
            }
        });

//        test("min", () -> {});
//
//        test("minOption", () -> {});
//
//        test("max", () -> {});
//
//        test("maxOption", () -> {});
//
//        test("minBy", () -> {});
//
//        test("minByOption", () -> {});
//
//        test("maxBy", () -> {});
//
//        test("maxByOption", () -> {});


        // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 特殊: 复制 🔴🍇🍉🍌🍋🍊🍐🍎🍏

        test("duplicate", () -> {});

        // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 sideEffect 🔴🍇🍉🍌🍋🍊🍐🍎🍏

        test("tapEach", () -> {
            range(0, 5).tapEach(System.out::print).toList();
            println();
        });

        test("foreach", () -> {});

        // 🍓🍅🥝🥥🍍🥭🍑🍒🍈🫐 toXXX 🔴🍇🍉🍌🍋🍊🍐🍎🍏

        test("addAll", () -> {});

        test("putAll", () -> {});

        test("toIterable", () -> {});

        test("toList", () -> {});

        test("toArray", () -> {});

        test("toSet", () -> {});

        test("toSortedSet", () -> {});

        test("toStream", () -> {});

        test("buffered", () -> {});
    }

    static void predicates() {
        test("Predicates", () -> {
            Fun1<Integer, Boolean> gt1 = x -> x > 1;
            Fun1<Integer, Boolean> gt2 = x -> x > 2;

            assertTrue(All().call(0));
            assertTrue(All(gt1, gt2).call(3));
            assertFalse(All(gt1, gt2).call(2));

            // None
            assertTrue(Not(Any()).call(0));
            assertTrue(Not(Any(gt1, gt2)).call(1));
            assertFalse(Not(Any(gt1, gt2)).call(2));

            assertFalse(Any().call(0));
            assertTrue(Any(gt1, gt2).call(3));
            assertTrue(Any(gt1, gt2).call(2));
            assertFalse(Any(gt1, gt2).call(1));

            Fun1<Iter<Integer>, Boolean> existsGt1 = Exists(gt1);
            assertTrue(existsGt1.call(range(0, 3)));
            assertFalse(existsGt1.call(range(0, 2)));

            Fun1<Iter<Integer>, Boolean> forallGt1 = Forall(gt1);
            assertFalse(forallGt1.call(Iter(0, 1, 2)));
            assertTrue(forallGt1.call(Iter(2, 3, 4)));

            Fun1<Object, Boolean> insNum = Instance(Number.class);
            assertTrue(insNum.call(1));
            assertFalse(insNum.call("1"));

            Fun1<Object, Boolean> is1 = Is(1);
            assertTrue(is1.call(1));
            assertFalse(is1.call(2));

            Fun1<Integer, Boolean> in123 = In(1, 2, 3);
            assertTrue(in123.call(1));
            assertFalse(in123.call(0));

            Fun1<Object, Boolean> notNull = Not(Null());
            assertTrue(notNull.call(0));
            assertFalse(notNull.call(null));

            Fun1<Object, Boolean> isNull = Null();
            assertFalse(isNull.call(0));
            assertTrue(isNull.call(null));
        });
    }

    static void partial_fun() {
        test("PartialFun 重复计算", () -> {
            Fun1<Integer, Boolean> isEven = n -> n % 2 == 0;
            PartialFun<Integer, String> oddlyEnough = Case(i -> !isEven.call(i), i1 -> {
                // println(i1 + " is odd");
                return i1 + " is odd";
            });
            PartialFun<Integer, Integer> half = Case(isEven, x -> {
                // println(x + " / 2");
                return x / 2;
            });
            PartialFun<Integer, String> oddByHalf = half.andThen(oddlyEnough);

            // 模拟 if (pf.isDefinedAt) { pf.call() }, 导致 复计算
            // isDefinedAt 1 false
            // 2 / 2
            // isDefinedAt 2 true call again 2 / 2
            // 1 is odd
            // isDefinedAt 3 false
            // 4 / 2
            // isDefinedAt 4 false
            // isDefinedAt 5 false
            // 6 / 2
            // isDefinedAt 6 true call again 6 / 2
            // 3 is odd
            // isDefinedAt 7 false
            // 8 / 2
            // isDefinedAt 8 false
            // isDefinedAt 9 false
            // [1 is odd, 3 is odd]

            assertEquals(range(1, 10).filter(a -> {
                boolean definedAt = oddByHalf.isDefinedAt(a);
                if (definedAt) {
                    // print("isDefinedAt " + a + " " + definedAt + " call again ");
                } else {
                    // println("isDefinedAt " + a + " " + definedAt);
                }
                return definedAt;
            }).map(oddByHalf), Iter("1 is odd", "3 is odd"));

            // 比 filter+map 快
            // 2 / 2
            // 1 is odd
            // 4 / 2
            // 6 / 2
            // 3 is odd
            // 8 / 2
            // [1 is odd, 3 is odd]
            assertEquals(range(1, 10).collect(oddByHalf), Iter("1 is odd", "3 is odd"));
        });

        test("PartialFun", () -> {
            Fun1<Integer, Boolean> isEven = n -> n % 2 == 0;
            PartialFun<Integer, String> eveningNews = Case(isEven, i -> i + " is even");
            // filter + map
            Iter<String> evenNumbers = range(1, 10).collect(eveningNews);
            assertEquals(evenNumbers, Iter("2 is even", "4 is even", "6 is even", "8 is even"));

            // === === === === === === === === === === === === === === === === === ===

            PartialFun<Integer, String> oddlyEnough = Case(i -> !isEven.call(i), i1 -> i1 + " is odd");
            // pf 可以链接到一起
            assertEquals(range(1, 10).map(eveningNews.orElse(oddlyEnough)),
                    Iter("1 is odd", "2 is even", "3 is odd", "4 is even", "5 is odd", "6 is even", "7 is odd", "8 is even", "9 is odd"));

            // 等同于
            // [1 is odd, 2 is even, 3 is odd, 4 is even, 5 is odd, 6 is even, 7 is odd, 8 is even, 9 is odd]
            assertEquals(range(1, 10).map(i1 -> eveningNews.callOrElse(i1, oddlyEnough)),
                    Iter("1 is odd", "2 is even", "3 is odd", "4 is even", "5 is odd", "6 is even", "7 is odd", "8 is even", "9 is odd"));

            // === === === === === === === === === === === === === === === === === ===

            PartialFun<Integer, Integer> half = Case(isEven, x -> x / 2);
            // 组合计算是昂贵的
            PartialFun<Integer, String> oddByHalf = half.andThen(oddlyEnough);
            // 1-10 =isEven=> 2 4 6 8 =x/2=> 1 2 3 4 =odd=> 1 3
            assertEquals(range(1, 10).collect(oddByHalf), Iter("1 is odd", "3 is odd"));

            // 1-10 =isEven=> 2 4 6 8 =x/2=> 1 2 3 4 =odd=> 1 3
            // 1 3 map 之前是 1-10 里头 的 2 6
            Iter<Integer> oddBalls1 = range(1, 10).filter(oddByHalf::isDefinedAt);
            assertEquals(oddBalls1, Iter(2, 6));

            // 会重复计算
            Iter<String> oddBalls2 = range(1, 10).filter(oddByHalf::isDefinedAt).map(oddByHalf);
            assertEquals(oddBalls2, Iter("1 is odd", "3 is odd"));

            // 比 filter+map 快
            Iter<String> oddBalls3 = range(1, 10).collect(oddByHalf);
            assertEquals(oddBalls3, Iter("1 is odd", "3 is odd"));

            // 提供默认值
            Iter<String> oddsAndEnds = range(1, 10).map(n -> oddByHalf.callOrElse(n, i -> "[" + i + "]"));
            assertEquals(oddsAndEnds, Iter("[1]", "1 is odd", "[3]", "[4]", "[5]", "3 is odd", "[7]", "[8]", "[9]"));
        });

        test("PartialFun", () -> {
            // 所有需要 Fun1 都可以用 Case 创建的 PartialFun 替代
            Fun1<Fun1<String, Integer>, Integer> f = a -> a.call("Hello");
            Fun1<Fun1<String, Integer>, Integer> f1 = a -> a.call("World");

            Case<String, Integer> pf = Cases(
                    Case(Is("Hello"), 42),
                    Case(__, 0)
            );
            assertEquals(f.call(pf), 42);
            assertEquals(f1.call(pf), 0);

            assertEquals(f.call(Case(__, 100)), 100);
        });
    }

    static void pattern_matching() {
        test("pattern matching", () -> {
            {
                Fun1<Integer, String> i2s = i -> Match(i).of(
                        Case(Is(1), a -> "one"),
                        Case(Is(2), a -> "two"),
                        Case(__, a -> "?")
                );
                assertEquals(i2s.call(1), "one");
                assertEquals(i2s.call(2), "two");
                assertEquals(i2s.call(3), "?");
            }
            {
                Fun1<Integer, Option<String>> i2s = i -> Match(i).option(
                        Case(Is(0), a -> "one")
                );
                assertTrue(i2s.call(0).isDefined());
                assertFalse(i2s.call(1).isDefined());
            }
            {
                Fun1<String, String> argsParser = args -> Match(args).of(
                        Case(In("-h", "--help"), a -> "help"),
                        Case(__, a -> "?")
                );
                assertEquals(argsParser.call("-h"), "help");
                assertEquals(argsParser.call("--help"), "help");
                assertEquals(argsParser.call("--version"), "?");
            }

            {
                // 副作用
                Fun1<String, Void> argsParser = args -> Match(args).of(
                        Case(In("-h", "--help"), () -> {
                            println("help");
                            return null;
                        }),
                        Case(In("-v", "--version"), () -> {
                            println("version");
                            return null;
                        }),
                        Case(__, () -> Throw(new IllegalArgumentException()))
                );
                argsParser.call("-h");
                argsParser.call("--help");
                argsParser.call("--version");
                assertThrows(() -> argsParser.call("-x"), IllegalArgumentException.class);
            }

            {
                Fun1<Number, Number> plusOne = o -> Match(o).of(
                        Case(Instance(Integer.class), i -> i + 1),
                        Case(Instance(Double.class), d -> d + 1),
                        Case(__, () -> Throw(new NumberFormatException()))
                );
                assertEquals(plusOne.call(42), 43);
                assertEquals(plusOne.call(3.14).doubleValue(), 4.14);
                assertThrows(() -> plusOne.call(1L), NumberFormatException.class);
            }

            {
                Fun1<Number, Number> plusOne = o -> Match(o).of(
                        Case(All(Instance(Integer.class), i -> i > 100), i -> i + 1),
                        // 这里这里故意把 instance 放后面了
                        Case(Any(d -> d < 100, Instance(Double.class)), d -> d + 1),
                        Case(__, () -> Throw(new NumberFormatException()))
                );
                assertThrows(() -> plusOne.call(3.14f), NumberFormatException.class);
            }
        });
    }

    static double stdDev1(List<Double> a) {
        double mean = fold(a, 0d, Double::sum) / a.size();
        Iter<Double> squareErrors = map(a, i -> i - mean).map(i -> i * i);
        return Math.sqrt(squareErrors.fold(0d, Double::sum) / a.size());
    }
    static double stdDev2(List<Double> a) {
        double mean = sumDouble(a) / a.size();
        Iter<Double> squareErrors = map(a, i -> i - mean).map(i -> i * i);
        return Math.sqrt(sumDouble(squareErrors) / a.size());
    }
    static boolean isValidSudoku(List<List<Integer>> grid) {
        return !range(0, 9).exists(i -> {
            Iterable<Integer> row = range(0, 9).map(j -> grid.get(i).get(j)).toIterable();
            Iterable<Integer> col = range(0, 9).map(j -> grid.get(j).get(i)).toIterable();
            Iterable<Integer> square = range(0, 9).map(j -> grid.get((i % 3) * 3 + j % 3).get((i / 3) * 3 + j / 3)).toIterable();
            return distinct(row).size() != size(row) ||
                    distinct(col).size() != size(col) ||
                    distinct(square).size() != size(square);
        });
    }
    static void combining() {
        assertEquals(
                stdDev1(List(1d, 2d, 3d, 4d, 5d)),
                1.4142135623730951
        );
        assertEquals(
                stdDev1(List(3d, 3d, 3d)),
                0d
        );
        assertEquals(
                stdDev2(List(1d, 2d, 3d, 4d, 5d)),
                1.4142135623730951
        );
        assertEquals(
                stdDev2(List(3d, 3d, 3d)),
                0d
        );

        assertTrue(isValidSudoku(List(
                List(5, 3, 4,   6, 7, 8,   9, 1, 2),
                List(6, 7, 2,   1, 9, 5,   3, 4, 8),
                List(1, 9, 8,   3, 4, 2,   5, 6, 7),

                List(8, 5, 9,   7, 6, 1,   4, 2, 3),
                List(4, 2, 6,   8, 5, 3,   7, 9, 1),
                List(7, 1, 3,   9, 2, 4,   8, 5, 6),

                List(9, 6, 1,   5, 3, 7,   2, 8, 4),
                List(2, 8, 7,   4, 1, 9,   6, 3, 5),
                List(3, 4, 5,   2, 8, 6,   1, 7, 9)
        )));
        assertFalse(isValidSudoku(List(
                List(5, 3, 4,   6, 7, 8,   9, 1, 2),
                List(6, 7, 2,   1, 9, 5,   3, 4, 8),
                List(1, 9, 8,   3, 4, 2,   5, 6, 7),

                List(8, 5, 9,   7, 6, 1,   4, 2, 3),
                List(4, 2, 6,   8, 5, 3,   7, 9, 1),
                List(7, 1, 3,   9, 2, 4,   8, 5, 6),

                List(9, 6, 1,   5, 3, 7,   2, 8, 4),
                List(2, 8, 7,   4, 1, 9,   6, 3, 5),
                List(3, 4, 5,   2, 8, 6,   1, 7, 8)
        )));
    }
}
```

🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶

### TODO
- 补充示例代码
- knownSize
- 改成基于 Iterable? 区分 OnceIter?
- fun0~4 添加 compose andThen 等默认方法? 或者静态方法?
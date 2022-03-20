package xiao;

import lombok.Value;
import lombok.val;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static xiao.Helper.*;
import static xiao.Data.*;
import static xiao.Funs.*;
import static xiao.IterFuns.*;
import static xiao.Iter.*;
import static xiao.PatternMatching.*;
import static xiao.Predicates.*;
import static xiao.Funs.Sneaky.sneak;
import static xiao.Test.*;

/**
 * @author chuxiaofeng
 */
public interface Tests {

    static void main(String[] ignored) {

        factory_methods();
        transforms();
        queries();
        aggregations();
        combining();


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

        test("collectFirst", () -> {
            Case pf = Case(Instance(Integer.class), i -> i * 10);
            Option a = Iter("a", 1, 5L).collectFirst(pf);
            assertTrue(a.isDefined());
            assertEquals(a.get(), 10);
        });

        test("groupBy", () -> {
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

        test("partition", () -> {
            Pair<Iter<Integer>, Iter<Integer>> p = range(0, 5).partition(it -> it >= 2);
            assertEquals(p._1, Iter(2, 3, 4));
            assertEquals(p._2, Iter(0, 1));

            p = Iter(0, 4, 1, 3, 2).partition(it -> it >= 2);
            assertEquals(p._1, Iter(4, 3, 2));
            assertEquals(p._2, Iter(0, 1));
        });

        test("partitionMap", () -> {
            Pair<Iter<Integer>, Iter<String>> p = Iter(1, "one", 2, "two", 3, "three")
                    .partitionMap(Cases(
                            Case(Instance(Integer.class), Data::Left),
                            Case(Instance(String.class), Data::Right)
                    ));
            assertEquals(p._1, Iter(1, 2, 3));
            assertEquals(p._2, Iter("one", "two", "three"));
        });

        test("zip", () -> {
            assertEquals(range(0, 5).zip(range(10, 14)), Iter(Tuple(0, 10), Tuple(1, 11), Tuple(2, 12), Tuple(3, 13)));
        });

        test("zipAll", () -> {
            assertEquals(range(0, 5).zipAll(range(10, 14), 0, 42),
                    Iter(Tuple(0, 10), Tuple(1, 11), Tuple(2, 12), Tuple(3, 13), Tuple(4, 42)));
            assertEquals(range(10, 14).zipAll(range(0, 5), 0, 42),
                    Iter(Tuple(10, 0), Tuple(11, 1), Tuple(12, 2), Tuple(13, 3), Tuple(0, 4)));
        });

        test("zipWithIndex", () -> {
            assertEquals(range(10, 15).zipWithIndex(),
                    Iter(Tuple(10, 0), Tuple(11, 1), Tuple(12, 2), Tuple(13, 3), Tuple(14, 4)));
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

        test("tapEach", () -> {
            range(0, 5).tapEach(System.out::print).toList();
            println();
        });

        test("update", () -> {
            assertEquals(range(0, 5).update(0, 42), Iter(42, 1, 2, 3, 4));
            assertEquals(range(0, 5).update(4, 42), Iter(0, 1, 2, 3, 42));
        });

        test("patch", () -> {
            assertEquals(range(1, 7).patch(2, Iter(), 2), Iter(1, 2, 5, 6));
            assertEquals(range(1, 7).patch(2, Iter(1), 2), Iter(1, 2, 1, 5, 6));
            assertEquals(range(1, 7).patch(2, Iter(1, 2, 3), 2), Iter(1, 2, 1, 2, 3, 5, 6));
            assertEquals(range(1, 7).patch(0, Iter(42), 2), Iter(42, 3, 4, 5, 6));
            assertEquals(range(1, 7).patch(-1, Iter(42), 2), Iter(42, 3, 4, 5, 6));
            assertEquals(range(1, 7).patch(100, Iter(42), 2), Iter(1, 2, 3, 4, 5, 6, 42));
        });

        test("padTo", () -> {
            assertEquals(Iter().padTo(2, 42), Iter(42, 42));
            assertEquals(Iter(1, 2).padTo(2, 42), Iter(1, 2));
        });

        test("takeRight", () -> {
            assertEquals(Iter().takeRight(2), Iter());
            assertEquals(Iter(1).takeRight(2), Iter(1));
            assertEquals(Iter(1, 2).takeRight(2), Iter(1, 2));
            assertEquals(Iter(1, 2, 3).takeRight(2), Iter(2, 3));
            assertEquals(Iter(1, 2, 3).takeRight(0), Iter());
        });

        test("dropRight", () -> {
            assertEquals(Iter().dropRight(2), Iter());
            assertEquals(Iter(1).dropRight(2), Iter());
            assertEquals(range(1, 4).dropRight(0), Iter(1, 2, 3));
            assertEquals(range(1, 4).dropRight(2), Iter(1));
        });

        test("tail", () -> {
            assertEquals(range(0, 10).slice(0, -1).tail(), range(1, 10));
        });

        test("init", () -> {
            assertEquals(range(0, 10).slice(0, -1).init(), range(0, 9));
        });

        test("tails", () -> {
            assertEquals(
                    Iter(1, 2, 3).tails().map(Iter::toList),
                    Iter(
                            List(1, 2, 3), List(2, 3), List(3), List()
                    )
            );
        });

        test("inits", () -> {
            assertEquals(
                    Iter(1, 2, 3).inits().map(Iter::toList),
                    Iter(
                            List(1, 2, 3), List(1, 2), List(1), List()
                    )
            );
        });

        test("splitAt", () -> {
            val it = range(0, 10).splitAt(5);
            assertEquals(it._1, range(0, 5));
            assertEquals(it._2, range(5, 10));
        });

        test("scan", () -> {
            assertEquals(Iter(1, 2, 3, 4).scanLeft(0, (acc, i) -> acc + i), Iter(0, 1, 3, 6, 10));
            assertEquals(Iter(1, 2, 3, 4).scanRight(0, (i, acc) -> acc + i), Iter(0, 4, 7, 9, 10));
        });

        test("fill", () -> {
            assertEquals(Iter.fill(5, () -> 42), Iter(42, 42, 42, 42, 42));
            assertEquals(Iter.fill(5, 3, () -> "A").map(Iter::toList), Iter(List("A", "A", "A"), List("A", "A", "A"), List("A", "A", "A"), List("A", "A", "A"), List("A", "A", "A")));
            assertEquals(Iter.fill(5, 3, 2, () -> "A").map(it -> it.map(Iter::toList).toList()), Iter(List(List("A", "A"), List("A", "A"), List("A", "A")), List(List("A", "A"), List("A", "A"), List("A", "A")), List(List("A", "A"), List("A", "A"), List("A", "A")), List(List("A", "A"), List("A", "A"), List("A", "A")), List(List("A", "A"), List("A", "A"), List("A", "A"))));
        });

        test("tabulate", () -> {
            assertEquals(tabulate(5, i -> i), Iter(0, 1, 2, 3, 4));
            assertEquals(tabulate(5, 3, Data::Pair).map(Iter::toList), Iter(List(Tuple(0, 0), Tuple(0, 1), Tuple(0, 2)), List(Tuple(1, 0), Tuple(1, 1), Tuple(1, 2)), List(Tuple(2, 0), Tuple(2, 1), Tuple(2, 2)), List(Tuple(3, 0), Tuple(3, 1), Tuple(3, 2)), List(Tuple(4, 0), Tuple(4, 1), Tuple(4, 2))));
            assertEquals(tabulate(5, 3, 2, Data::Tuple).map(it1 -> it1.map(Iter::toList).toList()), Iter(List(List(Tuple(0, 0, 0), Tuple(0, 0, 1)), List(Tuple(0, 1, 0), Tuple(0, 1, 1)), List(Tuple(0, 2, 0), Tuple(0, 2, 1))),
                    List(List(Tuple(1, 0, 0), Tuple(1, 0, 1)), List(Tuple(1, 1, 0), Tuple(1, 1, 1)), List(Tuple(1, 2, 0), Tuple(1, 2, 1))),
                    List(List(Tuple(2, 0, 0), Tuple(2, 0, 1)), List(Tuple(2, 1, 0), Tuple(2, 1, 1)), List(Tuple(2, 2, 0), Tuple(2, 2, 1))),
                    List(List(Tuple(3, 0, 0), Tuple(3, 0, 1)), List(Tuple(3, 1, 0), Tuple(3, 1, 1)), List(Tuple(3, 2, 0), Tuple(3, 2, 1))),
                    List(List(Tuple(4, 0, 0), Tuple(4, 0, 1)), List(Tuple(4, 1, 0), Tuple(4, 1, 1)), List(Tuple(4, 2, 0), Tuple(4, 2, 1)))));
        });

        test("from range", () -> {
            assertEquals(range(Integer.MAX_VALUE - 3, Integer.MAX_VALUE, 2), Iter(2147483644, 2147483646));
            assertEquals(range(1, 5, 2), Iter(1, 3));
            assertEquals(range(1, -5, -2), Iter(1, -1, -3));
            assertEquals(from(1, 2).take(3), Iter(1, 3, 5));
            assertEquals(from(1, -2).take(3), Iter(1, -1, -3));
        });

        test("sizeCompare", () -> {
            assertTrue(range(1, 4).sizeCompare(1) > 0);
            assertTrue(range(1, 4).sizeCompare(3) == 0);
            assertTrue(range(1, 4).sizeCompare(4) < 0);

            assertTrue(range(1, 4).sizeCompare(Iter()) > 0);
            assertTrue(range(1, 4).sizeCompare(Iter(2, 3, 4)) == 0);
            assertTrue(range(1, 4).sizeCompare(range(1, 5)) < 0);
        });

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

        test("continually", () -> {
            try (val in = new ByteArrayInputStream("Hello".getBytes(StandardCharsets.UTF_8))) {
                try (val buf = new BufferedInputStream(in)) {
                    assertEquals(
                            continually(Sneaky.sneak(() -> buf.read())).takeWhile(i -> i != -1),
                            Iter('H', 'e', 'l', 'l', 'o').map(it -> ((int) it))
                    );
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

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


        test("slice", () -> {
            assertEquals(Iter().slice(0, 1), Iter());
            assertEquals(Iter(1, 2, 3, 4, 5).slice(3, 2), Iter());
            assertEquals(Iter(1, 2, 3, 4, 5).slice(0, -1), Iter(1, 2, 3, 4, 5));
            assertEquals(Iter(1, 2, 3, 4, 5).slice(1, -1), Iter(2, 3, 4, 5));
            assertEquals(Iter(1, 2, 3, 4, 5).slice(4, 5), Iter(5));
        });

        test("iterate", () -> {
            assertEquals(iterate(0, 5, i -> i + 1), Iter(0, 1, 2, 3, 4));
        });

        test("unfold", () -> {
            assertEquals(unfold("!", s -> Some(Pair(s, s + "!"))).take(5), Iter("!", "!!", "!!!", "!!!!", "!!!!!"));
            assertEquals(unfold("!", s -> s.length() > 5 ? None() : Some(Pair(s, s + "!"))), Iter("!", "!!", "!!!", "!!!!", "!!!!!"));
        });

        test("takeWhile", () -> {
            assertEquals(Iter(1, 2 ,3, 4, 5).takeWhile(a -> a < 1), Iter());
            assertEquals(Iter(1, 2 ,3, 4, 5).takeWhile(a -> a < 5), Iter(1, 2, 3, 4));
        });

        test("dropWhile", () -> {
            assertEquals(Iter().takeWhile(a -> false), Iter());
            assertEquals(Iter().takeWhile(a -> true), Iter());
            assertEquals(Iter(1, 2 ,3, 4, 5).dropWhile(a -> a < 1), Iter(1, 2, 3, 4, 5));
            assertEquals(Iter(1, 2 ,3, 4, 5).dropWhile(a -> a < 4), Iter(4, 5));
        });

        test("min max", () -> {
            {
                Fun0<Iter<Integer>> i = () -> Iter(3, 5, 2, 4, 1);
                assertEquals(min(i.call()), 1);
                assertEquals(max(i.call()), 5);
                assertEquals(minBy(i.call(), a -> 10 - a), 5);
                assertEquals(maxBy(i.call(), a -> 10 - a), 1);
            }

            {
                @Value class Int { Integer i; }
                Fun0<Iter<Int>> i = () -> Iter(3, 5, 2, 4, 1).map(Int::new);
                Comparator<Int> ord = Comparator.comparingInt(a -> a.i);
                assertEquals(i.call().min(ord).i, 1);
                assertEquals(i.call().max(ord).i, 5);
                assertEquals(i.call().minBy(a -> new Int(10 - a.i), ord).i, 5);
                assertEquals(i.call().maxBy(a -> new Int(10 - a.i), ord).i, 1);
            }
            {
                @Value class Int { Integer i; }
                Fun0<Iter<Int>> i = () -> Iter(3, 5, 2, 4, 1).map(Int::new);
                Comparator<Int> ord = Comparator.comparingInt(a -> a.i);
                assertEquals(min(i.call(), ord).i, 1);
                assertEquals(max(i.call(), ord).i, 5);
                assertEquals(minBy(i.call(), a -> new Int(10 - a.i), ord).i, 5);
                assertEquals(maxBy(i.call(), a -> new Int(10 - a.i), ord).i, 1);
            }
        });

        test("flatmap", () -> {
            assertEquals(Iter("HELLO", "WORLD", "!").flatMap(a -> Iter(a.split(""))), Iter("H", "E", "L", "L", "O", "W", "O", "R", "L", "D", "!"));
            assertEquals(Data.<String>Iter().flatMap(a -> Iter(a.split(""))), Iter());

            assertEquals(Iter("HELLO", "WORLD", "!").flatMap1(a -> Iter(a.split(""))), Iter("H", "E", "L", "L", "O", "W", "O", "R", "L", "D", "!"));
            assertEquals(Data.<String>Iter().flatMap1(a -> Iter(a.split(""))), Iter());

            assertEquals(Iter("HELLO", "WORLD", "!").flatMap2(a -> Iter(a.split(""))), Iter("H", "E", "L", "L", "O", "W", "O", "R", "L", "D", "!"));
            assertEquals(Data.<String>Iter().flatMap2(a -> Iter(a.split(""))), Iter());
        });


        test("filter", () -> {
            assertEquals(range(0, 10).filter(it -> false), Iter());
            assertEquals(range(0, 10).filter(it -> it % 2 == 0), Iter(0, 2, 4, 6, 8));

            assertEquals(range(0, 10).filter1(it -> false), Iter());
            assertEquals(range(0, 10).filter1(it -> it % 2 == 0), Iter(0, 2, 4, 6, 8));

            assertEquals(range(0, 10).filter2(it -> false), Iter());
            assertEquals(range(0, 10).filter2(it -> it % 2 == 0), Iter(0, 2, 4, 6, 8));
        });

        test("distinctBy", () -> {
            assertEquals(Iter().distinctBy(a -> a), Iter());
            assertEquals(Iter(1).distinctBy(a -> a), Iter(1));
            assertEquals(Iter(1,2,1).distinctBy(a -> a), Iter(1, 2));
            assertEquals(Iter(1,2,1,2).distinctBy(a -> a), Iter(1, 2));

            assertEquals(Iter().distinctBy1(a -> a), Iter());
            assertEquals(Iter(1).distinctBy1(a -> a), Iter(1));
            assertEquals(Iter(1,2,1).distinctBy1(a -> a), Iter(1, 2));
            assertEquals(Iter(1,2,1,2).distinctBy1(a -> a), Iter(1, 2));
        });

        test("removeAll", () -> {
            assertEquals(Iter(1, 2, 3, 4, 5).removeAll(Iter(1,3,5)), Iter(2, 4));
        });

        test("reverse", () -> {
            assertEquals(Iter(1, 2, 3, 4, 5).reverse(), Iter(5, 4, 3, 2, 1));
        });

        test("fold", () -> {
            assertEquals(Iter("abcde".split("")).foldLeft("", (r, s) -> r + s), "abcde");
            assertEquals(Iter("abcde".split("")).foldRight("", (s, r) -> r + s), "edcba");
            assertEquals(Iter("abcde".split("")).foldRight1("", (s, r) -> r + s), "edcba");
        });

        test("index", () -> {
            assertEquals(range(0, 10).indexOf(5), 5);
        });
    }

    static void factory_methods() {
        assertEquals(
                fill(5, () -> "hello"),
                Iter("hello", "hello", "hello", "hello", "hello")
        );
        assertEquals(
                tabulate(5, i -> "hello " + i),
                Iter("hello 0", "hello 1", "hello 2", "hello 3", "hello 4")
        );
    }

    static void transforms() {
        assertEquals(
                range(1, 6).map(i -> i * 2),
                Iter(2, 4, 6, 8, 10)
        );
        assertEquals(
                range(1, 6).filter(i -> i % 2 == 1),
                Iter(1, 3, 5)
        );
        assertEquals(
                range(1, 6).take(2),
                Iter(1, 2)
        );
        assertEquals(
                range(1, 6).drop(2),
                Iter(3, 4, 5)
        );
        assertEquals(
                range(1, 6).slice(1, 4),
                Iter(2, 3, 4)
        );
        assertEquals(
                Iter(1, 2, 3, 4, 5, 4, 3, 2, 1, 2, 3, 4, 5, 6, 7, 8).distinct(),
                range(1, 9)
        );
    }

    static void queries() {
        assertEquals(
                range(1, 8).find(i -> i % 2 == 0 && i > 4),
                Some(6)
        );
        assertEquals(
                range(1, 8).find(i -> i % 2 == 0 && i > 10),
                None()
        );
        assertEquals(
                range(1, 8).exists(i -> i > 1),
                true
        );
        assertEquals(
                range(1, 8).exists(i -> i < 0),
                false
        );
    }

    static void aggregations() {
        assertEquals(
                range(1, 8).mkString(","),
                "1,2,3,4,5,6,7"
        );
        assertEquals(
                range(1, 8).mkString("[", ",", "]"),
                "[1,2,3,4,5,6,7]"
        );
        assertEquals(
                range(1, 8).foldLeft(0, Integer::sum),
                28
        );
        assertEquals(
                range(1, 8).foldLeft(1, (x, y) -> x * y),
                5040
        );
        Map<Integer, List<Integer>> m = range(1, 8).groupBy(i -> i % 2);
        assertEquals(m.get(0), List(2, 4, 6));
        assertEquals(m.get(1), List(1, 3, 5, 7));
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

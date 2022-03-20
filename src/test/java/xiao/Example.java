package xiao;

import lombok.val;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static xiao.Data.*;
import static xiao.Data.Iter;
import static xiao.Data.Pair;
import static xiao.Iter.*;
import static xiao.Iter.range;
import static xiao.IterFuns.map;
import static xiao.Test.assertEquals;
import static xiao.Test.test;

public interface Example {

    static void main(String[] args) {
        System.out.println("Hello World!");
        data();
        iterFactory();
        iterFuns();
    }

    static void data() {
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

        Tuple(1);
        Tuple(1, 2);
        Tuple(1, 2, 3);
        Tuple(1, 2, 3, 4);

        assertEquals(
                Tuple(1, 2),
                Pair(1, 2)
        );

        assertEquals(
                Iter(1, 2, 3, 4),
                range(1, 5)
        );

        assertEquals(
                Iter(1, 2, 3, 4).toList(),
                List(1, 2, 3, 4)
        );

        Set(1, 2, 3, 4);
        SortedSet(1, 2, 3, 4);

        Map<Integer, String> m = Data.Map(Pair(1, "a"), Pair(2, "b"));

        assertEquals(
                map(m.entrySet(), Data::Pair),
                PairIter(m)
        );

        assertEquals(
                Data.Map(Pair(1, "a"), Pair(2, "b")),
                Data.Map(Iter(Pair(1, "a"), Pair(2, "b")))
        );

        assertEquals(
                SortedMap(Pair(1, "a"), Pair(2, "b")),
                SortedMap(Iter(Pair(1, "a"), Pair(2, "b")))
        );
    }

    static void iterFactory() {
        assertEquals(
                empty(),
                Iter()
        );

        assertEquals(
                single(1),
                Iter(1)
        );

        assertEquals(
                range(1, 7),
                concat(
                        Iter(1, 2),
                        Iter(3, 4),
                        Iter(5, 6)
                )
        );

        assertEquals(
                fill(2, () -> 1),
                Iter(1, 1)
        );

        assertEquals(
                tabulate(3, Object::toString),
                range(0, 3).map(Object::toString)
        );


        assertEquals(
                from(1).take(3),
                Iter(1, 2, 3)
        );

        assertEquals(
                from(1, 2).take(3),
                Iter(1, 3, 5)
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

        // iterate : f(init), f(f(init)), f(f(f(init))) ...
        assertEquals(
                iterate(5, i -> i + 5).take(3),
                Iter(5, 10, 15)
        );

        assertEquals(
                iterate(5, 3, i -> i + 5),
                Iter(5, 10, 15)
        );

        assertEquals(
                // 初始状态 i = 1
                unfold(1, i -> {
                    if (i > 3) return None();
                    // 通过 pair 返回值同时更新状态
                    else return Some(Pair(i.toString(), i + 1));
                }),
                range(1, 4).map(Object::toString)
        );

        try (val in = new ByteArrayInputStream("Hello".getBytes(StandardCharsets.UTF_8))) {
            try (val buf = new BufferedInputStream(in)) {
                assertEquals(
                        continually(Funs.Sneaky.sneak(() -> buf.read())).takeWhile(i -> i != -1),
                        Iter('H', 'e', 'l', 'l', 'o').map(it -> ((int) it))
                );
            }
        } catch (IOException ignored) {}
    }

    static void iterFuns() {
        assertEquals(
                range(1, 5).init(),
                range(1, 4)
        );
        assertEquals(
                range(1, 5).tail(),
                range(2, 5)
        );

        assertEquals(
                Iter(1, 2, 3).inits().map(Iter::toList),
                Iter(
                        List(1, 2, 3), List(1, 2), List(1), List()
                )
        );
        assertEquals(
                Iter(1, 2, 3).tails().map(Iter::toList),
                Iter(
                        List(1, 2, 3), List(2, 3), List(3), List()
                )
        );

        assertEquals(
                range(0, 10).reverse(),
                range(9, -1, -1)
        );

        assertEquals(
                concat(
                        range(0, 5),
                        range(0, 5)
                ).distinct(),
                range(0, 5)
        );

        assertEquals(
                range(0, 10).distinctBy(it -> it % 2),
                Iter(0, 1)
        );

        // todo 从 find 开始继续写 example
    }

    static void partialFun() { }

    static void patternMatching() { }
}

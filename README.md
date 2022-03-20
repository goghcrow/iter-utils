### 介绍
- Iter 一次性的迭代器
    - 各种构造 Iter 的工厂方法, range,from,iterate,tabulate...
    - 给迭代器加了一坨方法, 全部转发 IterFuns, 用来写链式
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
      - 加入了签名抛出受检异常的 CFun0~3 CAct0~3, 用来在 lambda 中使用抛受检异常的方法,
      - 并在 Sneaky 中 提供 sneak 方法将签名抛出受检异常的函数接口转换成对应正常的函数接口
      - 并提供了 java.util.function.* 的版本 JSneaky
    - narrow, 用来绕过 java 类型系统的限制
- Predicates 常用谓词函数组合
- Data 常用数据结构 和 工厂方法
    - 工厂方法：Left/Right/None/Some/Tuple1~4/Iter/List/Set/SortedSet/Map/SortedMap
    - 数据结构
        - Ref: 就是 Ref, 最常见用法, 绕过 java lambda upValue 的 final 语义, 或者延迟构造递归结构
        - Lazy: 就是 Lazy
        - Monuple/Pair/Triple/Quadruple: 就是 Tuple1~4 之前只有 Pair, 懒得改名了
        - Option, Either: 最常用的两个 SumType, 用 CE 做的, 处理了exhaustiveness checking, 保证类型安全
    - 注意: 不考虑性能, immutable 粗暴的 unmodifiableSet 或者复制
- PartialFun 偏函数
    - 抄袭 scala 的偏函数, 处理了重复计算的问题
    - 所有需要 Fun1 的场景都可以使用 Case 或者 Cases 构造偏函数, 多个参数可以用 Tuple 桥接
- PatternMatching 模式匹配
    - 参考了 vavr, 用抄袭 scala 的偏函数做的, 没 vavr case0-n pattern0-n $ 那么零碎的东西
- Ord
- Contract 就是 Contract
- Helper

🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶
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

🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶🥶
### TODO
- 写一些示例代码
- knownSize
- 改成基于 Iterable? 区分 OnceIter?
- fun0~4 添加 compose andThen 等默认方法? 或者静态方法?
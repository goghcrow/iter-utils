package xiao;

import java.sql.Time;
import java.util.function.Function;

@SuppressWarnings("unused")
public interface SumType {

/*
sum type \ product type 都是 algebra data type (ADT) , 是数学的集合概念
可以参考理解 https://fsharpforfunandprofit.com/posts/type-size-and-design/
    ```
    https://en.wikipedia.org/wiki/Algebraic_data_type
    Two common classes of algebraic types are product types (i.e., tuples and records)
    and sum types (i.e., tagged or disjoint unions, coproduct types or variant types).

    The values of a product type typically contain several values, called fields.
    All values of that type have the same combination of field types.
    The set of all possible values of a product type is the set-theoretic product,
    i.e., the Cartesian product, of the sets of all possible values of its field types.

    The values of a sum type are typically grouped into several classes, called variants.
    A value of a variant type is usually created with a quasi-functional entity called a constructor.
    Each variant has its own constructor, which takes a specified number of arguments with specified types.
    The set of all possible values of a sum type is the set-theoretic sum,
    i.e., the disjoint union(不交并), of the sets of all possible values of its variants.
    Enumerated types are a special case of sum types in which the constructors take no arguments,
    as exactly one value is defined for each constructor.

    Values of algebraic types are analyzed with pattern matching,
    which identifies a value by its constructor or field names and extracts the data it contains.
    ```
----------------------------------------------------------------------------
record (或者叫 struct\class) 是 product type, 有名字的 product type, 容易理解
tuple 是没有名字的 product type
    怎么区分 身高x体重的(int x int) 代表的学生 和 身高x体重(int x int) 代表的教授
    but 没有名字会有问题, 如何表示用 union type 表示 学生 或 教授?
----------------------------------------------------------------------------
sum type 需要注意的点, variant 的构造函数签名可以不同, 枚举是零参 ctor 的 sum type
java 的枚举零参 ctor, 算 sum type, 如果定义参数应该不算, 且 java 没有 没有 exhaustiveness checking
sum type 是一种特殊的 union type
通常说的 union type 指的是 untagged union
    ```
    https://en.wikipedia.org/wiki/Union_type
    a union type definition will specify which of a number of permitted primitive types may be stored in its instances,
    e.g., "float or long integer". In contrast with a record (or structure),
    which could be defined to contain a float and an integer; in a union, there is only one value at any given time.
    ```
    ```
    https://en.wikipedia.org/wiki/Tagged_union
    In computer science,
    a tagged union, also called a variant, variant record, choice type, discriminated union, disjoint union, sum type or coproduct,
    is a data structure used to hold a value that could take on several different, but fixed, types.
    Only one of the types can be in use at any one time, and a tag field explicitly indicates which one is in use.
    It can be thought of as a type that has several "cases", each of which should be handled correctly when that type is manipulated.

    https://www.typescriptlang.org/docs/handbook/unions-and-intersections.html
    ```
    可以参考下 TS 的 union type 的解释, 更容易理解, 里头强调了几个点
    1. fixed size
    2. a tag field (这也是为啥其中一个别名叫 tagged union)
    3. exhaustiveness checking
如果用 union type , 是没法表达 英里 | 公里 , int | int
之所以 ts 的 case 会选择要用 literal type 来手动加 tag, 因为 ts 是 structural type 而不是 nominative type
如果名义类型的话 record 英里 {val: int} | record 公里 {val: int}, 这里的name 本质也是 tag
    ```
    https://en.wikipedia.org/wiki/Structural_type_system
    A structural type system (or property-based type system) is a major class of type systems
    in which type compatibility and equivalence are determined by the type's actual structure or definition
    and not by other characteristics such as its name or place of declaration.
    Structural systems are used to determine if types are equivalent and whether a type is a subtype of another.
    It contrasts with nominative systems, where comparisons are based on the names of the types or explicit declarations,
    and duck typing, in which only the part of the structure accessed at runtime is checked for compatibility.
    ```
通常, tagged union 要配合 pattern matching 采用使用意义
另外, 王垠说 checked exception 本质是 union type
    ```
    https://www.yinwang.org/blog-cn/2017/05/23/kotlin
    Java 的 CE 其实对应着一种强大的逻辑概念，一种根本性的语言特性，它叫做“union type”。
    这个特性只存在于 Typed Racket 等一两个不怎么流行的语言里。Union type 也存在于 PySonar 类型推导和 Yin 语言里面。
    你可以把 Java 的 CE 看成是对 union type 的一种不完美的，丑陋的实现。虽然实现丑陋，写法麻烦，CE 却仍然有着 union type 的基本功能。
    如果使用得当，union type 不但会让代码的出错处理无懈可击，还可以完美的解决 null 指针等头痛的问题。
    ```
为啥呢? 我觉得 CE 不止是 union type, 可以 hack 成 disjoint union
(严格意义上, 这些ts、java、scala、haskell 等一票语言貌似都没有 ADT, https://www.zhihu.com/question/24460419/answer/759928084)
 */


    // Scala ADT
    /*
    // sealed trait 表明 Command 只能在当前 module 被实现,
    // 或者字面理解, 类继承层次是封闭的, 没指望给人扩展
    sealed trait Command
    // 伴生对象
	object Command {
	    // case class 就是个值对象, 实现了
	    // toString, hashCode, equals, copy, apply(不用 new 构造), unapply(模式匹配解构)

	    // 注意每个 Command 类型的 class 都有自己的数据, move 有坐标, 攻击有对象, 停止有时间
	    // 如果 Command 有数据或者方法, 各个 class 还能共享
		case class Move(dest: Coord) extends Command
		case class Attack(target: Entity) extends Command
		case class Pause(time: Time) extends Command
		case class Stop extends Command
	}
	// 关键 可以有模式匹配可以直接用, 编译器可以保证你 match 的 case 不会漏
	def execute(cmd: Command) = cmd match {
		case Move(dest) => ... dest ...
		case    ...     =>   ...
	}
     */


    static <T extends Throwable> void sneakyThrows(Throwable t) throws T {
        //noinspection unchecked
        throw ((T) t);
    }

    class Coord { }
    class Entity { }


    // 首先没有用枚举, 因为每个Command 的数据都不一样
    // 用 OO 的方式定义的话
    interface OOStyle {
        interface Command {
            void execute();
            final class Move implements Command {
                public final Coord dest;
                public Move(Coord dest) { this.dest = dest; }
                @Override public void execute() { /*dest*/ }
            }
            // 其他
        }
        // 问题, 没有 sealed trait, 做不到 fixed size
    }
    // 可以用 visitor 来实现冗长的实现 封闭的语义
    interface OOADTStyle {
        interface Command {
            void accept(Visitor v);

            interface Visitor {
                void visit(Move c);
                void visit(Attack c);
                void visit(Pause c);
                void visit(Stop c);
            }

            // 会发现 java 的 visitor 模式大量的样板代码
            final class Move implements Command {
                public final Coord dest;
                public Move(Coord dest) { this.dest = dest; }
                @Override public void accept(Visitor v) { v.visit(this); }
            }
            final class Attack implements Command {
                public final Entity target;
                public Attack(Entity target) { this.target = target; }
                @Override public void accept(Visitor v) { v.visit(this); }
            }
            final class Pause implements Command {
                public final Time duration;
                public Pause(Time duration) { this.duration = duration; }
                @Override public void accept(Visitor v) { v.visit(this); }
            }
            final class Stop implements Command {
                @Override public void accept(Visitor v) { v.visit(this); }
            }

            // 使用方法
            // 这里虽然啰嗦, 但是达成了两个成就
            // 1. 模式匹配的语义, 虽然不支持解构赋值, 但是通过参数对象访问属性也还不错
            // 2. 封闭的语义, exhaustiveness 编译期检查, 你少实现一个 visitor 方法 编译不过
            // visitor 是在解决 java 不支持动态多分派,
            // 本质上 visitor 是一种支持递归的模式匹配, 比如用 visitor 写解释器
            static void execute(Command cmd) {
                cmd.accept(new Visitor() {
                    @Override public void visit(Move c) {  /* c.dest */ }
                    @Override public void visit(Attack c) {  /* c.target */ }
                    @Override public void visit(Pause c) { /* c.duration */ }
                    @Override public void visit(Stop c) { /* */ }
                });
            }
        }
    }

    interface TaggedUnionByCE {
        // 继承 Throwable 是为了 Checked 的语义, 这是实现 tagged union 的关键
        abstract class Command extends Throwable {
            // 这里声明成 private, 是为了实现封闭语义的
            // 通过声明构造函数, 让继承的类必须也写构造函数, 且必须调用父类构造函数初始化
            // 因为父类构造函数是私有, 所以通过 private 语义限制了访问作用域外扩展 更多的 command
            // 这个语义跟 scala 很相似
            private Command() {
                // 注意这里抑制了生成异常 stack
                // 可以一定程度解决这种 tricky 的反模式的性能问题
                // 哪怕你不这么写, 同一个代码位置的异常频繁throw, jvm 都会看不下去给你把堆栈干掉, 反正基本每次都一样
                super(null, null, false, false);
            }

            public final static class Move extends Command {
                public final Coord dest;
                public Move(Coord dest) { this.dest = dest; }
            }

            public final static class Attack extends Command {
                public final Entity target;
                public Attack(Entity target) { this.target = target; }
            }

            public final static class Pause extends Command {
                public final Time duration;
                public Pause(Time duration) { this.duration = duration; }
            }

            public final static class Stop extends Command { }

            // 如何使用
            // 比 visitor 简洁多了, 没了乱七八糟的样板代码
            // 虽然 try-catch 语义不是干这个事情的, 会让阅读代码的人一脸懵逼
            // 本质上这里 用 try-catch 来表达 match 语义
            public static void execute(Command cmd) {
                // 这里 CE 也是实现 Exhaustiveness 的关键, 利用了CE 必须都捕获的特性, 类型安全不必 visitor 差
                // 如果省去Command 分支, 或者 没写 Command, 只写了 Pause, 没写 Stop 都会编译不过
                try { throw cmd; }                      // 匹配 Cmd
                catch (Move c)    { /* c.dest */ }      // 匹配使用 Mov Cmd
                catch (Attack c)  { /* c.target */ }    // 匹配使用 Attach Cmd
                catch (Command c) { /**/ }              // 默认分支, 会匹配并处理剩余 commands
            }
        }
    }

    interface TaggedUnionByCEV2_NiceSyntax {
        abstract class Command extends Throwable {
            // 通过定义个只用来抛自己的 match 方法
            // 用来优化语法, 减少一些语义的困扰
            public abstract void match() throws Move, Attack, Pause, Stop;

            private Command() {
                super(null, null, false, false);
            }

            public final static class Move extends Command {
                public final Coord dest;
                public Move(Coord dest) { this.dest = dest; }
                @Override public void match() throws Move { throw this; }
            }

            public final static class Attack extends Command {
                public final Entity target;
                public Attack(Entity target) { this.target = target; }
                @Override public void match() throws Attack { throw this; }
            }

            public final static class Pause extends Command {
                public final Time duration;
                public Pause(Time duration) { this.duration = duration; }
                @Override public void match() throws Pause { throw this; }
            }

            public final static class Stop extends Command {
                @Override public void match() throws Stop { throw this; }
            }

            public static void execute(Command cmd) {
                // 注意这里不是 throw, 语义稍微正常一点
                try { cmd.match(); }                    // 匹配 Cmd
                catch (Move c)    { /* c.dest */ }      // 匹配使用 Mov Cmd
                catch (Attack c)  { /* c.target */ }    // 匹配使用 Attach Cmd
                catch (Command c) { /**/ }              // 默认分支, 会匹配并处理剩余 commands
            }
        }
    }

    interface TaggedUnionByCEV3_SneakyThrow {
        abstract class Command extends Throwable {
            // 这里返回多态为了绕过类型检查, 可以写 return match(), 适配任何返回值, 不会导致 try-catch 之后提示必须有返回值
            public <T> T match() throws Move, Attack, Pause, Stop {
                // 这里 idea 的提示 ❌
                assert this.getClass() != Command.class;
                // 通过 java 类型系统的 bug 绕过 CE, 来再次优化语法, 节省样板代码
                // throw this;
                sneakyThrows(this);
                // 当然这里也可以写成 if () else if () ... 穷举子类抛出的形式
                return null;
            }

            private Command() {
                super(null, null, false, false);
            }

            public final static class Move extends Command {
                public final Coord dest;
                public Move(Coord dest) { this.dest = dest; }
            }

            public final static class Attack extends Command {
                public final Entity target;
                public Attack(Entity target) { this.target = target; }
            }

            public final static class Pause extends Command {
                public final Time duration;
                public Pause(Time duration) { this.duration = duration; }
            }

            public final static class Stop extends Command { }

            public static void execute(Command cmd) {
                // 注意这里不是 throw, 语义稍微正常一点
                try { cmd.match(); }                    // 匹配 Cmd
                catch (Move c)    { /* c.dest */ }      // 匹配使用 Mov Cmd
                catch (Attack c)  { /* c.target */ }    // 匹配使用 Attach Cmd
                catch (Command c) { /**/ }              // 默认分支, 会匹配并处理剩余 commands
            }
        }
    }

    interface TaggedUnionByCEV3_UnionSyntax {
        abstract class Command extends Throwable {
            static <T extends Throwable> void sneakyThrows(Throwable t) throws T {
                //noinspection unchecked
                throw ((T) t);
            }

            public <T> T match() throws Move, Attack, Pause, Stop {
                sneakyThrows(this);
                return null;
            }

            private Command() {
                super(null, null, false, false);
            }

            public final static class Move extends Command {
                public final Coord dest;
                public Move(Coord dest) { this.dest = dest; }
            }

            public final static class Attack extends Command {
                public final Entity target;
                public Attack(Entity target) { this.target = target; }
            }

            public final static class Pause extends Command {
                public final Time duration;
                public Pause(Time duration) { this.duration = duration; }
            }

            public final static class Stop extends Command { }

            public static boolean isAction(Command cmd) {
                //noinspection CatchMayIgnoreException
                try { cmd.match(); }
                // 还会获得额外的 union 语法 特性
                // 这里是 java 语法唯一支持 union 语义的语法
                // 这里让基于 CE 的 match 更像模式匹配了
                catch (Move | Attack c) { return true; }
                catch (Command c) { }
                return false;
            }

            public void execute(Command cmd) {
                try { cmd.match(); }
                catch (Move c)    { /* c.dest */ }
                catch (Attack c)  { /* c.target */ }
                catch (Command c) { /**/ }
            }
        }
    }

    interface TaggedUnionByCEV3_MetaCase {
        abstract class Command extends Throwable {
            static <T extends Throwable> void sneakyThrows(Throwable t) throws T {
                //noinspection unchecked
                throw ((T) t);
            }

            public <T> T match() throws Move, Attack, Pause, Stop {
                sneakyThrows(this);
                return null;
            }

            private Command() {
                super(null, null, false, false);
            }

            // 利用继承层次定义 meta-cases (覆盖不同子类的 case, 起个有用的名字, 一些方法)
            // 可以更方便的进行模式匹配, 参见使用场景
            public static abstract class Action extends Command {
                @Override
                public <T> T match() throws Move, Attack {
                    sneakyThrows(this);
                    return null;
                }
                public final boolean isViolent() {
                    //noinspection CatchMayIgnoreException
                    try { match(); }
                    catch (Attack c) { return true;  }
                    catch (Move c) { }
                    return false;
                }
            }

            public final static class Move extends Action {
                public final Coord dest;
                public Move(Coord dest) { this.dest = dest; }
            }

            public final static class Attack extends Action {
                public final Entity target;
                public Attack(Entity target) { this.target = target; }
            }

            public final static class Pause extends Command {
                public final Time duration;
                public Pause(Time duration) { this.duration = duration; }
            }

            public final static class Stop extends Command { }

            public static boolean isAction(Command cmd) {
                //noinspection CatchMayIgnoreException
                try { cmd.match(); }
                // 还会获得额外的 union 语法 特性
                // 这里是 java 语法唯一支持 union 语义的语法
                // 这里让基于 CE 的 match 更像模式匹配了
                catch (Move | Attack c) { return true; }
                catch (Command c) { }
                return false;
            }

            // 使用场景
            static void warnOnViolentAction(Command cmd) {
                try { cmd.match(); }
                catch (Action c) { if (c.isViolent()) System.out.println("Careful!"); }
                catch (Command c) { System.out.println("We're safe... not even an action!"); }
            }
            // 使用场景
            static void uselessnessCheck(Action act) {
                //noinspection TryWithIdenticalCatches,CatchMayIgnoreException
                try { act.match(); }
                catch (Move move) { }
                catch (Attack attack) { }
                // 编译不过, 这里同样是 exhaustiveness 语义, 捕获多了也会有问题
                // catch (Stop stop) { }
            }

            public void execute(Command cmd) {
                try { cmd.match(); }
                catch (Move c)    { /* c.dest */ }
                catch (Attack c)  { /* c.target */ }
                catch (Command c) { /**/ }
            }
        }
    }

    // 当然这个方法用限制
    // 1. 比如 catch (Throwable t)
    // 2. 泛型限制
    //      泛型 class 不能继承 Throwable, 所以就不能实现类型安全的 Union<T>
    //      class GenericOptional<T> extends Throwable {}
    // 3. 性能问题, 不能用在热点路径, 虽然可以抑制堆栈, 但是仍旧有回卷之类操作 (没具体测过, 不过感觉应该还可以)

    // 通过加一层 token 绕过泛型的限制
    interface WorkaroundThrowingTokens {
        abstract class Option<T> {
            private Option() { }

            public static class NoneToken extends Throwable { private NoneToken() { super(null, null, false, false); } }
            public static class SomeToken extends Throwable { private SomeToken() { super(null, null, false, false); } }

            // 不直接throw Option<T>, 而是 throw 标记 token
            public <Y> Y match() throws NoneToken, SomeToken
            { if (this instanceof None) throw new NoneToken(); else throw new SomeToken(); }

            public T get(SomeToken t) { return ((Some<T>)this).value; }

            // 这个绕过方法本质上还是 visitor 模式的伪装
            // 动态分派是通过异常处理实现, 而不是通过 invoke-virtual 分派
            // token 的作用是为了静态选择正确的 visitor 方法重载 (这里指 get, 还可以有更多)
            // 跟 visitor 中 accept 方法实现中 visitor 方法的调用如出一辙

            public<U> Option<U> map(Function<T, U> f) {
                //noinspection CatchMayIgnoreException
                try { match(); }
                catch (SomeToken t) { return some(f.apply(get(t))); }
                catch (NoneToken t) { }
                return none();
            }

            public static class Some<T> extends Option<T> {
                public final T value;
                public Some(T value) { this.value = value; }
            }
            public static class None<T> extends Option<T> { }

            // public final static None none = new None();
            public static<T> None<T> none() { return new None<>(); }
            public static<T> Some<T> some(T value) { return new Some<>(value); }

            // 使用场景
            public static void main(String[] args) {
                Option<Integer> opti = some(42);
                foo(opti);
                foo(some(666));
                foo(none());

                foo(opti.map(x -> x+1));
                foo(opti.map(x -> x.toString().length()));
            }

            static<T> void foo(Option<T> opt) {
                try { opt.match(); }
                catch (SomeToken t) { System.out.println(opt.get(t)); }
                catch (NoneToken t) { System.out.println("???"); }
            }
        }
    }


    // 当然, 如果想在 java 使用模式匹配, 要么新版本, switch 语法 (反正 visitor 是现在的模式匹配)
    // https://cr.openjdk.java.net/~briangoetz/amber/pattern-match.html
    // 要么，推荐 lambda 形式
    // https://github.com/klappdev/jpml
}


// https://kerflyn.wordpress.com/2012/05/09/towards-pattern-matching-in-java/
// https://lptk.github.io/programming/2015/03/24/algebraic-data-types-in-java.html
package xiao;

import static xiao.Data.None;
import static xiao.Data.Some;
import static xiao.Helper.println;

public class OptionExample {
    void test() {
        Data.Option<Integer> opti = Some(42);
        foo(opti);
        foo(Some(666));
        foo(None());

        foo(opti.map(x -> x + 1));
        foo(opti.map(x -> x.toString().length()));
    }

    <T> void foo(Data.Option<T> opt) {
        try {
            opt.match();
        } catch (Data.Option.SomeToken t) {
            println(opt.get(t));
        } catch (Data.Option.NoneToken t) {
            println("???");
        }
    }
}

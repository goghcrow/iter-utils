package xiao;

import static xiao.Data.Left;
import static xiao.Data.Right;
import static xiao.Helper.println;

public class EitherExample {
    void test() {
        Data.Either<Integer, String> e1 = Left(42);
        Data.Either<Integer, String> e2 = Right("hello");
        foo(e1);
        foo(e2);
    }

    void foo(Data.Either<Integer, String> et) {
        try {
            et.match();
        } catch (Data.Either.LeftToken t) {
            println(et.get(t).doubleValue());
        } catch (Data.Either.RightToken t) {
            println(et.get(t).toUpperCase());
        }
    }
}

package xiao;

import java.util.Iterator;
import java.util.Objects;

import static xiao.Helper.println;
import static xiao.IterFuns.corresponds;

/**
 * @author chuxiaofeng
 */
public interface Test {
    static void assertTrue(boolean cond) { assertEquals(cond, true); }
    static void assertFalse(boolean cond) { assertEquals(cond, false); }
    static void assertEquals(Iterator<?> actual, Iterator<?> expected) {
        assertTrue(corresponds(actual, expected, Objects::equals));
    }
    static void assertEquals(Integer actual, Integer expected) { assertTrue(Objects.equals(actual, expected)); }
    static void assertEquals(Double actual, Double expected) {
        double epsilon = 0.000001d;
        assertTrue(Math.abs(actual - expected) <= epsilon);
    }
    static void assertEquals(Object actual, Object expected) {
        if (!Objects.equals(actual, expected)) {
            throw new AssertionError("expected " + expected + ", actual " + actual);
        }
    }
    static void assertThrows(Funs.Act0 act, Class<? extends Throwable> typ) {
        try {
            act.call();
            throw new AssertionError("expected throws " + typ.getSimpleName());
        } catch (Throwable t) {
            if (!typ.isInstance(t)) {
                throw new AssertionError("expected throws " + typ.getSimpleName() + ", actual throws " + t.getClass().getSimpleName());
            }
        }
    }
    static void test(String begin, Funs.Act0 test) {
        println("test " + begin);
        test.call();
        println("===========================");
    }
}

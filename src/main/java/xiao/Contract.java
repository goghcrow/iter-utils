package xiao;

/**
 * @author chuxiaofeng
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
public interface Contract {
    static <T> T require(boolean requirement, String msg) { if (!requirement) throw new RuntimeException(msg);                          return null; }
    static <T> T require(boolean requirement)             { if (!requirement) throw new IllegalArgumentException("requirement failed"); return null; }
}

package xiao;

/**
 * @author chuxiaofeng
 */
@SuppressWarnings("unused")
public interface Helper {

    static void  exit()            { System.exit(0); }
    static void  exit(int i)       { System.exit(i);        }

    static void  print(Object a)   { System.out.print(a);   }
    static void  println()         { System.out.println();  }
    static void  println(Object a) { System.out.println(a); }

    static <A> A Throw(RuntimeException e) { throw e; }

    static <A> A TODO() { throw new UnsupportedOperationException(); }

}
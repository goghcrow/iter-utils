package xiao;

import xiao.Data.Option;

import static xiao.Data.None;
import static xiao.Data.Some;

/**
 * @author chuxiaofeng
 */
public interface BufferedIter<A> extends Iter<A> {

    // 可以提前 peek
    A head();

    default Option<A> headOption() {
        if (hasNext()) {
            return Some(head());
        } else {
            return None();
        }
    }

    @Override
    default BufferedIter<A> buffered() {
        return this;
    }

}

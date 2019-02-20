package io.metawiring.metafs;

import java.util.Iterator;
import java.util.function.Function;

public class TransformingIterator<I,O> implements Iterator<O> {

    private final Function<I,O> function;
    private Iterator<I> wrapped;

    public TransformingIterator(Function<I,O> function, Iterator<I> wrapped) {
        this.function = function;
        this.wrapped = wrapped;
    }

    @Override
    public boolean hasNext() {
        return wrapped.hasNext();
    }

    @Override
    public O next() {
        return function.apply(wrapped.next());
    }
}

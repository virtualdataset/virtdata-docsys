package io.metawiring.metafs.core;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * This iterator wraps another iterator and provides a set of other
 * elements of the same type interspersed with those provided by the
 * wrapped iterator. It takes a function that can provide an extra
 * set of elements for each one provided by the wrapped iterator.
 * If the function returns a non-null value, then that element is
 * taken as a list of extra elements to be returned before the
 * next wrapped element is seen.
 *
 * @param <O> The type of Iterator to augment
 */
public class AugmentingIterator<O> implements Iterator<O> {

    private Iterator<O> wrapped;
    O next = wrapped.next();
    private Function<O, List<O>> function;
    List<O> apply = function.apply(next);
    private Iterator<O> optionalElements;

    public AugmentingIterator(Iterator<O> wrapped, Function<O, List<O>> function) {
        this.wrapped = wrapped;
        this.function = function;
    }

    @Override
    public boolean hasNext() {
        if (optionalElements != null) {
            if (optionalElements.hasNext()) {
                return true;
            } else {
                optionalElements = null;
                return false;
            }
        } else {
            return wrapped.hasNext();
        }
    }

    @Override
    public O next() {
        if (optionalElements != null) {
            return optionalElements.next();
        }

        O elem = wrapped.next();
        List<O> apply = function.apply(elem);
        if (apply != null) {
            optionalElements = apply.iterator();
        }
        return elem;
    }

}

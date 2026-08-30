/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openjpa.lib.util.collections;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * Converts an {@link Iterator} into a {@link ResettableListIterator}.
 * For plain <code>Iterator</code>s this is accomplished by caching the returned
 * elements. This class can also be used to simply add
 * {@link ResettableIterator}
 * functionality to a given {@link ListIterator}.
 * <p>
 * The <code>ListIterator</code> interface has additional useful methods
 * for navigation - <code>previous()</code> and the index methods.
 * This class allows a regular <code>Iterator</code> to behave as a
 * <code>ListIterator</code>. It achieves this by building a list internally
 * of as the underlying iterator is traversed.
 * <p>
 * The optional operations of <code>ListIterator</code> are not supported for plain <code>Iterator</code>s.
 * <p>
 * This class implements ResettableListIterator from Commons Collections 3.2.
 *
 * @since 2.1
 */
public class ListIteratorWrapper<E> implements ResettableListIterator<E> {

    /** Message used when set or add are called. */
    private static final String UNSUPPORTED_OPERATION_MESSAGE =
        "ListIteratorWrapper does not support optional operations of ListIterator.";

    /** Message used when removal is not allowed. */
    private static final String CANNOT_REMOVE_MESSAGE =
        "Cannot remove element at index {0}.";

    /** The underlying iterator being decorated. */
    private final Iterator<? extends E> iterator;

    /** The list being used to cache the iterator. */
    private final List<E> list = new ArrayList<>();

    /** The current index of this iterator. */
    private int currentIndex = 0;

    /** The current index of the wrapped iterator. */
    private int wrappedIteratorIndex = 0;

    /**
     * Recall whether the wrapped iterator's cursor is in such a state
     * as to allow remove() to be called.
     */
    private boolean removeState;

    /**
     * Constructs a new <code>ListIteratorWrapper</code> that will wrap
     * the given iterator.
     *
     * @param iterator the iterator to wrap
     * @throws NullPointerException if the iterator is null
     */
    public ListIteratorWrapper(final Iterator<? extends E> iterator) {
        super();
        if (iterator == null) {
            throw new NullPointerException("Iterator must not be null");
        }
        this.iterator = iterator;
    }

    /**
     * Returns the wrapped iterator as a ListIterator when possible.
     *
     * @return the wrapped ListIterator or null if not available
     */
    @SuppressWarnings("unchecked")
    private ListIterator<E> getListIterator() {
        return iterator instanceof ListIterator
            ? (ListIterator<E>) iterator
            : null;
    }

    @Override
    public void add(final E obj) throws UnsupportedOperationException {
        final ListIterator<E> listIterator = getListIterator();
        if (listIterator != null) {
            listIterator.add(obj);
            return;
        }
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public boolean hasNext() {
        if (currentIndex == wrappedIteratorIndex || getListIterator() != null) {
            return iterator.hasNext();
        }
        return true;
    }

    @Override
    public boolean hasPrevious() {
        final ListIterator<E> listIterator = getListIterator();
        if (listIterator != null) {
            return listIterator.hasPrevious();
        }
        return currentIndex > 0;
    }

    @Override
    public E next() throws NoSuchElementException {
        if (getListIterator() != null) {
            return iterator.next();
        }

        if (currentIndex < wrappedIteratorIndex) {
            currentIndex++;
            return list.get(currentIndex - 1);
        }

        final E value = iterator.next();
        list.add(value);
        currentIndex++;
        wrappedIteratorIndex++;
        removeState = true;
        return value;
    }

    @Override
    public int nextIndex() {
        final ListIterator<E> listIterator = getListIterator();
        if (listIterator != null) {
            return listIterator.nextIndex();
        }
        return currentIndex;
    }

    @Override
    public E previous() throws NoSuchElementException {
        final ListIterator<E> listIterator = getListIterator();
        if (listIterator != null) {
            return listIterator.previous();
        }

        if (currentIndex == 0) {
            throw new NoSuchElementException();
        }

        removeState = wrappedIteratorIndex == currentIndex;
        return list.get(--currentIndex);
    }

    @Override
    public int previousIndex() {
        final ListIterator<E> listIterator = getListIterator();
        if (listIterator != null) {
            return listIterator.previousIndex();
        }
        return currentIndex - 1;
    }

    @Override
    public void remove() throws UnsupportedOperationException {
        if (getListIterator() != null) {
            iterator.remove();
            return;
        }

        int removeIndex = currentIndex;
        if (currentIndex == wrappedIteratorIndex) {
            removeIndex--;
        }

        if (!removeState || wrappedIteratorIndex - currentIndex > 1) {
            throw new IllegalStateException(
                MessageFormat.format(CANNOT_REMOVE_MESSAGE, removeIndex));
        }

        iterator.remove();
        list.remove(removeIndex);
        currentIndex = removeIndex;
        wrappedIteratorIndex--;
        removeState = false;
    }

    @Override
    public void set(final E obj) throws UnsupportedOperationException {
        final ListIterator<E> listIterator = getListIterator();
        if (listIterator != null) {
            listIterator.set(obj);
            return;
        }
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public void reset() {
        final ListIterator<E> listIterator = getListIterator();
        if (listIterator != null) {
            while (listIterator.previousIndex() >= 0) {
                listIterator.previous();
            }
            return;
        }

        currentIndex = 0;
    }
}

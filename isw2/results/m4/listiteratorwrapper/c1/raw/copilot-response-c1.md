Ho applicato un refactoring volutamente conservativo, poiché la baseline Sonar della versione C0 non segnala code smell e la priorità è preservare integralmente il comportamento osservabile. La modifica principale consiste nel determinare una sola volta se l’iteratore ricevuto implementa `ListIterator`, eliminando controlli e cast ripetuti senza cambiare la logica di navigazione o aggiornamento dello stato.

```java
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
 * as the underlying iterator is traversed.
 * <p>
 * The optional operations of <code>ListIterator</code> are not supported for
 * plain <code>Iterator</code>s.
 * <p>
 * This class implements ResettableListIterator from Commons Collections 3.2.
 *
 * @since 2.1
 */
public class ListIteratorWrapper<E> implements ResettableListIterator<E> {

    /** Message used when set or add are called. */
    private static final String UNSUPPORTED_OPERATION_MESSAGE =
        "ListIteratorWrapper does not support optional operations of ListIterator.";

    /** Message used when remove cannot be performed. */
    private static final String CANNOT_REMOVE_MESSAGE =
        "Cannot remove element at index {0}.";

    /** The underlying iterator being decorated. */
    private final Iterator<? extends E> iterator;

    /**
     * The underlying iterator as a ListIterator, or {@code null} when the
     * wrapped iterator does not implement ListIterator.
     */
    private final ListIterator<E> listIterator;

    /** The list being used to cache the iterator. */
    private final List<E> list = new ArrayList<>();

    /** The current index of this iterator. */
    private int currentIndex;

    /** The current index of the wrapped iterator. */
    private int wrappedIteratorIndex;

    /**
     * Whether the wrapped iterator's cursor is in a state that allows
     * remove() to be called.
     */
    private boolean removeState;

    // Constructor
    //-------------------------------------------------------------------------

    /**
     * Constructs a new <code>ListIteratorWrapper</code> that will wrap
     * the given iterator.
     *
     * @param iterator the iterator to wrap
     * @throws NullPointerException if the iterator is null
     */
    public ListIteratorWrapper(final Iterator<? extends E> iterator) {
        if (iterator == null) {
            throw new NullPointerException("Iterator must not be null");
        }

        this.iterator = iterator;
        this.listIterator = asListIterator(iterator);
    }

    /**
     * Returns the supplied iterator as a ListIterator when supported.
     *
     * @param iterator the iterator to inspect
     * @return the ListIterator view, or {@code null} if it is not supported
     */
    @SuppressWarnings("unchecked")
    private static <E> ListIterator<E> asListIterator(
            final Iterator<? extends E> iterator) {
        if (iterator instanceof ListIterator) {
            return (ListIterator<E>) iterator;
        }
        return null;
    }

    // ListIterator interface
    //-------------------------------------------------------------------------

    /**
     * Throws {@link UnsupportedOperationException}
     * unless the underlying <code>Iterator</code> is a <code>ListIterator</code>.
     *
     * @param obj the object to add
     * @throws UnsupportedOperationException if the underlying iterator is not
     * of type {@link ListIterator}
     */
    @Override
    public void add(final E obj) throws UnsupportedOperationException {
        if (listIterator == null) {
            throw new UnsupportedOperationException(
                UNSUPPORTED_OPERATION_MESSAGE);
        }

        listIterator.add(obj);
    }

    /**
     * Returns true if there are more elements in the iterator.
     *
     * @return true if there are more elements
     */
    @Override
    public boolean hasNext() {
        return listIterator != null
            || currentIndex == wrappedIteratorIndex
            ? iterator.hasNext()
            : true;
    }

    /**
     * Returns true if there are previous elements in the iterator.
     *
     * @return true if there are previous elements
     */
    @Override
    public boolean hasPrevious() {
        if (listIterator != null) {
            return listIterator.hasPrevious();
        }
        return currentIndex > 0;
    }

    /**
     * Returns the next element from the iterator.
     *
     * @return the next element from the iterator
     * @throws NoSuchElementException if there are no more elements
     */
    @Override
    public E next() throws NoSuchElementException {
        if (listIterator != null) {
            return listIterator.next();
        }

        if (currentIndex < wrappedIteratorIndex) {
            return list.get(currentIndex++);
        }

        final E element = iterator.next();
        list.add(element);
        currentIndex++;
        wrappedIteratorIndex++;
        removeState = true;
        return element;
    }

    /**
     * Returns the index of the next element.
     *
     * @return the index of the next element
     */
    @Override
    public int nextIndex() {
        if (listIterator != null) {
            return listIterator.nextIndex();
        }
        return currentIndex;
    }

    /**
     * Returns the previous element.
     *
     * @return the previous element
     * @throws NoSuchElementException if there are no previous elements
     */
    @Override
    public E previous() throws NoSuchElementException {
        if (listIterator != null) {
            return listIterator.previous();
        }

        if (currentIndex == 0) {
            throw new NoSuchElementException();
        }

        removeState = wrappedIteratorIndex == currentIndex;
        return list.get(--currentIndex);
    }

    /**
     * Returns the index of the previous element.
     *
     * @return the index of the previous element
     */
    @Override
    public int previousIndex() {
        if (listIterator != null) {
            return listIterator.previousIndex();
        }
        return currentIndex - 1;
    }

    /**
     * Throws {@link IllegalStateException} when removal is not valid for the
     * current iterator state.
     *
     * @throws UnsupportedOperationException if removal is not supported by
     * the underlying iterator
     */
    @Override
    public void remove() throws UnsupportedOperationException {
        if (listIterator != null) {
            listIterator.remove();
            return;
        }

        final int removeIndex =
            currentIndex == wrappedIteratorIndex
                ? currentIndex - 1
                : currentIndex;

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

    /**
     * Throws {@link UnsupportedOperationException}
     * unless the underlying <code>Iterator</code> is a <code>ListIterator</code>.
     *
     * @param obj the object to set
     * @throws UnsupportedOperationException if the underlying iterator is not
     * of type {@link ListIterator}
     */
    @Override
    public void set(final E obj) throws UnsupportedOperationException {
        if (listIterator == null) {
            throw new UnsupportedOperationException(
                UNSUPPORTED_OPERATION_MESSAGE);
        }

        listIterator.set(obj);
    }

    // ResettableIterator interface
    //-------------------------------------------------------------------------

    /**
     * Resets this iterator back to the position at which the iterator
     * was created.
     *
     * @since 3.2
     */
    @Override
    public void reset() {
        if (listIterator != null) {
            while (listIterator.previousIndex() >= 0) {
                listIterator.previous();
            }
            return;
        }

        currentIndex = 0;
    }
}
```

## Principali refactoring effettuati

- Introdotto il campo privato e immutabile `listIterator`, inizializzato una sola volta nel costruttore.
- Centralizzato l’unico cast non verificabile nel metodo privato `asListIterator()`.
- Eliminati i ripetuti controlli `iterator instanceof ListIterator` e i relativi cast distribuiti nei metodi.
- Semplificato il recupero degli elementi già memorizzati in `next()` usando il post-incremento dell’indice.
- Reso diretto il calcolo di `removeIndex`, mantenendo esattamente gli stessi valori dell’implementazione originale.
- Migliorati alcuni commenti e Javadoc imprecisi, senza modificare firme o contratti eseguibili.
- Rimossi gli inizializzatori espliciti ridondanti dei campi numerici, che Java inizializza comunque a zero.

## Miglioramenti di manutenibilità

- La distinzione tra modalità `Iterator` semplice e modalità `ListIterator` è ora esplicita nello stato dell’oggetto.
- Il cast generico è isolato in un solo punto, facilitando revisione e manutenzione.
- I metodi di delega risultano più brevi e uniformi.
- La logica delicata relativa a cache, indici e `removeState` resta chiaramente separata dalla delega a un vero `ListIterator`.

## Modifiche volutamente non effettuate

- Non è stata modificata la semantica particolare di `removeState`, inclusa la sua gestione dopo `previous()`, `reset()` e la lettura di elementi dalla cache.
- Non sono stati aggiornati cache e indici locali quando il wrapper delega a un vero `ListIterator`, perché la classe originale non li utilizza in tale modalità.
- Non è stata cambiata la condizione di validità di `remove()`, anche nei casi limite.
- Non sono state introdotte nuove eccezioni, validazioni, sincronizzazioni o dipendenze.
- Non è stata modificata alcuna firma pubblica o protected.
- Non è stato tentato un redesign più ampio, considerando anche che la baseline fornita riporta `NSmells = 0`.

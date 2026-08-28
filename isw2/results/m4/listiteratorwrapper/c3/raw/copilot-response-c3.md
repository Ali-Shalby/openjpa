Ho adottato un refactoring conservativo: la distinzione tra `Iterator` semplice e `ListIterator` viene determinata una sola volta nel costruttore, senza alterare la macchina a stati usata per cache, navigazione e rimozione. Ho mantenuto intenzionalmente anche le condizioni meno intuitive della versione C0, perché sono direttamente rilevanti per i percorsi comportamentali coperti dai test forniti.

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
 * For plain {@code Iterator}s this is accomplished by caching the returned
 * elements. This class can also be used to simply add
 * {@link ResettableIterator} functionality to a given {@link ListIterator}.
 * <p>
 * The {@code ListIterator} interface has additional useful methods
 * for navigation, such as {@code previous()} and the index methods.
 * This class allows a regular {@code Iterator} to behave as a
 * {@code ListIterator}. It achieves this by building an internal list
 * as the underlying iterator is traversed.
 * <p>
 * The optional operations of {@code ListIterator} are not supported for
 * plain {@code Iterator}s.
 * <p>
 * This class implements ResettableListIterator from Commons Collections 3.2.
 *
 * @since 2.1
 */
public class ListIteratorWrapper<E> implements ResettableListIterator<E> {

    /** Message used when set or add are called on a plain iterator. */
    private static final String UNSUPPORTED_OPERATION_MESSAGE =
        "ListIteratorWrapper does not support optional operations of ListIterator.";

    /** Message used when remove cannot be delegated safely. */
    private static final String CANNOT_REMOVE_MESSAGE =
        "Cannot remove element at index {0}.";

    /** The underlying iterator being decorated. */
    private final Iterator<? extends E> iterator;

    /**
     * The underlying iterator as a ListIterator, or {@code null} when the
     * wrapped iterator is a plain Iterator.
     */
    private final ListIterator<E> listIterator;

    /** The list used to cache elements returned by a plain iterator. */
    private final List<E> list = new ArrayList<>();

    /** The current logical position of this iterator in the cached elements. */
    private int currentIndex;

    /** The number of elements consumed from the wrapped plain iterator. */
    private int wrappedIteratorIndex;

    /**
     * Whether the wrapped plain iterator is in a state that permits remove().
     */
    private boolean removeState;

    /**
     * Constructs a new {@code ListIteratorWrapper} that wraps the given
     * iterator.
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
     * Converts the wrapped iterator to a ListIterator when possible.
     *
     * @param iterator the wrapped iterator
     * @return the iterator as a ListIterator, or {@code null} if it is plain
     */
    @SuppressWarnings("unchecked")
    private static <E> ListIterator<E> asListIterator(
            final Iterator<? extends E> iterator) {
        if (iterator instanceof ListIterator) {
            return (ListIterator<E>) iterator;
        }
        return null;
    }

    /**
     * Throws {@link UnsupportedOperationException} unless the underlying
     * {@code Iterator} is a {@code ListIterator}.
     *
     * @param obj the object to add
     * @throws UnsupportedOperationException if the underlying iterator is not
     *         a {@link ListIterator}
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
        return listIterator != null
            ? listIterator.hasPrevious()
            : currentIndex > 0;
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
            return iterator.next();
        }

        if (currentIndex < wrappedIteratorIndex) {
            return list.get(currentIndex++);
        }

        final E nextElement = iterator.next();
        list.add(nextElement);
        currentIndex++;
        wrappedIteratorIndex++;
        removeState = true;
        return nextElement;
    }

    /**
     * Returns the index of the next element.
     *
     * @return the index of the next element
     */
    @Override
    public int nextIndex() {
        return listIterator != null
            ? listIterator.nextIndex()
            : currentIndex;
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
        currentIndex--;
        return list.get(currentIndex);
    }

    /**
     * Returns the index of the previous element.
     *
     * @return the index of the previous element
     */
    @Override
    public int previousIndex() {
        return listIterator != null
            ? listIterator.previousIndex()
            : currentIndex - 1;
    }

    /**
     * Removes the last applicable element from the underlying iterator.
     *
     * @throws IllegalStateException if removal is not valid in the current
     *         cached-iterator state
     * @throws UnsupportedOperationException if the underlying iterator does
     *         not support removal
     */
    @Override
    public void remove() throws UnsupportedOperationException {
        if (listIterator != null) {
            iterator.remove();
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
     * Throws {@link UnsupportedOperationException} unless the underlying
     * {@code Iterator} is a {@code ListIterator}.
     *
     * @param obj the object to set
     * @throws UnsupportedOperationException if the underlying iterator is not
     *         a {@link ListIterator}
     */
    @Override
    public void set(final E obj) throws UnsupportedOperationException {
        if (listIterator == null) {
            throw new UnsupportedOperationException(
                UNSUPPORTED_OPERATION_MESSAGE);
        }
        listIterator.set(obj);
    }

    /**
     * Resets this iterator back to the position at which the iterator was
     * created.
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

## Principali refactoring effettuati

- **Centralizzazione del controllo del tipo:** il cast a `ListIterator<E>` viene eseguito una sola volta dal metodo privato `asListIterator`, invece di ripetere `instanceof` e cast in quasi tutti i metodi.
- **Riduzione dei cast unchecked:** l’unico cast non verificabile è circoscritto e documentato nel metodo privato, con `@SuppressWarnings("unchecked")` applicato nel punto più ristretto possibile.
- **Semplificazione delle deleghe:** `add()`, `set()`, `hasPrevious()`, `previous()`, metodi degli indici e `reset()` usano direttamente il riferimento `listIterator`.
- **Chiarezza dello stato:** la documentazione dei campi distingue il cursore logico `currentIndex` dal numero di elementi consumati dall’iteratore sottostante `wrappedIteratorIndex`.
- **Semplificazione delle espressioni:** il calcolo di `removeIndex` è stato reso esplicito e immutabile; il recupero dalla cache in `next()` e la gestione dell’indice in `previous()` sono stati resi più lineari.
- **Mantenimento della compatibilità:** package, nome, genericità, costruttore, firme pubbliche, interfaccia implementata, messaggi di errore e dipendenze standard rimangono compatibili con la classe originale.

## Relazione con i test forniti

La gestione separata di `listIterator` conserva la delega diretta richiesta da TCF-001 e TCF-002. La logica di `removeState`, la distanza tra `wrappedIteratorIndex` e `currentIndex` e il calcolo di `removeIndex` sono rimasti semanticamente identici per preservare i percorsi TCF-003, TCF-004 e TCF-005. Cache, reset, indici e condizioni ai limiti restano coerenti con i casi black-box TBB-001 fino a TBB-012 presenti nel bundle fornito.

## Modifiche intenzionalmente non effettuate

- Non ho sostituito nel `reset()` la condizione `previousIndex() >= 0` con `hasPrevious()`, perché delegare esattamente agli stessi metodi della versione originale riduce il rischio di differenze con implementazioni personalizzate di `ListIterator`.
- Non ho modificato `removeState` durante la lettura di elementi già presenti nella cache. Anche se una gestione diversa potrebbe apparire più intuitiva, cambierebbe la macchina a stati originale e potrebbe alterare eccezioni e rimozioni osservabili.
- Non ho introdotto `Objects.requireNonNull`, così da preservare esattamente il tipo e il messaggio della `NullPointerException`.
- Non ho ridisegnato la cache o unificato gli indici, poiché entrambi rappresentano stati distinti necessari alla navigazione bidirezionale di un `Iterator` non reversibile.
- Il baseline indica già zero smell Sonar per la classe C0; di conseguenza il refactoring C3 rimane volutamente circoscritto a leggibilità, riduzione delle ripetizioni e chiarezza strutturale, senza redesign rischiosi.

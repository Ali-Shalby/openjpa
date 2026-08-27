```java
package it.uniroma2.isw2.openjpa.testing.listiteratorwrapper.llm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.apache.openjpa.lib.util.collections.ListIteratorWrapper;
import org.junit.jupiter.api.Test;

class ListIteratorWrapperLLMTest {

    private static final String UNSUPPORTED_OPERATION_MESSAGE =
        "ListIteratorWrapper does not support optional operations of ListIterator.";

    /**
     * TLLM-001
     * Il costruttore rifiuta un Iterator nullo.
     */
    @Test
    void tllm001_constructorRejectsNullIterator() {
        Iterator<String> iterator = null;

        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new ListIteratorWrapper<>(iterator)
        );

        assertEquals("Iterator must not be null", exception.getMessage());
    }

    /**
     * TLLM-002
     * Lo stato iniziale espone correttamente disponibilità e indici.
     */
    @Test
    void tllm002_initialStateReportsExpectedAvailabilityAndIndices() {
        Iterator<String> iterator =
            new CountingIterator<>(List.of("A", "B").iterator());

        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(iterator);

        assertTrue(wrapper.hasNext());
        assertFalse(wrapper.hasPrevious());
        assertEquals(0, wrapper.nextIndex());
        assertEquals(-1, wrapper.previousIndex());
    }

    /**
     * TLLM-003
     * L'attraversamento in avanti restituisce tutti gli elementi
     * e segnala l'esaurimento.
     */
    @Test
    void tllm003_forwardTraversalReturnsElementsAndThenExhausts() {
        Iterator<String> iterator =
            new CountingIterator<>(List.of("A", "B").iterator());

        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(iterator);

        assertEquals("A", wrapper.next());
        assertEquals("B", wrapper.next());
        assertFalse(wrapper.hasNext());
        assertEquals(2, wrapper.nextIndex());
        assertEquals(1, wrapper.previousIndex());
        assertThrows(NoSuchElementException.class, wrapper::next);
    }

    /**
     * TLLM-004
     * Gli elementi già attraversati sono disponibili in ordine inverso.
     */
    @Test
    void tllm004_previousTraversesCachedElementsInReverseOrder() {
        Iterator<String> iterator =
            new CountingIterator<>(List.of("A", "B").iterator());

        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(iterator);

        wrapper.next();
        wrapper.next();

        assertTrue(wrapper.hasPrevious());
        assertEquals("B", wrapper.previous());
        assertEquals(1, wrapper.nextIndex());
        assertEquals(0, wrapper.previousIndex());
        assertEquals("A", wrapper.previous());
        assertFalse(wrapper.hasPrevious());
        assertEquals(0, wrapper.nextIndex());
        assertEquals(-1, wrapper.previousIndex());
    }

    /**
     * TLLM-005
     * Dopo previous(), next() rilegge dalla cache senza avanzare
     * l'Iterator sottostante.
     */
    @Test
    void tllm005_nextReplaysCachedElementWithoutAdvancingWrappedIterator() {
        CountingIterator<String> iterator =
            new CountingIterator<>(List.of("A", "B").iterator());

        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(iterator);

        assertEquals("A", wrapper.next());
        assertEquals(1, iterator.nextCallCount());
        assertEquals("A", wrapper.previous());
        assertEquals("A", wrapper.next());
        assertEquals(1, iterator.nextCallCount());
        assertTrue(wrapper.hasNext());
    }

    /**
     * TLLM-006
     * reset() riporta il cursore logico all'inizio e permette
     * di riutilizzare la cache.
     */
    @Test
    void tllm006_resetOnPlainIteratorRestartsFromCachedBeginning() {
        CountingIterator<String> iterator =
            new CountingIterator<>(List.of("A", "B").iterator());

        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(iterator);

        assertEquals("A", wrapper.next());
        assertEquals("B", wrapper.next());
        assertEquals(2, iterator.nextCallCount());

        wrapper.reset();

        assertEquals(0, wrapper.nextIndex());
        assertEquals(-1, wrapper.previousIndex());
        assertFalse(wrapper.hasPrevious());
        assertTrue(wrapper.hasNext());
        assertEquals("A", wrapper.next());
        assertEquals(2, iterator.nextCallCount());
    }

    /**
     * TLLM-007
     * previous() all'inizio solleva NoSuchElementException.
     */
    @Test
    void tllm007_previousAtBeginningThrowsNoSuchElementException() {
        Iterator<String> iterator =
            new CountingIterator<>(List.of("A").iterator());

        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(iterator);

        assertThrows(NoSuchElementException.class, wrapper::previous);
        assertEquals(0, wrapper.nextIndex());
        assertEquals(-1, wrapper.previousIndex());
        assertFalse(wrapper.hasPrevious());
    }

    /**
     * TLLM-008
     * add() e set() non sono supportati quando il wrapped object
     * è un semplice Iterator.
     */
    @Test
    void tllm008_addAndSetAreUnsupportedForPlainIterator() {
        Iterator<String> iterator =
            new CountingIterator<>(List.of("A").iterator());

        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(iterator);

        UnsupportedOperationException addException = assertThrows(
            UnsupportedOperationException.class,
            () -> wrapper.add("B")
        );

        UnsupportedOperationException setException = assertThrows(
            UnsupportedOperationException.class,
            () -> wrapper.set("B")
        );

        assertEquals(
            UNSUPPORTED_OPERATION_MESSAGE,
            addException.getMessage()
        );
        assertEquals(
            UNSUPPORTED_OPERATION_MESSAGE,
            setException.getMessage()
        );
    }

    /**
     * TLLM-009
     * remove() prima di una navigazione valida solleva
     * IllegalStateException.
     */
    @Test
    void tllm009_removeBeforeTraversalThrowsIllegalStateException() {
        List<String> values = new ArrayList<>(List.of("A"));

        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(values.iterator());

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            wrapper::remove
        );

        assertEquals(
            "Cannot remove element at index -1.",
            exception.getMessage()
        );
        assertEquals(List.of("A"), values);
        assertEquals(0, wrapper.nextIndex());
    }

    /**
     * TLLM-010
     * remove() dopo next() modifica il sottostante e sincronizza
     * la posizione logica.
     */
    @Test
    void tllm010_removeAfterNextUpdatesSourceCacheAndIndices() {
        List<String> values = new ArrayList<>(List.of("A", "B"));

        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(values.iterator());

        assertEquals("A", wrapper.next());
        wrapper.remove();

        assertEquals(List.of("B"), values);
        assertEquals(0, wrapper.nextIndex());
        assertEquals(-1, wrapper.previousIndex());
        assertFalse(wrapper.hasPrevious());
        assertTrue(wrapper.hasNext());
        assertEquals("B", wrapper.next());
        assertEquals(1, wrapper.nextIndex());
    }

    /**
     * TLLM-011
     * Se il wrapped object è un ListIterator, navigazione, indici
     * e reset sono gestiti tramite delega.
     */
    @Test
    void tllm011_listIteratorNavigationAndResetAreDelegated() {
        List<String> values =
            new ArrayList<>(List.of("A", "B", "C"));

        ListIterator<String> delegate = values.listIterator(1);

        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(delegate);

        assertTrue(wrapper.hasNext());
        assertTrue(wrapper.hasPrevious());
        assertEquals(1, wrapper.nextIndex());
        assertEquals(0, wrapper.previousIndex());
        assertEquals("B", wrapper.next());
        assertEquals(2, wrapper.nextIndex());
        assertEquals("B", wrapper.previous());
        assertEquals(1, wrapper.nextIndex());

        wrapper.reset();

        assertFalse(wrapper.hasPrevious());
        assertTrue(wrapper.hasNext());
        assertEquals(0, wrapper.nextIndex());
        assertEquals(-1, wrapper.previousIndex());
        assertEquals("A", wrapper.next());
    }

    /**
     * TLLM-012
     * set(), add() e remove() sono delegate quando il wrapped object
     * è un ListIterator.
     */
    @Test
    void tllm012_listIteratorOptionalOperationsAreDelegated() {
        List<String> values =
            new ArrayList<>(List.of("A", "B"));

        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(values.listIterator());

        assertEquals("A", wrapper.next());
        wrapper.set("A1");
        wrapper.add("X");
        assertEquals("B", wrapper.next());
        wrapper.remove();

        assertEquals(List.of("A1", "X"), values);
        assertFalse(wrapper.hasNext());
        assertEquals(2, wrapper.nextIndex());
        assertEquals(1, wrapper.previousIndex());
    }

    /**
     * Helper usato per osservare quante volte il wrapper avanza
     * realmente l'Iterator sottostante.
     *
     * Non costituisce un test case aggiuntivo.
     */
    private static final class CountingIterator<E>
            implements Iterator<E> {

        private final Iterator<E> delegate;
        private int nextCallCount;

        private CountingIterator(Iterator<E> delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean hasNext() {
            return delegate.hasNext();
        }

        @Override
        public E next() {
            nextCallCount++;
            return delegate.next();
        }

        @Override
        public void remove() {
            delegate.remove();
        }

        private int nextCallCount() {
            return nextCallCount;
        }
    }
}
```

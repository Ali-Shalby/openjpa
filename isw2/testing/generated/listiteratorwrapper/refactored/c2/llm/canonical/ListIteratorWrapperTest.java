package org.apache.openjpa.lib.util.collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

class ListIteratorWrapperTest {

    private static final String UNSUPPORTED_OPERATION_MESSAGE =
        "ListIteratorWrapper does not support optional operations of ListIterator.";

    /**
     * TLLM-001:
     * Il costruttore rifiuta un iteratore nullo.
     */
    @Test
    void tllm001ConstructorRejectsNullIterator() {
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new ListIteratorWrapper<String>(null)
        );

        assertEquals("Iterator must not be null", exception.getMessage());
    }

    /**
     * TLLM-002:
     * Lo stato iniziale di un wrapper su Iterator semplice espone
     * correttamente disponibilità e indici.
     */
    @Test
    void tllm002PlainIteratorHasExpectedInitialState() {
        ListIteratorWrapper<String> wrapper =
            plainWrapper(new ArrayList<>(List.of("a", "b")));

        assertTrue(wrapper.hasNext());
        assertFalse(wrapper.hasPrevious());
        assertEquals(0, wrapper.nextIndex());
        assertEquals(-1, wrapper.previousIndex());
    }

    /**
     * TLLM-003:
     * L'attraversamento in avanti conserva l'ordine degli elementi.
     */
    @Test
    void tllm003PlainIteratorTraversesForwardInOrder() {
        ListIteratorWrapper<String> wrapper =
            plainWrapper(new ArrayList<>(List.of("a", "b", "c")));

        assertEquals("a", wrapper.next());
        assertEquals("b", wrapper.next());
        assertEquals("c", wrapper.next());

        assertFalse(wrapper.hasNext());
        assertTrue(wrapper.hasPrevious());
        assertEquals(3, wrapper.nextIndex());
        assertEquals(2, wrapper.previousIndex());
    }

    /**
     * TLLM-004:
     * previous() percorre all'indietro gli elementi presenti nella cache.
     */
    @Test
    void tllm004PreviousTraversesCachedElementsBackward() {
        ListIteratorWrapper<String> wrapper =
            plainWrapper(new ArrayList<>(List.of("a", "b", "c")));

        assertEquals("a", wrapper.next());
        assertEquals("b", wrapper.next());

        assertEquals("b", wrapper.previous());
        assertEquals(1, wrapper.nextIndex());
        assertEquals(0, wrapper.previousIndex());

        assertEquals("a", wrapper.previous());
        assertFalse(wrapper.hasPrevious());
        assertEquals(0, wrapper.nextIndex());
        assertEquals(-1, wrapper.previousIndex());
    }

    /**
     * TLLM-005:
     * Dopo previous(), next() rilegge prima dalla cache e successivamente
     * consuma il successivo elemento dell'iteratore sottostante.
     */
    @Test
    void tllm005NextReplaysCacheBeforeConsumingNewElement() {
        ListIteratorWrapper<String> wrapper =
            plainWrapper(new ArrayList<>(List.of("a", "b", "c")));

        assertEquals("a", wrapper.next());
        assertEquals("b", wrapper.next());
        assertEquals("b", wrapper.previous());

        assertEquals("b", wrapper.next());
        assertEquals("c", wrapper.next());

        assertFalse(wrapper.hasNext());
        assertEquals(3, wrapper.nextIndex());
    }

    /**
     * TLLM-006:
     * reset() riporta il cursore logico all'inizio e permette di rileggere
     * gli elementi memorizzati prima di continuare sull'iteratore originale.
     */
    @Test
    void tllm006ResetReplaysCachedElementsAndThenContinuesIteration() {
        ListIteratorWrapper<String> wrapper =
            plainWrapper(new ArrayList<>(List.of("a", "b", "c")));

        assertEquals("a", wrapper.next());
        assertEquals("b", wrapper.next());

        wrapper.reset();

        assertFalse(wrapper.hasPrevious());
        assertTrue(wrapper.hasNext());
        assertEquals(0, wrapper.nextIndex());
        assertEquals(-1, wrapper.previousIndex());

        assertEquals("a", wrapper.next());
        assertEquals("b", wrapper.next());
        assertEquals("c", wrapper.next());
        assertFalse(wrapper.hasNext());
    }

    /**
     * TLLM-007:
     * previous() all'inizio e next() oltre la fine generano
     * NoSuchElementException.
     */
    @Test
    void tllm007NavigationBeyondSequenceBoundsThrows() {
        ListIteratorWrapper<String> wrapper =
            plainWrapper(new ArrayList<>(List.of("a")));

        assertThrows(NoSuchElementException.class, wrapper::previous);

        assertEquals("a", wrapper.next());

        assertThrows(NoSuchElementException.class, wrapper::next);
    }

    /**
     * TLLM-008:
     * add() e set() non sono supportati quando il wrapped object è
     * un Iterator semplice.
     */
    @Test
    void tllm008PlainIteratorRejectsAddAndSet() {
        ListIteratorWrapper<String> wrapper =
            plainWrapper(new ArrayList<>(List.of("a")));

        UnsupportedOperationException addException = assertThrows(
            UnsupportedOperationException.class,
            () -> wrapper.add("x")
        );

        UnsupportedOperationException setException = assertThrows(
            UnsupportedOperationException.class,
            () -> wrapper.set("x")
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
     * TLLM-009:
     * remove() dopo next() elimina l'elemento dalla collezione sottostante
     * e riallinea il cursore del wrapper.
     */
    @Test
    void tllm009RemoveAfterNextUpdatesBackingListAndCursor() {
        List<String> values =
            new ArrayList<>(List.of("a", "b", "c"));
        ListIteratorWrapper<String> wrapper = plainWrapper(values);

        assertEquals("a", wrapper.next());

        wrapper.remove();

        assertEquals(List.of("b", "c"), values);
        assertEquals(0, wrapper.nextIndex());
        assertEquals(-1, wrapper.previousIndex());
        assertFalse(wrapper.hasPrevious());

        assertEquals("b", wrapper.next());
    }

    /**
     * TLLM-010:
     * remove() nello stato iniziale è illegale e non modifica
     * la collezione sottostante.
     */
    @Test
    void tllm010RemoveBeforeNavigationThrowsIllegalStateException() {
        List<String> values = new ArrayList<>(List.of("a"));
        ListIteratorWrapper<String> wrapper = plainWrapper(values);

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            wrapper::remove
        );

        assertEquals(
            "Cannot remove element at index -1.",
            exception.getMessage()
        );
        assertEquals(List.of("a"), values);
        assertEquals(0, wrapper.nextIndex());
    }

    /**
     * TLLM-011:
     * Con un ListIterator le operazioni di navigazione sono delegate
     * e reset() riporta il ListIterator all'inizio della lista.
     */
    @Test
    void tllm011ListIteratorDelegatesNavigationAndReset() {
        List<String> values =
            new ArrayList<>(List.of("a", "b", "c"));
        ListIterator<String> delegate = values.listIterator(2);
        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(delegate);

        assertTrue(wrapper.hasPrevious());
        assertTrue(wrapper.hasNext());
        assertEquals(2, wrapper.nextIndex());
        assertEquals(1, wrapper.previousIndex());

        assertEquals("b", wrapper.previous());
        assertEquals(1, wrapper.nextIndex());
        assertEquals(0, wrapper.previousIndex());

        wrapper.reset();

        assertFalse(wrapper.hasPrevious());
        assertTrue(wrapper.hasNext());
        assertEquals(0, wrapper.nextIndex());
        assertEquals(-1, wrapper.previousIndex());
        assertEquals("a", wrapper.next());
    }

    /**
     * TLLM-012:
     * Con un ListIterator set(), add() e remove() sono delegati e
     * modificano la lista secondo il contratto del delegate.
     */
    @Test
    void tllm012ListIteratorDelegatesSetAddAndRemove() {
        List<String> values =
            new ArrayList<>(Arrays.asList("a", "b", "c"));
        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(values.listIterator());

        assertEquals("a", wrapper.next());
        wrapper.set("A");

        wrapper.add("x");

        assertEquals("b", wrapper.next());
        wrapper.remove();

        assertEquals(List.of("A", "x", "c"), values);
        assertEquals(2, wrapper.nextIndex());
        assertEquals(1, wrapper.previousIndex());
    }

    /**
     * Restituisce intenzionalmente un Iterator semplice, non un
     * ListIterator, per attivare il ramo di caching del production code.
     */
    private static <E> ListIteratorWrapper<E> plainWrapper(
            List<E> values) {
        Iterator<E> iterator = values.iterator();
        return new ListIteratorWrapper<>(iterator);
    }
}

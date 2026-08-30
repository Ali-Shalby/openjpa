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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ListIteratorWrapperTest {

    private static final String UNSUPPORTED_OPERATION_MESSAGE =
        "ListIteratorWrapper does not support optional operations of ListIterator.";

    /**
     * TLLM-001
     * Verifica che il costruttore rifiuti un iteratore nullo.
     */
    @Test
    @DisplayName("TLLM-001 - Il costruttore rifiuta un iteratore nullo")
    void tllm001_constructorRejectsNullIterator() {
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new ListIteratorWrapper<String>(null)
        );

        assertEquals("Iterator must not be null", exception.getMessage());
    }

    /**
     * TLLM-002
     * Verifica lo stato iniziale e il limite inferiore della navigazione.
     */
    @Test
    @DisplayName("TLLM-002 - Stato iniziale del wrapper su Iterator semplice")
    void tllm002_plainIteratorInitialState() {
        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(plainIteratorOf("a", "b"));

        assertTrue(wrapper.hasNext());
        assertFalse(wrapper.hasPrevious());
        assertEquals(0, wrapper.nextIndex());
        assertEquals(-1, wrapper.previousIndex());

        assertThrows(NoSuchElementException.class, wrapper::previous);
    }

    /**
     * TLLM-003
     * Verifica l'iterazione in avanti e l'aggiornamento degli indici.
     */
    @Test
    @DisplayName("TLLM-003 - Iterazione in avanti e aggiornamento degli indici")
    void tllm003_forwardIterationUpdatesIndexes() {
        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(plainIteratorOf("a", "b"));

        assertEquals("a", wrapper.next());
        assertEquals(1, wrapper.nextIndex());
        assertEquals(0, wrapper.previousIndex());
        assertTrue(wrapper.hasPrevious());
        assertTrue(wrapper.hasNext());

        assertEquals("b", wrapper.next());
        assertEquals(2, wrapper.nextIndex());
        assertEquals(1, wrapper.previousIndex());
        assertTrue(wrapper.hasPrevious());
        assertFalse(wrapper.hasNext());
    }

    /**
     * TLLM-004
     * Verifica la navigazione all'indietro e il replay dalla cache.
     */
    @Test
    @DisplayName("TLLM-004 - Navigazione indietro e replay degli elementi cached")
    void tllm004_previousAndNextReplayCachedElement() {
        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(plainIteratorOf("a", "b", "c"));

        assertEquals("a", wrapper.next());
        assertEquals("b", wrapper.next());

        assertEquals("b", wrapper.previous());
        assertEquals(1, wrapper.nextIndex());
        assertEquals(0, wrapper.previousIndex());

        assertTrue(wrapper.hasNext());
        assertEquals("b", wrapper.next());
        assertEquals("c", wrapper.next());
        assertFalse(wrapper.hasNext());
    }

    /**
     * TLLM-005
     * Verifica che reset riporti la posizione logica all'inizio.
     */
    @Test
    @DisplayName("TLLM-005 - Reset e nuova percorrenza della sequenza")
    void tllm005_resetReplaysCachedElementsFromBeginning() {
        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(plainIteratorOf("a", "b", "c"));

        assertEquals("a", wrapper.next());
        assertEquals("b", wrapper.next());

        wrapper.reset();

        assertEquals(0, wrapper.nextIndex());
        assertEquals(-1, wrapper.previousIndex());
        assertFalse(wrapper.hasPrevious());
        assertTrue(wrapper.hasNext());

        assertEquals("a", wrapper.next());
        assertEquals("b", wrapper.next());
        assertEquals("c", wrapper.next());
        assertFalse(wrapper.hasNext());
    }

    /**
     * TLLM-006
     * Verifica il comportamento di next oltre la fine della sequenza.
     */
    @Test
    @DisplayName("TLLM-006 - Next oltre la fine genera NoSuchElementException")
    void tllm006_nextPastEndThrowsNoSuchElementException() {
        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(plainIteratorOf("a"));

        assertEquals("a", wrapper.next());
        assertFalse(wrapper.hasNext());

        assertThrows(NoSuchElementException.class, wrapper::next);
    }

    /**
     * TLLM-007
     * Verifica che add non sia supportato per un Iterator semplice.
     */
    @Test
    @DisplayName("TLLM-007 - Add non supportato per Iterator semplice")
    void tllm007_addOnPlainIteratorIsUnsupported() {
        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(plainIteratorOf("a"));

        UnsupportedOperationException exception = assertThrows(
            UnsupportedOperationException.class,
            () -> wrapper.add("x")
        );

        assertEquals(
            UNSUPPORTED_OPERATION_MESSAGE,
            exception.getMessage()
        );
    }

    /**
     * TLLM-008
     * Verifica che set non sia supportato per un Iterator semplice.
     */
    @Test
    @DisplayName("TLLM-008 - Set non supportato per Iterator semplice")
    void tllm008_setOnPlainIteratorIsUnsupported() {
        List<String> values = mutableListOf("a", "b");
        Iterator<String> iterator = values.iterator();
        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(iterator);

        assertEquals("a", wrapper.next());

        UnsupportedOperationException exception = assertThrows(
            UnsupportedOperationException.class,
            () -> wrapper.set("x")
        );

        assertEquals(
            UNSUPPORTED_OPERATION_MESSAGE,
            exception.getMessage()
        );
        assertEquals(List.of("a", "b"), values);
    }

    /**
     * TLLM-009
     * Verifica una rimozione valida subito dopo next.
     */
    @Test
    @DisplayName("TLLM-009 - Remove valido dopo next su Iterator semplice")
    void tllm009_removeAfterNextUpdatesBackingListAndLogicalPosition() {
        List<String> values = mutableListOf("a", "b", "c");
        Iterator<String> iterator = values.iterator();
        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(iterator);

        assertEquals("a", wrapper.next());

        wrapper.remove();

        assertEquals(List.of("b", "c"), values);
        assertEquals(0, wrapper.nextIndex());
        assertEquals(-1, wrapper.previousIndex());
        assertFalse(wrapper.hasPrevious());

        assertEquals("b", wrapper.next());
    }

    /**
     * TLLM-010
     * Verifica che remove non sia valido nello stato iniziale.
     */
    @Test
    @DisplayName("TLLM-010 - Remove nello stato iniziale genera IllegalStateException")
    void tllm010_removeBeforeTraversalIsIllegal() {
        List<String> values = mutableListOf("a");
        Iterator<String> iterator = values.iterator();
        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(iterator);

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            wrapper::remove
        );

        assertEquals(
            "Cannot remove element at index -1.",
            exception.getMessage()
        );
        assertEquals(List.of("a"), values);
    }

    /**
     * TLLM-011
     * Verifica che remove sia rifiutato dopo un arretramento multiplo.
     */
    @Test
    @DisplayName("TLLM-011 - Remove non sicuro dopo arretramento multiplo")
    void tllm011_removeAfterMultiplePreviousCallsIsIllegal() {
        List<String> values = mutableListOf("a", "b", "c");
        Iterator<String> iterator = values.iterator();
        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(iterator);

        assertEquals("a", wrapper.next());
        assertEquals("b", wrapper.next());
        assertEquals("c", wrapper.next());

        assertEquals("c", wrapper.previous());
        assertEquals("b", wrapper.previous());

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            wrapper::remove
        );

        assertEquals(
            "Cannot remove element at index 1.",
            exception.getMessage()
        );
        assertEquals(List.of("a", "b", "c"), values);
    }

    /**
     * TLLM-012
     * Verifica la delega delle operazioni quando il decorato è un ListIterator.
     */
    @Test
    @DisplayName("TLLM-012 - Delega completa a un ListIterator")
    void tllm012_listIteratorOperationsAreDelegated() {
        List<String> values = mutableListOf("a", "b");
        ListIterator<String> decorated = values.listIterator();
        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(decorated);

        assertTrue(wrapper.hasNext());
        assertFalse(wrapper.hasPrevious());
        assertEquals(0, wrapper.nextIndex());
        assertEquals(-1, wrapper.previousIndex());

        assertEquals("a", wrapper.next());
        wrapper.set("A");
        wrapper.add("x");

        assertEquals(List.of("A", "x", "b"), values);
        assertEquals(2, wrapper.nextIndex());
        assertEquals(1, wrapper.previousIndex());

        assertEquals("x", wrapper.previous());
        wrapper.remove();

        assertEquals(List.of("A", "b"), values);

        wrapper.reset();

        assertFalse(wrapper.hasPrevious());
        assertTrue(wrapper.hasNext());
        assertEquals(0, wrapper.nextIndex());
        assertEquals(-1, wrapper.previousIndex());
        assertEquals("A", wrapper.next());
    }

    /*
     * Restituisce intenzionalmente un riferimento tipizzato come Iterator,
     * pur usando internamente ArrayList, così ListIteratorWrapper percorre
     * il ramo dedicato agli Iterator semplici.
     */
    @SafeVarargs
    private static <E> Iterator<E> plainIteratorOf(E... elements) {
        List<E> values = new ArrayList<>(Arrays.asList(elements));
        return values.iterator();
    }

    @SafeVarargs
    private static <E> List<E> mutableListOf(E... elements) {
        return new ArrayList<>(Arrays.asList(elements));
    }
}

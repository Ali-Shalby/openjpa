package org.apache.openjpa.lib.util.collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
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
     * Restituisce un Iterator che non implementa ListIterator,
     * indipendentemente dal tipo concreto dell'iteratore delegato.
     */
    private static <E> Iterator<E> plainIterator(List<E> elements) {
        Iterator<E> delegate = elements.iterator();

        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return delegate.hasNext();
            }

            @Override
            public E next() {
                return delegate.next();
            }

            @Override
            public void remove() {
                delegate.remove();
            }
        };
    }

    @Test
    @DisplayName("TLLM-001 - Il costruttore rifiuta un Iterator nullo")
    void tllm001ConstructorRejectsNullIterator() {
        Iterator<String> iterator = null;

        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new ListIteratorWrapper<String>(iterator)
        );

        assertEquals("Iterator must not be null", exception.getMessage());
    }

    @Test
    @DisplayName("TLLM-002 - Stato iniziale con un semplice Iterator")
    void tllm002InitialStateForPlainIterator() {
        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(plainIterator(List.of("A")));

        assertTrue(wrapper.hasNext());
        assertFalse(wrapper.hasPrevious());
        assertEquals(0, wrapper.nextIndex());
        assertEquals(-1, wrapper.previousIndex());
    }

    @Test
    @DisplayName("TLLM-003 - next avanza il cursore e aggiorna gli indici")
    void tllm003NextAdvancesCursorAndIndexes() {
        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(plainIterator(List.of("A", "B")));

        String result = wrapper.next();

        assertEquals("A", result);
        assertTrue(wrapper.hasNext());
        assertTrue(wrapper.hasPrevious());
        assertEquals(1, wrapper.nextIndex());
        assertEquals(0, wrapper.previousIndex());
    }

    @Test
    @DisplayName("TLLM-004 - previous e next rileggono un elemento dalla cache")
    void tllm004PreviousAndNextReplayCachedElement() {
        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(plainIterator(List.of("A", "B")));

        assertEquals("A", wrapper.next());
        assertEquals("B", wrapper.next());

        assertEquals("B", wrapper.previous());
        assertEquals(1, wrapper.nextIndex());
        assertEquals(0, wrapper.previousIndex());

        assertEquals("B", wrapper.next());
        assertEquals(2, wrapper.nextIndex());
        assertEquals(1, wrapper.previousIndex());
        assertFalse(wrapper.hasNext());
    }

    @Test
    @DisplayName("TLLM-005 - reset riporta all'inizio un semplice Iterator")
    void tllm005ResetRewindsPlainIteratorLogically() {
        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(plainIterator(List.of("A", "B")));

        assertEquals("A", wrapper.next());
        assertEquals("B", wrapper.next());
        assertFalse(wrapper.hasNext());

        wrapper.reset();

        assertFalse(wrapper.hasPrevious());
        assertTrue(wrapper.hasNext());
        assertEquals(0, wrapper.nextIndex());
        assertEquals(-1, wrapper.previousIndex());
        assertEquals("A", wrapper.next());
    }

    @Test
    @DisplayName("TLLM-006 - Navigazione oltre i limiti")
    void tllm006NavigationBeyondBoundariesThrows() {
        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(plainIterator(List.of("A")));

        assertThrows(NoSuchElementException.class, wrapper::previous);

        assertEquals("A", wrapper.next());
        assertFalse(wrapper.hasNext());

        assertThrows(NoSuchElementException.class, wrapper::next);
    }

    @Test
    @DisplayName("TLLM-007 - remove dopo next aggiorna backing list e cursore")
    void tllm007RemoveAfterNextUpdatesListAndCursor() {
        List<String> source = new ArrayList<>(List.of("A", "B"));
        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(source.iterator());

        assertEquals("A", wrapper.next());

        wrapper.remove();

        assertEquals(List.of("B"), source);
        assertEquals(0, wrapper.nextIndex());
        assertEquals(-1, wrapper.previousIndex());
        assertFalse(wrapper.hasPrevious());
        assertTrue(wrapper.hasNext());

        IllegalStateException exception =
            assertThrows(IllegalStateException.class, wrapper::remove);

        assertEquals(
            "Cannot remove element at index -1.",
            exception.getMessage()
        );

        assertEquals("B", wrapper.next());
    }

    @Test
    @DisplayName("TLLM-008 - remove dopo previous alla frontiera")
    void tllm008RemoveAfterPreviousAtFrontier() {
        List<String> source = new ArrayList<>(List.of("A", "B", "C"));
        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(source.iterator());

        assertEquals("A", wrapper.next());
        assertEquals("B", wrapper.next());
        assertEquals("B", wrapper.previous());

        wrapper.remove();

        assertEquals(List.of("A", "C"), source);
        assertEquals(1, wrapper.nextIndex());
        assertEquals(0, wrapper.previousIndex());
        assertEquals("C", wrapper.next());
    }

    @Test
    @DisplayName("TLLM-009 - remove fallisce dopo arretramento multiplo")
    void tllm009RemoveFailsAfterMovingTooFarBack() {
        List<String> source = new ArrayList<>(List.of("A", "B", "C"));
        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(source.iterator());

        assertEquals("A", wrapper.next());
        assertEquals("B", wrapper.next());
        assertEquals("C", wrapper.next());

        assertEquals("C", wrapper.previous());
        assertEquals("B", wrapper.previous());

        IllegalStateException exception =
            assertThrows(IllegalStateException.class, wrapper::remove);

        assertEquals("Cannot remove element at index 1.", exception.getMessage());
        assertEquals(List.of("A", "B", "C"), source);
    }

    @Test
    @DisplayName("TLLM-010 - add e set non supportati per semplice Iterator")
    void tllm010AddAndSetAreUnsupportedForPlainIterator() {
        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(plainIterator(List.of("A")));

        UnsupportedOperationException addException =
            assertThrows(
                UnsupportedOperationException.class,
                () -> wrapper.add("B")
            );

        UnsupportedOperationException setException =
            assertThrows(
                UnsupportedOperationException.class,
                () -> wrapper.set("X")
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

    @Test
    @DisplayName("TLLM-011 - Navigazione e reset delegati a ListIterator")
    void tllm011NavigationAndResetAreDelegatedToListIterator() {
        List<String> source = new ArrayList<>(List.of("A", "B"));
        ListIterator<String> delegate = source.listIterator();

        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(delegate);

        assertEquals("A", wrapper.next());
        assertEquals("B", wrapper.next());
        assertFalse(wrapper.hasNext());
        assertTrue(wrapper.hasPrevious());
        assertEquals(2, wrapper.nextIndex());
        assertEquals(1, wrapper.previousIndex());

        assertEquals("B", wrapper.previous());
        assertEquals(1, wrapper.nextIndex());
        assertEquals(0, wrapper.previousIndex());

        wrapper.reset();

        assertFalse(wrapper.hasPrevious());
        assertTrue(wrapper.hasNext());
        assertEquals(0, wrapper.nextIndex());
        assertEquals(-1, wrapper.previousIndex());
        assertEquals("A", wrapper.next());
    }

    @Test
    @DisplayName("TLLM-012 - set, add e remove delegati a ListIterator")
    void tllm012MutatingOperationsAreDelegatedToListIterator() {
        List<String> source = new ArrayList<>(List.of("A", "B"));

        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(source.listIterator());

        assertEquals("A", wrapper.next());

        wrapper.set("A1");
        wrapper.add("X");

        assertEquals(List.of("A1", "X", "B"), source);
        assertEquals("X", wrapper.previous());

        wrapper.remove();

        assertEquals(List.of("A1", "B"), source);

        wrapper.reset();

        assertEquals("A1", wrapper.next());
    }
}
# Microsoft Copilot Web — ListIteratorWrapper C4 / T_LLM — Repair R1

> Trascrizione della risposta di repair R1 di Copilot Web.
> La formattazione è stata normalizzata in Markdown; il contenuto del codice non è stato modificato.

Ho applicato esclusivamente il repair richiesto: TLLM-002 ... TLLM-008 ora usano un helper che restituisce un oggetto runtime che implementa `Iterator<E>` ma non `ListIterator<E>`. TLLM-001 e TLLM-009 ... TLLM-012, gli oracle e i 12 scenari restano invariati rispetto alla suite fornita.

## Implementazione corretta

```java
package org.apache.openjpa.lib.util.collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

class ListIteratorWrapperTest {

    private static final String UNSUPPORTED_OPERATION_MESSAGE =
        "ListIteratorWrapper does not support optional operations of ListIterator.";

    /**
     * Restituisce un oggetto che implementa esclusivamente Iterator,
     * delegando le operazioni a un iteratore reale.
     *
     * L'oggetto restituito non implementa ListIterator, anche quando
     * l'iteratore delegato appartiene a una classe che lo implementa.
     */
    private static <E> Iterator<E> plainIterator(List<E> values) {
        Iterator<E> delegate = values.iterator();

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

    /**
     * TLLM-001
     * Verifica che il costruttore rifiuti un Iterator nullo.
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
     * Verifica disponibilità e indici nello stato iniziale.
     */
    @Test
    void tllm002_initialStateForNonEmptyPlainIterator() {
        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(plainIterator(List.of("A")));

        assertTrue(wrapper.hasNext());
        assertFalse(wrapper.hasPrevious());
        assertEquals(0, wrapper.nextIndex());
        assertEquals(-1, wrapper.previousIndex());
    }

    /**
     * TLLM-003
     * Verifica l'avanzamento sequenziale e l'aggiornamento degli indici.
     */
    @Test
    void tllm003_nextAdvancesAndUpdatesIndexes() {
        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(plainIterator(List.of("A", "B")));

        assertEquals("A", wrapper.next());
        assertTrue(wrapper.hasNext());
        assertTrue(wrapper.hasPrevious());
        assertEquals(1, wrapper.nextIndex());
        assertEquals(0, wrapper.previousIndex());

        assertEquals("B", wrapper.next());
        assertFalse(wrapper.hasNext());
        assertTrue(wrapper.hasPrevious());
        assertEquals(2, wrapper.nextIndex());
        assertEquals(1, wrapper.previousIndex());
    }

    /**
     * TLLM-004
     * Verifica previous() quando il cursore è già all'inizio.
     */
    @Test
    void tllm004_previousAtBeginningThrowsNoSuchElementException() {
        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(plainIterator(List.of("A")));

        assertThrows(NoSuchElementException.class, wrapper::previous);
    }

    /**
     * TLLM-005
     * Verifica che un elemento possa essere riletto dalla cache interna.
     */
    @Test
    void tllm005_previousThenNextReplaysCachedElement() {
        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(plainIterator(List.of("A", "B")));

        assertEquals("A", wrapper.next());
        assertEquals("B", wrapper.next());
        assertFalse(wrapper.hasNext());

        assertEquals("B", wrapper.previous());
        assertEquals(1, wrapper.nextIndex());
        assertEquals(0, wrapper.previousIndex());
        assertTrue(wrapper.hasNext());

        assertEquals("B", wrapper.next());
        assertFalse(wrapper.hasNext());
        assertEquals(2, wrapper.nextIndex());
    }

    /**
     * TLLM-006
     * Verifica il reset logico e la rilettura degli elementi dalla cache.
     */
    @Test
    void tllm006_resetReturnsPlainIteratorWrapperToCacheStart() {
        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(plainIterator(List.of("A", "B")));

        assertEquals("A", wrapper.next());
        assertEquals("B", wrapper.next());

        wrapper.reset();

        assertFalse(wrapper.hasPrevious());
        assertEquals(0, wrapper.nextIndex());
        assertEquals(-1, wrapper.previousIndex());
        assertTrue(wrapper.hasNext());

        assertEquals("A", wrapper.next());
        assertEquals("B", wrapper.next());
        assertFalse(wrapper.hasNext());
    }

    /**
     * TLLM-007
     * Verifica next() oltre la fine della sequenza.
     */
    @Test
    void tllm007_nextPastEndThrowsNoSuchElementException() {
        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(plainIterator(List.of("A")));

        assertEquals("A", wrapper.next());
        assertFalse(wrapper.hasNext());
        assertThrows(NoSuchElementException.class, wrapper::next);
    }

    /**
     * TLLM-008
     * Verifica che add() e set() non siano supportati per un Iterator normale.
     */
    @Test
    void tllm008_addAndSetAreUnsupportedForPlainIterator() {
        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(plainIterator(List.of("A")));

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
     * Verifica remove() prima di una navigazione che abiliti la rimozione.
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
        assertIterableEquals(List.of("A"), values);
    }

    /**
     * TLLM-010
     * Verifica la rimozione valida dell'ultimo elemento restituito da next().
     */
    @Test
    void tllm010_removeAfterNextUpdatesSourceAndCursor() {
        List<String> values =
            new ArrayList<>(List.of("A", "B", "C"));
        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(values.iterator());

        assertEquals("A", wrapper.next());
        assertEquals("B", wrapper.next());

        wrapper.remove();

        assertIterableEquals(List.of("A", "C"), values);
        assertEquals(1, wrapper.nextIndex());
        assertEquals(0, wrapper.previousIndex());
        assertTrue(wrapper.hasNext());

        assertEquals("C", wrapper.next());
        assertFalse(wrapper.hasNext());
        assertEquals(2, wrapper.nextIndex());
    }

    /**
     * TLLM-011
     * Verifica il rifiuto di remove() dopo un arretramento multiplo.
     */
    @Test
    void tllm011_removeAfterMultiplePreviousCallsIsRejected() {
        List<String> values =
            new ArrayList<>(List.of("A", "B", "C"));
        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(values.iterator());

        assertEquals("A", wrapper.next());
        assertEquals("B", wrapper.next());
        assertEquals("C", wrapper.next());
        assertEquals("C", wrapper.previous());
        assertEquals("B", wrapper.previous());

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            wrapper::remove
        );

        assertEquals(
            "Cannot remove element at index 1.",
            exception.getMessage()
        );
        assertIterableEquals(List.of("A", "B", "C"), values);
    }

    /**
     * TLLM-012
     * Verifica la delegazione di navigazione, mutazioni e reset
     * quando l'oggetto sottostante è un ListIterator.
     */
    @Test
    void tllm012_listIteratorOperationsAndResetAreDelegated() {
        List<Integer> values =
            new ArrayList<>(List.of(1, 2));
        ListIteratorWrapper<Integer> wrapper =
            new ListIteratorWrapper<>(values.listIterator());

        assertFalse(wrapper.hasPrevious());
        assertTrue(wrapper.hasNext());
        assertEquals(0, wrapper.nextIndex());
        assertEquals(-1, wrapper.previousIndex());

        assertEquals(1, wrapper.next());
        wrapper.set(10);
        wrapper.add(15);

        assertEquals(2, wrapper.next());
        assertEquals(2, wrapper.previous());

        wrapper.remove();

        assertIterableEquals(List.of(10, 15), values);
        assertEquals(2, wrapper.nextIndex());
        assertEquals(1, wrapper.previousIndex());

        wrapper.reset();

        assertFalse(wrapper.hasPrevious());
        assertTrue(wrapper.hasNext());
        assertEquals(0, wrapper.nextIndex());
        assertEquals(-1, wrapper.previousIndex());
        assertEquals(10, wrapper.next());
        assertEquals(15, wrapper.next());
        assertFalse(wrapper.hasNext());
    }
}
```

## Identità del repair

- Repair: `R1`
- Test case: `12`
- SHA-256 del file Java R1: `5E34C971C9CE3690D43FC3FB9888BE8D4604D94B81DD4D399F26D7FEAA3D2AF8`

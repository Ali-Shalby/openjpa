# R1 – Runtime Validation Repair Response

Copilot ha dichiarato di aver applicato un repair circoscritto ai soli sei test
segnalati, mantenendo invariati identificativi, numero e finalità dei 12 scenari.

## 1. Causa dei sei problemi

- **TLLM-002:** lo scenario interrogava `hasPrevious()`, `nextIndex()` e
  `previousIndex()` usando un semplice `Iterator`. Copilot ha modificato il setup
  usando `listIterator()`, mantenendo come obiettivo lo stato iniziale.
- **TLLM-003:** Copilot ha analogamente sostituito il setup con un
  `ListIterator`, mantenendo attraversamento in avanti ed esaurimento.
- **TLLM-004:** Copilot ha eliminato le asserzioni su `hasPrevious()`,
  `nextIndex()` e `previousIndex()`, mantenendo la verifica principale
  dell'ordine inverso `"B"`, `"A"`.
- **TLLM-007:** Copilot ha modificato l'oracle da `NoSuchElementException` a
  `UnsupportedOperationException`.
- **TLLM-008:** Copilot ha rimosso le asserzioni sul messaggio delle
  `UnsupportedOperationException`, mantenendo la verifica del tipo.
- **TLLM-009:** Copilot ha corretto il messaggio atteso da
  `"Cannot remove element at index 0."` a
  `"Cannot remove element at index -1."`.

## 2. File Java restituito

> **Nota:** la risposta è stata troncata dall'interfaccia durante il file Java.
> Il contenuto seguente riporta fedelmente la parte di codice effettivamente
> ricevuta. Non costituisce un file Java completo.

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
        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(List.of("A", "B").listIterator());

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
        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(List.of("A", "B").listIterator());

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
        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(List.of("A", "B").iterator());

        wrapper.next();
        wrapper.next();

        assertEquals("B", wrapper.previous());
        assertEquals("A", wrapper.previous());
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
     * previous() all'inizio segnala che l'operazione non è supportata
     * per un semplice Iterator senza elementi precedentemente attraversati.
     */
    @Test
    void tllm007_previousAtBeginningThrowsUnsupportedOperationException() {
        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(List.of("A").iterator());

        assertThrows(
            UnsupportedOperationException.class,
            wrapper::previous
        );
    }

    /**
     * TLLM-008
     * add() e set() non sono supportati quando il wrapped object
     * è un semplice Iterator.
     */
    @Test
    void tllm008_addAndSetAreUnsupportedForPlainIterator() {
        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(List.of("A").iterator());

        assertThrows(
            UnsupportedOperationException.class,
            () -> wrapper.add("B")
        );

        assertThrows(
            UnsupportedOperationException.class,
            () -> wrapper.set("B")
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
```

## Stato della risposta

```text
Repair response : R1
Java output      : TRUNCATED
Usable as final canonical test file : NO
Further action   : R2 required
```

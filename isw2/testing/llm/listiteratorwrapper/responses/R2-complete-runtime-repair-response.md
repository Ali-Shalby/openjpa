# R2 – Complete Runtime Repair Response

Copilot ha restituito una tabella di riepilogo delle modifiche e il file Java corretto, ma il blocco di codice è stato troncato dall'interfaccia prima della fine.

## Modifiche dichiarate

| TLLM | Modifica effettuata | Motivo |
|---|---|---|
| TLLM-002 | Il setup usa `CountingIterator` come vero plain `Iterator`. | L’iteratore restituito da `List.of` implementa a runtime `ListIterator`, facendo selezionare accidentalmente il ramo delegato. Il ramo plain restituisce direttamente stato e indici dal cursore interno. |
| TLLM-003 | Il setup usa `CountingIterator`; oracle e asserzioni restano invariati. | Consente di verificare attraversamento, esaurimento e indici sul ramo plain originariamente previsto. |
| TLLM-004 | Il setup usa `CountingIterator`; tutte le asserzioni significative originali sono mantenute. | La cache, la navigazione backward e gli indici sono implementati dal wrapper quando l’oggetto ricevuto non è un `ListIterator`. |
| TLLM-007 | Il setup usa `CountingIterator`; resta attesa `NoSuchElementException`. | Nel ramo plain, `previous()` con `currentIndex == 0` solleva esplicitamente `NoSuchElementException`. |
| TLLM-008 | Il setup usa `CountingIterator`; sono mantenuti tipo e messaggio attesi. | Sul ramo plain, `add()` e `set()` generano direttamente `UnsupportedOperationException` con il messaggio costante garantito dal production code. |
| TLLM-009 | Il messaggio atteso usa l’indice `-1`. | Prima dell’attraversamento, `currentIndex` e `wrappedIteratorIndex` valgono zero; il calcolo decrementa l’indice di rimozione a `-1`. |

## File Java restituito

> **Nota:** il blocco seguente è riportato così come restituito da Copilot. L'output è stato troncato dall'interfaccia durante `TLLM-007`, quindi non costituisce il file Java completo.

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
            new ListIteratorWrapper
```

## Verifica finale dichiarata da Copilot

- Numero di metodi annotati con `@Test`: **12**
- Identificativi presenti: **TLLM-001, TLLM-002, TLLM-003, TLLM-004, TLLM-005, TLLM-006, TLLM-007, TLLM-008, TLLM-009, TLLM-010, TLLM-011, TLLM-012**
- Test aggiunti o rimossi: **nessuno**

## Stato della risposta

```text
Repair response : R2
Decision summary : PRESENT
Java output      : TRUNCATED
Usable as final canonical test file : NO
Further action   : request code-only completion
```

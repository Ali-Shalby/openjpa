# Microsoft Copilot Web — ListIteratorWrapper C4 / T_LLM

> Trascrizione della risposta RAW di Copilot Web.
> È stata normalizzata soltanto la formattazione Markdown; contenuto, scenari e codice non sono stati modificati.

Il production context mostra due modalità operative nettamente distinte: wrapping di un normale `Iterator`, con cache interna e navigazione simulata, oppure wrapping di un `ListIterator`, al quale le operazioni vengono delegate direttamente. I test seguenti coprono entrambe senza utilizzare informazioni esterne, suite esistenti o risultati di coverage/mutation testing.

## 1. Analisi sintetica dal punto di vista del testing

Le principali responsabilità osservabili di `ListIteratorWrapper<E>` sono:

- **Validazione di costruzione**: rifiuta un iteratore `null`.
- **Adattamento di un `Iterator` a `ListIterator`**:
  - memorizza in una cache gli elementi consumati;
  - permette di tornare indietro sugli elementi già visitati;
  - permette di rileggere gli elementi dalla cache senza riavanzare l’iteratore sottostante.
- **Gestione del cursore**:
  - espone `hasNext()`, `hasPrevious()`;
  - espone `nextIndex()` e `previousIndex()`;
  - mantiene coerenti gli indici durante navigazione, reset e rimozione.
- **Reset**:
  - per un normale `Iterator`, riporta il cursore all’inizio della cache;
  - per un `ListIterator`, arretra il cursore sottostante fino all’inizio.
- **Operazioni opzionali**:
  - `add()` e `set()` non sono supportate per un normale `Iterator`;
  - sono delegate quando l’oggetto sottostante è un `ListIterator`;
  - `remove()` su un normale `Iterator` è consentita solo in specifici stati del cursore.
- **Propagazione delle condizioni terminali**:
  - `next()` oltre la fine;
  - `previous()` prima dell’inizio.
- **Delegazione trasparente a `ListIterator`**:
  - navigazione;
  - indici;
  - mutazioni;
  - reset.

Un punto particolarmente delicato è `remove()`: per un normale `Iterator`, la classe deve sincronizzare iteratore sottostante, cache, `currentIndex`, `wrappedIteratorIndex` e stato di rimozione.

---

## 2. Progettazione dei 12 test case

### TLLM-001 — Rifiuto di un iteratore nullo

- **Comportamento considerato**: validazione dell’argomento del costruttore.
- **Setup/input**: riferimento `Iterator<String>` uguale a `null`.
- **Azione**: costruzione di `ListIteratorWrapper`.
- **Risultato atteso**: viene sollevata `NullPointerException` con messaggio `"Iterator must not be null"`.

### TLLM-002 — Stato iniziale con iteratore non vuoto

- **Comportamento considerato**: stato iniziale del cursore.
- **Setup/input**: iteratore normale contenente `"A"`.
- **Azione**: interrogazione di disponibilità e indici.
- **Risultato atteso**:
  - `hasNext()` restituisce `true`;
  - `hasPrevious()` restituisce `false`;
  - `nextIndex()` restituisce `0`;
  - `previousIndex()` restituisce `-1`.

### TLLM-003 — Avanzamento e aggiornamento degli indici

- **Comportamento considerato**: consumo sequenziale di elementi da un normale `Iterator`.
- **Setup/input**: iteratore contenente `"A"` e `"B"`.
- **Azione**: chiamata a `next()` due volte, controllando stato e indici.
- **Risultato atteso**:
  - vengono restituiti `"A"` e `"B"` nell’ordine;
  - dopo il primo elemento gli indici sono `1` e `0`;
  - dopo il secondo elemento non esiste un successivo e gli indici sono `2` e `1`.

### TLLM-004 — Navigazione precedente prima dell’inizio

- **Comportamento considerato**: limite inferiore della navigazione.
- **Setup/input**: wrapper appena creato su un normale iteratore.
- **Azione**: chiamata a `previous()`.
- **Risultato atteso**: viene sollevata `NoSuchElementException`.

### TLLM-005 — Rilettura di un elemento dalla cache

- **Comportamento considerato**: navigazione indietro e successiva rilettura dalla cache.
- **Setup/input**: iteratore contenente `"A"` e `"B"`; entrambi gli elementi vengono inizialmente letti.
- **Azione**: chiamata a `previous()`, poi a `next()`.
- **Risultato atteso**:
  - `previous()` restituisce `"B"`;
  - durante il posizionamento nella cache `hasNext()` resta `true`;
  - `next()` restituisce nuovamente `"B"`;
  - il wrapper torna alla fine della sequenza.

### TLLM-006 — Reset di un normale Iterator

- **Comportamento considerato**: riposizionamento logico all’inizio della cache.
- **Setup/input**: iteratore contenente `"A"` e `"B"`; entrambi gli elementi vengono letti.
- **Azione**: chiamata a `reset()`, seguita da nuove letture.
- **Risultato atteso**:
  - gli indici tornano a `0` e `-1`;
  - non esiste un elemento precedente;
  - `"A"` e `"B"` possono essere riletti dalla cache nell’ordine originale.

### TLLM-007 — Avanzamento oltre la fine

- **Comportamento considerato**: limite superiore della navigazione.
- **Setup/input**: iteratore contenente il solo elemento `"A"`.
- **Azione**: consumo di `"A"` e ulteriore chiamata a `next()`.
- **Risultato atteso**: la seconda chiamata a `next()` solleva `NoSuchElementException`.

### TLLM-008 — `add` e `set` non supportati per un Iterator normale

- **Comportamento considerato**: operazioni opzionali non disponibili.
- **Setup/input**: wrapper su un normale iteratore.
- **Azione**: chiamata a `add("B")` e a `set("B")`.
- **Risultato atteso**: entrambe sollevano `UnsupportedOperationException` con il messaggio previsto dalla classe.

### TLLM-009 — Rimozione prima di una navigazione valida

- **Comportamento considerato**: controllo dello stato richiesto da `remove()`.
- **Setup/input**: wrapper appena creato su un normale iteratore.
- **Azione**: chiamata immediata a `remove()`.
- **Risultato atteso**: viene sollevata `IllegalStateException` con messaggio `"Cannot remove element at index -1."`.

### TLLM-010 — Rimozione dopo `next()`

- **Comportamento considerato**: rimozione valida dell’ultimo elemento restituito.
- **Setup/input**: lista mutabile `["A", "B", "C"]` e relativo iteratore.
- **Azione**: lettura di `"A"`, lettura di `"B"`, quindi `remove()`.
- **Risultato atteso**:
  - la lista sottostante diventa `["A", "C"]`;
  - gli indici vengono riallineati;
  - la navigazione successiva restituisce `"C"`.

### TLLM-011 — Rimozione non valida dopo arretramento multiplo

- **Comportamento considerato**: divieto di rimuovere quando il cursore è troppo distante dalla posizione dell’iteratore sottostante.
- **Setup/input**: lista `["A", "B", "C"]`; tutti gli elementi vengono letti.
- **Azione**: due chiamate a `previous()`, seguite da `remove()`.
- **Risultato atteso**:
  - `remove()` solleva `IllegalStateException`;
  - la lista sottostante resta invariata.

### TLLM-012 — Delegazione completa a un ListIterator

- **Comportamento considerato**: delegazione di navigazione, mutazioni e reset.
- **Setup/input**: `ListIterator` della lista mutabile `[1, 2]`.
- **Azione**:
  - `next()`;
  - `set(10)`;
  - `add(15)`;
  - navigazione su `2`;
  - `remove()`;
  - `reset()`.
- **Risultato atteso**:
  - le mutazioni producono la lista `[10, 15]`;
  - il reset riporta il cursore all’inizio;
  - indici, disponibilità e successive letture riflettono lo stato del `ListIterator` sottostante.

---

## 3. Implementazione Java 21 con JUnit Jupiter

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
            new ListIteratorWrapper<>(List.of("A").iterator());

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
            new ListIteratorWrapper<>(List.of("A", "B").iterator());

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
            new ListIteratorWrapper<>(List.of("A").iterator());

        assertThrows(NoSuchElementException.class, wrapper::previous);
    }

    /**
     * TLLM-005
     * Verifica che un elemento possa essere riletto dalla cache interna.
     */
    @Test
    void tllm005_previousThenNextReplaysCachedElement() {
        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(List.of("A", "B").iterator());

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
            new ListIteratorWrapper<>(List.of("A", "B").iterator());

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
            new ListIteratorWrapper<>(List.of("A").iterator());

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
            new ListIteratorWrapper<>(List.of("A").iterator());

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

## Verifica di consistenza

- Test case progettati: **12**.
- Metodi annotati con `@Test`: **12**.
- Identificativi implementati: da **TLLM-001** a **TLLM-012**, senza salti o duplicazioni.
- Nessun test aggiunto o sostituito tra progettazione e implementazione.
- Nessun uso di Mockito, test nativi, suite esterne o dati di coverage/mutation testing.
- Tutte le asserzioni derivano esclusivamente dal comportamento osservabile nel production context fornito.

## File production mancanti realmente necessari

- `ResettableListIterator.java`, o il corrispondente artefatto già compilato presente nella baseline, è necessario per compilare da sorgente `ListIteratorWrapper.java`, poiché la classe dichiara `implements ResettableListIterator<E>`. La firma completa di tale interfaccia non è inclusa nel production context fornito.

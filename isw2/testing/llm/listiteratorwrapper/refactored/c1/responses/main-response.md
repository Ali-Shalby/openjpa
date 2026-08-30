Di seguito propongo una suite black-box mirata ai comportamenti osservabili della classe, usando esclusivamente il production context contenuto nel file fornito. La progettazione copre sia il percorso basato su un semplice `Iterator`, con cache interna, sia il percorso di delega diretta quando l’oggetto decorato è un `ListIterator`. [\[ListIterat...apper.java | Txt\]](https://uniroma2-my.sharepoint.com/personal/ali_shalby_students_uniroma2_eu/Documents/File%20di%20Microsoft%20Copilot%20Chat/ListIteratorWrapper.java.txt)

## 1. Analisi sintetica dal punto di vista del testing

`ListIteratorWrapper<E>` converte un `Iterator<? extends E>` in un `ResettableListIterator<E>`.

Le responsabilità principali osservabili sono:

1. **Validazione del costruttore**
   - Rifiuta un iteratore `null` con `NullPointerException`.
2. **Adattamento di un semplice** **`Iterator`**
   - Memorizza in una cache interna gli elementi ottenuti dall’iteratore sottostante.
   - Consente di tornare indietro sugli elementi già visitati tramite `previous()`.
   - Consente di rileggere dalla cache gli elementi già attraversati senza richiedere nuovamente tali elementi all’iteratore sottostante.
   - Mantiene coerenti `hasNext()`, `hasPrevious()`, `nextIndex()` e `previousIndex()`.
3. **Reset**
   - Per un semplice `Iterator`, riporta il cursore logico all’inizio della cache.
   - Per un `ListIterator`, torna all’inizio invocando ripetutamente `previous()` sul delegato.
4. **Gestione di** **`remove()`** **con semplice** **`Iterator`**
   - La rimozione è consentita soltanto quando lo stato del cursore può essere coerentemente associato all’ultimo elemento restituito dall’iteratore sottostante.
   - Aggiorna sia la collezione sottostante sia la cache interna.
   - Rifiuta invocazioni illegali con `IllegalStateException`.
5. **Operazioni opzionali**
   - `add()` e `set()` non sono supportate quando il delegato è un semplice `Iterator`.
   - Quando il delegato implementa `ListIterator`, `add()`, `set()`, `remove()` e tutte le operazioni di navigazione sono delegate direttamente.
6. **Condizioni limite**
   - `previous()` all’inizio e `next()` oltre la fine producono `NoSuchElementException`.
   - Gli indici iniziali sono `nextIndex() == 0` e `previousIndex() == -1`. [\[ListIterat...apper.java | Txt\]](https://uniroma2-my.sharepoint.com/personal/ali_shalby_students_uniroma2_eu/Documents/File%20di%20Microsoft%20Copilot%20Chat/ListIteratorWrapper.java.txt)

---

## 2. Progettazione dei 12 test case

### TLLM-001: rifiuto di un iteratore nullo

- **Comportamento considerato:** validazione dell’argomento del costruttore.
- **Setup/input:** riferimento `Iterator<String>` nullo.
- **Azione:** costruzione di `ListIteratorWrapper`.
- **Risultato atteso:** viene sollevata `NullPointerException` con messaggio `"Iterator must not be null"`.

### TLLM-002: stato iniziale con semplice Iterator

- **Comportamento considerato:** stato iniziale e indici del cursore.
- **Setup/input:** iteratore sulla lista `["A"]`.
- **Azione:** interrogazione di `hasNext()`, `hasPrevious()`, `nextIndex()` e `previousIndex()`.
- **Risultato atteso:** rispettivamente `true`, `false`, `0` e `-1`.

### TLLM-003: avanzamento e aggiornamento degli indici

- **Comportamento considerato:** lettura sequenziale mediante `next()`.
- **Setup/input:** iteratore sulla lista `["A", "B"]`.
- **Azione:** lettura di `"A"` e successiva verifica dello stato.
- **Risultato atteso:** `next()` restituisce `"A"`; entrambi i versi di navigazione sono disponibili; gli indici diventano `1` e `0`.

### TLLM-004: navigazione all’indietro e rilettura dalla cache

- **Comportamento considerato:** uso della cache nella navigazione bidirezionale.
- **Setup/input:** iteratore sulla lista `["A", "B"]`, con entrambi gli elementi già letti.
- **Azione:** invocazione di `previous()` seguita da `next()`.
- **Risultato atteso:** entrambe le operazioni restituiscono `"B"` e il cursore torna dopo il secondo elemento.

### TLLM-005: reset di un semplice Iterator

- **Comportamento considerato:** riposizionamento logico all’inizio.
- **Setup/input:** iteratore sulla lista `["A", "B"]`, interamente attraversato.
- **Azione:** invocazione di `reset()` e nuova lettura.
- **Risultato atteso:** gli indici tornano a `0` e `-1`; `hasPrevious()` è `false`; `next()` restituisce nuovamente `"A"`.

### TLLM-006: eccezioni ai confini della sequenza

- **Comportamento considerato:** navigazione oltre i limiti.
- **Setup/input:** iteratore sulla lista `["A"]`.
- **Azione:** chiamata a `previous()` prima di ogni lettura; successivamente consumo di `"A"` e chiamata a `next()` oltre la fine.
- **Risultato atteso:** entrambe le operazioni fuori limite sollevano `NoSuchElementException`.

### TLLM-007: rimozione dopo next()

- **Comportamento considerato:** rimozione valida dell’ultimo elemento letto in avanti.
- **Setup/input:** lista mutabile `["A", "B"]` e relativo iteratore.
- **Azione:** `next()`, poi `remove()`, quindi una seconda `remove()`.
- **Risultato atteso:** `"A"` viene eliminato dalla lista; il cursore torna all’indice `0`; la chiamata ripetuta a `remove()` solleva `IllegalStateException`.

### TLLM-008: rimozione dopo previous() alla frontiera dell’iteratore sottostante

- **Comportamento considerato:** rimozione valida dopo un singolo passo indietro dalla posizione più avanzata.
- **Setup/input:** lista mutabile `["A", "B", "C"]`; vengono letti `"A"` e `"B"`.
- **Azione:** `previous()` restituisce `"B"`, quindi viene chiamato `remove()`.
- **Risultato atteso:** `"B"` viene rimosso; la lista diventa `["A", "C"]`; il successivo `next()` restituisce `"C"`.

### TLLM-009: rimozione non valida dopo arretramento multiplo

- **Comportamento considerato:** protezione dalla rimozione quando il cursore della cache è troppo distante dal cursore dell’iteratore sottostante.
- **Setup/input:** lista `["A", "B", "C"]` interamente attraversata.
- **Azione:** due chiamate a `previous()`, seguite da `remove()`.
- **Risultato atteso:** `remove()` solleva `IllegalStateException` e la lista sottostante resta invariata.

### TLLM-010: add() e set() non supportati per semplice Iterator

- **Comportamento considerato:** operazioni opzionali non disponibili.
- **Setup/input:** semplice iteratore sulla lista `["A"]`.
- **Azione:** invocazione di `add("B")` e `set("X")`.
- **Risultato atteso:** entrambe sollevano `UnsupportedOperationException` con il messaggio previsto dalla classe.

### TLLM-011: navigazione e reset delegati a ListIterator

- **Comportamento considerato:** percorso di delega quando il decorato è un `ListIterator`.
- **Setup/input:** `ListIterator` sulla lista `["A", "B"]`.
- **Azione:** avanzamento di due posizioni, arretramento di una posizione, `reset()` e nuova lettura.
- **Risultato atteso:** valori, indici e disponibilità delle direzioni coincidono con quelli del `ListIterator`; dopo il reset, la nuova lettura restituisce `"A"`.

### TLLM-012: operazioni mutative delegate a ListIterator

- **Comportamento considerato:** delega di `set()`, `add()` e `remove()`.
- **Setup/input:** lista mutabile `["A", "B"]` e relativo `ListIterator`.
- **Azione:** lettura di `"A"`, sostituzione con `"A1"`, aggiunta di `"X"`, ritorno su `"X"` e rimozione.
- **Risultato atteso:** la lista finale è `["A1", "B"]`; dopo il reset, il wrapper restituisce `"A1"`.

I dodici scenari derivano direttamente dai rami e dagli stati osservabili presenti in [ListIteratorWrapper.java](https://uniroma2-my.sharepoint.com/personal/ali_shalby_students_uniroma2_eu/Documents/File%20di%20Microsoft%20Copilot%20Chat/ListIteratorWrapper.java.txt).

```java
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
            new ListIteratorWrapper<>(List.of("A").iterator());

        assertTrue(wrapper.hasNext());
        assertFalse(wrapper.hasPrevious());
        assertEquals(0, wrapper.nextIndex());
        assertEquals(-1, wrapper.previousIndex());
    }

    @Test
    @DisplayName("TLLM-003 - next avanza il cursore e aggiorna gli indici")
    void tllm003NextAdvancesCursorAndIndexes() {
        ListIteratorWrapper<String> wrapper =
            new ListIteratorWrapper<>(List.of("A", "B").iterator());

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
            new ListIteratorWrapper<>(List.of("A", "B").iterator());

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
            new ListIteratorWrapper<>(List.of("A", "B").iterator());

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
            new ListIteratorWrapper<>(List.of("A").iterator());

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
        assertEquals("B", wrapper.next());

        IllegalStateException exception =
            assertThrows(IllegalStateException.class, wrapper::remove);

        /*
         * La remove immediatamente precedente è valida, ma dopo la successiva
         * next() la rimozione è nuovamente valida. Per verificare esattamente
         * il divieto di remove ripetuta, occorre farlo subito dopo la prima
         * remove. Tale controllo viene eseguito sotto mediante un secondo
         * wrapper nello stesso scenario.
         */
        assertEquals("Cannot remove element at index 0.", exception.getMessage());

        List<String> secondSource = new ArrayList<>(List.of("A", "B"));
        ListIteratorWrapper<String> secondWrapper =
            new ListIteratorWrapper<>(secondSource.iterator());

        secondWrapper.next();
        secondWrapper.remove();

        IllegalStateException repeatedRemove =
            assertThrows(IllegalStateException.class, secondWrapper::remove);

        assertEquals(
            "Cannot remove element at index -1.",
            repeatedRemove.getMessage()
        );
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
            new ListIteratorWrapper<>(List.of("A").iterator());

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
```

## Correzione necessaria in TLLM-007

Nel codice sopra, la prima parte di `TLLM-007` contiene una contraddizione: dopo aver eseguito una nuova `next()` su `"B"`, `remove()` è valida e non deve produrre `IllegalStateException`. Per mantenere il test esattamente aderente allo scenario progettato, il metodo corretto è il seguente:

```java
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
```

Questa seconda versione deve sostituire integralmente il metodo `tllm007RemoveAfterNextUpdatesListAndCursor()` nella classe precedente.

## File production mancanti

Nessun file production aggiuntivo è necessario per determinare le firme usate direttamente dai test. La compilazione presuppone che `ResettableListIterator`, già richiesto dalla classe production fornita, sia presente nelle dipendenze production del progetto Apache OpenJPA 4.1.1.

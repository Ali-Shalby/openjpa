# Microsoft Copilot Web — ListIteratorWrapper C3 / T_LLM

> Trascrizione della risposta RAW di Copilot Web.
> È stata normalizzata soltanto la formattazione Markdown; contenuto, scenari e codice non sono stati modificati.

Di seguito propongo una suite black-box mirata ai comportamenti pubblicamente osservabili della classe, usando esclusivamente il production context contenuto in `ListIteratorWrapper.java`. La progettazione e l’implementazione mantengono una corrispondenza esatta di 12 casi, da `TLLM-001` a `TLLM-012`.

## 1. Analisi sintetica dal punto di vista del testing

`ListIteratorWrapper<E>` adatta un `Iterator<? extends E>` all’interfaccia `ResettableListIterator<E>` e presenta due modalità operative distinte.

### Wrapper di un `Iterator` semplice

Quando l’oggetto decorato è un semplice `Iterator`:

- gli elementi letti vengono memorizzati in una lista interna;
- il wrapper consente la navigazione all’indietro sugli elementi già consumati;
- gli elementi memorizzati possono essere ripercorsi senza richiederli nuovamente all’iteratore sottostante;
- `nextIndex()` e `previousIndex()` sono calcolati rispetto alla posizione logica nella cache;
- `reset()` riporta la posizione logica all’inizio, senza ricreare o riavvolgere l’iteratore sottostante;
- `add()` e `set()` non sono supportati;
- `remove()` viene delegato solo quando l’elemento logicamente selezionato coincide con quello che l’iteratore sottostante può ancora rimuovere in sicurezza.

### Wrapper di un `ListIterator`

Quando l’oggetto decorato implementa già `ListIterator`:

- la navigazione viene delegata direttamente;
- gli indici vengono delegati direttamente;
- `add()`, `set()` e `remove()` vengono delegati;
- `reset()` richiama ripetutamente `previous()` fino a raggiungere l’inizio del `ListIterator`.

### Aree principali da verificare

Dal punto di vista del testing, le aree più rilevanti sono:

1. validazione dell’argomento del costruttore;
2. stato iniziale e limiti di navigazione;
3. lettura in avanti e aggiornamento degli indici;
4. caching e navigazione bidirezionale;
5. reset e riutilizzo degli elementi memorizzati;
6. gestione dell’esaurimento;
7. operazioni opzionali non supportate per un `Iterator` semplice;
8. condizioni valide e non valide di `remove()`;
9. delega completa quando il decorato è un `ListIterator`.

---

## 2. Progettazione dei 12 test case

### TLLM-001: rifiuto di un iteratore nullo

- **Comportamento considerato:** validazione dell’argomento del costruttore.
- **Setup/input:** riferimento `Iterator<String>` nullo.
- **Azione:** costruzione di `ListIteratorWrapper`.
- **Risultato atteso:** `NullPointerException` con messaggio `"Iterator must not be null"`.

### TLLM-002: stato iniziale di un wrapper su iteratore semplice

- **Comportamento considerato:** posizione iniziale, indici e assenza di elementi precedenti.
- **Setup/input:** iteratore semplice sulla lista `["a", "b"]`.
- **Azione:** interrogazione di `hasNext()`, `hasPrevious()`, `nextIndex()`, `previousIndex()` e chiamata a `previous()`.
- **Risultato atteso:** esiste un elemento successivo, non esiste un precedente, gli indici sono rispettivamente `0` e `-1`; `previous()` genera `NoSuchElementException`.

### TLLM-003: iterazione in avanti e aggiornamento degli indici

- **Comportamento considerato:** consumo progressivo dell’iteratore semplice.
- **Setup/input:** iteratore semplice sulla lista `["a", "b"]`.
- **Azione:** chiamate successive a `next()`, osservando indici e disponibilità.
- **Risultato atteso:** vengono restituiti `"a"` e `"b"` nell’ordine corretto; gli indici avanzano coerentemente; al termine `hasNext()` è `false`.

### TLLM-004: navigazione indietro e replay dalla cache

- **Comportamento considerato:** recupero degli elementi memorizzati dopo uno spostamento all’indietro.
- **Setup/input:** iteratore semplice sulla lista `["a", "b", "c"]`, con `"a"` e `"b"` già consumati.
- **Azione:** chiamata a `previous()`, quindi `next()` due volte.
- **Risultato atteso:** `previous()` restituisce `"b"`; il primo `next()` restituisce nuovamente `"b"` dalla cache e il successivo restituisce `"c"`.

### TLLM-005: reset e nuova percorrenza degli elementi memorizzati

- **Comportamento considerato:** riposizionamento logico all’inizio.
- **Setup/input:** iteratore semplice sulla lista `["a", "b", "c"]`, con i primi due elementi consumati.
- **Azione:** chiamata a `reset()`, seguita da tre `next()`.
- **Risultato atteso:** gli indici tornano allo stato iniziale e la sequenza osservata è nuovamente `"a"`, `"b"`, `"c"`.

### TLLM-006: richiesta di un elemento oltre la fine

- **Comportamento considerato:** propagazione della condizione di esaurimento.
- **Setup/input:** iteratore semplice sulla lista `["a"]`, completamente consumato.
- **Azione:** ulteriore chiamata a `next()`.
- **Risultato atteso:** `NoSuchElementException`.

### TLLM-007: `add()` non supportato per un iteratore semplice

- **Comportamento considerato:** indisponibilità dell’operazione opzionale `add`.
- **Setup/input:** iteratore semplice su una lista.
- **Azione:** chiamata a `add("x")`.
- **Risultato atteso:** `UnsupportedOperationException` con il messaggio definito dalla classe.

### TLLM-008: `set()` non supportato per un iteratore semplice

- **Comportamento considerato:** indisponibilità dell’operazione opzionale `set`.
- **Setup/input:** iteratore semplice su una lista, dopo una chiamata a `next()`.
- **Azione:** chiamata a `set("x")`.
- **Risultato atteso:** `UnsupportedOperationException` con il messaggio definito dalla classe; la lista sottostante resta invariata.

### TLLM-009: rimozione valida dopo `next()` su iteratore semplice

- **Comportamento considerato:** rimozione dell’ultimo elemento consumato dall’iteratore sottostante.
- **Setup/input:** lista mutabile `["a", "b", "c"]` e relativo iteratore semplice.
- **Azione:** `next()` seguito da `remove()`.
- **Risultato atteso:** `"a"` viene eliminato; la lista diventa `["b", "c"]`; la posizione logica viene aggiornata e il successivo `next()` restituisce `"b"`.

### TLLM-010: rimozione senza una precedente operazione valida

- **Comportamento considerato:** controllo dello stato iniziale di `remove()`.
- **Setup/input:** iteratore semplice su `["a"]`.
- **Azione:** chiamata immediata a `remove()`.
- **Risultato atteso:** `IllegalStateException` con messaggio `"Cannot remove element at index -1."`; la lista resta invariata.

### TLLM-011: rimozione non sicura dopo arretramento multiplo

- **Comportamento considerato:** divieto di delegare `remove()` quando la posizione logica è troppo distante dalla posizione dell’iteratore sottostante.
- **Setup/input:** iteratore semplice su `["a", "b", "c"]`, interamente consumato.
- **Azione:** due chiamate a `previous()`, seguite da `remove()`.
- **Risultato atteso:** `IllegalStateException` con messaggio `"Cannot remove element at index 1."`; la lista resta invariata.

### TLLM-012: delega delle operazioni a un vero `ListIterator`

- **Comportamento considerato:** modalità di delega, comprese `set`, `add`, `remove`, navigazione e `reset`.
- **Setup/input:** `ListIterator` di una lista mutabile `["a", "b"]`.
- **Azione:** lettura, sostituzione, inserimento, navigazione indietro, rimozione e reset.
- **Risultato atteso:** le modifiche sono applicate alla lista sottostante; dopo il reset il wrapper è nuovamente posizionato all’inizio.

---

## 3. Implementazione Java 21 con JUnit Jupiter

```java
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
```

## Nota sui file production mancanti

**Nessun file production aggiuntivo è necessario per definire o compilare questa classe di test all’interno della baseline indicata**, a condizione che `ListIteratorWrapper` e la sua dipendenza production `ResettableListIterator` siano già compilate o disponibili nel normale classpath del progetto. I test usano esclusivamente l’API pubblica osservabile di `ListIteratorWrapper` e API standard Java 21/JUnit Jupiter.

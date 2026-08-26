# ListIteratorWrapper - Testing Black-Box

## 1. Target

- **Classe:** `org.apache.openjpa.lib.util.collections.ListIteratorWrapper`
- **Baseline:** Apache OpenJPA `4.1.1`
- **Suite:** `T_BB`
- **Tecnica:** Category Partition
- **Cardinalità congelata:** `12 test`

## 2. Obiettivo metodologico

La suite verifica esclusivamente il comportamento pubblico documentato di
`ListIteratorWrapper`.

La progettazione è stata effettuata prima dell'osservazione di risultati
JaCoCo o PIT e senza utilizzare la struttura interna dell'implementazione.

Le fonti utilizzate per gli oracle sono:

1. Javadoc OpenJPA 4.1.1 di `ListIteratorWrapper`;
2. contratto pubblico di `Iterator`;
3. contratto pubblico di `ListIterator`;
4. contratto pubblico di `ResettableListIterator`.

Non vengono utilizzati:

- test nativi OpenJPA;
- branch dell'implementazione;
- campi privati;
- JaCoCo;
- PIT;
- mutanti sopravvissuti.

## 3. Preflight

Il testing harness utilizza:

`org.apache.openjpa:openjpa-lib:4.1.1`

La dipendenza viene risolta transitivamente tramite:

`org.apache.openjpa:openjpa-kernel:4.1.1`

Il JAR canonico è stato verificato localmente e l'API pubblica è stata
confermata mediante `javap`.

Nessuna modifica al codice production è necessaria.

## 4. Category Partition

### C1 - Input al costruttore

- iteratore valido;
- `null`.

### C2 - Tipo dell'iteratore

- plain `Iterator`;
- `ListIterator`.

### C3 - Cardinalità

- vuota: `[]`;
- singolo elemento: `["A"]`;
- più elementi: `["A","B","C"]`.

### C4 - Navigazione

- forward;
- backward;
- alternata forward/backward.

### C5 - Boundary

- elemento disponibile;
- `next()` oltre la fine;
- `previous()` prima dell'inizio.

### C6 - Reset

- dopo avanzamento parziale;
- dopo consumo completo.

### C7 - Operazioni opzionali con plain Iterator

- `add(E)`;
- `set(E)`.

Il prodotto cartesiano delle categorie non viene utilizzato.
Sono selezionati solamente frame rappresentativi di classi di equivalenza
distinte.

## 5. Inventory T_BB congelato

| ID | Scenario | Oracle |
|---|---|---|
| TBB-001 | costruttore con `null` | `NullPointerException` |
| TBB-002 | plain Iterator vuoto | no next/previous, indici `0/-1` |
| TBB-003 | `next()` su iteratore vuoto | `NoSuchElementException` |
| TBB-004 | `previous()` in posizione iniziale | `NoSuchElementException` |
| TBB-005 | un elemento, forward e backward | stesso elemento e indici coerenti |
| TBB-006 | navigazione forward completa | `A,B,C` |
| TBB-007 | navigazione backward completa | `C,B,A` |
| TBB-008 | `next → previous → next` | `A,A,A` |
| TBB-009 | reset dopo avanzamento parziale | ritorno alla posizione iniziale |
| TBB-010 | reset dopo consumo completo | seconda iterazione `A,B,C` |
| TBB-011 | `add()` su plain Iterator | `UnsupportedOperationException` |
| TBB-012 | `set()` su plain Iterator | `UnsupportedOperationException` |

## 6. Condizioni a contorno

Sono esplicitamente rappresentate:

- input `null`;
- sequenza vuota;
- sequenza con un solo elemento;
- richiesta dell'elemento successivo oltre la fine;
- richiesta dell'elemento precedente prima dell'inizio;
- reset dopo avanzamento parziale;
- reset dopo esaurimento completo.

## 7. Oracle-feasibility review

Durante la progettazione iniziale era stato considerato un ulteriore candidato
relativo al reset di un `ListIterator` creato da una posizione non iniziale.

Il candidato prevedeva la creazione del wrapped `ListIterator` con cursore
iniziale maggiore di zero e la verifica del ritorno, dopo `reset()`, alla
medesima posizione.

La formulazione della documentazione pubblica non consente tuttavia di
stabilire un oracle sufficientemente univoco per tale scenario senza ricorrere
all'implementazione o a conoscenza aggiuntiva del comportamento concreto.

Il candidato è stato quindi escluso dalla suite canonica prima della
misurazione della coverage e prima del mutation testing.

La decisione non modifica gli altri frame della Category Partition.

## 8. Esclusione di remove()

`remove()` non viene utilizzato nella suite T_BB iniziale poiché la
documentazione OpenJPA 4.1.1 contiene indicazioni non completamente coerenti
sulla sua semantica.

Non viene utilizzata l'implementazione production per risolvere tale
ambiguità, in modo da preservare la natura specification-based della suite.

## 9. Freeze

Stato:

`CATEGORY PARTITION FROZEN`

Cardinalità:

`TBB-001 ... TBB-012 = 12 test`

Prima del freeze:

- nessun risultato JaCoCo osservato;
- nessun risultato PIT osservato;
- nessun test T_CF progettato;
- nessun test T_MT progettato.

Dopo l'implementazione della suite, gli eventuali risultati strutturali
potranno essere utilizzati esclusivamente per la fase T_CF.

Gli eventuali survivor PIT potranno essere utilizzati esclusivamente per
la fase T_MT.

## 10. Risultati

Da compilare dopo l'esecuzione della suite.

===== FINE =====

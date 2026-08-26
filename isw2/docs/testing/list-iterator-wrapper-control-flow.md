# ListIteratorWrapper – suite manuale control-flow `T_CF`

## Scopo

Questo documento descrive la fase di **adequacy improvement manuale guidata dalla
coverage** per:

```text
org.apache.openjpa.lib.util.collections.ListIteratorWrapper
```

Baseline production:

```text
Apache OpenJPA 4.1.1
Tag Git: 4.1.1
```

Stato:

```text
T_BB             : FROZEN, 12 test
T_BB outcome     : 12 PASS, 0 FAIL
T_CF additions   : FROZEN, 5 test
Suite cumulativa : 17 test
Outcome cumulativo: 17 PASS, 0 FAIL
LINE finale      : 98.61%
BRANCH finale    : 87.50%
METHOD finale    : 100.00%
T_CF STATUS      : FROZEN
```

`T_CF` non sostituisce e non modifica retroattivamente `T_BB`. La suite
black-box iniziale resta quella derivata tramite Category Partition; i test
control-flow sono aggiunte manuali successive, selezionate solo dopo aver
misurato l'adeguatezza della suite `T_BB` congelata.

---

## 1. Posizione metodologica di T_CF

La sequenza adottata è:

```text
Category Partition
        ↓
freeze T_BB
        ↓
full execution T_BB
        ↓
Line / Branch / Method Coverage baseline
        ↓
analisi dei gap strutturali
        ↓
manual source / reachability analysis
        ↓
selezione di scenari control-flow distinti
        ↓
implementazione T_CF
        ↓
standalone run T_CF
        ↓
cumulative coverage measurement T_BB + T_CF
        ↓
stopping rule
        ↓
freeze T_CF
```

La coverage non viene utilizzata per modificare gli oracle black-box già
congelati.

La suite `T_CF` è invece intenzionalmente white-box: dopo il freeze di `T_BB`
è consentito osservare il sorgente e il controllo di flusso della classe per
selezionare percorsi strutturali non sufficientemente esercitati.

---

## 2. Scope e metriche di adeguatezza

L'esperimento è class-focused. La metrica primaria viene calcolata sulla sola
classe selezionata:

```text
org.apache.openjpa.lib.util.collections.ListIteratorWrapper
```

Metriche:

```text
LINE Coverage
BRANCH Coverage
METHOD Coverage
```

Strumento:

```text
JaCoCo 0.8.15
```

La misurazione utilizza l'agent JaCoCo durante l'esecuzione dei test e genera
il report mediante JaCoCo CLI sul bytecode production esatto estratto da:

```text
org.apache.openjpa:openjpa-lib:4.1.1
```

L'agent viene limitato a:

```text
org.apache.openjpa.lib.util.collections.ListIteratorWrapper*
```

In questo modo il denominatore della coverage coincide con la classe production
oggetto dell'esperimento e non con l'intero modulo OpenJPA.

---

## 3. Baseline T_BB prima di T_CF

La suite black-box era già congelata prima dell'analisi control-flow:

```text
Tests   : 12
PASS    : 12
FAIL    : 0
Errors  : 0
Skipped : 0
```

Baseline JaCoCo:

| Metrica | Covered | Missed | Totale | Coverage |
|---|---:|---:|---:|---:|
| Line | 38 | 34 | 72 | 52.78% |
| Branch | 18 | 22 | 40 | 45.00% |
| Method | 10 | 1 | 11 | 90.91% |

La baseline mostrava quindi un'adeguatezza black-box buona a livello di metodi,
ma ancora ampi gap a livello di linee e soprattutto di branch.

Artefatti di misurazione prodotti durante la fase:

```text
isw2/testing/target/jacoco-listiteratorwrapper-tbb/
└── report/
    ├── jacoco.xml
    ├── jacoco.csv
    └── html/
```

---

## 4. Criterio di selezione dei T_CF

Dopo il freeze della T_BB sono stati considerati i percorsi strutturali ancora
non sufficientemente esercitati.

La selezione ha privilegiato scenari che soddisfacessero contemporaneamente:

1. presenza di un gap reale di Line e/o Branch Coverage;
2. percorso strutturalmente distinto da quelli già coperti dalla T_BB;
3. raggiungibilità con input e strutture dati normali;
4. comportamento osservabile mediante oracle semplici;
5. limitata sovrapposizione tra i test aggiunti;
6. valore esplicativo sufficiente per giustificare il test come caso
   control-flow manuale.

Non viene adottato come obiettivo il raggiungimento artificiale del 100% di
Branch Coverage.

---

## 5. Inventory finale T_CF

Tutti i test control-flow sono contenuti in:

```text
isw2/testing/src/test/java/
it/uniroma2/isw2/openjpa/testing/listiteratorwrapper/cf/
ListIteratorWrapperControlFlowTest.java
```

Inventory:

| ID | Scenario | Obiettivo strutturale |
|---|---|---|
| TCF-001 | `ListIterator` – navigazione, indici e reset | ramo dedicato al wrapped `ListIterator` |
| TCF-002 | `ListIterator` – `add`, `set`, `remove` | delega delle operazioni opzionali |
| TCF-003 | plain `Iterator` – `remove()` subito dopo `next()` | percorso di rimozione immediata |
| TCF-004 | plain `Iterator` – `remove()` dopo un passo backward | percorso di rimozione con elemento già memorizzato |
| TCF-005 | plain `Iterator` – rimozione in stato non valido | percorso eccezionale `IllegalStateException` |

Cardinalità:

```text
T_CF additions = 5
```

---

## 6. TCF-001 – ListIterator navigation, indices and reset

### WHY

La T_BB congelata utilizza prevalentemente un plain `Iterator`. Dopo la
misurazione JaCoCo rimanevano quindi percorsi specifici del caso in cui
l'iteratore wrapped implementa direttamente `ListIterator`.

Il primo scenario T_CF esercita in modo coerente il percorso dedicato in:

```text
hasNext()
hasPrevious()
next()
previous()
nextIndex()
previousIndex()
reset()
```

### HOW

Il test costruisce:

```text
[A, B, C]
↓
values.listIterator()
↓
ListIteratorWrapper
```

e verifica:

- disponibilità forward/backward;
- indici iniziali;
- navigazione forward;
- navigazione backward;
- coerenza degli indici;
- reset dalla posizione iniziale documentata.

Il caso usa un `ListIterator` creato all'indice `0`, evitando l'ambiguità
documentale precedentemente individuata per iteratore creato da posizione
non iniziale.

### RESULT

```text
TCF-001 : PASS
```

Il test viene mantenuto nella suite congelata.

---

## 7. TCF-002 – Optional operations delegated to ListIterator

### WHY

Il controllo di flusso presenta percorsi distinti per le operazioni opzionali
quando l'iteratore wrapped è già un `ListIterator`.

La T_BB verificava `add()` e `set()` sul plain `Iterator`, dove il contratto
porta a `UnsupportedOperationException`; questo non esercitava il ramo
alternativo di delega.

### HOW

Il test utilizza un `ListIterator` modificabile e verifica in sequenza:

```text
add("B")
next()
set("X")
remove()
```

Gli oracle osservano direttamente il contenuto della lista sottostante dopo
ogni operazione significativa.

### RESULT

```text
TCF-002 : PASS
```

Il test copre un comportamento strutturalmente distinto rispetto agli
equivalenti casi black-box sul plain `Iterator`.

---

## 8. TCF-003 – Plain Iterator remove immediately after next

### WHY

La baseline T_BB lasciava scoperto l'unico metodo non ancora esercitato
completamente:

```text
remove()
```

Il primo scenario di rimozione selezionato rappresenta il percorso più diretto:
un elemento viene ottenuto mediante `next()` e immediatamente rimosso.

### HOW

Sequenza:

```text
[A, B, C]
next()   -> A
remove()
```

Oracle:

```text
lista risultante = [B, C]
next()            = B
```

### RESULT

```text
TCF-003 : PASS
```

Questo scenario contribuisce anche al passaggio da 10/11 a 11/11 metodi
coperti nella suite cumulativa.

---

## 9. TCF-004 – Plain Iterator remove after one backward step

### WHY

`ListIteratorWrapper` conserva gli elementi già attraversati per consentire
navigazione backward anche quando l'oggetto wrapped è soltanto un `Iterator`.

Questo introduce un percorso di rimozione distinto dal caso immediatamente
successivo a `next()`.

### HOW

Sequenza:

```text
next()     -> A
next()     -> B
next()     -> C
previous() -> C
remove()
```

Oracle:

```text
lista risultante = [A, B]
hasNext()         = false
```

### RESULT

```text
TCF-004 : PASS
```

Il caso esercita il percorso di rimozione relativo a un elemento già
attraversato e reso nuovamente corrente tramite navigazione backward.

---

## 10. TCF-005 – Invalid remove state

### WHY

Dopo aver percorso più elementi e aver effettuato più passi backward, esiste
un percorso di controllo distinto nel quale `remove()` non è più valido.

La suite T_CF deve rappresentare anche questo esito eccezionale, invece di
limitarsi ai soli percorsi di rimozione con successo.

### HOW

Sequenza:

```text
next()     -> A
next()     -> B
next()     -> C
previous() -> C
previous() -> B
remove()
```

Oracle:

```text
IllegalStateException
lista invariata = [A, B, C]
```

### RESULT

```text
TCF-005 : PASS
```

---

## 11. Esecuzione standalone T_CF

La suite control-flow è stata eseguita separatamente prima della misura
cumulativa:

```text
Tests run : 5
Failures  : 0
Errors    : 0
Skipped   : 0

BUILD SUCCESS
```

Quindi:

```text
T_CF standalone = 5/5 PASS
```

---

## 12. Coverage cumulativa T_BB + T_CF

Dopo l'aggiunta dei cinque T_CF è stata rieseguita la suite cumulativa:

```text
T_BB : 12
T_CF : 5
Total: 17
```

Risultato funzionale:

```text
17/17 PASS
```

Coverage finale:

| Metrica | T_BB | T_BB + T_CF | Delta |
|---|---:|---:|---:|
| Line | 52.78% (38/72) | 98.61% (71/72) | +45.83 pp |
| Branch | 45.00% (18/40) | 87.50% (35/40) | +42.50 pp |
| Method | 90.91% (10/11) | 100.00% (11/11) | +9.09 pp |

Incremento assoluto:

```text
Additional covered lines    : +33
Additional covered branches : +17
Additional covered methods  : +1
```

Artefatti prodotti:

```text
isw2/testing/target/jacoco-listiteratorwrapper-tbb-tcf/
└── report/
    ├── jacoco.xml
    ├── jacoco.csv
    └── html/
```

---

## 13. Stopping rule

Dopo T_CF rimangono:

```text
Missed lines    : 1 / 72
Missed branches : 5 / 40
Missed methods  : 0 / 11
```

La presenza di coverage residua non implica automaticamente la necessità di
aggiungere ulteriori test.

La stopping rule adottata è:

> T_CF deve coprire i principali percorsi strutturali distinti e
> legittimamente raggiungibili individuati dopo la baseline T_BB. Non vengono
> aggiunti micro-test esclusivamente per inseguire il 100% di coverage quando
> la suite ha già prodotto un incremento sostanziale e i percorsi principali
> sono rappresentati.

Con soli cinque test aggiuntivi la suite passa:

```text
LINE   : 52.78% -> 98.61%
BRANCH : 45.00% -> 87.50%
METHOD : 90.91% -> 100.00%
```

Decisione:

```text
TCF-006 planned            : NO
T_CF stopping rule reached : YES
T_CF STATUS                : FROZEN
```

---

## 14. Freeze finale T_CF

Composizione congelata:

```text
T_BB additions : 12
T_CF additions : 5
Manual tests   : 17

T_BB PASS      : 12/12
T_CF PASS      : 5/5
Cumulative PASS: 17/17
```

Test congelati:

```text
TCF-001
TCF-002
TCF-003
TCF-004
TCF-005
```

File:

```text
ListIteratorWrapperControlFlowTest.java
```

Stato:

```text
T_BB STATUS : FROZEN
T_CF STATUS : FROZEN
```

Da questo punto T_BB e T_CF non vengono modificati per inseguire coverage
residua.

---

## 15. Handoff alla mutation analysis

La fase successiva parte dalla suite manuale cumulativa congelata:

```text
T_BB + T_CF = 17 test verdi
```

La mutation analysis viene quindi eseguita sulla suite cumulativa, non sulla
sola baseline T_BB.

Sequenza:

```text
T_BB + T_CF frozen
        ↓
PIT mutation baseline
        ↓
survivor audit
        ↓
eventuale T_MT mutation-guided
```

I test `T_MT`, se introdotti, vengono documentati separatamente e non
modificano retroattivamente la motivazione dei cinque `T_CF`.

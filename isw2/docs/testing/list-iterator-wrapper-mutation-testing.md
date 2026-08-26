# ListIteratorWrapper – Mutation Testing e suite manuale `T_MT`

## Scopo

Questo documento descrive la fase di **mutation testing manuale guidata da PIT**
per la classe:

```text
org.apache.openjpa.lib.util.collections.ListIteratorWrapper
```

Baseline production:

```text
Apache OpenJPA 4.1.1
Artifact: org.apache.openjpa:openjpa-lib:4.1.1
```

La fase parte esclusivamente dopo il freeze delle suite manuali precedenti:

```text
T_BB : 12 test
T_CF : 5 test
```

e utilizza i mutanti sopravvissuti alla baseline `T_BB + T_CF` per progettare
una piccola suite `T_MT` mutation-guided.

Stato finale:

```text
T_BB additions      : 12
T_CF additions      : 5
T_MT additions      : 2
Manual suite        : 19 test
Cumulative outcome  : 19 PASS, 0 FAIL, 0 ERROR, 0 SKIPPED

PIT baseline        : 46 KILLED / 4 SURVIVED / 2 NO_COVERAGE
PIT post T_MT       : 50 KILLED / 2 SURVIVED / 0 NO_COVERAGE

Mutation Score      : 88.46% -> 96.15%
Test Strength       : 92.00% -> 96.15%

T_MT STATUS         : FROZEN
```

---

## 1. Posizione metodologica

La sequenza adottata è:

```text
T_BB frozen
    ↓
JaCoCo baseline
    ↓
T_CF frozen
    ↓
JaCoCo T_BB + T_CF
    ↓
PIT mutation baseline
    ↓
survivor / no-coverage audit
    ↓
T_MT mutation-guided
    ↓
cumulative functional run
    ↓
PIT post T_MT
    ↓
residual mutant audit
    ↓
stopping rule
    ↓
freeze T_MT
```

La suite `T_MT` non viene progettata a partire dalla coverage residua e non
modifica retroattivamente né `T_BB` né `T_CF`.

Il suo input metodologico è esclusivamente il risultato della mutation
baseline sulla suite cumulativa già congelata.

---

## 2. Suite utilizzata come mutation baseline

La baseline PIT utilizza:

```text
T_BB : 12
T_CF : 5
---------------
Total: 17 green tests
```

Esito funzionale prima di PIT:

```text
17/17 PASS
```

Coverage JaCoCo cumulativa prima della mutation analysis:

| Metrica | T_BB | T_BB + T_CF |
|---|---:|---:|
| LINE | 52.78% (38/72) | 98.61% (71/72) |
| BRANCH | 45.00% (18/40) | 87.50% (35/40) |
| METHOD | 90.91% (10/11) | 100.00% (11/11) |

Questi valori rappresentano la misura strutturale canonica prima della fase
mutation-guided.

---

## 3. Protocollo PIT

Configurazione adottata:

```text
PIT                     : 1.25.8
PIT JUnit 5 plugin      : 1.2.3
Mutators                : DEFAULTS
Threads                 : 1
Target class            : org.apache.openjpa.lib.util.collections.ListIteratorWrapper
Native OpenJPA tests    : NOT USED
```

La classe production viene estratta dal JAR:

```text
org.apache.openjpa:openjpa-lib:4.1.1
```

e staged nel testing harness affinché PIT muti esattamente il bytecode della
release selezionata.

Identità production bytecode:

```text
Class size : 4353 bytes
SHA-256    : C06F2D6F83082E8CC538069769BA1C2241678054E9C27B0510A77C0ADCE4B0F4
```

Lo stesso SHA-256 è stato verificato nella baseline e nella run post-T_MT.

---

## 4. Mutation baseline `T_BB + T_CF`

Risultato:

```text
TOTAL          : 52
KILLED         : 46
SURVIVED       : 4
NO_COVERAGE    : 2
TIMED_OUT      : 0
RUN_ERROR      : 0
MEMORY_ERROR   : 0

Mutation Score : 88.46%
Test Strength  : 92.00%
```

Formula utilizzata:

```text
Mutation Score = KILLED / TOTAL
              = 46 / 52
              = 88.46%

Test Strength  = KILLED / (KILLED + SURVIVED)
              = 46 / (46 + 4)
              = 92.00%
```

La baseline è considerata valida perché:

- la suite `T_BB + T_CF` era completamente verde;
- il target era limitato alla sola classe selezionata;
- il bytecode production era quello di OpenJPA 4.1.1;
- non sono stati usati test nativi OpenJPA;
- non sono presenti timeout, run error o memory error.

---

## 5. Audit dei mutanti problematici della baseline

La baseline presenta sei mutanti non uccisi:

```text
4 SURVIVED
2 NO_COVERAGE
```

Inventory:

| ID audit | Stato baseline | Metodo | Linea PIT | Mutator | Valutazione |
|---|---|---|---:|---|---|
| M-01 | SURVIVED | `hasNext()` | 111 | `RemoveConditionalMutator_EQUAL_ELSE` | killable |
| M-02 | NO_COVERAGE | `hasNext()` | 111 | `RemoveConditionalMutator_EQUAL_ELSE` | da verificare |
| M-03 | NO_COVERAGE | `hasNext()` | 114 | `BooleanFalseReturnValsMutator` | killable |
| M-04 | SURVIVED | `hasPrevious()` | 128 | `RemoveConditionalMutator_ORDER_ELSE` | killable |
| M-05 | SURVIVED | `next()` | 139 | `RemoveConditionalMutator_EQUAL_ELSE` | candidato equivalente |
| M-06 | SURVIVED | `remove()` | 220 | `RemoveConditionalMutator_ORDER_ELSE` | killable |

La progettazione T_MT si concentra sui mutanti con comportamento discriminante
raggiungibile tramite API pubblica.

---

## 6. Strategia di progettazione T_MT

Non viene creato un test per ogni mutante.

I mutanti vengono raggruppati per stato comportamentale e percorso necessario
a distinguerli dal programma originale.

Sono stati selezionati due soli test:

```text
TMT-001
TMT-002
```

con l'obiettivo di massimizzare la capacità discriminante mantenendo la suite
piccola e spiegabile.

File:

```text
isw2/testing/src/test/java/
it/uniroma2/isw2/openjpa/testing/listiteratorwrapper/mt/
ListIteratorWrapperMutationTest.java
```

---

## 7. TMT-001 – Navigation predicates across cached and frontier states

### WHY

Tre gap della baseline erano collegati alla navigazione di un wrapper basato
su plain `Iterator`:

```text
M-01 : SURVIVED    hasNext()
M-03 : NO_COVERAGE hasNext()
M-04 : SURVIVED    hasPrevious()
```

La suite precedente non osservava in una singola sequenza tutti gli stati
necessari a distinguere:

- presenza di elementi precedenti;
- presenza di elementi memorizzati nella cache del wrapper;
- raggiungimento del frontier dell'Iterator sottostante;
- esaurimento effettivo dell'Iterator.

### HOW

Sequenza:

```text
[A, B]

next()     -> A
hasPrevious() -> true

next()     -> B
previous() -> B

hasNext()  -> true
next()     -> B

hasNext()  -> false
```

Il test attraversa quindi sia uno stato cached sia il frontier finale.

### RESULT

```text
TMT-001 : PASS
```

Nella run PIT post_TMT il test viene indicato esplicitamente come killing test
per:

```text
M-01 : hasNext(), linea 111
M-03 : hasNext(), linea 114
M-04 : hasPrevious(), linea 128
```

Transizioni:

```text
M-01 : SURVIVED    -> KILLED
M-03 : NO_COVERAGE -> KILLED
M-04 : SURVIVED    -> KILLED
```

---

## 8. TMT-002 – remove after reset from consumed Iterator

### WHY

Il survivor `M-06` riguardava la condizione di validità di `remove()`:

```text
wrappedIteratorIndex - currentIndex > 1
```

Il precedente `TCF-005` percorreva un caso eccezionale ma non isolava questo
predicato, perché un'altra condizione era già sufficiente a produrre
`IllegalStateException`.

Era quindi necessario costruire uno stato in cui il secondo predicato fosse
semanticamente decisivo.

### HOW

Sequenza:

```text
[A, B, C]

next() -> A
next() -> B
next() -> C

reset()

remove()
```

Dopo il consumo completo il wrapped Iterator è avanzato fino al frontier.
`reset()` riporta il cursore logico del wrapper alla posizione iniziale.

L'operazione `remove()` deve quindi essere rifiutata.

Oracle:

```text
IllegalStateException
underlying list unchanged: [A, B, C]
```

### RESULT

```text
TMT-002 : PASS
```

Nella run PIT post_TMT viene indicato esplicitamente come killing test per:

```text
M-06 : remove(), linea 220
```

Transizione:

```text
M-06 : SURVIVED -> KILLED
```

---

## 9. Run funzionale cumulativa post T_MT

Dopo l'aggiunta dei due test mutation-guided è stata eseguita l'intera suite
manuale:

```text
T_BB : 12
T_CF : 5
T_MT : 2
---------------
Total: 19
```

Esito:

```text
Tests run : 19
Failures  : 0
Errors    : 0
Skipped   : 0

BUILD SUCCESS
```

Quindi:

```text
Manual cumulative suite = 19/19 PASS
```

---

## 10. PIT post T_MT

La seconda run PIT mantiene invariati:

- versione PIT;
- plugin JUnit 5;
- mutatori;
- thread count;
- target class;
- bytecode production.

L'unica differenza sperimentale è l'aggiunta dei due test `T_MT`.

Risultato:

```text
TOTAL          : 52
KILLED         : 50
SURVIVED       : 2
NO_COVERAGE    : 0
TIMED_OUT      : 0
RUN_ERROR      : 0
MEMORY_ERROR   : 0

Mutation Score : 96.15%
Test Strength  : 96.15%
```

PIT riporta inoltre:

```text
Line Coverage for mutated class: 72/72 = 100%
```

Questa è la coverage osservata internamente da PIT durante la run post_T_MT.
La misura strutturale canonica della fase T_CF resta invece quella JaCoCo
registrata prima della mutation-guided phase:

```text
JaCoCo post T_CF LINE: 71/72 = 98.61%
```

Le due misure vengono mantenute distinte.

---

## 11. Delta prodotto da T_MT

Confronto:

| Metrica | Baseline T_BB+T_CF | Post T_MT | Delta |
|---|---:|---:|---:|
| TOTAL | 52 | 52 | 0 |
| KILLED | 46 | 50 | +4 |
| SURVIVED | 4 | 2 | -2 |
| NO_COVERAGE | 2 | 0 | -2 |
| Mutation Score | 88.46% | 96.15% | +7.69 pp |
| Test Strength | 92.00% | 96.15% | +4.15 pp |

I due test aggiunti eliminano quindi quattro dei sei gap osservati nella
baseline.

---

## 12. Transizioni dei sei mutanti iniziali

| ID | Baseline | Post T_MT | Killing test / esito |
|---|---|---|---|
| M-01 `hasNext` line 111 | SURVIVED | KILLED | `TMT-001` |
| M-02 `hasNext` line 111 | NO_COVERAGE | SURVIVED | ora raggiunto, ma non discriminato |
| M-03 `hasNext` line 114 | NO_COVERAGE | KILLED | `TMT-001` |
| M-04 `hasPrevious` line 128 | SURVIVED | KILLED | `TMT-001` |
| M-05 `next` line 139 | SURVIVED | SURVIVED | residual candidate |
| M-06 `remove` line 220 | SURVIVED | KILLED | `TMT-002` |

Questo confronto consente di attribuire direttamente il miglioramento della
mutation adequacy ai due test mutation-guided.

---

## 13. Survivor residuo S-01 – `hasNext()`

Mutante residuo:

```text
Method  : hasNext()
Line    : 111
Mutator : RemoveConditionalMutator_EQUAL_ELSE
Status  : SURVIVED
```

Il mutante era `NO_COVERAGE` nella baseline ed è diventato `SURVIVED` dopo
TMT-001.

Questo significa che il nuovo test ha reso il codice mutato raggiungibile, ma
non ha prodotto una differenza osservabile.

Il comportamento discriminante richiederebbe uno stato nel quale la condizione
rimossa cambi il risultato pubblico di `hasNext()`.

L'analisi di raggiungibilità evidenzia che tale combinazione è fortemente
vincolata dal modo in cui i percorsi `Iterator` e `ListIterator` mantengono i
rispettivi cursori.

Classificazione finale:

```text
STRONG EQUIVALENT / INFEASIBLE-DISCRIMINATION CANDIDATE
```

Non viene dichiarato formalmente equivalente in senso matematico; viene
documentato come candidato forte per il quale non è stato individuato un
comportamento pubblico discriminante raggiungibile.

---

## 14. Survivor residuo S-02 – `next()`

Mutante residuo:

```text
Method  : next()
Line    : 139
Mutator : RemoveConditionalMutator_EQUAL_ELSE
Status  : SURVIVED
```

Il mutante elimina il conditional path dedicato al caso in cui l'Iterator
wrapped sia già un `ListIterator`.

Il ramo originale delega direttamente a:

```text
iterator.next()
```

Il percorso mutato continua comunque ad avanzare l'Iterator e restituisce il
medesimo elemento, mentre aggiorna anche strutture interne utilizzate dal ramo
plain-Iterator.

Per il caso `ListIterator`, gli altri metodi pubblici rilevanti continuano a
seguire i propri percorsi delegati al wrapped `ListIterator`.

Non è stato individuato uno stato pubblico stabile che consenta di distinguere
il programma originale dal mutante senza accedere a stato interno.

Classificazione finale:

```text
STRONG EQUIVALENT-MUTANT CANDIDATE
```

Anche in questo caso non viene dichiarata equivalenza formale; viene
documentata l'assenza di un discriminante pubblico identificato.

---

## 15. Stopping rule

La suite mutation-guided viene arrestata quando:

1. i survivor chiaramente killable sono stati attaccati con test mirati;
2. i `NO_COVERAGE` utilmente raggiungibili sono stati trasformati in
   `KILLED` oppure sottoposti a feasibility audit;
3. i survivor residui richiederebbero test artificiali o accesso a stato
   interno senza un chiaro comportamento pubblico discriminante;
4. ulteriori test aumenterebbero la cardinalità senza una giustificazione
   metodologica sufficientemente forte.

Risultato raggiunto:

```text
2 T_MT
+4 mutants killed
0 NO_COVERAGE
96.15% Mutation Score
96.15% Test Strength
```

Decisione:

```text
TMT-003 planned            : NO
Residual survivor audit    : COMPLETE
T_MT stopping rule reached : YES
T_MT STATUS                : FROZEN
```

---

## 16. Freeze finale della suite manuale

Composizione finale:

```text
T_BB additions : 12
T_CF additions : 5
T_MT additions : 2
-------------------------
Manual suite   : 19
```

Esito:

```text
19/19 PASS
```

Mutation adequacy:

```text
Baseline:
  46 KILLED
   4 SURVIVED
   2 NO_COVERAGE
  Mutation Score = 88.46%
  Test Strength  = 92.00%

Post T_MT:
  50 KILLED
   2 SURVIVED
   0 NO_COVERAGE
  Mutation Score = 96.15%
  Test Strength  = 96.15%
```

Stato:

```text
T_BB STATUS : FROZEN
T_CF STATUS : FROZEN
T_MT STATUS : FROZEN
```

---

## 17. Evidenze

Baseline mutation:

```text
isw2/results/testing/list-iterator-wrapper/mutation/baseline/
├── listiteratorwrapper_pit_baseline_run.txt
├── listiteratorwrapper_pit_baseline_mutations.xml
├── listiteratorwrapper_pit_baseline_mutations.csv
└── listiteratorwrapper_pit_baseline_summary.txt
```

Post T_MT:

```text
isw2/results/testing/list-iterator-wrapper/mutation/post-tmt/
├── listiteratorwrapper_post_tmt_pit_run.txt
├── listiteratorwrapper_post_tmt_mutations.xml
├── listiteratorwrapper_post_tmt_mutations.csv
└── listiteratorwrapper_post_tmt_summary.txt
```

Test mutation-guided:

```text
isw2/testing/src/test/java/
it/uniroma2/isw2/openjpa/testing/listiteratorwrapper/mt/
ListIteratorWrapperMutationTest.java
```

---

## 18. Handoff alle suite automatiche

La parte manuale della seconda classe è conclusa.

Baseline manuale finale da preservare:

```text
ListIteratorWrapper
T_BB = 12
T_CF = 5
T_MT = 2
Total manual = 19
```

Le suite automatiche successive vengono valutate separatamente e non vengono
utilizzate per alterare retroattivamente T_BB, T_CF o T_MT.

Ordine operativo:

```text
T_RND
   ↓
T_ES
   ↓
T_LLM
```

Le relative metriche verranno confrontate mantenendo separati:

- origine dei test;
- cardinalità;
- coverage;
- mutation score;
- test strength;
- qualità/manutenibilità della suite.

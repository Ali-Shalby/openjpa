# ListIteratorWrapper – Generazione tramite LLM (`T_LLM`)

## 1. Obiettivo

Questa fase costruisce e valuta la suite automatica `T_LLM` per:

```text
org.apache.openjpa.lib.util.collections.ListIteratorWrapper
```

sulla baseline:

```text
Apache OpenJPA 4.1.1
```

La cardinalità sperimentale è fissata prima della generazione a:

```text
N = 12
```

in modo da mantenere il confronto same-cardinality con le altre suite
automatiche della stessa classe:

```text
T_RND
T_ES
T_LLM
```

La suite viene generata in modo indipendente rispetto alle altre tecniche.
I test nativi di OpenJPA, le suite manuali e le suite automatiche precedenti
non vengono fornite all'LLM come contesto.

Coverage e mutation testing vengono osservati esclusivamente dopo il freeze
della suite e non vengono utilizzati per rigenerare, selezionare o modificare
i test.

---

## 2. Tool e ambiente

Configurazione sperimentale:

```text
Suite                  : T_LLM
Target                 : org.apache.openjpa.lib.util.collections.ListIteratorWrapper
Baseline               : Apache OpenJPA 4.1.1
Cardinalità            : 12 test case/scenari
Framework              : JUnit Jupiter
Runtime                : Java 21
LLM client             : Microsoft Copilot
Interaction mode       : browser chat
Model                  : GPT 5.6 Think Deeper
Model provider         : OpenAI
```

L'ambiente completo è registrato in:

```text
isw2/testing/llm/listiteratorwrapper/evidence/
└── T_LLM-environment.txt
```

Il sorgente production consegnato all'LLM è conservato come copia testuale in:

```text
isw2/testing/llm/listiteratorwrapper/evidence/
└── ListIteratorWrapper.java.txt
```

La copia è utilizzata esclusivamente come production context
dell'esperimento.

---

## 3. Protocollo di generazione

Il protocollo viene fissato prima della generazione e conservato in:

```text
isw2/testing/llm/listiteratorwrapper/
└── llm-test-generation-protocol.md
```

La strategia prevede un solo prompt principale.

Il prompt richiede, nella stessa risposta:

1. una breve analisi di `ListIteratorWrapper` dal punto di vista del testing;
2. la progettazione di esattamente 12 scenari;
3. l'identificazione degli scenari come `TLLM-001 ... TLLM-012`;
4. la descrizione sintetica di comportamento, setup/input, azione e risultato
   atteso;
5. l'implementazione degli stessi 12 casi in Java 21 con JUnit Jupiter;
6. la tracciabilità uno-a-uno tra scenario progettato e test implementato.

Mockito è disponibile nell'ambiente ma non è obbligatorio.

La cardinalità viene definita come numero di scenari/test case:

```text
N = 12
```

Fixture, helper e setup condiviso non incrementano `N`.

---

## 4. Isolamento del contesto

Per mantenere indipendente la generazione, non vengono forniti all'LLM:

```text
test nativi OpenJPA
T_BB
T_CF
T_MT
T_RND
T_ES
risultati JaCoCo
risultati PIT
survivor mutation
metriche delle altre suite
fonti Web o esterne
```

Il contesto comprende esclusivamente:

```text
ListIteratorWrapper.java
informazioni tecniche dell'ambiente T_LLM
prompt e protocollo preregistrati
```

JaCoCo e PIT restano misure esclusivamente post-freeze.

---

## 5. Preregistrazione

Prima dell'interazione di generazione vengono congelati:

```text
protocollo
production context
environment
prompt principale
```

La preregistrazione precede la produzione dei test e impedisce di adattare il
protocollo in funzione delle metriche osservate successivamente.

Il prompt principale è conservato in:

```text
isw2/testing/llm/listiteratorwrapper/prompts/
└── ListIteratorWrapper-LLM-single-prompt.txt
```

La risposta originale è conservata in:

```text
isw2/testing/llm/listiteratorwrapper/responses/
└── main-response.md
```

---

## 6. Generazione iniziale

La risposta principale produce:

```text
Scenari progettati      : 12
Identificativi          : TLLM-001 ... TLLM-012
Test JUnit Jupiter      : 12
Mapping design -> code  : 1:1
```

La suite generata copre sia il comportamento con un semplice `Iterator` sia
il ramo di delega quando l'oggetto sottostante è un `ListIterator`.

Fra i comportamenti generati sono presenti:

```text
costruttore con Iterator nullo
stato iniziale e indici
navigazione forward
navigazione backward sulla cache
replay della cache
reset
boundary di previous()
operazioni add()/set() su plain Iterator
remove() prima della navigazione
remove() dopo next()
delega di navigazione a ListIterator
delega delle operazioni opzionali a ListIterator
```

---

## 7. Prima integrazione nel testing harness

La suite viene integrata nel package:

```text
it.uniroma2.isw2.openjpa.testing.listiteratorwrapper.llm
```

File canonico:

```text
isw2/testing/src/test/java/
└── it/uniroma2/isw2/openjpa/testing/listiteratorwrapper/llm/
    └── ListIteratorWrapperLLMTest.java
```

L'integrazione iniziale applica esclusivamente gli adattamenti necessari al
testing harness:

```text
package di progetto
nome della classe di test
import della classe production
```

Prima della validazione non vengono osservati né JaCoCo né PIT.

---

## 8. Prima validazione runtime

La prima esecuzione reale con Java 21 compila correttamente la suite ma
evidenzia problemi runtime in sei scenari.

Risultato:

```text
Tests run              : 12
Failures               : 3
Errors                 : 3
Skipped                : 0
```

I problemi interessano:

```text
TLLM-002
TLLM-003
TLLM-004
TLLM-007
TLLM-008
TLLM-009
```

L'evidence completa è conservata in:

```text
isw2/testing/llm/listiteratorwrapper/evidence/
└── T_LLM-raw-validation-01.txt
```

---

## 9. Diagnosi della prima validazione

La causa principale riguarda alcuni setup basati su:

```text
List.of(...).iterator()
```

Il runtime concreto restituito da questa espressione implementa anche
`ListIterator`.

Di conseguenza, `ListIteratorWrapper` riconosce il collaboratore come
`ListIterator` e percorre il ramo di delega anziché il comportamento previsto
dagli scenari che volevano esercitare un plain `Iterator`.

Questo produce eccezioni differenti in alcuni casi e altera la semantica del
setup rispetto all'intento dichiarato degli scenari.

Sono inoltre emerse due discrepanze di oracle:

```text
TLLM-008:
tipo di eccezione corretto, ma il messaggio runtime non coincide nel setup RAW

TLLM-009:
messaggio osservato:
"Cannot remove element at index -1."

invece di:
"Cannot remove element at index 0."
```

La diagnosi deriva esclusivamente dall'esecuzione della suite e dal production
context.

Non vengono utilizzati:

```text
JaCoCo
PIT
survivor
mutation score
coverage gap
```

---

## 10. Politica di repair pre-freeze

Poiché la suite non è ancora congelata, il protocollo consente esclusivamente
repair necessari a ottenere una suite tecnicamente valida e coerente con il
production context.

I vincoli del repair sono:

```text
cardinalità             : invariata, N = 12
identificativi          : TLLM-001 ... TLLM-012 invariati
nuovi scenari           : NO
scenari eliminati       : NO
feedback JaCoCo         : NONE
feedback PIT            : NONE
feedback da altre suite : NONE
production code edit    : NONE
```

Le interrogazioni di repair vengono mantenute nella stessa conversazione LLM
e sono registrate integralmente.

---

## 11. Repair R1

Il primo repair richiede di correggere esclusivamente i sei problemi rilevati
durante la validazione runtime.

Evidence:

```text
isw2/testing/llm/listiteratorwrapper/prompts/
└── R1-runtime-validation-repair.txt

isw2/testing/llm/listiteratorwrapper/responses/
└── R1-runtime-validation-repair-response.md
```

La risposta R1 propone correzioni, ma presenta due limiti:

1. in alcuni casi modifica il tipo di iteratore utilizzato in modo non
   pienamente coerente con l'intento originale di esercitare il ramo
   plain-`Iterator`;
2. il file Java restituito è troncato.

Stato:

```text
R1 Java output         : TRUNCATED
Usabile come canonical : NO
```

La risposta viene conservata come evidence ma non viene adottata come suite
finale.

---

## 12. Repair R2

Il secondo repair rende esplicito il requisito di preservare l'intento degli
scenari che devono utilizzare un vero plain `Iterator`.

Per questo viene richiesto di utilizzare il wrapper helper:

```text
CountingIterator
```

che implementa soltanto `Iterator` e impedisce che l'oggetto runtime venga
riconosciuto come `ListIterator`.

Decisioni principali R2:

```text
TLLM-002 : plain Iterator tramite CountingIterator
TLLM-003 : plain Iterator tramite CountingIterator
TLLM-004 : plain Iterator tramite CountingIterator
TLLM-007 : plain Iterator tramite CountingIterator
TLLM-008 : plain Iterator tramite CountingIterator
TLLM-009 : oracle del messaggio corretto a index -1
```

Evidence:

```text
isw2/testing/llm/listiteratorwrapper/prompts/
└── R2-complete-runtime-repair.txt

isw2/testing/llm/listiteratorwrapper/responses/
└── R2-complete-runtime-repair-response.md
```

Le decisioni prodotte sono coerenti con il production context, ma anche la
risposta R2 viene troncata durante il blocco Java.

Stato:

```text
R2 repair decisions    : USABLE
R2 Java output         : TRUNCATED
Usabile come canonical : NO
```

---

## 13. Repair R3

Il terzo repair non richiede una nuova analisi né nuovi scenari.

Viene richiesto esclusivamente di completare il file Java sulla base delle
decisioni già definite in R2:

```text
output richiesto       : code only
cardinalità            : 12
identificativi         : TLLM-001 ... TLLM-012
nuovi scenari          : NO
coverage feedback      : NONE
mutation feedback      : NONE
```

Evidence:

```text
isw2/testing/llm/listiteratorwrapper/prompts/
└── R3-code-only-completion.txt

isw2/testing/llm/listiteratorwrapper/responses/
└── R3-code-only-completion-response.md
```

R3 restituisce il file Java completo.

Questa versione diventa la candidata canonica per la validazione finale.

---

## 14. Validazione finale

La suite R3 viene compilata ed eseguita con:

```text
Java                   : 21
Framework              : JUnit Jupiter
Target suite           : ListIteratorWrapperLLMTest
```

Risultato:

```text
Tests run              : 12
Failures               : 0
Errors                 : 0
Skipped                : 0

Result                 : 12 / 12 PASS
```

La suite soddisfa quindi la cardinalità sperimentale e la tracciabilità
preregistrate.

---

## 15. Stability validation

Prima del freeze viene effettuata una verifica di stabilità mediante cinque
esecuzioni consecutive della sola suite `T_LLM`.

Risultato:

```text
Runs requested         : 5
Runs passed            : 5
Runs failed            : 0

Stability              : 5 / 5 PASS
```

Evidence:

```text
isw2/testing/llm/listiteratorwrapper/evidence/
└── T_LLM-stability-validation.txt
```

Non vengono osservate metriche di adequacy prima del completamento di questa
verifica.

---

## 16. Freeze pre-adequacy

Dopo:

```text
12 / 12 PASS
5 / 5 stability PASS
```

la suite viene congelata.

Freeze audit:

```text
Cardinality            : 12
Traceability           : TLLM-001 ... TLLM-012 PASS
Validation             : 12 / 12 PASS
Stability              : 5 / 5 PASS
Repair chain           : R1 -> R2 -> R3
Final usable repair    : R3
JaCoCo before freeze   : NOT RUN
PIT before freeze      : NOT RUN
```

SHA-256 canonico:

```text
9044AC58592FD650B0080B27D42526A85A8762029C51DDD69E874666004C5F8C
```

Evidence:

```text
isw2/testing/llm/listiteratorwrapper/evidence/
└── T_LLM-freeze-audit.txt
```

Da questo punto:

```text
T_LLM STATUS           : FROZEN
```

e il file Java non viene più modificato.

---

## 17. Identità del bytecode production

Le misure post-freeze vengono eseguite sul bytecode production di
OpenJPA 4.1.1.

SHA-256:

```text
C06F2D6F83082E8CC538069769BA1C2241678054E9C27B0510A77C0ADCE4B0F4
```

La stessa identità production è utilizzata per le altre suite sperimentali di
`ListIteratorWrapper`.

Questo permette di mantenere confrontabili coverage e mutation testing.

---

## 18. Coverage con JaCoCo

La coverage viene misurata esclusivamente dopo il freeze.

Configurazione:

```text
JaCoCo                 : 0.8.15
Target                 : org.apache.openjpa.lib.util.collections.ListIteratorWrapper
Suite                  : T_LLM
Tests                  : 12
Result                 : 12 / 12 PASS
```

Risultati:

| Metrica | Covered | Missed | Totale | Coverage |
| ------- | ------: | -----: | -----: | -------: |
| Line    |      72 |      0 |     72 |  100.00% |
| Branch  |      37 |      3 |     40 |   92.50% |
| Method  |      11 |      0 |     11 |  100.00% |

L'universo strutturale coincide con quello già utilizzato per le altre suite:

```text
Lines                  : 72
Branches               : 40
Methods                : 11
```

Il controllo dell'hash dopo la misurazione conferma che la suite non viene
modificata durante la fase JaCoCo.

Evidence:

```text
isw2/results/testing/list-iterator-wrapper/llm/coverage/
├── jacoco.csv
├── jacoco.xml
├── listiteratorwrapper_tllm_coverage_summary.txt
└── listiteratorwrapper_tllm_jacoco.exec
```

---

## 19. Interpretazione della coverage

`T_LLM` raggiunge:

```text
100.00% Line Coverage
92.50% Branch Coverage
100.00% Method Coverage
```

La copertura totale di linee e metodi non implica una copertura completa dei
branch.

Restano infatti:

```text
3 branch non coperti su 40
```

La suite viene comunque mantenuta integralmente come outcome sperimentale
congelato.

I branch residui non vengono utilizzati per aggiungere, eliminare o modificare
test.

---

## 20. Mutation testing

La mutation analysis viene eseguita esclusivamente dopo il freeze.

Configurazione:

```text
PIT                    : 1.25.8
pitest-junit5-plugin   : 1.2.3
Target                 : org.apache.openjpa.lib.util.collections.ListIteratorWrapper
Target tests           : ListIteratorWrapperLLMTest
Mutators               : DEFAULTS
Threads                : 1
Runtime                : Java 21
Native OpenJPA tests   : NOT USED
```

Il run usa il medesimo bytecode production congelato già utilizzato nelle
altre suite della classe.

---

## 21. Identità della popolazione mutante

Prima dell'interpretazione dei risultati viene verificata l'identità della
popolazione rispetto alla baseline della classe.

Risultato:

```text
Reference population   : 52
Current population     : 52
Population diff        : 0
Population identity    : PASS (52/52)
```

La popolazione mutante è quindi direttamente confrontabile con quella
utilizzata per `T_RND` e `T_ES`.

---

## 22. Risultati PIT

Risultato:

```text
TOTAL                  : 52
KILLED                 : 47
SURVIVED               : 5
NO_COVERAGE            : 0
TIMED_OUT              : 0
RUN_ERROR              : 0
MEMORY_ERROR           : 0
```

Mutanti raggiunti:

```text
KILLED + SURVIVED
= 47 + 5
= 52
```

Mutation Score:

```text
47 / 52
= 90.38%
```

Test Strength:

```text
47 / (47 + 5)
= 90.38%
```

Poiché:

```text
NO_COVERAGE = 0
```

tutti i 52 mutanti vengono raggiunti dalla suite.

Di conseguenza, in questo esperimento:

```text
Mutation Score = Test Strength = 90.38%
```

---

## 23. Survivor post-freeze

PIT registra cinque mutanti `SURVIVED`.

Distribuzione:

```text
hasNext()   : 1 survivor
next()      : 1 survivor
previous()  : 1 survivor
remove()    : 2 survivor
```

I survivor interessano mutazioni condizionali e di boundary.

In particolare risultano presenti survivor nei seguenti punti:

```text
hasNext()   line 111
next()      line 139
previous()  line 187
remove()    line 220
```

I survivor vengono mantenuti come risultato sperimentale.

Non viene effettuata alcuna classificazione automatica come mutanti
equivalenti e non vengono utilizzati per creare nuovi test o modificare gli
oracle della suite congelata.

```text
PIT -> test editing     : NO
PIT -> regeneration    : NO
```

---

## 24. Evidence mutation

Gli artefatti PIT sono conservati in:

```text
isw2/results/testing/list-iterator-wrapper/llm/mutation/
├── listiteratorwrapper_tllm_mutation_summary.txt
├── listiteratorwrapper_tllm_mutations.csv
├── listiteratorwrapper_tllm_mutations.xml
└── listiteratorwrapper_tllm_pit_run.txt
```

Gli artefatti riportano:

```text
Population identity    : PASS (52/52)
KILLED                 : 47
SURVIVED               : 5
NO_COVERAGE            : 0
Mutation Score         : 90.38%
Test Strength          : 90.38%
```

---

## 25. Confronto con le altre suite automatiche

Le tre suite automatiche vengono confrontate a parità di cardinalità:

```text
N = 12
```

e sulla stessa classe production.

| Suite   | Line Coverage | Branch Coverage | Method Coverage | KILLED | SURVIVED | NO_COVERAGE | Mutation Score | Test Strength |
| ------- | ------------: | --------------: | --------------: | -----: | -------: | ----------: | -------------: | ------------: |
| `T_RND` |        58.33% |          47.50% |         100.00% |      6 |       19 |          27 |         11.54% |        24.00% |
| `T_ES`  |        83.33% |          82.50% |          81.82% |     29 |       15 |           8 |         55.77% |        65.91% |
| `T_LLM` |       100.00% |          92.50% |         100.00% |     47 |        5 |           0 |         90.38% |        90.38% |

Tutte le suite utilizzano:

```text
same-cardinality       : N = 12
same production target : ListIteratorWrapper
same mutation universe : 52 mutanti
```

---

## 26. Delta rispetto a T_RND

Differenze `T_LLM - T_RND`:

| Metrica         |   T_RND |   T_LLM |     Delta |
| --------------- | ------: | ------: | --------: |
| Line Coverage   |  58.33% | 100.00% | +41.67 pp |
| Branch Coverage |  47.50% |  92.50% | +45.00 pp |
| Method Coverage | 100.00% | 100.00% |   0.00 pp |
| Mutation Score  |  11.54% |  90.38% | +78.84 pp |
| Test Strength   |  24.00% |  90.38% | +66.38 pp |
| KILLED          |       6 |      47 |       +41 |
| NO_COVERAGE     |      27 |       0 |       -27 |

`T_LLM` raggiunge l'intera popolazione mutante e presenta una capacità
discriminante molto maggiore della suite random in questo esperimento.

---

## 27. Delta rispetto a T_ES

Differenze `T_LLM - T_ES`:

| Metrica         |   T_ES |   T_LLM |     Delta |
| --------------- | -----: | ------: | --------: |
| Line Coverage   | 83.33% | 100.00% | +16.67 pp |
| Branch Coverage | 82.50% |  92.50% | +10.00 pp |
| Method Coverage | 81.82% | 100.00% | +18.18 pp |
| Mutation Score  | 55.77% |  90.38% | +34.61 pp |
| Test Strength   | 65.91% |  90.38% | +24.47 pp |
| KILLED          |     29 |      47 |       +18 |
| NO_COVERAGE     |      8 |       0 |        -8 |

Nel target `ListIteratorWrapper`, `T_LLM` ottiene valori superiori a `T_ES`
per tutte le metriche strutturali e mutation osservate.

---

## 28. Interpretazione

Sul target sperimentale `ListIteratorWrapper`, la suite `T_LLM` mostra il
risultato più elevato tra le tre suite automatiche considerate.

Risultati principali:

```text
LINE                   : 100.00%
BRANCH                 : 92.50%
METHOD                 : 100.00%

Mutation Score         : 90.38%
Test Strength          : 90.38%
```

La suite copre tutte le linee e tutti i metodi del target, raggiunge 37 dei 40
branch e raggiunge tutti i 52 mutanti della popolazione canonica.

La mutation analysis mostra inoltre che 47 dei 52 mutanti vengono uccisi.

Il risultato viene interpretato esclusivamente nel contesto
dell'esperimento su `ListIteratorWrapper`.

Non costituisce una generalizzazione sulla superiorità degli LLM rispetto ad
altre tecniche automatiche.

---

## 29. Valutazione qualitativa

La suite LLM esercita esplicitamente entrambe le modalità principali della
classe:

```text
plain Iterator
ListIterator delegation
```

La suite contiene oracle su:

```text
valori restituiti
indici logici
predicati hasNext()/hasPrevious()
eccezioni
messaggi di eccezione
modifiche della collection sottostante
riuso della cache
numero di avanzamenti del delegate
reset
operazioni opzionali add()/set()/remove()
```

L'helper `CountingIterator` rende osservabile un aspetto che non sarebbe
visibile controllando soltanto i valori restituiti: il numero di chiamate
effettive a `next()` sul delegate.

Questo permette di verificare il replay dalla cache senza confondere la
navigazione logica del wrapper con l'avanzamento dell'iteratore sottostante.

---

## 30. Neutralità rispetto alle metriche

La sequenza applicata è:

```text
Protocol definition
        |
        v
Main LLM generation
        |
        v
RAW runtime validation
        |
        v
R1 repair attempt
        |
        v
R2 repair decisions
        |
        v
R3 code completion
        |
        v
Canonical validation (12/12)
        |
        v
Stability validation (5/5)
        |
        v
PRE-ADEQUACY FREEZE
        |
        +--> JaCoCo
        |
        +--> PIT
```

Non viene applicato alcun feedback post-freeze:

```text
JaCoCo -> regeneration  : NO
PIT    -> regeneration  : NO
JaCoCo -> oracle edit   : NO
PIT    -> oracle edit   : NO
Survivor -> new tests   : NO
```

I repair R1-R3 avvengono prima del freeze e derivano esclusivamente da problemi
osservati durante la validazione runtime e dal production context.

---

## 31. Evidence della generazione LLM

La generazione e i repair sono conservati in:

```text
isw2/testing/llm/listiteratorwrapper/
|
+-- llm-test-generation-protocol.md
|
+-- evidence/
|   +-- ListIteratorWrapper.java.txt
|   +-- T_LLM-environment.txt
|   +-- T_LLM-freeze-audit.txt
|   +-- T_LLM-raw-validation-01.txt
|   +-- T_LLM-stability-validation.txt
|
+-- prompts/
|   +-- ListIteratorWrapper-LLM-single-prompt.txt
|   +-- R1-runtime-validation-repair.txt
|   +-- R2-complete-runtime-repair.txt
|   +-- R3-code-only-completion.txt
|
+-- responses/
    +-- main-response.md
    +-- R1-runtime-validation-repair-response.md
    +-- R2-complete-runtime-repair-response.md
    +-- R3-code-only-completion-response.md
```

---

## 32. Evidence delle misure post-freeze

Le misure di adequacy sono conservate in:

```text
isw2/results/testing/list-iterator-wrapper/llm/
|
+-- coverage/
|   +-- jacoco.csv
|   +-- jacoco.xml
|   +-- listiteratorwrapper_tllm_coverage_summary.txt
|   +-- listiteratorwrapper_tllm_jacoco.exec
|
+-- mutation/
    +-- listiteratorwrapper_tllm_mutation_summary.txt
    +-- listiteratorwrapper_tllm_mutations.csv
    +-- listiteratorwrapper_tllm_mutations.xml
    +-- listiteratorwrapper_tllm_pit_run.txt
```

Suite canonica:

```text
isw2/testing/src/test/java/
└── it/uniroma2/isw2/openjpa/testing/listiteratorwrapper/llm/
    └── ListIteratorWrapperLLMTest.java
```

---

## 33. Risultato finale

```text
Target                  : ListIteratorWrapper
Technique               : T_LLM
Client                  : Microsoft Copilot
Model                   : GPT 5.6 Think Deeper
Runtime                 : Java 21
Framework               : JUnit Jupiter

Target N                : 12
Generated scenarios     : 12
Canonical tests         : 12
Canonical PASS          : 12 / 12
Stability               : 5 / 5 PASS

Repair chain            : R1 -> R2 -> R3
Final usable repair     : R3

Frozen SHA-256:
9044AC58592FD650B0080B27D42526A85A8762029C51DDD69E874666004C5F8C

Production SHA-256:
C06F2D6F83082E8CC538069769BA1C2241678054E9C27B0510A77C0ADCE4B0F4

JaCoCo
------
LINE                    : 72 / 72 = 100.00%
BRANCH                  : 37 / 40 = 92.50%
METHOD                  : 11 / 11 = 100.00%

PIT
---
Population              : 52
Population identity     : PASS (52/52)
KILLED                  : 47
SURVIVED                : 5
NO_COVERAGE             : 0
TIMED_OUT               : 0
RUN_ERROR               : 0
MEMORY_ERROR            : 0
Mutation Score          : 90.38%
Test Strength           : 90.38%

Coverage feedback       : NONE
Mutation feedback       : NONE
Post-freeze editing     : NONE

T_LLM STATUS            : FROZEN / COMPLETE
```

---

## 34. Chiusura della fase

Con il completamento di `T_LLM`, per `ListIteratorWrapper` risultano disponibili
tutte le tre suite automatiche same-cardinality:

```text
T_RND : 12
T_ES  : 12
T_LLM : 12
```

Le suite sono state generate con tecniche differenti e congelate prima
dell'osservazione delle rispettive metriche di adequacy.

Il confronto finale può quindi essere effettuato senza modificare
retroattivamente gli outcome sperimentali.

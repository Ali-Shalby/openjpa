# ListIteratorWrapper – EvoSuite Testing

## 1. Obiettivo

Dopo il freeze della suite automatica random `T_RND`, è stata costruita una seconda suite automatica indipendente mediante **EvoSuite** per la classe:

```text
org.apache.openjpa.lib.util.collections.ListIteratorWrapper
```

La suite viene indicata come:

```text
T_ES
```

Per mantenere il confronto same-cardinality con la suite black-box iniziale, la cardinalità sperimentale è stata fissata prima della generazione a:

```text
N = 12
```

Coverage e mutation testing sono stati osservati soltanto dopo il freeze della suite e non sono stati utilizzati per rigenerare, selezionare o modificare i test.

---

## 2. Tool e ambiente

Generatore:

```text
EvoSuite 1.2.0
```

Runtime tecnico di generazione e validazione:

```text
Zulu JDK 11
```

Framework dei test generati:

```text
JUnit 4.13.2
```

Runtime EvoSuite:

```text
evosuite-standalone-runtime 1.2.0
```

Il bytecode production utilizzato appartiene a:

```text
org.apache.openjpa:openjpa-lib:4.1.1
```

---

## 3. Protocollo di generazione

Il protocollo è stato fissato prima dell'osservazione delle metriche:

```text
Target                  : org.apache.openjpa.lib.util.collections.ListIteratorWrapper
Target N                : 12
Criterion               : LINE:BRANCH
Search budget           : 120 s per seed
Stopping condition      : MAXTIME
Minimization            : enabled
Max suite size          : 12
Seed iniziale           : 0
Seed aggiuntivi         : solo se necessari per raggiungere N
Coverage feedback       : NONE
Mutation feedback       : NONE
Post-adequacy editing   : NONE
Native OpenJPA tests    : NOT USED
```

La politica multi-seed prevede di consumare seed deterministici consecutivi e di arrestare la generazione appena sono disponibili almeno `N` test validi.

---

## 4. Generazione seed 0

Il seed canonico utilizzato è:

```text
seed = 0
```

La generazione termina correttamente con:

```text
Exit code               : 0
Java files              : 2
Generated tests         : 15
Target N                : 12
```

I due file prodotti sono:

```text
ListIteratorWrapper_ESTest.java
ListIteratorWrapper_ESTest_scaffolding.java
```

Poiché il seed 0 produce già almeno 12 test validi, non sono necessari seed aggiuntivi.

---

## 5. Validazione RAW

Prima di qualsiasi selezione finale, la suite RAW generata dal seed 0 viene compilata ed eseguita integralmente in ambiente Java 11.

Risultato:

```text
Generated               : 15
Compiled                : PASS
JUnit                   : 15 / 15 PASS
Target N                : 12
Additional seeds        : NOT REQUIRED
Coverage observed       : NO
Mutation observed       : NO
```

La generazione seed 0 è quindi considerata valida.

---

## 6. Selezione canonica

La cardinalità sperimentale prefissata è:

```text
N = 12
```

La suite RAW contiene 15 test validi. La selezione canonica è quindi puramente posizionale:

```text
Selected                : first 12 / 15
Excluded                : last 3 / 15
Selection criterion     : generator order only
```

I tre test eccedenti non vengono esclusi sulla base di coverage, mutation score o valutazioni semantiche.

Non viene eseguito alcun seed successivo.

---

## 7. Integrazione nel testing harness

La suite canonica viene integrata nel package:

```text
it.uniroma2.isw2.openjpa.testing.listiteratorwrapper.es
```

File canonici:

```text
ListIteratorWrapperEvoSuiteSeed0Test.java
ListIteratorWrapperEvoSuiteSeed0Scaffolding.java
```

L'integrazione applica esclusivamente adattamenti infrastrutturali necessari al testing harness:

- package e nomi delle classi;
- selezione posizionale dei primi 12 test;
- configurazione `separateClassLoader = false` prima del freeze per consentire la misurazione JaCoCo sul bytecode target.

Gli oracle generati non vengono modificati sulla base delle metriche post-generation.

---

## 8. Validazione canonica e freeze pre-adequacy

La suite canonica viene compilata con Java 11 e poi eseguita mediante JUnit 4.

Risultato:

```text
Canonical tests         : 12
Canonical JUnit         : 12 / 12 PASS
Coverage observed       : NO
Mutation observed       : NO
Suite modifications     : FROZEN
```

Hash SHA-256 dei sorgenti congelati:

```text
Test:
206731C8322758C64612DC194113549C4BDB583D557521B4D3E9629B1BC564D9

Scaffolding:
7E501C4C62BBA117013BA5AC081AEE2B4A1615209359EA32077750157FF29CC5
```

Da questo punto la suite non viene più modificata.

---

## 9. Identità del bytecode production

La classe production utilizzata per le misure di adequacy è quella della release OpenJPA 4.1.1.

SHA-256:

```text
C06F2D6F83082E8CC538069769BA1C2241678054E9C27B0510A77C0ADCE4B0F4
```

La stessa identità production è utilizzata nelle altre suite sperimentali di `ListIteratorWrapper`.

---

## 10. Coverage con JaCoCo

La coverage viene misurata dopo il freeze mediante:

```text
JaCoCo 0.8.15
```

Target:

```text
org.apache.openjpa.lib.util.collections.ListIteratorWrapper
```

Risultati:

| Metrica | Covered | Missed | Totale | Coverage |
|---|---:|---:|---:|---:|
| Line | 60 | 12 | 72 | 83.33% |
| Branch | 33 | 7 | 40 | 82.50% |
| Method | 9 | 2 | 11 | 81.82% |

La misura strutturale canonica per il confronto tra suite rimane quella JaCoCo.

---

## 11. Confronto strutturale con T_RND

Entrambe le suite automatiche hanno cardinalità:

```text
N = 12
```

| Metrica | T_RND | T_ES | Delta T_ES |
|---|---:|---:|---:|
| Line Coverage | 58.33% | 83.33% | +25.00 pp |
| Branch Coverage | 47.50% | 82.50% | +35.00 pp |
| Method Coverage | 100.00% | 81.82% | -18.18 pp |

`T_ES` raggiunge quindi una quota significativamente maggiore di linee e branch rispetto a `T_RND`, mentre `T_RND` raggiunge tutti gli 11 metodi e `T_ES` ne raggiunge 9.

Il confronto è puramente descrittivo e non viene utilizzato per modificare la suite.

---

## 12. Mutation testing

La mutation analysis viene eseguita dopo il freeze.

Configurazione:

```text
PIT                     : 1.25.8
Mutators                : DEFAULTS
Threads                 : 1
Target                  : ListIteratorWrapper
Native OpenJPA tests    : NOT USED
```

La popolazione mutante di riferimento è quella già congelata per la stessa classe.

---

## 13. Identità della popolazione mutante

Risultato del population audit:

```text
Reference population    : 52
Current population      : 52
Population differences  : 0
Population identity     : PASS (52/52)
```

La mutation analysis di `T_ES` è quindi direttamente confrontabile con quella delle altre suite di `ListIteratorWrapper`.

---

## 14. Risultati PIT

Risultati:

```text
TOTAL                   : 52
KILLED                  : 29
SURVIVED                : 15
NO_COVERAGE             : 8
TIMED_OUT               : 0
RUN_ERROR               : 0
MEMORY_ERROR            : 0
```

Mutation Score:

```text
29 / 52 = 55.77%
```

Test Strength:

```text
29 / (29 + 15) = 65.91%
```

I mutanti raggiunti dalla suite sono:

```text
KILLED + SURVIVED
= 29 + 15
= 44
```

---

## 15. Confronto mutation T_RND vs T_ES

A parità di cardinalità e popolazione:

| Metrica | T_RND | T_ES | Delta T_ES |
|---|---:|---:|---:|
| Mutation Score | 11.54% | 55.77% | +44.23 pp |
| Test Strength | 24.00% | 65.91% | +41.91 pp |
| KILLED | 6 | 29 | +23 |
| SURVIVED | 19 | 15 | -4 |
| NO_COVERAGE | 27 | 8 | -19 |

EvoSuite raggiunge una porzione molto più ampia della popolazione mutante e discrimina un numero maggiore di mutanti rispetto alla suite random.

Anche questo risultato viene osservato soltanto dopo il freeze e non genera alcun ciclo di ottimizzazione.

---

## 16. Interpretazione

`T_ES` mostra una forte crescita rispetto a `T_RND` sia nella coverage di linee e branch sia nelle metriche mutation.

Risultati principali:

```text
LINE                    : 83.33%
BRANCH                  : 82.50%
METHOD                  : 81.82%

Mutation Score          : 55.77%
Test Strength           : 65.91%
```

La Method Coverage non segue lo stesso andamento delle altre metriche: pur ottenendo Line e Branch Coverage molto elevate, EvoSuite raggiunge 9 degli 11 metodi, mentre Randoop ne raggiunge 11.

Questo conferma che le metriche strutturali descrivono aspetti differenti dell'adeguatezza di una suite e non devono essere interpretate come intercambiabili.

La suite viene mantenuta integralmente come outcome sperimentale della tecnica coverage-guided.

---

## 17. Politica di freeze

La sequenza sperimentale applicata è:

```text
Protocol definition
        |
        v
Seed 0 generation
        |
        v
RAW validation (15/15)
        |
        v
Positional selection (first 12)
        |
        v
Canonical validation (12/12)
        |
        v
PRE-ADEQUACY FREEZE
        |
        +--> JaCoCo
        |
        +--> PIT
```

Non viene applicato alcun feedback:

```text
JaCoCo -> generation    : NO
PIT    -> generation    : NO
JaCoCo -> oracle edit   : NO
PIT    -> oracle edit   : NO
```

---

## 18. Evidence

Evidence principali:

```text
isw2/results/testing/list-iterator-wrapper/es/
```

Struttura:

```text
list-iterator-wrapper/es/
|
+-- listiteratorwrapper_evosuite_summary.txt
|
+-- audit/
|   +-- listiteratorwrapper_evosuite_freeze_audit.txt
|
+-- coverage/
|   +-- jacoco.csv
|   +-- jacoco.xml
|   +-- listiteratorwrapper_evosuite_coverage_summary.txt
|   +-- listiteratorwrapper_evosuite_jacoco.exec
|
+-- generation/
|   +-- listiteratorwrapper_evosuite_generation_summary.txt
|   +-- listiteratorwrapper_evosuite_seed0_run.txt
|
+-- mutation/
|   +-- listiteratorwrapper_evosuite_mutations.csv
|   +-- listiteratorwrapper_evosuite_mutations.xml
|   +-- listiteratorwrapper_evosuite_mutation_summary.txt
|   +-- listiteratorwrapper_evosuite_pit_run.txt
|
+-- validation/
    +-- listiteratorwrapper_evosuite_validation_summary.txt
```

Output RAW:

```text
isw2/testing/generated/listiteratorwrapper/evosuite/raw/seed-0/
```

Suite canonica:

```text
isw2/testing/src/test/java/
it/uniroma2/isw2/openjpa/testing/listiteratorwrapper/es/
```

---

## 19. Risultato finale

```text
Target                  : ListIteratorWrapper
Technique               : T_ES / EvoSuite
Generator               : EvoSuite 1.2.0
Runtime generation      : Zulu Java 11

RAW seed 0              : 15 / 15 PASS
Canonical selection     : first 12 / 15
Canonical tests         : 12
Canonical PASS          : 12 / 12

JaCoCo
------
LINE                    : 60 / 72 = 83.33%
BRANCH                  : 33 / 40 = 82.50%
METHOD                  : 9 / 11 = 81.82%

PIT
---
Population              : 52
KILLED                  : 29
SURVIVED                : 15
NO_COVERAGE             : 8
TIMED_OUT               : 0
RUN_ERROR               : 0
MEMORY_ERROR            : 0
Mutation Score          : 55.77%
Test Strength           : 65.91%
Population identity     : PASS (52/52)

Coverage feedback       : NONE
Mutation feedback       : NONE
Post-adequacy editing   : NONE

T_ES STATUS             : FROZEN
```

---

## 20. Handoff

Con `T_ES` congelata, la fase automatica successiva è:

```text
T_LLM
```

La suite `T_LLM` dovrà mantenere la stessa cardinalità sperimentale:

```text
N = 12
```

e dovrà essere congelata prima dell'osservazione di JaCoCo e PIT.

Il confronto finale tra tecniche automatiche verrà effettuato solo dopo il freeze di `T_RND`, `T_ES` e `T_LLM`.

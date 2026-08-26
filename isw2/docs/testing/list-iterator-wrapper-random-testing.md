# ListIteratorWrapper – Random Testing con Randoop

## 1. Obiettivo

Dopo il freeze delle suite manuali `T_BB`, `T_CF` e `T_MT`, è stata costruita una suite automatica indipendente mediante **Randoop** per la classe:

```text
org.apache.openjpa.lib.util.collections.ListIteratorWrapper
```

La suite automatica viene indicata come `T_RND`.

Per rendere il confronto coerente con la suite black-box iniziale, la cardinalità è stata fissata a `N = 12`, pari alla cardinalità finale di `T_BB`.

Coverage e mutation testing sono stati misurati **solo dopo il freeze della suite** e non sono stati utilizzati per selezionare, rigenerare o modificare i test.

## 2. Tool e configurazione

Generatore:

```text
Randoop 4.3.4
```

Versione osservata durante l'esecuzione:

```text
Randoop for Java version "4.3.4, local changes, branch master,
commit 96e7279, 2025-06-06"
```

Framework dei test generati:

```text
JUnit 4.13.2
```

Configurazione adottata:

```text
Target class             : org.apache.openjpa.lib.util.collections.ListIteratorWrapper
Target N                 : 12
Generated limit          : 20000
Output limit             : 12
Time limit               : 60 s per seed
Only public members      : YES
Error-revealing tests    : DISABLED
Deterministic seeds      : YES
Coverage feedback        : NONE
Mutation feedback        : NONE
Manual oracle editing    : NONE
Native OpenJPA tests     : NOT USED
```

Il bytecode production utilizzato appartiene a `org.apache.openjpa:openjpa-lib:4.1.1`.

## 3. Costruzione degli input

Il costruttore pubblico di `ListIteratorWrapper` richiede un oggetto `Iterator<? extends E>`. La sola classe target non fornisce quindi a Randoop un producer sufficiente per costruire facilmente istanze valide.

Per la generazione è stata aggiunta `java.util.ArrayList` esclusivamente come producer pubblico di oggetti `Iterator`:

```text
ArrayList
   |
   +--> iterator()
           |
           +--> ListIteratorWrapper(...)
```

`ArrayList` non costituisce un secondo target sperimentale. La classe richiesta come effettivamente coperta rimane `org.apache.openjpa.lib.util.collections.ListIteratorWrapper` tramite `require-covered-classes`.

## 4. Politica di cardinalità e multi-seed

La cardinalità finale è stata fissata prima di osservare JaCoCo o PIT:

```text
N = 12
```

La politica adottata è stata:

1. utilizzare seed deterministici consecutivi;
2. mantenere tutti i regression test validi prodotti dal primo seed;
3. se il numero di test è inferiore a `N`, utilizzare il seed successivo;
4. mantenere i test nell'ordine prodotto da Randoop;
5. interrompere la selezione appena raggiunto `N`;
6. non utilizzare coverage, mutation score o analisi semantica dei test come criterio di scelta.

In questo modo la composizione della suite non è influenzata dalle metriche osservate successivamente.

## 5. Generazione seed 0

Configurazione:

```text
Seed                    : 0
Time limit              : 60 s
Generated limit         : 20000
Output limit            : 12
```

Randoop ha prodotto:

```text
Steps                    : 572
Sequences generated      : 328
Candidate regressions    : 12
Regression tests finali  : 7
Error tests              : 0
Invalid tests            : 0
```

La suite RAW è stata compilata ed eseguita prima dell'integrazione:

```text
Tests run               : 7
PASS                    : 7
FAIL                    : 0

OK (7 tests)
```

Tutti i 7 test sono stati mantenuti.

## 6. Generazione seed 1

Poiché il seed 0 ha prodotto soltanto 7 test validi, è stato eseguito un secondo seed mantenendo invariato il protocollo.

Configurazione:

```text
Seed                    : 1
Time limit              : 60 s
Generated limit         : 20000
Output limit            : 12
```

Randoop ha prodotto:

```text
Steps                    : 368
Sequences generated      : 226
Candidate regressions    : 12
Regression tests finali  : 9
Error tests              : 0
Invalid tests            : 0
```

Validazione RAW:

```text
Tests run               : 9
PASS                    : 9
FAIL                    : 0

OK (9 tests)
```

## 7. Composizione canonica della suite

Per raggiungere `N = 12`, la suite finale è stata composta come segue:

```text
Seed 0                  : test1 ... test7 = 7
Seed 1                  : test1 ... test5 = 5
------------------------------------------------
T_RND                   : 12
```

I test `test6`, `test7`, `test8` e `test9` del seed 1 non sono stati esclusi in base alla loro qualità: ricadevano semplicemente oltre la cardinalità prefissata.

I file canonici sono:

```text
isw2/testing/src/test/java/
it/uniroma2/isw2/openjpa/testing/listiteratorwrapper/rnd/
├── ListIteratorWrapperRandoopSeed0Test.java
└── ListIteratorWrapperRandoopSeed1Test.java
```

Per l'integrazione sono stati modificati esclusivamente package, nome delle classi e pruning posizionale dei test `6..9` del seed 1. I corpi e gli oracle dei 12 test selezionati non sono stati modificati manualmente.

## 8. Validazione della suite canonica

La suite integrata è stata eseguita tramite Maven:

```text
Tests run               : 12
Failures                : 0
Errors                  : 0
Skipped                 : 0

BUILD SUCCESS
```

Pertanto:

```text
T_RND                   : 12 / 12 PASS
```

A questo punto la suite è stata congelata.

## 9. Coverage con JaCoCo

La coverage è stata misurata dopo il freeze mediante **JaCoCo 0.8.15** sul solo target `ListIteratorWrapper`.

| Metrica | Covered | Missed | Totale | Coverage |
|---|---:|---:|---:|---:|
| Line | 42 | 30 | 72 | 58.33% |
| Branch | 19 | 21 | 40 | 47.50% |
| Method | 11 | 0 | 11 | 100.00% |

La suite Randoop raggiunge quindi tutti gli 11 metodi della classe.

## 10. Confronto strutturale con T_BB

Il confronto è effettuato a parità di cardinalità (`N = 12`).

| Metrica | T_BB | T_RND | Delta T_RND |
|---|---:|---:|---:|
| Line Coverage | 52.78% | 58.33% | +5.55 pp |
| Branch Coverage | 45.00% | 47.50% | +2.50 pp |
| Method Coverage | 90.91% | 100.00% | +9.09 pp |

Randoop ottiene quindi una coverage strutturale leggermente superiore rispetto alla suite black-box della stessa cardinalità. Questo confronto è descrittivo e non è stato utilizzato per modificare `T_RND`.

## 11. Mutation testing

La mutation analysis è stata eseguita dopo il freeze con:

```text
PIT                     : 1.25.8
pitest-junit5-plugin    : 1.2.3
Mutators                : DEFAULTS
Threads                 : 1
```

Target:

```text
org.apache.openjpa.lib.util.collections.ListIteratorWrapper
```

Sono stati utilizzati esclusivamente i test canonici `T_RND`.

## 12. Identità del bytecode production

Per garantire la confrontabilità con la mutation analysis manuale è stato utilizzato lo stesso bytecode production di OpenJPA 4.1.1.

SHA-256:

```text
C06F2D6F83082E8CC538069769BA1C2241678054E9C27B0510A77C0ADCE4B0F4
```

## 13. Risultati PIT

La popolazione mutante è la stessa utilizzata per le suite manuali:

```text
TOTAL                   : 52
KILLED                  : 6
SURVIVED                : 19
NO_COVERAGE             : 27
TIMED_OUT               : 0
RUN_ERROR               : 0
MEMORY_ERROR            : 0
```

Mutation Score:

```text
6 / 52 = 11.54%
```

Test Strength:

```text
6 / (6 + 19) = 24.00%
```

La suite raggiunge quindi 25 mutanti (`6 KILLED + 19 SURVIVED`), dei quali 6 vengono uccisi.

## 14. Coverage interna PIT

PIT riporta anche:

```text
Line Coverage for mutated class : 45 / 72
```

Questa misura appartiene alla coverage interna utilizzata da PIT durante la mutation analysis e non sostituisce la misura strutturale canonica ottenuta con JaCoCo:

```text
LINE                    : 42 / 72 = 58.33%
BRANCH                  : 19 / 40 = 47.50%
METHOD                  : 11 / 11 = 100.00%
```

## 15. Interpretazione

La suite Randoop mostra una differenza tra **raggiungibilità strutturale** e **capacità discriminante degli oracle**.

Da un lato ottiene:

```text
Method Coverage         : 100.00%
Line Coverage           : 58.33%
Branch Coverage         : 47.50%
```

Dall'altro lato, la mutation analysis mostra:

```text
Mutants reached         : 25 / 52
Mutants killed          : 6
Test Strength           : 24.00%
```

Il risultato indica che raggiungere un metodo o una porzione di codice non implica automaticamente disporre di oracle sufficientemente forti da distinguere il programma originale dalle sue versioni mutate.

Dopo il freeze non sono stati aggiunti test, modificati oracle o eseguite rigenerazioni guidate dalle metriche.

## 16. Politica di freeze

La sequenza sperimentale è stata:

```text
Generation
    |
    v
RAW validation
    |
    v
Deterministic cardinality selection
    |
    v
Canonical execution
    |
    v
FREEZE
    |
    +--> JaCoCo
    |
    +--> PIT
```

Non è stato applicato alcun ciclo di feedback:

```text
JaCoCo -> generation    : NO
PIT    -> generation    : NO
```

## 17. Evidence

Evidence principali:

```text
isw2/results/testing/list-iterator-wrapper/rnd/
```

Struttura:

```text
list-iterator-wrapper/rnd/
|
+-- listiteratorwrapper_randoop_summary.txt
|
+-- audit/
|   +-- listiteratorwrapper_randoop_freeze_audit.txt
|
+-- coverage/
|   +-- jacoco.csv
|   +-- jacoco.xml
|   +-- listiteratorwrapper_randoop_coverage_summary.txt
|   +-- listiteratorwrapper_randoop_jacoco.exec
|
+-- generation/
|   +-- listiteratorwrapper_randoop_generation_metadata.txt
|
+-- mutation/
|   +-- listiteratorwrapper_randoop_mutations.csv
|   +-- listiteratorwrapper_randoop_mutations.xml
|   +-- listiteratorwrapper_randoop_mutation_summary.txt
|   +-- listiteratorwrapper_randoop_pit_run.txt
|
+-- validation/
    +-- listiteratorwrapper_randoop_raw_validation_summary.txt
```

Output RAW:

```text
isw2/testing/generated/listiteratorwrapper/randoop/
```

Suite canonica:

```text
isw2/testing/src/test/java/
it/uniroma2/isw2/openjpa/testing/listiteratorwrapper/rnd/
```

## 18. Risultato finale

```text
Target                  : ListIteratorWrapper
Technique               : T_RND / Randoop
Generator               : Randoop 4.3.4

Tests                   : 12
PASS                    : 12
FAIL                    : 0
Errors                  : 0
Skipped                 : 0

JaCoCo
------
LINE                    : 42 / 72 = 58.33%
BRANCH                  : 19 / 40 = 47.50%
METHOD                  : 11 / 11 = 100.00%

PIT
---
Population              : 52
KILLED                  : 6
SURVIVED                : 19
NO_COVERAGE             : 27
TIMED_OUT               : 0
RUN_ERROR               : 0
MEMORY_ERROR            : 0

Mutation Score          : 11.54%
Test Strength           : 24.00%

Coverage feedback       : NONE
Mutation feedback       : NONE
Manual oracle editing   : NONE

T_RND STATUS            : FROZEN
```

## 19. Handoff

Con `T_RND` congelata, la fase automatica successiva è:

```text
T_ES – EvoSuite
```

Il confronto definitivo tra le tecniche automatiche verrà effettuato soltanto dopo il freeze anche di `T_ES` e `T_LLM`.

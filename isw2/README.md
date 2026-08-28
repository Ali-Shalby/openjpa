# Progetto ISW2 – Apache OpenJPA

Progetto sviluppato per il corso di **Ingegneria del Software 2**
Università degli Studi di Roma Tor Vergata – A.A. 2025/2026.

Il progetto open-source assegnato è **Apache OpenJPA**.

Repository upstream:

`https://github.com/apache/openjpa`

Tutto il materiale sviluppato specificamente per il progetto universitario è raccolto nella directory:

```text
isw2/
```

Il codice e la documentazione originali di Apache OpenJPA vengono mantenuti separati dal materiale ISW2.

---

## Baseline

La baseline scelta è:

```text
Apache OpenJPA 4.1.1
```

Tag Git:

```text
4.1.1
```

Branch principali:

```text
baseline-4.1.1   → copia immutabile della baseline
isw2-project     → sviluppo del progetto ISW2
```

La baseline è stata verificata sia tramite build locale sia tramite la CI GitHub Actions originale di OpenJPA.

Per maggiori dettagli:

[`docs/setup.md`](docs/setup.md)

---

## Struttura del progetto

```text
isw2/
├── README.md
├── analyzer/
├── datasets/
├── docs/
│   └── testing/
├── results/
└── testing/
```

### `analyzer/`

Progetto Maven indipendente contenente gli strumenti sviluppati per automatizzare le attività delle milestone.

### `datasets/`

Dataset e cataloghi generati automaticamente.

### `results/`

Risultati sperimentali delle analisi.

### `docs/`

Documentazione metodologica dettagliata.

### `testing/`

Harness Maven indipendente dedicato agli esperimenti della parte De Angelis.
Le suite sperimentali vengono sviluppate senza riutilizzare i test già presenti
nel repository OpenJPA.

---

## Parte Falessi

Il progetto comprende:

1. **Milestone 1 – Dataset Creation**
2. **Milestone 2 – Classification**
3. **Milestone 3 – What-if Analysis**
4. **Milestone 4 – Automated Refactoring**

La granularità utilizzata è la **classe Java**.

---

## Parte De Angelis

La parte di Software Testing viene svolta su due classi OpenJPA e comprende:

* Category Partition;
* test manuali;
* random testing;
* generazione guidata dalla coverage;
* test generati tramite LLM;
* code coverage;
* mutation testing;
* miglioramento delle suite;
* reliability;
* confronto dei test sulle versioni refactored.

Per la suite manuale iniziale viene adottato un approccio **black-box**:
le categorie vengono ricavate prima dalla documentazione e dal contratto pubblico
della classe. Coverage, mutation testing, code smell e dettagli del controllo di
flusso non vengono usati per costruire retroattivamente la suite iniziale.

Le due classi selezionate per la parte De Angelis sono:

```text
org.apache.openjpa.enhance.PCEnhancer
org.apache.openjpa.lib.util.collections.ListIteratorWrapper
```

Per entrambe le classi la suite manuale iniziale `T_BB` viene derivata tramite
Category Partition e congelata prima di osservare coverage e mutation score.
Le suite automatiche vengono inoltre congelate prima delle misure di adequacy,
così da evitare feedback retroattivi da JaCoCo o PIT.

Stato corrente:

```text
T_BB             : 30 test, FROZEN
T_BB outcome     : 30 PASS, 0 FAIL
T_BB LINE        : 43.31% (1169 / 2699)
T_BB BRANCH      : 30.24% (368 / 1217)

T_CF additions   : 5 test, FROZEN
Suite post-T_CF  : 35 test
T_CF outcome     : 35 PASS, 0 FAIL
T_CF LINE        : 70.47% (1902 / 2699)
T_CF BRANCH      : 54.89% (668 / 1217)

T_MT additions   : 5 test, FROZEN
Suite manuale    : 40 test
Outcome finale   : 40 PASS, 0 FAIL
PIT population   : 1700 mutanti
Raw KILLED       : 828
Raw SURVIVED     : 355
NO_COVERAGE      : 515
TIMED_OUT        : 2
Mutation Score   : 48.71%
Test Strength    : 69.99%

T_RND            : 30 test, FROZEN
T_RND outcome    : 30 PASS, 0 FAIL
T_RND LINE       : 1.96% (53 / 2699)
T_RND BRANCH     : 0.82% (10 / 1217)
T_RND PIT        : 1700 mutanti
T_RND KILLED     : 2
T_RND SURVIVED   : 3
T_RND NO_COVERAGE: 1695
T_RND TIMED_OUT  : 0
T_RND Mut. Score : 0.12%
T_RND Strength   : 40.00%

T_ES             : 30 test, FROZEN
T_ES outcome     : 30 PASS, 0 FAIL
T_ES LINE        : 3.00% (81 / 2699)
T_ES BRANCH      : 1.56% (19 / 1217)
T_ES PIT         : 1700 mutanti, identity 1700/1700
T_ES KILLED      : 10
T_ES SURVIVED    : 4
T_ES NO_COVERAGE : 1686
T_ES TIMED_OUT   : 0
T_ES Mut. Score  : 0.59%
T_ES Strength    : 71.43%

T_LLM            : 30 test, FROZEN
T_LLM outcome    : 30 PASS, 0 FAIL
T_LLM stability  : 5/5 PASS
T_LLM LINE       : 4.41% (119 / 2699)
T_LLM BRANCH     : 3.53% (43 / 1217)
T_LLM PIT        : 1700 mutanti
T_LLM KILLED     : 39
T_LLM SURVIVED   : 6
T_LLM NO_COVERAGE: 1655
T_LLM TIMED_OUT  : 0
T_LLM Mut. Score : 2.29%
T_LLM Strength   : 86.67%
```

Per `ListIteratorWrapper`, le tre suite automatiche same-cardinality
(`N = 12`) risultano completate:

```text
T_RND LINE       : 58.33% (42 / 72)
T_RND BRANCH     : 47.50% (19 / 40)
T_RND METHOD     : 100.00% (11 / 11)
T_RND KILLED     : 6 / 52
T_RND Mut. Score : 11.54%
T_RND Strength   : 24.00%

T_ES LINE        : 83.33% (60 / 72)
T_ES BRANCH      : 82.50% (33 / 40)
T_ES METHOD      : 81.82% (9 / 11)
T_ES KILLED      : 29 / 52
T_ES Mut. Score  : 55.77%
T_ES Strength    : 65.91%

T_LLM            : 12 test, FROZEN
T_LLM outcome    : 12 PASS, 0 FAIL
T_LLM stability  : 5/5 PASS
T_LLM LINE       : 100.00% (72 / 72)
T_LLM BRANCH     : 92.50% (37 / 40)
T_LLM METHOD     : 100.00% (11 / 11)
T_LLM PIT        : 52 mutanti, identity 52/52
T_LLM KILLED     : 47
T_LLM SURVIVED   : 5
T_LLM NO_COVERAGE: 0
T_LLM Mut. Score : 90.38%
T_LLM Strength   : 90.38%
```

`TBB-026` rappresenta il frame F8 relativo alla validità della configurazione
del tool. Il caso usa la proprietà documentata `RuntimeUnenhancedClasses` con
il valore deliberatamente invalido `definitely-invalid`; la configurazione
viene rifiutata con `ParseException`. Il frame è parte della Category Partition
congelata e non deriva da feedback di coverage o mutation.

Documentazione:
* [`docs/testing/pcenhancer-black-box.md`](docs/testing/pcenhancer-black-box.md)
* [`docs/testing/pcenhancer-control-flow.md`](docs/testing/pcenhancer-control-flow.md)
* [`docs/testing/pcenhancer-mutation-testing.md`](docs/testing/pcenhancer-mutation-testing.md)
* [`docs/testing/pcenhancer-random-testing.md`](docs/testing/pcenhancer-random-testing.md)
* [`docs/testing/pcenhancer-evosuite-testing.md`](docs/testing/pcenhancer-evosuite-testing.md)
* [`docs/testing/pcenhancer-llm-testing.md`](docs/testing/pcenhancer-llm-testing.md)

---

## Tecnologie principali

| Tecnologia      | Utilizzo                             |
| --------------- | ------------------------------------ |
| Java            | Analyzer e testing                   |
| Maven           | Build                                |
| Git / GitHub    | Versionamento e analisi della storia |
| GitHub Actions  | Continuous Integration               |
| Apache JIRA     | Release e ticket                     |
| Weka            | Machine Learning                     |
| SonarQube Cloud | Code smell                           |
| JUnit           | Testing                              |
| JaCoCo          | Coverage                             |
| PIT             | Mutation testing                     |
| Randoop         | Random test generation               |
| EvoSuite        | Coverage-based generation            |
| LLM / Copilot   | Test generation e refactoring        |

Gli strumenti vengono documentati nel dettaglio quando effettivamente introdotti.

---

## Stato di avanzamento

### Completato

* [x] Fork di Apache OpenJPA
* [x] Configurazione `origin` e `upstream`
* [x] Selezione della baseline OpenJPA 4.1.1
* [x] Creazione branch `baseline-4.1.1`
* [x] Creazione branch `isw2-project`
* [x] Validazione della baseline
* [x] Configurazione GitHub Actions
* [x] Creazione del modulo `isw2/analyzer`
* [x] Recupero delle release OpenJPA da JIRA
* [x] Generazione del catalogo RAW delle release
* [x] Identificazione delle release stabili
* [x] Associazione release → commit Git
* [x] Selezione delle 12 release del Dataset A
* [x] Identificazione e classificazione delle classi Java production
* [x] Generazione e validazione delle metriche di classe
* [x] Calcolo e validazione di `NSmells`
* [x] Recupero e validazione dei defect ticket JIRA
* [x] Identificazione dei fix commit production
* [x] Calcolo e validazione di `NFIX`
* [x] Pilot e FULL SZZ
* [x] Validazione delle evidence SZZ e dei bug-introducing commit
* [x] Calcolo e validazione di `Proportion Total`
* [x] Determinazione di `EffectiveIV` ed `EffectiveFV`
* [x] Ricostruzione dei lifecycle `[IV,FV)`
* [x] Generazione e validazione della `Bugginess`
* [x] Audit dell'andamento della `Bugginess`
* [x] Verifica mirata del caso `OPENJPA-896`
* [x] Assemblaggio del Dataset A
* [x] Validazione finale delle 12.836 osservazioni
* [x] Milestone 1 – Dataset Creation
* [x] Pipeline di classificazione Weka
* [x] Feature Selection con `CfsSubsetEval + BestFirst`
* [x] Balancing con SMOTE training-only
* [x] Validazione `10 times 10-folds`
* [x] Confronto RandomForest / NaiveBayes / IBk
* [x] Calcolo Precision / Recall / AUC / Kappa / NPofB20
* [x] Milestone 2 – Classification
* [x] Selezione `BClassifier = RandomForest`
* [x] Costruzione dataset M3 `B+`, `B`, `C`
* [x] Training `BClassifierA` su Dataset A
* [x] What-if analysis su A / B+ / B / C
* [x] Stima classi buggy potenzialmente prevenibili
* [x] Milestone 3 – What-if Analysis
* [x] Creazione dell'harness Maven indipendente `isw2/testing`
* [x] Selezione di `PCEnhancer` come prima classe della parte De Angelis
* [x] Category Partition black-box di `PCEnhancer`
* [x] Freeze audit della suite manuale iniziale `T_BB` (`N = 30`)
* [x] Implementazione completa `T_BB` – 30 test
* [x] Full regression `T_BB` – 30 PASS, 0 FAIL
* [x] Audit finale e traceability degli oracle `T_BB`
* [x] Baseline JaCoCo `T_BB` – 43.31% Line / 30.24% Branch
* [x] Coverage-gap audit pre-`T_CF`
* [x] Implementazione `T_CF` – 5 test coverage-guided
* [x] Feasibility preflight dei candidati complessi `TCF-003..005`
* [x] Coverage cumulativa finale – 70.47% Line / 54.89% Branch
* [x] Final gap audit e stopping rule (`TCF-006` non pianificato)
* [x] Freeze audit `T_CF` – 5 test, 6 fixture, manifest SHA-256
* [x] Mutation baseline sulla suite `T_BB + T_CF` – 1700 mutanti
* [x] Survivor analysis e `TMT-001` – Application Identity runtime semantics
* [x] `TMT-002` – Externalization runtime round-trip semantics
* [x] `TMT-003` – Standard Java Serialization runtime round-trip
* [x] `TMT-004` – PersistenceCapable / StateManager runtime semantics
* [x] `TMT-005` – Relationship-valued / derived identity runtime semantics
* [x] Final PIT – 828 KILLED / 355 SURVIVED / 515 NO_COVERAGE / 2 TIMED_OUT
* [x] Clean full regression – 40 test, 40 PASS, 0 FAIL
* [x] Freeze `T_MT` – Mutation Score 48.71% / Test Strength 69.99%
* [x] Protocollo Randoop 4.3.4 – `N = 30`, seed `0`, generazione deterministica
* [x] Generazione `T_RND` – 30 regression test
* [x] Validazione RAW `T_RND` – 30 PASS, 0 FAIL
* [x] Integrazione JUnit 4 tramite JUnit Vintage – 30 PASS
* [x] JaCoCo isolato `T_RND` – 1.96% Line / 0.82% Branch
* [x] PIT isolato `T_RND` – 2 KILLED / 3 SURVIVED / 1695 NO_COVERAGE
* [x] Freeze `T_RND` – Mutation Score 0.12% / Test Strength 40.00%
* [x] Protocollo EvoSuite 1.2.0 – `N = 30`, criteri `LINE:BRANCH`, budget 120 s per seed
* [x] Generazione multi-seed `T_ES` – seed 0 = 15 test, seed 1 = 18 test
* [x] Freeze `T_ES` – 15 test dal seed 0 + primi 15 dal seed 1 = 30 test
* [x] Validazione isolata Java 11 `T_ES` – 30 PASS, 0 FAIL
* [x] JaCoCo isolato `T_ES` – 3.00% Line / 1.56% Branch
* [x] PIT isolato `T_ES` – popolazione identica 1700/1700, 10 KILLED / 4 SURVIVED / 1686 NO_COVERAGE
* [x] Freeze `T_ES` – Mutation Score 0.59% / Test Strength 71.43%
* [x] Protocollo single-prompt `T_LLM` – `N = 30`, Java 21, JUnit Jupiter
* [x] Generazione `T_LLM` – 30 scenari `TLLM-001 ... TLLM-030`
* [x] Validazione `T_LLM` – 30 PASS, 0 FAIL / stabilità 5/5 PASS
* [x] JaCoCo isolato `T_LLM` – 4.41% Line / 3.53% Branch
* [x] PIT isolato `T_LLM` – 39 KILLED / 6 SURVIVED / 1655 NO_COVERAGE
* [x] Freeze `T_LLM` – Mutation Score 2.29% / Test Strength 86.67%
* [x] Selezione di `ListIteratorWrapper` come seconda classe della parte De Angelis
* [x] Category Partition black-box di `ListIteratorWrapper`
* [x] Freeze `ListIteratorWrapper T_BB` – 12 test, 12 PASS
* [x] JaCoCo `ListIteratorWrapper T_BB` – 52.78% Line / 45.00% Branch / 90.91% Method
* [x] Implementazione `ListIteratorWrapper T_CF` – 5 test coverage-guided
* [x] Coverage cumulativa `T_BB + T_CF` – 98.61% Line / 87.50% Branch / 100.00% Method
* [x] PIT baseline `ListIteratorWrapper` – 46 KILLED / 4 SURVIVED / 2 NO_COVERAGE
* [x] Implementazione `ListIteratorWrapper T_MT` – 2 test mutation-guided
* [x] PIT post-`T_MT` – 50 KILLED / 2 SURVIVED / 0 NO_COVERAGE
* [x] Freeze suite manuale `ListIteratorWrapper` – 19 test, 19 PASS
* [x] Generazione multi-seed `ListIteratorWrapper T_RND` – seed 0 = 7 test, seed 1 = 9 test
* [x] Freeze `ListIteratorWrapper T_RND` – 7 test seed 0 + primi 5 seed 1 = 12 test
* [x] Validazione `ListIteratorWrapper T_RND` – 12 PASS, 0 FAIL
* [x] JaCoCo `ListIteratorWrapper T_RND` – 58.33% Line / 47.50% Branch / 100.00% Method
* [x] PIT `ListIteratorWrapper T_RND` – 6 KILLED / 19 SURVIVED / 27 NO_COVERAGE
* [x] Freeze `ListIteratorWrapper T_RND` – Mutation Score 11.54% / Test Strength 24.00%
* [x] Protocollo `ListIteratorWrapper T_ES` – EvoSuite 1.2.0, `N = 12`, `LINE:BRANCH`, budget 120 s
* [x] Generazione `ListIteratorWrapper T_ES` – seed 0 = 15 test
* [x] Validazione RAW `ListIteratorWrapper T_ES` – 15 PASS, 0 FAIL
* [x] Freeze `ListIteratorWrapper T_ES` – primi 12/15 test, 12 PASS
* [x] JaCoCo `ListIteratorWrapper T_ES` – 83.33% Line / 82.50% Branch / 81.82% Method
* [x] PIT `ListIteratorWrapper T_ES` – popolazione identica 52/52, 29 KILLED / 15 SURVIVED / 8 NO_COVERAGE
* [x] Freeze `ListIteratorWrapper T_ES` – Mutation Score 55.77% / Test Strength 65.91%
* [x] Protocollo single-prompt `ListIteratorWrapper T_LLM` – `N = 12`, Java 21, JUnit Jupiter
* [x] Generazione `ListIteratorWrapper T_LLM` – 12 scenari `TLLM-001 ... TLLM-012`
* [x] Repair pre-freeze `ListIteratorWrapper T_LLM` – catena `R1 -> R2 -> R3`
* [x] Validazione `ListIteratorWrapper T_LLM` – 12 PASS, 0 FAIL / stabilità 5/5 PASS
* [x] Freeze `ListIteratorWrapper T_LLM` – SHA-256 canonico registrato prima di JaCoCo e PIT
* [x] JaCoCo `ListIteratorWrapper T_LLM` – 100.00% Line / 92.50% Branch / 100.00% Method
* [x] PIT `ListIteratorWrapper T_LLM` – popolazione identica 52/52, 47 KILLED / 5 SURVIVED / 0 NO_COVERAGE
* [x] Freeze `ListIteratorWrapper T_LLM` – Mutation Score 90.38% / Test Strength 90.38%

### In corso

* [ ] Reliability di `PCEnhancer`

### Successivamente

* [ ] Completamento Software Testing – De Angelis
* [ ] Milestone 4 – Automated Refactoring

---

## Output principali della Milestone 1

Tra gli output già generati e validati:

```text
isw2/datasets/release_catalog.csv
isw2/datasets/java_class_inventory.csv
isw2/datasets/class_metrics.csv
isw2/datasets/sonar_smell_metrics.csv
isw2/datasets/class_metrics_with_smells.csv
isw2/datasets/defect_ticket_catalog_raw.csv
isw2/datasets/fix_commit_catalog.csv
isw2/datasets/nfix_metrics.csv
isw2/datasets/szz_evidence.csv
isw2/datasets/bugginess_labels.csv
isw2/datasets/openjpa_dataset_a.csv
```

L'audit dei fix analizzati da SZZ è disponibile in:

```text
isw2/results/szz/szz_fix_audit.csv
```

Il calcolo di `Proportion Total` ha prodotto:

```text
P_TOTAL = 1.9688220484114205
```

Il labeling finale della `Bugginess` contiene:

```text
Observations : 12836
BUGGY=YES    : 2010
BUGGY=NO     : 10826
Buggy rate   : 15.66%
```

La `Bugginess` è stata validata anche rispetto all'andamento tra le release.
In particolare, il picco della release 10 è stato ricondotto principalmente
a `OPENJPA-896`, un defect cross-cutting relativo ai caratteri di fine riga
che coinvolge un numero elevato di file sorgente.

Gli audit principali della fase di labeling sono disponibili in:

```text
isw2/results/labeling/bugginess_trend_diagnostic.txt
isw2/results/labeling/openjpa_896_audit.txt
isw2/results/dataset/dataset_a_validation.txt
```

Il Dataset A finale è disponibile in:

```text
isw2/datasets/openjpa_dataset_a.csv
```

ed è stato generato tramite `DatasetAGenerator` unendo:

```text
class_metrics_with_smells.csv
nfix_metrics.csv
bugginess_labels.csv
```

La validazione finale ha prodotto:

```text
Rows                : 12836
Unique observations : 12836
Releases            : 12
Feature columns     : 18
Sum(NSmells)        : 94308
Sum(NFIX)           : 7523
BUGGY=YES           : 2010
BUGGY=NO            : 10826
ValidationPassed    : True
```

---

## Milestone 2 – Classification

La Milestone 2 confronta:

```text
RandomForest
NaiveBayes
IBk
```

utilizzando:

```text
Precision
Recall
AUC
Kappa
NPofB20
```

con validazione:

```text
10 times 10-folds
```

Sono state confrontate quattro configurazioni:

```text
C1 = no Feature Selection / no balancing
C2 = Feature Selection / no balancing
C3 = no Feature Selection / SMOTE
C4 = Feature Selection / SMOTE
```

La Feature Selection utilizza:

```text
CfsSubsetEval + BestFirst
```

e SMOTE viene applicato esclusivamente ai training fold.

Il run FULL ha prodotto:

```text
1200 model training
120 risultati raw
12 configurazioni finali aggregate
```

I vincitori per metrica sono:

```text
Precision -> C1 RandomForest = 0.842343
Recall    -> C3 RandomForest = 0.793881
AUC       -> C3 RandomForest = 0.963241
Kappa     -> C3 RandomForest = 0.750842
NPofB20   -> C1 RandomForest = 0.691393
```

Il classificatore selezionato è quindi:

```text
BClassifier = RandomForest
```

Gli output finali versionati sono:

```text
isw2/results/m2/full/classifier_metrics_full.csv
isw2/results/m2/full/experiment_validation_full.txt
isw2/results/m2/summary/classifier_summary_full.csv
isw2/results/m2/summary/summary_validation_full.txt
```

I risultati intermedi QUICK, fold plan e audit completi sono rigenerabili
tramite l'analyzer e non vengono versionati.

---


## Milestone 3 – What-if Analysis

La Milestone 3 utilizza il classificatore selezionato nella Milestone 2:

```text
BClassifier = RandomForest
```

e costruisce i dataset:

```text
B+ = osservazioni di A con NSmells > 0
C  = osservazioni di A con NSmells = 0
B  = copia di B+ con NSmells impostato a 0
```

Per OpenJPA:

```text
A  = 12836 osservazioni
B+ = 8933 osservazioni
B  = 8933 osservazioni
C  = 3903 osservazioni
```

Il modello finale:

```text
BClassifierA = RandomForest addestrato sull'intero Dataset A
```

utilizza tutti i 18 predictor, senza Feature Selection e senza SMOTE, così da
mantenere esplicitamente `NSmells` nel modello.

La tabella finale del what-if è:

```text
Dataset   Actual BUGGY   Estimated BUGGY
A             2010            2010
B+            1723            1723
B                -            1300
C              287             287
```

Nel passaggio controfattuale `B+ -> B`:

```text
predicted YES -> NO = 425
predicted NO  -> YES =   2
```

La riduzione netta stimata è quindi:

```text
423 classi buggy
```

equivalente a:

```text
21.04% di tutte le classi BUGGY di A
24.55% delle classi BUGGY appartenenti a B+
```

Gli output principali versionati sono:

```text
isw2/datasets/openjpa_m3_bplus.csv
isw2/datasets/openjpa_m3_b.csv
isw2/datasets/openjpa_m3_c.csv

isw2/results/m3/what_if_prediction_summary.csv
isw2/results/m3/what_if_validation.txt
isw2/results/m3/what_if_result.csv
isw2/results/m3/what_if_result_validation.txt
```

Gli artefatti diagnostici e le prediction riga-per-riga rimangono rigenerabili
tramite l'analyzer e non vengono versionati.

---


## Software Testing – PCEnhancer

### Suite manuale black-box `T_BB`

La Category Partition iniziale è costruita sul contratto pubblico di
`PCEnhancer` e congelata prima di osservare coverage e mutation score.

```text
N = 30
```

Il frame `TBB-026` verifica una configurazione documentata con valore invalido:

```text
RuntimeUnenhancedClasses = definitely-invalid
```

Oracle:

```text
ParseException
```

Full regression canonica:

```text
Tests run : 30
PASS      : 30
FAIL      : 0
Errors    : 0
Skipped   : 0
```

Baseline JaCoCo sulla sola classe esterna
`org.apache.openjpa.enhance.PCEnhancer`:

```text
LINE   : 43.31% (1169 / 2699)
BRANCH : 30.24% (368 / 1217)
METHOD : 65.64% (107 / 163)
```

Evidence:

```text
isw2/results/testing/pcenhancer/tbb/runs/pcenhancer_tbb_full_run.txt
isw2/results/testing/pcenhancer/tbb/coverage/
```

Documentazione dettagliata:

[`docs/testing/pcenhancer-black-box.md`](docs/testing/pcenhancer-black-box.md)

### Suite manuale coverage-guided `T_CF`

Dopo il freeze di `T_BB`, il gap audit guida l'aggiunta di cinque scenari
manuali:

```text
TCF-001 Application Identity
TCF-002 Detached-state Externalization
TCF-003 Standard Serialization
TCF-004 Relationship-valued Identity
TCF-005 Optimized IdClass Copy
```

Suite cumulativa:

```text
T_BB             : 30
T_CF additions   : 5
Total            : 35
PASS             : 35
FAIL             : 0
Errors           : 0
Skipped          : 0
```

Coverage cumulativa:

```text
LINE   : 70.47% (1902 / 2699)
BRANCH : 54.89% (668 / 1217)
METHOD : 86.50% (141 / 163)
```

Contributo di `T_CF` rispetto a `T_BB`:

```text
Covered lines    : +733
Covered branches : +300
LINE delta       : +27.16 pp
BRANCH delta     : +24.65 pp
```

Il final gap audit applica una stopping rule esplicita: i gap residui non sono
sufficienti, da soli, a giustificare ulteriori micro-test. `TCF-006` non viene
pianificato e `T_CF` è congelata.

Evidence:

```text
isw2/results/testing/pcenhancer/tcf/
```

Documentazione dettagliata:

[`docs/testing/pcenhancer-control-flow.md`](docs/testing/pcenhancer-control-flow.md)

### Mutation testing e suite mutation-guided `T_MT`

La mutation analysis viene applicata solo dopo il freeze di `T_BB` e `T_CF`.

Protocollo:

```text
PIT                 : 1.25.8
PIT JUnit 5 plugin  : 1.2.3
Mutators            : DEFAULTS
Threads             : 1
Mutation population : 1700
Target              : org.apache.openjpa.enhance.PCEnhancer
Native OpenJPA tests: NOT USED
```

Baseline mutation su `T_BB + T_CF`:

```text
KILLED         : 466
SURVIVED       : 714
NO_COVERAGE    : 520
TIMED_OUT      : 0
Mutation Score : 27.41%
Test Strength  : 39.49%
```

L'analisi dei survivor guida cinque aggiunte comportamentali:

```text
TMT-001 Application Identity runtime object-id semantics
TMT-002 Detached-state Externalization runtime round-trip
TMT-003 Standard Java Serialization runtime round-trip
TMT-004 PersistenceCapable / StateManager runtime field semantics
TMT-005 Relationship-valued / derived identity runtime semantics
```

Checkpoint canonici disponibili:

| Stage | KILLED | SURVIVED | NO_COVERAGE | TIMED_OUT | Mutation Score | Test Strength | Δ KILLED |
|---|---:|---:|---:|---:|---:|---:|---:|
| Baseline | 466 | 714 | 520 | 0 | 27.41% | 39.49% | – |
| Post TMT-001 | 633 | 551 | 515 | 1 | 37.24% | 53.46% | +167 |
| Post TMT-002 | 719 | 465 | 515 | 1 | 42.29% | 60.73% | +86 |
| Final TMT-001..005 | 828 | 355 | 515 | 2 | 48.71% | 69.99% | +109 vs TMT-002 |

Risultato finale:

```text
Population          : 1700
KILLED              : 828
SURVIVED            : 355
NO_COVERAGE         : 515
TIMED_OUT           : 2
RUN_ERROR           : 0
MEMORY_ERROR        : 0
Mutation Score      : 48.71%
Test Strength       : 69.99%

Additional KILLED   : +362
SURVIVED reduction  : -359
NO_COVERAGE delta   : -5
TIMED_OUT delta     : +2
```

La console PIT può riportare un conteggio aggregato che comprende i timeout;
le metriche canoniche usano gli status raw dell'XML:

```text
828 KILLED
2 TIMED_OUT
```

Regressione manuale definitiva da build pulita:

```text
T_BB             : 30, FROZEN
T_CF additions   : 5, FROZEN
T_MT additions   : 5, FROZEN
Manual suite     : 40
PASS             : 40
FAIL             : 0
Errors           : 0
Skipped          : 0
T_MT STATUS      : FROZEN
```

La stopping rule non richiede l'azzeramento dei survivor. `TMT-006` non viene
pianificato: i 355 survivor residui vengono conservati come risultato
sperimentale senza classificarli automaticamente come equivalenti.

Evidence:

```text
isw2/results/testing/pcenhancer/mutation/
```

Documentazione dettagliata:

[`docs/testing/pcenhancer-mutation-testing.md`](docs/testing/pcenhancer-mutation-testing.md)

### Suite automatica random `T_RND`

Dopo il freeze delle suite manuali è stata generata una suite automatica
indipendente tramite Randoop 4.3.4.

Il protocollo è stato fissato prima di osservare le metriche:

```text
Target                  : org.apache.openjpa.enhance.PCEnhancer
N                       : 30
Random seed             : 0
Deterministica          : SI
Time limit              : 0
Generated limit         : 20000
Output limit            : 30
Solo membri pubblici    : SI
Error-revealing test    : DISABILITATI
Feedback coverage       : NESSUNO
Feedback mutation       : NESSUNO
Modifica manuale oracle : NESSUNA
```

La generazione canonica ha prodotto direttamente 30 regression test.

La suite RAW è stata compilata ed eseguita senza modifica degli oracle.
Successivamente è stata integrata nell'harness Maven tramite JUnit Vintage,
mantenendo invariati input, assertion ed eccezioni attese.

Risultato della suite integrata:

```text
T_RND                   : 30
PASS                    : 30
FAIL                    : 0
Errors                  : 0
Skipped                 : 0
```

Adeguatezza JaCoCo sulla sola classe esterna `PCEnhancer`:

```text
LINE                    : 1.96% (53 / 2699)
BRANCH                  : 0.82% (10 / 1217)
```

Mutation testing isolato sulla stessa popolazione congelata:

```text
Population              : 1700
KILLED                  : 2
SURVIVED                : 3
NO_COVERAGE             : 1695
TIMED_OUT               : 0
Mutation Score          : 0.12%
Test Strength           : 40.00%
```

La bassa adequacy viene mantenuta come risultato sperimentale della
generazione random. La suite non viene rigenerata né modificata dopo
l'osservazione di JaCoCo o PIT.

```text
T_RND STATUS            : FROZEN
```

Evidence principali:

```text
isw2/results/testing/pcenhancer/rnd/
```

Documentazione dettagliata:

[`docs/testing/pcenhancer-random-testing.md`](docs/testing/pcenhancer-random-testing.md)

### Suite automatica coverage-guided `T_ES`

Dopo il freeze di `T_RND` è stata costruita una seconda suite automatica
indipendente tramite EvoSuite 1.2.0.

Il protocollo è stato fissato prima di osservare le metriche:

```text
Target                  : org.apache.openjpa.enhance.PCEnhancer
N                       : 30
Criterion               : LINE:BRANCH
Search budget           : 120 s per seed
Stopping condition      : MAXTIME
Test format             : JUnit 4
Minimization            : enabled
Max suite size          : 30
Runtime EvoSuite        : Zulu JDK 11
Feedback coverage       : NESSUNO
Feedback mutation       : NESSUNO
Modifica manuale oracle : NESSUNA
```

La cardinalità finale è stata ottenuta mediante seed deterministici consecutivi:

```text
Seed 0                  : 15 test finali
Seed 1                  : 18 test finali
T_ES finale             : 15 + primi 15 del seed 1 = 30
```

Gli ultimi tre test del seed 1 non vengono inclusi. La selezione è
puramente posizionale e non usa JaCoCo o PIT.

Per l'integrazione sono state mantenute separate le due infrastrutture di
scaffolding generate da EvoSuite. Un bridge esclusivamente di test,
`PCEnhancerTestAccess`, delega agli helper package-protected necessari senza
modificare il production code e senza introdurre nuovi oracle.

EvoSuite 1.2.0 richiede, per questa suite, un runtime tecnico Java 11.
Il normale harness Maven del progetto resta su Java 21 ed esclude `T_ES`
dalla compilazione standard; la suite EvoSuite viene validata e misurata in
un'esecuzione isolata Java 11.

Risultato della suite congelata:

```text
T_ES                    : 30
PASS                    : 30
FAIL                    : 0
Errors                  : 0
Skipped                 : 0
```

Adeguatezza JaCoCo sulla sola classe esterna `PCEnhancer`:

```text
LINE                    : 3.00% (81 / 2699)
BRANCH                  : 1.56% (19 / 1217)
```

Mutation testing isolato sulla stessa popolazione congelata:

```text
Population              : 1700
Population identity     : 1700/1700
KILLED                  : 10
SURVIVED                : 4
NO_COVERAGE             : 1686
TIMED_OUT               : 0
Mutation Score          : 0.59%
Test Strength           : 71.43%
```

Le metriche vengono osservate soltanto dopo il freeze della suite e non
vengono utilizzate per rigenerare o selezionare ulteriormente i test.

```text
T_ES STATUS             : FROZEN
```

Evidence principali:

```text
isw2/results/testing/pcenhancer/es/
```

Documentazione dettagliata:

[`docs/testing/pcenhancer-evosuite-testing.md`](docs/testing/pcenhancer-evosuite-testing.md)

### Suite automatica LLM `T_LLM`

Dopo il freeze di `T_ES` è stata costruita una terza suite automatica
indipendente tramite LLM, mantenendo la stessa cardinalità sperimentale:

```text
N = 30
```

Il protocollo è stato fissato prima di osservare le metriche di adequacy:

```text
Target                  : org.apache.openjpa.enhance.PCEnhancer
N                       : 30
Framework               : JUnit Jupiter
Runtime                 : Java 21
LLM client              : Microsoft Copilot
Interaction mode        : browser chat
Model                   : GPT 5.6 Think Deeper
Feedback coverage       : NESSUNO
Feedback mutation       : NESSUNO
Modifica post-adequacy  : NESSUNA
```

Il prompt principale ha richiesto nella stessa risposta la progettazione di
30 scenari `TLLM-001 ... TLLM-030` e la relativa implementazione.

Durante l'integrazione sono stati effettuati soltanto repair tecnici di
rendering/completamento dell'output e di compatibilità Mockito sui generic,
senza modificare gli oracle sulla base di JaCoCo o PIT.

La suite è stata validata e congelata prima delle misure di adequacy:

```text
T_LLM                  : 30
PASS                   : 30
FAIL                   : 0
Errors                 : 0
Skipped                : 0
Stability              : 5/5 PASS
```

Adeguatezza JaCoCo sulla sola classe esterna `PCEnhancer`:

```text
LINE                   : 4.41% (119 / 2699)
BRANCH                 : 3.53% (43 / 1217)
```

Mutation testing isolato sulla stessa configurazione PIT utilizzata per il
confronto delle suite automatiche:

```text
Population             : 1700
KILLED                 : 39
SURVIVED               : 6
NO_COVERAGE            : 1655
TIMED_OUT              : 0
Mutation Score         : 2.29%
Test Strength          : 86.67%
```

JaCoCo e PIT restano misure esclusivamente post-freeze. I survivor non vengono
utilizzati per modificare o rigenerare `T_LLM`.

```text
T_LLM STATUS           : FROZEN / COMPLETE
```

Evidence principali:

```text
isw2/testing/llm/pcenhancer/
isw2/results/testing/pcenhancer/llm/
```

Documentazione dettagliata:

[`docs/testing/pcenhancer-llm-testing.md`](docs/testing/pcenhancer-llm-testing.md)

Prossima fase:

```text
Reliability di PCEnhancer
```

---

## Software Testing – ListIteratorWrapper

La seconda classe selezionata per la parte De Angelis è:

```text
org.apache.openjpa.lib.util.collections.ListIteratorWrapper
```

La classe è stata scelta dopo un audit di testing suitability: il candidato
automatico precedente risultava troppo semplice per sostenere in modo
significativo Category Partition, controllo di flusso e mutation-guided
improvement. `ListIteratorWrapper` mantiene invece un'API pubblica compatta ma
stateful, con branch e operazioni osservabili sufficienti per un confronto
sperimentale difendibile.

### Suite manuale black-box `T_BB`

La Category Partition è stata costruita esclusivamente sul contratto pubblico
della classe e congelata prima di coverage e mutation testing.

Suite finale:

```text
T_BB                    : 12 test, FROZEN
PASS                    : 12
FAIL                    : 0
Errors                  : 0
Skipped                 : 0
```

Baseline JaCoCo:

```text
LINE                    : 52.78% (38 / 72)
BRANCH                  : 45.00% (18 / 40)
METHOD                  : 90.91% (10 / 11)
```

Il metodo `remove()` non è stato utilizzato per costruire la suite black-box
iniziale perché la documentazione pubblica presenta elementi non sufficientemente
univoci per definire un oracle specification-based forte.

### Suite manuale coverage-guided `T_CF`

Dopo il freeze di `T_BB` sono stati aggiunti cinque test mirati ai gap di
controllo di flusso.

```text
T_CF additions          : 5
Suite T_BB + T_CF       : 17
PASS                    : 17
FAIL                    : 0
```

Coverage cumulativa:

```text
LINE                    : 98.61% (71 / 72)
BRANCH                  : 87.50% (35 / 40)
METHOD                  : 100.00% (11 / 11)
```

Delta rispetto a `T_BB`:

```text
LINE delta              : +45.83 pp
BRANCH delta            : +42.50 pp
METHOD delta            : +9.09 pp
Covered lines           : +33
Covered branches        : +17
Covered methods         : +1
```

La stopping rule viene applicata senza inseguire artificialmente il 100% dei
branch residui.

### Mutation testing e suite mutation-guided `T_MT`

La baseline mutation viene misurata sulla suite congelata `T_BB + T_CF`.

Protocollo:

```text
PIT                     : 1.25.8
pitest-junit5-plugin    : 1.2.3
Mutators                : DEFAULTS
Threads                 : 1
Production              : openjpa-lib 4.1.1
Native OpenJPA tests    : NOT USED
```

Identità production:

```text
SHA-256:
C06F2D6F83082E8CC538069769BA1C2241678054E9C27B0510A77C0ADCE4B0F4
```

Baseline:

```text
Population              : 52
KILLED                  : 46
SURVIVED                : 4
NO_COVERAGE             : 2
Mutation Score          : 88.46%
Test Strength           : 92.00%
```

L'analisi dei mutanti problematici ha guidato due test aggiuntivi:

```text
TMT-001                 : navigation predicates across cached/frontier states
TMT-002                 : remove after reset from consumed iterator
```

Regressione manuale finale:

```text
T_BB                    : 12
T_CF additions          : 5
T_MT additions          : 2
Manual suite            : 19
PASS                    : 19
FAIL                    : 0
```

PIT post-`T_MT`:

```text
Population              : 52
KILLED                  : 50
SURVIVED                : 2
NO_COVERAGE             : 0
Mutation Score          : 96.15%
Test Strength           : 96.15%
```

I due survivor residui sono conservati come strong equivalent/infeasible-
discrimination candidates, senza presentarli come equivalenti formalmente
dimostrati. Non viene pianificato `TMT-003`.

```text
T_MT STATUS             : FROZEN
```

Evidence principali:

```text
isw2/results/testing/list-iterator-wrapper/mutation/
```

Documentazione:

* [`docs/testing/list-iterator-wrapper-black-box.md`](docs/testing/list-iterator-wrapper-black-box.md)
* [`docs/testing/list-iterator-wrapper-control-flow.md`](docs/testing/list-iterator-wrapper-control-flow.md)
* [`docs/testing/list-iterator-wrapper-mutation-testing.md`](docs/testing/list-iterator-wrapper-mutation-testing.md)

### Suite automatica random `T_RND`

Dopo il freeze della fase manuale è stata costruita una suite automatica
indipendente tramite Randoop 4.3.4.

Per mantenere il confronto same-cardinality con `T_BB`:

```text
N = 12
```

Poiché il costruttore di `ListIteratorWrapper` richiede un `Iterator`,
`java.util.ArrayList` viene utilizzata esclusivamente come producer pubblico
di input; la classe richiesta come effettivamente coperta rimane
`ListIteratorWrapper`.

Protocollo:

```text
Generator               : Randoop 4.3.4
Target N                : 12
Seeds                   : 0, 1
Generated limit         : 20000
Output limit            : 12
Time limit              : 60 s per seed
Only public members     : YES
Error-revealing tests   : DISABLED
Coverage feedback       : NONE
Mutation feedback       : NONE
Manual oracle editing   : NONE
```

Generazione e validazione RAW:

```text
Seed 0                  : 7 regression test, 7 / 7 PASS
Seed 1                  : 9 regression test, 9 / 9 PASS
```

La cardinalità canonica viene ottenuta deterministicamente prima delle
metriche:

```text
Seed 0                  : tutti i 7 test
Seed 1                  : primi 5 test
T_RND finale            : 12
```

Suite integrata:

```text
T_RND                   : 12
PASS                    : 12
FAIL                    : 0
Errors                  : 0
Skipped                 : 0
```

JaCoCo isolato:

```text
LINE                    : 58.33% (42 / 72)
BRANCH                  : 47.50% (19 / 40)
METHOD                  : 100.00% (11 / 11)
```

PIT isolato sulla stessa popolazione mutante:

```text
Population              : 52
KILLED                  : 6
SURVIVED                : 19
NO_COVERAGE             : 27
TIMED_OUT               : 0
RUN_ERROR               : 0
MEMORY_ERROR            : 0
Mutation Score          : 11.54%
Test Strength           : 24.00%
```

A parità di `N = 12`, Randoop supera leggermente `T_BB` nella coverage
strutturale, ma mostra oracle significativamente meno discriminanti nella
mutation analysis. Il risultato viene mantenuto senza rigenerazione o
ottimizzazione post-hoc.

```text
T_RND STATUS            : FROZEN
```

Evidence:

```text
isw2/results/testing/list-iterator-wrapper/rnd/
```

Documentazione dettagliata:

[`docs/testing/list-iterator-wrapper-random-testing.md`](docs/testing/list-iterator-wrapper-random-testing.md)

### Suite automatica coverage-guided `T_ES`

Dopo il freeze di `T_RND` è stata costruita una seconda suite automatica
indipendente tramite EvoSuite 1.2.0.

Per mantenere il confronto same-cardinality con `T_BB`:

```text
N = 12
```

Il protocollo è stato fissato prima di osservare le metriche:

```text
Target                  : org.apache.openjpa.lib.util.collections.ListIteratorWrapper
Generator               : EvoSuite 1.2.0
Runtime                 : Zulu JDK 11
Criterion               : LINE:BRANCH
Search budget           : 120 s
Stopping condition      : MAXTIME
Minimization            : enabled
Max suite size          : 12
Seed                    : 0
Coverage feedback       : NONE
Mutation feedback       : NONE
Post-adequacy editing   : NONE
```

Il seed 0 ha prodotto direttamente più test della cardinalità richiesta:

```text
RAW seed 0             : 15 test
RAW validation         : 15 / 15 PASS
Additional seeds       : NOT REQUIRED
```

La suite canonica è stata selezionata prima di JaCoCo e PIT mediante pruning
puramente posizionale:

```text
Canonical selection    : first 12 / 15
Canonical tests        : 12
Canonical validation   : 12 / 12 PASS
```

Per l'integrazione sono stati applicati soltanto adattamenti infrastrutturali:
package e nomi delle classi, pruning posizionale e
`separateClassLoader = false` prima del freeze per consentire la misurazione
JaCoCo. Gli oracle generati non sono stati modificati sulla base delle metriche.

Hash SHA-256 della suite congelata:

```text
Test:
206731C8322758C64612DC194113549C4BDB583D557521B4D3E9629B1BC564D9

Scaffolding:
7E501C4C62BBA117013BA5AC081AEE2B4A1615209359EA32077750157FF29CC5
```

Adeguatezza JaCoCo sulla sola classe `ListIteratorWrapper`:

```text
LINE                    : 83.33% (60 / 72)
BRANCH                  : 82.50% (33 / 40)
METHOD                  : 81.82% (9 / 11)
```

Mutation testing isolato sulla stessa popolazione congelata utilizzata per le
altre suite della classe:

```text
Population              : 52
Population identity     : PASS (52/52)
KILLED                  : 29
SURVIVED                : 15
NO_COVERAGE             : 8
TIMED_OUT               : 0
RUN_ERROR               : 0
MEMORY_ERROR            : 0
Mutation Score          : 55.77%
Test Strength           : 65.91%
```

A parità di `N = 12`, EvoSuite migliora sensibilmente `T_RND` in Line
Coverage, Branch Coverage, Mutation Score e Test Strength, mentre raggiunge
9 degli 11 metodi contro gli 11 raggiunti da Randoop. Il risultato viene
mantenuto senza rigenerazione o ottimizzazione post-hoc.

```text
T_ES STATUS             : FROZEN
```

Evidence:

```text
isw2/results/testing/list-iterator-wrapper/es/
```

Documentazione dettagliata:

[`docs/testing/list-iterator-wrapper-evosuite-testing.md`](docs/testing/list-iterator-wrapper-evosuite-testing.md)

### Suite automatica LLM `T_LLM`

Dopo il freeze di `T_ES` è stata costruita la terza suite automatica
indipendente tramite LLM, mantenendo la stessa cardinalità sperimentale:

```text
N = 12
```

Il protocollo è stato fissato prima di osservare JaCoCo e PIT:

```text
Target                  : org.apache.openjpa.lib.util.collections.ListIteratorWrapper
N                       : 12
Framework               : JUnit Jupiter
Runtime                 : Java 21
LLM client              : Microsoft Copilot
Interaction mode        : browser chat
Model                   : GPT 5.6 Think Deeper
Feedback coverage       : NONE
Feedback mutation       : NONE
Post-freeze editing     : NONE
```

Il prompt principale ha richiesto nella stessa risposta la progettazione di
12 scenari `TLLM-001 ... TLLM-012` e la relativa implementazione.

La prima validazione runtime ha mantenuto la cardinalità di 12 test ma ha
evidenziato problemi in sei scenari:

```text
Tests run               : 12
Failures                : 3
Errors                  : 3
Skipped                 : 0
```

La correzione è stata gestita prima del freeze con una catena di repair
documentata:

```text
R1                      : repair proposto, output Java troncato
R2                      : decisioni di repair corrette, output Java troncato
R3                      : completamento code-only utilizzabile
Final usable repair     : R3
```

I repair hanno riguardato esclusivamente setup/oracle necessari ad allineare
gli stessi scenari al comportamento runtime del production context. Non sono
stati usati JaCoCo, PIT, survivor o risultati delle altre suite.

La suite finale è stata validata e sottoposta a stability check prima delle
misure di adequacy:

```text
T_LLM                   : 12
PASS                    : 12
FAIL                    : 0
Errors                  : 0
Skipped                 : 0
Stability               : 5/5 PASS
```

Hash SHA-256 canonico del test congelato:

```text
9044AC58592FD650B0080B27D42526A85A8762029C51DDD69E874666004C5F8C
```

Adeguatezza JaCoCo sulla sola classe `ListIteratorWrapper`:

```text
LINE                    : 100.00% (72 / 72)
BRANCH                  : 92.50% (37 / 40)
METHOD                  : 100.00% (11 / 11)
```

Mutation testing isolato sulla stessa popolazione canonica utilizzata per le
altre suite della classe:

```text
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
```

I cinque survivor post-freeze vengono conservati come risultato sperimentale
e non vengono utilizzati per modificare o rigenerare la suite.

Nel confronto same-cardinality `N = 12`, `T_LLM` ottiene per
`ListIteratorWrapper` la coverage strutturale e la mutation effectiveness più
alte tra `T_RND`, `T_ES` e `T_LLM`. Il risultato resta specifico del target e
non viene generalizzato oltre l'esperimento.

```text
T_LLM STATUS            : FROZEN / COMPLETE
```

Evidence principali:

```text
isw2/testing/llm/listiteratorwrapper/
isw2/results/testing/list-iterator-wrapper/llm/
```

Documentazione dettagliata:

[`docs/testing/list-iterator-wrapper-llm-testing.md`](docs/testing/list-iterator-wrapper-llm-testing.md)

Prossima fase:

```text
Reliability di PCEnhancer
```


---

## Documentazione

* [Setup e baseline](docs/setup.md)
* [Milestone 1 – Dataset Creation](docs/milestone1.md)
* [Milestone 2 – Classification](docs/milestone2.md)
* [Milestone 3 – What-if Analysis](docs/milestone3.md)
* [Testing – PCEnhancer black-box](docs/testing/pcenhancer-black-box.md)
* [Testing – PCEnhancer control-flow](docs/testing/pcenhancer-control-flow.md)
* [Testing – PCEnhancer mutation testing](docs/testing/pcenhancer-mutation-testing.md)
* [Testing – PCEnhancer random testing](docs/testing/pcenhancer-random-testing.md)
* [Testing – PCEnhancer EvoSuite](docs/testing/pcenhancer-evosuite-testing.md)
* [Testing – PCEnhancer LLM](docs/testing/pcenhancer-llm-testing.md)
* [Testing – ListIteratorWrapper black-box](docs/testing/list-iterator-wrapper-black-box.md)
* [Testing – ListIteratorWrapper control-flow](docs/testing/list-iterator-wrapper-control-flow.md)
* [Testing – ListIteratorWrapper mutation testing](docs/testing/list-iterator-wrapper-mutation-testing.md)
* [Testing – ListIteratorWrapper random testing](docs/testing/list-iterator-wrapper-random-testing.md)
* [Testing – ListIteratorWrapper EvoSuite](docs/testing/list-iterator-wrapper-evosuite-testing.md)
* [Testing – ListIteratorWrapper LLM](docs/testing/list-iterator-wrapper-llm-testing.md)

La documentazione viene aggiornata progressivamente durante lo sviluppo.

---

## Principi metodologici

Durante il progetto:

1. il materiale ufficiale del corso rappresenta la fonte primaria;
2. ogni fase viene verificata prima di procedere alla successiva;
3. i risultati vengono generati automaticamente quando possibile;
4. i dataset generati non vengono corretti manualmente;
5. le scelte metodologiche vengono documentate e motivate;
6. il branch `baseline-4.1.1` rimane immutabile;
7. i defect non vengono esclusi sulla base della natura del problema se soddisfano i criteri adottati;
8. gli audit diagnostici vengono utilizzati per verificare e spiegare i risultati senza modificare manualmente i dataset;
9. la suite manuale black-box iniziale viene congelata prima di osservare coverage e mutation score;
10. i test già presenti nel repository OpenJPA non vengono riutilizzati come suite sperimentale;
11. ogni famiglia di test viene documentata, implementata, eseguita e validata prima di procedere alla successiva;
12. i test `T_CF` vengono selezionati dai gap di Line/Branch Coverage solo dopo il freeze di `T_BB` e vengono congelati prima della mutation analysis;
13. i test `T_MT` vengono selezionati da cluster di survivor behaviorally meaningful, mantenuti solo se dimostrano capacità aggiuntiva di fault detection e congelati quando la stopping rule rende non giustificata un'ulteriore iterazione;
14. la suite `T_RND` viene generata e congelata prima di osservare JaCoCo e PIT; coverage e mutation testing sono utilizzati esclusivamente come metriche di valutazione e non come feedback per rigenerare o selezionare i test random;
15. la suite `T_ES` viene costruita con protocollo e stopping rule fissati prima delle misure di adequacy; i seed vengono consumati in ordine deterministico fino a raggiungere la cardinalità sperimentale fissata per la classe, e JaCoCo/PIT sono usati soltanto dopo il freeze come metriche di valutazione;
16. la suite `T_LLM` viene generata con protocollo e cardinalità fissati prima delle misure di adequacy; eventuali repair pre-freeze sono ammessi soltanto per problemi emersi durante compilazione/esecuzione e per allineare gli stessi scenari al production context, senza usare feedback di coverage o mutation; dopo il freeze, JaCoCo e PIT sono usati esclusivamente per la valutazione e non come feedback per modificare o rigenerare i test;
17. per `ListIteratorWrapper`, le suite automatiche vengono confrontate a cardinalità `N = 12`, pari alla `T_BB` congelata; eventuali seed multipli vengono consumati in ordine e la selezione si arresta appena raggiunta la cardinalità prevista.

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
* test generati tramite LLM;
* generazione guidata dalla coverage;
* code coverage;
* mutation testing;
* miglioramento delle suite;
* reliability;
* confronto dei test sulle versioni refactored.

Per la suite manuale iniziale viene adottato un approccio **black-box**:
le categorie vengono ricavate prima dalla documentazione e dal contratto pubblico
della classe. Coverage, mutation testing, code smell e dettagli del controllo di
flusso non vengono usati per costruire retroattivamente la suite iniziale.

La prima classe attualmente in lavorazione è:

```text
org.apache.openjpa.enhance.PCEnhancer
```

Per `PCEnhancer` la suite manuale iniziale `T_BB` è stata derivata tramite
Category Partition e congelata prima di osservare coverage e mutation score.
Successivamente è stata costruita una suite manuale coverage-guided `T_CF`,
selezionando scenari a partire dai gap del controllo di flusso.

Stato corrente:

```text
T_BB             : 30 test, FROZEN
T_BB outcome     : 29 PASS, 1 FAIL noto (TBB-026)
T_BB LINE        : 43.61%
T_BB BRANCH      : 30.57%

T_CF additions   : 5 test, FROZEN
Suite cumulativa : 35 test
Outcome finale   : 34 PASS, 1 FAIL noto (TBB-026)
Final LINE       : 70.77%
Final BRANCH     : 55.22%
```

La failure `TBB-026` resta invariata: il contratto pubblico di
`PCEnhancer.run(..., Options)` dichiara `false` per opzioni invalide, mentre la
baseline OpenJPA 4.1.1 restituisce `true` per il representative input congelato.
L'oracle non viene modificato per rendere artificialmente verde la suite.

Documentazione:

* [`docs/testing/pcenhancer-black-box.md`](docs/testing/pcenhancer-black-box.md)
* [`docs/testing/pcenhancer-control-flow.md`](docs/testing/pcenhancer-control-flow.md)

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
* [x] Full regression `T_BB` – 29 PASS, 1 FAIL documentato (`TBB-026`)
* [x] Audit finale e traceability degli oracle `T_BB`
* [x] Baseline JaCoCo `T_BB` – 43.61% Line / 30.57% Branch
* [x] Coverage-gap audit pre-`T_CF`
* [x] Implementazione `T_CF` – 5 test coverage-guided
* [x] Feasibility preflight dei candidati complessi `TCF-003..005`
* [x] Coverage cumulativa finale – 70.77% Line / 55.22% Branch
* [x] Final gap audit e stopping rule (`TCF-006` non pianificato)
* [x] Freeze audit `T_CF` – 5 test, 6 fixture, manifest SHA-256

### In corso

* [ ] Mutation analysis della suite manuale congelata `T_BB + T_CF`
* [ ] Evoluzione mutation-guided `T_MT`
* [ ] Suite automatiche `T_RND`, `T_LLM`, `T_ES`
* [ ] Reliability di `PCEnhancer`
* [ ] Testing della seconda classe OpenJPA

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

La Category Partition iniziale è stata congelata a:

```text
N = 30
```

Full regression canonica:

```text
isw2/results/testing/pcenhancer/tbb/runs/pcenhancer_tbb_full_run.txt
```

Risultato:

```text
Tests run : 30
PASS      : 29
FAIL      : 1 (TBB-026)
Errors    : 0
Skipped   : 0
```

Baseline di adeguatezza sulla classe esterna
`org.apache.openjpa.enhance.PCEnhancer`:

```text
LINE   : 43.61% (1177 / 2699)
BRANCH : 30.57% (372 / 1217)
```

### Suite manuale coverage-guided `T_CF`

Dopo il freeze di `T_BB`, un gap audit formale ha guidato l'aggiunta di cinque
scenari manuali:

```text
TCF-001 Application Identity
TCF-002 Detached-state Externalization
TCF-003 Standard Serialization
TCF-004 Relationship-valued Identity
TCF-005 Optimized IdClass Copy
```

La suite cumulativa finale contiene:

```text
T_BB             : 30
T_CF additions   : 5
Total            : 35
PASS             : 34
Known FAIL       : 1 (TBB-026)
Errors           : 0
Skipped          : 0
```

Coverage finale:

```text
LINE   : 70.77% (1910 / 2699)
BRANCH : 55.22% (672 / 1217)
```

Incremento rispetto alla baseline `T_BB`:

```text
Covered lines    : +733
Covered branches : +300
LINE delta       : +27.16 pp
BRANCH delta     : +24.65 pp
```

Il final gap audit applica una stopping rule esplicita: i gap residui non sono
sufficienti, da soli, a giustificare nuovi micro-test. `TCF-006` non è
pianificato e `T_CF` è stata congelata.

Evidence principali:

```text
isw2/results/testing/pcenhancer/tbb/
isw2/results/testing/pcenhancer/tcf/
```

Il freeze finale di `T_CF` verifica cinque test, sei fixture, assenza di
diagnostic temporanei e micro-test obsoleti, clean compilation e manifest
SHA-256 degli 11 artefatti Java definitivi.

Prossima fase:

```text
Mutation analysis -> T_MT
```

---

## Documentazione

* [Setup e baseline](docs/setup.md)
* [Milestone 1 – Dataset Creation](docs/milestone1.md)
* [Milestone 2 – Classification](docs/milestone2.md)
* [Milestone 3 – What-if Analysis](docs/milestone3.md)
* [Testing – PCEnhancer black-box](docs/testing/pcenhancer-black-box.md)
* [Testing – PCEnhancer control-flow](docs/testing/pcenhancer-control-flow.md)

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
11. ogni famiglia di test viene documentata, implementata, eseguita e validata prima di procedere alla successiva.
12. i test `T_CF` vengono selezionati dai gap di Line/Branch Coverage solo dopo il freeze di `T_BB` e vengono congelati prima della mutation analysis.

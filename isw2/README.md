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
└── results/
```

### `analyzer/`

Progetto Maven indipendente contenente gli strumenti sviluppati per automatizzare le attività delle milestone.

### `datasets/`

Dataset e cataloghi generati automaticamente.

### `results/`

Risultati sperimentali delle analisi.

### `docs/`

Documentazione metodologica dettagliata.

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

La parte di Software Testing verrà svolta su due classi OpenJPA e comprenderà:

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

### Successivamente

* [ ] Milestone 3 – What-if Analysis
* [ ] Selezione delle due classi OpenJPA
* [ ] Software Testing – De Angelis
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

## Documentazione

* [Setup e baseline](docs/setup.md)
* [Milestone 1 – Dataset Creation](docs/milestone1.md)
* [Milestone 2 – Classification](docs/milestone2.md)

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
8. gli audit diagnostici vengono utilizzati per verificare e spiegare i risultati senza modificare manualmente i dataset.

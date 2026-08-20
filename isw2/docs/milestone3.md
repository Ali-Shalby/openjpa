# Milestone 3 – What-if Analysis

## 1. Obiettivo

La Milestone 3 studia il seguente scenario controfattuale:

```text
How many buggy classes could have been prevented by having zero smells?
```

La milestone parte dagli artefatti già completati:

```text
Dataset A      -> Milestone 1
BClassifier    -> Milestone 2
```

Per Apache OpenJPA:

```text
Dataset A   = isw2/datasets/openjpa_dataset_a.csv
BClassifier = RandomForest
```

Il Dataset A contiene:

```text
12836 osservazioni
12 release
18 predictor
BUGGY=YES 2010
BUGGY=NO 10826
```

---

## 2. Requisiti del materiale ufficiale

Il materiale della Milestone 3 richiede di:

```text
1. utilizzare A, creato nella Milestone 1;
2. utilizzare il miglior classificatore scelto nella Milestone 2,
   chiamato BClassifier;
3. creare B+, B e C;
4. addestrare BClassifier su A, ottenendo BClassifierA;
5. predire A, B, B+ e C;
6. costruire una tabella di confronto;
7. rispondere:
   - Is BClassifier accurate?
   - How many buggy classes could have been prevented by having zero smells?
     - in total;
     - in proportion;
     - out of the preventable ones.
```

Definizioni ufficiali:

```text
B+ = porzione di A con NSmells > 0
C  = porzione di A con NSmells = 0
B  = B+ manipolato impostando NSmells = 0
```

---

## 3. Interpretazione del what-if

`B+` contiene osservazioni reali per cui erano presenti code smell.

`C` contiene osservazioni reali che avevano già:

```text
NSmells = 0
```

`B` è invece sintetico.

Per ogni osservazione di `B+`, `B` mantiene:

```text
Project
Class
ReleaseIndex
tutti gli altri predictor
BUGGY
```

e modifica esclusivamente:

```text
NSmells -> 0
```

Di conseguenza, il confronto:

```text
B+ -> B
```

isola il what-if relativo alla rimozione degli smell, mantenendo invariati gli
altri attributi disponibili nel Dataset A.

---

## 4. Principi di lavoro

Come nelle milestone precedenti, ogni blocco segue:

```text
definizione
↓
implementazione
↓
esecuzione
↓
validazione
↓
documentazione
```

Non si procede al blocco successivo finché quello corrente non è stato
validato.

Le regole principali sono:

```text
1. il materiale del professore è la fonte primaria;
2. le scelte non specificate nel materiale vengono dichiarate come scelte
   metodologiche del progetto;
3. Dataset A non viene modificato;
4. B+, B e C vengono generati automaticamente;
5. B e B+ devono differire esclusivamente in NSmells;
6. nessun valore BUGGY viene modificato durante la costruzione dei dataset;
7. ogni output viene validato prima del training del classifier.
```

---

## 5. Primo blocco – Costruzione B+, B e C

Classe:

```text
it.uniroma2.isw2.openjpa.classification.M3DatasetBuilder
```

Sorgente:

```text
isw2/analyzer/src/main/java/it/uniroma2/isw2/openjpa/classification/M3DatasetBuilder.java
```

Input:

```text
isw2/datasets/openjpa_dataset_a.csv
```

Output previsti:

```text
isw2/datasets/openjpa_m3_bplus.csv
isw2/datasets/openjpa_m3_b.csv
isw2/datasets/openjpa_m3_c.csv

isw2/results/m3/dataset_builder_validation.txt
```

---

## 6. Validazioni del Dataset Builder

Il builder è stato eseguito sul Dataset A finale.

Risultati:

```text
Dataset A rows  : 12836
Dataset B+ rows : 8933
Dataset B rows  : 8933
Dataset C rows  : 3903
```

La partizione è quindi:

```text
B+ = 8933 / 12836 = 69.59% delle osservazioni
C  = 3903 / 12836 = 30.41% delle osservazioni
```

Distribuzione `BUGGY`:

```text
A:
YES = 2010
NO  = 10826

B+:
YES = 1723
NO  = 7210

B:
YES = 1723
NO  = 7210

C:
YES = 287
NO  = 3616
```

I tassi descrittivi di bugginess sono:

```text
B+ = 1723 / 8933 = 19.29%
C  =  287 / 3903 =  7.35%
```

Inoltre:

```text
1723 / 2010 = 85.72%
```

delle osservazioni realmente `BUGGY=YES` del Dataset A appartengono a `B+`,
cioè alla porzione con almeno uno smell.

Questo valore è soltanto descrittivo e non rappresenta ancora il numero di bug
prevenibili: la stima controfattuale richiederà le prediction del modello su B.

Somma degli smell:

```text
A  Sum(NSmells)  = 94308
B+ Sum(NSmells)  = 94308
B  Sum(NSmells)  = 0
C  Sum(NSmells)  = 0
```

Questo conferma che tutte le osservazioni con smell presenti in A sono
contenute in B+.

Le validazioni automatiche hanno verificato:

```text
|B+| + |C| = |A|                     : PASSED
|B| = |B+|                            : PASSED
B+ -> NSmells > 0                     : PASSED
C  -> NSmells = 0                     : PASSED
B  -> NSmells = 0                     : PASSED
B/B+ stesse osservazioni              : PASSED
B/B+ differiscono solo in NSmells     : PASSED
B/B+ BUGGY invariato                  : PASSED
B+ e C disgiunti                      : PASSED
B+ union C ricostruisce A             : PASSED
schema preservato                     : PASSED
```

È stato inoltre effettuato un controllo indipendente sui CSV generati:

```text
B      : 8933 righe × 22 colonne
B+     : 8933 righe × 22 colonne
C      : 3903 righe × 22 colonne

header identici                        : True
tutte le colonne B/B+ eccetto NSmells : identiche
Class B/B+                             : identica
ReleaseIndex B/B+                      : identico
BUGGY B/B+                             : identico
```

Report:

```text
isw2/results/m3/dataset_builder_validation.txt
```

Risultato finale:

```text
ValidationPassed=True
```

La costruzione di B+, B e C è quindi considerata completata e validata.

---

## 7. Modello M3

Dal risultato della Milestone 2:

```text
BClassifier = RandomForest
```

Il materiale ufficiale richiede:

```text
Train BClassifier on A
```

e successivamente:

```text
Predict A
Predict B+
Predict B
Predict C
```

### 7.1 Rapporto con training/test della Milestone 2

La selezione di `RandomForest` è già stata effettuata nella Milestone 2 tramite:

```text
10 times 10-folds
```

utilizzando prediction out-of-fold.

Quella fase costituisce la valutazione out-of-sample del classificatore.

La Milestone 3 ha uno scopo differente: costruire il modello finale
`BClassifierA` e applicarlo allo scenario what-if.

Per questo motivo:

```text
BClassifierA = RandomForest addestrato sull'intero Dataset A
```

Le prediction M3 su A, B+, B e C non sostituiscono la validazione
cross-validated della Milestone 2.

In particolare, la prediction su A è una prediction in-sample e viene usata
come controllo descrittivo del modello finale, non come nuova stima imparziale
della sua generalizzazione.

### 7.2 Preprocessing scelto

La slide M3 non specifica un preprocessing da applicare a `BClassifierA`.

Scelta del progetto:

```text
Training          = A completo
Predictor         = tutti i 18 predictor
Feature Selection = NO
Balancing         = NO
Classifier        = RandomForest
Seed              = 1
```

`NSmells` viene mantenuto esplicitamente tra i predictor.

Motivazione:

```text
NSmells è la feature manipolata nel passaggio B+ -> B.
```

Rimuoverla tramite Feature Selection renderebbe il what-if privo della
variabile che si intende modificare.

SMOTE non viene utilizzato perché la Milestone 3 richiede di addestrare il
classifier su A e non prescrive un bilanciamento aggiuntivo.

La scelta mantiene inoltre separati:

```text
valutazione del classificatore -> Milestone 2
analisi controfattuale         -> Milestone 3
```

### 7.3 Target durante la prediction

Durante le prediction il valore reale `BUGGY` non viene fornito al modello.

La classe dell'istanza Weka viene impostata come missing prima di chiamare:

```text
distributionForInstance(...)
```

Il valore storico `BUGGY` viene mantenuto soltanto nei metadata per poter
confrontare valori reali e stimati.

Per B questo valore viene chiamato `ReferenceBUGGY`, perché rappresenta la
label storica della corrispondente osservazione B+ e non una ground truth
osservata dello scenario controfattuale.

---


## 7.4 Esecuzione e validazione del What-if Runner

È stata implementata la classe:

```text
it.uniroma2.isw2.openjpa.classification.M3WhatIfRunner
```

Il modello finale è:

```text
BClassifierA = RandomForest
```

con:

```text
Training dataset       : A
Training rows          : 12836
Predictor              : 18
NSmells                : incluso
Feature Selection      : False
Balancing              : False
RandomForest seed      : 1
```

Opzioni effettive Weka:

```text
-P 100 -I 100 -num-slots 1 -K 0 -M 1.0 -V 0.001 -S 1
```

Durante la prediction il target `BUGGY` viene impostato come missing e quindi
non viene fornito al modello.

### Prediction ottenute

| Dataset | Rows | Reference YES | Predicted YES | Mean P(YES) |
| --- | ---: | ---: | ---: | ---: |
| A | 12836 | 2010 | 2010 | 0.154853 |
| B+ | 8933 | 1723 | 1723 | 0.189733 |
| B | 8933 | 1723* | 1300 | 0.162006 |
| C | 3903 | 287 | 287 | 0.075023 |

`*` Per B, `Reference YES` indica la label storica della corrispondente
osservazione B+ e non una ground truth osservata nello scenario sintetico.

Numero totale di prediction:

```text
12836 + 8933 + 8933 + 3903 = 34605
```

### Controlli automatici

```text
A = B+ union C predictions : PASSED
A/B+ unchanged prediction  : PASSED
A/C unchanged prediction   : PASSED
B/B+ observation alignment : PASSED
B/B+ only NSmells differs  : PASSED
Probability validation     : PASSED
ValidationPassed=True
```

Il fatto che A, B+ e C vengano predetti esattamente come le rispettive label
storiche è coerente con il fatto che `BClassifierA` sia stato addestrato
sull'intero A.

Questi risultati non vengono utilizzati come nuova stima out-of-sample
dell'accuratezza del classifier. La valutazione di generalizzazione resta
quella della Milestone 2 tramite `10 × 10-fold`.

### Confronto B+ -> B a livello di osservazione

È stato effettuato un controllo indipendente sulle prediction B+/B.

Matrice delle transizioni:

```text
                         B
                  NO          YES

B+  NO          7208            2
B+  YES          425         1298
```

Quindi:

```text
YES -> NO = 425
NO  -> YES =   2
```

La riduzione netta di classi predette buggy è:

```text
425 - 2 = 423
```

coerente con:

```text
1723 - 1300 = 423
```

La probabilità media `P(BUGGY=YES)` passa da:

```text
B+ = 0.189733
B  = 0.162006
```

con riduzione media:

```text
0.027727
```

Il classifier non è monotono rispetto a `NSmells` per ogni singola
osservazione: la manipolazione interagisce con gli altri predictor
dell'ensemble. Per questo motivo l'interpretazione finale usa il cambiamento
netto del numero di classi stimate buggy, coerentemente con la logica della
tabella what-if.

Output:

```text
isw2/results/m3/what_if_predictions.csv
isw2/results/m3/what_if_prediction_summary.csv
isw2/results/m3/what_if_validation.txt
```


## 8. Analisi finale prevista

La tabella finale dovrà distinguere valori reali e stimati sui quattro dataset.

Il materiale di supporto mostra la logica:

```text
A  -> actual / estimated
B+ -> actual / estimated
B  -> estimated
C  -> actual / estimated
```

Il confronto controfattuale centrale sarà:

```text
buggy sulle osservazioni smelly
-
buggy stimate sulle stesse osservazioni con NSmells = 0
```

Da questo valore verranno calcolati i risultati richiesti dal professore.

---


## 8.1 Risultato finale del What-if

È stata implementata la classe:

```text
it.uniroma2.isw2.openjpa.classification.M3SummaryGenerator
```

Input:

```text
isw2/results/m3/what_if_predictions.csv
```

Output finali:

```text
isw2/results/m3/what_if_result.csv
isw2/results/m3/what_if_result_validation.txt
```

La tabella finale è:

| Dataset | Actual BUGGY | Estimated BUGGY |
| --- | ---: | ---: |
| A | 2010 | 2010 |
| B+ | 1723 | 1723 |
| B | — | 1300 |
| C | 287 | 287 |

Per B non esiste un valore `Actual BUGGY` osservato nello scenario senza smell,
perché B è un dataset controfattuale.

### Transizioni B+ -> B

Il confronto riga-per-riga tra le prediction di B+ e B produce:

```text
B+ predicted YES -> B predicted NO = 425
B+ predicted NO  -> B predicted YES =   2
```

La variazione netta è quindi:

```text
425 - 2 = 423
```

che coincide anche con:

```text
1723 - 1300 = 423
```

### Quante classi buggy potrebbero essere state prevenute?

Risultato totale:

```text
423 classi
```

In proporzione rispetto a tutte le classi buggy reali di A:

```text
423 / 2010 = 0.210448 = 21.04%
```

Rispetto alle classi buggy appartenenti alla parte potenzialmente prevenibile
B+:

```text
423 / 1723 = 0.245502 = 24.55%
```

Quindi:

```text
Totale prevenibile stimato        : 423 classi
Su tutte le classi buggy di A     : 21.04%
Tra le buggy della porzione B+    : 24.55%
```

### Probabilità media di bugginess

```text
Mean P(YES) B+ = 0.189733
Mean P(YES) B  = 0.162006
```

Riduzione:

```text
0.189733 - 0.162006 = 0.027727
```

La rimozione controfattuale degli smell riduce quindi anche la probabilità media
stimata di bugginess sulle stesse osservazioni.

### Validazione finale

Il summary generator verifica:

```text
Transition matrix               : PASSED
Net = YES->NO - NO->YES         : PASSED
A = B+ union C predicted counts : PASSED
Proportion ranges               : PASSED
ValidationPassed                : True
```

La Milestone 3 è quindi considerata completata e validata.

---

## 8.2 Risposte finali alle domande della Milestone 3

### Is BClassifier accurate?

Il classifier scelto è:

```text
RandomForest
```

La sua accuratezza non viene stimata in M3 attraverso le prediction in-sample
su A.

La valutazione out-of-sample è quella già eseguita nella Milestone 2 con:

```text
10 times 10-folds
```

dove RandomForest è risultato il miglior classificatore per tutte le metriche
considerate.

Le prediction M3 servono invece alla what-if analysis.

### How many buggy classes could have been prevented by having zero smells?

```text
In total:
423 classi

In proportion rispetto a tutte le BUGGY di A:
21.04%

Out of the preventable ones, cioè tra le BUGGY di B+:
24.55%
```

Questa stima è controfattuale: indica ciò che `BClassifierA` predice quando le
stesse osservazioni di B+ vengono valutate con `NSmells = 0`, mantenendo
invariati gli altri predictor.

---


## 9. Decisioni metodologiche

### Decisione 1 – Dataset di input

Decisione:

```text
utilizzare esclusivamente openjpa_dataset_a.csv come sorgente di A, B+, B e C
```

Motivazione:

```text
è il Dataset A finale validato nelle Milestone 1 e 2
```

### Decisione 2 – Identità B/B+

Decisione:

```text
B viene costruito come copia riga-per-riga di B+ modificando esclusivamente
NSmells
```

Motivazione:

```text
il what-if deve isolare la variazione della feature relativa agli smell
```

### Decisione 3 – Nessuna modifica al target

Decisione:

```text
BUGGY viene mantenuto identico tra B+ e B
```

Motivazione:

```text
B rappresenta un input controfattuale al classifier; il target storico non
viene riscritto durante la manipolazione del dataset
```


### Decisione 4 – Training finale su A completo

Decisione:

```text
BClassifierA viene addestrato sull'intero Dataset A
```

Motivazione:

```text
è la procedura richiesta dalla Milestone 3 dopo che il BClassifier è stato
selezionato e validato nella Milestone 2
```

La valutazione out-of-sample rimane quella della `10 × 10-fold` M2.

### Decisione 5 – Nessuna Feature Selection / nessun balancing

Decisione:

```text
FS      = NO
SMOTE   = NO
predictor = tutti i 18
```

Motivazione:

```text
NSmells deve rimanere nel modello perché è la variabile del what-if;
la Milestone 3 non prescrive ulteriore balancing
```

### Decisione 6 – Seed RandomForest

Decisione:

```text
seed = 1
```

Motivazione:

```text
rendere riproducibile il singolo modello finale BClassifierA
```

---

## 10. Problemi incontrati e soluzioni

Da aggiornare progressivamente.

---

## 11. Output disponibili

Dataset di partenza:

```text
isw2/datasets/openjpa_dataset_a.csv
```

Dataset M3 generati:

```text
isw2/datasets/openjpa_m3_bplus.csv
isw2/datasets/openjpa_m3_b.csv
isw2/datasets/openjpa_m3_c.csv
```

Validazione dataset:

```text
isw2/results/m3/dataset_builder_validation.txt
```

Prediction what-if:

```text
isw2/results/m3/what_if_predictions.csv
isw2/results/m3/what_if_prediction_summary.csv
isw2/results/m3/what_if_validation.txt
```

Risultati finali:

```text
isw2/results/m3/what_if_result.csv
isw2/results/m3/what_if_result_validation.txt
```

---

## 12. Stato di avanzamento

### Completato

* [x] Milestone 1 – Dataset A
* [x] Milestone 2 – Classification
* [x] `BClassifier = RandomForest`
* [x] Materiale ufficiale Milestone 3 acquisito
* [x] Definizioni A / B+ / B / C identificate
* [x] Piano di validazione dei dataset definito

### Completato nel blocco dataset M3

* [x] Implementazione `M3DatasetBuilder`
* [x] Generazione B+
* [x] Generazione B
* [x] Generazione C
* [x] Validazione partizione A = B+ union C
* [x] Validazione B/B+
* [x] `ValidationPassed=True`

### Completato nel design del modello

* [x] Definizione operativa del training `BClassifierA`
* [x] Training set = A completo
* [x] 18 predictor, incluso `NSmells`
* [x] Feature Selection = NO
* [x] Balancing = NO
* [x] RandomForest seed = 1
* [x] Distinzione tra validazione M2 e prediction M3 documentata

### Completato nel blocco prediction

* [x] Implementazione `M3WhatIfRunner`
* [x] Training RandomForest su A
* [x] Prediction A
* [x] Prediction B+
* [x] Prediction B
* [x] Prediction C
* [x] 34605 prediction generate
* [x] Allineamento B/B+ validato
* [x] `ValidationPassed=True`

### Completato nel blocco finale

* [x] Implementazione `M3SummaryGenerator`
* [x] Tabella finale What-if
* [x] Matrice transizioni B+ -> B
* [x] Calcolo riduzione netta = 423
* [x] Calcolo proporzione su tutte le BUGGY = 21.04%
* [x] Calcolo proporzione sulle BUGGY di B+ = 24.55%
* [x] Validazione finale `ValidationPassed=True`
* [x] Risposte alle domande del professore definite

### Restano attività di repository

* [ ] Aggiornamento README
* [ ] Selezione degli output M3 da versionare
* [ ] Verifica `git status`
* [ ] Commit logici M3
* [ ] Push
* [ ] Verifica CI verde

# Milestone 2 – Classification

## 1. Obiettivo

La Milestone 2 ha l'obiettivo di confrontare l'accuratezza di tre classificatori
sul Dataset A di Apache OpenJPA generato nella Milestone 1.

Il confronto avverrà tra i seguenti modelli di machine learning:

```text
RandomForest
NaiveBayes
IBk
```

utilizzando le metriche:

```text
Precision
Recall
AUC
Kappa
NPofB20
```

e la tecnica di validazione:

```text
10 times 10-folds
```

Il materiale richiede inoltre l'utilizzo di filtri per:

```text
feature selection
balancing
```

Dataset di partenza:

```text
isw2/datasets/openjpa_dataset_a.csv
```

---

# 2. Fonte dei requisiti

La fonte primaria della Milestone 2 è il materiale ufficiale del corso.

Requisiti espliciti:

```text
3 classificatori:
- RandomForest
- NaiveBayes
- IBk

5 metriche:
- Precision
- Recall
- AUC
- Kappa
- NPofB20

Validazione:
- 10 times 10-folds

Preprocessing:
- feature selection
- balancing
```

Il materiale presenta inoltre come domande da analizzare:

```text
Quale classificatore è più accurato?

Il miglior classificatore cambia in base a:
- dataset;
- numero di release;
- metrica?

Quale classificatore è migliore per una determinata metrica?
```

Il materiale non specifica direttamente:

```text
algoritmo esatto di feature selection
tecnica esatta di balancing
ordine dei preprocessing
eventuale tuning degli iperparametri
seed della cross-validation
formato esatto degli output
```

Queste scelte verranno quindi trattate come decisioni metodologiche del progetto
e motivate esplicitamente.

---

# 3. Principi di lavoro

La Milestone 2 viene sviluppata con lo stesso approccio utilizzato nella
Milestone 1.

Ogni blocco deve seguire il ciclo:

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

Non si procede al blocco successivo finché il precedente non è stato validato.

Le principali regole sono:

```text
1. il materiale del professore è la fonte primaria;
2. le scelte non presenti nel materiale vengono dichiarate come scelte nostre;
3. nessun risultato viene corretto manualmente;
4. gli esperimenti devono essere riproducibili;
5. tutti i preprocessing che apprendono dai dati devono essere fitted solo sul training;
6. il test fold non deve influenzare feature selection o balancing;
7. tutti i classificatori devono essere confrontati sugli stessi fold;
8. prima del FULL run viene eseguita e validata una modalità QUICK;
9. ogni output intermedio importante deve essere validato;
10. README e milestone2.md vengono aggiornati progressivamente.
```

---

# 4. Dataset di input

Dataset:

```text
isw2/datasets/openjpa_dataset_a.csv
```

Stato validato al termine della Milestone 1:

```text
Rows                 : 12836
Unique observations  : 12836
Releases             : 12
Feature columns      : 18
BUGGY=YES            : 2010
BUGGY=NO             : 10826
Buggy rate           : 15.66%
```

Schema:

```text
Project
Class
ReleaseIndex

LOC
LOC_TOUCHED
NR
NAUTH
LOC_ADDED
MAX_LOC_ADDED
AVG_LOC_ADDED
CHURN
MAX_CHURN
AVG_CHURN
CHANGE_SET_SIZE
MAX_CHANGE_SET
AVG_CHANGE_SET
AGE_WEEKS
WEIGHTED_AGE_WEEKS
IGNORED_ZERO_LOC_REVS
NSmells
NFIX

BUGGY
```

Identificatori:

```text
Project
Class
ReleaseIndex
```

Target:

```text
BUGGY
```

Gli identificatori non devono essere utilizzati automaticamente come predictor.

`LOC` deve essere mantenuta disponibile anche perché necessaria per il calcolo
di `NPofB20`.

---

## 4.1 Pre-flight Milestone 2

Prima di iniziare l'implementazione della pipeline di classificazione è stato
eseguito un pre-flight sull'ambiente e sul Dataset A.

### Dataset A

Il dataset finale della Milestone 1 è stato verificato come input della
Milestone 2.

Risultati confermati:

```text
Rows                 : 12836
Columns              : 22
Unique observations  : 12836
Releases             : 12
Predictor             : 18
BUGGY=YES            : 2010
BUGGY=NO             : 10826
```

Gli identificatori:

```text
Project
Class
ReleaseIndex
```

rimangono disponibili per tracciabilità, ma non verranno utilizzati come
predictor.

La classe target è:

```text
BUGGY
```

con:

```text
YES = classe positiva
NO  = classe negativa
```

### Weka

Il modulo:

```text
isw2/analyzer
```

non conteneva inizialmente Weka tra le dipendenze Maven.

È stata aggiunta la dipendenza:

```xml
<dependency>
    <groupId>nz.ac.waikato.cms.weka</groupId>
    <artifactId>weka-stable</artifactId>
    <version>3.8.7</version>
</dependency>
```

La scelta di Weka è coerente con l'implementazione dei classificatori richiesti
nella Milestone 2.

La dipendenza è stata validata eseguendo:

```text
mvn -f .\isw2\analyzer\pom.xml -DskipTests compile
```

Risultato:

```text
BUILD SUCCESS
```

Il pre-flight della Milestone 2 è quindi considerato completato.

---


## 4.2 Loader M2 per Weka

È stata implementata la classe:

```text
it.uniroma2.isw2.openjpa.classification.M2DatasetLoader
```

Sorgente:

```text
isw2/analyzer/src/main/java/it/uniroma2/isw2/openjpa/classification/M2DatasetLoader.java
```

Il loader utilizza come input:

```text
isw2/datasets/openjpa_dataset_a.csv
```

e costruisce un oggetto Weka `Instances` contenente esclusivamente:

```text
18 predictor numerici
+
1 target nominale BUGGY
```

Gli identificatori:

```text
Project
Class
ReleaseIndex
```

non vengono inseriti nella matrice utilizzata dai classificatori.

Rimangono però disponibili separatamente come metadata per mantenere la
tracciabilità tra prediction e osservazione originale.

Il loader conserva inoltre:

```text
originalIndex
Project
Class
ReleaseIndex
LOC
actual BUGGY
```

nei metadata associati a ogni osservazione.

La conservazione di `LOC` è necessaria per il successivo calcolo di
`NPofB20`.

### Classe positiva

Il target Weka è:

```text
BUGGY
```

con valori nominali ordinati come:

```text
NO
YES
```

La classe positiva utilizzata nelle metriche è:

```text
YES
```

con indice Weka:

```text
1
```

### Controlli automatici

Il loader fallisce se viene rilevato uno dei seguenti problemi:

```text
Dataset A assente
header inatteso
numero di colonne errato
Project diverso da OPENJPA
Class vuota
ReleaseIndex fuori intervallo
duplicato (ReleaseIndex, Class)
predictor non numerico
predictor non finito
LOC <= 0
BUGGY diverso da YES/NO
numero totale di righe inatteso
numero di predictor inatteso
numero di release inatteso
distribuzione BUGGY inattesa
missing value nelle Instances Weka
conteggio per release inatteso
```

### Validazione

Il loader è stato compilato ed eseguito con Weka 3.8.7.

Report:

```text
isw2/results/m2/dataset_loader_validation.txt
```

Risultati:

```text
Rows                 : 12836
Weka attributes      : 19
Predictors           : 18
Class attribute      : BUGGY
Positive class       : YES
Positive class index : 1
BUGGY=YES            : 2010
BUGGY=NO             : 10826
Metadata rows        : 12836
Releases             : 12
Min LOC              : 3.0
Max LOC              : 3799.0
ValidationPassed     : True
```

Distribuzione per release:

| Release | Osservazioni |
| ------: | -----------: |
| 1 | 932 |
| 2 | 949 |
| 3 | 948 |
| 4 | 996 |
| 5 | 1029 |
| 6 | 1058 |
| 7 | 1045 |
| 8 | 1050 |
| 9 | 1051 |
| 10 | 1185 |
| 11 | 1300 |
| 12 | 1293 |

I conteggi coincidono con il Dataset A validato nella Milestone 1.

Il loader M2 è quindi considerato completato e validato.

---


## 4.3 Fold planner M2

È stata implementata la classe:

```text
it.uniroma2.isw2.openjpa.classification.M2FoldPlanner
```

Sorgente:

```text
isw2/analyzer/src/main/java/it/uniroma2/isw2/openjpa/classification/M2FoldPlanner.java
```

Il planner costruisce in modo deterministico e stratificato i fold della
Milestone 2 utilizzando esclusivamente:

```text
originalIndex
BUGGY
```

dei record caricati da `M2DatasetLoader`.

I predictor non vengono utilizzati nella costruzione dei fold.

Questo evita che informazioni sulle feature possano influenzare la
suddivisione train/test.

### Modalità QUICK

La modalità QUICK utilizza:

```text
1 repetition
2 fold
seed = 1
```

Risultato validato:

```text
Rows/repetition      : 12836

Fold 1:
train                : 6418
test                 : 6418
test BUGGY=YES       : 1005
test BUGGY=NO        : 5413

Fold 2:
train                : 6418
test                 : 6418
test BUGGY=YES       : 1005
test BUGGY=NO        : 5413
```

Fingerprint del piano QUICK:

```text
2d58388e8b32104270b8ea1b300e7e7ca7add6a214d23b3d8161cfdb863b376e
```

Output:

```text
isw2/results/m2/folds/fold_plan_quick.csv
isw2/results/m2/folds/fold_validation_quick.txt
```

### Modalità FULL

La modalità FULL utilizza:

```text
10 repetition
10 fold per repetition
seed repetition = 1..10
```

Per ciascuna repetition:

```text
12836 osservazioni complessive di test
2010 BUGGY=YES
10826 BUGGY=NO
```

Ogni osservazione compare:

```text
esattamente una volta
```

in un test fold della stessa repetition.

La stratificazione produce, per ogni repetition:

```text
6 fold:
test rows     : 1284
test YES      : 201
test NO       : 1083
train rows    : 11552

4 fold:
test rows     : 1283
test YES      : 201
test NO       : 1082
train rows    : 11553
```

Il file FULL contiene quindi:

```text
12836 × 10 = 128360
```

assegnazioni test complessive.

Fingerprint del piano FULL:

```text
b67164ea440d16576420a3ae1a8af4ac39d05c1c51085ea166ca6035af08b10c
```

Output:

```text
isw2/results/m2/folds/fold_plan_full.csv
isw2/results/m2/folds/fold_validation_full.txt

isw2/analyzer/src/main/java/it/uniroma2/isw2/openjpa/classification/M2Preprocessor.java
isw2/results/m2/preprocessing/preprocessing_quick.csv
isw2/results/m2/preprocessing/preprocessing_validation_quick.txt

isw2/analyzer/src/main/java/it/uniroma2/isw2/openjpa/classification/M2ClassifierRunner.java
isw2/results/m2/classification/predictions_quick.csv
isw2/results/m2/classification/model_runs_quick.csv
isw2/results/m2/classification/classification_validation_quick.txt

isw2/analyzer/src/main/java/it/uniroma2/isw2/openjpa/classification/M2Metrics.java
isw2/results/m2/metrics/metrics_quick.csv
isw2/results/m2/metrics/metrics_validation_quick.txt

isw2/analyzer/src/main/java/it/uniroma2/isw2/openjpa/classification/M2ExperimentRunner.java
isw2/results/m2/full/classifier_metrics_one_rep.csv
isw2/results/m2/full/preprocessing_audit_one_rep.csv
isw2/results/m2/full/model_runs_one_rep.csv
isw2/results/m2/full/experiment_validation_one_rep.txt
isw2/results/m2/full/classifier_metrics_full.csv
isw2/results/m2/full/preprocessing_audit_full.csv
isw2/results/m2/full/model_runs_full.csv
isw2/results/m2/full/experiment_validation_full.txt
```

### Validazioni automatiche

Il planner verifica:

```text
nessuna sovrapposizione train/test
nessun indice duplicato nel training fold
nessun indice duplicato nel test fold
ogni indice valido
copertura completa del Dataset A
ogni osservazione testata una sola volta per repetition
presenza di entrambe le classi in ogni train e test fold
differenza massima di 1 tra i conteggi YES dei fold
differenza massima di 1 tra i conteggi NO dei fold
seed coerente con la repetition
numero corretto di repetition e fold
```

Risultato QUICK:

```text
Train/test overlap        : 0
Missing test observations : 0
Duplicate test coverage   : 0
Class stratification      : PASSED
Reusable plan             : True
ValidationPassed          : True
```

Risultato FULL:

```text
Train/test overlap        : 0
Missing test observations : 0
Duplicate test coverage   : 0
Class stratification      : PASSED
Reusable plan             : True
ValidationPassed          : True
```

Il piano è creato una sola volta per repetition e viene successivamente
riutilizzato da tutti i classificatori e da tutte le configurazioni
FS/balancing.

Il fold planner è quindi considerato completato e validato.

---


## 4.4 Preprocessing M2

È stata implementata la classe:

```text
it.uniroma2.isw2.openjpa.classification.M2Preprocessor
```

Sorgente:

```text
isw2/analyzer/src/main/java/it/uniroma2/isw2/openjpa/classification/M2Preprocessor.java
```

La classe implementa le quattro configurazioni sperimentali:

```text
C1 = FS No  / Balancing No
C2 = FS Yes / Balancing No
C3 = FS No  / Balancing Yes
C4 = FS Yes / Balancing Yes
```

Tecniche utilizzate:

```text
Feature Selection : CfsSubsetEval + BestFirst
Balancing         : SMOTE
Ordine combinato  : Feature Selection -> SMOTE
```

La Feature Selection viene fitted esclusivamente sul training fold.

Quando attiva, lo stesso selector già fitted viene successivamente applicato
al test fold.

SMOTE viene applicato esclusivamente al training fold trasformato.

Il test fold non viene mai oversamplato.

### Dipendenza SMOTE

`weka-stable 3.8.7` non rendeva disponibile direttamente la classe:

```text
weka.filters.supervised.instance.SMOTE
```

È stata quindi aggiunta al `pom.xml` la dipendenza separata:

```xml
<dependency>
    <groupId>nz.ac.waikato.cms.weka</groupId>
    <artifactId>SMOTE</artifactId>
    <version>1.0.3</version>
    <exclusions>
        <exclusion>
            <groupId>nz.ac.waikato.cms.weka</groupId>
            <artifactId>weka-dev</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

L'esclusione di `weka-dev` evita di introdurre una seconda distribuzione Weka
in parallelo a:

```text
weka-stable 3.8.7
```

Dopo l'aggiunta della dipendenza è stato rigenerato il classpath Maven usato
per l'esecuzione da riga di comando.

### Modalità QUICK

Il preprocessing QUICK utilizza:

```text
1 repetition
2 fold
4 configurazioni
```

per un totale di:

```text
8 preprocessing run
```

La distribuzione originale di ciascun training fold è:

```text
BUGGY=YES : 1005
BUGGY=NO  : 5413
```

La distribuzione originale di ciascun test fold è:

```text
BUGGY=YES : 1005
BUGGY=NO  : 5413
```

### C1 – Nessun preprocessing

Per entrambi i fold:

```text
Predictor : 18 -> 18

Train:
1005 YES / 5413 NO
    ->
1005 YES / 5413 NO

Test:
1005 YES / 5413 NO
    ->
1005 YES / 5413 NO
```

### C2 – Solo Feature Selection

Fold 1:

```text
Predictor : 18 -> 7

LOC
LOC_TOUCHED
NR
MAX_LOC_ADDED
AVG_CHANGE_SET
NSmells
NFIX
```

Fold 2:

```text
Predictor : 18 -> 3

LOC
AVG_CHANGE_SET
NFIX
```

La differenza tra i subset selezionati nei due fold è attesa: la Feature
Selection viene fitted indipendentemente sul training di ciascun fold.

La distribuzione di classe di train e test rimane invariata.

### C3 – Solo SMOTE

Per entrambi i fold:

```text
Predictor : 18 -> 18
```

Il training viene bilanciato:

```text
1005 YES / 5413 NO
    ->
5413 YES / 5413 NO
```

Percentuale SMOTE:

```text
438.607%
```

Seed:

```text
Fold 1 -> 1001
Fold 2 -> 1002
```

Il test rimane invariato:

```text
1005 YES / 5413 NO
```

### C4 – Feature Selection + SMOTE

La Feature Selection produce gli stessi subset di C2 sullo stesso training
fold, perché viene eseguita prima di SMOTE.

Fold 1:

```text
Predictor : 18 -> 7

LOC
LOC_TOUCHED
NR
MAX_LOC_ADDED
AVG_CHANGE_SET
NSmells
NFIX
```

Fold 2:

```text
Predictor : 18 -> 3

LOC
AVG_CHANGE_SET
NFIX
```

Successivamente SMOTE bilancia il training:

```text
1005 YES / 5413 NO
    ->
5413 YES / 5413 NO
```

Il test rimane invariato.

### Validazioni automatiche

Il preprocessing verifica:

```text
test row count invariato
test YES/NO invariati
sequenza delle label test invariata
SMOTE applicato solo al training
Feature Selection fitted solo sul training
nessun missing value introdotto
stesso schema finale tra train e test
BUGGY mantenuto come class attribute
NO invariati durante SMOTE
bilanciamento training circa 1:1
nessuna modifica al training quando balancing è disattivato
```

Risultato:

```text
Test oversampling      : False
FS fit scope           : training only
SMOTE scope            : training only
ValidationPassed       : True
```

Il preprocessing QUICK è quindi considerato completato e validato.

---


## 4.5 Classificatori QUICK

È stata implementata la classe:

```text
it.uniroma2.isw2.openjpa.classification.M2ClassifierRunner
```

Sorgente:

```text
isw2/analyzer/src/main/java/it/uniroma2/isw2/openjpa/classification/M2ClassifierRunner.java
```

Il runner utilizza:

```text
RandomForest
NaiveBayes
IBk
```

sulle quattro configurazioni già validate:

```text
C1 = FS No  / Balancing No
C2 = FS Yes / Balancing No
C3 = FS No  / Balancing Yes
C4 = FS Yes / Balancing Yes
```

e riutilizza il piano QUICK già validato:

```text
1 repetition
2 fold
```

Fingerprint:

```text
2d58388e8b32104270b8ea1b300e7e7ca7add6a214d23b3d8161cfdb863b376e
```

### Parametri dei classificatori

Non viene effettuato hyperparameter tuning.

`RandomForest` utilizza i default Weka con seed esplicito per rendere
riproducibile il componente randomico.

Nel QUICK:

```text
Fold 1 -> seed 1001
Fold 2 -> seed 1002
```

Opzioni osservate:

```text
-P 100 -I 100 -num-slots 1 -K 0 -M 1.0 -V 0.001 -S <seed>
```

`NaiveBayes` utilizza i default Weka.

`IBk` utilizza i default Weka:

```text
-K 1
-W 0
LinearNNSearch
EuclideanDistance
```

Le opzioni effettive vengono registrate in:

```text
isw2/results/m2/classification/model_runs_quick.csv
```

### Esecuzione QUICK

Numero di training:

```text
1 repetition
× 2 fold
× 4 configurazioni
× 3 classifier
= 24 model training
```

Per ogni coppia:

```text
Configuration × Classifier
```

vengono raccolte tutte le prediction out-of-fold dei due test fold.

Numero di prediction per esperimento:

```text
12836
```

Numero totale di prediction QUICK:

```text
12836
× 4 configurazioni
× 3 classifier
= 154032
```

### Prediction registrate

Per ogni osservazione vengono conservati:

```text
Configuration
FeatureSelection
Balancing
Classifier
Repetition
Fold
OriginalIndex
ReleaseIndex
Class
Actual
Predicted
ProbabilityNO
ProbabilityYES
LOC
PredictorsAfter
```

Questo permette di calcolare successivamente tutte le metriche richieste
senza rieseguire il training.

In particolare, la presenza congiunta di:

```text
Actual
ProbabilityYES
LOC
```

permette il calcolo di `NPofB20`.

### Validazioni

Il runner verifica:

```text
24 training attesi
154032 prediction complessive
12 esperimenti Configuration × Classifier
12836 prediction OOF per esperimento
nessuna prediction OOF duplicata
stesso insieme di OriginalIndex per tutti gli esperimenti
distribuzione reale 2010 YES / 10826 NO per ogni esperimento
probabilità finite e comprese in [0,1]
ProbabilityYES + ProbabilityNO = 1
coerenza tra classifyInstance e argmax della distribuzione
coerenza tra metadata e label del test
LOC > 0 associata a ogni prediction
stesso fingerprint del fold plan per tutti gli esperimenti
```

Risultato:

```text
Model trainings                  : 24
Predictions                      : 154032
OOF rows per experiment          : 12836
Same fold plan for all experiments: True
OOF coverage per experiment      : 12836/12836
Duplicate OOF predictions        : 0
Probability range                : PASSED
Probability sum                  : PASSED
Actual metadata/test consistency : PASSED
LOC linked to every prediction   : True
ValidationPassed                 : True
```

Output:

```text
isw2/results/m2/classification/predictions_quick.csv
isw2/results/m2/classification/model_runs_quick.csv
isw2/results/m2/classification/classification_validation_quick.txt
```

Il blocco dei classificatori QUICK è quindi considerato completato e validato.

---


## 4.6 Metriche QUICK

È stata implementata la classe:

```text
it.uniroma2.isw2.openjpa.classification.M2Metrics
```

Sorgente:

```text
isw2/analyzer/src/main/java/it/uniroma2/isw2/openjpa/classification/M2Metrics.java
```

La classe legge direttamente:

```text
isw2/results/m2/classification/predictions_quick.csv
```

e calcola le metriche sulle prediction out-of-fold aggregate della repetition.

Non viene effettuato alcun nuovo training.

Per ogni:

```text
Configuration × Classifier × Repetition
```

vengono utilizzate esattamente:

```text
12836 prediction OOF
```

e vengono calcolate:

```text
Precision
Recall
AUC
Kappa
NPofB20
```

### Precision e Recall

La classe positiva è:

```text
BUGGY=YES
```

Le metriche vengono ricavate dalla confusion matrix aggregata della repetition:

```text
Precision = TP / (TP + FP)

Recall = TP / (TP + FN)
```

### AUC

L'AUC viene calcolata sulle probabilità:

```text
P(BUGGY=YES)
```

utilizzando una formulazione rank-based equivalente alla Mann-Whitney statistic.

In presenza di probabilità identiche viene assegnato il rank medio, in modo
che i tie contribuiscano correttamente con peso 0.5.

### Kappa

La Cohen's Kappa viene calcolata dalla confusion matrix aggregata tramite:

```text
Kappa = (Po - Pe) / (1 - Pe)
```

dove:

```text
Po = observed agreement
Pe = expected agreement
```

### NPofB20

Per ogni prediction viene calcolato:

```text
NormalizedScore =
    P(BUGGY=YES) / LOC
```

Le osservazioni vengono ordinate in ordine decrescente di `NormalizedScore`.

A parità di score viene usato:

```text
OriginalIndex crescente
```

come tie-break deterministico.

Il ranking viene percorso fino a raggiungere o superare:

```text
20% delle LOC totali
```

Poiché l'unità di ispezione è la classe, viene inclusa anche la classe che fa
raggiungere o superare il budget.

La metrica finale è:

```text
NPofB20 =
    BUGGY reali individuati nel budget
    ----------------------------------
    BUGGY reali totali
```

Per il Dataset A:

```text
Total LOC  : 1750165
Budget LOC : 350033
```

cioè esattamente:

```text
20%
```

delle LOC complessive.

### Risultati QUICK

Sono state prodotte:

```text
12 righe metriche
```

corrispondenti a:

```text
4 configurazioni
× 3 classificatori
× 1 repetition QUICK
```

Risultati:

| Config. | Classifier | Precision | Recall | AUC | Kappa | NPofB20 |
| --- | --- | ---: | ---: | ---: | ---: | ---: |
| C1 | IBk | 0.627835 | 0.605970 | 0.777608 | 0.547036 | 0.498010 |
| C1 | NaiveBayes | 0.523151 | 0.376617 | 0.804234 | 0.353158 | 0.204478 |
| C1 | RandomForest | 0.797430 | 0.648259 | 0.941119 | 0.668610 | 0.648756 |
| C2 | IBk | 0.646697 | 0.574627 | 0.869095 | 0.540884 | 0.544279 |
| C2 | NaiveBayes | 0.567002 | 0.336816 | 0.749580 | 0.346304 | 0.280597 |
| C2 | RandomForest | 0.703464 | 0.596020 | 0.917197 | 0.585804 | 0.618408 |
| C3 | IBk | 0.574865 | 0.689552 | 0.804020 | 0.550181 | 0.534826 |
| C3 | NaiveBayes | 0.507585 | 0.399502 | 0.806259 | 0.358640 | 0.216418 |
| C3 | RandomForest | 0.715097 | 0.751741 | 0.942824 | 0.681907 | 0.651244 |
| C4 | IBk | 0.591027 | 0.681592 | 0.878276 | 0.559139 | 0.573134 |
| C4 | NaiveBayes | 0.519974 | 0.395025 | 0.753640 | 0.362816 | 0.343284 |
| C4 | RandomForest | 0.641475 | 0.692537 | 0.914652 | 0.601188 | 0.617910 |

Nel QUICK:

```text
migliore Precision : C1 RandomForest = 0.797430
migliore Recall    : C3 RandomForest = 0.751741
migliore AUC       : C3 RandomForest = 0.942824
migliore Kappa     : C3 RandomForest = 0.681907
migliore NPofB20   : C3 RandomForest = 0.651244
```

Questi valori sono utilizzati esclusivamente come sanity check della pipeline.

Non costituiscono ancora i risultati sperimentali della Milestone 2, perché il
QUICK utilizza soltanto:

```text
1 repetition
2 fold
```

mentre il confronto finale deve essere basato sul FULL:

```text
10 repetition
10 fold
```

### Validazioni

Per tutte le 12 righe:

```text
Precision in [0,1]          : PASSED
Recall in [0,1]             : PASSED
AUC in [0,1]                : PASSED
Kappa in [-1,1]             : PASSED
NPofB20 in [0,1]            : PASSED
confusion matrix total      : 12836
OOF coverage                : PASSED
NPofB20 LOC budget          : PASSED
```

La coerenza di Precision, Recall, Kappa e NPofB20 è stata inoltre verificata
a partire dai valori grezzi presenti in `metrics_quick.csv`.

Output:

```text
isw2/results/m2/metrics/metrics_quick.csv
isw2/results/m2/metrics/metrics_validation_quick.txt
```

Risultato finale:

```text
ValidationPassed=True
```

Con questa validazione l'intera pipeline QUICK:

```text
Dataset A
↓
Fold planner
↓
Feature Selection / SMOTE
↓
RandomForest / NaiveBayes / IBk
↓
Prediction OOF
↓
Precision / Recall / AUC / Kappa / NPofB20
```

è considerata completata e validata end-to-end.

---


## 4.7 Validazione ONE_REP sul piano FULL

Prima dell'esecuzione completa `10 × 10` è stata introdotta una modalità
intermedia:

```text
ONE_REP
```

che utilizza la prima repetition del piano FULL già validato.

La modalità esegue:

```text
1 repetition
× 10 fold
× 4 configurazioni
× 3 classificatori
```

per un totale di:

```text
120 model training
```

La repetition utilizza lo stesso piano FULL definitivo, identificato dal
fingerprint:

```text
b67164ea440d16576420a3ae1a8af4ac39d05c1c51085ea166ca6035af08b10c
```

### Output

Sono stati generati:

```text
isw2/results/m2/full/classifier_metrics_one_rep.csv
isw2/results/m2/full/preprocessing_audit_one_rep.csv
isw2/results/m2/full/model_runs_one_rep.csv
isw2/results/m2/full/experiment_validation_one_rep.txt
```

Conteggi validati:

```text
Metric rows           : 12
Preprocessing rows    : 40
Model runs            : 120
OOF rows/metric row   : 12836
```

### Verifica del preprocessing

Il preprocessing audit contiene:

```text
10 righe C1
10 righe C2
10 righe C3
10 righe C4
```

Per ogni fold:

```text
C1 e C3 mantengono tutti i 18 predictor
C2 e C4 selezionano lo stesso identico subset di feature
```

La seconda proprietà verifica direttamente l'ordine:

```text
Feature Selection -> SMOTE
```

poiché la feature selection di C4 è determinata prima dell'oversampling e
deve quindi coincidere con C2 sullo stesso training fold.

Nei fold FULL la feature selection ha selezionato tra:

```text
6 e 8 predictor
```

con media:

```text
7.1 predictor
```

sui 10 training fold della repetition.

SMOTE mantiene il numero di `NO` del training e aumenta `YES` fino a un
bilanciamento circa `1:1`.

Il test fold rimane invariato.

### Verifica dei classificatori

Sono presenti:

```text
10 training per ogni Configuration × Classifier
```

per un totale di:

```text
120 model run
```

I parametri restano quelli validati nel QUICK.

RandomForest utilizza seed deterministici dipendenti esclusivamente da:

```text
repetition
fold
```

e non dalla configurazione di preprocessing.

### Metriche della repetition 1

Ogni riga è calcolata sulle:

```text
12836 prediction out-of-fold
```

aggregate dei 10 test fold.

| Config. | Classifier | Precision | Recall | AUC | Kappa | NPofB20 |
| --- | --- | ---: | ---: | ---: | ---: | ---: |
| C1 | RandomForest | 0.841435 | 0.723383 | 0.960187 | 0.740367 | 0.692537 |
| C1 | NaiveBayes | 0.518056 | 0.371144 | 0.805615 | 0.347120 | 0.198010 |
| C1 | IBk | 0.659553 | 0.645771 | 0.800298 | 0.588893 | 0.528856 |
| C2 | RandomForest | 0.739371 | 0.666169 | 0.941351 | 0.648721 | 0.672637 |
| C2 | NaiveBayes | 0.543943 | 0.341791 | 0.792323 | 0.340041 | 0.201493 |
| C2 | IBk | 0.707173 | 0.657214 | 0.905078 | 0.624656 | 0.632338 |
| C3 | RandomForest | 0.789837 | 0.796517 | 0.961606 | 0.754569 | 0.688557 |
| C3 | NaiveBayes | 0.510503 | 0.399005 | 0.806568 | 0.359985 | 0.209950 |
| C3 | IBk | 0.608586 | 0.719403 | 0.823166 | 0.589772 | 0.561692 |
| C4 | RandomForest | 0.697452 | 0.762687 | 0.941676 | 0.675534 | 0.674627 |
| C4 | NaiveBayes | 0.520848 | 0.379104 | 0.792711 | 0.353526 | 0.247761 |
| C4 | IBk | 0.671875 | 0.748756 | 0.907914 | 0.650554 | 0.640796 |

Questi valori non vengono usati come risultati finali della Milestone 2:
servono a validare il percorso reale a 10 fold prima dell'esecuzione delle 10
repetition.

### Controlli indipendenti sugli output

Sono stati verificati:

```text
40 righe preprocessing
120 righe model audit
12 righe metriche
10 fold per ogni Configuration × Classifier
C2/C4 con subset selezionato identico nello stesso fold
test invariato durante il preprocessing
confusion matrix totale = 12836 per ogni riga metrica
Precision ricostruibile da TP/FP
Recall ricostruibile da TP/FN
Kappa ricostruibile dalla confusion matrix
NPofB20 = BuggyFound / TotalBuggy
Total LOC = 1750165
Budget LOC = 350033 = 20% delle LOC totali
```

Risultato del report:

```text
Same FULL fold plan          : True
FS-before-SMOTE invariant    : PASSED
Test oversampling            : False
Metric ranges                : PASSED
Confusion totals             : PASSED
NPofB20 LOC budget           : PASSED
ValidationPassed             : True
```

La modalità `ONE_REP` è quindi considerata completata e validata.

Il percorso reale a 10 fold è pronto per l'esecuzione FULL delle 10 repetition.

---


## 4.8 Esecuzione FULL 10 × 10

Dopo la validazione `ONE_REP` è stata eseguita la modalità:

```text
FULL
```

sul piano definitivo:

```text
10 repetition
× 10 fold
× 4 configurazioni
× 3 classificatori
```

per un totale di:

```text
1200 model training
```

Il fingerprint del fold plan è rimasto:

```text
b67164ea440d16576420a3ae1a8af4ac39d05c1c51085ea166ca6035af08b10c
```

### Output prodotti

```text
isw2/results/m2/full/classifier_metrics_full.csv
isw2/results/m2/full/preprocessing_audit_full.csv
isw2/results/m2/full/model_runs_full.csv
isw2/results/m2/full/experiment_validation_full.txt
```

Conteggi finali:

```text
Metric rows           : 120
Preprocessing rows    : 400
Model runs            : 1200
```

Le `120` righe metriche corrispondono a:

```text
10 repetition
× 4 configurazioni
× 3 classificatori
```

Ogni riga metrica è calcolata su:

```text
12836 prediction OOF
```

aggregate dei 10 test fold della relativa repetition.

### Validazione indipendente della struttura

Sugli output FULL sono stati verificati:

```text
120 chiavi uniche Configuration × Classifier × Repetition
400 chiavi uniche Configuration × Repetition × Fold
1200 chiavi uniche Configuration × Classifier × Repetition × Fold
10 repetition presenti
10 fold presenti per repetition
4 configurazioni presenti
3 classificatori presenti
12836 OOF prediction per ogni riga metrica
TP + FP + TN + FN = 12836 per ogni riga
TP + FN = 2010 BUGGY=YES
TN + FP = 10826 BUGGY=NO
TotalBuggy = 2010
TotalLOC = 1750165
BudgetLOC = 350033 = 20% delle LOC totali
```

### Validazione preprocessing FULL

Per `C1` e `C3`:

```text
PredictorsAfter = 18
```

in tutti i fold.

Per ogni coppia:

```text
Repetition × Fold
```

`C2` e `C4` selezionano lo stesso identico subset di feature.

Questo conferma anche nel FULL l'invariante:

```text
Feature Selection -> SMOTE
```

Il numero di predictor selezionati nei 100 fold FULL è:

```text
min  = 5
mean = 6.79
max  = 9
```

Distribuzione:

```text
5 predictor : 2 fold
6 predictor : 53 fold
7 predictor : 11 fold
8 predictor : 32 fold
9 predictor : 2 fold
```

Per tutte le configurazioni:

```text
test YES/NO prima = test YES/NO dopo
```

Per `C1` e `C2` il training non viene bilanciato e la distribuzione resta
invariata.

Per `C3` e `C4`:

```text
TrainNOAfter = TrainNOBefore
TrainYESAfter = TrainNOAfter
```

quindi SMOTE raggiunge il bilanciamento `1:1` senza modificare la maggioranza.

### Validazione model audit FULL

Per ogni:

```text
Configuration × Classifier × Repetition
```

sono presenti esattamente:

```text
10 fold
```

e la somma delle osservazioni di test dei 10 fold è:

```text
12836
```

Per ogni model run:

```text
PredictedYES + PredictedNO = TestRows
```

I seed RandomForest rispettano:

```text
seed = repetition * 1000 + fold
```

e sono identici tra C1-C4 quando repetition e fold coincidono.

Il tempo registrato esclusivamente nei `1200` model run è circa:

```text
38.5 minuti
```

### Medie FULL preliminari

Le seguenti medie sono ottenute sulle `10` repetition e saranno formalizzate
nel successivo summary aggregato.

| Config. | Classifier | Precision | Recall | AUC | Kappa | NPofB20 |
| --- | --- | ---: | ---: | ---: | ---: | ---: |
| C1 | IBk | 0.660342 | 0.648408 | 0.803075 | 0.590826 | 0.530199 |
| C1 | NaiveBayes | 0.519176 | 0.372438 | 0.805079 | 0.348503 | 0.198308 |
| C1 | RandomForest | 0.842343 | 0.721642 | 0.960649 | 0.739719 | 0.691393 |
| C2 | IBk | 0.703973 | 0.651841 | 0.901090 | 0.619624 | 0.626667 |
| C2 | NaiveBayes | 0.550951 | 0.341592 | 0.793467 | 0.342946 | 0.206418 |
| C2 | RandomForest | 0.738167 | 0.670100 | 0.942305 | 0.650377 | 0.674129 |
| C3 | IBk | 0.607013 | 0.722189 | 0.826392 | 0.589799 | 0.562338 |
| C3 | NaiveBayes | 0.510160 | 0.399701 | 0.806138 | 0.360202 | 0.211343 |
| C3 | RandomForest | 0.786253 | 0.793881 | 0.963241 | 0.750842 | 0.690995 |
| C4 | IBk | 0.666297 | 0.752239 | 0.906993 | 0.648212 | 0.640547 |
| C4 | NaiveBayes | 0.522222 | 0.381542 | 0.794368 | 0.355737 | 0.259403 |
| C4 | RandomForest | 0.699132 | 0.763284 | 0.942625 | 0.676976 | 0.678109 |

Sulla media delle 10 repetition:

```text
migliore Precision : C1 RandomForest = 0.842343
migliore Recall    : C3 RandomForest = 0.793881
migliore AUC       : C3 RandomForest = 0.963241
migliore Kappa     : C3 RandomForest = 0.750842
migliore NPofB20   : C1 RandomForest = 0.691393
```

RandomForest è il miglior classificatore per tutte e cinque le metriche
all'interno di ciascuna delle quattro configurazioni.

Il balancing modifica però il trade-off:

```text
C1 RandomForest:
Precision più alta
NPofB20 medio leggermente più alto

C3 RandomForest:
Recall più alto
AUC più alta
Kappa più alta
```

La differenza media di NPofB20 tra C1 e C3 RandomForest è molto piccola:

```text
C1 = 0.691393
C3 = 0.690995
```

e deve quindi essere interpretata insieme alla variabilità tra repetition,
che verrà riportata nel summary.

### Validazione finale del runner

Il report FULL conclude con:

```text
Completed repetitions       : 10
OOF rows per metric row     : 12836
Same FULL fold plan         : True
FS-before-SMOTE invariant   : PASSED
Test oversampling           : False
Metric ranges               : PASSED
Confusion totals            : PASSED
NPofB20 LOC budget          : PASSED
ValidationPassed            : True
```

L'esecuzione sperimentale FULL è quindi considerata completata e validata.

Il passo successivo è la generazione del summary sulle 10 repetition.

---


## 4.9 Summary FULL e analisi comparativa finale

È stata implementata la classe:

```text
it.uniroma2.isw2.openjpa.classification.M2SummaryGenerator
```

Sorgente:

```text
isw2/analyzer/src/main/java/it/uniroma2/isw2/openjpa/classification/M2SummaryGenerator.java
```

La classe aggrega:

```text
isw2/results/m2/full/classifier_metrics_full.csv
```

contenente:

```text
120 righe raw
```

nelle:

```text
12 combinazioni finali
=
4 configurazioni × 3 classificatori
```

Per ciascuna delle cinque metriche vengono calcolati sulle 10 repetition:

```text
Mean
Sample StdDev (n-1)
Min
Max
```

### Validazione del summary

Il summary finale contiene:

```text
Raw metric rows       : 120
Summary rows          : 12
Repetitions/group     : 10
StdDev                : sample (n-1)
```

La ricostruzione indipendente a partire dalle 120 righe raw coincide con il
summary generato.

Sono state inoltre verificate:

```text
10 repetition distinte per ogni Configuration × Classifier
nessuna repetition duplicata
nessuna repetition mancante
mean corrette
sample standard deviation corrette
min/max corretti
range delle metriche validi
```

Risultato:

```text
Repetition coverage   : PASSED
Summary metric ranges : PASSED
ValidationPassed=True
```

Output:

```text
isw2/results/m2/summary/classifier_summary_full.csv
isw2/results/m2/summary/summary_validation_full.txt
```

### Risultati finali

| Config. | Classifier | Precision | Recall | AUC | Kappa | NPofB20 |
| --- | --- | ---: | ---: | ---: | ---: | ---: |
| C1 | IBk | 0.660342 ± 0.004612 | 0.648408 ± 0.005281 | 0.803075 ± 0.003007 | 0.590826 ± 0.005211 | 0.530199 ± 0.005223 |
| C1 | NaiveBayes | 0.519176 ± 0.001329 | 0.372438 ± 0.001200 | 0.805079 ± 0.000445 | 0.348503 ± 0.001346 | 0.198308 ± 0.000819 |
| C1 | RandomForest | **0.842343 ± 0.002775** | 0.721642 ± 0.002240 | 0.960649 ± 0.000679 | 0.739719 ± 0.001973 | **0.691393 ± 0.002967** |
| C2 | IBk | 0.703973 ± 0.006658 | 0.651841 ± 0.005792 | 0.901090 ± 0.004411 | 0.619624 ± 0.006789 | 0.626667 ± 0.007365 |
| C2 | NaiveBayes | 0.550951 ± 0.005409 | 0.341592 ± 0.003747 | 0.793467 ± 0.003544 | 0.342946 ± 0.003781 | 0.206418 ± 0.004734 |
| C2 | RandomForest | 0.738167 ± 0.004521 | 0.670100 ± 0.004359 | 0.942305 ± 0.001078 | 0.650377 ± 0.004136 | 0.674129 ± 0.002745 |
| C3 | IBk | 0.607013 ± 0.004870 | 0.722189 ± 0.003558 | 0.826392 ± 0.002605 | 0.589799 ± 0.004451 | 0.562338 ± 0.003709 |
| C3 | NaiveBayes | 0.510160 ± 0.001219 | 0.399701 ± 0.001054 | 0.806138 ± 0.000284 | 0.360202 ± 0.001219 | 0.211343 ± 0.001364 |
| C3 | RandomForest | 0.786253 ± 0.003477 | **0.793881 ± 0.004795** | **0.963241 ± 0.000795** | **0.750842 ± 0.004497** | 0.690995 ± 0.002531 |
| C4 | IBk | 0.666297 ± 0.008681 | 0.752239 ± 0.007179 | 0.906993 ± 0.003977 | 0.648212 ± 0.008384 | 0.640547 ± 0.005188 |
| C4 | NaiveBayes | 0.522222 ± 0.003725 | 0.381542 ± 0.004112 | 0.794368 ± 0.003324 | 0.355737 ± 0.003174 | 0.259403 ± 0.010643 |
| C4 | RandomForest | 0.699132 ± 0.006833 | 0.763284 ± 0.006042 | 0.942625 ± 0.001001 | 0.676976 ± 0.006072 | 0.678109 ± 0.002622 |

### Vincitori globali per metrica

```text
Precision -> C1 RandomForest = 0.842343
Recall    -> C3 RandomForest = 0.793881
AUC       -> C3 RandomForest = 0.963241
Kappa     -> C3 RandomForest = 0.750842
NPofB20   -> C1 RandomForest = 0.691393
```

Quindi il classificatore vincente è:

```text
RandomForest
```

per tutte le cinque metriche considerate.

Inoltre RandomForest è il miglior classificatore all'interno di ciascuna delle
quattro configurazioni `C1-C4` per tutte e cinque le metriche.

Questo rende la scelta del modello robusta rispetto alla configurazione di
Feature Selection / balancing adottata.

### Effetto del balancing su RandomForest

Confrontando:

```text
C1 -> C3
```

cioè introducendo SMOTE senza Feature Selection:

```text
Precision : -0.056090
Recall    : +0.072239
AUC       : +0.002592
Kappa     : +0.011123
NPofB20   : -0.000398
```

SMOTE produce quindi il comportamento atteso sul trade-off precision/recall:

```text
aumenta sensibilmente Recall;
riduce Precision;
migliora leggermente AUC e Kappa.
```

Per `NPofB20`, C1 e C3 RandomForest sono sostanzialmente equivalenti:

```text
C1 RF = 0.691393 ± 0.002967
C3 RF = 0.690995 ± 0.002531
```

con differenza media:

```text
0.000398
```

molto piccola rispetto alla scala della metrica.

### Effetto della Feature Selection su RandomForest

Senza balancing:

```text
C2 - C1
```

produce:

```text
Precision : -0.104176
Recall    : -0.051542
AUC       : -0.018343
Kappa     : -0.089341
NPofB20   : -0.017264
```

Con balancing:

```text
C4 - C3
```

produce:

```text
Precision : -0.087121
Recall    : -0.030597
AUC       : -0.020615
Kappa     : -0.073866
NPofB20   : -0.012886
```

Per RandomForest, quindi, la Feature Selection `CfsSubsetEval + BestFirst`
riduce tutte le cinque metriche rispetto alla configurazione corrispondente che
mantiene i 18 predictor.

Questo non rende la Feature Selection errata: mostra semplicemente che, su
questo Dataset A e con RandomForest, il subset selezionato non migliora la
capacità predittiva del modello.

### Risposte alle domande della Milestone 2

#### Quale classificatore è più accurato?

Per Apache OpenJPA il classificatore migliore è:

```text
RandomForest
```

La conclusione non dipende dalla metrica: RandomForest ottiene il valore medio
più alto di Precision, Recall, AUC, Kappa e NPofB20 rispetto a NaiveBayes e
IBk nelle configurazioni sperimentate.

#### Il miglior classificatore cambia in base alla metrica?

Nel nostro esperimento:

```text
No.
```

Il classificatore migliore rimane RandomForest per tutte e cinque le metriche.

Cambia invece la migliore configurazione di preprocessing:

```text
Precision -> C1
Recall    -> C3
AUC       -> C3
Kappa     -> C3
NPofB20   -> C1
```

#### Il miglior classificatore cambia in base a dataset / numero di release?

La Milestone 2 di questo progetto utilizza un unico Dataset A di Apache OpenJPA
contenente le 12 release selezionate.

Non sono stati costruiti dataset incrementali con numeri differenti di release.

Di conseguenza, dai risultati ottenuti è possibile affermare che il vincitore
non cambia rispetto a:

```text
metrica
configurazione FS/balancing
```

ma non è possibile generalizzare sperimentalmente il risultato a dataset o
numeri di release che non sono stati valutati.

#### Quale classificatore è migliore per una specifica metrica?

```text
Precision -> RandomForest
Recall    -> RandomForest
AUC       -> RandomForest
Kappa     -> RandomForest
NPofB20   -> RandomForest
```

Le configurazioni globalmente migliori sono invece C1 o C3 a seconda della
metrica.

### BClassifier selezionato

Il modello selezionato come:

```text
BClassifier
```

è:

```text
RandomForest
```

Motivazione:

```text
- è primo per tutte le cinque metriche;
- è primo in tutte le quattro configurazioni sperimentali;
- presenta variabilità contenuta tra le 10 repetition;
- mantiene prestazioni elevate sia senza balancing sia con SMOTE.
```

`BClassifier` identifica il classificatore scelto, non una singola
configurazione di preprocessing.

La distinzione resta importante perché:

```text
C1 RandomForest
```

massimizza Precision e NPofB20, mentre:

```text
C3 RandomForest
```

massimizza Recall, AUC e Kappa.

---

# 5. Experimental design

L'experimental design è stato definito prima dell'implementazione del runner.

La struttura prende come riferimento i requisiti espliciti del professore e,
per le scelte non specificate nel materiale, adotta decisioni progettuali
documentate e validate nel contesto di OpenJPA.

I requisiti ufficiali rimangono quelli indicati dal materiale della Milestone 2;
le scelte implementative aggiuntive sono motivate e registrate nella
documentazione del progetto.

---

## 5.1 Classificatori

Classificatori richiesti:

```text
RandomForest
NaiveBayes
IBk
```

Implementazioni:

```text
weka.classifiers.trees.RandomForest
weka.classifiers.bayes.NaiveBayes
weka.classifiers.lazy.IBk
```

Non viene effettuato hyperparameter tuning, poiché non richiesto dal materiale
della Milestone 2.

Si utilizzano quindi i parametri di default di Weka 3.8.7, salvo i seed
necessari a rendere riproducibili i componenti randomici.

Le opzioni effettivamente utilizzate verranno comunque registrate negli output
dell'esperimento.

Non viene selezionato in anticipo alcun classificatore migliore.

Il miglior classificatore dovrà emergere dai risultati sperimentali.

---

## 5.2 Dataset utilizzato

La classificazione viene eseguita sull'intero Dataset A:

```text
isw2/datasets/openjpa_dataset_a.csv
```

contenente:

```text
12 release
12836 osservazioni
18 predictor
```

Non vengono creati dataset cumulativi separati del tipo:

```text
R1
R1-R2
R1-R3
...
R1-R12
```

La scelta utilizza l'intero Dataset A finale e validato della Milestone 1,
mantenendo un unico insieme di osservazioni per il confronto delle configurazioni
sperimentali.

La frase del materiale relativa al possibile cambiamento del miglior
classificatore in funzione del "numeroRelease" viene quindi trattata come
domanda di analisi e non come prescrizione esplicita di costruire dataset
incrementali.

---

## 5.3 Validazione 10 × 10-fold

Requisito del professore:

```text
10 times 10-folds
```

La modalità FULL utilizza:

```text
10 repetition
10 fold per repetition
```

Ogni repetition utilizza una randomizzazione indipendente ma riproducibile.

Seed scelti:

```text
repetition 1  -> seed 1
repetition 2  -> seed 2
...
repetition 10 -> seed 10
```

Per ogni repetition il piano dei fold viene generato una sola volta e
riutilizzato identico per:

```text
RandomForest
NaiveBayes
IBk

e per tutte le configurazioni FS / balancing
```

Questo rende il confronto diretto tra classificatori e preprocessing più
controllato, perché ogni configurazione viene valutata sulle stesse identiche
partizioni train/test.

La cross-validation deve essere stratificata rispetto a:

```text
BUGGY
```

in modo da preservare, per quanto possibile, la distribuzione della classe nei
fold.

Le predizioni dei 10 test fold di una stessa repetition vengono aggregate in
un unico insieme out-of-fold.

Le cinque metriche della repetition vengono calcolate su tale insieme
aggregato, non come semplice media delle metriche calcolate sui singoli fold.

---

## 5.4 Feature selection

Il professore richiede l'uso della feature selection ma non specifica
l'algoritmo.

Scelta del progetto:

```text
CfsSubsetEval
+
BestFirst
```

Implementazioni Weka:

```text
weka.attributeSelection.CfsSubsetEval
weka.attributeSelection.BestFirst
```

Motivazione:

```text
CfsSubsetEval valuta subset di feature premiando caratteristiche correlate
con la classe target e penalizzando la ridondanza tra predictor.

BestFirst esplora lo spazio dei possibili subset senza richiedere una ricerca
esaustiva.
```

La scelta è particolarmente adatta al Dataset A, che contiene metriche software
potenzialmente correlate tra loro.

Per ogni fold verranno registrati almeno:

```text
numero di predictor iniziali
numero di predictor selezionati
nomi/indici delle feature selezionate
```

La selezione può quindi variare tra fold e repetition.

Regola anti-leakage:

```text
feature selector fitted esclusivamente sul training fold
↓
training trasformato
↓
test trasformato usando lo stesso selector già fitted
```

Il test fold non può influenzare la selezione delle feature.

---

## 5.5 Balancing

Il professore richiede l'uso del balancing ma non specifica la tecnica.

Scelta del progetto:

```text
SMOTE
```

Implementazione Weka:

```text
weka.filters.supervised.instance.SMOTE
```

Il Dataset A è sbilanciato:

```text
BUGGY=YES : 2010
BUGGY=NO  : 10826
```

SMOTE viene utilizzato per aumentare sinteticamente la classe minoritaria
`BUGGY=YES` sul solo training fold fino a raggiungere, per quanto consentito
dalla percentuale calcolata, un bilanciamento circa 1:1 rispetto alla classe
maggioritaria.

Per ogni training fold verranno registrati almeno:

```text
YES prima del balancing
NO prima del balancing
YES dopo il balancing
NO dopo il balancing
percentuale SMOTE utilizzata
```

Regola anti-leakage:

```text
SMOTE applicato esclusivamente al training fold
```

Il test fold mantiene sempre la distribuzione originale e non contiene
istanze sintetiche.

---

## 5.6 Ordine Feature Selection / SMOTE

Quando feature selection e balancing sono entrambi attivi, l'ordine scelto è:

```text
training reale
    ↓
Feature Selection
    ↓
SMOTE
    ↓
Classifier
```

La feature selection viene quindi determinata sui dati reali del training
prima di generare istanze sintetiche.

Motivazione:

```text
la selezione delle feature deve riflettere le relazioni presenti nelle
osservazioni reali del training e non essere influenzata direttamente dalle
istanze sintetiche introdotte da SMOTE.
```

Successivamente SMOTE opera nello spazio delle feature già selezionato.

Questa scelta mantiene inoltre separati i due obiettivi:

```text
Feature Selection -> riduzione/rilevanza dei predictor
SMOTE             -> correzione dello sbilanciamento della classe
```

L'ordine viene mantenuto fisso per garantire coerenza e confrontabilità tra le configurazioni sperimentali.

---

## 5.7 Configurazioni sperimentali

Vengono utilizzate quattro configurazioni:

```text
C1 = FS No  / Balancing No
C2 = FS Yes / Balancing No
C3 = FS No  / Balancing Yes
C4 = FS Yes / Balancing Yes
```

Questa matrice non è indicata esplicitamente dal professore, ma è adottata come
experimental design perché permette di misurare separatamente:

```text
baseline senza preprocessing
effetto della sola feature selection
effetto del solo balancing
effetto combinato di feature selection e balancing
```

Con:

```text
4 configurazioni
3 classificatori
10 repetition
```

il FULL run produce:

```text
4 × 3 × 10 = 120
```

righe raw di metriche, una per:

```text
configurazione
classificatore
repetition
```

Ogni riga deriva dall'aggregazione delle predizioni out-of-fold dei 10 fold
della relativa repetition.

Il numero totale di training dei modelli nel FULL run è:

```text
10 repetition
× 10 fold
× 4 configurazioni
× 3 classificatori
= 1200 training
```

---

## 5.8 Modalità QUICK

Prima della modalità FULL viene eseguita una modalità QUICK end-to-end.

Configurazione:

```text
1 repetition
2 fold
4 configurazioni
3 classificatori
```

Training attesi:

```text
1 × 2 × 4 × 3 = 24
```

La modalità QUICK attraversa tutti i rami principali della pipeline:

```text
FS No / Yes
Balancing No / Yes
RandomForest
NaiveBayes
IBk
```

Serve esclusivamente per verificare:

```text
loader
fold planner
feature selection
SMOTE
classifier
prediction
metriche
output
controlli anti-leakage
```

I risultati QUICK non vengono utilizzati per rispondere alle domande
sperimentali della Milestone 2.

---

# 6. Prevenzione del data leakage

La prevenzione del leakage è una proprietà obbligatoria dell'esperimento.

Per ogni fold:

```text
training fold
    ↓
fit feature selection
    ↓
transform training
    ↓
transform test con lo stesso selector
    ↓
balancing solo sul training
    ↓
training classifier
    ↓
prediction sul test originale/preprocessato
```

Il test fold non deve essere utilizzato per:

```text
selezione delle feature
stima dei parametri del balancing
oversampling
training del classificatore
tuning dei parametri
```

Eventuali controlli automatici verranno aggiunti al runner.

---

# 7. Metriche

## 7.1 Precision

Da calcolare considerando:

```text
BUGGY=YES
```

come classe positiva.

Formula:

```text
Precision = TP / (TP + FP)
```

---

## 7.2 Recall

Formula:

```text
Recall = TP / (TP + FN)
```

---

## 7.3 AUC

Verrà calcolata sulla classe positiva:

```text
BUGGY=YES
```

utilizzando le probabilità prodotte dal classificatore.

---

## 7.4 Kappa

Verrà utilizzata la Cohen's Kappa fornita/calcolata sull'insieme delle
predizioni out-of-fold.

---

## 7.5 NPofB20

`NPofB20` è la versione normalizzata rispetto alla dimensione della classe di
`PofB20`.

Per ogni osservazione out-of-fold viene mantenuta la probabilità predetta:

```text
P(BUGGY=YES)
```

e viene calcolato lo score:

```text
NormalizedScore =
    P(BUGGY=YES) / LOC
```

Le osservazioni vengono ordinate in ordine decrescente di
`NormalizedScore`.

Il ranking viene quindi percorso accumulando le LOC fino a raggiungere il:

```text
20% delle LOC totali
```

dell'insieme out-of-fold della repetition.

La metrica è:

```text
NPofB20 =
    numero di osservazioni realmente BUGGY trovate nel budget del 20% LOC
    ----------------------------------------------------------------------
    numero totale di osservazioni realmente BUGGY
```

Il target reale:

```text
BUGGY
```

viene utilizzato soltanto per verificare quante osservazioni buggy siano state
effettivamente individuate nel budget di ispezione.

La probabilità utilizzata per il ranking è invece quella prodotta dal
classificatore.

Per il calcolo devono quindi essere conservati per ogni prediction:

```text
indice/osservazione originale
actual BUGGY
predicted BUGGY
P(BUGGY=YES)
LOC
```

La normalizzazione per `LOC` favorisce, a parità di probabilità predetta,
classi più piccole e rappresenta quindi un ranking orientato all'efficienza
dell'ispezione rispetto alla quantità di codice analizzata.

---

# 8. Output previsti

La struttura esatta verrà definita prima dell'esecuzione FULL.

Sono previsti almeno:

```text
risultati raw per repetition/configurazione/classificatore
summary aggregato
feature selection audit
fold plan / fold validation
eventuali prediction audit
```

Possibile organizzazione:

```text
isw2/results/m2/
```

e documentazione:

```text
isw2/docs/milestone2.md
```

---

# 9. Validazioni previste

Prima di accettare il FULL run dovranno essere verificati almeno:

```text
Dataset A caricato correttamente
12836 osservazioni
18 predictor
2010 BUGGY=YES
10826 BUGGY=NO

nessun identificatore usato come predictor

10 repetition
10 fold per repetition

nessuna osservazione mancante nei test fold
nessuna osservazione duplicata nei test fold della stessa repetition

stessi fold per:
- RandomForest
- NaiveBayes
- IBk
- tutte le configurazioni

feature selection fitted solo sul training
balancing applicato solo sul training

nessun test fold oversamplato

metriche finite
metriche nei range validi

prediction count coerente con il Dataset A
```

---

# 10. Modalità QUICK

Prima dell'esperimento FULL verrà implementata una modalità QUICK.

Obiettivo:

```text
verificare end-to-end:
dataset
fold
preprocessing
classifier
prediction
metriche
output
```

La modalità QUICK dovrà essere sufficientemente piccola da permettere
debug rapido.

I parametri QUICK verranno documentati quando definiti.

Il risultato QUICK non verrà utilizzato per rispondere alle domande
sperimentali della Milestone 2.

---

# 11. Modalità FULL

La modalità FULL utilizzerà:

```text
10 repetitions
10 folds
3 classificatori
configurazioni sperimentali validate
```

Il numero atteso di esperimenti verrà calcolato e documentato una volta
fissato definitivamente l'experimental design.

---

# 12. Analisi finale

A esperimento concluso verranno analizzate:

```text
Precision
Recall
AUC
Kappa
NPofB20
```

per ciascun:

```text
classificatore
configurazione
repetition
```

Il summary dovrà permettere di rispondere alle domande del professore:

```text
Quale classificatore è più accurato?

Quale classificatore è migliore per ciascuna metrica?

Feature selection modifica il risultato?

Balancing modifica il risultato?

Il miglior classificatore cambia in funzione della metrica?
```

L'esperimento principale viene eseguito sull'intero Dataset A contenente le
12 release. Non vengono costruiti dataset incrementali per numero di release.

---

# 13. Registro delle decisioni metodologiche

Questa sezione verrà aggiornata durante lo sviluppo.

Formato:

```text
Decisione:
Motivazione:
Alternative considerate:
Fonte:
Impatto sull'esperimento:
```

## Decisione 1 – Dataset di input

Decisione:

```text
Utilizzare openjpa_dataset_a.csv come unica sorgente della Milestone 2.
```

Motivazione:

```text
è il Dataset A finale generato e validato nella Milestone 1
```

Fonte:

```text
Milestone 1 completata
```

## Decisione 2 – Libreria di Machine Learning

Decisione:

```text
Utilizzare Weka tramite la dipendenza Maven weka-stable 3.8.7.
```

Motivazione:

```text
Weka fornisce implementazioni dei tre classificatori richiesti
(RandomForest, NaiveBayes e IBk) e permette di integrare nello stesso
analyzer anche preprocessing, valutazione e metriche.
```

Fonte:

```text
Scelta implementativa del progetto, validata tramite build Maven.
```

Impatto sull'esperimento:

```text
le implementazioni dei classificatori, dei filtri e delle metriche dovranno
essere documentate specificando le classi Weka e i parametri effettivamente
utilizzati.
```


## Decisione 3 – Dataset completo

Decisione:

```text
Eseguire la Milestone 2 sull'intero Dataset A contenente le 12 release.
```

Motivazione:

```text
il materiale non prescrive esplicitamente la costruzione di dataset cumulativi
per numero di release; viene quindi utilizzato l'intero Dataset A finale e validato.
```

Impatto sull'esperimento:

```text
tutte le 12836 osservazioni partecipano alla 10 × 10-fold cross-validation.
```

## Decisione 4 – Configurazioni FS / balancing

Decisione:

```text
C1 = FS No  / Balancing No
C2 = FS Yes / Balancing No
C3 = FS No  / Balancing Yes
C4 = FS Yes / Balancing Yes
```

Motivazione:

```text
misurare separatamente l'effetto dei due preprocessing e il loro effetto
combinato.
```

## Decisione 5 – Feature selection

Decisione:

```text
CfsSubsetEval + BestFirst
```

Motivazione:

```text
selezionare subset informativi riducendo la ridondanza tra metriche software.
```

Regola:

```text
fit solo sul training fold.
```

## Decisione 6 – Balancing

Decisione:

```text
SMOTE sul solo training fold fino a un bilanciamento circa 1:1.
```

Motivazione:

```text
compensare lo sbilanciamento 2010 YES / 10826 NO senza alterare il test set.
```

## Decisione 7 – Ordine dei preprocessing

Decisione:

```text
Feature Selection -> SMOTE
```

Motivazione:

```text
la selezione delle feature viene determinata sulle osservazioni reali del
training prima dell'introduzione di istanze sintetiche.
```

## Decisione 8 – Fold e seed

Decisione:

```text
10 repetition × 10 fold stratificati
seed repetition = 1..10
stessi fold per tutti i classifier e tutte le configurazioni.
```

Motivazione:

```text
garantire riproducibilità e confronto equo tra modelli/configurazioni.
```

## Decisione 9 – Parametri dei classificatori

Decisione:

```text
utilizzare i default Weka 3.8.7 senza hyperparameter tuning.
```

Motivazione:

```text
il tuning non è richiesto dal materiale e introdurrebbe un ulteriore livello
di selezione che dovrebbe essere nested per evitare leakage.
```

## Decisione 10 – NPofB20

Decisione:

```text
ranking tramite P(BUGGY=YES) / LOC e valutazione dei BUGGY reali individuati
entro il budget del 20% delle LOC totali.
```

Impatto sull'esperimento:

```text
le prediction out-of-fold devono mantenere anche probabilità della classe
positiva e LOC dell'osservazione originale.
```



## Decisione 11 – Identificatori e metadata nel modeling

Decisione:

```text
Project, Class e ReleaseIndex non vengono utilizzati come predictor.
```

Motivazione:

```text
sono identificatori/tracciabilità dell'osservazione e non caratteristiche
software da usare per predire BUGGY.
```

Implementazione:

```text
le Instances Weka contengono 18 predictor numerici + BUGGY;
gli identificatori restano separati nei RowMetadata.
```

Impatto sull'esperimento:

```text
nessun identificatore può influenzare l'addestramento;
LOC e gli altri metadata necessari restano comunque collegati alla prediction
originale.
```



## Decisione 12 – Piano dei fold riutilizzabile

Decisione:

```text
generare il piano dei fold una sola volta per repetition e riutilizzarlo
identico per ogni classificatore e configurazione.
```

Motivazione:

```text
il confronto tra modelli deve avvenire sugli stessi identici test set;
eventuali differenze nelle metriche non devono dipendere da partizioni diverse.
```

Riproducibilità:

```text
seed repetition = 1..10
fingerprint SHA-256 del piano
```

Il fingerprint FULL validato è:

```text
b67164ea440d16576420a3ae1a8af4ac39d05c1c51085ea166ca6035af08b10c
```



## Decisione 13 – Prediction OOF come base delle metriche

Decisione:

```text
salvare una prediction out-of-fold per ogni osservazione originale e per ogni
Configuration × Classifier × Repetition.
```

Motivazione:

```text
Precision, Recall, AUC, Kappa e NPofB20 devono essere calcolate sullo stesso
insieme OOF della repetition e non sulla media delle metriche dei singoli fold.
```

Dati conservati:

```text
actual class
predicted class
P(BUGGY=YES)
P(BUGGY=NO)
LOC
originalIndex
```

Impatto:

```text
le metriche possono essere calcolate e validate separatamente dal training dei
modelli, mantenendo la tracciabilità completa delle prediction.
```



## Decisione 14 – Calcolo delle metriche sulla repetition OOF

Decisione:

```text
calcolare le metriche sull'insieme completo delle prediction out-of-fold della
repetition, invece di calcolare la media delle metriche dei singoli fold.
```

Motivazione:

```text
ogni osservazione del Dataset A è predetta esattamente una volta per repetition;
l'insieme OOF costituisce quindi una predizione completa del dataset per quella
repetition.
```

Impatto:

```text
ogni riga raw di metriche rappresenta una intera repetition e non un singolo
fold.
```

## Decisione 15 – Boundary di NPofB20

Decisione:

```text
includere la classe che fa raggiungere o superare il budget del 20% LOC.
```

Motivazione:

```text
l'ispezione avviene a granularità di classe e una classe non può essere
ispezionata parzialmente.
```

Tie-break:

```text
OriginalIndex crescente
```

per garantire determinismo in caso di identico `P(YES)/LOC`.



## Decisione 16 – Gate ONE_REP prima del FULL

Decisione:

```text
eseguire e validare una repetition completa a 10 fold prima del FULL 10 × 10.
```

Motivazione:

```text
il QUICK a 2 fold verifica la pipeline end-to-end ma non esercita esattamente
il percorso 10-fold richiesto dall'esperimento finale.
```

Impatto:

```text
il FULL viene lanciato solo dopo aver validato:
- 10 fold reali;
- 120 training;
- 12 metriche OOF;
- preprocessing C1-C4;
- invarianti anti-leakage.
```

## Decisione 17 – Checkpoint per repetition nel FULL

Decisione:

```text
scrivere gli output di checkpoint al termine di ogni repetition completata.
```

Motivazione:

```text
l'esecuzione FULL è significativamente più lunga del QUICK; salvare le
repetition già validate evita di perdere completamente gli output prodotti in
caso di interruzione successiva.
```


---

# 14. Problemi incontrati e soluzioni

Questa sezione verrà aggiornata durante lo sviluppo.

Ogni problema rilevante verrà registrato con:

```text
Problema
Causa
Diagnosi
Soluzione
Validazione della soluzione
```

Non verranno documentati problemi irrilevanti o puramente locali se non
influenzano metodologia o riproducibilità.

---


## Problema 1 – Classe SMOTE non disponibile a runtime

Problema:

```text
java.lang.NoClassDefFoundError:
weka/filters/supervised/instance/SMOTE
```

Causa:

```text
la classe SMOTE non era disponibile nel classpath runtime generato con la sola
dipendenza weka-stable; inoltre il classpath.txt era stato generato prima
dell'aggiunta della dipendenza SMOTE.
```

Soluzione:

```text
aggiunta della dipendenza Maven SMOTE 1.0.3
esclusione della dipendenza transitiva weka-dev
rigenerazione di target/classpath.txt
```

Validazione:

```text
M2Preprocessor eseguito correttamente
ValidationPassed=True
```

Il warning relativo al caricamento dell'implementazione nativa ARPACK non ha
impedito l'esecuzione: il fallback/native loader disponibile è stato caricato e
il preprocessing si è concluso correttamente.

---

# 15. Output disponibili

Input:

```text
isw2/datasets/openjpa_dataset_a.csv
```

Configurazione:

```text
isw2/analyzer/pom.xml
```

Sorgenti M2:

```text
isw2/analyzer/src/main/java/it/uniroma2/isw2/openjpa/classification/M2DatasetLoader.java
isw2/analyzer/src/main/java/it/uniroma2/isw2/openjpa/classification/M2FoldPlanner.java
isw2/analyzer/src/main/java/it/uniroma2/isw2/openjpa/classification/M2Preprocessor.java
isw2/analyzer/src/main/java/it/uniroma2/isw2/openjpa/classification/M2ClassifierRunner.java
isw2/analyzer/src/main/java/it/uniroma2/isw2/openjpa/classification/M2Metrics.java
isw2/analyzer/src/main/java/it/uniroma2/isw2/openjpa/classification/M2ExperimentRunner.java
isw2/analyzer/src/main/java/it/uniroma2/isw2/openjpa/classification/M2SummaryGenerator.java
```

Validazione dataset:

```text
isw2/results/m2/dataset_loader_validation.txt
```

Fold planner:

```text
isw2/results/m2/folds/fold_plan_quick.csv
isw2/results/m2/folds/fold_validation_quick.txt
isw2/results/m2/folds/fold_plan_full.csv
isw2/results/m2/folds/fold_validation_full.txt
```

Preprocessing QUICK:

```text
isw2/results/m2/preprocessing/preprocessing_quick.csv
isw2/results/m2/preprocessing/preprocessing_validation_quick.txt
```

Classificazione QUICK:

```text
isw2/results/m2/classification/predictions_quick.csv
isw2/results/m2/classification/model_runs_quick.csv
isw2/results/m2/classification/classification_validation_quick.txt
```

Metriche QUICK:

```text
isw2/results/m2/metrics/metrics_quick.csv
isw2/results/m2/metrics/metrics_validation_quick.txt
```

Validazione `ONE_REP`:

```text
isw2/results/m2/full/classifier_metrics_one_rep.csv
isw2/results/m2/full/preprocessing_audit_one_rep.csv
isw2/results/m2/full/model_runs_one_rep.csv
isw2/results/m2/full/experiment_validation_one_rep.txt
```

Esecuzione FULL:

```text
isw2/results/m2/full/classifier_metrics_full.csv
isw2/results/m2/full/preprocessing_audit_full.csv
isw2/results/m2/full/model_runs_full.csv
isw2/results/m2/full/experiment_validation_full.txt
```

Summary finale:

```text
isw2/results/m2/summary/classifier_summary_full.csv
isw2/results/m2/summary/summary_validation_full.txt
```

---

# 16. Stato di avanzamento

## Completato

* [x] Milestone 1 – Dataset A generato e validato
* [x] Requisiti Milestone 2 identificati
* [x] Weka `3.8.7` e SMOTE `1.0.3` configurati
* [x] Dataset loader implementato e validato
* [x] Fold planner QUICK e FULL implementato e validato
* [x] Piano FULL riproducibile con seed `1..10`
* [x] Quattro configurazioni `C1-C4` implementate
* [x] Feature Selection `CfsSubsetEval + BestFirst`
* [x] SMOTE training-only
* [x] Pipeline anti-leakage validata
* [x] RandomForest, NaiveBayes e IBk implementati
* [x] Prediction OOF validate
* [x] Precision, Recall, AUC, Kappa e NPofB20 validate
* [x] Pipeline QUICK completata
* [x] `ONE_REP` 10-fold completata e validata
* [x] FULL `10 × 10` completato
* [x] `1200` model training completati
* [x] `120` righe raw FULL generate e validate
* [x] `400` righe preprocessing audit validate
* [x] `1200` model audit validate
* [x] Summary finale a `12` righe generato
* [x] Mean / sample StdDev / Min / Max validate
* [x] Analisi comparativa completata
* [x] Risposte alle domande della Milestone 2 definite
* [x] `BClassifier = RandomForest`

## Restano attività di repository

* [ ] Aggiornamento README con sintesi Milestone 2
* [ ] Verifica `git status`
* [ ] Commit logici della Milestone 2
* [ ] Push
* [ ] Verifica CI verde


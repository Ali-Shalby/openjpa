# Milestone 4 — Analisi del refactoring automatico di `PCEnhancer`

## 1. Obiettivo

Questa analisi valuta quattro varianti refattorizzate della classe:

`org.apache.openjpa.enhance.PCEnhancer`

a partire dalla stessa baseline `C0`, secondo il protocollo previsto dalla Milestone 4.

Le quattro condizioni sperimentali differiscono esclusivamente per il contesto di testing fornito al modello durante la generazione:

| Variante | Test forniti al modello |
| -------- | ----------------------- |
| `C1`     | Nessun test             |
| `C2`     | `T_BB`                  |
| `C3`     | `T_BB + T_CF`           |
| `C4`     | `T_BB + T_CF + T_MT`    |

Le suite automatiche `T_RND`, `T_ES` e `T_LLM` non sono state utilizzate come vincolo di generazione delle varianti.

Ogni variante è stata generata a partire da `C0`, in una nuova conversazione indipendente, senza utilizzare come input le varianti precedenti né i relativi errori di compilazione.

Le varianti sono state successivamente congelate e valutate senza eseguire cicli di correzione iterativa del codice generato.

---

## 2. Baseline `C0`

SHA-256 della baseline utilizzata nella Milestone 4:

```text
F7D85F9663B68430521FBEDE824C273971BAEF790A3C5291A9F8E0B7BC64AC7A
```

La stessa baseline è stata utilizzata come riferimento per:

- generazione delle varianti `C1`–`C4`;
- verifica della compilazione;
- analisi SonarCloud;
- confronto delle feature correlate alla bugginess.

---

## 3. Domanda 1 — Le varianti compilano?

Per ciascuna variante è stata tentata la compilazione nel sistema OpenJPA tramite:

```text
mvn -pl openjpa-kernel -am -DskipTests compile
```

### Risultati

| Variante | Compilazione | Esito                                  |
| -------- | ------------ | -------------------------------------- |
| `C0`     | PASS         | baseline compilabile                   |
| `C1`     | FAIL         | errore su `InsnList.isEmpty()`         |
| `C2`     | FAIL         | errore su `InsnList.isEmpty()`         |
| `C3`     | FAIL         | incompatibilità `String` → `String[]`  |
| `C4`     | FAIL         | campo finale `repos` non inizializzato |

### Dettaglio sintetico

#### C1

La compilazione fallisce per due invocazioni a:

```text
InsnList.isEmpty()
```

metodo non disponibile nell'API ASM utilizzata dal progetto.

Errori rilevati nelle zone corrispondenti alle linee 1084 e 1216 del sorgente generato.

#### C2

La compilazione fallisce per una invocazione residua a:

```text
InsnList.isEmpty()
```

nella zona corrispondente alla linea 1216.

#### C3

La compilazione fallisce nella costruzione di un `MethodNode`, dove viene fornita una `String` in una posizione che richiede un array:

```text
incompatible types: java.lang.String cannot be converted to java.lang.String[]
```

#### C4

La compilazione fallisce perché il campo finale:

```text
private final MetaDataRepository repos;
```

non viene inizializzato correttamente nei costruttori.

Il refactoring assegna il parametro locale invece del campo dell'istanza, omettendo `this.repos`.

### Risposta alla domanda 1

**Nessuna variante `C1`–`C4` compila.**

Di conseguenza, le suite di test fornite come vincoli di generazione non sono state rieseguite post-hoc sulle varianti non compilabili.

---

## 4. Domanda 2 — Le varianti hanno code smell? Sono vecchi o nuovi?

L'analisi è stata eseguita con SonarCloud mediante un repository ausiliario dedicato, mantenendo costante il progetto Sonar e sostituendo esclusivamente il sorgente di `PCEnhancer`.

Per ogni variante è stata salvata:

- revisione Git esatta analizzata;
- SHA-256 del sorgente;
- analysis key Sonar;
- elenco delle issue;
- riepilogo per regola;
- confronto con `C0`.

Sono state considerate esclusivamente issue:

```text
IssueStatus = OPEN
SoftwareQuality = MAINTAINABILITY
Type = CODE_SMELL
```

### Conteggio complessivo

| Variante | Compile | NSmells | Regole distinte | Δ vs C0 | Riduzione |
| -------- | ------: | ------: | --------------: | ------: | --------: |
| `C0`     | PASS    | 190     | 28              | 0       | 0,00%     |
| `C1`     | FAIL    | 138     | 24              | -52     | 27,37%    |
| `C2`     | FAIL    | 141     | 26              | -49     | 25,79%    |
| `C3`     | FAIL    | 162     | 23              | -28     | 14,74%    |
| `C4`     | FAIL    | 167     | 26              | -23     | 12,11%    |

Tutte le varianti riducono il numero totale di smell rispetto a `C0`.

La maggiore riduzione netta è ottenuta da `C1`, mentre `C3` presenta il numero minore di categorie di regole distinte.

---

## 5. Analisi old / resolved / new smell

Per distinguere le issue già presenti in `C0` da quelle eliminate o introdotte nelle varianti è stato utilizzato l'`IssueKey` di Sonar come identità operativa dell'issue.

Il confronto delle chiavi è stato effettuato con uguaglianza **case-sensitive ordinal**, poiché le `IssueKey` Sonar sono case-sensitive.

### Transizioni rispetto a `C0`

| Variante | Issue C0 | Issue variante | Retained from C0 | Resolved from C0 | New vs C0 | Delta netto |
| -------- | -------: | -------------: | ---------------: | ---------------: | --------: | ----------: |
| `C1`     | 190      | 138            | 135              | 55               | 3         | -52         |
| `C2`     | 190      | 141            | 132              | 58               | 9         | -49         |
| `C3`     | 190      | 162            | 128              | 62               | 34        | -28         |
| `C4`     | 190      | 167            | 128              | 62               | 39        | -23         |

Le equazioni di consistenza risultano verificate:

```text
Retained + Resolved = 190
Retained + New = NSmells(Cx)
New - Resolved = DeltaVsC0
```

### Nuove categorie di regole

Non viene introdotta nessuna categoria Sonar completamente nuova rispetto a `C0`.

```text
NEW RULE CATEGORIES = NONE
```

Questo non implica però assenza di nuove singole issue: una variante può introdurre una nuova issue appartenente a una regola già presente in `C0`.

### Categorie eliminate

#### C1

- `java:S108`
- `java:S1155`
- `java:S1659`
- `java:S1845`

#### C2

- `java:S116`
- `java:S1659`

#### C3

- `java:S1133`
- `java:S1135`
- `java:S116`
- `java:S1659`
- `java:S6355`

#### C4

- `java:S116`
- `java:S1659`

### Interpretazione

Le quattro varianti non si limitano a rimuovere smell preesistenti.

Al contrario:

- `C1` elimina 55 issue di `C0`, ma ne introduce 3 nuove;
- `C2` elimina 58 issue di `C0`, ma ne introduce 9 nuove;
- `C3` elimina 62 issue di `C0`, ma ne introduce 34 nuove;
- `C4` elimina 62 issue di `C0`, ma ne introduce 39 nuove.

Il valore `NEW_VS_C0` deve essere interpretato come **classificazione operativa basata sul tracking Sonar tramite `IssueKey`**, non come prova assoluta che ogni issue costituisca un nuovo difetto semantico.

### Risposta alla domanda 2

**Sì, tutte le varianti continuano ad avere code smell.**

Tutte riducono il numero totale di smell rispetto a `C0`, ma tutte introducono anche nuove singole issue Sonar.

Non viene introdotta alcuna nuova categoria di regola Sonar.

---

## 6. Domande 3 e 4 — Feature correlate alla bugginess

### Metodo

La correlazione tra predictor e variabile `BUGGY` è stata calcolata sull'intero Dataset A.

Dimensione dataset:

```text
12836 osservazioni
BUGGY=YES: 2010
BUGGY=NO : 10826
```

È stata usata la correlazione di Pearson con codifica:

```text
BUGGY=NO  -> 0
BUGGY=YES -> 1
```

Con una variabile binaria 0/1, questa misura coincide con la correlazione point-biserial.

---

## 7. Feature considerate nel confronto C0 → Cx

Le varianti `C1`–`C4` rappresentano sostituzioni controfattuali del sorgente di `C0`, non nuove release storiche.

Per questo motivo, le metriche dipendenti dalla storia Git non sono state artificialmente ricalcolate.

Sono state considerate direttamente modificabili dal refactoring:

- `LOC`
- `NSmells`

Sono state mantenute invarianti:

- `LOC_TOUCHED`
- `NR`
- `NAUTH`
- `LOC_ADDED`
- `MAX_LOC_ADDED`
- `AVG_LOC_ADDED`
- `CHURN`
- `MAX_CHURN`
- `AVG_CHURN`
- `CHANGE_SET_SIZE`
- `MAX_CHANGE_SET`
- `AVG_CHANGE_SET`
- `AGE_WEEKS`
- `WEIGHTED_AGE_WEEKS`
- `IGNORED_ZERO_LOC_REVS`
- `NFIX`

---

## 8. Correlazioni rilevanti

| Feature   | Correlazione con BUGGY | Direzione |
| --------- | ---------------------: | --------- |
| `LOC`     | +0,3748 circa          | positiva  |
| `NSmells` | +0,3527 circa          | positiva  |

Entrambe le feature direttamente modificabili dal refactoring risultano quindi positivamente correlate con la bugginess nel Dataset A.

---

## 9. Valori C0–C4

| Variante | LOC  | NSmells |
| -------- | ---: | ------: |
| `C0`     | 3958 | 190     |
| `C1`     | 3986 | 138     |
| `C2`     | 3986 | 141     |
| `C3`     | 3961 | 162     |
| `C4`     | 3961 | 167     |

### Delta LOC

| Variante | Δ LOC vs C0 |
| -------- | ----------: |
| `C1`     | +28         |
| `C2`     | +28         |
| `C3`     | +3          |
| `C4`     | +3          |

### Delta NSmells

| Variante | Δ NSmells vs C0 |
| -------- | --------------: |
| `C1`     | -52             |
| `C2`     | -49             |
| `C3`     | -28             |
| `C4`     | -23             |

---

## 10. Domanda 3

> Is any positively bug-correlated feature higher in C_X than C0?

### Risposta

**YES per tutte le varianti.**

`LOC` è positivamente correlata con `BUGGY` e aumenta in tutte le varianti:

```text
C1: 3958 -> 3986 (+28)
C2: 3958 -> 3986 (+28)
C3: 3958 -> 3961 (+3)
C4: 3958 -> 3961 (+3)
```

Contemporaneamente `NSmells`, anch'essa positivamente correlata alla bugginess, diminuisce in tutte le varianti.

Il risultato è quindi misto: una feature positivamente correlata migliora (`NSmells` diminuisce), mentre un'altra peggiora (`LOC` aumenta).

---

## 11. Domanda 4

> Is any negatively bug-correlated feature higher in C_X than C0?

### Risposta

**NO.**

Nessuna feature negativamente correlata e direttamente modificabile dal refactoring aumenta.

Le feature negativamente correlate presenti nel dataset appartengono principalmente alla storia evolutiva/processuale della classe e sono state mantenute invarianti, poiché `C1`–`C4` non costituiscono nuove release storiche.

---

## 12. Matrice finale delle quattro domande M4

| Variante | 1. Compila? | 2. Code smell                  | 3. Feature +corr più alta? | 4. Feature -corr più alta? |
| -------- | ----------- | ------------------------------ | -------------------------- | -------------------------- |
| `C1`     | **FAIL**    | 138; -52 vs C0; 3 nuove issue  | **YES** — `LOC +28`        | **NO**                     |
| `C2`     | **FAIL**    | 141; -49 vs C0; 9 nuove issue  | **YES** — `LOC +28`        | **NO**                     |
| `C3`     | **FAIL**    | 162; -28 vs C0; 34 nuove issue | **YES** — `LOC +3`         | **NO**                     |
| `C4`     | **FAIL**    | 167; -23 vs C0; 39 nuove issue | **YES** — `LOC +3`         | **NO**                     |

---

## 13. Interpretazione complessiva

L'esperimento mostra che il refactoring automatico produce un miglioramento apparente della maintainability statica misurata tramite il numero totale di smell Sonar, ma non produce una variante utilizzabile del sistema.

In particolare:

1. nessuna variante `C1`–`C4` compila;
2. tutte le varianti riducono il numero totale di code smell;
3. tutte le varianti eliminano issue presenti in `C0`;
4. tutte introducono anche nuove singole issue Sonar;
5. nessuna introduce nuove categorie di regole;
6. `LOC`, positivamente correlata alla bugginess, aumenta in tutte le varianti;
7. `NSmells`, anch'essa positivamente correlata alla bugginess, diminuisce in tutte le varianti.

La riduzione degli smell non può quindi essere usata isolatamente come indicatore di successo del refactoring.

La preservazione della compilabilità e del comportamento del sistema rimane un requisito fondamentale.

---

## 14. Confronto tra le condizioni sperimentali

La variante `C1`, generata senza test come contesto, presenta il miglior saldo netto Sonar:

```text
190 -> 138 smell
Delta = -52
```

La variante `C4`, generata con il contesto di testing più ricco, presenta invece la riduzione netta più contenuta:

```text
190 -> 167 smell
Delta = -23
```

Inoltre il numero di nuove issue rispetto a `C0` cresce:

```text
C1 = 3
C2 = 9
C3 = 34
C4 = 39
```

Questa sequenza costituisce un **risultato osservato nel presente esperimento**, ma non permette di concludere che l'aumento del numero di test forniti al modello causi un peggioramento del refactoring.

Ogni condizione dispone infatti di una singola generazione e non è stata eseguita una campagna statistica con repliche multiple.

---

## 15. Limiti metodologici

L'interpretazione dei risultati deve tenere conto dei seguenti limiti:

- una sola generazione per ciascuna condizione `C1`–`C4`;
- impossibilità di eseguire le suite post-hoc sulle varianti a causa dei fallimenti di compilazione;
- classificazione `old/new` basata sul tracking operativo delle `IssueKey` Sonar;
- metriche storiche mantenute invarianti nel confronto controfattuale;
- correlazione con `BUGGY` interpretata come associazione statistica, non come relazione causale.

---

## 16. Conclusione

Per `PCEnhancer`, nessuna delle quattro condizioni sperimentali produce un refactoring complessivamente riuscito.

Sebbene tutte le varianti riducano i code smell Sonar rispetto alla baseline, tutte compromettono la compilabilità della classe.

Inoltre, tutte aumentano almeno una feature positivamente correlata alla bugginess (`LOC`) e introducono nuove singole issue Sonar.

Il risultato evidenzia quindi che un miglioramento locale delle metriche statiche non è sufficiente a garantire un refactoring corretto, compatibile e utilizzabile nel sistema.

La valutazione deve considerare congiuntamente:

- correttezza strutturale;
- compilabilità;
- preservazione del comportamento;
- qualità statica;
- impatto sulle feature associate alla bugginess.

---

## 17. Evidenze prodotte

### Generazione e compilazione

Le varianti congelate e le relative evidenze sono conservate in:

```text
isw2/results/m4/pcenhancer/c1/
isw2/results/m4/pcenhancer/c2/
isw2/results/m4/pcenhancer/c3/
isw2/results/m4/pcenhancer/c4/
```

Per tutte le varianti il sorgente generato è conservato come:

```text
PCEnhancer.java
```

e l'evidenza di compilazione è conservata in:

```text
raw/compile.txt
```

Gli input e le risposte Copilot preservati differiscono leggermente per nome perché
sono stati salvati durante le singole esecuzioni sperimentali:

```text
C1:
  raw/PCEnhancer-C0.java
  raw/PCEnhancer-C0.txt
  raw/pcenhancer-sonar-metrics.csv
  raw/pcenhancer-sonar-smells.csv
  raw/compile.txt

C2:
  raw/PCEnhancer-C0.txt
  raw/pcenhancer-sonar-metrics.csv
  raw/pcenhancer-sonar-smells.csv
  raw/pcenhancer-tbb-tests.txt
  raw/copilot-response.txt
  raw/compile.txt

C3:
  raw/PCEnhancer-C0.txt
  raw/pcenhancer-sonar-metrics.csv
  raw/pcenhancer-sonar-smells.csv
  raw/pcenhancer-tbb-tcf-tests.txt
  raw/copilot-response-c3.txt
  raw/compile.txt

C4:
  raw/PCEnhancer-C0.txt
  raw/pcenhancer-sonar-metrics.csv
  raw/pcenhancer-sonar-smells.csv
  raw/pcenhancer-tbb-tcf-tmt-tests.txt
  raw/copilot-response-c4.txt
  raw/compile.txt
```

Per `C1` la risposta completa della conversazione Copilot non è stata preservata:
la conversazione era stata eliminata dopo la generazione. La variante non è stata
rigenerata e la risposta non è stata ricostruita artificialmente. Restano invece
preservati il sorgente `C1` congelato, gli input disponibili e l'evidenza di
compilazione.

Poiché `C1`–`C4` non compilano, le suite dinamiche non sono state eseguite sulle
varianti: il relativo esito è `NOT RUN / BLOCKED BY COMPILATION`, non `FAIL`.

### Sonar

```text
isw2/results/m4/pcenhancer/sonar/c0/
isw2/results/m4/pcenhancer/sonar/c1/
isw2/results/m4/pcenhancer/sonar/c2/
isw2/results/m4/pcenhancer/sonar/c3/
isw2/results/m4/pcenhancer/sonar/c4/
isw2/results/m4/pcenhancer/sonar/comparison/
```

Principali artifact di confronto:

```text
comparison-summary.csv
issue-transition-summary.csv
issue-transitions-vs-c0.csv
rule-category-transitions.csv
rule-comparison.csv
comparison.txt
```

### Feature correlate alla bugginess

```text
isw2/results/m4/pcenhancer/features/
```

Artifact:

```text
bugginess-correlation.csv
variant-feature-values.csv
feature-comparison-vs-c0.csv
analysis.txt
```

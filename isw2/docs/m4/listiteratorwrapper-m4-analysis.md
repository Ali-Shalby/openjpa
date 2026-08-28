# Milestone 4 — Analisi del refactoring automatico di `ListIteratorWrapper`

## 1. Obiettivo

Questa analisi valuta quattro varianti refattorizzate della classe:

`org.apache.openjpa.lib.util.collections.ListIteratorWrapper`

a partire dalla stessa baseline `C0`, secondo il protocollo previsto dalla Milestone 4.

Le quattro condizioni sperimentali differiscono esclusivamente per il contesto di testing fornito al modello durante la generazione:

| Variante | Test forniti al modello |
| -------- | ----------------------- |
| `C1`     | Nessun test             |
| `C2`     | `T_BB`                  |
| `C3`     | `T_BB + T_CF`           |
| `C4`     | `T_BB + T_CF + T_MT`    |

Le suite automatiche `T_RND`, `T_ES` e `T_LLM` non sono state utilizzate come vincolo di generazione delle varianti.

Ogni variante è stata generata a partire da `C0`, in una nuova conversazione indipendente, senza utilizzare come input le varianti precedenti né i risultati ottenuti sulle altre condizioni.

Le varianti sono state successivamente congelate e valutate senza cicli di correzione iterativa del codice generato.

Poiché la baseline `C0` presentava già `NSmells = 0`, l'obiettivo di generazione non è stato formulato come riduzione artificiale di smell inesistenti, ma come miglioramento ragionevole della manutenibilità strutturale, mantenendo invariati comportamento osservabile, API pubblica, package e compatibilità con il resto di OpenJPA. La valutazione Sonar resta comunque parte integrante del protocollo M4.

---

## 2. Baseline `C0`

SHA-256 della baseline utilizzata nella Milestone 4:

```text
44CE059F3834F781ECE28CFA0253E8EA17F6298B9632E0F79B44DFD2CC09EE44
```

La baseline corrisponde al sorgente production di OpenJPA 4.1.1 per:

```text
openjpa-lib/src/main/java/org/apache/openjpa/lib/util/collections/ListIteratorWrapper.java
```

La stessa baseline è stata utilizzata come riferimento per:

- generazione delle varianti `C1`–`C4`;
- verifica della compilazione;
- esecuzione post-hoc della suite manuale congelata;
- analisi SonarCloud;
- confronto delle feature correlate alla bugginess.

SHA-256 delle varianti congelate:

| Variante | SHA-256                                                            |
| -------- | ------------------------------------------------------------------ |
| `C1`     | `E1F56231EEFCF83EDBEEBB751ABA162CCADE5774E1F973932BC2F6868DA68579` |
| `C2`     | `1280FB36A0CD4E40B9B99FEAA23B6939D72B3D75DB18D0B700D0D79D34F51065` |
| `C3`     | `31E6F2F681A141B8973D9C1E36E5BD7EB00938AFBB52ADD5F87B70216EF28A44` |
| `C4`     | `C9506C2C0BC998434D2BA33B48F653959B5B29ED56C342FD6E59594F854FAB3D` |

---

## 3. Domanda 1 — Le varianti compilano?

Per ciascuna variante è stata verificata la compilazione nel modulo `openjpa-lib` del sistema OpenJPA.

Il sorgente della variante è stato installato temporaneamente al posto di `C0`, compilato nel progetto reale e poi congelato come risultato sperimentale.

### Risultati

| Variante | Compilazione | Esito                |
| -------- | ------------ | -------------------- |
| `C0`     | PASS         | baseline compilabile |
| `C1`     | PASS         | variante compilabile |
| `C2`     | PASS         | variante compilabile |
| `C3`     | PASS         | variante compilabile |
| `C4`     | PASS         | variante compilabile |

### Verifica comportamentale post-hoc

Poiché tutte le varianti compilano, è stata rieseguita su ciascuna la suite manuale congelata completa:

```text
T_BB = 12 test
T_CF = 5 test
T_MT = 2 test
Totale = 19 test
```

Risultati:

| Variante | Test eseguiti | PASS | FAIL | ERROR | SKIPPED |
| -------- | ------------: | ---: | ---: | ----: | ------: |
| `C1`     | 19            | 19   | 0    | 0     | 0       |
| `C2`     | 19            | 19   | 0    | 0     | 0       |
| `C3`     | 19            | 19   | 0    | 0     | 0       |
| `C4`     | 19            | 19   | 0    | 0     | 0       |

La suite post-hoc non è stata utilizzata per correggere le varianti: viene impiegata esclusivamente come verifica successiva al freeze.

### Risposta alla domanda 1

**Sì. Tutte le varianti `C1`–`C4` compilano e tutte superano 19/19 test manuali post-hoc.**

---

## 4. Domanda 2 — Le varianti hanno code smell? Sono vecchi o nuovi?

L'analisi è stata eseguita con SonarCloud mediante un repository ausiliario dedicato, mantenendo costante il progetto Sonar e sostituendo esclusivamente il sorgente di `ListIteratorWrapper`.

Per evitare confronti tra analisi non omogenee, anche `C0` è stata rianalizzata con le stesse regole Sonar correnti utilizzate per `C1`–`C4`.

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

| Variante | Compile | Post-hoc   | NSmells | Regole distinte | Δ vs C0 |
| -------- | ------- | ---------- | ------: | --------------: | ------: |
| `C0`     | PASS    | baseline   | 0       | 0               | 0       |
| `C1`     | PASS    | 19/19 PASS | 1       | 1               | +1      |
| `C2`     | PASS    | 19/19 PASS | 0       | 0               | 0       |
| `C3`     | PASS    | 19/19 PASS | 1       | 1               | +1      |
| `C4`     | PASS    | 19/19 PASS | 0       | 0               | 0       |

La baseline è quindi confermata a zero smell anche con l'analisi Sonar uniforme della M4.

`C1` e `C3` introducono una singola issue ciascuna; `C2` e `C4` rimangono a zero.

### Revisioni Sonar analizzate

| Variante | Sonar revision                             | Analysis key                           |
| -------- | ------------------------------------------ | -------------------------------------- |
| `C0`     | `b84a880cb205013a29add98e3a00f6abe3797c4d` | `6f373dee-c04d-4760-81ac-41b86b6877e2` |
| `C1`     | `a4bedc4ebde0214b1aee15cfb402bf71cc66a65f` | `448d637d-71f0-467a-8aa8-849fb9eaa26c` |
| `C2`     | `e56019dc4c27c94aa8509d8fdbf6bc39abe23b5d` | `6c084a62-1c35-49d8-9e94-a23691bf28d6` |
| `C3`     | `8815e9d1b9dc27bd9e036b71b97cff9371e1b5ed` | `eb010eb4-3407-4e6b-86b8-457effa13fbf` |
| `C4`     | `d0f32eecee19fb2d56b2f125029173b6f67bb57b` | `f73af8a2-0cda-4af2-b95b-3f664f0f0107` |

---

## 5. Analisi old / resolved / new smell

Per distinguere le issue già presenti in `C0` da quelle eliminate o introdotte nelle varianti è stato utilizzato l'`IssueKey` di Sonar come identità operativa dell'issue.

Il confronto delle chiavi è stato effettuato con uguaglianza **case-sensitive ordinal**, poiché le `IssueKey` Sonar sono case-sensitive.

### Transizioni rispetto a `C0`

| Variante | Issue C0 | Issue variante | Retained from C0 | Resolved from C0 | New vs C0 | Delta netto |
| -------- | -------: | -------------: | ---------------: | ---------------: | --------: | ----------: |
| `C1`     | 0        | 1              | 0                | 0                | 1         | +1          |
| `C2`     | 0        | 0              | 0                | 0                | 0         | 0           |
| `C3`     | 0        | 1              | 0                | 0                | 1         | +1          |
| `C4`     | 0        | 0              | 0                | 0                | 0         | 0           |

Poiché `C0` contiene zero issue Sonar, non esistono smell originari che possano essere mantenuti o risolti.

### Nuove categorie di regole

`C1` e `C3` introducono la stessa categoria di regola:

```text
java:S1125
```

Messaggio Sonar:

```text
Remove the unnecessary boolean literal.
```

La issue è localizzata:

```text
C1: linea 145
C3: linea 136
```

`C2` e `C4` non introducono alcuna categoria di regola.

### Interpretazione

Il risultato osservato è quindi:

- `C1`: 1 nuova issue, regola `java:S1125`;
- `C2`: nessuna issue;
- `C3`: 1 nuova issue, regola `java:S1125`;
- `C4`: nessuna issue.

Il valore `NEW_VS_C0` deve essere interpretato come **classificazione operativa basata sul tracking Sonar tramite `IssueKey`**, non come prova di un nuovo difetto funzionale.

Nel presente caso, infatti, sia `C1` sia `C3` compilano e superano la suite manuale post-hoc completa.

### Risposta alla domanda 2

**C1 e C3 introducono un nuovo code smell ciascuna. C2 e C4 non presentano code smell.**

Non esistono smell vecchi da conservare o rimuovere perché `C0` presenta `NSmells = 0`.

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

La misura `LOC` è stata calcolata con la stessa logica utilizzata per la costruzione delle metriche sorgente del progetto, contando le linee contenenti codice e gestendo commenti, stringhe e literal carattere.

---

## 8. Correlazioni rilevanti

| Feature   | Correlazione con BUGGY | Direzione |
| --------- | ---------------------: | --------- |
| `LOC`     | +0,374815228649        | positiva  |
| `NSmells` | +0,352745614334        | positiva  |

Entrambe le feature direttamente modificabili dal refactoring risultano quindi positivamente correlate con la bugginess nel Dataset A.

---

## 9. Valori C0–C4

| Variante | LOC | NSmells |
| -------- | ---: | ------: |
| `C0`     | 134 | 0       |
| `C1`     | 134 | 1       |
| `C2`     | 134 | 0       |
| `C3`     | 132 | 1       |
| `C4`     | 139 | 0       |

### Delta LOC

| Variante | Δ LOC vs C0 |
| -------- | ----------: |
| `C1`     | 0           |
| `C2`     | 0           |
| `C3`     | -2          |
| `C4`     | +5          |

### Delta NSmells

| Variante | Δ NSmells vs C0 |
| -------- | --------------: |
| `C1`     | +1              |
| `C2`     | 0               |
| `C3`     | +1              |
| `C4`     | 0               |

---

## 10. Domanda 3

> Is any positively bug-correlated feature higher in C_X than C0?

### Risposta

Il risultato dipende dalla variante.

#### C1 — YES

`NSmells` è positivamente correlata con `BUGGY` e aumenta:

```text
NSmells: 0 -> 1 (+1)
LOC     : 134 -> 134 (0)
```

#### C2 — NO

Nessuna delle due feature snapshot positivamente correlate aumenta:

```text
NSmells: 0 -> 0 (0)
LOC     : 134 -> 134 (0)
```

#### C3 — YES

`NSmells` aumenta, anche se `LOC` diminuisce:

```text
NSmells: 0 -> 1 (+1)
LOC     : 134 -> 132 (-2)
```

Il risultato è quindi misto: una feature positivamente correlata migliora (`LOC` diminuisce), mentre un'altra peggiora (`NSmells` aumenta).

#### C4 — YES

`LOC` aumenta, mentre `NSmells` rimane invariata:

```text
LOC     : 134 -> 139 (+5)
NSmells: 0 -> 0 (0)
```

### Sintesi domanda 3

```text
C1: YES — NSmells +1
C2: NO
C3: YES — NSmells +1
C4: YES — LOC +5
```

---

## 11. Domanda 4

> Is any negatively bug-correlated feature higher in C_X than C0?

### Risposta

**NO per tutte le varianti.**

Nessuna feature negativamente correlata e direttamente modificabile dal refactoring aumenta.

Le feature negativamente correlate presenti nel dataset appartengono alla storia evolutiva/processuale della classe e sono state mantenute invarianti, poiché `C1`–`C4` non costituiscono nuove release storiche.

---

## 12. Matrice finale delle quattro domande M4

| Variante | 1. Compila? | 2. Code smell          | 3. Feature +corr più alta? | 4. Feature -corr più alta? |
| -------- | ----------- | ---------------------- | -------------------------- | -------------------------- |
| `C1`     | **PASS**    | 1 nuova issue `S1125`  | **YES** — `NSmells +1`     | **NO**                     |
| `C2`     | **PASS**    | 0; nessuna nuova issue | **NO**                     | **NO**                     |
| `C3`     | **PASS**    | 1 nuova issue `S1125`  | **YES** — `NSmells +1`     | **NO**                     |
| `C4`     | **PASS**    | 0; nessuna nuova issue | **YES** — `LOC +5`         | **NO**                     |

Tutte le varianti compilabili superano inoltre la suite manuale post-hoc:

```text
C1 = 19/19 PASS
C2 = 19/19 PASS
C3 = 19/19 PASS
C4 = 19/19 PASS
```

---

## 13. Interpretazione complessiva

L'esperimento su `ListIteratorWrapper` produce un risultato molto diverso rispetto a un semplice confronto di smell.

In particolare:

1. tutte le varianti `C1`–`C4` compilano;
2. tutte superano 19/19 test manuali post-hoc;
3. `C0` parte già da zero code smell;
4. `C1` e `C3` introducono una singola issue `java:S1125`;
5. `C2` e `C4` mantengono `NSmells = 0`;
6. `C1` mantiene invariato il numero di LOC ma aumenta `NSmells`;
7. `C2` mantiene invariati sia `LOC` sia `NSmells`;
8. `C3` riduce `LOC` di 2 unità ma aumenta `NSmells`;
9. `C4` mantiene zero smell ma aumenta `LOC` di 5 unità.

La compilabilità e il superamento della suite congelata indicano che, nei comportamenti coperti dall'esperimento, nessuna delle quattro varianti mostra una regressione osservata.

Tuttavia, la qualità statica e le feature associate alla bugginess differenziano chiaramente le condizioni.

---

## 14. Confronto tra le condizioni sperimentali

Tra le quattro condizioni, `C2` presenta il profilo migliore rispetto ai criteri M4 osservati:

```text
Compilation : PASS
Post-hoc    : 19/19 PASS
NSmells     : 0
LOC         : 134
Q3          : NO
Q4          : NO
```

`C1` e `C3` introducono entrambe una nuova issue `java:S1125`.

`C4` mantiene zero smell, ma aumenta `LOC` da 134 a 139; poiché `LOC` è positivamente correlata con la bugginess, la risposta alla domanda 3 è `YES`.

Il risultato osservato per le quattro condizioni è:

```text
C1: compile PASS; tests PASS; +1 smell; LOC invariata
C2: compile PASS; tests PASS;  0 smell; LOC invariata
C3: compile PASS; tests PASS; +1 smell; LOC -2
C4: compile PASS; tests PASS;  0 smell; LOC +5
```

Questo rende `C2` la variante più favorevole **nel presente esperimento e rispetto alle metriche considerate**.

Non è però metodologicamente corretto concludere che fornire esclusivamente `T_BB` al modello causi un refactoring migliore.

Ogni condizione dispone infatti di una singola generazione e non è stata eseguita una campagna statistica con repliche multiple.

---

## 15. Limiti metodologici

L'interpretazione dei risultati deve tenere conto dei seguenti limiti:

- una sola generazione per ciascuna condizione `C1`–`C4`;
- la suite manuale post-hoc verifica i comportamenti coperti dai 19 test congelati, non l'equivalenza semantica completa tra `C0` e `Cx`;
- classificazione `old/new` basata sul tracking operativo delle `IssueKey` Sonar;
- baseline Sonar già pari a zero smell, quindi non esiste margine per una riduzione numerica di `NSmells`;
- metriche storiche mantenute invarianti nel confronto controfattuale;
- correlazione con `BUGGY` interpretata come associazione statistica, non come relazione causale;
- il confronto tra quantità di test forniti al modello è descrittivo e non permette inferenze causali con una sola generazione per condizione.

---

## 16. Conclusione

Per `ListIteratorWrapper`, tutte le quattro condizioni sperimentali producono varianti compilabili che superano la suite manuale post-hoc completa.

La baseline `C0` presenta già zero code smell Sonar. In questo contesto:

- `C1` introduce una nuova issue `java:S1125`;
- `C2` mantiene zero smell e non aumenta `LOC`;
- `C3` introduce una nuova issue `java:S1125`, pur riducendo `LOC`;
- `C4` mantiene zero smell ma aumenta `LOC`.

Tra le quattro varianti, `C2` presenta il risultato complessivamente più favorevole rispetto ai criteri M4 misurati: compila, supera 19/19 test, non introduce smell e non aumenta alcuna feature snapshot positivamente correlata alla bugginess.

Il risultato conferma che la valutazione di un refactoring automatico non può essere ridotta a una sola metrica.

Devono essere considerate congiuntamente:

- compilabilità;
- preservazione del comportamento osservato;
- qualità statica;
- introduzione o rimozione di issue;
- impatto sulle feature associate alla bugginess.

---

## 17. Evidenze prodotte

### Generazione, compilazione e test post-hoc

Le varianti congelate e le relative evidenze sono conservate in:

```text
isw2/results/m4/listiteratorwrapper/c1/
isw2/results/m4/listiteratorwrapper/c2/
isw2/results/m4/listiteratorwrapper/c3/
isw2/results/m4/listiteratorwrapper/c4/
```

Per tutte le varianti il sorgente generato è:

```text
ListIteratorWrapper.java
```

Gli artifact reali preservati nelle directory `raw/` sono:

```text
C1:
  ListIteratorWrapper-C0.txt
  sonar-baseline.csv
  copilot-response-c1.md
  compile.txt
  tests.txt

C2:
  ListIteratorWrapper-C0.txt
  sonar-baseline.csv
  listiteratorwrapper-tbb-tests.txt
  copilot-response-c2.md
  compile.txt
  tests.txt

C3:
  ListIteratorWrapper-C0.txt
  sonar-baseline.csv
  listiteratorwrapper-tbb-tcf-tests.txt
  copilot-response-c3.md
  compile.txt
  tests.txt

C4:
  ListIteratorWrapper-C0.txt
  sonar-baseline.csv
  listiteratorwrapper-tbb-tcf-tmt-tests.txt
  copilot-response-c4.md
  compile.txt
  tests.txt
```

Le quattro varianti compilano e la suite manuale post-hoc completa è stata
eseguita su ciascuna, con risultato `19/19 PASS`.

### Sonar

```text
isw2/results/m4/listiteratorwrapper/sonar/c0/
isw2/results/m4/listiteratorwrapper/sonar/c1/
isw2/results/m4/listiteratorwrapper/sonar/c2/
isw2/results/m4/listiteratorwrapper/sonar/c3/
isw2/results/m4/listiteratorwrapper/sonar/c4/
isw2/results/m4/listiteratorwrapper/sonar/comparison/
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
isw2/results/m4/listiteratorwrapper/features/
```

Artifact:

```text
bugginess-correlation.csv
variant-feature-values.csv
feature-comparison-vs-c0.csv
analysis.txt
```

# PCEnhancer — Confronto globale C0–C4 delle suite automatiche

## Scopo

Questo documento riassume il confronto finale relativo a
`org.apache.openjpa.enhance.PCEnhancer` per le configurazioni **C0–C4** e per le
suite automatiche:

- **T_RND** — Randoop;
- **T_ES** — EvoSuite;
- **T_LLM** — LLM.

Per PCEnhancer il confronto C0–C4 è necessariamente diverso da quello svolto per
`ListIteratorWrapper`: le varianti refactored **C1, C2, C3 e C4 non compilano**.
Di conseguenza non è disponibile bytecode valido delle varianti refactored e non
è metodologicamente corretto produrre o misurare suite automatiche runtime
contro tali configurazioni.

Le configurazioni C1–C4 sono quindi registrate come:

**NOT RUN / BLOCKED BY VARIANT COMPILATION**

e non come semplicemente "non eseguite".

---

## 1. Stato delle varianti refactored

| Variante | Compilazione | Blocco osservato | Suite automatiche runtime |
|---|---|---|---|
| C0 | PASS | — | RND / ES / LLM misurate |
| C1 | FAIL | uso di `InsnList.isEmpty()` non disponibile; due errori di compilazione | NOT RUN / BLOCKED BY VARIANT COMPILATION |
| C2 | FAIL | uso di `InsnList.isEmpty()` non disponibile; un errore di compilazione | NOT RUN / BLOCKED BY VARIANT COMPILATION |
| C3 | FAIL | incompatibilità nel campo `exceptions` di `MethodNode` (`String` / `String[]`) | NOT RUN / BLOCKED BY VARIANT COMPILATION |
| C4 | FAIL | variabile finale `repos` potenzialmente non inizializzata | NOT RUN / BLOCKED BY VARIANT COMPILATION |

### Conseguenza sperimentale

Le suite Randoop ed EvoSuite richiedono bytecode valido della classe target.
Poiché C1–C4 non producono bytecode compilabile, la generazione e la successiva
misurazione di coverage e mutation testing non possono essere eseguite in modo
valido.

Per T_LLM sarebbe tecnicamente possibile ottenere codice di test testuale a
partire dal sorgente, ma tale suite non potrebbe essere compilata ed eseguita
contro la variante target. Non sarebbe quindi possibile validarla, congelarla e
misurarne l'adequacy con lo stesso protocollo usato per C0 e per
`ListIteratorWrapper`.

Per evitare risultati non confrontabili o artefatti costruiti su bytecode stale,
le configurazioni C1–C4 vengono lasciate esplicitamente bloccate.

---

# 2. Baseline C0 — risultati delle suite automatiche

Le tre suite C0 sono state misurate a parità di cardinalità, con **N = 30 test**.

| Metrica | T_RND | T_ES | T_LLM |
|---|---:|---:|---:|
| Test | 30/30 PASS | 30/30 PASS | 30/30 PASS |
| Line coverage | 1.96% | 3.00% | **4.41%** |
| Branch coverage | 0.82% | 1.56% | **3.53%** |
| Mutanti uccisi | 2 | 10 | **39** |
| Mutation score | 0.12% | 0.59% | **2.29%** |
| Test strength | 40.00% | 71.43% | **86.67%** |
| Logic LOC | 473 | 512 | **458** |
| Support LOC | 25 | 892 | **0** |
| Total LOC | 498 | 1404 | **458** |
| Test | 30 | 30 | 30 |
| Assertion-like | 63 | 30 | 30 |
| Assertion/test | 2.10 | 1.00 | 1.00 |
| Nomi opachi | 30 | 30 | **0** |
| Nomi descrittivi | 0% | 0% | **100%** |
| Sonar code smell | 57 | **0** | 6 |

---

# 3. T_RND — C0 e blocco C1–C4

## C0

Randoop su C0 raggiunge:

- line coverage: 1.96%;
- branch coverage: 0.82%;
- 2 mutanti uccisi;
- mutation score: 0.12%;
- test strength: 40.00%.

La suite contiene:

- 473 LOC di logica;
- 25 LOC di supporto;
- 498 LOC complessive;
- 63 assertion-like statements;
- 2.10 assertion per test;
- 30/30 nomi opachi.

SonarQube Cloud rileva **57 code smell**.

### Regole Sonar C0 — T_RND

| Regola | Issue | Severity | Clean Code attribute | Categoria |
|---|---:|---|---|---|
| `java:S1481` | 17 | LOW | CLEAR | INTENTIONAL |
| `java:S1854` | 17 | MEDIUM | LOGICAL | INTENTIONAL |
| `java:S3415` | 5 | MEDIUM | LOGICAL | INTENTIONAL |
| `java:S3577` | 1 | LOW | IDENTIFIABLE | CONSISTENT |
| `java:S5785` | 16 | MEDIUM | CLEAR | INTENTIONAL |
| `java:S5976` | 1 | MEDIUM | CONVENTIONAL | CONSISTENT |

## C1–C4

Per tutte le varianti refactored:

**NOT RUN / BLOCKED BY VARIANT COMPILATION**

Non sono quindi disponibili delta validi di coverage, mutation score, test
strength, chiarezza o Sonar della suite Randoop rispetto a C0.

---

# 4. T_ES — C0 e blocco C1–C4

## C0

EvoSuite su C0 raggiunge:

- line coverage: 3.00%;
- branch coverage: 1.56%;
- 10 mutanti uccisi;
- mutation score: 0.59%;
- test strength: 71.43%.

La suite contiene:

- 512 LOC di logica;
- 892 LOC di scaffolding;
- 1404 LOC complessive;
- 30 assertion-like statements;
- 1.00 assertion per test;
- 30/30 nomi opachi.

SonarQube Cloud non rileva code smell sui file di test analizzati:

- UTS verificati: 4/4;
- code smell: 0.

## C1–C4

Per tutte le varianti refactored:

**NOT RUN / BLOCKED BY VARIANT COMPILATION**

Non sono quindi disponibili delta validi di coverage, mutation score, test
strength, chiarezza o Sonar della suite EvoSuite rispetto a C0.

---

# 5. T_LLM — C0 e blocco C1–C4

## C0

T_LLM su C0 raggiunge:

- line coverage: 4.41%;
- branch coverage: 3.53%;
- 39 mutanti uccisi;
- mutation score: 2.29%;
- test strength: 86.67%;
- stabilità: 5/5.

La suite contiene:

- 458 LOC di logica;
- 0 LOC di supporto;
- 458 LOC complessive;
- 30 assertion-like statements;
- 1.00 assertion per test;
- 0/30 nomi opachi;
- 100% nomi descrittivi.

SonarQube Cloud rileva **6 code smell**.

### Regole Sonar C0 — T_LLM

| Regola | Issue | Severity | Clean Code attribute | Categoria |
|---|---:|---|---|---|
| `java:S1068` | 1 | MEDIUM | CLEAR | INTENTIONAL |
| `java:S1128` | 1 | LOW | CLEAR | INTENTIONAL |
| `java:S8924` | 4 | LOW | CLEAR | INTENTIONAL |

## C1–C4

La generazione puramente testuale tramite LLM sarebbe in astratto possibile, ma
la suite risultante non potrebbe essere:

- compilata contro la variante;
- validata runtime;
- sottoposta a stability check;
- misurata con JaCoCo;
- misurata con PIT.

Per mantenere lo stesso protocollo sperimentale adottato altrove, C1–C4 sono
quindi registrate come:

**NOT RUN / BLOCKED BY VARIANT COMPILATION**

e non vengono usate per produrre pseudo-delta non verificabili.

---

# 6. Confronto interno C0

Sulla baseline compilabile C0 l'ordine osservato è:

## Adequacy

**T_LLM > T_ES > T_RND**

sia per coverage sia per mutation testing.

Pur restando bassi in valore assoluto a causa delle dimensioni e della
complessità di `PCEnhancer`, i risultati mostrano:

- T_RND: mutation score 0.12%;
- T_ES: mutation score 0.59%;
- T_LLM: mutation score 2.29%.

La stessa progressione compare nella test strength:

- T_RND: 40.00%;
- T_ES: 71.43%;
- T_LLM: 86.67%.

## Chiarezza

- T_RND: 0% nomi descrittivi;
- T_ES: 0% nomi descrittivi;
- T_LLM: 100% nomi descrittivi.

T_LLM è quindi la tecnica con naming più leggibile e direttamente riconducibile
all'intento del test.

## Manutenibilità

La dimensione complessiva delle suite è:

- T_RND: 498 LOC;
- T_ES: 1404 LOC;
- T_LLM: 458 LOC.

EvoSuite è molto più estesa a causa delle 892 LOC di scaffolding. T_LLM è la
suite più compatta e non richiede supporto separato.

Per code smell Sonar:

- T_RND: 57;
- T_ES: 0;
- T_LLM: 6.

T_ES presenta quindi il miglior risultato Sonar statico, mentre T_LLM combina
dimensione ridotta, naming descrittivo e un backlog contenuto.

---

# 7. Delta C0–C4: interpretazione corretta

Per `PCEnhancer` non è possibile compilare una matrice numerica C0–C4
equivalente a quella di `ListIteratorWrapper`.

Il risultato sperimentale sulle configurazioni refactored è esso stesso un
risultato:

| Tecnica | C0 | C1 | C2 | C3 | C4 |
|---|---|---|---|---|---|
| T_RND | misurata | BLOCKED | BLOCKED | BLOCKED | BLOCKED |
| T_ES | misurata | BLOCKED | BLOCKED | BLOCKED | BLOCKED |
| T_LLM | misurata | BLOCKED | BLOCKED | BLOCKED | BLOCKED |

Dove **BLOCKED** significa:

`NOT RUN / BLOCKED BY VARIANT COMPILATION`

Di conseguenza:

- **Coverage delta C0→Cx:** non disponibile;
- **Mutation Score delta C0→Cx:** non disponibile;
- **Test Strength delta C0→Cx:** non disponibile;
- **Chiarezza/manutenibilità della nuova suite Cx:** non misurabile secondo il protocollo;
- **Sonar della nuova suite Cx:** non disponibile perché non esiste una suite Cx validata e congelata.

Questo evita di confondere "assenza di miglioramento" con "assenza di una
misurazione valida".

---

# 8. Conclusione finale

Il caso `PCEnhancer` evidenzia un limite importante del processo di refactoring
automatico: **la preservazione della compilabilità è una precondizione per la
valutazione dinamica successiva**.

Le varianti C1–C4 falliscono tutte il gate di compilazione. Di conseguenza non
è possibile applicare in modo metodologicamente valido la seconda fase
dell'esperimento automatico su tali configurazioni.

Sulla sola baseline C0:

- T_LLM presenta la migliore adequacy e la migliore chiarezza del naming;
- T_ES presenta il miglior risultato Sonar statico ma richiede molto più
  scaffolding;
- T_RND presenta coverage e mutation adequacy inferiori e il backlog Sonar più
  elevato.

Il risultato globale di PCEnhancer non deve quindi essere interpretato come una
serie di delta numerici mancanti, ma come un esito distinto rispetto a
`ListIteratorWrapper`: nel primo target il refactoring rompe la compilabilità
delle varianti, mentre nel secondo target è possibile completare la matrice
C0–C4.

---

## Evidenze

Confronto C0:

```text
isw2/results/testing/automatic-suite-quality/pcenhancer/c0/
└── automatic-suite-comparison.md
```

Analisi delle varianti e gate di compilazione:

```text
isw2/docs/m4/pcenhancer-m4-analysis.md
```

Confronto globale:

```text
isw2/results/testing/automatic-suite-quality/pcenhancer/
└── automatic-suite-comparison-c0-c4.md
```

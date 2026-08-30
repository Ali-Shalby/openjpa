# ListIteratorWrapper — Confronto globale C0–C4 delle suite automatiche

## Scopo

Questo documento raccoglie e confronta i risultati finali delle suite automatiche generate per
`org.apache.openjpa.lib.util.collections.ListIteratorWrapper` sulle cinque configurazioni:

- **C0** — baseline;
- **C1** — variante refactored generata senza test forniti;
- **C2** — variante refactored con T_BB fornita;
- **C3** — variante refactored con T_BB + T_CF;
- **C4** — variante refactored con T_BB + T_CF + T_MT.

Per ogni variante sono state generate e congelate indipendentemente tre suite automatiche, tutte con **N = 12**:

- **T_RND** — Randoop;
- **T_ES** — EvoSuite;
- **T_LLM** — Microsoft Copilot Web.

Il confronto finale considera i delta richiesti in termini di:

- coverage;
- mutation score e test strength;
- chiarezza;
- manutenibilità;
- code smell;
- categorie/regole Sonar osservate.

Le metriche di adequacy sono state misurate solo dopo il freeze delle suite. I valori assoluti di mutanti non vengono usati per confrontare direttamente varianti diverse, perché la popolazione PIT cambia con il codice production: **52 (C0), 51 (C1), 55 (C2), 47 (C3), 53 (C4)**.

---

## Contesto delle varianti production

| Variante | LOC production | Smell production |
|---|---:|---:|
| C0 | 134 | 0 |
| C1 | 134 | 1 |
| C2 | 134 | 0 |
| C3 | 132 | 1 |
| C4 | 139 | 0 |

Le differenze tra le popolazioni PIT confermano che il refactoring modifica la struttura mutabile del codice; per questo i confronti C0–C4 usano principalmente **percentuali**, non conteggi assoluti di mutanti uccisi.

---

# 1. T_RND — Randoop

| Variante | Line | Branch | Method | Mutation Score | Test Strength | No Coverage | Total LOC | Sonar Smell | Descrittivi |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| C0 | 58.33% | 47.50% | 100.00% | 11.54% | 24.00% | 27 | 397 | 65 | 0% |
| C1 | 58.82% | 40.48% | 91.67% | 17.65% | 32.14% | 23 | 695 | 154 | 0% |
| C2 | 58.21% | 52.27% | 100.00% | 20.00% | 37.93% | 26 | 394 | 69 | 0% |
| C3 | 57.97% | 52.38% | 100.00% | 19.15% | 34.62% | 21 | 394 | 69 | 0% |
| C4 | 61.84% | 50.00% | 100.00% | 13.21% | 26.92% | 27 | 394 | 69 | 0% |

## Delta rispetto a C0

| Variante | Δ Line | Δ Branch | Δ Method | Δ Mutation Score | Δ Test Strength | Δ Total LOC | Δ Sonar Smell |
|---|---:|---:|---:|---:|---:|---:|---:|
| C1 vs C0 | +0.49 pp | -7.02 pp | -8.33 pp | +6.11 pp | +8.14 pp | +298 | +89 |
| C2 vs C0 | -0.12 pp | +4.77 pp | +0.00 pp | +8.46 pp | +13.93 pp | -3 | +4 |
| C3 vs C0 | -0.36 pp | +4.88 pp | +0.00 pp | +7.61 pp | +10.62 pp | -3 | +4 |
| C4 vs C0 | +3.51 pp | +2.50 pp | +0.00 pp | +1.67 pp | +2.92 pp | -3 | +4 |

### Lettura dei risultati

Randoop mostra un profilo relativamente stabile tra C0, C2, C3 e C4: line coverage resta intorno al 58–62%, method coverage è completa, mentre branch coverage rimane circa tra 47% e 52%.

C1 è l'eccezione principale: la suite cresce fino a **695 LOC**, presenta **154 smell** e perde method coverage, pur ottenendo un mutation score superiore alla baseline.

Il mutation score migliora rispetto a C0 su tutte le varianti refactored, ma in modo non monotono:

- C1: 17.65%;
- C2: 20.00%;
- C3: 19.15%;
- C4: 13.21%.

C2 è quindi la variante più favorevole a Randoop in mutation score, mentre C4 torna vicino alla baseline.

### Chiarezza e manutenibilità

Il naming resta sempre completamente generato/opaco:

- 12/12 nomi opachi;
- 0% nomi descrittivi in tutte le varianti.

Per C0, C2, C3 e C4 la struttura è quasi invariata: circa 394–397 LOC e 92 assertion-like statements. C1 è molto più verbosa: 695 LOC e 231 assertion-like statements.

### Sonar

- C0: `S1481` 5, `S1854` 5, `S3415` 6, `S5785` 49.
- C1: `S1220` 2, `S1481` 13, `S1854` 13, `S2133` 1, `S3415` 18, `S3577` 2, `S5785` 105.
- C2/C3/C4: `S1220` 2, `S1481` 5, `S1854` 5, `S3415` 6, `S3577` 2, `S5785` 49.

Le categorie Clean Code osservate lungo C0–C4 includono:

- **INTENTIONAL**;
- **CONSISTENT**;
- **ADAPTABLE**.

Gli attributi coinvolti includono `CLEAR`, `LOGICAL`, `IDENTIFIABLE` e `MODULAR`.

**Sintesi T_RND:** le varianti refactored non producono un miglioramento progressivo e monotono. L'adeguatezza rimane limitata e il naming resta opaco; C1 peggiora nettamente la manutenibilità statica della suite.

---

# 2. T_ES — EvoSuite

| Variante | Line | Branch | Method | Mutation Score | Test Strength | No Coverage | Total LOC | Sonar Smell | Descrittivi |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| C0 | 83.33% | 82.50% | 81.82% | 55.77% | 65.91% | 8 | 286 | 1 | 0% |
| C1 | 88.24% | 88.10% | 91.67% | 64.71% | 68.75% | 3 | 292 | 0 | 0% |
| C2 | 92.54% | 93.18% | 100.00% | 67.27% | 67.27% | 0 | 300 | 7 | 0% |
| C3 | 82.61% | 80.95% | 83.33% | 55.32% | 63.41% | 6 | 286 | 8 | 0% |
| C4 | 94.74% | 97.62% | 100.00% | 66.04% | 66.04% | 0 | 304 | 10 | 0% |

## Delta rispetto a C0

| Variante | Δ Line | Δ Branch | Δ Method | Δ Mutation Score | Δ Test Strength | Δ Total LOC | Δ Sonar Smell |
|---|---:|---:|---:|---:|---:|---:|---:|
| C1 vs C0 | +4.91 pp | +5.60 pp | +9.85 pp | +8.94 pp | +2.84 pp | +6 | -1 |
| C2 vs C0 | +9.21 pp | +10.68 pp | +18.18 pp | +11.50 pp | +1.36 pp | +14 | +6 |
| C3 vs C0 | -0.72 pp | -1.55 pp | +1.51 pp | -0.45 pp | -2.50 pp | +0 | +7 |
| C4 vs C0 | +11.41 pp | +15.12 pp | +18.18 pp | +10.27 pp | +0.13 pp | +18 | +9 |

### Lettura dei risultati

EvoSuite è più sensibile alla variante production rispetto a Randoop.

Le varianti migliori sono:

- **C2**: 92.54% line, 93.18% branch, 100% method, mutation score 67.27%;
- **C4**: 94.74% line, 97.62% branch, 100% method, mutation score 66.04%.

C1 migliora rispetto alla baseline su tutte le principali metriche di adequacy. C3, invece, torna sostanzialmente ai livelli di C0 e presenta method coverage dell'83.33%.

Anche qui l'effetto non è monotono rispetto alla quantità di test fornita durante il refactoring: C4 ottiene la coverage più alta, ma C2 mantiene il mutation score leggermente migliore.

### Chiarezza e manutenibilità

EvoSuite mantiene in tutte le varianti:

- 12/12 nomi opachi;
- 0% nomi descrittivi;
- circa 1.17–1.33 assertion-like statements per test;
- circa 100–101 LOC di scaffolding.

La dimensione complessiva rimane contenuta e stabile: 286–304 LOC.

### Sonar

- C0: `S108` 1.
- C1: nessun code smell.
- C2: `S108` 1, `S1128` 2, `S1598` 2, `S5738` 2.
- C3: `S108` 1, `S1128` 2, `S1598` 2, `S5738` 3.
- C4: `S108` 1, `S1128` 2, `S1481` 1, `S1598` 2, `S1854` 1, `S5738` 3.

Le categorie Clean Code osservate includono:

- **INTENTIONAL**;
- **CONSISTENT**.

Gli attributi coinvolti includono `CLEAR`, `CONVENTIONAL`, `COMPLETE` e, in C4, `LOGICAL`.

C1 è l'unica variante con **0 smell**, mentre il backlog aumenta progressivamente da C2 a C4.

**Sintesi T_ES:** il refactoring può migliorare molto la testabilità per EvoSuite, soprattutto in C2 e C4, ma non esiste una crescita monotona. La qualità statica resta generalmente buona, pur con scaffolding e naming generato.

---

# 3. T_LLM — Copilot Web

| Variante | Line | Branch | Method | Mutation Score | Test Strength | No Coverage | Total LOC | Sonar Smell | Descrittivi |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| C0 | 100.00% | 92.50% | 100.00% | 90.38% | 90.38% | 0 | 344 | 0 | 100% |
| C1 | 100.00% | 97.62% | 100.00% | 96.08% | 96.08% | 0 | 289 | 1 | 100% |
| C2 | 100.00% | 95.45% | 100.00% | 89.09% | 89.09% | 0 | 298 | 1 | 100% |
| C3 | 100.00% | 97.62% | 100.00% | 87.23% | 87.23% | 0 | 324 | 1 | 100% |
| C4 | 100.00% | 95.24% | 100.00% | 86.79% | 86.79% | 0 | 321 | 0 | 100% |

## Delta rispetto a C0

| Variante | Δ Line | Δ Branch | Δ Method | Δ Mutation Score | Δ Test Strength | Δ Total LOC | Δ Sonar Smell |
|---|---:|---:|---:|---:|---:|---:|---:|
| C1 vs C0 | +0.00 pp | +5.12 pp | +0.00 pp | +5.70 pp | +5.70 pp | -55 | +1 |
| C2 vs C0 | +0.00 pp | +2.95 pp | +0.00 pp | -1.29 pp | -1.29 pp | -46 | +1 |
| C3 vs C0 | +0.00 pp | +5.12 pp | +0.00 pp | -3.15 pp | -3.15 pp | -20 | +1 |
| C4 vs C0 | +0.00 pp | +2.74 pp | +0.00 pp | -3.59 pp | -3.59 pp | -23 | +0 |

### Stabilità e repair

| Variante | Stabilità | Repair |
|---|---:|---|
| C0 | 5/5 | non rilevante per il confronto |
| C1 | 5/5 | R1 |
| C2 | 5/5 | NONE |
| C3 | 5/5 | NONE |
| C4 | 5/5 | R1 |

C1 e C4 hanno richiesto un repair tecnico pre-freeze per garantire che i test destinati al ramo “plain Iterator” usassero a runtime un oggetto che non implementasse `ListIterator`. Il repair non ha usato feedback di coverage o mutation testing.

### Lettura dei risultati

T_LLM raggiunge **100% line coverage e 100% method coverage in tutte le varianti**.

La branch coverage è sempre molto alta:

- C0: 92.50%;
- C1: 97.62%;
- C2: 95.45%;
- C3: 97.62%;
- C4: 95.24%.

Il mutation score rimane anch'esso elevato:

- C0: 90.38%;
- C1: **96.08%**;
- C2: 89.09%;
- C3: 87.23%;
- C4: 86.79%.

C1 ottiene quindi il miglior risultato LLM in mutation adequacy. C2–C4 mantengono comunque valori molto elevati, ma non mostrano un miglioramento progressivo rispetto alla baseline.

### Chiarezza e manutenibilità

T_LLM mantiene in tutte le configurazioni:

- 0 nomi opachi;
- 100% nomi descrittivi;
- nessuno scaffolding separato.

La dimensione varia tra 289 e 344 LOC, mentre la densità di assertion rimane circa tra 6.08 e 6.67 per test.

### Sonar

- C0: nessun code smell.
- C1: `S1598` 1.
- C2: `S1598` 1.
- C3: `S1598` 1.
- C4: nessun code smell.

La sola regola osservata in C1–C3 è `java:S1598`, categoria **CONSISTENT**, attributo **CONVENTIONAL**. C0 e C4 non presentano smell.

**Sintesi T_LLM:** la qualità rimane elevata e stabile lungo tutte le varianti, con piena line/method coverage, mutation score sempre superiore all'86%, naming descrittivo e backlog Sonar nullo o quasi nullo. L'aumento dell'informazione di test fornita durante il refactoring non produce però un miglioramento monotono dell'adequacy della suite LLM successivamente generata.

---

# 4. Confronto trasversale tra tecniche

## Adequacy

Considerando tutte le varianti:

- **T_LLM** è sempre la tecnica con il mutation score più alto;
- **T_ES** occupa stabilmente la posizione intermedia;
- **T_RND** presenta i mutation score più bassi.

Per branch coverage, T_ES supera T_LLM solo in C4:

- C4 T_ES: 97.62%;
- C4 T_LLM: 95.24%.

Questo non si traduce però in maggiore mutation adequacy: su C4 T_LLM raggiunge 86.79%, contro 66.04% di T_ES.

Questo conferma che una coverage strutturale più elevata non implica automaticamente oracle più forti.

## Chiarezza

Il pattern è stabile in tutte le varianti:

- T_RND: 0% naming descrittivo;
- T_ES: 0% naming descrittivo;
- T_LLM: 100% naming descrittivo.

T_LLM è quindi la tecnica più favorevole alla comprensione immediata dell'intento dei test.

## Manutenibilità

- T_RND presenta il backlog Sonar più elevato e, in C1, una forte crescita della dimensione.
- T_ES mantiene dimensione contenuta ma introduce scaffolding dedicato.
- T_LLM non richiede scaffolding e presenta tra 0 e 1 smell per variante.

Non emerge quindi un trade-off unico basato soltanto sulle LOC: EvoSuite può essere più compatta, ma T_LLM combina naming descrittivo, assenza di scaffolding e backlog Sonar minimo.

---

# 5. Effetto della configurazione C1–C4

Il confronto complessivo **non supporta una relazione monotona** del tipo:

`più test forniti al refactoring → migliore suite automatica successiva`.

I risultati dipendono dalla tecnica:

- **Randoop** migliora maggiormente su C2, ma C4 perde mutation score;
- **EvoSuite** ottiene i risultati migliori soprattutto su C2 e C4;
- **LLM** raggiunge il massimo mutation score su C1, mentre C2–C4 restano molto forti ma leggermente sotto C0/C1.

Questo suggerisce che le informazioni di test fornite durante il refactoring influenzano la struttura della variante production, ma l'effetto sulla testabilità successiva non è lineare e dipende dal generatore automatico considerato.

---

# 6. Analisi dei delta richiesti

## Coverage

- T_RND: variazioni contenute e non monotone.
- T_ES: miglioramenti marcati in C1, C2 e C4; C3 torna vicino a C0.
- T_LLM: line e method coverage restano sempre al 100%; branch coverage migliora rispetto a C0 in tutte le varianti.

## Mutation Score

- T_RND: tutte le Cx superano C0, ma C4 solo marginalmente.
- T_ES: C1, C2 e C4 migliorano C0; C3 è sostanzialmente equivalente.
- T_LLM: C1 migliora nettamente C0; C2–C4 risultano leggermente inferiori alla baseline, pur mantenendo valori molto elevati.

## Chiarezza

Il refactoring C1–C4 non cambia il pattern di naming delle tecniche:

- RND/ES rimangono opachi;
- LLM rimane completamente descrittivo.

## Manutenibilità

- RND: stabile salvo il forte peggioramento C1.
- ES: dimensione stabile, ma smell variabili da 0 a 10.
- LLM: dimensione moderata, nessuno scaffolding e smell tra 0 e 1.

## Smell e categorie Sonar

Le categorie osservate nelle suite automatiche sono:

- **ADAPTABLE**;
- **INTENTIONAL**;
- **CONSISTENT**.

T_RND concentra soprattutto smell INTENTIONAL e CONSISTENT, con ADAPTABLE presente da C1 in poi. T_ES mostra INTENTIONAL e CONSISTENT. T_LLM presenta al massimo una singola issue CONSISTENT nelle varianti C1–C3.

---

# 7. Conclusione finale

Nel caso di `ListIteratorWrapper`, il confronto C0–C4 mostra che la qualità delle suite automatiche dipende molto più dalla **tecnica di generazione** che da una progressione monotona delle configurazioni di refactoring.

**T_LLM** presenta il profilo multidimensionale più forte:

- 100% line coverage in tutte le varianti;
- 100% method coverage in tutte le varianti;
- branch coverage sempre superiore al 92%;
- mutation score tra 86.79% e 96.08%;
- 100% naming descrittivo;
- nessuno scaffolding;
- 0–1 smell Sonar.

**T_ES** rappresenta il compromesso intermedio:

- coverage spesso molto alta;
- mutation score tra 55.32% e 67.27%;
- dimensione contenuta;
- scaffolding costante;
- naming opaco;
- backlog Sonar generalmente contenuto.

**T_RND** mostra il profilo meno forte:

- line/branch coverage più basse;
- mutation score tra 11.54% e 20.00%;
- naming completamente opaco;
- backlog Sonar elevato;
- forte anomalia di dimensione e smell su C1.

La principale conclusione sperimentale è quindi duplice:

1. **non emerge un effetto monotono C1→C4** sulla qualità delle suite automatiche;
2. **la scelta della tecnica automatica domina il risultato**, con T_LLM sistematicamente più forte su adequacy, chiarezza e qualità statica, T_ES intermedio e T_RND più debole.

Questa interpretazione è limitata al target `ListIteratorWrapper` e alle configurazioni sperimentali qui misurate; non va generalizzata oltre l'esperimento senza ulteriori evidenze.

---

## Evidenze principali

```text
isw2/results/testing/automatic-suite-quality/list-iterator-wrapper/
├── c0/
│   └── listiteratorwrapper-c0-automatic-suite-comparison.md
├── c1/
│   └── listiteratorwrapper-c1-automatic-suite-comparison.md
├── c2/
│   └── listiteratorwrapper-c2-automatic-suite-comparison.md
├── c3/
│   └── listiteratorwrapper-c3-automatic-suite-comparison.md
├── c4/
│   └── listiteratorwrapper-c4-automatic-suite-comparison.md
└── automatic-suite-comparison-c0-c4.md
```

Le misurazioni di coverage e mutation testing sono conservate sotto:

```text
isw2/results/testing/list-iterator-wrapper/
├── automatic/
└── refactored/
    ├── c1/
    ├── c2/
    ├── c3/
    └── c4/
```

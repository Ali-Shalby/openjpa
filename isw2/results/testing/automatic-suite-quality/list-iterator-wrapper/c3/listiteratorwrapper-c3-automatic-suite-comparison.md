# ListIteratorWrapper C3 — Confronto delle suite automatiche

## Scopo

Questa sezione confronta le tre suite automatiche generate sulla variante **C3** di
`org.apache.openjpa.lib.util.collections.ListIteratorWrapper`:

- **T_RND** — Randoop
- **T_ES** — EvoSuite
- **T_LLM** — LLM

Il confronto è svolto a parità di cardinalità, con **N = 12 test per suite**, e considera:

- coverage;
- mutation score e test strength;
- dimensione della suite;
- codice di supporto/scaffolding;
- naming;
- densità di assertion;
- code smell Sonar;
- chiarezza e manutenibilità.

Il confronto tra le diverse varianti **C0–C4** è rinviato all'analisi finale, dopo il completamento delle misurazioni su tutte le varianti.

---

## Risultati C3

| Metrica | T_RND | T_ES | T_LLM |
|---|---:|---:|---:|
| Test | 12 | 12 | 12 |
| Line coverage | 57.97% | 82.61% | **100.00%** |
| Branch coverage | 52.38% | 80.95% | **97.62%** |
| Method coverage | **100.00%** | 83.33% | **100.00%** |
| Mutanti totali | 47 | 47 | 47 |
| Mutanti uccisi | 9 | 26 | **41** |
| Mutanti sopravvissuti | 17 | 15 | **6** |
| Mutanti non coperti | 21 | 6 | **0** |
| Mutation score | 19.15% | 55.32% | **87.23%** |
| Test strength | 34.62% | 63.41% | **87.23%** |
| Logic LOC | 394 | 186 | 324 |
| Support LOC | **0** | 100 | **0** |
| Total LOC | 394 | **286** | 324 |
| Assertion-like statements | 92 | 14 | 77 |
| Assertion/test | 7.67 | 1.17 | 6.42 |
| Nomi opachi | 12 | 12 | **0** |
| Nomi descrittivi | 0% | 0% | **100%** |
| Sonar code smell | 69 | 8 | **1** |
| Smell nella logica | 69 | 5 | **1** |
| Smell nel supporto | 0 | 3 | **0** |
| Regole Sonar distinte | 6 | 4 | **1** |

Per T_LLM è stata inoltre verificata la stabilità tramite **5 esecuzioni consecutive**, tutte concluse con esito positivo. La suite è stata congelata senza necessità di repair.

---

## Analisi T_RND

Randoop raggiunge:

- line coverage: 57.97%;
- branch coverage: 52.38%;
- method coverage: 100.00%;
- mutation score: 19.15%;
- test strength: 34.62%.

Su 47 mutanti:

- 9 sono uccisi;
- 17 sopravvivono;
- 21 risultano non coperti.

La suite contiene:

- 394 LOC di logica;
- 0 LOC di supporto;
- 394 LOC complessive;
- 92 assertion-like statements;
- 7.67 assertion per test;
- 12/12 nomi opachi.

SonarQube Cloud rileva **69 code smell**, tutti nella logica della suite, distribuiti su 6 regole.

### Regole Sonar — T_RND

| Regola | Issue | Ruolo | Maintainability severity | Clean Code attribute | Categoria |
|---|---:|---|---|---|---|
| `java:S1220` | 2 | LOGIC | LOW | MODULAR | ADAPTABLE |
| `java:S1481` | 5 | LOGIC | LOW | CLEAR | INTENTIONAL |
| `java:S1854` | 5 | LOGIC | MEDIUM | LOGICAL | INTENTIONAL |
| `java:S3415` | 6 | LOGIC | MEDIUM | LOGICAL | INTENTIONAL |
| `java:S3577` | 2 | LOGIC | LOW | IDENTIFIABLE | CONSISTENT |
| `java:S5785` | 49 | LOGIC | MEDIUM | CLEAR | INTENTIONAL |

Randoop raggiunge la copertura completa dei metodi, ma presenta line e branch coverage sensibilmente inferiori alle altre tecniche e lascia una parte rilevante dei mutanti non coperta. È inoltre la suite con il maggior numero di smell statici su C3.

---

## Analisi T_ES

EvoSuite raggiunge:

- line coverage: 82.61%;
- branch coverage: 80.95%;
- method coverage: 83.33%;
- mutation score: 55.32%;
- test strength: 63.41%.

Su 47 mutanti:

- 26 sono uccisi;
- 15 sopravvivono;
- 6 risultano non coperti.

La suite contiene:

- 186 LOC di logica;
- 100 LOC di scaffolding;
- 286 LOC complessive;
- 14 assertion-like statements;
- 1.17 assertion per test;
- 12/12 nomi opachi.

SonarQube Cloud rileva **8 code smell**:

- 5 nella logica;
- 3 nel supporto/scaffolding;
- 4 regole distinte.

### Regole Sonar — T_ES

| Regola | Issue | Ruolo | Maintainability severity | Clean Code attribute | Categoria |
|---|---:|---|---|---|---|
| `java:S108` | 1 | SUPPORT | MEDIUM | CLEAR | INTENTIONAL |
| `java:S1128` | 2 | LOGIC;SUPPORT | LOW | CLEAR | INTENTIONAL |
| `java:S1598` | 2 | LOGIC;SUPPORT | HIGH | CONVENTIONAL | CONSISTENT |
| `java:S5738` | 3 | LOGIC | MEDIUM | COMPLETE | INTENTIONAL |

EvoSuite migliora nettamente rispetto a Randoop in coverage e mutation adequacy e riduce fortemente il backlog Sonar. Rimangono però 6 mutanti non coperti, una method coverage non completa e 100 LOC di scaffolding.

---

## Analisi T_LLM

T_LLM raggiunge i valori più elevati di adequacy sulla variante C3:

- line coverage: 100.00%;
- branch coverage: 97.62%;
- method coverage: 100.00%;
- mutation score: 87.23%;
- test strength: 87.23%.

Su 47 mutanti:

- 41 sono uccisi;
- 6 sopravvivono;
- 0 risultano non coperti.

La suite contiene:

- 324 LOC di logica;
- 0 LOC di supporto;
- 324 LOC complessive;
- 77 assertion-like statements;
- 6.42 assertion per test;
- 0/12 nomi opachi;
- 100% nomi descrittivi.

La generazione è stata validata con:

- 12/12 test PASS;
- stabilità 5/5 PASS;
- nessun repair richiesto;
- freeze precedente alle misurazioni di adequacy.

SonarQube Cloud rileva **1 solo code smell**, nella logica:

| Regola | Issue | Ruolo | Maintainability severity | Clean Code attribute | Categoria |
|---|---:|---|---|---|---|
| `java:S1598` | 1 | LOGIC | HIGH | CONVENTIONAL | CONSISTENT |

T_LLM combina quindi la coverage più alta, il mutation score più alto, naming completamente descrittivo, assenza di scaffolding e un solo smell Sonar.

---

## Confronto di chiarezza

### Naming

- T_RND: 0% nomi descrittivi;
- T_ES: 0% nomi descrittivi;
- T_LLM: 100% nomi descrittivi.

T_LLM rende immediatamente riconoscibile l'intento di ciascun test tramite nomi descrittivi e tracciabilità esplicita degli scenari.

### Assertion density

- T_RND: 7.67 assertion/test;
- T_ES: 1.17 assertion/test;
- T_LLM: 6.42 assertion/test.

Randoop presenta la densità di assertion più alta, ma questo non si traduce in mutation adequacy equivalente. T_LLM mantiene una densità elevata accompagnata dai migliori risultati di mutation testing.

---

## Confronto di manutenibilità

### Dimensione

- T_RND: 394 LOC;
- T_ES: 286 LOC, di cui 100 di supporto;
- T_LLM: 324 LOC, senza supporto dedicato.

EvoSuite è l'artefatto complessivamente più compatto, ma circa un terzo della sua dimensione deriva dallo scaffolding. T_LLM è leggermente più esteso in termini di LOC complessive, ma non richiede codice di supporto separato. Randoop rimane la suite più estesa.

### SonarQube Cloud

Per code smell:

1. T_LLM: **1**
2. T_ES: **8**
3. T_RND: **69**

T_LLM presenta quindi il backlog Sonar più contenuto su C3. EvoSuite mantiene un numero ridotto di issue, distribuite tra logica e scaffolding, mentre Randoop presenta un numero molto maggiore di smell.

### Categorie Clean Code osservate

Le categorie Sonar osservate nelle tre suite C3 sono:

- **ADAPTABLE** — T_RND;
- **INTENTIONAL** — T_RND e T_ES;
- **CONSISTENT** — T_RND, T_ES e T_LLM.

Gli attributi Clean Code coinvolti sono:

- MODULAR;
- CLEAR;
- LOGICAL;
- IDENTIFIABLE;
- CONVENTIONAL;
- COMPLETE.

---

## Conclusione C3

Per `ListIteratorWrapper` C3:

- per **adequacy**, l'ordine osservato è `T_LLM > T_ES > T_RND`;
- per **chiarezza del naming**, T_LLM è nettamente favorito;
- per **Sonar smell**, T_LLM ottiene il risultato migliore con una sola issue;
- per **dimensione complessiva**, EvoSuite è la suite più compatta, ma include 100 LOC di scaffolding;
- per **assenza di scaffolding**, T_RND e T_LLM non richiedono supporto dedicato;
- EvoSuite rappresenta una soluzione intermedia per coverage, mutation adequacy e qualità statica;
- Randoop raggiunge method coverage completa, ma mostra il profilo più debole per line/branch coverage, mutation adequacy e smell;
- T_LLM presenta il profilo complessivamente più forte sulla variante C3, combinando elevata adequacy, naming descrittivo e un backlog Sonar minimo.

Questa conclusione riguarda esclusivamente il confronto **interno alla variante C3**. Il confronto tra **C0, C1, C2, C3 e C4** viene effettuato separatamente al termine della raccolta di tutte le misurazioni.

---

## Evidenze

```text
isw2/results/testing/automatic-suite-quality/list-iterator-wrapper/c3/
├── structural-quality/
│   └── file-manifest.csv
├── rnd/
│   └── sonar-ci/
├── es/
│   └── sonar-ci/
├── llm/
│   └── sonar-ci/
└── automatic-suite-comparison.md
```

I dati strutturali C3 sono inoltre conservati in:

```text
isw2/results/testing/automatic-suite-quality/
├── structural-quality-c3.csv
└── structural-quality-c3.txt
```

Le evidenze di coverage e mutation testing delle suite automatiche C3 sono conservate sotto:

```text
isw2/results/testing/list-iterator-wrapper/refactored/c3/
├── rnd/
├── es/
└── llm/
```

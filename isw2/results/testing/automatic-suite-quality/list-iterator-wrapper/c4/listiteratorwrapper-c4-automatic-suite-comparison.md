# ListIteratorWrapper C4 — Confronto delle suite automatiche

## Scopo

Questa sezione confronta le tre suite automatiche generate sulla variante **C4** di
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

## Risultati C4

| Metrica | T_RND | T_ES | T_LLM |
|---|---:|---:|---:|
| Test | 12 | 12 | 12 |
| Line coverage | 61.84% | 94.74% | **100.00%** |
| Branch coverage | 50.00% | **97.62%** | 95.24% |
| Method coverage | **100.00%** | **100.00%** | **100.00%** |
| Mutanti totali | 53 | 53 | 53 |
| Mutanti uccisi | 7 | 35 | **46** |
| Mutanti sopravvissuti | 19 | 18 | **7** |
| Mutanti non coperti | 27 | **0** | **0** |
| Mutation score | 13.21% | 66.04% | **86.79%** |
| Test strength | 26.92% | 66.04% | **86.79%** |
| Logic LOC | 394 | 203 | 321 |
| Support LOC | **0** | 101 | **0** |
| Total LOC | 394 | **304** | 321 |
| Assertion-like statements | 92 | 14 | 80 |
| Assertion/test | 7.67 | 1.17 | 6.67 |
| Nomi opachi | 12 | 12 | **0** |
| Nomi descrittivi | 0% | 0% | **100%** |
| Sonar code smell | 69 | 10 | **0** |
| Smell nella logica | 69 | 7 | **0** |
| Smell nel supporto | 0 | 3 | **0** |
| Regole Sonar distinte | 6 | 6 | **0** |

Per T_LLM è stata inoltre verificata la stabilità tramite **5 esecuzioni consecutive**, tutte concluse con esito positivo. La suite è stata congelata dopo **un repair tecnico R1**, eseguito prima delle misurazioni di adequacy.

---

## Analisi T_RND

Randoop raggiunge:

- line coverage: 61.84%;
- branch coverage: 50.00%;
- method coverage: 100.00%;
- mutation score: 13.21%;
- test strength: 26.92%.

Su 53 mutanti:

- 7 sono uccisi;
- 19 sopravvivono;
- 27 risultano non coperti.

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

Randoop raggiunge la copertura completa dei metodi, ma lascia una quota significativa della popolazione mutante non coperta e mostra il backlog statico più elevato tra le tre tecniche.

---

## Analisi T_ES

EvoSuite raggiunge:

- line coverage: 94.74%;
- branch coverage: 97.62%;
- method coverage: 100.00%;
- mutation score: 66.04%;
- test strength: 66.04%.

Su 53 mutanti:

- 35 sono uccisi;
- 18 sopravvivono;
- 0 risultano non coperti.

La suite contiene:

- 203 LOC di logica;
- 101 LOC di scaffolding;
- 304 LOC complessive;
- 14 assertion-like statements;
- 1.17 assertion per test;
- 12/12 nomi opachi.

SonarQube Cloud rileva **10 code smell**:

- 7 nella logica;
- 3 nel supporto/scaffolding;
- 6 regole distinte.

### Regole Sonar — T_ES

| Regola | Issue | Ruolo | Maintainability severity | Clean Code attribute | Categoria |
|---|---:|---|---|---|---|
| `java:S108` | 1 | SUPPORT | MEDIUM | CLEAR | INTENTIONAL |
| `java:S1128` | 2 | LOGIC;SUPPORT | LOW | CLEAR | INTENTIONAL |
| `java:S1481` | 1 | LOGIC | LOW | CLEAR | INTENTIONAL |
| `java:S1598` | 2 | LOGIC;SUPPORT | HIGH | CONVENTIONAL | CONSISTENT |
| `java:S1854` | 1 | LOGIC | MEDIUM | LOGICAL | INTENTIONAL |
| `java:S5738` | 3 | LOGIC | MEDIUM | COMPLETE | INTENTIONAL |

EvoSuite elimina completamente i mutanti non coperti e raggiunge la branch coverage più elevata tra le tre suite C4. Rimangono però naming generato, 101 LOC di scaffolding e 10 smell Sonar.

---

## Analisi T_LLM

T_LLM raggiunge:

- line coverage: 100.00%;
- branch coverage: 95.24%;
- method coverage: 100.00%;
- mutation score: 86.79%;
- test strength: 86.79%.

Su 53 mutanti:

- 46 sono uccisi;
- 7 sopravvivono;
- 0 risultano non coperti.

La suite contiene:

- 321 LOC di logica;
- 0 LOC di supporto;
- 321 LOC complessive;
- 80 assertion-like statements;
- 6.67 assertion per test;
- 0/12 nomi opachi;
- 100% nomi descrittivi.

La generazione è stata validata con:

- 12/12 test PASS;
- stabilità 5/5 PASS;
- un repair tecnico R1;
- freeze precedente alle misurazioni di adequacy.

Il repair R1 ha corretto esclusivamente il setup dei casi che dovevano esercitare il ramo relativo a un semplice `Iterator`, senza modificare cardinalità, scenari o oracle.

SonarQube Cloud non rileva alcun code smell:

- 0 smell nella logica;
- 0 smell nel supporto;
- 0 regole distinte.

T_LLM combina quindi line e method coverage complete, il mutation score più elevato, naming completamente descrittivo, assenza di scaffolding e assenza di code smell Sonar.

---

## Confronto di chiarezza

### Naming

- T_RND: 0% nomi descrittivi;
- T_ES: 0% nomi descrittivi;
- T_LLM: 100% nomi descrittivi.

T_LLM rende immediatamente riconoscibile l'intento dei test tramite naming descrittivo e tracciabilità TLLM.

### Assertion density

- T_RND: 7.67 assertion/test;
- T_ES: 1.17 assertion/test;
- T_LLM: 6.67 assertion/test.

Randoop presenta la densità di assertion più alta, ma la sua mutation adequacy rimane molto più bassa. T_LLM mantiene una densità elevata accompagnata dal mutation score migliore.

---

## Confronto di manutenibilità

### Dimensione

- T_RND: 394 LOC;
- T_ES: 304 LOC, di cui 101 di supporto;
- T_LLM: 321 LOC, senza supporto dedicato.

EvoSuite è l'artefatto più compatto in termini di LOC complessive, ma circa un terzo della sua dimensione è costituito da scaffolding. T_LLM è leggermente più esteso ma non richiede supporto dedicato. Randoop rimane la suite più estesa.

### SonarQube Cloud

Per code smell:

1. T_LLM: **0**
2. T_ES: **10**
3. T_RND: **69**

T_LLM presenta quindi il miglior risultato statico su C4, senza issue di maintainability aperte. EvoSuite mantiene un backlog contenuto ma distribuito tra logica e scaffolding; Randoop presenta il numero di smell nettamente maggiore.

### Categorie Clean Code osservate

Le categorie Sonar osservate sulle suite C4 sono:

- **ADAPTABLE** — T_RND;
- **INTENTIONAL** — T_RND e T_ES;
- **CONSISTENT** — T_RND e T_ES.

Gli attributi Clean Code coinvolti sono:

- MODULAR;
- CLEAR;
- LOGICAL;
- IDENTIFIABLE;
- CONVENTIONAL;
- COMPLETE.

Per T_LLM non risultano categorie associate a code smell aperti, poiché Sonar non rileva issue sulla suite.

---

## Conclusione C4

Per `ListIteratorWrapper` C4:

- per **mutation adequacy**, l'ordine osservato è `T_LLM > T_ES > T_RND`;
- T_ES ottiene la **branch coverage più elevata** (97.62%), mentre T_LLM raggiunge line e method coverage complete;
- per **chiarezza del naming**, T_LLM è nettamente favorito;
- per **Sonar smell**, T_LLM ottiene il miglior risultato possibile con 0 issue;
- per **dimensione complessiva**, EvoSuite è la suite più compatta, ma include 101 LOC di scaffolding;
- per **assenza di scaffolding**, T_RND e T_LLM non richiedono supporto dedicato;
- EvoSuite rappresenta una soluzione intermedia, con coverage molto elevata e nessun mutante non coperto;
- Randoop mostra il profilo più debole in mutation adequacy e qualità statica;
- T_LLM presenta il profilo multidimensionale complessivamente più forte sulla variante C4, pur richiedendo un repair tecnico R1 prima del freeze.

Questa conclusione riguarda esclusivamente il confronto **interno alla variante C4**. Il confronto tra **C0, C1, C2, C3 e C4** viene effettuato separatamente al termine della raccolta di tutte le misurazioni.

---

## Evidenze

```text
isw2/results/testing/automatic-suite-quality/list-iterator-wrapper/c4/
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

I dati strutturali C4 sono inoltre conservati in:

```text
isw2/results/testing/automatic-suite-quality/
├── structural-quality-c4.csv
└── structural-quality-c4.txt
```

Le evidenze di coverage e mutation testing delle suite automatiche C4 sono conservate sotto:

```text
isw2/results/testing/list-iterator-wrapper/refactored/c4/
├── rnd/
├── es/
└── llm/
```

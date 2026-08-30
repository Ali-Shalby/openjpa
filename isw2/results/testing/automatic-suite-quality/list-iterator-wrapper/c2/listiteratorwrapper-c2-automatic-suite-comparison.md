# ListIteratorWrapper C2 — Confronto delle suite automatiche

## Scopo

Questa sezione confronta le tre suite automatiche generate sulla variante **C2** di
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

## Risultati C2

| Metrica | T_RND | T_ES | T_LLM |
|---|---:|---:|---:|
| Test | 12 | 12 | 12 |
| Line coverage | 58.21% | 92.54% | **100.00%** |
| Branch coverage | 52.27% | 93.18% | **95.45%** |
| Method coverage | **100.00%** | **100.00%** | **100.00%** |
| Mutanti totali | 55 | 55 | 55 |
| Mutanti uccisi | 11 | 37 | **49** |
| Mutanti sopravvissuti | 18 | 18 | **6** |
| Mutanti non coperti | 26 | **0** | **0** |
| Mutation score | 20.00% | 67.27% | **89.09%** |
| Test strength | 37.93% | 67.27% | **89.09%** |
| Logic LOC | 394 | 199 | **298** |
| Support LOC | **0** | 101 | **0** |
| Total LOC | 394 | 300 | **298** |
| Assertion-like statements | 92 | 15 | 73 |
| Assertion/test | 7.67 | 1.25 | 6.08 |
| Nomi opachi | 12 | 12 | **0** |
| Nomi descrittivi | 0% | 0% | **100%** |
| Sonar code smell | 69 | 7 | **1** |
| Smell nella logica | 69 | 4 | **1** |
| Smell nel supporto | 0 | 3 | **0** |
| Regole Sonar distinte | 6 | 4 | **1** |

Per T_LLM è stata inoltre verificata la stabilità tramite **5 esecuzioni consecutive**, tutte concluse con esito positivo. La suite è stata congelata senza necessità di repair.

---

## Analisi T_RND

Randoop raggiunge:

- line coverage: 58.21%;
- branch coverage: 52.27%;
- method coverage: 100.00%;
- mutation score: 20.00%;
- test strength: 37.93%.

Su 55 mutanti:

- 11 sono uccisi;
- 18 sopravvivono;
- 26 risultano non coperti.

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

Randoop ottiene quindi una copertura completa dei metodi, ma una parte rilevante dei mutanti resta non coperta e la suite presenta il maggior numero di smell tra le tre tecniche.

---

## Analisi T_ES

EvoSuite raggiunge:

- line coverage: 92.54%;
- branch coverage: 93.18%;
- method coverage: 100.00%;
- mutation score: 67.27%;
- test strength: 67.27%.

Su 55 mutanti:

- 37 sono uccisi;
- 18 sopravvivono;
- 0 risultano non coperti.

La suite contiene:

- 199 LOC di logica;
- 101 LOC di scaffolding;
- 300 LOC complessive;
- 15 assertion-like statements;
- 1.25 assertion per test;
- 12/12 nomi opachi.

SonarQube Cloud rileva **7 code smell**:

- 4 nella logica;
- 3 nel supporto/scaffolding;
- 4 regole distinte.

### Regole Sonar — T_ES

| Regola | Issue | Ruolo | Maintainability severity | Clean Code attribute | Categoria |
|---|---:|---|---|---|---|
| `java:S108` | 1 | SUPPORT | MEDIUM | CLEAR | INTENTIONAL |
| `java:S1128` | 2 | LOGIC;SUPPORT | LOW | CLEAR | INTENTIONAL |
| `java:S1598` | 2 | LOGIC;SUPPORT | HIGH | CONVENTIONAL | CONSISTENT |
| `java:S5738` | 2 | LOGIC | MEDIUM | COMPLETE | INTENTIONAL |

EvoSuite elimina completamente i mutanti non coperti e raggiunge una coverage elevata, mantenendo una dimensione complessiva inferiore a Randoop. Rimane però dipendente da 101 LOC di scaffolding e conserva naming generato/opaco.

---

## Analisi T_LLM

T_LLM raggiunge i valori più elevati di adequacy sulla variante C2:

- line coverage: 100.00%;
- branch coverage: 95.45%;
- method coverage: 100.00%;
- mutation score: 89.09%;
- test strength: 89.09%.

Su 55 mutanti:

- 49 sono uccisi;
- 6 sopravvivono;
- 0 risultano non coperti.

La suite contiene:

- 298 LOC di logica;
- 0 LOC di supporto;
- 298 LOC complessive;
- 73 assertion-like statements;
- 6.08 assertion per test;
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

T_LLM combina quindi i migliori risultati di coverage e mutation adequacy con naming completamente descrittivo, assenza di scaffolding e un solo smell Sonar.

---

## Confronto di chiarezza

### Naming

- T_RND: 0% nomi descrittivi;
- T_ES: 0% nomi descrittivi;
- T_LLM: 100% nomi descrittivi.

T_LLM è nettamente favorito per comprensibilità immediata dell'intento dei test.

### Assertion density

- T_RND: 7.67 assertion/test;
- T_ES: 1.25 assertion/test;
- T_LLM: 6.08 assertion/test.

Randoop presenta la densità più alta, ma questo non si traduce in mutation adequacy comparabile alle altre tecniche. T_LLM mantiene una densità intermedia accompagnata dal mutation score più alto.

---

## Confronto di manutenibilità

### Dimensione

- T_RND: 394 LOC;
- T_ES: 300 LOC, di cui 101 di supporto;
- T_LLM: 298 LOC, senza supporto dedicato.

T_LLM è quindi l'artefatto complessivamente più compatto, anche se la differenza rispetto a EvoSuite è minima. EvoSuite richiede però una quota significativa di scaffolding, mentre Randoop è la suite più estesa.

### SonarQube Cloud

Per code smell:

1. T_LLM: **1**
2. T_ES: **7**
3. T_RND: **69**

T_LLM presenta il risultato Sonar migliore su C2. EvoSuite mantiene un numero contenuto di issue, distribuite tra logica e scaffolding, mentre Randoop presenta un backlog statico sensibilmente maggiore.

### Categorie Clean Code osservate

Le categorie Sonar osservate nelle tre suite C2 sono:

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

## Conclusione C2

Per `ListIteratorWrapper` C2:

- per **adequacy**, l'ordine osservato è `T_LLM > T_ES > T_RND`;
- per **chiarezza del naming**, T_LLM è nettamente favorito;
- per **Sonar smell**, T_LLM ottiene il risultato migliore con una sola issue;
- per **dimensione complessiva**, T_LLM è leggermente più compatto di EvoSuite e più compatto di Randoop;
- per **assenza di scaffolding**, T_RND e T_LLM non richiedono supporto dedicato;
- EvoSuite rappresenta una soluzione intermedia, con coverage elevata e nessun mutante non coperto, ma con scaffolding e naming generato;
- Randoop ottiene method coverage completa, ma presenta il profilo più debole in mutation adequacy e qualità statica.

Nel complesso, **T_LLM presenta il profilo multidimensionale più forte su C2** tra le tre suite osservate.

Questa conclusione riguarda esclusivamente il confronto **interno alla variante C2**. Il confronto tra **C0, C1, C2, C3 e C4** viene effettuato separatamente al termine della raccolta di tutte le misurazioni, evitando conclusioni progressive prima della disponibilità dell'intera matrice sperimentale.

---

## Evidenze

```text
isw2/results/testing/automatic-suite-quality/list-iterator-wrapper/c2/
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

I dati strutturali C2 sono inoltre conservati in:

```text
isw2/results/testing/automatic-suite-quality/
├── structural-quality-c2.csv
└── structural-quality-c2.txt
```

Le evidenze di coverage e mutation testing delle suite automatiche C2 sono conservate sotto:

```text
isw2/results/testing/list-iterator-wrapper/refactored/c2/
├── rnd/
├── es/
└── llm/
```

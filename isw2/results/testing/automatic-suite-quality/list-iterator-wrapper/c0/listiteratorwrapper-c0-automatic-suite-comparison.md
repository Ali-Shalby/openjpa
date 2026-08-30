# ListIteratorWrapper C0 — Confronto delle suite automatiche

## Scopo

Questa sezione confronta le tre suite automatiche generate sulla versione **C0** di
`org.apache.openjpa.lib.util.collections.ListIteratorWrapper`:

- **T_RND** — Randoop
- **T_ES** — EvoSuite
- **T_LLM** — LLM

Il confronto è svolto a parità di cardinalità, con **N = 12 test per suite**, e considera congiuntamente:

- coverage;
- mutation score;
- test strength;
- dimensione della suite;
- codice di supporto/scaffolding;
- leggibilità dei nomi dei test;
- densità di assertion;
- code smell rilevati da SonarQube Cloud;
- regole e categorie Sonar;
- chiarezza e manutenibilità complessive.

L'obiettivo è discutere i trade-off tra efficacia, leggibilità e qualità statica del codice di test senza ridurre il confronto a una sola metrica.

---

## Risultati quantitativi

| Metrica | T_RND | T_ES | T_LLM |
|---|---:|---:|---:|
| Test | 12 | 12 | 12 |
| Line coverage | 58.33% | 83.33% | **100.00%** |
| Branch coverage | 47.50% | 82.50% | **92.50%** |
| Method coverage | **100.00%** | 81.82% | **100.00%** |
| Mutanti uccisi | 6 | 29 | **47** |
| Mutanti sopravvissuti | 19 | 15 | **5** |
| Mutanti non coperti | 27 | 8 | **0** |
| Mutation score | 11.54% | 55.77% | **90.38%** |
| Test strength | 24.00% | 65.91% | **90.38%** |
| Logic LOC | 397 | **186** | 344 |
| Support LOC | **0** | 100 | **0** |
| Total LOC | 397 | **286** | 344 |
| Assertion-like statements | 92 | 14 | 74 |
| Assertion/test | 7.67 | 1.17 | 6.17 |
| Nomi opachi | 12 | 12 | **0** |
| Nomi descrittivi | 0% | 0% | **100%** |
| Sonar code smell | 65 | 1 | **0** |
| Smell nella logica | 65 | **0** | **0** |
| Smell nel supporto | 0 | 1 | **0** |
| Regole Sonar distinte | 4 | 1 | **0** |

Per T_LLM è stata inoltre verificata la stabilità mediante **5 esecuzioni consecutive**, tutte concluse con esito positivo.

---

## Analisi T_RND

La suite Randoop raggiunge una copertura discreta della classe, ma resta nettamente inferiore alle altre due tecniche sulle metriche di adequacy:

- line coverage: 58.33%;
- branch coverage: 47.50%;
- method coverage: 100%;
- mutation score: 11.54%;
- test strength: 24.00%.

Su 52 mutanti complessivi, ne uccide 6, ne lascia sopravvivere 19 e non ne copre 27.

Dal punto di vista strutturale, la suite contiene 397 LOC e non richiede scaffolding esterno. Le 92 assertion-like statements producono una densità media di 7.67 assertion per test.

La densità elevata di assertion non si traduce però in una corrispondente efficacia sul mutation testing: il mutation score resta il più basso delle tre suite. Questo evidenzia come il semplice numero di assertion non sia sufficiente a descrivere la forza degli oracle.

Tutti i 12 test hanno nomi opachi/generati, quindi l'intento dei singoli casi deve essere ricostruito leggendo il corpo del metodo.

L'analisi SonarQube Cloud ha rilevato **65 code smell**, tutti nella logica della suite.

### Regole Sonar — T_RND

| Regola | Issue | Maintainability severity | Clean Code attribute | Categoria |
|---|---:|---|---|---|
| `java:S1481` | 5 | LOW | CLEAR | INTENTIONAL |
| `java:S1854` | 5 | MEDIUM | LOGICAL | INTENTIONAL |
| `java:S3415` | 6 | MEDIUM | LOGICAL | INTENTIONAL |
| `java:S5785` | 49 | MEDIUM | CLEAR | INTENTIONAL |

La regola dominante è `java:S5785`, con 49 issue. La presenza di 65 smell concentrati interamente nella logica generata rafforza l'evidenza di una bassa qualità statica della suite rispetto alle alternative.

---

## Analisi T_ES

EvoSuite migliora nettamente l'adequacy rispetto a Randoop:

- line coverage: 83.33%;
- branch coverage: 82.50%;
- method coverage: 81.82%;
- mutation score: 55.77%;
- test strength: 65.91%.

Su 52 mutanti, ne uccide 29, ne lascia sopravvivere 15 e ne lascia 8 non coperti.

Dal punto di vista strutturale, la suite contiene:

- 186 LOC di logica;
- 100 LOC di scaffolding;
- 286 LOC complessive.

È quindi l'artefatto più compatto dei tre in termini di LOC totali, pur richiedendo codice di supporto dedicato.

I 12 test hanno nomi opachi/generati e nessun nome descrittivo. La comprensione semantica del comportamento verificato richiede quindi l'ispezione del corpo dei test.

SonarQube Cloud ha rilevato **1 solo code smell**, localizzato interamente nello scaffolding e non nella logica dei test.

### Regola Sonar — T_ES

| Regola | Issue | Ruolo | Maintainability severity | Clean Code attribute | Categoria |
|---|---:|---|---|---|---|
| `java:S108` | 1 | SUPPORT | MEDIUM | CLEAR | INTENTIONAL |

Questo dato è importante: la logica EvoSuite risulta priva di smell secondo il quality profile Sonar utilizzato, mentre l'unica issue appartiene al supporto generato.

---

## Analisi T_LLM

La suite LLM ottiene i valori più elevati sulle principali metriche di adequacy:

- line coverage: 100%;
- branch coverage: 92.50%;
- method coverage: 100%;
- mutation score: 90.38%;
- test strength: 90.38%.

Su 52 mutanti, ne uccide 47, ne lascia sopravvivere 5 e non presenta mutanti non coperti.

Dal punto di vista strutturale, la suite contiene 344 LOC complessive e non richiede scaffolding.

Le 74 assertion-like statements corrispondono a 6.17 assertion per test. A differenza di Randoop, l'alta densità di assertion è accompagnata da un mutation score molto elevato.

Tutti i 12 test hanno nomi descrittivi, quindi la percentuale di naming descrittivo è pari al 100%.

L'analisi SonarQube Cloud ha rilevato:

- **0 maintainability issues**;
- **0 code smell**;
- **0 regole Sonar coinvolte**.

Nel caso di ListIteratorWrapper C0, quindi, T_LLM combina risultati elevati di adequacy con un risultato completamente pulito sul piano Sonar.

---

## Confronto di chiarezza

La chiarezza viene discussa usando proprietà osservabili del codice.

### Naming

- **T_RND:** 0% di nomi descrittivi;
- **T_ES:** 0% di nomi descrittivi;
- **T_LLM:** 100% di nomi descrittivi.

T_LLM comunica quindi l'intento dei test in modo più immediato attraverso il naming.

### Oracle e assertion

Randoop presenta la densità di assertion più alta, seguito da LLM ed EvoSuite.

Tuttavia, il mutation testing mostra che il numero di assertion da solo non predice la forza della suite:

- RND: 7.67 assertion/test → mutation score 11.54%;
- ES: 1.17 assertion/test → mutation score 55.77%;
- LLM: 6.17 assertion/test → mutation score 90.38%.

L'efficacia dipende quindi dalla rilevanza degli oracle e dagli scenari esercitati, non soltanto dalla quantità di assertion.

---

## Confronto di manutenibilità

### Dimensione e scaffolding

- T_RND: 397 LOC, nessuno scaffolding;
- T_ES: 286 LOC totali, di cui 100 di supporto;
- T_LLM: 344 LOC, nessuno scaffolding.

EvoSuite è l'artefatto più piccolo in termini di LOC complessive, ma è l'unico a richiedere scaffolding dedicato.

T_LLM mantiene una dimensione contenuta senza introdurre codice di supporto esterno.

### SonarQube Cloud

Per numero di code smell:

1. T_LLM: **0**
2. T_ES: **1**
3. T_RND: **65**

Nel caso di ListIteratorWrapper C0, T_LLM ottiene quindi anche il miglior risultato Sonar, mentre EvoSuite resta molto vicino con una sola issue nel supporto.

---

## Discussione complessiva

### T_RND

Randoop produce una suite senza scaffolding e raggiunge il 100% di method coverage, ma presenta:

- branch coverage sensibilmente inferiore;
- mutation score molto basso;
- test strength molto basso;
- naming opaco;
- 65 code smell Sonar.

È quindi la tecnica più debole nel confronto C0 corrente.

### T_ES

EvoSuite rappresenta un miglioramento marcato rispetto a Randoop:

- coverage più elevata;
- mutation score e test strength molto superiori;
- soltanto 1 smell Sonar;
- artefatto complessivamente compatto.

I principali limiti osservati sono il naming completamente generato e la dipendenza da scaffolding.

### T_LLM

La suite LLM presenta:

- 100% line coverage;
- 92.50% branch coverage;
- 100% method coverage;
- mutation score 90.38%;
- test strength 90.38%;
- nessun mutante non coperto;
- 100% di nomi descrittivi;
- nessuno scaffolding;
- 0 code smell Sonar.

Per questa classe e questa configurazione sperimentale, T_LLM ottiene quindi un profilo molto forte su tutte le dimensioni considerate.

---

## Conclusione

Per **ListIteratorWrapper C0**, i risultati permettono di formulare le seguenti conclusioni circoscritte all'esperimento:

- per **adequacy**, l'ordine osservato è `T_LLM > T_ES > T_RND`;
- per **chiarezza del naming**, T_LLM è nettamente favorito;
- per **qualità statica Sonar**, T_LLM ottiene 0 smell, EvoSuite 1 smell e Randoop 65;
- per **assenza di scaffolding**, T_RND e T_LLM non richiedono supporto dedicato, mentre EvoSuite introduce 100 LOC di scaffolding;
- per **dimensione totale**, EvoSuite è l'artefatto più compatto, ma il vantaggio deve essere letto insieme alla dipendenza dal supporto generato.

Nel complesso, **T_LLM mostra il profilo multidimensionale più forte su ListIteratorWrapper C0**, mentre **T_ES costituisce una soluzione intermedia molto più efficace e pulita di T_RND**.

Queste conclusioni sono specifiche della classe, della cardinalità e della configurazione sperimentale adottate e non vengono generalizzate automaticamente ad altri target o varianti.

---

## Evidenze

Le evidenze Sonar delle tre suite sono conservate in:

```text
isw2/results/testing/automatic-suite-quality/list-iterator-wrapper/c0/
├── rnd/sonar-ci/
├── es/sonar-ci/
└── llm/sonar-ci/
```

I dati strutturali comuni alle suite automatiche sono conservati in:

```text
isw2/results/testing/automatic-suite-quality/
├── structural-quality-baseline.csv
├── structural-quality-baseline.txt
└── structural-quality-protocol.txt
```

Il presente confronto costituisce la sintesi C0 per ListIteratorWrapper e verrà successivamente utilizzato nel confronto tra C0 e le varianti rifattorizzate.

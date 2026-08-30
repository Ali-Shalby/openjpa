# PCEnhancer C0 — Confronto delle suite automatiche

## Scopo

Questa sezione confronta le tre suite automatiche generate sulla versione **C0** di `org.apache.openjpa.enhance.PCEnhancer`:

- **T_RND** — Randoop
- **T_ES** — EvoSuite
- **T_LLM** — LLM

Il confronto è svolto a parità di cardinalità, con **N = 30 test per suite**, e considera congiuntamente:

- coverage;
- mutation score;
- test strength;
- dimensione della suite;
- dipendenza da codice di supporto/scaffolding;
- leggibilità dei nomi dei test;
- densità di assertion;
- code smell rilevati da SonarQube Cloud;
- regole e categorie Sonar;
- chiarezza e manutenibilità complessive.

L'obiettivo non è individuare un vincitore sulla base di una singola metrica, ma discutere i trade-off tra efficacia, leggibilità e qualità statica del codice di test.

---

## Risultati quantitativi

| Metrica | T_RND | T_ES | T_LLM |
|---|---:|---:|---:|
| Test | 30 | 30 | 30 |
| Test passati | 30/30 | 30/30 | 30/30 |
| Line coverage | 1.96% | 3.00% | **4.41%** |
| Branch coverage | 0.82% | 1.56% | **3.53%** |
| Mutanti uccisi | 2 | 10 | **39** |
| Mutation score | 0.12% | 0.59% | **2.29%** |
| Test strength | 40.00% | 71.43% | **86.67%** |
| Logic LOC | 473 | 512 | **458** |
| Support LOC | 25 | 892 | **0** |
| Total LOC | 498 | 1404 | **458** |
| Assertion-like statements | 63 | 30 | 30 |
| Assertion/test | 2.10 | 1.00 | 1.00 |
| Nomi opachi | 30 | 30 | **0** |
| Nomi descrittivi | 0% | 0% | **100%** |
| Sonar code smell | 57 | **0** | 6 |
| Regole Sonar distinte | 6 | **0** | 3 |

Per T_LLM è stata inoltre verificata la stabilità della suite tramite **5 esecuzioni consecutive**, tutte concluse con esito positivo.

---

## Analisi T_RND

La suite generata da Randoop è quella con i valori più bassi di adequacy tra le tre tecniche.

Con 30 test, raggiunge:

- 1.96% di line coverage;
- 0.82% di branch coverage;
- 2 mutanti uccisi;
- mutation score pari a 0.12%;
- test strength pari al 40.00%.

Dal punto di vista strutturale, la suite contiene 473 LOC di logica e 25 LOC di supporto, per un totale di 498 LOC. Le 63 assertion-like statements corrispondono a una media di 2.10 assertion per test.

Tutti i 30 test presentano nomi generati e non descrittivi. Questo riduce la comprensibilità immediata dell'intento di ciascun caso di test e rende più difficile ricostruire il comportamento verificato senza leggere direttamente il corpo del metodo.

L'analisi SonarQube Cloud ha rilevato **57 code smell**, tutti nella logica della suite e nessuno nel file di supporto.

### Regole Sonar — T_RND

| Regola | Issue | Maintainability severity | Clean Code attribute | Categoria |
|---|---:|---|---|---|
| `java:S1481` | 17 | LOW | CLEAR | INTENTIONAL |
| `java:S1854` | 17 | MEDIUM | LOGICAL | INTENTIONAL |
| `java:S3415` | 5 | MEDIUM | LOGICAL | INTENTIONAL |
| `java:S3577` | 1 | LOW | IDENTIFIABLE | CONSISTENT |
| `java:S5785` | 16 | MEDIUM | CLEAR | INTENTIONAL |
| `java:S5976` | 1 | MEDIUM | CONVENTIONAL | CONSISTENT |

La presenza di 57 smell distribuiti su 6 regole differenti indica che il problema non è riconducibile a una singola anomalia isolata, ma coinvolge più aspetti della qualità statica del codice generato.

---

## Analisi T_ES

EvoSuite migliora l'adequacy rispetto a Randoop:

- line coverage: 3.00%;
- branch coverage: 1.56%;
- mutanti uccisi: 10;
- mutation score: 0.59%;
- test strength: 71.43%.

L'analisi SonarQube Cloud non ha rilevato code smell nei quattro file analizzati:

- 0 smell nella logica;
- 0 smell nello scaffolding;
- 0 regole Sonar coinvolte.

Questo risultato deve però essere interpretato insieme alle altre dimensioni di qualità.

La suite contiene infatti:

- 512 LOC di logica;
- 892 LOC di supporto/scaffolding;
- 1404 LOC complessive.

Il codice di supporto rappresenta quindi una parte molto rilevante dell'artefatto generato. Inoltre i 30 test utilizzano nomi opachi/generati, con 0% di nomi descrittivi.

Di conseguenza, **assenza di smell Sonar non equivale automaticamente ad alta chiarezza o manutenibilità manuale**. EvoSuite risulta molto pulito secondo il quality profile Sonar adottato, ma introduce un costo infrastrutturale sensibilmente superiore alle altre due tecniche e non comunica semanticamente l'intento dei singoli test attraverso il naming.

---

## Analisi T_LLM

La suite LLM ottiene i risultati di adequacy più elevati tra le tre suite C0 considerate:

- 4.41% di line coverage;
- 3.53% di branch coverage;
- 39 mutanti uccisi;
- mutation score pari a 2.29%;
- test strength pari a 86.67%.

Dal punto di vista strutturale, la suite contiene 458 LOC complessive e non richiede scaffolding dedicato.

Tutti i 30 test utilizzano nomi descrittivi, quindi la percentuale di naming descrittivo è pari al 100%.

L'analisi SonarQube Cloud ha rilevato **6 code smell**, tutti nella logica della suite e distribuiti su 3 regole.

### Regole Sonar — T_LLM

| Regola | Issue | Maintainability severity | Clean Code attribute | Categoria |
|---|---:|---|---|---|
| `java:S1068` | 1 | MEDIUM | CLEAR | INTENTIONAL |
| `java:S1128` | 1 | LOW | CLEAR | INTENTIONAL |
| `java:S8924` | 4 | LOW | CLEAR | INTENTIONAL |

Rispetto a Randoop, il numero di smell è sensibilmente inferiore. Rispetto a EvoSuite, Sonar segnala invece alcune issue che impediscono di considerare T_LLM la suite migliore su ogni singola dimensione.

La suite mantiene tuttavia un vantaggio rilevante in termini di leggibilità semantica, assenza di scaffolding e adequacy.

---

## Confronto di chiarezza

La chiarezza viene discussa tramite proprietà osservabili del codice, senza introdurre punteggi soggettivi arbitrari.

### Naming

- **T_RND:** 0% di nomi descrittivi.
- **T_ES:** 0% di nomi descrittivi.
- **T_LLM:** 100% di nomi descrittivi.

Sotto questo aspetto T_LLM permette di comprendere più rapidamente lo scopo dei test senza dover ricostruire l'intento esclusivamente dal corpo del metodo.

### Oracle e assertion

Randoop presenta il numero più elevato di assertion-like statements, ma il semplice conteggio delle assertion non implica automaticamente una migliore leggibilità o una maggiore efficacia.

I risultati di mutation testing mostrano infatti che una maggiore densità di assertion non corrisponde, in questo caso, a una maggiore capacità di rilevare fault artificiali.

T_LLM ed EvoSuite hanno entrambe una media di circa una assertion per test, ma T_LLM raggiunge valori di mutation score e test strength significativamente superiori.

---

## Confronto di manutenibilità

La manutenibilità viene valutata combinando più evidenze.

### Dimensione e scaffolding

- T_RND: 498 LOC totali.
- T_ES: 1404 LOC totali, di cui 892 di supporto.
- T_LLM: 458 LOC totali e nessuno scaffolding.

EvoSuite introduce quindi il costo infrastrutturale maggiore, mentre T_LLM produce l'artefatto complessivamente più compatto.

### SonarQube Cloud

Per numero di code smell:

1. T_ES: **0**
2. T_LLM: **6**
3. T_RND: **57**

Sotto la sola dimensione Sonar, EvoSuite presenta quindi il risultato migliore.

Questo dato non deve però essere usato come sostituto della valutazione complessiva di manutenibilità: il volume dello scaffolding e il naming dei test sono caratteristiche concrete non catturate dal semplice conteggio degli smell.

---

## Discussione complessiva

Le tre tecniche mostrano profili differenti.

### T_RND

Randoop produce una suite eseguibile e relativamente compatta, ma sul target PCEnhancer C0 mostra:

- adequacy molto bassa;
- naming opaco;
- 57 smell Sonar;
- ridotta capacità di uccidere mutanti.

È quindi la tecnica meno efficace nel confronto corrente.

### T_ES

EvoSuite migliora nettamente coverage, mutation score e test strength rispetto a Randoop e non presenta smell secondo SonarQube Cloud.

Il principale trade-off riguarda però:

- forte dipendenza dallo scaffolding;
- dimensione complessiva molto elevata;
- naming completamente generato e poco descrittivo.

### T_LLM

La suite LLM presenta:

- coverage più elevata;
- mutation score più elevato;
- test strength più elevato;
- 100% di nomi descrittivi;
- nessuno scaffolding;
- dimensione complessiva inferiore alle altre suite;
- 6 smell Sonar.

Pertanto T_LLM non domina ogni singola metrica — EvoSuite mantiene il miglior risultato Sonar — ma presenta il profilo complessivo più equilibrato tra adequacy, chiarezza e costo strutturale.

---

## Conclusione

Per **PCEnhancer C0**, i risultati permettono di formulare le seguenti conclusioni circoscritte all'esperimento svolto:

- per **adequacy**, l'ordine osservato è `T_LLM > T_ES > T_RND`;
- per **chiarezza del naming**, T_LLM è nettamente favorito;
- per **assenza di smell Sonar**, T_ES ottiene il risultato migliore;
- per **peso infrastrutturale**, T_LLM è favorito dall'assenza di scaffolding, mentre T_ES presenta il costo maggiore;
- T_RND è la suite più debole sia sul piano dell'adequacy sia per numero di smell.

Non viene quindi adottata una nozione di “migliore suite” basata su un'unica metrica. Nel complesso, **T_LLM mostra il miglior compromesso multidimensionale per PCEnhancer C0**, mentre **T_ES è la suite più pulita secondo SonarQube Cloud**.

Queste conclusioni sono specifiche della classe e della configurazione sperimentale considerate e non vengono generalizzate automaticamente ad altre classi o varianti.

---

## Evidenze

Le evidenze Sonar delle tre suite sono conservate in:

```text
isw2/results/testing/automatic-suite-quality/pcenhancer/c0/
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

Il presente confronto costituisce la sintesi C0 per PCEnhancer e verrà successivamente utilizzato come input per il confronto finale tra tecniche e varianti previsto dall'analisi De Angelis.

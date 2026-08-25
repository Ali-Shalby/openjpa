# PCEnhancer – Generazione tramite LLM (`T_LLM`)

## Obiettivo

Questa fase costruisce e valuta la suite automatica `T_LLM` per:

```text
org.apache.openjpa.enhance.PCEnhancer
```

sulla baseline:

```text
Apache OpenJPA 4.1.1
```

La cardinalità sperimentale è fissata a:

```text
N = 30
```

in modo da mantenere confrontabili le suite automatiche `T_RND`, `T_ES` e
`T_LLM`.

I test nativi di OpenJPA e le altre suite sperimentali non vengono utilizzati
come contesto di generazione.

---

## Protocollo di generazione

L'esperimento utilizza:

```text
Suite              : T_LLM
Target             : org.apache.openjpa.enhance.PCEnhancer
Baseline           : OpenJPA 4.1.1
Cardinalità        : 30 test case/scenari
Framework          : JUnit Jupiter
Runtime            : Java 21
LLM client         : Microsoft Copilot
Interaction mode   : browser chat
Model              : GPT 5.6 Think Deeper
Model provider     : OpenAI
```

Il protocollo viene fissato prima della generazione e prevede un solo prompt
principale.

Il prompt richiede, nella stessa risposta:

1. una breve analisi di `PCEnhancer` dal punto di vista del testing;
2. la progettazione di esattamente 30 scenari `TLLM-001 ... TLLM-030`;
3. l'implementazione degli stessi 30 casi in Java 21 con JUnit Jupiter;
4. la tracciabilità fra casi progettati e relativa implementazione.

Il contesto iniziale comprende esclusivamente il sorgente production di
`PCEnhancer` e le informazioni tecniche minime dell'ambiente `T_LLM`.

Non vengono forniti all'LLM:

```text
test nativi OpenJPA
T_BB
T_CF
T_MT
T_RND
T_ES
risultati JaCoCo
risultati PIT
survivor mutation
fonti Web o esterne
```

JaCoCo e PIT vengono utilizzati soltanto dopo il freeze della suite e non
costituiscono feedback di generazione.

Le evidence complete del protocollo, del prompt e delle risposte sono
conservate in:

```text
isw2/testing/llm/pcenhancer/
```

---

## Generazione e repair tecnici

Il prompt principale ha prodotto:

```text
30 scenari distinti
TLLM-001 ... TLLM-030
30 test JUnit Jupiter
mapping design -> implementation
```

Durante l'integrazione sono stati necessari esclusivamente repair tecnici.

### Rendering dell'output

Il blocco Java restituito dall'interfaccia Copilot presentava caratteri spuri
inseriti nel codice.

Il repair è stato utilizzato esclusivamente per recuperare il codice già
generato, senza modificare:

- casi `TLLM-001 ... TLLM-030`;
- oracle;
- fixture;
- logica dei test.

### Completamento dell'output

Una risposta di repair risultava troncata dopo i primi casi della suite.

È stata quindi richiesta esclusivamente la parte mancante del codice già
generato, senza introdurre nuovi scenari.

### Repair Mockito

La compilazione ha evidenziato tre errori di generic wildcard nel mocking
utilizzato da TLLM-029 e TLLM-030.

La correzione ha sostituito esclusivamente lo stubbing:

```text
when(...).thenReturn(...)
```

con:

```text
Mockito.doReturn(...).when(...)
```

mantenendo invariati significato e oracle dei due test.

Nessun repair è stato motivato da Line Coverage, Branch Coverage, Mutation
Score o survivor PIT.

---

## Integrazione nel testing harness

La suite canonica è:

```text
isw2/testing/src/test/java/
└── org/apache/openjpa/enhance/
    └── PCEnhancerLLMTest.java
```

Il package coincide con quello production perché alcuni scenari generati
utilizzano helper package-private di `PCEnhancer`.

Mockito viene utilizzato per costruire collaborator controllati quando il caso
di test non richiede l'infrastruttura completa di OpenJPA.

---

## Validazione e freeze

La suite viene compilata ed eseguita con il normale harness Java 21.

Risultato:

```text
Tests run : 30
PASS      : 30
Failures  : 0
Errors    : 0
Skipped   : 0
```

La stabilità viene verificata mediante cinque esecuzioni consecutive:

```text
Stability : 5/5 PASS
```

Dopo questa verifica la suite viene congelata.

```text
T_LLM STATUS : FROZEN
```

Il sorgente canonico congelato contiene esattamente:

```text
30 @Test
TLLM-001 ... TLLM-030
```

Successivamente è stato aggiunto esclusivamente l'header di licenza ASF
richiesto dal controllo RAT del repository.

La modifica è comment-only e non altera cardinalità, oracle, fixture, helper o
semantica eseguibile dei test.

SHA-256 canonico corrente:

```text
4E785E30F56C5E07C454DA15D9E2EEE6DC50419C12CECBD792327CDD0C6ADBFC
```

Il freeze manifest è conservato in:

```text
isw2/testing/llm/pcenhancer/evidence/
└── pcenhancer-tllm-freeze-manifest.txt
```

---

## Line e Branch Coverage

JaCoCo viene eseguito esclusivamente dopo il freeze sulla sola classe esterna:

```text
org.apache.openjpa.enhance.PCEnhancer
```

Risultato:

```text
LINE
Covered  : 119
Missed   : 2580
Total    : 2699
Coverage : 4.41%

BRANCH
Covered  : 43
Missed   : 1174
Total    : 1217
Coverage : 3.53%
```

I denominatori coincidono con l'universo canonico utilizzato per il confronto
delle suite di `PCEnhancer`.

La coverage non viene utilizzata per modificare o rigenerare `T_LLM`.

Evidence:

```text
isw2/results/testing/pcenhancer/llm/coverage/
├── jacoco.csv
├── jacoco.xml
├── pcenhancer-tllm-coverage-summary.csv
└── pcenhancer-tllm-jacoco-measurement-note.md
```

---

## Mutation testing

La mutation analysis viene effettuata dopo il freeze con:

```text
PIT          : 1.25.8
Target       : outer PCEnhancer only
Mutators     : DEFAULTS
Threads      : 1
Runtime      : Java 21
Native tests : NOT USED
```

Il run PIT finale utilizza il medesimo target e la medesima configurazione di
mutazione adottati per il confronto delle suite automatiche.

Risultato RAW:

```text
Population   : 1700
KILLED       : 39
SURVIVED     : 6
NO_COVERAGE  : 1655
TIMED_OUT    : 0
RUN_ERROR    : 0
MEMORY_ERROR : 0
```

Metriche:

```text
Mutation Score = KILLED / TOTAL
               = 39 / 1700
               = 2.29%

Test Strength  = KILLED / (KILLED + SURVIVED)
               = 39 / 45
               = 86.67%
```

Il Mutation Score resta basso in valore assoluto principalmente per l'elevato
numero di mutanti `NO_COVERAGE`.

Sui 45 mutanti raggiunti e classificati come `KILLED` o `SURVIVED`, la suite
ne uccide 39, ottenendo un Test Strength pari a 86.67%.

I survivor vengono mantenuti come risultato sperimentale e non vengono
utilizzati per modificare la suite congelata.

Evidence:

```text
isw2/results/testing/pcenhancer/llm/mutation/
├── pcenhancer_tllm_mutation_summary.txt
├── pcenhancer_tllm_mutations.csv
├── pcenhancer_tllm_mutations.xml
└── pcenhancer_tllm_pit_launcherfix_run.txt
```

---

## Confronto con le altre suite automatiche

A parità di cardinalità:

```text
N = 30
```

i risultati sono:

| Suite | Line Coverage | Branch Coverage | KILLED | Mutation Score | Test Strength |
|---|---:|---:|---:|---:|---:|
| `T_RND` | 1.96% | 0.82% | 2 | 0.12% | 40.00% |
| `T_ES` | 3.00% | 1.56% | 10 | 0.59% | 71.43% |
| `T_LLM` | 4.41% | 3.53% | 39 | 2.29% | 86.67% |

Sul target `PCEnhancer`, `T_LLM` ottiene i valori più elevati tra le tre suite
automatiche per entrambe le metriche di coverage e per le metriche mutation.

I valori assoluti di coverage restano comunque contenuti. Il risultato viene
quindi interpretato come specifico dell'esperimento su `PCEnhancer` e non come
una valutazione generale degli strumenti di generazione.

---

## Valutazione qualitativa

La suite LLM privilegia comportamenti deterministici e direttamente
osservabili di `PCEnhancer`, tra cui:

- costanti e stati configurabili;
- riconoscimento dei nomi delle sottoclassi persistence-capable;
- conversione verso il nome del managed type;
- configurazione di redefine e subclassing;
- configurazione della directory e del `BytecodeWriter`;
- analisi di getter e setter tramite helper package-private;
- risoluzione del tipo concreto associato ai metadata.

Sono meno rappresentati i percorsi completi di enhancement che richiedono una
configurazione più ampia di metadata, identity, serialization e runtime
OpenJPA.

Questo è coerente con le metriche osservate: la suite presenta oracle efficaci
sulle porzioni raggiunte, ma esercita una parte limitata della classe completa.

---

## Evidence

Le evidence della generazione sono conservate in:

```text
isw2/testing/llm/pcenhancer/
```

Le misure post-freeze sono conservate in:

```text
isw2/results/testing/pcenhancer/llm/
├── coverage/
└── mutation/
```

---

## Freeze finale

La fase `T_LLM` viene chiusa con:

```text
T_LLM                : 30
Validation           : 30/30 PASS
Stability            : 5/5 PASS

Line Coverage        : 4.41% (119 / 2699)
Branch Coverage      : 3.53% (43 / 1217)

Mutation population  : 1700
KILLED               : 39
SURVIVED             : 6
NO_COVERAGE          : 1655
TIMED_OUT            : 0
Mutation Score       : 2.29%
Test Strength        : 86.67%

T_LLM STATUS         : FROZEN / COMPLETE
```

JaCoCo e PIT rimangono esclusivamente metriche post-freeze.

Non vengono pianificate ulteriori interrogazioni LLM sulla base delle metriche
di adequacy osservate.

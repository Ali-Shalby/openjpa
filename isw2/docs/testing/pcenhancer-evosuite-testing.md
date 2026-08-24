# PCEnhancer – Generazione coverage-guided con EvoSuite (`T_ES`)

## Obiettivo

Questa fase costruisce e valuta la suite automatica coverage-guided `T_ES` per:

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

in modo da mantenere confrontabili le suite automatiche con la suite black-box iniziale `T_BB`.

I test nativi di OpenJPA non vengono utilizzati.

---

## Protocollo di generazione

La suite è stata generata con:

```text
Tool              : EvoSuite 1.2.0
Target            : org.apache.openjpa.enhance.PCEnhancer
Criterion         : LINE:BRANCH
Search budget     : 120 s per seed
Stopping condition: MAXTIME
Test format       : JUnit 4
Minimization      : enabled
Max suite size    : 30
Runtime generator : Zulu JDK 11
```

La generazione è stata effettuata senza utilizzare risultati JaCoCo o PIT come feedback per
rigenerare, selezionare o migliorare i test.

La scelta di JDK 11 è un requisito tecnico dell'ambiente EvoSuite 1.2.0 utilizzato per la
generazione e l'esecuzione della suite. La baseline production analizzata resta OpenJPA 4.1.1;
il normale testing harness del progetto mantiene Java 21.

---

## Strategia multi-seed e freeze di `N`

La cardinalità finale è stata ottenuta mediante seed deterministici consecutivi.

```text
Seed 0 : 15 test finali
Seed 1 : 18 test finali
```

La stopping rule adottata, fissata prima di osservare JaCoCo e PIT, richiede di fermarsi
appena siano disponibili almeno `N = 30` test validi. Non vengono eseguiti seed ulteriori.

La suite finale utilizza:

```text
Seed 0 : test00 ... test14 = 15
Seed 1 : test00 ... test14 = 15
--------------------------------
T_ES                          = 30
```

Gli ultimi tre test del seed 1 (`test15`, `test16`, `test17`) non vengono inclusi. La selezione
è puramente posizionale e non utilizza informazioni di coverage o mutation score.

Gli output RAW dei due seed vengono conservati nel workspace sperimentale locale, separati
per seed:

```text
isw2/testing/generated/pcenhancer/evosuite/raw/
├── seed-0/
└── seed-1/
```

La directory `generated/` non costituisce il sorgente canonico eseguibile versionato: nel
repository vengono mantenuti i test integrati sotto `src/test/java` e le evidence di generazione
sotto `isw2/results/testing/pcenhancer/es/`.

---

## Integrazione nel testing harness

La suite integrata è organizzata in:

```text
isw2/testing/src/test/java/
├── it/uniroma2/isw2/openjpa/testing/pcenhancer/es/
│   ├── PCEnhancerEvoSuiteSeed0Test.java
│   ├── PCEnhancerEvoSuiteSeed0Scaffolding.java
│   ├── PCEnhancerEvoSuiteSeed1Test.java
│   └── PCEnhancerEvoSuiteSeed1Scaffolding.java
└── org/apache/openjpa/enhance/
    └── PCEnhancerTestAccess.java
```

I due scaffolding restano separati perché EvoSuite ha prodotto infrastrutture runtime differenti
per i due seed.

`PCEnhancerTestAccess` è un bridge esclusivamente di test. Delega direttamente a helper
package-protected di `PCEnhancer` necessari ad alcuni casi generati; non contiene logica di
testing aggiuntiva e non modifica il production code.

Gli adattamenti di integrazione sono meccanici:

- rinomina delle classi per evitare collisioni;
- mantenimento dei 15 test del seed 0;
- mantenimento dei primi 15 test del seed 1;
- bridge di accesso per helper package-protected;
- conservazione separata degli scaffolding;
- nessuna modifica manuale degli oracle generati.

Per la misurazione canonica viene utilizzato:

```text
separateClassLoader = false
```

mantenendo comunque invariati i 30 casi di test. Questa configurazione consente di eseguire la
suite con il runtime tecnico Java 11 e di rendere osservabile l'esecuzione del target a JaCoCo.

Il normale harness Maven resta configurato su Java 21. I sorgenti `T_ES` e
`PCEnhancerTestAccess` sono esclusi dalla compilazione standard Java 21 e vengono compilati ed
eseguiti nell'harness isolato Java 11 dedicato a EvoSuite. La separazione riguarda soltanto
l'infrastruttura di esecuzione: target, input e oracle della suite congelata restano invariati.

---

## Validazione della suite

Ambiente canonico di validazione:

```text
Runtime : Zulu JDK 11
JUnit   : 4.13.2
T_ES    : 30
```

Risultato:

```text
Tests run : 30
PASS      : 30
FAIL      : 0
Errors    : 0
Skipped   : 0
```

La suite è quindi verde prima delle misure di adequacy.

Evidence:

```text
isw2/results/testing/pcenhancer/es/validation/
└── pcenhancer_tes_java11_validation.txt
```

---

## Line e Branch Coverage

La coverage viene misurata con:

```text
JaCoCo : 0.8.15
Target : outer class org.apache.openjpa.enhance.PCEnhancer
Tests  : T_ES = 30
```

Risultato:

```text
LINE
Covered : 81
Missed  : 2618
Total   : 2699
Coverage: 3.00%

BRANCH
Covered : 19
Missed  : 1198
Total   : 1217
Coverage: 1.56%
```

La misurazione viene effettuata soltanto dopo il freeze della suite e non viene utilizzata come
feedback per modificarla.

Evidence:

```text
isw2/results/testing/pcenhancer/es/coverage/
├── jacoco.csv
├── pcenhancer_tes_coverage_summary.txt
├── pcenhancer_tes_jacoco.exec
└── pcenhancer_tes_jacoco_execution.txt
```

---

## Mutation testing

La mutation analysis utilizza lo stesso protocollo congelato per il confronto delle suite di
`PCEnhancer`:

```text
PIT          : 1.25.8
Target       : outer PCEnhancer only
Mutators     : DEFAULTS
Threads      : 1
Runtime      : Zulu JDK 11
Native tests : NOT USED
```

PIT viene eseguito in modalità command-line diretta sul bytecode canonico di `PCEnhancer`.
Il bytecode production utilizzato è quello di `openjpa-kernel:4.1.1`, verificato mediante SHA-256:

```text
3C825DF257CC2FCF6550448E177A602495600A0470B742457AFC46BF4D788911
```

La popolazione viene confrontata con la baseline mutation già congelata:

```text
isw2/results/testing/pcenhancer/mutation/baseline/pcenhancer_mutations.xml
```

Verifica di identità:

```text
Mutants             : 1700
Expected            : 1700
Population identity : True
Identity match      : 1700/1700
```

Risultato RAW:

```text
KILLED      : 10
SURVIVED    : 4
NO_COVERAGE : 1686
TIMED_OUT   : 0
RUN_ERROR   : 0
MEMORY_ERROR: 0
```

Metriche:

```text
Mutation Score = KILLED / TOTAL
               = 10 / 1700
               = 0.59%

Test Strength  = KILLED / (KILLED + SURVIVED)
               = 10 / 14
               = 71.43%
```

Il Mutation Score molto basso è principalmente determinato dall'elevato numero di mutanti
`NO_COVERAGE`. Sui pochi mutanti effettivamente raggiunti dalla suite, invece, la percentuale di
mutanti uccisi è elevata, come mostrato dal Test Strength.

Evidence:

```text
isw2/results/testing/pcenhancer/es/mutation/
├── pcenhancer_tes_mutation_summary.txt
├── pcenhancer_tes_mutations.csv
├── pcenhancer_tes_mutations.xml
├── pcenhancer_tes_pit_run.txt
└── pcenhancer_tes_pre_pit_validation.txt
```

---

## Confronto con il random testing

A parità di cardinalità `N = 30`:

| Suite | Line Coverage | Branch Coverage | KILLED | Mutation Score | Test Strength |
|---|---:|---:|---:|---:|---:|
| `T_RND` | 1.96% | 0.82% | 2 | 0.12% | 40.00% |
| `T_ES` | 3.00% | 1.56% | 10 | 0.59% | 71.43% |

`T_ES` supera `T_RND` sia in Line/Branch Coverage sia nella mutation analysis, pur mantenendo
valori assoluti di coverage contenuti sulla classe `PCEnhancer`.

---

## Evidence di generazione

Le evidence canoniche delle due generazioni utilizzate sono:

```text
isw2/results/testing/pcenhancer/es/generation/
├── pcenhancer_evosuite_seed0_result.txt
└── pcenhancer_evosuite_seed1_result.txt
```

Questi file registrano i due seed che hanno contribuito alla costruzione della suite finale.

---

## Freeze finale

La fase `T_ES` è congelata con:

```text
T_ES                : 30
Seed 0 contribution : 15
Seed 1 contribution : 15
Validation           : 30/30 PASS

Line Coverage        : 3.00%  (81 / 2699)
Branch Coverage      : 1.56%  (19 / 1217)

Mutation population  : 1700/1700 identical
KILLED               : 10
SURVIVED             : 4
NO_COVERAGE          : 1686
TIMED_OUT            : 0
Mutation Score       : 0.59%
Test Strength        : 71.43%
```

Non vengono pianificate ulteriori generazioni EvoSuite sulla base delle metriche osservate.
La suite resta congelata per il successivo confronto tra le tecniche di testing.

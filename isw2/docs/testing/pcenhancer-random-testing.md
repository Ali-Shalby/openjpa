# PCEnhancer â€“ Random Test Generation (`T_RND`)

## 1. Obiettivo

La fase `T_RND` valuta una suite generata automaticamente mediante
approccio randomico sulla classe:

```text
org.apache.openjpa.enhance.PCEnhancer
```

L'obiettivo Ã¨ confrontare una tecnica di generazione automatica random
con le suite manuali e con le altre tecniche automatiche previste
dall'esperimento.

La generazione viene mantenuta indipendente dalle metriche di adequacy:
JaCoCo e PIT vengono utilizzati solamente dopo il freeze della suite.

---

## 2. Strumento

Ãˆ stato utilizzato:

```text
Randoop 4.3.4
```

Randoop applica una strategia di feedback-directed random test
generation, costruendo sequenze di chiamate sui membri raggiungibili
della classe target.

---

## 3. Protocollo di generazione

Il protocollo Ã¨ stato definito prima della misurazione di coverage e
mutation testing.

```text
Target                  : org.apache.openjpa.enhance.PCEnhancer
CardinalitÃ  N           : 30
Random seed             : 0
ModalitÃ  deterministica : SI
Time limit              : 0
Generated limit         : 20000
Output limit            : 30
Solo membri pubblici    : SI
Error-revealing test    : DISABILITATI
Feedback coverage       : NESSUNO
Feedback mutation       : NESSUNO
Test nativi OpenJPA     : NON UTILIZZATI
```

Il seed fisso e l'assenza di un limite temporale wall-clock sono stati
adottati per rendere la generazione riproducibile.

Una prima invocazione non esponeva correttamente `PCEnhancer` nel
classpath di Randoop e ha prodotto zero test.

Tale tentativo viene classificato come errore infrastrutturale
precedente all'esperimento e non come esecuzione sperimentale.

La prima generazione con infrastruttura valida ha raggiunto
direttamente la cardinalitÃ  richiesta.

---

## 4. Output generato

La generazione valida ha prodotto:

```text
Regression test         : 30
Error-revealing test    : 0
File Java               : 2
```

I file prodotti da Randoop sono:

```text
PCEnhancerRandoopRegression.java
PCEnhancerRandoopRegression0.java
```

Il primo costituisce il wrapper della suite JUnit 4.

Il secondo contiene i 30 metodi `@Test` generati automaticamente.

---

## 5. Validazione RAW

Prima dell'integrazione nell'harness Maven, l'output originale Ã¨ stato
compilato ed eseguito senza modifiche utilizzando JUnit 4.13.2.

Risultato:

```text
Tests run               : 30
Failures                : 0
Errors                  : 0
Skipped                 : 0
Risultato               : PASS
```

Durante la compilazione `javac` ha segnalato l'utilizzo o override di
una API deprecata.

Il messaggio costituisce esclusivamente un warning e non ha impedito
la compilazione.

---

## 6. Integrazione nel testing harness

Randoop 4.3.4 genera test JUnit 4, mentre l'harness del progetto
utilizza la JUnit Platform.

Per eseguire la suite generata senza riscriverla Ã¨ stato aggiunto:

```text
junit-vintage-engine
```

Le sole trasformazioni effettuate sui sorgenti generati sono state
meccaniche:

```text
1. aggiunta dell'header ASF;
2. aggiunta del package:
   it.uniroma2.isw2.openjpa.testing.pcenhancer.rnd
3. rinomina del wrapper:
   PCEnhancerRandoopRegression
   -> PCEnhancerRandoopRegressionTest
```

La rinomina permette a Maven Surefire di individuare un unico entry
point evitando una doppia esecuzione della classe contenente i 30 test.

Non sono stati modificati:

- input generati;
- assertion;
- eccezioni attese;
- oracle;
- struttura dei singoli test.

La suite integrata produce:

```text
Tests run               : 30
Failures                : 0
Errors                  : 0
Skipped                 : 0
BUILD                   : SUCCESS
```

---

## 7. Freeze della suite

Dopo la validazione tecnica la suite Ã¨ stata congelata:

```text
T_RND = 30
```

Da questo momento JaCoCo e PIT vengono utilizzati esclusivamente per
valutare la tecnica di generazione.

Non vengono utilizzati per:

- rigenerare i test;
- selezionare test migliori;
- modificare gli oracle;
- aggiungere test manuali alla suite random.

---

## 8. Coverage JaCoCo

La coverage Ã¨ stata misurata sulla sola suite `T_RND`.

Target:

```text
org.apache.openjpa.enhance.PCEnhancer
```

Versione:

```text
JaCoCo 0.8.15
```

Risultato:

| Metrica | Covered | Total | Coverage |
|---|---:|---:|---:|
| Line | 53 | 2699 | 1.96% |
| Branch | 10 | 1217 | 0.82% |

La coverage particolarmente bassa Ã¨ coerente con la struttura dei test
prodotti da Randoop.

La maggior parte delle sequenze raggiunge comportamento pubblico
superficiale, costanti, semplici trasformazioni oppure percorsi che
terminano rapidamente con eccezioni previste.

La logica piÃ¹ profonda di enhancement richiede configurazioni di
metadata, bytecode e stato di persistenza che la generazione random
non riesce a costruire efficacemente.

Il risultato viene mantenuto invariato come evidenza sperimentale.

---

## 9. Mutation testing

La suite congelata Ã¨ stata successivamente valutata con PIT utilizzando
la stessa popolazione di mutanti adottata per l'analisi manuale.

```text
PIT                     : 1.25.8
Mutators                : DEFAULTS
Threads                 : 1
Popolazione             : 1700
```

Risultato:

| Stato | Mutanti |
|---|---:|
| KILLED | 2 |
| SURVIVED | 3 |
| NO_COVERAGE | 1695 |
| TIMED_OUT | 0 |
| Totale | 1700 |

Le metriche utilizzate sono:

```text
Mutation Score =
    KILLED / TOTAL MUTANTS

Test Strength =
    KILLED / (KILLED + SURVIVED)
```

Da cui:

```text
Mutation Score          : 0.12%
Test Strength           : 40.00%
```

La suite raggiunge soltanto cinque mutanti della popolazione:
due vengono uccisi e tre sopravvivono.

L'elevato numero di `NO_COVERAGE` Ã¨ coerente con la coverage JaCoCo
misurata indipendentemente.

La suite non viene rigenerata nÃ© modificata sulla base di questi
risultati.

---

## 10. Confronto preliminare

A paritÃ  di cardinalitÃ , `T_BB` e `T_RND` mostrano una differenza
significativa:

| Suite | Test | Line | Branch |
|---|---:|---:|---:|
| `T_BB` | 30 | 43.31% | 30.24% |
| post-`T_CF` | 35 | 70.47% | 54.89% |
| `T_RND` | 30 | 1.96% | 0.82% |

Nel caso di `PCEnhancer`, la generazione random riesce quindi a
costruire una suite completamente eseguibile ma non riesce a esplorare
in profonditÃ  la logica della classe.

La conclusione resta descrittiva: la suite non viene modificata dopo
l'osservazione delle metriche.

---

## 11. Evidence

Le evidence canoniche sono organizzate in:

```text
isw2/results/testing/pcenhancer/rnd/
â”œâ”€â”€ pcenhancer_randoop_summary.txt
â”œâ”€â”€ generation/
â”‚   â”œâ”€â”€ pcenhancer_randoop_generation_metadata.txt
â”‚   â””â”€â”€ pcenhancer_randoop_generation_run.txt
â”œâ”€â”€ validation/
â”‚   â”œâ”€â”€ pcenhancer_randoop_raw_run.txt
â”‚   â””â”€â”€ pcenhancer_randoop_raw_validation_summary.txt
â”œâ”€â”€ runs/
â”‚   â””â”€â”€ pcenhancer_randoop_integrated_test_result.xml
â”œâ”€â”€ coverage/
â”‚   â”œâ”€â”€ jacoco.csv
â”‚   â”œâ”€â”€ pcenhancer_randoop_jacoco.exec
â”‚   â””â”€â”€ pcenhancer_randoop_coverage_summary.txt
â”œâ”€â”€ mutation/
â”‚   â”œâ”€â”€ pcenhancer_randoop_mutations.xml
â”‚   â”œâ”€â”€ pcenhancer_randoop_pit_run.txt
â”‚   â””â”€â”€ pcenhancer_randoop_mutation_summary.txt
â””â”€â”€ audit/
    â””â”€â”€ pcenhancer_randoop_freeze_audit.txt
```

I sorgenti canonici sono:

```text
isw2/testing/src/test/java/
â””â”€â”€ it/uniroma2/isw2/openjpa/testing/pcenhancer/rnd/
    â”œâ”€â”€ PCEnhancerRandoopRegression0.java
    â””â”€â”€ PCEnhancerRandoopRegressionTest.java
```

---

## 12. Stato finale

```text
Generatore              : Randoop 4.3.4

T_RND                   : 30
PASS                    : 30
FAIL                    : 0

Line Coverage           : 1.96%
Branch Coverage         : 0.82%

Popolazione PIT         : 1700
KILLED                  : 2
SURVIVED                : 3
NO_COVERAGE             : 1695
TIMED_OUT               : 0

Mutation Score          : 0.12%
Test Strength           : 40.00%

Feedback coverage       : NESSUNO
Feedback mutation       : NESSUNO
Modifica manuale oracle : NESSUNA

Stato                   : FROZEN
```

La successiva fase di generazione automatica per `PCEnhancer` Ã¨
`T_ES`, basata su EvoSuite.
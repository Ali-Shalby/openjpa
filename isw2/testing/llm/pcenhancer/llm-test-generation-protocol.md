# PCEnhancer - Protocollo di generazione LLM (`T_LLM`)

## Obiettivo

Generare e valutare una suite automatica di test tramite LLM per:

```text
org.apache.openjpa.enhance.PCEnhancer
```

sulla baseline Apache OpenJPA 4.1.1.

Il protocollo viene congelato prima della prima interazione con l'LLM.

---

## Parametri sperimentali

```text
Suite                  : T_LLM
Target                 : org.apache.openjpa.enhance.PCEnhancer
Baseline               : OpenJPA 4.1.1
Target cardinality     : N = 30 test case/scenari distinti
Framework              : JUnit Jupiter
Runtime                : Java 21
LLM client             : Microsoft Copilot
Interaction mode       : browser chat
Model                  : da registrare prima di P1 se esposto dall'interfaccia;
                         altrimenti "non esposto"
Native OpenJPA tests   : NON UTILIZZATI
```

Il valore `N = 30` deriva dalla cardinalità della suite black-box iniziale ed è
fissato prima della generazione LLM per mantenere confrontabili le tecniche.

`N` indica 30 test case/scenari concettualmente distinti, non necessariamente
30 metodi Java. L'implementazione può utilizzare una o più classi di test,
fixture, setup condiviso, metodi helper e utility di test. Tali elementi di
supporto non costituiscono test case aggiuntivi.

Deve essere mantenuta una tracciabilità esplicita tra i 30 test case
`TLLM-001 ... TLLM-030` e la loro implementazione/esecuzione.

---

## Principio di neutralità del prompting

Il protocollo controlla rigidamente il contesto che l'LLM può utilizzare, ma
evita di prescrivere a priori caratteristiche qualitative della suite.

I prompt non richiedono esplicitamente di:
- massimizzare coverage o mutation score;
- produrre test "forti", "non banali" o "ottimali";
- privilegiare specifiche categorie di casi;
- imitare le suite già costruite;
- utilizzare specifiche tecniche di isolamento se non necessarie.

La rilevanza, la significatività, la ridondanza e la qualità degli oracle
prodotti dall'LLM vengono valutate a posteriori come risultati
dell'esperimento.

---

## Contesto consentito

L'LLM può ricevere esclusivamente:

- il production code di `PCEnhancer`;
- production code aggiuntivo richiesto esplicitamente dall'LLM e necessario a comprenderne le API;
- informazioni tecniche minime sull'harness di testing;
- Java 21;
- JUnit Jupiter;
- Mockito, disponibile ma non obbligatorio;
- OpenJPA 4.1.1.

Ogni file production aggiunto al contesto viene registrato nell'evidence.

---

## Contesto vietato

Durante analisi, progettazione e generazione non devono essere forniti all'LLM:

- test nativi di OpenJPA;
- `T_BB`;
- `T_CF`;
- `T_MT`;
- `T_RND`;
- `T_ES`;
- risultati JaCoCo delle suite precedenti;
- risultati PIT;
- survivor mutation;
- documentazione del progetto che riveli i risultati delle suite precedenti;
- indicazioni su linee, branch o mutanti da raggiungere.

Non vengono fornite all'LLM fonti esterne aggiuntive per colmare informazioni
mancanti. Se una dipendenza production necessaria non è disponibile, deve
richiederla esplicitamente anziché inventarla.

---

## Strategia di interrogazione

L'interazione viene articolata in tre interrogazioni distinte e documentate.

### P1 - Functional Analysis

L'LLM analizza `PCEnhancer` dal punto di vista del testing.

In questa fase:
- non viene generato codice di test;
- non vengono ancora definiti i 30 casi finali;
- l'LLM può segnalare eventuali classi production mancanti necessarie a
  comprendere correttamente il comportamento.

### P2 - Test Design

Sulla base del production context disponibile e dell'analisi P1, l'LLM
progetta esattamente 30 test case/scenari:

```text
TLLM-001 ... TLLM-030
```

Per ogni caso vengono descritti:
- comportamento considerato;
- input/setup;
- azione;
- risultato atteso o altro elemento osservabile.

Non viene ancora prodotto codice Java.

### P3 - Implementation

L'LLM implementa in JUnit Jupiter i 30 test case definiti in P2.

L'organizzazione del codice è libera: può utilizzare una o più classi di test,
fixture, setup condiviso, helper e utility. Questi elementi non incrementano `N`.

Il requisito è mantenere chiaramente distinguibili e tracciabili i 30 test case
`TLLM-001 ... TLLM-030`.

Non devono essere aggiunti o rimossi test case rispetto a P2.

---

## Repair policy

Dopo P3 sono consentite ulteriori interrogazioni esclusivamente per problemi
tecnici che impediscono compilazione o corretta esecuzione della suite, ad
esempio:

- errori di compilazione;
- import errati;
- firme API errate;
- setup non implementabile con il contesto fornito;
- problemi di test discovery;
- configurazioni tecniche incompatibili.

Ogni repair viene conservato come evidence (`P3-R1`, `P3-R2`, ...).

Non sono consentiti repair motivati da:
- Line Coverage;
- Branch Coverage;
- Mutation Score;
- survivor PIT;
- confronto con altre suite.

Gli oracle non vengono modificati manualmente per rendere artificialmente
verde la suite. Se un FAIL sembra rappresentare una reale discrepanza tra
comportamento e aspettativa generata, viene analizzato e documentato.

---

## Freeze

La suite può essere congelata solo dopo:

1. presenza di esattamente 30 test case/scenari tracciabili;
2. compilazione riuscita;
3. corrispondenza verificabile tra `TLLM-001 ... TLLM-030` e implementazione;
4. esecuzione completa della suite;
5. verifica di stabilità mediante esecuzioni ripetute.

Prima del freeze NON vengono eseguiti JaCoCo o PIT sulla suite LLM.

Dopo il freeze vengono registrati gli SHA-256 dei sorgenti canonici.

---

## Adequacy post-freeze

Dopo il freeze vengono calcolate:

```text
Line Coverage
Branch Coverage
```

con JaCoCo sulla sola classe esterna `PCEnhancer`.

I denominatori canonici attesi sono:

```text
Lines    : 2699
Branches : 1217
```

Le metriche non vengono utilizzate per rigenerare o modificare `T_LLM`.

---

## Mutation testing post-freeze

PIT viene eseguito sulla stessa popolazione canonica utilizzata per il confronto
delle altre suite:

```text
PIT        : 1.25.8
Mutators   : DEFAULTS
Target     : outer PCEnhancer
Threads    : 1
Population : 1700
```

La popolazione deve essere verificata anche per identità, non soltanto per
cardinalità.

Metriche:

```text
Mutation Score = KILLED / 1700
Test Strength  = KILLED / (KILLED + SURVIVED)
```

---

## Valutazione qualitativa post-freeze

Dopo il freeze la suite viene valutata anche qualitativamente rispetto a:

- funzionalità esercitate;
- rilevanza dei casi;
- eventuale ridondanza;
- chiarezza degli oracle;
- uso delle dipendenze;
- leggibilità e manutenibilità;
- eventuali comportamenti rilevanti non considerati.

Questa valutazione è successiva alla generazione e non viene usata per
modificare retroattivamente `T_LLM`.

---

## Evidence da conservare

Vengono versionati:

- protocollo;
- prompt P1/P2/P3;
- eventuali prompt di repair;
- risposte complete dell'LLM;
- manifest del contesto fornito;
- ambiente di generazione;
- progetto dei 30 casi;
- suite finale;
- validation e repeatability;
- manifest SHA-256;
- JaCoCo post-freeze;
- PIT post-freeze;
- confronto finale con le altre tecniche.

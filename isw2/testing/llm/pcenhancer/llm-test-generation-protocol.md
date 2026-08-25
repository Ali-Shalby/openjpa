# PCEnhancer - Protocollo T_LLM

## Obiettivo

Generare e valutare una suite automatica di test tramite LLM per:

`org.apache.openjpa.enhance.PCEnhancer`

sulla baseline Apache OpenJPA 4.1.1.

Il protocollo viene congelato prima dell'esecuzione dell'esperimento.

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
Model                  : GPT 5.6 Think Deeper
Model provider         : OpenAI
Copilot plan           : Copilot Chat (Basic)
```

`N = 30` indica 30 test case/scenari distinti, non necessariamente 30 metodi
Java. Fixture, helper, setup condiviso e utility non incrementano `N`.

Deve essere mantenuta la tracciabilità tra `TLLM-001 ... TLLM-030` e la loro
implementazione.

## Contesto iniziale

Prima del prompt principale vengono forniti esclusivamente:

- il sorgente production `PCEnhancer.java`, consegnato come copia byte-identica
  `PCEnhancer.java.txt` per compatibilità con l'interfaccia;
- `T_LLM-environment.txt`.

Non vengono forniti inizialmente il `pom.xml`, test preesistenti o altre
dipendenze production.

## Strategia di interrogazione

L'esperimento usa un solo prompt principale.

Il prompt richiede, nella stessa risposta:

1. analisi sintetica di `PCEnhancer` dal punto di vista del testing;
2. progettazione di esattamente 30 test case/scenari
   `TLLM-001 ... TLLM-030`;
3. implementazione degli stessi 30 casi in Java 21 con JUnit Jupiter;
4. tracciabilità fra design e implementazione.

Mockito è disponibile ma non obbligatorio.

L'organizzazione del codice è libera: possono essere usate una o più classi di
test, fixture, helper e setup condiviso.

## Contesto vietato

Non vengono forniti all'LLM:

- test nativi OpenJPA;
- `T_BB`;
- `T_CF`;
- `T_MT`;
- `T_RND`;
- `T_ES`;
- risultati JaCoCo;
- risultati PIT;
- survivor mutation;
- documentazione del progetto che riveli risultati delle suite precedenti;
- fonti Web o esterne.

## Repair policy

Sono ammessi repair esclusivamente per blocchi tecnici concreti, per esempio:

- allegato non leggibile o perso dall'interfaccia;
- errore di compilazione;
- import o firma API errati;
- dipendenza production realmente mancante;
- problema tecnico di test discovery o runtime.

I repair:

- non costituiscono nuovi prompt principali;
- devono correggere soltanto il blocco tecnico osservato;
- non possono usare feedback JaCoCo/PIT;
- non possono modificare i 30 casi per migliorarne la qualità;
- devono essere conservati come evidence.

Se è necessario production context aggiuntivo, vengono forniti esclusivamente i
file realmente richiesti nella run corrente.

Gli oracle non vengono modificati manualmente per rendere artificialmente verde
la suite. Eventuali FAIL vengono analizzati e documentati.

## Freeze

La suite viene congelata solo dopo:

1. presenza di esattamente 30 test case/scenari tracciabili;
2. compilazione riuscita;
3. corrispondenza verificabile tra `TLLM-001 ... TLLM-030` e implementazione;
4. esecuzione completa;
5. analisi di eventuali FAIL;
6. verifica di stabilità mediante esecuzioni ripetute.

Prima del freeze non vengono eseguiti JaCoCo o PIT sulla suite LLM.

Dopo il freeze vengono registrati gli SHA-256 dei sorgenti canonici.

## Adequacy post-freeze

Dopo il freeze vengono calcolate:

```text
Line Coverage
Branch Coverage
```

con JaCoCo sulla sola classe esterna `PCEnhancer`.

Denominatori canonici:

```text
Lines    : 2699
Branches : 1217
```

Le metriche non vengono usate per rigenerare o modificare la suite.

## Mutation testing post-freeze

PIT viene eseguito sulla popolazione canonica:

```text
PIT        : 1.25.8
Mutators   : DEFAULTS
Target     : outer PCEnhancer
Threads    : 1
Population : 1700
```

La popolazione deve essere verificata anche per identità.

Metriche:

```text
Mutation Score = KILLED / 1700
Test Strength  = KILLED / (KILLED + SURVIVED)
```

## Valutazione qualitativa post-freeze

Dopo il freeze vengono valutati anche:

- funzionalità esercitate;
- rilevanza dei casi;
- eventuale ridondanza;
- chiarezza degli oracle;
- uso delle dipendenze;
- leggibilità e manutenibilità;
- comportamenti rilevanti non considerati.

Questa valutazione non viene retro-propagata all'LLM.

## Evidence da conservare

Vengono conservati e versionati, quando applicabili:

- protocollo congelato;
- prompt principale;
- risposta completa dell'LLM;
- eventuali prompt di repair;
- eventuali context completion;
- manifest del contesto fornito;
- ambiente di generazione;
- design dei 30 casi;
- suite finale;
- validation e repeatability;
- manifest SHA-256;
- risultati JaCoCo post-freeze;
- risultati PIT post-freeze;
- confronto finale con le altre tecniche.

# PCEnhancer - Protocollo T_LLM

## Obiettivo

Generare e valutare una suite automatica di test tramite LLM per:

`org.apache.openjpa.enhance.PCEnhancer`

sulla baseline Apache OpenJPA 4.1.1.

Il protocollo viene congelato prima della nuova esecuzione dell'esperimento.

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

`N = 30` indica 30 test case/scenari distinti, non 30 metodi Java obbligatori.
Fixture, helper, setup condiviso e utility non costituiscono test case
aggiuntivi.

Deve essere mantenuta una tracciabilità esplicita tra i 30 test case
`TLLM-001 ... TLLM-030` e la loro implementazione/esecuzione.

## Contesto iniziale

Prima del primo prompt vengono forniti esclusivamente:

- il sorgente production `PCEnhancer.java`;
- `T_LLM-environment.txt`.

Non vengono pre-selezionate altre dipendenze production sulla base di
precedenti esecuzioni dell'esperimento.

Se durante la nuova run l'LLM dichiara che una o più dipendenze production sono
necessarie per procedere senza inventare contratti o API, tali file possono
essere aggiunti come context completion, ma soltanto dopo una richiesta
esplicita dell'LLM nella run corrente.

Ogni context completion deve essere registrato come evidence.

## Strategia di interrogazione

L'esperimento usa due prompt principali consecutivi.

### P1 - Analysis + Test Design

Il primo prompt richiede di:

1. analizzare `PCEnhancer` dal punto di vista del testing;
2. progettare esattamente 30 test case/scenari;
3. identificarli come `TLLM-001 ... TLLM-030`;
4. descrivere per ogni caso setup/input, azione e risultato atteso;
5. non generare ancora codice Java.

Se il contesto production non è sufficiente per completare in modo affidabile
l'analisi e il progetto dei 30 casi, l'LLM deve fermarsi e indicare soltanto i
file production realmente necessari.

Dopo un eventuale context completion tecnico, P1 viene ripreso senza cambiare
obiettivo o vincoli.

### P2 - Implementation

Il secondo prompt richiede di:

1. implementare i 30 casi definiti in P1;
2. usare JUnit Jupiter;
3. mantenere la tracciabilità `TLLM-001 ... TLLM-030`;
4. non aggiungere, eliminare o sostituire casi;
5. restituire codice completo e mapping tra ID e implementazione.

L'organizzazione del codice è libera: possono essere usate una o più classi di
test, fixture, helper e setup condiviso.

Se per implementare uno o più casi manca production context, l'LLM deve
fermarsi e indicare soltanto i file production realmente necessari.

Non sono previste ulteriori interrogazioni principali.

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
- fonti Web o esterne usate per colmare lacune.

## Principio di neutralità

I prompt controllano contesto, cardinalità e tracciabilità, ma non prescrivono
a priori la qualità della suite.

Non viene chiesto di:

- massimizzare coverage;
- massimizzare mutation score;
- produrre test "forti", "ottimali" o "non banali";
- privilegiare boundary case;
- evitare ridondanza;
- utilizzare obbligatoriamente Mockito;
- imitare suite precedenti.

Rilevanza, significatività, ridondanza e qualità degli oracle vengono valutate
a posteriori.

## Repair policy

Sono ammessi repair esclusivamente per blocchi tecnici concreti, per esempio:

- allegato non leggibile;
- perdita di contesto documentata;
- errore di compilazione;
- import o firma API errati;
- dipendenza production realmente mancante;
- problema tecnico di test discovery o runtime.

I repair:

- non costituiscono nuovi prompt principali;
- devono correggere soltanto il blocco tecnico osservato;
- non possono usare feedback JaCoCo/PIT;
- non possono modificare i 30 casi per migliorarne la qualità;
- devono essere conservati integralmente come evidence.

Gli oracle non vengono modificati manualmente per rendere artificialmente
verde la suite. Se un FAIL sembra rappresentare una reale discrepanza tra
comportamento osservato e aspettativa generata dall'LLM, viene analizzato e
documentato.

Se si crea una catena ripetuta di repair dovuta alla perdita del contesto
dell'interfaccia, la run viene interrotta e riavviata dal protocollo congelato
invece di proseguire indefinitamente.

## Freeze

La suite viene congelata solo dopo:

1. presenza di esattamente 30 test case/scenari tracciabili;
2. compilazione riuscita;
3. corrispondenza verificabile tra `TLLM-001 ... TLLM-030` e implementazione;
4. esecuzione completa della suite;
5. analisi di eventuali FAIL;
6. verifica di stabilità mediante esecuzioni ripetute.

Prima del freeze non vengono eseguiti JaCoCo o PIT sulla suite LLM.

Dopo il freeze vengono registrati gli SHA-256 dei sorgenti canonici della suite.

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

La popolazione deve essere verificata anche per identità, non soltanto per
cardinalità.

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
- prompt principali P1 e P2;
- eventuali context completion;
- eventuali prompt di repair;
- risposte complete dell'LLM;
- manifest del contesto fornito;
- ambiente di generazione;
- progetto dei 30 test case;
- suite finale;
- validation e repeatability;
- manifest SHA-256 della suite congelata;
- risultati JaCoCo post-freeze;
- risultati PIT post-freeze;
- confronto finale con le altre tecniche.

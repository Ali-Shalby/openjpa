# ListIteratorWrapper – LLM Test Generation Protocol

## 1. Obiettivo

Generare una suite automatica di test tramite LLM per la classe:

```text
org.apache.openjpa.lib.util.collections.ListIteratorWrapper
```

La suite viene indicata come:

```text
T_LLM
```

L'obiettivo è ottenere una suite indipendente dalle altre tecniche automatiche già utilizzate e confrontabile a parità di cardinalità.

## 2. Cardinalità

La cardinalità della suite viene fissata prima della generazione a:

```text
N = 12
```

Il valore deriva dalla cardinalità della suite black-box iniziale `T_BB` della stessa classe.

La suite LLM deve quindi contenere esattamente 12 test case/scenari distinti:

```text
TLLM-001
...
TLLM-012
```

Fixture, helper, setup condiviso e utility non vengono conteggiati come test case aggiuntivi.

## 3. Ambiente

Configurazione prevista:

```text
Runtime        : Java 21
Framework      : JUnit Jupiter
LLM client     : Microsoft Copilot Web
Interaction    : browser chat
```

Mockito può essere utilizzato solo se realmente necessario per costruire test validi; non è un requisito della suite.

## 4. Contesto fornito all'LLM

L'LLM deve lavorare esclusivamente sul contesto fornito per l'esperimento.

Il contesto iniziale comprende il sorgente production della classe target:

```text
ListIteratorWrapper.java
```

e le sole informazioni tecniche necessarie per produrre test compatibili con l'harness.

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
- confronti con suite precedenti;
- documentazione che riveli le metriche ottenute dalle altre tecniche.

## 5. Strategia di generazione

La generazione utilizza un singolo prompt principale.

Il prompt deve richiedere nella stessa risposta:

1. una breve analisi della classe dal punto di vista del testing;
2. la progettazione di esattamente 12 scenari distinti;
3. la loro implementazione in Java 21 con JUnit Jupiter;
4. la tracciabilità esplicita tra `TLLM-001 ... TLLM-012` e i test implementati.

La progettazione e l'implementazione devono riferirsi agli stessi 12 casi.

Non devono essere aggiunti, rimossi o sostituiti scenari tra la parte di progettazione e il codice prodotto.

## 6. Vincoli

La generazione deve rispettare i seguenti vincoli:

```text
Target cardinality     : 12
Runtime                : Java 21
Framework              : JUnit Jupiter
Native OpenJPA tests   : NOT USED
Coverage feedback      : NONE
Mutation feedback      : NONE
Previous suites        : NOT PROVIDED
Post-adequacy editing  : NONE
```

I test devono essere:

- indipendenti;
- deterministici;
- eseguibili senza rete;
- privi di dipendenze temporali non controllate;
- basati su comportamenti osservabili della classe target.

È consentito utilizzare classi standard Java per costruire gli input necessari.

## 7. Neutralità sperimentale

Il prompt non deve richiedere esplicitamente di:

- massimizzare la Line Coverage;
- massimizzare la Branch Coverage;
- massimizzare il Mutation Score;
- massimizzare il Test Strength;
- imitare Randoop;
- imitare EvoSuite;
- replicare test manuali già esistenti.

La qualità della suite viene osservata solo dopo il freeze.

## 8. Repair tecnico

Se l'output iniziale presenta un problema tecnico concreto, sono ammessi repair limitati a:

- errori di compilazione;
- import mancanti o errati;
- firme API non corrette;
- incompatibilità tecniche con Java 21 o JUnit Jupiter;
- output troncato o incompleto.

I repair non possono essere guidati da JaCoCo o PIT.

Non possono essere usati per sostituire test deboli con test migliori dopo aver osservato le metriche.

## 9. Validazione pre-freeze

Prima del freeze devono essere verificati:

1. presenza di esattamente 12 test case/scenari;
2. tracciabilità `TLLM-001 ... TLLM-012`;
3. compilazione corretta;
4. esecuzione completa della suite;
5. assenza di failure non spiegate;
6. stabilità della suite mediante esecuzioni ripetute.

JaCoCo e PIT non devono essere eseguiti prima del freeze.

## 10. Freeze

Quando la suite soddisfa i requisiti di validazione, viene congelata.

Da quel momento:

- i test non vengono più modificati;
- gli oracle non vengono più modificati;
- non vengono aggiunti nuovi casi;
- non vengono eliminati casi;
- gli SHA-256 dei sorgenti canonici vengono registrati.

Lo stato diventa:

```text
T_LLM STATUS : FROZEN
```

## 11. Coverage post-freeze

Dopo il freeze viene misurata con JaCoCo la coverage della sola classe:

```text
org.apache.openjpa.lib.util.collections.ListIteratorWrapper
```

Metriche:

```text
Line Coverage
Branch Coverage
Method Coverage
```

I risultati non vengono utilizzati come feedback per modificare la suite.

## 12. Mutation testing post-freeze

Dopo il freeze viene eseguito PIT sulla stessa configurazione sperimentale utilizzata per le altre suite della classe.

Configurazione:

```text
PIT        : 1.25.8
Mutators   : DEFAULTS
Threads    : 1
Target     : ListIteratorWrapper
Population : 52
```

La popolazione mutante deve essere verificata per identità rispetto alla popolazione di riferimento.

Metriche:

```text
Mutation Score = KILLED / TOTAL

Test Strength =
KILLED / (KILLED + SURVIVED)
```

## 13. Valutazione finale

Dopo il freeze e le misure di adequacy, `T_LLM` viene confrontata con le altre suite automatiche della stessa classe mantenendo:

```text
N = 12
```

Il confronto considera almeno:

- Line Coverage;
- Branch Coverage;
- Method Coverage;
- Mutation Score;
- Test Strength;
- chiarezza degli oracle;
- leggibilità;
- eventuale ridondanza;
- varietà dei comportamenti esercitati.

La valutazione è esclusivamente post-hoc e non modifica la suite congelata.

# PCEnhancer T_LLM - Context Manifest

Questo manifest descrive il contesto production reso disponibile all'LLM prima
e durante la generazione di `T_LLM`.

## Baseline

```text
Project              : Apache OpenJPA
Release              : 4.1.1
Target               : org.apache.openjpa.enhance.PCEnhancer
Suite                : T_LLM
N                    : 30 test case/scenari distinti
LLM client           : Microsoft Copilot
Interaction mode     : browser chat
Model                : GPT 5.6 Think Deeper
Model provider       : OpenAI
Copilot plan         : Copilot Chat (Basic)
Repository branch    : isw2-project
Repository HEAD      : fa2cf51186b00cd833338b20bdf2a2d3709ece75
```

`Repository HEAD` identifica lo stato del progetto precedente al commit di
preregistrazione `T_LLM`.

## Pre-generation environment refinement

Dopo il commit iniziale di preregistrazione, ma prima del prompt principale,
l'accesso a Microsoft Copilot tramite l'account istituzionale ha esposto
esplicitamente il modello selezionabile. L'ambiente è stato quindi raffinato
pre-P1 registrando `GPT 5.6 Think Deeper` come modello effettivamente scelto.

Nessun prompt dell'esperimento era stato ancora inviato e il production context
non è stato modificato.

## Contesto iniziale autorizzato

Prima del prompt principale l'LLM riceve soltanto:

```text
openjpa-kernel/src/main/java/org/apache/openjpa/enhance/PCEnhancer.java
(consegnato all'interfaccia come copia byte-identica `PCEnhancer.java.txt`)
T_LLM-environment.txt
```

## Contesto esplicitamente escluso

Non vengono resi disponibili all'LLM:

- test nativi OpenJPA;
- sorgenti `T_BB`;
- sorgenti `T_CF`;
- sorgenti `T_MT`;
- sorgenti `T_RND`;
- sorgenti `T_ES`;
- risultati JaCoCo;
- risultati PIT;
- survivor mutation;
- documentazione del progetto contenente risultati delle suite precedenti;
- ulteriori fonti esterne fornite per colmare informazioni mancanti.

## Context completion

Eventuali classi production aggiuntive possono essere aggiunte soltanto se
richieste esplicitamente dall'LLM durante la run corrente perché necessarie alla
comprensione o implementazione.

Per ciascuna aggiunta registrare:

| Step | File production aggiunto | Motivo dichiarato dall'LLM | SHA-256 |
|---|---|---|---|
| - | - | - | - |

## Fingerprint del contesto iniziale

```text
PCEnhancer.java SHA-256 : F7D85F9663B68430521FBEDE824C273971BAEF790A3C5291A9F8E0B7BC64AC7A
Environment SHA-256     : 250C6EF4401AEABCF50B3E37C33DA4B12467E739FF21C69F214AE7CAA77E13A7
```

La copia di `PCEnhancer.java` usata nel contesto LLM è stata verificata
byte-identica al sorgente presente nel repository:

```text
Repository/Context match : True
```

## Freeze del contesto

Il contesto iniziale è congelato prima del prompt principale.

Il manifest può essere aggiornato successivamente soltanto per registrare
context completion leciti e richiesti esplicitamente dall'LLM. Non devono
essere aggiunti file derivati da coverage, mutation analysis o suite
sperimentali precedenti.

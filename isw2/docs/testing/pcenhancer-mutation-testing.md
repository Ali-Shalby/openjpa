# PCEnhancer – mutation testing e suite mutation-guided `T_MT`

## Scopo

Questo documento descrive la fase di **mutation analysis** e la successiva
evoluzione manuale **mutation-guided** per:

```text
org.apache.openjpa.enhance.PCEnhancer
```

Baseline production:

```text
Apache OpenJPA 4.1.1
Tag Git: 4.1.1
Baseline sperimentale: C0
```

Stato conclusivo:

```text
T_BB                    : FROZEN, 30 test
T_CF                    : FROZEN, 5 test
T_MT additions          : FROZEN, 5 test
Suite manuale cumulativa: 40 test
Full regression clean   : 40 PASS, 0 FAIL
Mutation population     : 1700
KILLED                  : 828
SURVIVED                : 355
NO_COVERAGE             : 515
TIMED_OUT               : 2
Mutation Score          : 48.71%
Test Strength           : 69.99%
T_MT STATUS             : FROZEN
```

`T_MT` non modifica retroattivamente né la Category Partition `T_BB` né la
suite coverage-guided `T_CF`.

---

## 1. Posizione metodologica di `T_MT`

La fase viene trattata come un processo iterativo:

```text
freeze T_BB
        ↓
freeze T_CF
        ↓
mutation baseline
        ↓
survivor audit
        ↓
selezione di un gap comportamentale osservabile
        ↓
implementazione del T_MT
        ↓
PIT cumulativo sulla stessa popolazione
        ↓
misura del delta
        ↓
KEEP se aggiunge fault-detection ability
        ↓
nuovo survivor audit oppure STOP
```

Durante la progettazione possono essere usati preflight o diagnostic per
validare fixture e oracle. Queste esecuzioni sono supporto tecnico e non stage
sperimentali autonomi.

L'obiettivo non è azzerare i survivor né raggiungere una soglia numerica
arbitraria.

---

## 2. Protocollo mutation testing

```text
PIT                     : 1.25.8
PIT JUnit 5 plugin      : 1.2.3
Integration             : pitest-maven
Mutator set             : DEFAULTS
Threads                 : 1
Target                  : org.apache.openjpa.enhance.PCEnhancer
Target scope            : sola classe esterna PCEnhancer
Class limit             : NONE
Mutation population     : 1700
```

La classe production viene estratta dall'artefatto Maven:

```text
org.apache.openjpa:openjpa-kernel:4.1.1
```

Identità del bytecode:

```text
PCEnhancer.class size : 110190 bytes
SHA-256               : 3C825DF257CC2FCF6550448E177A602495600A0470B742457AFC46BF4D788911
```

La popolazione rimane invariata in tutte le misurazioni.

### Test nativi OpenJPA

I test già presenti nel repository OpenJPA non vengono utilizzati come suite
sperimentale.

### TBB-026

`TBB-026` fa parte della suite black-box congelata e verifica:

```text
RuntimeUnenhancedClasses = definitely-invalid
```

Oracle:

```text
ParseException
```

Il test è verde e partecipa normalmente alle run PIT.

---

## 3. Metriche adottate

```text
Mutation Score = KILLED / TOTAL MUTANTS
Test Strength  = KILLED / (KILLED + SURVIVED)
```

`NO_COVERAGE` e `TIMED_OUT` vengono riportati separatamente.

Nel run finale la console PIT può sintetizzare:

```text
Generated 1700 mutations Killed 830
```

Gli status raw dell'XML distinguono:

```text
KILLED    : 828
TIMED_OUT : 2
```

Le metriche canoniche del progetto usano gli status raw:

```text
Mutation Score = 828 / 1700 = 48.71%
Test Strength  = 828 / (828 + 355) = 69.99%
```

I timeout non vengono riclassificati manualmente come `KILLED`.

---

## 4. Mutation baseline

Suite:

```text
T_BB              : 30
T_CF              : 5
Manual suite      : 35
PASS              : 35
FAIL              : 0
PIT execution set : 35 green tests
```

Risultato:

| Stato | Mutanti |
|---|---:|
| KILLED | 466 |
| SURVIVED | 714 |
| NO_COVERAGE | 520 |
| TIMED_OUT | 0 |
| **Totale** | **1700** |

```text
Mutation Score : 27.41%
Test Strength  : 39.49%
```

Evidence:

```text
isw2/results/testing/pcenhancer/mutation/baseline/
```

---

## 5. Regola di selezione dei `T_MT`

Un nuovo test viene ammesso quando:

1. il survivor audit evidenzia un comportamento production riconoscibile;
2. il comportamento è osservabile tramite un oracle stabile;
3. il test aggiunge informazione rispetto alle suite già congelate;
4. il PIT cumulativo mostra un incremento misurabile dei `KILLED`;
5. il costo del nuovo test resta proporzionato al beneficio.

I diagnostic intermedi sono supporto tecnico alla progettazione.

---

## 6. Inventory finale `T_MT`

| ID | Scenario | Test class |
|---|---|---|
| TMT-001 | Application Identity runtime object-id semantics | `PCEnhancerMutationIdentityTest` |
| TMT-002 | Detached-state Externalization runtime round-trip | `PCEnhancerMutationExternalizationTest` |
| TMT-003 | Standard Java Serialization runtime round-trip | `PCEnhancerMutationSerializationTest` |
| TMT-004 | PersistenceCapable / StateManager runtime field semantics | `PCEnhancerMutationStateManagerTest` |
| TMT-005 | Relationship-valued / derived identity runtime semantics | `PCEnhancerMutationDerivedIdentityTest` |

---

## 7. TMT-001 – Application Identity runtime object-id semantics

### WHY

Il survivor audit mostra un cluster rilevante nei generatori del supporto
Application Identity:

```text
addCopyKeyFieldsToObjectIdMethod
addCopyKeyFieldsFromObjectIdMethod
addNewObjectIdInstanceMethod
```

`TCF-001` verifica la generazione dei metodi, ma non il trasferimento runtime
dei valori.

### HOW

Il test esercita:

```text
pcCopyKeyFieldsToObjectId
pcCopyKeyFieldsFromObjectId
pcNewObjectIdInstance
```

con primary key distinguibili e oracle sui valori trasferiti.

### RESULT

```text
KILLED        : 633
SURVIVED      : 551
NO_COVERAGE   : 515
TIMED_OUT     : 1
Mutation Score: 37.24%
Test Strength : 53.46%
Δ KILLED      : +167
```

Decisione: `KEEP`.

---

## 8. TMT-002 – Detached-state Externalization runtime round-trip

### WHY

Il survivor audit successivo evidenzia un gap distinto nei percorsi
Externalization (`addWriteExternal`, `addReadExternal`, `writeExternal`,
`readExternal` e metodi collegati).

### HOW

Il test enhance una fixture detached-state ed esegue un round-trip reale
`writeExternal` / `readExternal`, verificando valori persistent e detached
state.

### RESULT

```text
KILLED        : 719
SURVIVED      : 465
NO_COVERAGE   : 515
TIMED_OUT     : 1
Mutation Score: 42.29%
Test Strength : 60.73%
Δ KILLED      : +86
```

Decisione: `KEEP`.

---

## 9. TMT-003 – Standard Java Serialization runtime round-trip

### WHY

Rimangono survivor nei generatori:

```text
modifyWriteObjectMethod
modifyReadObjectMethod
```

`TCF-003` verifica strutturalmente il supporto serialization ma non un vero
round-trip runtime.

### HOW

La fixture `Serializable` viene enhanced, serializzata con
`ObjectOutputStream`, deserializzata con `ObjectInputStream` e verificata sui
valori:

```text
id      = 17
counter = 314159
label   = SER-A
```

### RESULT

Il test è mantenuto nella suite finale `T_MT`. Il risultato quantitativo
canonico viene riportato nel checkpoint cumulativo finale, evitando di
attribuire a questo singolo passaggio una misura intermedia non necessaria al
riepilogo finale.

Decisione: `KEEP`.

---

## 10. TMT-004 – PersistenceCapable / StateManager runtime semantics

### WHY

Il survivor audit evidenzia mutanti coperti nei generatori del protocollo
`PersistenceCapable`, indicando un gap di oracle più che di reachability.

### HOW

Il test verifica a runtime:

```text
pcGetManagedFieldCount
pcProvideFields
pcReplaceFields
pcCopyFields
pcNewInstance
pcReplaceStateManager / pcGetStateManager
```

con valori distinguibili e `StateManager` controllato.

### RESULT

Il test è mantenuto nella suite finale `T_MT`. La sua efficacia confluisce nel
checkpoint cumulativo finale.

Decisione: `KEEP`.

---

## 11. TMT-005 – Relationship-valued / derived identity runtime semantics

### WHY

Il survivor audit finale evidenzia un gap distinto nei percorsi di identity
relazionale/derivata, inclusi i generatori di copia dell'object-id.

### HOW

Il test riusa la fixture controllata di `TCF-004` e verifica a runtime:

```text
parent object-id type : org.apache.openjpa.util.LongId
target object-id type : org.apache.openjpa.util.ObjectId
actual IdClass        : RelationshipIdentityId
parent id             : 424242
sequence id           : 777
FieldConsumer values  : [424242, 777]
```

### RESULT

Il test completa la suite finale `T_MT`. L'effetto quantitativo complessivo
viene misurato nel checkpoint cumulativo finale.

Decisione: `KEEP`.

---

## 12. Evoluzione complessiva

I checkpoint canonici disponibili sono:

| Stage | KILLED | SURVIVED | NO_COVERAGE | TIMED_OUT | Mutation Score | Test Strength | Δ KILLED |
|---|---:|---:|---:|---:|---:|---:|---:|
| Baseline | 466 | 714 | 520 | 0 | 27.41% | 39.49% | – |
| Post TMT-001 | 633 | 551 | 515 | 1 | 37.24% | 53.46% | +167 |
| Post TMT-002 | 719 | 465 | 515 | 1 | 42.29% | 60.73% | +86 |
| Final TMT-001..005 | 828 | 355 | 515 | 2 | 48.71% | 69.99% | +109 vs TMT-002 |

Miglioramento complessivo rispetto alla baseline:

```text
Additional KILLED      : +362
SURVIVED reduction     : -359
NO_COVERAGE reduction  : -5
TIMED_OUT delta        : +2
Mutation Score delta   : +21.30 pp
Test Strength delta    : +30.50 pp
```

---

## 13. Raw status e timeout

Nel risultato finale vengono conservati due `TIMED_OUT` raw:

```text
addMultipleFieldsMethodVersion
replaceAndValidateFieldAccess
```

Per `replaceAndValidateFieldAccess` il timeout è associato a un mutante
`VoidMethodCallMutator` che rimuove `InsnList::remove`.

I timeout non vengono riclassificati manualmente.

I survivor residui vengono conservati come evidence e non vengono
automaticamente classificati come equivalenti.

---

## 14. Final PIT

```text
Population      : 1700
KILLED          : 828
SURVIVED        : 355
NO_COVERAGE     : 515
TIMED_OUT       : 2
RUN_ERROR       : 0
MEMORY_ERROR    : 0
Mutation Score  : 48.71%
Test Strength   : 69.99%
```

Il run finale PIT completa con `BUILD SUCCESS`.

Evidence:

```text
isw2/results/testing/pcenhancer/mutation/final/
```

---

## 15. Regressione manuale definitiva

Dopo PIT viene eseguita una clean regression della suite manuale completa.

Risultato:

```text
Tests run : 40
PASS      : 40
FAIL      : 0
Errors    : 0
Skipped   : 0
BUILD SUCCESS
```

La clean execution conferma che lo staging temporaneo usato per PIT non altera
lo stato finale della suite.

---

## 16. Stopping rule finale

Il freeze viene effettuato dopo `TMT-005` perché:

1. sono stati affrontati cinque comportamenti runtime distinti;
2. i test mantenuti aggiungono fault-detection ability alla suite manuale;
3. la popolazione resta costante a 1700;
4. la Test Strength passa da 39.49% a 69.99%;
5. i survivor residui sono distribuiti su numerosi dettagli interni e non
   giustificano da soli nuovi test costruiti soltanto per aumentare lo score.

```text
TMT-006 planned            : NO
T_MT stopping rule reached : YES
T_MT STATUS                : FROZEN
```

---

## 17. Evidence versionate

Struttura canonica:

```text
isw2/results/testing/pcenhancer/mutation/
├── preflight/
├── baseline/
├── tmt001/
├── tmt002/
├── tmt003/
├── tmt004/
├── tmt005/
└── final/
```

I checkpoint quantitativi riportati nel documento sono quelli mantenuti come
risultati canonici. Diagnostic, feasibility audit e dump tecnici vengono
conservati quando utili alla tracciabilità ma non vengono confusi con i
risultati principali.

---

## 18. Freeze finale

```text
T_BB : 30, FROZEN
T_CF : 5, FROZEN
T_MT : 5, FROZEN
Total: 40
PASS : 40
FAIL : 0
```

Mutation testing:

```text
Population      : 1700
KILLED          : 828
SURVIVED        : 355
NO_COVERAGE     : 515
TIMED_OUT       : 2
Mutation Score  : 48.71%
Test Strength   : 69.99%
```

Da questo punto `T_BB`, `T_CF` e `T_MT` non vengono più modificati per inseguire
coverage o survivor residui.

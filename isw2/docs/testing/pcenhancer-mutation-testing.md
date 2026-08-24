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
Full regression clean   : 39 PASS, 1 FAIL noto (TBB-026)
Mutation population     : 1700
KILLED                  : 827
SURVIVED                : 355
NO_COVERAGE             : 516
TIMED_OUT               : 2
Mutation Score          : 48.65%
Test Strength           : 69.97%
T_MT STATUS             : FROZEN
```

`T_MT` non modifica retroattivamente né la Category Partition `T_BB` né la
suite coverage-guided `T_CF`.

---

## 1. Posizione metodologica di `T_MT`

La fase viene trattata come un **processo iterativo unico**:

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
validare fixture e oracle. Queste esecuzioni sono **supporto tecnico**, non
stage sperimentali autonomi. La catena canonica dei risultati è costituita
dalla baseline e dai PIT cumulativi dopo `TMT-001..TMT-005`.

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

La classe production utilizzata durante l'esperimento viene estratta
dall'artefatto Maven:

```text
org.apache.openjpa:openjpa-kernel:4.1.1
```

Identità del bytecode congelato:

```text
PCEnhancer.class size : 110190 bytes
SHA-256               : 3C825DF257CC2FCF6550448E177A602495600A0470B742457AFC46BF4D788911
```

La popolazione rimane invariata in tutte le misurazioni.

### Test nativi OpenJPA

I test già presenti nel repository OpenJPA non vengono utilizzati come suite
sperimentale.

### TBB-026

`TBB-026` resta parte della suite manuale congelata e il suo oracle non viene
modificato. È escluso soltanto dall'esecuzione PIT, perché la baseline
non-mutata richiesta da PIT deve essere verde.

---

## 3. Metriche adottate

```text
Mutation Score = KILLED / TOTAL MUTANTS
Test Strength  = KILLED / (KILLED + SURVIVED)
```

`NO_COVERAGE` e `TIMED_OUT` vengono riportati separatamente.

Nel run finale la console PIT sintetizza:

```text
Generated 1700 mutations Killed 829
```

mentre l'XML raw distingue:

```text
KILLED    : 827
TIMED_OUT : 2
```

Per le metriche del progetto vengono usati gli status raw dell'XML:

```text
Mutation Score = 827 / 1700 = 48.65%
Test Strength  = 827 / (827 + 355) = 69.97%
```

---

## 4. Mutation baseline

Suite:

```text
T_BB              : 30
T_CF              : 5
Manual suite      : 35
Known failure     : TBB-026
PIT execution set : 34 green tests
```

Risultato:

| Stato | Mutanti |
|---|---:|
| KILLED | 465 |
| SURVIVED | 714 |
| NO_COVERAGE | 521 |
| TIMED_OUT | 0 |
| **Totale** | **1700** |

```text
Mutation Score : 27.35%
Test Strength  : 39.44%
```

Il survivor audit della baseline viene conservato in:

```text
isw2/results/testing/pcenhancer/mutation/baseline/
pcenhancer_mutation_gap_audit.txt
```

---

## 5. Regola di selezione dei `T_MT`

Un nuovo test viene ammesso quando:

1. il survivor audit evidenzia un comportamento production riconoscibile;
2. il comportamento è osservabile tramite un oracle stabile;
3. il test aggiunge informazione rispetto alle suite già congelate;
4. il PIT cumulativo mostra un incremento misurabile dei `KILLED`;
5. il costo del nuovo test resta proporzionato al beneficio.

I diagnostic intermedi non vengono versionati come risultati canonici.

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

La baseline mostrava un cluster rilevante nei generatori del supporto
Application Identity:

```text
addCopyKeyFieldsToObjectIdMethod   : 74 survivor
addCopyKeyFieldsFromObjectIdMethod : 61 survivor
addNewObjectIdInstanceMethod       : 33 survivor
```

`TCF-001` verificava la generazione dei metodi, ma non il trasferimento runtime
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
KILLED        : 632
SURVIVED      : 551
NO_COVERAGE   : 516
TIMED_OUT     : 1
Mutation Score: 37.18%
Test Strength : 53.42%
Δ KILLED      : +167
```

Decisione: `KEEP`.

---

## 8. TMT-002 – Detached-state Externalization runtime round-trip

### WHY

Il survivor audit successivo evidenziava un gap distinto nei percorsi
Externalization (`addWriteExternal`, `addReadExternal`, `writeExternal`,
`readExternal` e metodi collegati).

### HOW

Il test enhance una fixture detached-state ed esegue un round-trip reale
`writeExternal` / `readExternal`, verificando valori persistent e detached
state.

### RESULT

```text
KILLED        : 718
SURVIVED      : 465
NO_COVERAGE   : 516
TIMED_OUT     : 1
Mutation Score: 42.24%
Test Strength : 60.69%
Δ KILLED      : +86
```

Decisione: `KEEP`.

---

## 9. TMT-003 – Standard Java Serialization runtime round-trip

### WHY

Rimanevano survivor nei generatori:

```text
modifyWriteObjectMethod : 19
modifyReadObjectMethod  : 6
```

`TCF-003` verificava strutturalmente il supporto serialization ma non un vero
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

```text
KILLED        : 745
SURVIVED      : 438
NO_COVERAGE   : 516
TIMED_OUT     : 1
Mutation Score: 43.82%
Test Strength : 62.98%
Δ KILLED      : +27
```

Decisione: `KEEP`.

---

## 10. TMT-004 – PersistenceCapable / StateManager runtime semantics

### WHY

Il survivor audit mostrava ancora mutanti coperti nei generatori del protocollo
`PersistenceCapable`, quindi un gap di oracle più che di reachability.

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

```text
KILLED        : 773
SURVIVED      : 409
NO_COVERAGE   : 516
TIMED_OUT     : 2
Mutation Score: 45.47%
Test Strength : 65.40%
Δ KILLED      : +28
```

Decisione: `KEEP`.

---

## 11. TMT-005 – Relationship-valued / derived identity runtime semantics

### WHY

Il survivor audit finale evidenziava ancora un gap distinto nei percorsi di
identity relazionale/derivata, inclusi i generatori di copia dell'object-id.

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

```text
KILLED        : 827
SURVIVED      : 355
NO_COVERAGE   : 516
TIMED_OUT     : 2
Mutation Score: 48.65%
Test Strength : 69.97%
Δ KILLED      : +54
```

Decisione: `KEEP`.

---

## 12. Evoluzione complessiva

| Stage | KILLED | SURVIVED | NO_COVERAGE | TIMED_OUT | Mutation Score | Test Strength | Δ KILLED |
|---|---:|---:|---:|---:|---:|---:|---:|
| Baseline | 465 | 714 | 521 | 0 | 27.35% | 39.44% | – |
| Post TMT-001 | 632 | 551 | 516 | 1 | 37.18% | 53.42% | +167 |
| Post TMT-002 | 718 | 465 | 516 | 1 | 42.24% | 60.69% | +86 |
| Post TMT-003 | 745 | 438 | 516 | 1 | 43.82% | 62.98% | +27 |
| Post TMT-004 | 773 | 409 | 516 | 2 | 45.47% | 65.40% | +28 |
| Post TMT-005 | 827 | 355 | 516 | 2 | 48.65% | 69.97% | +54 |

Miglioramento complessivo:

```text
Additional KILLED      : +362
SURVIVED reduction     : -359
Mutation Score delta   : +21.30 pp
Test Strength delta    : +30.53 pp
```

---

## 13. Raw status e timeout

Nel risultato finale vengono conservati due `TIMED_OUT` raw:

```text
addMultipleFieldsMethodVersion
replaceAndValidateFieldAccess
```

Non vengono riclassificati manualmente.

I survivor residui vengono conservati come evidence e non vengono
automaticamente classificati come equivalenti.

---

## 14. Final PIT

```text
Population      : 1700
KILLED          : 827
SURVIVED        : 355
NO_COVERAGE     : 516
TIMED_OUT       : 2
Mutation Score  : 48.65%
Test Strength   : 69.97%
```

Il run finale PIT completa con `BUILD SUCCESS`.

---

## 15. Regressione manuale definitiva

La regressione canonica viene eseguita da workspace pulito:

```text
mvn -f isw2/testing/pom.xml clean -Dtest=**/PCEnhancer*Test test
```

Risultato:

```text
Tests run : 40
PASS      : 39
FAIL      : 1
Errors    : 0
Skipped   : 0
```

La sola failure è `TBB-026`, già congelata nella fase black-box.

---

## 16. Stopping rule finale

Il freeze viene effettuato dopo `TMT-005` perché:

1. sono stati affrontati cinque comportamenti runtime distinti;
2. tutti i TMT mantenuti producono un incremento misurabile dei `KILLED`;
3. la popolazione resta costante a 1700;
4. la Test Strength passa da 39.44% a 69.97%;
5. i survivor residui sono distribuiti su numerosi dettagli interni e non
   giustificano da soli nuovi test costruiti soltanto per aumentare il score.

```text
TMT-006 planned            : NO
T_MT stopping rule reached : YES
T_MT STATUS                : FROZEN
```

---

## 17. Evidence versionate

La cartella mutation viene mantenuta volutamente compatta e omogenea:

```text
isw2/results/testing/pcenhancer/mutation/
├── preflight/
│   └── pcenhancer_mutation_preflight.txt
├── baseline/
│   ├── pcenhancer_mutation_baseline_summary.txt
│   ├── pcenhancer_mutation_gap_audit.txt
│   └── pcenhancer_mutations.xml
├── tmt001/
│   ├── pcenhancer_tmt001_summary.txt
│   └── pcenhancer_tmt001_mutations.xml
├── tmt002/
│   ├── pcenhancer_tmt002_summary.txt
│   └── pcenhancer_tmt002_mutations.xml
├── tmt003/
│   ├── pcenhancer_tmt003_summary.txt
│   └── pcenhancer_tmt003_mutations.xml
├── tmt004/
│   ├── pcenhancer_tmt004_summary.txt
│   └── pcenhancer_tmt004_mutations.xml
├── tmt005/
│   ├── pcenhancer_tmt005_summary.txt
│   └── pcenhancer_tmt005_mutations.xml
└── final/
    ├── pcenhancer_final_manual_regression_clean.txt
    ├── pcenhancer_final_pit_run.txt
    ├── pcenhancer_final_summary.txt
    ├── pcenhancer_final_survivor_audit.txt
    └── pcenhancer_mutation_evolution.csv
```

I file di candidate feasibility, gate intermedi, transition dump, CSV derivati e
run PIT intermedi molto verbosi sono artefatti di lavoro rigenerabili e non
vengono versionati nella struttura finale.

---

## 18. Freeze finale

```text
T_BB : 30, FROZEN
T_CF : 5, FROZEN
T_MT : 5, FROZEN
Total: 40
PASS : 39
FAIL : 1 noto (TBB-026)
```

Da questo punto `T_BB`, `T_CF` e `T_MT` non vengono più modificati per inseguire
coverage o survivor residui.

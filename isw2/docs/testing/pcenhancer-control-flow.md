# PCEnhancer – suite manuale control-flow `T_CF`

## Scopo

Questo documento descrive la fase di **adequacy improvement manuale guidata
dalla coverage** per:

```text
org.apache.openjpa.enhance.PCEnhancer
```

Baseline production:

```text
Apache OpenJPA 4.1.1
Tag Git: 4.1.1
Baseline sperimentale: C0
```

Stato:

```text
T_BB            : FROZEN, 30 test
T_CF additions  : FROZEN, 5 test
Suite cumulativa: 35 test
PASS             : 35
FAIL             : 0
LINE finale      : 70.47%
BRANCH finale    : 54.89%
METHOD finale    : 86.50%
T_CF STATUS      : FROZEN
```

`T_CF` non sostituisce e non modifica retroattivamente `T_BB`. La suite
black-box iniziale resta quella derivata tramite Category Partition; i test
control-flow sono aggiunte manuali successive selezionate dopo la misura
dell'adeguatezza della suite congelata.

---

## 1. Posizione metodologica di T_CF

La sequenza adottata è:

```text
Category Partition
        ↓
freeze T_BB
        ↓
full execution T_BB
        ↓
Line / Branch Coverage baseline
        ↓
formal coverage-gap audit
        ↓
manual source/reachability analysis
        ↓
selezione di uno scenario coerente
        ↓
feasibility preflight quando necessario
        ↓
implementazione TCF
        ↓
standalone run
        ↓
cumulative coverage measurement
        ↓
nuovo gap audit
        ↓
stopping rule
        ↓
freeze T_CF
```

La coverage non viene usata per cambiare gli oracle black-box già congelati.

---

## 2. Scope e metriche di adeguatezza

L'esperimento è class-focused. La metrica primaria viene calcolata sulla sola
classe esterna selezionata:

```text
org.apache.openjpa.enhance.PCEnhancer
```

I tipi nested presenti in `PCEnhancer.java` possono essere attraversati durante
l'esecuzione, ma non vengono inclusi nel denominatore primario della classe
selezionata.

Metriche:

```text
LINE Coverage
BRANCH Coverage
METHOD Coverage
```

Strumento:

```text
JaCoCo 0.8.15
```

Denominatori canonici:

```text
LINE   : 2699
BRANCH : 1217
METHOD : 163
```

---

## 3. Baseline T_BB prima di T_CF

La suite manuale black-box iniziale è congelata e completamente verde:

```text
Tests   : 30
PASS    : 30
FAIL    : 0
Errors  : 0
Skipped : 0
```

Baseline primaria:

| Metrica | Covered | Missed | Totale | Coverage |
|---|---:|---:|---:|---:|
| Line | 1169 | 1530 | 2699 | 43.31% |
| Branch | 368 | 849 | 1217 | 30.24% |
| Method | 107 | 56 | 163 | 65.64% |

Evidence:

```text
isw2/results/testing/pcenhancer/tbb/coverage/
```

---

## 4. Formal pre-T_CF gap audit

Prima di definire la suite definitiva viene utilizzato un gap audit sulla
coverage della `T_BB` congelata.

I principali cluster funzionali considerati sono:

```text
Application Identity
Externalization
Detached State
Serialization
Identity Optimization
Property / Field Access
Cloning
```

I gap sono segnali di prioritizzazione, non obiettivi da azzerare.

### Regola di selezione

Un candidato `T_CF` viene preferito quando presenta una combinazione convincente
di:

1. gap sostanziale in Line e/o Branch Coverage;
2. comportamento production funzionalmente coerente;
3. raggiungibilità legittima tramite una fixture/configurazione ragionevole;
4. limitata sovrapposizione con i test già accettati;
5. incremento di coverage atteso non marginale;
6. valore esplicativo sufficiente per essere difeso come singolo test manuale.

Non vengono aggiunti test esclusivamente per chiudere un singolo `if` o per
ottenere pochi decimi percentuali.

---

## 5. Razionalizzazione della prima esplorazione T_CF

Prima del formal gap audit erano stati realizzati piccoli candidati esplorativi
centrati su guardie o rami locali di `run()`, `checkEnhancementLevel(...)` e
`isPCSubclassName(...)`.

L'audit ha mostrato che tali micro-candidati avevano valore marginale rispetto
a macro-scenari funzionali più rilevanti. I micro-test non fanno parte della
suite definitiva.

I sorgenti obsoleti:

```text
PCEnhancerControlFlowTest.java
PCEnhancerControlFlowCompactGapTest.java
```

sono assenti dalla versione congelata.

---

## 6. Inventory finale T_CF

| ID | Scenario | Test class |
|---|---|---|
| TCF-001 | Application Identity con IdClass composita | `PCEnhancerControlFlowApplicationIdentityTest` |
| TCF-002 | Detached-state Externalization | `PCEnhancerControlFlowExternalizationTest` |
| TCF-003 | Standard Java Serialization | `PCEnhancerControlFlowSerializationTest` |
| TCF-004 | Relationship-valued primary key / derived identity | `PCEnhancerControlFlowRelationshipIdentityTest` |
| TCF-005 | Optimized IdClass constructor copy | `PCEnhancerControlFlowOptimizedIdentityTest` |

Tutti i test definitivi si trovano in:

```text
isw2/testing/src/test/java/
it/uniroma2/isw2/openjpa/testing/pcenhancer/cf/
```

La convenzione è:

```text
*Test.java   = test JUnit della suite T_CF
*Target.java = fixture controllata fornita in input a PCEnhancer
```

Le fixture non sono ulteriori subject under test.

---

## 7. TCF-001 – Application Identity

### WHY

Il gap audit identifica Application Identity come scenario ad alta priorità.
Viene selezionato un caso con `@IdClass` e tre attributi identificativi scalari
per esercitare in modo coerente la generazione del supporto object-id.

### HOW

Test:

```text
PCEnhancerControlFlowApplicationIdentityTest.java
```

Fixture:

```text
PCEnhancerControlFlowApplicationIdentityTarget.java
```

Dopo `enhancer.run()` l'oracle osserva la generazione dei gruppi di metodi:

```text
pcCopyKeyFieldsToObjectId
pcCopyKeyFieldsFromObjectId
pcNewObjectIdInstance
```

### RESULT

Standalone run:

```text
isw2/results/testing/pcenhancer/tcf/runs/
pcenhancer_tcf001_application_identity_run.txt
```

Stato:

```text
TCF-001 PASS
```

---

## 8. TCF-002 – Detached-state Externalization

### WHY

Externalization rappresenta un macro-scenario distinto e raggiungibile tramite
una configurazione persistence legittima.

### HOW

Test:

```text
PCEnhancerControlFlowExternalizationTest.java
```

Fixture:

```text
PCEnhancerControlFlowExternalizationTarget.java
```

La fixture è una entity `Serializable` con detached state sintetico e viene
configurata con detached-state field non transient.

Oracle principali:

```text
Externalizable presente tra le interfacce generate
readExternal presente
writeExternal presente
```

### RESULT

Standalone run:

```text
isw2/results/testing/pcenhancer/tcf/runs/
pcenhancer_tcf002_externalization_run.txt
```

Stato:

```text
TCF-002 PASS
```

---

## 9. TCF-003 – Standard Serialization

### WHY

Lo scenario esercita la serializzazione Java standard mantenendola distinta dal
percorso Externalization coperto da TCF-002.

### Feasibility preflight

Il diagnostic verifica:

```text
Serializable                : true
Externalizable              : false
externalizeDetached         : false
createSubclass              : false
redefine                    : false
standard serialization path : true
```

Prima dell'enhancement:

```text
serialVersionUID : 0
writeObject      : 0
readObject       : 0
```

Dopo l'enhancement:

```text
serialVersionUID : 1
writeObject      : 1
readObject       : 1
readExternal     : 0
writeExternal    : 0
```

Verdetto:

```text
FEASIBILITY: VALID
```

Evidence:

```text
isw2/results/testing/pcenhancer/tcf/audits/
pcenhancer_tcf003_serialization_feasibility.txt
```

### HOW

Test:

```text
PCEnhancerControlFlowSerializationTest.java
```

Fixture:

```text
PCEnhancerControlFlowSerializationTarget.java
```

Oracle:

```text
run() == ENHANCE_PC
serialVersionUID generato
writeObject(ObjectOutputStream) generato
readObject(ObjectInputStream) generato
readExternal assente
writeExternal assente
Externalizable non aggiunta
```

### RESULT

Standalone run:

```text
isw2/results/testing/pcenhancer/tcf/runs/
pcenhancer_tcf003_serialization_run.txt
```

Stato:

```text
TCF-003 PASS
```

---

## 10. TCF-004 – Relationship-valued Identity

### WHY

Il percorso appartiene al caso in cui un attributo della primary key è esso
stesso un persistent type (`JavaTypes.PC`), definendo uno scenario coerente di
derived / relationship-valued identity.

### Feasibility preflight

Fixture:

```text
PCEnhancerControlFlowRelationshipIdentityParentTarget.java
PCEnhancerControlFlowRelationshipIdentityTarget.java
```

La primary key del target contiene:

```text
parent     : persistent relationship / JavaTypes.PC
sequenceId : long
```

Verdetti:

```text
RUNTIME FEASIBILITY : VALID
COVERAGE FEASIBILITY: VALID
```

Evidence:

```text
isw2/results/testing/pcenhancer/tcf/audits/
pcenhancer_tcf004_relationship_identity_feasibility.txt
```

### HOW

Test:

```text
PCEnhancerControlFlowRelationshipIdentityTest.java
```

Oracle principali:

```text
ID_APPLICATION
relationship PK con JavaTypes.PC
metadata del related type presente
ENHANCE_PC
pcCopyKeyFieldsToObjectId generato
pcCopyKeyFieldsFromObjectId generato
pcNewObjectIdInstance generato
```

### RESULT

Standalone run:

```text
isw2/results/testing/pcenhancer/tcf/runs/
pcenhancer_tcf004_relationship_identity_run.txt
```

Stato:

```text
TCF-004 PASS
```

---

## 11. TCF-005 – Optimized IdClass Copy

### WHY

Lo scenario copre il percorso di ottimizzazione della copia dell'IdClass,
distinto dal caso relationship-valued di TCF-004.

### Feasibility preflight

Fixture:

```text
PCEnhancerControlFlowOptimizedIdentityTarget.java
```

L'IdClass usa:

```text
private PK fields
no public setters
public compatible constructor
constructor parameter order = String, long, int
```

`OptimizeIdCopy` viene abilitato e il diagnostic verifica il bytecode generato.

Verdetti:

```text
RUNTIME FEASIBILITY : VALID
COVERAGE FEASIBILITY: VALID
```

Evidence:

```text
isw2/results/testing/pcenhancer/tcf/audits/
pcenhancer_tcf005_optimized_identity_feasibility.txt
```

### HOW

Test:

```text
PCEnhancerControlFlowOptimizedIdentityTest.java
```

Oracle principali:

```text
OptimizeIdCopy enabled
ID_APPLICATION
3 PK non-PC
ENHANCE_PC
identity copy methods generated
NEW IdClass generated
optimized constructor (String,long,int) invoked
```

### RESULT

Standalone run:

```text
isw2/results/testing/pcenhancer/tcf/runs/
pcenhancer_tcf005_optimized_identity_run.txt
```

Stato:

```text
TCF-005 PASS
```

---

## 12. Risultato cumulativo della coverage

Suite canoniche:

| Suite | Test | PASS | FAIL | Line covered | Line % | Branch covered | Branch % | Method covered | Method % |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| T_BB | 30 | 30 | 0 | 1169 | 43.31% | 368 | 30.24% | 107 | 65.64% |
| T_BB + T_CF | 35 | 35 | 0 | 1902 | 70.47% | 668 | 54.89% | 141 | 86.50% |

Contributo complessivo di `T_CF`:

```text
Additional covered lines    : +733
Additional covered branches : +300
LINE delta                  : +27.16 pp
BRANCH delta                : +24.65 pp
```

Evidence:

```text
isw2/results/testing/pcenhancer/tcf/coverage/
```

---

## 13. Gap audit final e stopping rule

Il final gap audit mostra che rimangono linee e branch non coperti. La presenza
di coverage residua non implica automaticamente la necessità di nuovi test.

Stopping rule:

> `T_CF` deve rappresentare i principali scenari control-flow coerenti e
> legittimamente raggiungibili individuati dai gap audit. La presenza di
> coverage residua, da sola, non è una giustificazione sufficiente per
> introdurre ulteriori micro-test.

Esito:

```text
TCF-006 planned              : NO
T_CF stopping rule reached   : YES
T_CF STATUS                  : FROZEN
```

Evidence:

```text
isw2/results/testing/pcenhancer/tcf/audits/
pcenhancer_tcf_final_gap_audit.txt
```

---

## 14. Freeze finale T_CF

Sorgenti definitivi:

```text
5 *Test.java
6 controlled fixture files
11 Java artifacts totali
```

Test:

```text
TCF-001 -> PCEnhancerControlFlowApplicationIdentityTest.java
TCF-002 -> PCEnhancerControlFlowExternalizationTest.java
TCF-003 -> PCEnhancerControlFlowSerializationTest.java
TCF-004 -> PCEnhancerControlFlowRelationshipIdentityTest.java
TCF-005 -> PCEnhancerControlFlowOptimizedIdentityTest.java
```

Il freeze verifica:

```text
temporary Diagnostic.java : NONE
obsolete micro-tests       : NONE
clean test compilation     : SUCCESS
TCF-006                    : NOT PLANNED
SHA-256 manifest           : CREATED
```

Evidence:

```text
isw2/results/testing/pcenhancer/tcf/audits/
├── pcenhancer_tcf_freeze_audit.txt
└── pcenhancer_tcf_freeze_manifest.csv
```

Stato:

```text
T_CF STATUS: FROZEN
```

---

## 15. Struttura canonica delle evidence

```text
isw2/results/testing/pcenhancer/
├── preflight/
├── tbb/
│   ├── runs/
│   ├── audits/
│   └── coverage/
└── tcf/
    ├── runs/
    ├── audits/
    └── coverage/
```

Per `T_CF`:

```text
tcf/runs/
    pcenhancer_tcf001_application_identity_run.txt
    pcenhancer_tcf002_externalization_run.txt
    pcenhancer_tcf003_serialization_run.txt
    pcenhancer_tcf004_relationship_identity_run.txt
    pcenhancer_tcf005_optimized_identity_run.txt
    pcenhancer_tcf_final_cumulative_run.txt

tcf/audits/
    pcenhancer_tcf003_serialization_feasibility.txt
    pcenhancer_tcf004_relationship_identity_feasibility.txt
    pcenhancer_tcf005_optimized_identity_feasibility.txt
    pcenhancer_tcf_final_gap_audit.txt
    pcenhancer_tcf_freeze_audit.txt
    pcenhancer_tcf_freeze_manifest.csv

tcf/coverage/
    pcenhancer_tcf_final_coverage_audit.txt
    pcenhancer_tcf_final_coverage_summary.csv
    jacoco.xml
    jacoco.csv
```

---

## 16. Stato conclusivo e handoff

```text
T_BB            : FROZEN
T_CF            : FROZEN
Manual tests    : 35 cumulative
PASS            : 35
FAIL            : 0
LINE            : 70.47%
BRANCH          : 54.89%
METHOD          : 86.50%
```

Da questo punto `T_BB` e `T_CF` non vengono più modificati per inseguire la
coverage residua.

La fase metodologica successiva è:

```text
mutation analysis della suite manuale congelata
        ↓
analisi dei survived mutants
        ↓
eventuale suite mutation-guided T_MT
```

`T_MT`, se introdotta, viene documentata separatamente e non viene confusa con
i cinque test `T_CF` congelati.

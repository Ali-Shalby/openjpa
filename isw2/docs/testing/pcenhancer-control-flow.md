# PCEnhancer – suite manuale control-flow `T_CF`

## Scopo

Questo documento descrive la fase di **adequacy improvement manuale guidata dalla
coverage** per:

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
PASS             : 34
FAIL             : 1 (TBB-026 già noto)
LINE finale      : 70.77%
BRANCH finale    : 55.22%
T_CF STATUS      : FROZEN
```

`T_CF` non sostituisce e non modifica retroattivamente `T_BB`. La suite
black-box iniziale resta quella derivata tramite Category Partition; i test
control-flow sono aggiunte manuali successive, selezionate solo dopo aver
misurato l'adeguatezza della suite congelata.

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
La failure `TBB-026` viene mantenuta durante tutte le regressioni cumulative.

---

## 2. Scope e metriche di adeguatezza

L'esperimento è class-focused. La metrica primaria viene quindi calcolata sulla
sola classe esterna selezionata:

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
```

Strumento:

```text
JaCoCo 0.8.15
```

Il report viene generato sui class file di `PCEnhancer` estratti dall'artefatto
`openjpa-kernel:4.1.1`; l'agent viene limitato a:

```text
org.apache.openjpa.enhance.PCEnhancer*
```

Evidence della decisione di scope:

```text
isw2/results/testing/pcenhancer/tbb/coverage/
pcenhancer_tbb_coverage_scope_decision.txt
```

---

## 3. Baseline T_BB prima di T_CF

La suite manuale black-box iniziale era già completamente congelata e
validata:

```text
Tests   : 30
PASS    : 29
FAIL    : 1 (TBB-026)
Errors  : 0
Skipped : 0
```

Baseline primaria:

| Metrica | Covered | Missed | Totale | Coverage |
|---|---:|---:|---:|---:|
| Line | 1177 | 1522 | 2699 | 43.61% |
| Branch | 372 | 845 | 1217 | 30.57% |

Evidence:

```text
isw2/results/testing/pcenhancer/tbb/coverage/
├── pcenhancer_tbb_coverage_baseline_audit.txt
├── pcenhancer_tbb_coverage_scope_decision.txt
├── pcenhancer_tbb_coverage_summary.csv
├── jacoco.xml
└── jacoco.csv
```

---

## 4. Formal pre-T_CF gap audit

Prima di definire la suite definitiva è stato ricostruito un gap audit usando
**solo la coverage della T_BB congelata**.

Evidence:

```text
isw2/results/testing/pcenhancer/tbb/coverage/
pcenhancer_tbb_pre_tcf_gap_audit.txt
```

Principali cluster iniziali:

| Cluster | Missed Line | Missed Branch |
|---|---:|---:|
| Application Identity | 551 | 266 |
| Externalization | 260 | 92 |
| Detached State | 109 | 62 |
| Serialization | 107 | 34 |
| Identity Optimization | 84 | 65 |
| Property / Field Access | 79 | 72 |
| Cloning | 25 | 19 |

I valori sono segnali di prioritizzazione, non obiettivi da azzerare.

### Regola di selezione

Un candidato T_CF viene preferito quando presenta una combinazione convincente
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

## 5. Correzione della prima esplorazione T_CF

Prima del formal pre-T_CF audit erano stati realizzati alcuni piccoli candidati
esplorativi, centrati su guardie o rami locali di `run()`,
`checkEnhancementLevel(...)` e `isPCSubclassName(...)`.

Ricostruendo l'audit dalla baseline T_BB congelata si è verificato che tali
micro-candidati rappresentavano gap molto piccoli, mentre il cluster
Application Identity era completamente scoperto e molto più rilevante.

I micro-test sono stati quindi rimossi dalla suite definitiva; non sono stati
mantenuti solo perché già implementati. I sorgenti obsoleti:

```text
PCEnhancerControlFlowTest.java
PCEnhancerControlFlowCompactGapTest.java
```

sono assenti dalla versione congelata.

La precedente macro-fixture Application Identity è diventata il nuovo
`TCF-001`. Questa correzione è importante perché allinea la costruzione della
suite alla regola:

```text
gap audit prima della selezione definitiva del test
```

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

Il pre-T_CF gap audit mostrava il cluster più ampio completamente scoperto:

```text
APPLICATION_IDENTITY
Missed lines    : 551
Missed branches : 266
```

È stato quindi selezionato un caso di Application Identity con `@IdClass` e tre
attributi identificativi scalari, in modo da percorrere in modo coerente la
generazione del supporto per la copia e la creazione dell'object id.

### HOW

Test:

```text
PCEnhancerControlFlowApplicationIdentityTest.java
```

Fixture:

```text
PCEnhancerControlFlowApplicationIdentityTarget.java
```

Il target utilizza una application identity composita. Dopo `enhancer.run()`
l'oracle osserva la generazione dei gruppi di metodi:

```text
pcCopyKeyFieldsToObjectId
pcCopyKeyFieldsFromObjectId
pcNewObjectIdInstance
```

La fixture è top-level per mantenere il metadata scenario semplice e
rappresentativo di una normale entity; l'`IdClass` è parte della fixture.

### RESULT

Standalone:

```text
isw2/results/testing/pcenhancer/tcf/runs/
pcenhancer_tcf001_application_identity_run.txt
```

Cumulative state dopo TCF-001:

| Metrica | Before | After | Delta |
|---|---:|---:|---:|
| Line | 43.61% | 51.91% | +8.30 pp |
| Branch | 30.57% | 39.11% | +8.55 pp |

Incremento assoluto:

```text
+224 covered lines
+104 covered branches
```

Evidence coverage:

```text
isw2/results/testing/pcenhancer/tcf/coverage/
pcenhancer_tcf001_coverage_audit.txt
```

Il post-TCF-001 gap audit ha mostrato che Externalization era ancora
completamente scoperta: 260 linee e 92 branch mancanti.

---

## 8. TCF-002 – Detached-state Externalization

### WHY

Dopo TCF-001 il cluster Externalization risultava ancora:

```text
covered lines    : 0
missed lines     : 260
covered branches : 0
missed branches  : 92
```

Il cluster era ampio, indipendente dal precedente scenario e raggiungibile con
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
configurata con detached-state field non transient. Il percorso deve quindi
produrre supporto `Externalizable`.

Oracle principali dopo enhancement:

```text
Externalizable presente tra le interfacce generate
readExternal presente
writeExternal presente
```

### RESULT

Standalone:

```text
isw2/results/testing/pcenhancer/tcf/runs/
pcenhancer_tcf002_externalization_run.txt
```

Coverage cumulativa:

| Metrica | Before | After | Delta |
|---|---:|---:|---:|
| Line | 51.91% | 61.13% | +9.23 pp |
| Branch | 39.11% | 47.08% | +7.97 pp |

Incremento assoluto di TCF-002:

```text
+249 covered lines
+97 covered branches
```

Evidence:

```text
isw2/results/testing/pcenhancer/tcf/coverage/
pcenhancer_tcf002_coverage_audit.txt
```

Dopo TCF-002, Serialization rimaneva quasi completamente intatta e veniva
selezionata come successivo macro-cluster indipendente.

---

## 9. TCF-003 – Standard Serialization

### WHY

Nel post-TCF-002 audit il cluster Serialization presentava ancora:

```text
missed lines    : 107
missed branches : 33
```

Lo scenario doveva esercitare la serializzazione Java standard senza entrare
nel percorso Externalization già coperto da TCF-002.

### Feasibility preflight

Durante la prima progettazione si è osservato che una singola proprietà del
metadata (`detachedState`) non era sufficiente per stabilire il percorso.
Per evitare una catena di correzioni successive è stato introdotto un
**feasibility diagnostic completo** prima di congelare il test.

Il diagnostic ha verificato contemporaneamente:

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

Il sorgente diagnostico temporaneo è stato eliminato dopo il preflight; il file
TXT viene conservato come evidence.

### HOW

Test:

```text
PCEnhancerControlFlowSerializationTest.java
```

Fixture:

```text
PCEnhancerControlFlowSerializationTarget.java
```

Oracle finali:

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

Standalone:

```text
isw2/results/testing/pcenhancer/tcf/runs/
pcenhancer_tcf003_serialization_run.txt
```

Coverage cumulativa:

| Metrica | Before | After | Delta |
|---|---:|---:|---:|
| Line | 61.13% | 64.02% | +2.89 pp |
| Branch | 47.08% | 49.22% | +2.14 pp |

Incremento assoluto:

```text
+78 covered lines
+26 covered branches
```

TCF-003 ha chiuso 72 delle 107 linee e 14 dei 33 branch precedentemente
mancanti nel cluster Serialization.

---

## 10. TCF-004 – Relationship-valued Identity

### WHY

Dopo TCF-003 il metodo:

```text
addExtractObjectIdFieldValueCode
```

era completamente scoperto:

```text
missed lines    : 172
missed branches : 58
```

L'analisi del controllo di flusso ha mostrato che il metodo appartiene al caso
in cui un attributo della primary key è esso stesso un persistent type
(`JavaTypes.PC`). Questo definisce un comportamento coerente di derived /
relationship-valued identity e non un micro-branch isolato.

### Feasibility preflight

Sono state create due fixture controllate:

```text
PCEnhancerControlFlowRelationshipIdentityParentTarget.java
PCEnhancerControlFlowRelationshipIdentityTarget.java
```

La primary key del target contiene:

```text
parent     : persistent relationship / JavaTypes.PC
sequenceId : long
```

Il diagnostic ha verificato metadata, object-id type, generated identity
support e coverage isolata del metodo target.

Risultato isolato di `addExtractObjectIdFieldValueCode`:

```text
covered lines    : 38
covered branches : 7
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

Test definitivo:

```text
PCEnhancerControlFlowRelationshipIdentityTest.java
```

L'oracle verifica tra l'altro:

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

Standalone:

```text
isw2/results/testing/pcenhancer/tcf/runs/
pcenhancer_tcf004_relationship_identity_run.txt
```

Coverage cumulativa:

| Metrica | Before | After | Delta |
|---|---:|---:|---:|
| Line | 64.02% | 67.25% | +3.22 pp |
| Branch | 49.22% | 51.11% | +1.89 pp |

Incremento assoluto:

```text
+87 covered lines
+23 covered branches
```

---

## 11. TCF-005 – Optimized IdClass Copy

### WHY

Nel gap audit erano rimasti completamente scoperti due metodi del cluster
Identity Optimization:

```text
optimizeIdCopy
getIdClassConstructorParmOrder
```

Gap prima di TCF-005:

```text
optimizeIdCopy                     : 15 missed lines / 16 missed branches
getIdClassConstructorParmOrder     : 34 missed lines / 22 missed branches
```

Questo percorso è distinto da TCF-004: `optimizeIdCopy` richiede primary-key
attributes non-PC e quindi non coincide con il caso relationship-valued.

### Feasibility preflight

Fixture:

```text
PCEnhancerControlFlowOptimizedIdentityTarget.java
```

L'IdClass è costruita deliberatamente con:

```text
private PK fields
no public setters
public compatible constructor
constructor parameter order = String, long, int
```

`OptimizeIdCopy` viene abilitato e il diagnostic verifica non soltanto il
metadata, ma anche il bytecode generato: il percorso deve creare l'IdClass e
invocare il costruttore ottimizzato.

Risultati isolated coverage:

| Metodo | Line covered | Branch covered |
|---|---:|---:|
| `optimizeIdCopy` | 12 | 9 |
| `getIdClassConstructorParmOrder` | 30 | 15 |

Il bytecode generato contiene:

```text
NEW IdClass instructions    : 2
optimized constructor calls : 2
```

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

Test definitivo:

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

Standalone:

```text
isw2/results/testing/pcenhancer/tcf/runs/
pcenhancer_tcf005_optimized_identity_run.txt
```

Coverage cumulativa finale:

| Metrica | Before | After | Delta |
|---|---:|---:|---:|
| Line | 67.25% | 70.77% | +3.52 pp |
| Branch | 51.11% | 55.22% | +4.11 pp |

Incremento assoluto:

```text
+95 covered lines
+50 covered branches
```

---

## 12. Evoluzione complessiva della coverage

| Suite cumulativa | Test | PASS | FAIL | Line covered | Line % | Branch covered | Branch % |
|---|---:|---:|---:|---:|---:|---:|---:|
| T_BB | 30 | 29 | 1 | 1177 | 43.61% | 372 | 30.57% |
| + TCF-001 | 31 | 30 | 1 | 1401 | 51.91% | 476 | 39.11% |
| + TCF-002 | 32 | 31 | 1 | 1650 | 61.13% | 573 | 47.08% |
| + TCF-003 | 33 | 32 | 1 | 1728 | 64.02% | 599 | 49.22% |
| + TCF-004 | 34 | 33 | 1 | 1815 | 67.25% | 622 | 51.11% |
| + TCF-005 | 35 | 34 | 1 | 1910 | 70.77% | 672 | 55.22% |

Contributo complessivo di T_CF rispetto alla T_BB congelata:

```text
Additional covered lines    : +733
Additional covered branches : +300
LINE delta                  : +27.16 pp
BRANCH delta                : +24.65 pp
```

L'unica failure lungo tutta la sequenza rimane `TBB-026`; i cinque T_CF
standalone passano individualmente.

---

## 13. Gap audit final e stopping rule

Il final gap audit mostra che rimangono linee e branch non coperti. Alcuni
metodi sono ancora parzialmente coperti e altri rimangono completamente
scoperti.

Questo non implica automaticamente la necessità di nuovi test.

La stopping rule adottata è:

> T_CF deve rappresentare i principali scenari control-flow coerenti e
> legittimamente raggiungibili individuati dai gap audit. La presenza di
> coverage residua, da sola, non è una giustificazione sufficiente per
> introdurre ulteriori micro-test.

Il final gap audit registra:

```text
TCF-006 planned              : NO
T_CF stopping rule reached   : YES
T_CF STATUS                  : READY TO FREEZE
```

Evidence:

```text
isw2/results/testing/pcenhancer/tcf/audits/
pcenhancer_tcf_final_gap_audit.txt
```

---

## 14. Freeze finale T_CF

Il freeze audit finale verifica la composizione fisica della suite.

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

Run standalone conservati:

```text
pcenhancer_tcf001_application_identity_run.txt
pcenhancer_tcf002_externalization_run.txt
pcenhancer_tcf003_serialization_run.txt
pcenhancer_tcf004_relationship_identity_run.txt
pcenhancer_tcf005_optimized_identity_run.txt
```

Il freeze verifica inoltre:

```text
temporary Diagnostic.java : NONE
obsolete micro-tests       : NONE
clean test compilation     : SUCCESS
final cumulative run       : VALID
TCF-006                    : NOT PLANNED
SHA-256 manifest           : CREATED
```

Evidence:

```text
isw2/results/testing/pcenhancer/tcf/audits/
├── pcenhancer_tcf_freeze_audit.txt
└── pcenhancer_tcf_freeze_manifest.csv
```

Esito:

```text
T_CF STATUS: FROZEN
RESULT: PCENHANCER T_CF FINAL FREEZE AUDIT PASSED
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

tcf/audits/
    pcenhancer_post_tcf001_gap_audit.txt
    pcenhancer_post_tcf002_gap_audit.txt
    pcenhancer_post_tcf003_gap_audit.txt
    pcenhancer_tcf003_serialization_feasibility.txt
    pcenhancer_tcf004_relationship_identity_feasibility.txt
    pcenhancer_tcf005_optimized_identity_feasibility.txt
    pcenhancer_tcf_final_gap_audit.txt
    pcenhancer_tcf_freeze_audit.txt
    pcenhancer_tcf_freeze_manifest.csv

tcf/coverage/
    pcenhancer_tcf001_coverage_audit.txt
    pcenhancer_tcf002_coverage_audit.txt
    pcenhancer_tcf003_coverage_audit.txt
    pcenhancer_tcf004_coverage_audit.txt
    pcenhancer_tcf_final_coverage_audit.txt
    pcenhancer_tcf_final_coverage_summary.csv
    jacoco.xml
    jacoco.csv
```

Gli eventuali file storici di esperimenti intermedi non fanno parte della
suite definitiva; la documentazione fa riferimento agli artefatti canonici del
freeze.

---

## 16. Stato conclusivo e handoff

```text
T_BB            : FROZEN
T_CF            : FROZEN
Manual tests    : 35 cumulative
Known failure   : TBB-026
LINE            : 70.77%
BRANCH          : 55.22%
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

`T_MT`, se introdotta, deve essere documentata separatamente e non deve essere
confusa con i cinque test `T_CF` congelati.

# PCEnhancer – Category Partition e suite manuale black-box

## Scopo

Questo documento raccoglie le decisioni metodologiche, le scelte implementative
e le evidenze di esecuzione relative alla suite manuale iniziale `T_BB` per:

```text
org.apache.openjpa.enhance.PCEnhancer
```

Baseline:

```text
Apache OpenJPA 4.1.1
Tag Git: 4.1.1
Baseline sperimentale: C0
```

Stato del documento:

```text
Category Partition : FROZEN
T_BB               : FROZEN / IMPLEMENTED / FULLY EXECUTED
N iniziale         : 30
Test eseguiti      : 30/30
PASS                : 29
FAIL                : 1 (TBB-026, documented-contract discrepancy)
LINE baseline       : 43.61%
BRANCH baseline     : 30.57%
```

Il documento viene aggiornato dopo ogni famiglia di test, prima di procedere
alla successiva.

---

## 1. Obiettivo metodologico

Per la parte De Angelis la suite manuale iniziale viene definita tramite
**Category Partition**, adottando un approccio **black-box**.

L'obiettivo non è scegliere a priori un numero conveniente di test.
Il valore `N` deve essere una conseguenza delle categorie, delle choices e delle
interazioni significative individuate a partire dal contratto pubblico della
classe.

La sequenza adottata è:

```text
documentazione / contratto pubblico
        ↓
funzionalità osservabili
        ↓
categorie e choices
        ↓
test frame candidati
        ↓
audit di validità, distinguibilità, oracle e fattibilità
        ↓
freeze di T_BB
        ↓
implementazione
        ↓
esecuzione della suite iniziale
        ↓
coverage / mutation / adequacy improvement
```

Coverage, mutation testing e dettagli del controllo di flusso vengono quindi
osservati **solo dopo** il freeze della suite manuale iniziale.

---

## 2. Gerarchia delle fonti

Le fonti vengono utilizzate con il seguente ordine di priorità:

1. materiale ufficiale del corso del prof. De Angelis;
2. Apache OpenJPA 4.1.1 User's Guide;
3. Javadoc e API pubblica di Apache OpenJPA 4.1.1;
4. audit dell'API compilata tramite `javap -public`;
5. ispezione minima di `C0` solo quando strettamente necessaria a:
    * istanziare con un valore concreto una categoria già derivata dalla documentazione;
    * completare un oracle già definito dal contratto pubblico.

Riferimenti OpenJPA:

```text
https://openjpa.apache.org/builds/4.1.1/apache-openjpa/docs/manual.html
https://openjpa.apache.org/builds/4.1.1/apidocs/
```

### Esclusioni

La suite `T_BB` iniziale **non** viene derivata da:

* test già presenti nel repository OpenJPA;
* branch o statement coverage;
* struttura privata del controllo di flusso;
* mutant generati da PIT;
* risultati SonarQube;
* varianti future della Milestone 4.

Questi elementi possono essere utilizzati in fasi successive per misurare o
migliorare l'adeguatezza, ma non per ridefinire retroattivamente la suite
black-box iniziale.

---

## 3. Freeze della Category Partition

Una prima specifica conteneva 13 frame. Durante il riesame si è osservato che
essa comprimeva eccessivamente alcune categorie e non rappresentava in modo
sistematico diversi casi speciali e condizioni al contorno.

La Category Partition è stata quindi ricostruita da zero.

Il rebuild ha prodotto:

```text
30 test frame candidati
```

Prima del freeze ogni frame è stato sottoposto alle seguenti domande:

1. il comportamento deriva dalla documentazione o dall'API pubblica?
2. rappresenta una choice o un'interazione semanticamente significativa?
3. possiede un oracle esternamente osservabile e sufficientemente stabile?
4. è possibile costruire una fixture senza riutilizzare i test nativi OpenJPA?

Non è stato applicato alcun limite artificiale alla dimensione della suite.

Esito:

```text
30 candidati
30 KEEP
0 DROP
N finale iniziale = 30
```

La precedente specifica da 13 frame e la successiva ipotesi intermedia da
20 frame sono considerate obsolete.

---

## 4. Inventory della suite T_BB

| Famiglia | Funzionalità | Frame | N |
|---|---|---|---:|
| F1 | Dynamic persistence-capable subclass names | TBB-001..004, TBB-006..007 | 6 |
| F2 | Default `PCEnhancer.Flags` | TBB-005 | 1 |
| F3 | Required no-argument constructor | TBB-008..011 | 4 |
| F4 | Enhancement target state and outcome | TBB-012..015 | 4 |
| F5 | Property-access restriction enforcement | TBB-016..018 | 3 |
| F6 | Bytecode recording destination | TBB-019..020 | 2 |
| F7 | Target representation and cardinality | TBB-021..025 | 5 |
| F8 | Tool invocation validity | TBB-026 | 1 |
| F9 | Direct enhancement vs generated subclass | TBB-027..028 | 2 |
| F10 | Enhancement contract level | TBB-029..030 | 2 |
| **Totale** |  |  | **30** |

---

## 5. Condizioni al contorno e casi speciali

Per `PCEnhancer` i confini più significativi non sono principalmente intervalli
numerici, ma **boundary di stato e di configurazione**.

Sono stati identificati, tra gli altri:

```text
generated name / ordinary name / empty string
no-arg constructor present / missing
constructor repair enabled / disabled
persistence metadata present / absent
not enhanced / already PersistenceCapable
property restriction satisfied / violated
restriction enforcement disabled / enabled
output directory null / explicit
zero / one explicit enhancement target
current / immediately older enhancement contract version
```

Le condizioni al contorno non sono state aggiunte alla fine soltanto per
aumentare il numero di test: fanno parte direttamente delle choices derivate
dalle categorie.

---

## 6. Traceability dei 30 frame

### F1 – Dynamic persistence-capable subclass names

API pubblica:

```text
PCEnhancer.isPCSubclassName(String)
PCEnhancer.toManagedTypeName(String)
```

| ID | Choice | Oracle |
|---|---|---|
| TBB-001 | generated PC-subclass name + recognition | `true` |
| TBB-002 | ordinary FQCN + recognition | `false` |
| TBB-003 | generated PC-subclass name + conversion | managed type name |
| TBB-004 | ordinary FQCN + conversion | input unchanged |
| TBB-006 | empty string + recognition | `false` |
| TBB-007 | empty string + conversion | empty string unchanged |

### F2 – Default enhancer flags

| ID | Choice | Oracle |
|---|---|---|
| TBB-005 | `new PCEnhancer.Flags()` | documented default values |

Valori verificati:

```text
directory == null
addDefaultConstructor == true
tmpClassLoader == true
enforcePropertyRestrictions == false
```

### F3 – Required no-argument constructor

Categorie:

```text
constructor state = present / missing
addDefaultConstructor = true / false
```

| ID | Constructor | Flag | Oracle |
|---|---|---:|---|
| TBB-008 | present | true | enhancement succeeds |
| TBB-009 | present | false | enhancement succeeds |
| TBB-010 | missing | true | enhancement succeeds and no-arg constructor is generated |
| TBB-011 | missing | false | enhancement is rejected |

TBB-008 e TBB-009 vengono mantenuti entrambi anche se condividono l'esito
finale, perché rappresentano due configurazioni diverse della choice
`addDefaultConstructor` nel caso in cui non sia necessaria alcuna riparazione.

### F4 – Enhancement target state and outcome

| ID | Target state | Oracle |
|---|---|---|
| TBB-012 | persistence metadata present | `ENHANCE_PC` |
| TBB-013 | persistence metadata absent | `ENHANCE_AWARE` |
| TBB-014 | already `PersistenceCapable` | `ENHANCE_NONE` |
| TBB-015 | managed interface | `ENHANCE_INTERFACE` |

### F5 – Property-access restriction enforcement

| ID | Entity | Enforcement | Oracle |
|---|---|---:|---|
| TBB-016 | compliant | true | accepted |
| TBB-017 | violating | false | not rejected for the restriction |
| TBB-018 | violating | true | rejected |

### F6 – Bytecode recording destination

| ID | Directory | Oracle |
|---|---|---|
| TBB-019 | `null` | disposable original class file is replaced |
| TBB-020 | explicit temp directory | enhanced class appears below the package tree |

Per TBB-019 non viene mai sovrascritto il bytecode production del repository.
Il test deve utilizzare esclusivamente una fixture o copia temporanea isolata.

### F7 – Target representation and cardinality

| ID | Target selection | Oracle |
|---|---|---|
| TBB-021 | one FQCN | selected fixture is enhanced |
| TBB-022 | one `.java` path | selected fixture is enhanced |
| TBB-023 | one `.class` path | selected fixture is enhanced |
| TBB-024 | one `.jdo` listing | listed fixture is selected |
| TBB-025 | zero explicit targets + persistent class list | configured fixture is selected |

Le quattro rappresentazioni esplicite vengono mantenute perché appartengono
all'interfaccia pubblicamente documentata del tool.

### F8 – Tool invocation validity

| ID | Choice | Oracle |
|---|---|---|
| TBB-026 | deliberately unknown/invalid option | public `run(..., Options)` returns `false` |

Representative invalid choice congelata:

```text
definitelyNotAValidOpenJPAOption = x
```

L'input invalido viene scelto prima dell'esecuzione. Il comportamento osservato
non viene usato per modificare a posteriori l'oracle.

### F9 – Enhancement strategy

| ID | Mode | Oracle |
|---|---|---|
| TBB-027 | `redefine=false`, `createSubclass=false` | direct enhancement succeeds |
| TBB-028 | `redefine=true`, `createSubclass=true` | generated persistence-capable subclass |

### F10 – Enhancement contract level

| ID | Contract version | Oracle |
|---|---|---|
| TBB-029 | `ENHANCER_VERSION` | `checkEnhancementLevel(...) == false` |
| TBB-030 | `ENHANCER_VERSION - 1` | `true` and down-level condition logged |

TBB-029/TBB-030 rappresentano un vero boundary numerico sul contratto di
enhancement.

---

## 7. Scelte implementative generali

### Harness separato

I test universitari vengono mantenuti in un harness Maven indipendente:

```text
isw2/testing/
```

Questo permette di separare chiaramente:

```text
codice production OpenJPA
test nativi OpenJPA
suite sperimentali ISW2
```

### Nessun riutilizzo dei test nativi

Le fixture e i test della suite sperimentale vengono progettati da zero.

I test già presenti nel repository possono essere consultati soltanto se una
fase futura del progetto lo richiederà esplicitamente; non vengono usati come
base per costruire `T_BB`.

### Fixture purpose-built

Quando una categoria richiede uno stato specifico, viene creata una fixture
minimale apposita.

Esempi:

```text
classe con costruttore no-arg
classe senza costruttore no-arg
entity property-access compliant
entity property-access violating
managed interface
controlled PersistenceCapable fixture
```

Ogni fixture deve esprimere solamente la proprietà necessaria al frame che
rappresenta.

### Oracle

Gli oracle devono essere, quando possibile:

* valori di ritorno pubblici;
* eccezioni/rifiuto documentati;
* stato pubblico dell'oggetto;
* presenza di artefatti prodotti;
* caratteristiche osservabili del bytecode generato strettamente necessarie
  al contratto documentato.

Si evita di controllare dettagli privati non necessari, come messaggi esatti di
eccezione o branch interni.

### Esecuzione incrementale

Le famiglie vengono implementate ed eseguite separatamente:

```text
documentazione famiglia
        ↓
fixture e oracle
        ↓
implementazione
        ↓
run isolato
        ↓
analisi del risultato
        ↓
aggiornamento documentazione
        ↓
famiglia successiva
```

In caso di errore non si procede alla famiglia successiva finché non viene
stabilito se il problema appartiene alla fixture, all'harness o alla baseline.

---

## 8. F1 – Implementazione ed evidenza

Test class:

```text
isw2/testing/src/test/java/
it/uniroma2/isw2/openjpa/testing/pcenhancer/bb/
PCEnhancerBlackBoxNamingTest.java
```

Frame implementati:

```text
TBB-001
TBB-002
TBB-003
TBB-004
TBB-006
TBB-007
```

### Scelta del generated subclass name

La categoria:

```text
generated persistence-capable subclass name
```

è stata definita prima dell'ispezione del codice.

La documentazione pubblica non fornisce un esempio concreto sufficiente a
costruire direttamente il valore di test. Una ispezione minima di `C0` è stata
quindi utilizzata **solo** per istanziare la categoria già congelata.

Valore utilizzato:

```text
org.apache.openjpa.enhance.com$example$Customer$pcsubclass
```

Managed type associato:

```text
com.example.Customer
```

Questa ispezione non è stata utilizzata per aggiungere categorie, branch o test
alla suite.

### Boundary della stringa vuota

La stringa vuota rappresenta il caso speciale del dominio `String` che non
soddisfa la categoria del generated subclass name.

Oracle:

```text
isPCSubclassName("") == false
toManagedTypeName("") == ""
```

### Esecuzione

Comando:

```powershell
mvn -f isw2/testing/pom.xml `
    "-Dtest=it.uniroma2.isw2.openjpa.testing.pcenhancer.bb.PCEnhancerBlackBoxNamingTest" `
    test 2>&1 |
    Tee-Object -FilePath isw2/results/testing/pcenhancer/tbb/runs/pcenhancer_tbb_f1_boundary_run.txt
```

Risultato:

```text
Tests run: 6
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

Evidence:

```text
isw2/results/testing/pcenhancer/tbb/runs/pcenhancer_tbb_f1_boundary_run.txt
```

Stato:

```text
F1 PASS — 6/6
```

---

## 9. F2 – Implementazione ed evidenza

Test class:

```text
isw2/testing/src/test/java/
it/uniroma2/isw2/openjpa/testing/pcenhancer/bb/
PCEnhancerBlackBoxFlagsTest.java
```

Frame:

```text
TBB-005
```

L'oracle controlla direttamente i default pubblici di `PCEnhancer.Flags`.

Evidence:

```text
isw2/results/testing/pcenhancer/tbb/runs/pcenhancer_tbb_f2_run.txt
```

Risultato:

```text
Tests run: 1
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

Stato:

```text
F2 PASS — 1/1
```

---

## 10. Stato della T_BB

```text
Frozen T_BB size : 30

F1 : 6/6 PASS
F2 : 1/1 PASS
F3 : 4/4 PASS
F4 : 4/4 PASS
F5 : 3/3 PASS
F6 : 2/2 PASS
F7 : 5/5 PASS
F8  : 0/1 PASS — 1 documented-contract failure
F9  : 2/2 PASS
F10 : 2/2 PASS

Executed         : 30/30
Passed           : 29/30
Failed           : 1/30
Not yet executed : 0/30
```

Il freeze della suite rimane indipendente dai risultati futuri di coverage e
mutation testing.

---

## 11. F3 – Required no-argument constructor

La famiglia F3 verifica la politica pubblica di `PCEnhancer` relativa alla
presenza del costruttore no-arg e all'opzione `addDefaultConstructor`.

### WHY

Le categorie definite tramite Category Partition sono:

```text
constructor state = present / missing
addDefaultConstructor = true / false
```

La combinazione produce quattro frame:

| ID | Constructor | addDefaultConstructor | Oracle |
|---|---|---:|---|
| TBB-008 | present | `true` | enhancement succeeds |
| TBB-009 | present | `false` | enhancement succeeds |
| TBB-010 | missing | `true` | enhancement succeeds and a no-arg constructor is generated |
| TBB-011 | missing | `false` | enhancement is rejected |

TBB-008 e TBB-009 vengono mantenuti entrambi perché rappresentano due diverse
configurazioni pubbliche dell'enhancer anche quando non è necessario generare
un nuovo costruttore.

### HOW

Sono state create due fixture purpose-built:

```text
WithNoArgConstructor
WithoutNoArgConstructor
```

`WithNoArgConstructor` contiene esplicitamente un costruttore no-arg.

`WithoutNoArgConstructor` dichiara invece soltanto un costruttore con parametro,
impedendo a `javac` di aggiungere implicitamente il costruttore no-arg.

Le fixture non provengono dai test nativi OpenJPA.

Per ogni fixture viene creato un `PCEnhancer` isolato con metadata dedicati e
viene impostato esplicitamente il valore di `addDefaultConstructor`.

Gli oracle verificano soltanto proprietà necessarie al contratto black-box:

```text
TBB-008 -> enhancement succeeds
TBB-009 -> enhancement succeeds
TBB-010 -> constructor ()V absent before run()
           enhancement succeeds
           constructor ()V present after run()
TBB-011 -> enhancement rejected
```

La visibilità esatta del costruttore generato non viene utilizzata come oracle,
poiché non è necessaria per verificare la categoria considerata.

Le fixture non contengono proprietà persistenti perché la famiglia F3 deve
isolare il comportamento relativo al costruttore. OpenJPA può quindi produrre
warning relativi all'assenza di persistent properties; tali warning non
costituiscono failure della suite.

### RESULT

Test class:

```text
isw2/testing/src/test/java/
it/uniroma2/isw2/openjpa/testing/pcenhancer/bb/
PCEnhancerBlackBoxConstructorTest.java
```

Comando:

```powershell
mvn -f isw2/testing/pom.xml `
    "-Dtest=it.uniroma2.isw2.openjpa.testing.pcenhancer.bb.PCEnhancerBlackBoxConstructorTest" `
    test 2>&1 |
    Tee-Object -FilePath isw2\results\testing\pcenhancer\tbb\runs\pcenhancer_tbb_f3_run.txt
```

Risultato finale:

```text
Tests run: 4
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

Evidence:

```text
isw2/results/testing/pcenhancer/tbb/runs/pcenhancer_tbb_f3_run.txt
```

Stato:

```text
F3 PASS — 4/4
```

---

## 12. F4 – Enhancement target state and outcome

La famiglia F4 verifica il risultato pubblico restituito da `PCEnhancer.run()`
in funzione dello stato del target.

### WHY

I quattro frame congelati sono:

| ID | Target state | Oracle |
|---|---|---|
| TBB-012 | persistence metadata present | `ENHANCE_PC` |
| TBB-013 | persistence metadata absent | `ENHANCE_AWARE` |
| TBB-014 | already `PersistenceCapable` | `ENHANCE_NONE` |
| TBB-015 | managed interface | `ENHANCE_INTERFACE` |

Questi frame rappresentano quattro stati pubblicamente distinguibili del target
e i corrispondenti valori di ritorno documentati dell'enhancer.

### HOW

Sono state create quattro fixture purpose-built:

```text
PersistentTarget
PersistenceAwareTarget
AlreadyPersistenceCapable
ManagedContract
```

`PersistentTarget` riceve metadata espliciti e contiene un campo persistente
minimale, così da rappresentare una classe persistente reale senza introdurre
warning non necessari.

`PersistenceAwareTarget` viene passato all'enhancer senza metadata.

`AlreadyPersistenceCapable` dichiara esplicitamente l'interfaccia pubblica
`PersistenceCapable`.

`ManagedContract` è una interface annotata con `@ManagedInterface`.

Il logging OpenJPA viene mantenuto a livello `WARN`, ma instradato su `stdout`
per evitare che PowerShell presenti normali warning come `NativeCommandError`.

### RESULT

Test class:

```text
isw2/testing/src/test/java/
it/uniroma2/isw2/openjpa/testing/pcenhancer/bb/
PCEnhancerBlackBoxOutcomeTest.java
```

Evidence:

```text
isw2/results/testing/pcenhancer/tbb/runs/pcenhancer_tbb_f4_run.txt
```

Risultato:

```text
Tests run: 4
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

Stato:

```text
F4 PASS — 4/4
```

---

## 13. F5 – Property-access restriction enforcement

La famiglia F5 verifica l'effetto dell'opzione
`enforcePropertyRestrictions` sui persistent type che usano PROPERTY access.

### WHY

Le categorie sono:

```text
property-access target = compliant / violating
enforcement            = enabled / disabled
```

I tre frame congelati sono:

| ID | Target | Enforcement | Oracle |
|---|---|---:|---|
| TBB-016 | compliant | `true` | enhancement accepted |
| TBB-017 | violating | `false` | violation does not reject enhancement |
| TBB-018 | violating | `true` | enhancement rejected |

La violazione è definita dalla regola documentata secondo cui, con PROPERTY
access, il backing field deve essere acceduto direttamente solo dal relativo
getter/setter.

### HOW

Sono state create due fixture:

```text
CompliantPropertyTarget
ViolatingPropertyTarget
```

Nella fixture compliant, un metodo esterno legge la proprietà tramite
`getValue()`.

Nella fixture violating, `readValueDirectly()` accede direttamente al backing
field `value`.

I metadata vengono popolati esplicitamente con:

```text
ACCESS_PROPERTY
```

così che la differenza osservata dipenda dalla regola di property access e non
da una scelta implicita dell'harness.

Il warning generato per la fixture violating è intenzionale e costituisce
evidenza del fatto che la violazione sia stata effettivamente rilevata.
Il logging rimane visibile su `stdout`.

### RESULT

Test class:

```text
isw2/testing/src/test/java/
it/uniroma2/isw2/openjpa/testing/pcenhancer/bb/
PCEnhancerBlackBoxPropertyAccessTest.java
```

Comando:

```powershell
mvn -f isw2/testing/pom.xml `
    "-Dtest=it.uniroma2.isw2.openjpa.testing.pcenhancer.bb.PCEnhancerBlackBoxPropertyAccessTest" `
    test 2>&1 |
    Tee-Object -FilePath isw2\results\testing\pcenhancer\tbb\runs\pcenhancer_tbb_f5_run.txt
```

Risultato:

```text
Tests run: 3
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

Evidence:

```text
isw2/results/testing/pcenhancer/tbb/runs/pcenhancer_tbb_f5_run.txt
```

Stato:

```text
F5 PASS — 3/3
```

---

## Regola di aggiornamento del documento

Per ogni famiglia successiva devono essere registrati separatamente:

### WHY

Scelta metodologica derivata dalla documentazione / Category Partition.

### HOW

Fixture, configurazione e oracle utilizzati per rendere eseguibile il frame.

### RESULT

Comando, evidence, numero di test, failures, errors, skipped ed eventuali
problemi riscontrati.

Questa separazione permette di distinguere ciò che era stato deciso
**prima dell'esecuzione** da ciò che è stato osservato **dopo l'esecuzione**.


## 14. F6 – Bytecode recording destination

La famiglia F6 verifica il comportamento pubblico di `PCEnhancer.record()`
rispetto alla configurazione della directory di output.

### WHY

Le due choices congelate sono:

| ID | Directory | Oracle |
|---|---|---|
| TBB-019 | `null` | il `.class` originale della fixture disposable viene sostituito |
| TBB-020 | directory esplicita | il `.class` enhanced viene scritto sotto la directory indicata rispettando il package tree |

Il caso `directory == null` è mantenuto perché rappresenta il comportamento di
default documentato. Per motivi di sicurezza non viene mai applicato al bytecode
production o alla copia normale della fixture nel repository.

### HOW

Per TBB-019 il bytecode della fixture viene copiato in una directory temporanea
isolata e caricato tramite un `URLClassLoader` dedicato. In questo modo
`record()` può esercitare il comportamento di overwrite senza modificare
`target/test-classes` o altri artefatti del progetto.

L'oracle confronta i byte del `.class` disposable prima e dopo `record()`.

Per TBB-020 viene configurata una directory temporanea esplicita tramite
`setDirectory(File)`. L'oracle verifica che:

```text
<output-dir>/<package-path>/<class-name>.class
```

venga creato e che la fixture originale rimanga invariata.

La fixture è purpose-built e contiene un singolo campo persistente minimale.

### RESULT

Test class:

```text
isw2/testing/src/test/java/
it/uniroma2/isw2/openjpa/testing/pcenhancer/bb/
PCEnhancerBlackBoxRecordingTest.java
```

Comando:

```powershell
mvn -f isw2/testing/pom.xml `
    "-Dtest=it.uniroma2.isw2.openjpa.testing.pcenhancer.bb.PCEnhancerBlackBoxRecordingTest" `
    test 2>&1 |
    Tee-Object -FilePath isw2\results\testing\pcenhancer\tbb\runs\pcenhancer_tbb_f6_run.txt
```

Risultato:

```text
Tests run: 2
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

Evidence:

```text
isw2/results/testing/pcenhancer/tbb/runs/pcenhancer_tbb_f6_run.txt
```

Stato:

```text
F6 PASS — 2/2
```

---


## 15. F7 – Target representation and cardinality

La famiglia F7 verifica le rappresentazioni pubblicamente documentate del
target accettate dall'entry point del tool `PCEnhancer.run(...)` e il boundary
con zero target espliciti.

### WHY

I frame congelati sono:

| ID | Target selection | Oracle |
|---|---|---|
| TBB-021 | one FQCN | selected fixture is enhanced |
| TBB-022 | one `.java` path | selected fixture is enhanced |
| TBB-023 | one `.class` path | selected fixture is enhanced |
| TBB-024 | one `.jdo` metadata file | listed fixture is selected |
| TBB-025 | zero explicit targets + configured persistent-type list | configured fixture is selected |

Le quattro forme esplicite vengono mantenute come choices distinte perché sono
parte dell'interfaccia pubblicamente documentata del tool.

TBB-025 rappresenta il boundary di cardinalità:

```text
explicit target count = 0
```

### HOW

Tutti i frame utilizzano la stessa fixture purpose-built:

```text
PCEnhancerBlackBoxToolTarget
```

così da variare soltanto la rappresentazione dell'input.

Per evitare qualunque modifica al bytecode del progetto viene utilizzato un
`BytecodeWriter` dedicato che cattura il nome della classe che `PCEnhancer`
intende registrare, senza scrivere file.

Per TBB-021 il target è fornito come FQCN.

Per TBB-022 viene creato un file `.java` temporaneo che rappresenta la stessa
classe.

Per TBB-023 viene copiata la relativa risorsa `.class` in una posizione
temporanea.

Per TBB-024 viene creato un metadata file temporaneo con estensione `.jdo` che
elenca la stessa fixture.

Per TBB-025 la lista dei persistent type viene configurata nel metadata factory
e `PCEnhancer.run(...)` viene invocato con un array di target vuoto.

`tmpClassLoader` viene impostato a `false` per mantenere stabile il classloader
della fixture; tale opzione non costituisce la variabile sotto test in F7.

### RESULT

Test class:

```text
isw2/testing/src/test/java/
it/uniroma2/isw2/openjpa/testing/pcenhancer/bb/
PCEnhancerBlackBoxTargetRepresentationTest.java
```

Comando:

```powershell
mvn -f isw2/testing/pom.xml `
    "-Dtest=it.uniroma2.isw2.openjpa.testing.pcenhancer.bb.PCEnhancerBlackBoxTargetRepresentationTest" `
    test 2>&1 |
    Tee-Object -FilePath isw2\results\testing\pcenhancer\tbb\runs\pcenhancer_tbb_f7_run.txt
```

Risultato:

```text
Tests run: 5
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

Evidence:

```text
isw2/results/testing/pcenhancer/tbb/runs/pcenhancer_tbb_f7_run.txt
```

Stato:

```text
F7 PASS — 5/5
```

---


## 16. F8 – Invalid tool option

La famiglia F8 contiene un singolo frame relativo al contratto pubblico
dell'overload `PCEnhancer.run(..., Options)`.

### WHY

La documentazione pubblica del metodo dichiara:

```text
Returns false if invalid options given.
```

Il representative value della partition è stato congelato prima
dell'esecuzione:

```text
definitelyNotAValidOpenJPAOption = x
```

Il nome non appartiene alle opzioni specifiche di `PCEnhancer` e non
corrisponde a una bean property della configurazione OpenJPA.

Frame:

| ID | Choice | Oracle |
|---|---|---|
| TBB-026 | unknown/invalid option | `run(...) == false` |

### HOW

Il test utilizza una fixture purpose-built e una directory temporanea per
evitare modifiche al bytecode originale.

Vengono inoltre impostate opzioni di supporto valide (`directory` e
`tmpClassLoader=false`), mentre l'unica variabile sotto test è la proprietà
deliberatamente inesistente.

L'oracle è rimasto quello congelato dalla documentazione e non è stato adattato
dopo aver osservato la baseline.

### RESULT

Test class:

```text
isw2/testing/src/test/java/
it/uniroma2/isw2/openjpa/testing/pcenhancer/bb/
PCEnhancerBlackBoxInvalidOptionsTest.java
```

Evidence:

```text
isw2/results/testing/pcenhancer/tbb/runs/pcenhancer_tbb_f8_run.txt
```

Risultato osservato:

```text
Tests run: 1
Failures: 1
Errors: 0
Skipped: 0
BUILD FAILURE

expected: false
actual:   true
```

Stato:

```text
F8 FAIL — TBB-026 reveals a documented-contract discrepancy in C0
```

### Analisi della discrepanza

Nel percorso pubblico `run(OpenJPAConfiguration, String[], Options)`,
le opzioni specifiche dell'enhancer vengono rimosse e le restanti opzioni
vengono passate a `Configurations.populateConfiguration(...)`.

`Options.setInto(...)` è in grado di restituire le entries per cui non è stato
trovato alcun setter/proprietà corrispondente. Tuttavia il percorso di
configurazione utilizzato da `PCEnhancer` non usa tale insieme per restituire
`false`; l'esecuzione prosegue e può terminare con `true`.

Per questo representative invalid input, quindi, C0 non rispetta il comportamento
dichiarato dal contratto pubblico del metodo.

Questa evidenza:

- non modifica la Category Partition;
- non modifica l'oracle;
- non viene trasformata artificialmente in un PASS;
- non è classificata come problema dell'harness.

Il test viene mantenuto nella suite come test black-box di conformità che
espone la discrepanza.

---


## 17. F9 – Direct enhancement vs generated subclass

La famiglia F9 verifica due strategie pubblicamente configurabili di
`PCEnhancer`.

### WHY

I frame congelati sono:

| ID | redefine | createSubclass | Oracle |
|---|---:|---:|---|
| TBB-027 | `false` | `false` | il managed type viene enhanced direttamente |
| TBB-028 | `true` | `true` | viene generato bytecode persistence-capable distinto, come subclass del managed type |

La variabile sotto test è quindi la strategia di enhancement, non la
conversione inversa dei nomi già coperta in F1.

### HOW

La fixture `StrategyTarget` è purpose-built ed è compatibile con entrambe le
strategie: tipo non-final, costruttore no-arg accessibile e getter/setter
pubblici non-final.

Per TBB-027 gli oracle verificano che:

```text
managed type bytecode == persistence-capable bytecode
```

Per TBB-028 gli oracle verificano che:

```text
managed type bytecode != persistence-capable bytecode
generated type satisfies isPCSubclassName(...)
generated type extends the managed type
```

Durante il primo run di F9 era presente un controllo supplementare che
riapplicava `toManagedTypeName(...)` al nome generato. La fixture è una nested
class e tale controllo introduceva una limitazione nota del mapping dei nomi
nested; inoltre il mapping inverso appartiene già a F1 e non all'oracle
congelato di F9. Il controllo supplementare è stato quindi rimosso mantenendo
inalterata la finalità black-box di TBB-028.

### RESULT

Test class:

```text
isw2/testing/src/test/java/
it/uniroma2/isw2/openjpa/testing/pcenhancer/bb/
PCEnhancerBlackBoxStrategyTest.java
```

Evidence:

```text
isw2/results/testing/pcenhancer/tbb/runs/pcenhancer_tbb_f9_run.txt
```

Risultato finale:

```text
Tests run: 2
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

Stato:

```text
F9 PASS — 2/2
```

---


## 18. F10 – Enhancement contract level

La famiglia F10 verifica il boundary pubblico esposto da
`PCEnhancer.checkEnhancementLevel(Class<?>, Log)` rispetto alla versione del
contratto di enhancement.

### WHY

Il contratto distingue il caso corrente dal caso down-level tramite il confine:

```text
pcGetEnhancementContractVersion() < PCEnhancer.ENHANCER_VERSION
```

I frame congelati sono:

| ID | Contract version | Oracle |
|---|---|---|
| TBB-029 | `ENHANCER_VERSION` | `checkEnhancementLevel(...) == false` |
| TBB-030 | `ENHANCER_VERSION - 1` | `checkEnhancementLevel(...) == true` e condizione down-level registrata |

TBB-029 e TBB-030 rappresentano quindi i due valori immediatamente ai lati
della soglia.

### HOW

Sono state create due fixture purpose-built che implementano il contratto
pubblico `PersistenceCapable` e restituiscono rispettivamente:

```text
ENHANCER_VERSION
ENHANCER_VERSION - 1
```

Le fixture vengono registrate tramite la API pubblica `PCRegistry`.

Un `Log` minimale controllato dal test consente di osservare la presenza del
messaggio informativo nel caso down-level senza dipendere da un backend di
logging esterno.

I metodi `PersistenceCapable` non rilevanti per F10 restituiscono valori neutri
o non compiono operazioni; l'unica variabile sotto test è la versione del
contratto di enhancement.

### RESULT

Test class:

```text
isw2/testing/src/test/java/
it/uniroma2/isw2/openjpa/testing/pcenhancer/bb/
PCEnhancerBlackBoxEnhancementLevelTest.java
```

Comando:

```powershell
mvn -f isw2/testing/pom.xml `
    "-Dtest=it.uniroma2.isw2.openjpa.testing.pcenhancer.bb.PCEnhancerBlackBoxEnhancementLevelTest" `
    test 2>&1 |
    Tee-Object -FilePath isw2\results\testing\pcenhancer\tbb\runs\pcenhancer_tbb_f10_run.txt
```

Risultato:

```text
Tests run: 2
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

Evidence:

```text
isw2/results/testing/pcenhancer/tbb/runs/pcenhancer_tbb_f10_run.txt
```

Stato:

```text
F10 PASS — 2/2
```

---

## 19. Risultato della suite manuale iniziale T_BB

Tutti i frame congelati tramite Category Partition sono stati implementati ed
eseguiti.

Risultato complessivo per famiglia:

| Famiglia | Test | PASS | FAIL |
|---|---:|---:|---:|
| F1 | 6 | 6 | 0 |
| F2 | 1 | 1 | 0 |
| F3 | 4 | 4 | 0 |
| F4 | 4 | 4 | 0 |
| F5 | 3 | 3 | 0 |
| F6 | 2 | 2 | 0 |
| F7 | 5 | 5 | 0 |
| F8 | 1 | 0 | 1 |
| F9 | 2 | 2 | 0 |
| F10 | 2 | 2 | 0 |
| **Totale** | **30** | **29** | **1** |

Stato:

```text
T_BB size : 30
Executed  : 30/30
PASS      : 29/30
FAIL      : 1/30
```

L'unico test failing è:

```text
TBB-026 — invalid option
expected: false
actual:   true
```

La failure è mantenuta come evidenza di una discrepanza tra il contratto
pubblico documentato di `PCEnhancer.run(..., Options)` e il comportamento
osservato nella baseline OpenJPA 4.1.1 (`C0`).

La suite non viene modificata per rendere artificiosamente verde TBB-026.

### Freeze post-esecuzione

Con l'esecuzione di TBB-001..TBB-030 termina la costruzione della suite manuale
black-box iniziale.

Da questo punto:

- la Category Partition iniziale rimane congelata;
- gli eventuali test aggiunti sulla base di coverage appartengono alla fase di
  adequacy improvement e non modificano retroattivamente `T_BB`;
- gli eventuali test aggiunti sulla base di mutation testing vengono tracciati
  separatamente;
- TBB-026 resta parte della suite e resta failing finché C0 mantiene il
  comportamento osservato.

## 20. Full regression e audit finale T_BB

Dopo l'esecuzione family-by-family è stata eseguita una regressione completa
della suite congelata, senza introdurre nuovi frame e senza modificare gli
oracle.

Evidence canonica:

```text
isw2/results/testing/pcenhancer/tbb/runs/pcenhancer_tbb_full_run.txt
```

Risultato:

```text
Tests run : 30
PASS      : 29
FAIL      : 1
Errors    : 0
Skipped   : 0
```

La failure rimane esclusivamente `TBB-026`.

È stato inoltre eseguito un audit finale statico e di esecuzione:

```text
isw2/results/testing/pcenhancer/tbb/audits/pcenhancer_tbb_final_audit.txt
```

L'audit verifica:

```text
Test classes : 10
Total @Test  : 30
Full run     : 30 test
PASS         : 29
FAIL         : 1 (TBB-026)
```

Esito:

```text
RESULT: PCENHANCER T_BB FINAL AUDIT PASSED
```

Un audit separato conserva la traceability degli oracle e delle fonti:

```text
isw2/results/testing/pcenhancer/tbb/audits/pcenhancer_tbb_oracle_source_audit.txt
```

---

## 21. Baseline di adeguatezza della suite congelata

Solo dopo il freeze e la completa esecuzione di `T_BB` è stata introdotta la
misurazione di coverage con JaCoCo.

La metrica primaria è calcolata esclusivamente sulla classe esterna selezionata:

```text
org.apache.openjpa.enhance.PCEnhancer
```

Le due metriche adottate sono:

```text
LINE
BRANCH
```

La decisione di scope è conservata in:

```text
isw2/results/testing/pcenhancer/tbb/coverage/pcenhancer_tbb_coverage_scope_decision.txt
```

Baseline `T_BB`:

| Metrica | Covered | Missed | Totale | Coverage |
|---|---:|---:|---:|---:|
| Line | 1177 | 1522 | 2699 | 43.61% |
| Branch | 372 | 845 | 1217 | 30.57% |

Evidence:

```text
isw2/results/testing/pcenhancer/tbb/coverage/pcenhancer_tbb_coverage_baseline_audit.txt
isw2/results/testing/pcenhancer/tbb/coverage/jacoco.xml
isw2/results/testing/pcenhancer/tbb/coverage/jacoco.csv
```

Questi valori costituiscono la baseline immutabile rispetto alla quale viene
misurata la successiva suite manuale coverage-guided `T_CF`.

---

## 22. Passaggio a T_CF

Dalla baseline `T_BB` è stato prodotto un coverage-gap audit formale, basato
esclusivamente sui risultati della suite black-box congelata:

```text
isw2/results/testing/pcenhancer/tbb/coverage/pcenhancer_tbb_pre_tcf_gap_audit.txt
```

Da questo punto l'ispezione del controllo di flusso è ammessa per selezionare
nuovi test manuali di adequacy improvement. Tali test appartengono a `T_CF` e
**non modificano retroattivamente la Category Partition né `T_BB`**.

La metodologia, le fixture, i gap selezionati, le coverage incrementali e il
freeze della suite `T_CF` sono documentati separatamente in:

```text
isw2/docs/testing/pcenhancer-control-flow.md
```

Stato al termine della fase successiva:

```text
T_BB            : 30 test
T_CF additions  : 5 test
Cumulative      : 35 test
Final LINE      : 70.77%
Final BRANCH    : 55.22%
T_CF status     : FROZEN
```

---

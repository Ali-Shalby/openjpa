# Milestone 1 – Dataset Creation

## 1. Obiettivo

La Milestone 1 richiede la costruzione di un dataset a livello di **classe Java**.

Ogni istanza del dataset finale rappresenterà una classe appartenente a una determinata release OpenJPA.

Il dataset conterrà informazioni riconducibili a:

```text
Project
Class
Release
Metrics
NSmells
Bugginess
```

La costruzione viene automatizzata attraverso il modulo:

```text
isw2/analyzer/
```

---

# 2. Workflow generale

Il workflow seguito è:

```text
identificazione release
        ↓
associazione release → commit
        ↓
selezione della porzione iniziale delle release
        ↓
identificazione delle classi
        ↓
calcolo delle metriche
        ↓
calcolo NSmells
        ↓
analisi bug / SZZ / Proportion
        ↓
Bugginess
        ↓
Dataset A
```

---

# 3. Recupero delle release

Le informazioni sulle release vengono recuperate dal progetto Apache JIRA di OpenJPA.

Sono considerate inizialmente tutte le versioni che soddisfano:

```text
released = true
releaseDate presente
```

Le principali classi implementate sono:

```text
ReleaseInfo
JiraReleaseClient
ReleaseCatalogGenerator
```

---

# 4. Catalogo RAW

Il primo output generato è:

```text
isw2/datasets/release_catalog_raw.csv
```

Colonne:

```text
ChronologicalIndex
JiraVersionId
Version
ReleaseDate
GitTag
GitTagMatched
```

Risultati:

```text
Released JIRA versions : 42
Matching Git tags       : 37/42
First release           : 0.9.0 (2006-08-26)
Last release            : 4.1.1 (2025-05-15)
```

Il catalogo RAW viene mantenuto come output intermedio e non viene modificato manualmente.

---

# 5. Filtro delle release stabili

Nel catalogo JIRA sono presenti anche milestone e beta, tra cui:

```text
2.0.0-M1
2.0.0-M2
2.0.0-M3
2.0.0-beta
2.0.0-beta2
2.0.0-beta3
```

Per il dataset vengono considerate soltanto versioni nel formato:

```text
X.Y.Z
```

Pattern:

```regex
^\d+\.\d+\.\d+$
```

Risultato:

```text
Stable X.Y.Z releases : 36
Releases with Git tag : 33
```

---

# 6. Associazione release → commit

In una prima implementazione era stato considerato il tag Git come possibile snapshot della release.

La verifica del catalogo ha però mostrato che alcuni tag storici puntavano a commit temporalmente successivi alla rispettiva `releaseDate`.

È stato quindi scelto il metodo:

```text
DATE_CUTOFF
```

Per ogni release viene individuato:

```text
l'ultimo commit Git non successivo alla ReleaseDate
```

La ricerca viene effettuata sulla storia raggiungibile dalla baseline:

```text
4.1.1
```

Il Git tag rimane nel catalogo esclusivamente come informazione diagnostica.

La proprietà verificata è:

```text
ReleaseCommitDate <= ReleaseDate
```

Il controllo è risultato soddisfatto per tutte le 36 release stabili.

Le classi principali utilizzate per questa fase sono:

```text
GitReleaseResolver
ResolvedRelease
DatasetReleaseCatalogGenerator
```

---

# 7. Catalogo definitivo

Output:

```text
isw2/datasets/release_catalog.csv
```

Colonne:

```text
ChronologicalIndex
JiraVersionId
Version
ReleaseDate
GitTag
GitTagMatched
ReleaseCommit
ReleaseCommitDate
ResolutionMethod
DatasetIncluded
```

Risultati:

```text
Released JIRA versions : 42
Stable X.Y.Z releases  : 36
Releases with Git tag  : 33
Release resolution     : DATE_CUTOFF
```

Tutte le release stabili sono state associate a un commit Git.

---

# 8. Selezione delle release del Dataset A

La parte iniziale della storia del progetto viene utilizzata per la costruzione del dataset.

È stato adottato il primo:

```text
33%
```

delle release stabili.

Il numero viene calcolato mediante:

```text
ceil(numberOfReleases * 0.33)
```

Per OpenJPA:

```text
36 * 0.33 = 11.88
ceil(11.88) = 12
```

Vengono quindi selezionate **12 release**.

|  # | Versione | Release Date |
| -: | -------- | ------------ |
|  1 | 0.9.0    | 2006-08-26   |
|  2 | 0.9.6    | 2006-11-29   |
|  3 | 0.9.7    | 2007-04-27   |
|  4 | 1.0.0    | 2007-08-28   |
|  5 | 1.0.1    | 2007-11-09   |
|  6 | 1.0.2    | 2008-02-18   |
|  7 | 1.1.0    | 2008-05-22   |
|  8 | 1.0.3    | 2008-07-23   |
|  9 | 1.2.0    | 2008-08-12   |
| 10 | 1.2.1    | 2009-03-18   |
| 11 | 1.2.2    | 2010-01-18   |
| 12 | 2.0.0    | 2010-04-22   |

Intervallo:

```text
0.9.0 → 2.0.0
2006-08-26 → 2010-04-22
```

La selezione viene registrata nella colonna:

```text
DatasetIncluded
```

del catalogo definitivo.

---

---

# 9. Identificazione dei file Java

Dopo aver determinato le 12 revisioni Git da utilizzare per il dataset,
è stato costruito un inventario di tutti i file Java presenti in ciascuna release.

La raccolta viene effettuata direttamente sull'albero Git tramite:

```text
git ls-tree -r --name-only <commit>
```

Questa soluzione permette di analizzare lo snapshot esatto di ogni release
senza eseguire checkout ripetuti sulla working copy principale.

Il primo output prodotto è:

```text
isw2/datasets/java_class_inventory_raw.csv
```

Il catalogo RAW contiene tutti i file `.java` individuati nelle 12 release
e li classifica in base al loro ruolo.

Le categorie utilizzate sono:

```text
PRODUCTION
TEST
EXAMPLE
GENERATED
PARSER_SOURCE
NON_CLASS
OTHER
```

Risultati ottenuti:

```text
Dataset releases : 12
Java files       : 25186

PRODUCTION       : 12836
TEST             : 12281
EXAMPLE          : 66
GENERATED        : 0
PARSER_SOURCE    : 3
NON_CLASS        : 0
OTHER            : 0
```

La classificazione è stata verificata prima della costruzione
dell'inventario definitivo.

Durante la verifica sono stati individuati due sorgenti collocati sotto
`src/main/java` ma appartenenti a un modulo di integration testing:

```text
openjpa-integration/osgi-itests/src/main/java/hellojpa/Main.java
openjpa-integration/osgi-itests/src/main/java/hellojpa/Message.java
```

La sola presenza del percorso `src/main/java` non è quindi sufficiente per
stabilire che un file appartenga al codice production.

I due file sono stati correttamente riclassificati come:

```text
TEST
```

Sono stati inoltre individuati tre sorgenti sotto:

```text
src/main/jjtree
```

Questi file vengono classificati separatamente come:

```text
PARSER_SOURCE
```

e non vengono considerati normali classi production.

Al termine della verifica:

```text
OTHER : 0
```

quindi ogni file Java individuato risulta classificato in modo esplicito.

---

# 10. Inventario delle classi production

Per le successive attività della Milestone 1 vengono considerate le classi
appartenenti al codice production.

Sono pertanto escluse dall'inventario definitivo le categorie:

```text
TEST
EXAMPLE
GENERATED
PARSER_SOURCE
NON_CLASS
OTHER
```

L'output definitivo è:

```text
isw2/datasets/java_class_inventory.csv
```

Ogni osservazione è identificata dalla coppia:

```text
Release + Class
```

e contiene:

```text
ReleaseIndex
Version
CommitId
Class
```

Il numero totale di osservazioni production è:

```text
12836
```

Distribuzione per release:

|  # | Versione | Classi production |
| -: | -------- | -----------------: |
|  1 | 0.9.0    |                932 |
|  2 | 0.9.6    |                949 |
|  3 | 0.9.7    |                948 |
|  4 | 1.0.0    |                996 |
|  5 | 1.0.1    |               1029 |
|  6 | 1.0.2    |               1058 |
|  7 | 1.1.0    |               1045 |
|  8 | 1.0.3    |               1050 |
|  9 | 1.2.0    |               1051 |
| 10 | 1.2.1    |               1185 |
| 11 | 1.2.2    |               1300 |
| 12 | 2.0.0    |               1293 |

Sono stati verificati i seguenti invarianti:

```text
Release analizzate       : 12
Osservazioni production  : 12836
Chiavi duplicate         : 0
Valori mancanti          : 0
Scope esclusi presenti   : 0
File non Java            : 0
```

L'inventario definitivo costituisce la base sulla quale verranno calcolate
le metriche della Milestone 1.

---

# 11. Output disponibili

Attualmente sono disponibili:

```text
isw2/datasets/release_catalog_raw.csv
isw2/datasets/release_catalog.csv
isw2/datasets/java_class_inventory_raw.csv
isw2/datasets/java_class_inventory.csv
```

### `release_catalog_raw.csv`

Catalogo iniziale delle versioni recuperate da JIRA e confronto con i tag Git.

### `release_catalog.csv`

Catalogo definitivo delle release, con associazione release → commit e
indicazione delle release incluse nel Dataset A.

### `java_class_inventory_raw.csv`

Inventario completo dei file Java presenti nelle 12 release selezionate,
con classificazione del relativo scope.

### `java_class_inventory.csv`

Inventario definitivo delle sole osservazioni production utilizzate come
base per le metriche della Milestone 1.

Tutti i file sono generati automaticamente dall'analyzer e non vengono
modificati manualmente.

---

# 12. Decisioni metodologiche

Le principali decisioni adottate finora sono:

### Release stabili

Sono considerate release stabili solamente versioni `X.Y.Z`.

### Git tag

Il tag viene utilizzato come informazione diagnostica ma non come sorgente
dello snapshot della release.

### Release commit

Il commit rappresentativo viene selezionato tramite `DATE_CUTOFF`.

### Porzione delle release

Viene utilizzato il primo 33% delle release stabili, con arrotondamento
tramite `ceil`.

### Identificazione dei file Java

I file vengono letti direttamente dall'albero Git della revisione tramite
`git ls-tree`, senza modificare la working copy principale.

### Classi production

Per le successive analisi vengono utilizzate solamente le osservazioni
classificate come `PRODUCTION`.

Sono esclusi test, esempi, sorgenti generati, sorgenti parser e file che
non rappresentano classi production.

La classificazione non si basa esclusivamente sulla presenza di
`src/main/java`, ma considera anche il contesto del modulo e del percorso,
come nel caso di `osgi-itests`.

### Dataset

Gli output CSV vengono generati automaticamente e non modificati manualmente.

---

# 13. Stato Milestone 1

## Completato

* [x] Recupero release da JIRA
* [x] Generazione catalogo RAW delle release
* [x] Filtro release stabili
* [x] Verifica tag Git
* [x] Associazione release → commit
* [x] Validazione temporale dei commit
* [x] Generazione catalogo definitivo delle release
* [x] Selezione delle 12 release
* [x] Inventario RAW dei file Java
* [x] Classificazione dei sorgenti Java
* [x] Verifica delle regole di inclusione/esclusione
* [x] Gestione dei sorgenti di integration test
* [x] Gestione dei `PARSER_SOURCE`
* [x] Generazione dell'inventario production
* [x] Validazione delle 12.836 osservazioni production

## Prossimo step

* [ ] Calcolo delle metriche di classe

Successivamente:

* [ ] calcolo di `NSmells`;
* [ ] recupero e analisi dei ticket bug;
* [ ] SZZ;
* [ ] Proportion;
* [ ] determinazione della `Bugginess`;
* [ ] generazione del Dataset A.

Il presente documento verrà aggiornato durante l'avanzamento della milestone.

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

L'inventario definitivo costituisce la base sulla quale vengono calcolate
le metriche della Milestone 1.

---

# 11. Metriche di classe

Dopo la costruzione dell'inventario production vengono calcolate le metriche
di classe richieste per ciascuna osservazione:

```text
(release, class)
```

Il calcolo viene effettuato automaticamente dall'analyzer utilizzando
lo snapshot Git associato alla release e la storia precedente della classe.

L'output prodotto è:

```text
isw2/datasets/class_metrics.csv
```

Il dataset contiene:

```text
ReleaseIndex
Version
CommitId
Class
LOC
LOC_TOUCHED
NR
NAUTH
LOC_ADDED
MAX_LOC_ADDED
AVG_LOC_ADDED
CHURN
MAX_CHURN
AVG_CHURN
CHANGE_SET_SIZE
MAX_CHANGE_SET
AVG_CHANGE_SET
AGE_WEEKS
WEIGHTED_AGE_WEEKS
IGNORED_ZERO_LOC_REVS
```

`NFIX` non viene ancora calcolato in questa fase, poiché dipende
dall'identificazione dei defect ticket e dei relativi fix commit.
La metrica verrà aggiunta successivamente durante l'analisi JIRA/SZZ.

## 11.1 LOC

`LOC` rappresenta la dimensione della classe nello snapshot della release.

Il contenuto viene recuperato direttamente da Git tramite la coppia:

```text
CommitId + Class
```

senza modificare la working copy.

Il conteggio considera le linee fisiche contenenti codice Java ed esclude:

```text
linee vuote
commenti line-only
linee contenenti esclusivamente commenti block
```

La gestione dei commenti block mantiene lo stato tra linee consecutive.

## 11.2 Metriche storiche

Le metriche storiche vengono calcolate cumulativamente:

```text
from release 0
```

cioè utilizzando la storia della classe dall'inizio del progetto fino al
commit rappresentativo della release osservata.

La storia viene ricostruita attraverso Git utilizzando:

```text
git log <releaseCommit> --follow --no-merges --numstat
```

`--follow` permette di seguire la classe anche attraverso rename o move
del file.

Per la cronologia viene utilizzata la `committer date`, in quanto rappresenta
il momento in cui una modifica è entrata effettivamente nella storia Git
utilizzata per costruire le release.

## 11.3 Revisioni effettive e NR

`NR` rappresenta il **number of revisions**, cioè il numero di revisioni
della classe considerate nel calcolo storico.

È stata adottata la seguente definizione operativa di revisione sorgente
effettiva:

```text
added LOC + deleted LOC > 0
```

Un commit viene quindi conteggiato in `NR` quando modifica effettivamente
il contenuto sorgente Java della classe.

Operazioni che modificano esclusivamente metadata Git, come:

```text
file mode change
rename/move puro con contenuto invariato
```

vengono comunque seguite durante la ricostruzione della storia ma non
incrementano `NR`.

Un rename della classe che modifica anche il sorgente, ad esempio cambiando
la dichiarazione Java, oppure un rename/move accompagnato da qualsiasi
modifica testuale, viene invece conteggiato normalmente.

Questa scelta evita che operazioni amministrative sul repository modifichino
artificialmente il numero di revisioni e le relative metriche medie senza
alcuna modifica al codice Java.

Il numero di revisioni Git incontrate ma escluse per assenza di modifiche
testuali viene mantenuto come informazione diagnostica nella colonna:

```text
IGNORED_ZERO_LOC_REVS
```

## 11.4 Autori

`NAUTH` rappresenta il numero di autori distinti che hanno modificato
effettivamente la classe fino alla release osservata.

L'identificazione utilizza l'author email riportata dalla storia Git.

## 11.5 LOC Touched e LOC Added

Per una singola revisione:

```text
LOC_TOUCHED_revision = added + deleted
LOC_ADDED_revision   = added
```

Per ogni classe vengono quindi calcolati cumulativamente:

```text
LOC_TOUCHED
LOC_ADDED
MAX_LOC_ADDED
AVG_LOC_ADDED
```

dove:

```text
AVG_LOC_ADDED = LOC_ADDED / NR
```

## 11.6 Churn

`LOC_TOUCHED` e `CHURN` vengono mantenuti come metriche distinte.

Per una singola revisione:

```text
LOC_TOUCHED_revision = added + deleted
CHURN_revision       = added - deleted
```

Di conseguenza:

```text
CHURN = Σ(added - deleted)
```

mentre:

```text
LOC_TOUCHED = Σ(added + deleted)
```

Questa distinzione permette di rappresentare due fenomeni differenti:

```text
LOC_TOUCHED → quantità complessiva di codice modificato
CHURN       → variazione netta del codice
```

Sono inoltre calcolati:

```text
MAX_CHURN
AVG_CHURN
```

con:

```text
AVG_CHURN = CHURN / NR
```

La formulazione sintetica del materiale didattico per il Churn risulta
ambigua rispetto a `LOC_TOUCHED`; l'implementazione mantiene quindi le due
metriche semanticamente distinte, usando `added + deleted` per LOC Touched
e `added - deleted` per Churn.

## 11.7 Change Set Size

Per ogni revisione effettiva viene calcolato il numero di file modificati
dal relativo commit.

Il change set viene ricostruito mediante Git e memorizzato in cache per
commit, evitando di ricalcolare la stessa informazione per più classi
modificate insieme.

Per ciascuna osservazione vengono prodotti:

```text
CHANGE_SET_SIZE
MAX_CHANGE_SET
AVG_CHANGE_SET
```

dove `CHANGE_SET_SIZE` rappresenta la somma dei change-set size delle
revisioni effettive della classe e:

```text
AVG_CHANGE_SET = CHANGE_SET_SIZE / NR
```

## 11.8 Age e Weighted Age

Per ciascuna revisione viene calcolata l'età rispetto alla release:

```text
ageWeeks =
    (releaseDate - revisionDate) / 7
```

La metrica:

```text
AGE_WEEKS
```

rappresenta l'età della classe rispetto alla sua prima revisione effettiva.

`WEIGHTED_AGE_WEEKS` è invece una media pesata dell'età delle revisioni,
utilizzando come peso la quantità di LOC toccate dalla revisione:

```text
Σ(ageWeeks_revision * LOC_TOUCHED_revision)
------------------------------------------------
             Σ LOC_TOUCHED_revision
```

## 11.9 Identificatore della classe

La colonna:

```text
Class
```

mantiene il path completo del file Java, per esempio:

```text
openjpa-jdbc/src/main/java/org/apache/openjpa/jdbc/kernel/BrokerImpl.java
```

Non viene aggiunta una colonna separata contenente soltanto il simple class
name, poiché tale informazione è già derivabile dal path e sarebbe ridondante.

Il path completo è inoltre più robusto come identificatore, poiché permette
di distinguere classi omonime appartenenti a package o moduli differenti.

L'associazione con la release è disponibile direttamente attraverso:

```text
ReleaseIndex
Version
CommitId
```

---

# 12. Validazione delle metriche

Prima dell'esecuzione completa è stato effettuato un pilot sulla release:

```text
0.9.0
```

Risultato:

```text
Expected observations : 932
Successful            : 932
Failures              : 0
```

Successivamente il calcolo è stato eseguito su tutte le osservazioni
production.

Risultato finale:

```text
Expected observations : 12836
Successful            : 12836
Failures              : 0
Ignored zero-LOC revs : 14806
```

Sono stati verificati:

```text
osservazioni                   : 12836
valori mancanti                : 0
duplicati (Release, Class)     : 0
release                        : 12
un solo commit per release     : sì
```

Le 12.836 chiavi:

```text
(ReleaseIndex, Class)
```

coincidono esattamente con quelle dell'inventario production.

Le 932 osservazioni della release `0.9.0` contenute nel FULL coincidono
con quelle generate durante il pilot.

## 12.1 Invarianti verificati

Per tutte le osservazioni risultano soddisfatti:

```text
LOC > 0
NR > 0
NAUTH > 0
NAUTH <= NR

MAX_LOC_ADDED <= LOC_ADDED
MAX_CHANGE_SET <= CHANGE_SET_SIZE

AVG_LOC_ADDED = LOC_ADDED / NR
AVG_CHURN = CHURN / NR
AVG_CHANGE_SET = CHANGE_SET_SIZE / NR

AGE_WEEKS >= 0
WEIGHTED_AGE_WEEKS >= 0
WEIGHTED_AGE_WEEKS <= AGE_WEEKS
```

Inoltre, dalle metriche prodotte è possibile ricostruire:

```text
added   = (LOC_TOUCHED + CHURN) / 2
deleted = (LOC_TOUCHED - CHURN) / 2
```

Per tutte le 12.836 osservazioni:

```text
added == LOC_ADDED
deleted >= 0
deleted è intero
```

confermando la coerenza interna tra `LOC_TOUCHED`, `LOC_ADDED` e `CHURN`.

## 12.2 Revisioni zero-LOC

Le revisioni ignorate presentano la seguente distribuzione:

```text
classi con 0 revisioni ignorate : 1292
classi con 1 revisione ignorata : 8282
classi con 2 revisioni ignorata : 3262
```

Nessuna classe presenta più di due revisioni zero-LOC.

Durante il pilot è stato inoltre verificato manualmente almeno un commit
che modificava esclusivamente il file mode:

```text
100755 → 100644
```

senza alterare il contenuto del file.

La regolarità della distribuzione e la verifica manuale supportano la scelta
di non includere tali revisioni nelle metriche di modifica del sorgente.

---

# 13. Output disponibili

Sono attualmente disponibili:

```text
isw2/datasets/release_catalog_raw.csv
isw2/datasets/release_catalog.csv
isw2/datasets/java_class_inventory_raw.csv
isw2/datasets/java_class_inventory.csv
isw2/datasets/class_metrics.csv
```

Sono inoltre prodotti output diagnostici sotto:

```text
isw2/results/metrics/
```

tra cui i risultati dei pilot e gli eventuali failure report.

Tutti i dataset vengono generati automaticamente dall'analyzer e non vengono
modificati manualmente.

`class_metrics.csv` non rappresenta ancora il Dataset A finale, poiché
devono essere aggiunti:

```text
NFIX
NSmells
Bugginess
```

e, nella costruzione del dataset finale, l'identificativo del progetto.

---

# 14. Decisioni metodologiche

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

I file vengono letti direttamente dall'albero Git tramite `git ls-tree`,
senza modificare la working copy principale.

### Classi production

Vengono utilizzate solamente le osservazioni classificate come `PRODUCTION`.

Sono esclusi test, esempi, sorgenti generati, parser source e altri file
non appartenenti al codice production.

La classificazione non si basa esclusivamente sulla presenza di
`src/main/java`, ma considera anche il contesto del modulo e del percorso,
come nel caso di `osgi-itests`.

### Metriche storiche

Le metriche storiche vengono calcolate cumulativamente:

```text
from release 0
```

### Revisioni

`NR` indica il **number of revisions** e conta le revisioni che modificano
effettivamente il contenuto sorgente della classe.

Rename/move vengono seguiti per preservare la continuità della storia;
mode-only e rename puri senza modifica del contenuto non incrementano `NR`.

### Churn

Viene utilizzata la definizione:

```text
CHURN_revision = added - deleted
```

distinta da:

```text
LOC_TOUCHED_revision = added + deleted
```

### Identificatore della classe

Il path completo presente nella colonna `Class` viene mantenuto come
identificatore della classe. Non viene aggiunta una colonna ridondante
contenente soltanto il simple name.

### NFIX

`NFIX` viene rinviato alla fase di identificazione dei bug e dei fix commit,
in modo da non introdurre valori artificiali prima di avere evidenza JIRA/Git.

### Dataset

Gli output CSV vengono generati automaticamente e non vengono modificati
manualmente.

---

# 15. Stato Milestone 1

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
* [x] Implementazione del conteggio LOC
* [x] Implementazione delle metriche storiche Git
* [x] Gestione rename/move tramite `--follow`
* [x] Gestione delle revisioni zero-LOC
* [x] Pilot delle metriche sulla release `0.9.0`
* [x] Generazione FULL delle metriche
* [x] Validazione delle 12.836 righe di `class_metrics.csv`

## Prossimo step

* [ ] Calcolo di `NSmells`

Successivamente:

* [ ] recupero e analisi dei ticket bug;
* [ ] identificazione dei fix commit;
* [ ] calcolo di `NFIX`;
* [ ] SZZ;
* [ ] Proportion;
* [ ] determinazione della `Bugginess`;
* [ ] generazione del Dataset A.

Il presente documento verrà aggiornato durante l'avanzamento della milestone.

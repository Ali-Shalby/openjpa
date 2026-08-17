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

`NFIX` non viene calcolato insieme alle altre metriche in questo primo
passaggio, poiché dipende dall'identificazione dei defect ticket e dei relativi
fix commit. Il valore viene calcolato successivamente e mantenuto nel dataset
separato:

```text
isw2/datasets/nfix_metrics.csv
```

prima dell'assemblaggio finale del Dataset A.

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


# 13. Calcolo di NSmells

Dopo la validazione delle metriche di classe è stata calcolata la metrica:

```text
NSmells
```

per ciascuna osservazione:

```text
(ReleaseIndex, Class)
```

`NSmells` rappresenta il numero di issue SonarQube Cloud di tipo:

```text
CODE_SMELL
```

associate alla classe nello snapshot della specifica release.

Per evitare di includere issue storiche non più presenti nel codice della
release corrente, vengono considerate esclusivamente issue con:

```text
issueStatus = OPEN
type        = CODE_SMELL
```

Le issue `FIXED` vengono quindi escluse dal conteggio di `NSmells`, in quanto
rappresentano smell rilevati in analisi precedenti ma non più presenti nello
snapshot corrente.

## 13.1 Snapshot per SonarQube Cloud

Per ogni release selezionata viene costruito uno snapshot ausiliario contenente
esclusivamente le classi Java production già presenti nell'inventario
definitivo.

L'associazione tra i file dello snapshot e le osservazioni del dataset viene
mantenuta tramite:

```text
source-map.csv
```

che contiene, per ogni file analizzato:

```text
ReleaseIndex
Version
CommitId
OriginalClassPath
SonarPath
```

In questo modo ogni issue Sonar può essere ricondotta in modo deterministico
alla corrispondente osservazione:

```text
(ReleaseIndex, Class)
```

del dataset.

## 13.2 Estrazione e mapping degli smell

Per ogni release vengono recuperate le issue di maintainability nello stato
corrente e vengono selezionate quelle di tipo `CODE_SMELL`.

Ogni issue viene associata alla classe tramite il relativo `SonarPath`.

Per ogni classe viene quindi calcolato:

```text
NSmells =
    numero di CODE_SMELL OPEN associati alla classe
```

Le classi senza smell vengono mantenute nel dataset con:

```text
NSmells = 0
```

Per ciascuna release vengono prodotti:

```text
isw2/results/sonar/release-XX-<version>/smell_metrics.csv
isw2/results/sonar/release-XX-<version>/sonar_smell_evidence.csv
isw2/results/sonar/release-XX-<version>/summary.txt
```

`smell_metrics.csv` contiene una riga per classe, mentre
`sonar_smell_evidence.csv` mantiene l'evidenza delle singole issue utilizzate
nel conteggio.

## 13.3 Validazione per release

Il calcolo è stato prima verificato su una release pilota e successivamente
esteso alle 12 release selezionate.

Per ogni release sono stati verificati i seguenti invarianti:

```text
numero righe metriche = numero classi production
EvidenceRows = Sum(NSmells)
UnmatchedJavaCodeSmells = 0
NonJavaCodeSmells = 0
DuplicateMetricKeys = 0
NegativeCounts = 0
NonOpenEvidence = 0
ValidationPassed = True
```

Il controllo aggregato sulle 12 release ha prodotto:

```text
Release analizzate       : 12
Osservazioni              : 12836
Evidence Sonar            : 94308
Sum(NSmells)              : 94308
Release mancanti          : 0
Mismatch evidence         : 0
Smell Java non associati  : 0
Chiavi duplicate          : 0
NSmells negativi          : 0
Evidence non OPEN         : 0
Validazioni fallite       : 0
```

Il numero di osservazioni coincide esattamente con l'inventario production:

```text
12836
```

## 13.4 Aggregazione dei risultati Sonar

I risultati delle 12 release vengono aggregati nel file:

```text
isw2/datasets/sonar_smell_metrics.csv
```

con colonne:

```text
ReleaseIndex
Version
CommitId
Class
NSmells
```

Il file aggregato contiene:

```text
Righe totali       : 12836
Release            : 12
Chiavi duplicate   : 0
NSmells negativi   : 0
Sum(NSmells)       : 94308
```

Le chiavi:

```text
(ReleaseIndex, Class)
```

coincidono esattamente con quelle presenti in `class_metrics.csv`.

Sono inoltre stati verificati:

```text
classi mancanti in Sonar : 0
classi extra in Sonar    : 0
Version mismatch         : 0
CommitId mismatch        : 0
```

## 13.5 Join con le metriche di classe

`NSmells` viene aggiunto alle metriche di classe mediante una join 1:1 sulla
chiave:

```text
(ReleaseIndex, Class)
```

Il risultato viene salvato in:

```text
isw2/datasets/class_metrics_with_smells.csv
```

senza modificare il file originale:

```text
isw2/datasets/class_metrics.csv
```

La join finale ha prodotto:

```text
Input metric rows : 12836
Sonar rows        : 12836
Merged rows       : 12836
Missing matches   : 0
Metadata mismatch : 0
Extra Sonar rows  : 0
```

Il file `class_metrics_with_smells.csv` mantiene quindi tutte le metriche
precedentemente calcolate e aggiunge la colonna:

```text
NSmells
```

per tutte le 12.836 osservazioni.

# 14. Defect ticket e fix commit

Dopo il completamento delle metriche di classe e di `NSmells` è stata avviata
la parte di analisi dei defect necessaria per `NFIX`, SZZ e il successivo
labeling.

## 14.1 Catalogo RAW dei defect ticket

I ticket vengono recuperati dal progetto Apache JIRA di OpenJPA.

Sono considerati esclusivamente ticket che soddisfano i criteri:

```text
Issue Type  = Bug
Status      = Closed oppure Resolved
Resolution  = Fixed
```

L'output RAW è:

```text
isw2/datasets/defect_ticket_catalog_raw.csv
```

Risultati:

```text
Defect ticket validi          : 1132
Senza Affected Version/s      : 189
Senza Fix Version/s           : 71
Senza Resolution Date         : 0
IssueKey duplicate            : 0
```

Le informazioni mancanti in JIRA vengono mantenute come tali e non vengono
ricostruite artificialmente in questa fase.

## 14.2 Associazione ticket → fix commit

Per ciascun defect ticket viene ricercata la relativa chiave:

```text
OPENJPA-<numero>
```

nella storia Git raggiungibile dalla baseline del progetto.

Il matching utilizza la chiave JIRA esatta e considera solamente ticket già
presenti nel catalogo dei defect validati.

La prima ricerca ha prodotto:

```text
Candidate mappings              : 1658
Ticket con almeno un candidato  : 987
Commit candidati distinti       : 1626
```

I candidati vengono successivamente filtrati mantenendo solamente commit che
modificano almeno una classe Java classificata come:

```text
PRODUCTION
```

La classificazione dei file riutilizza lo stesso criterio già adottato per
l'inventario delle classi, tramite:

```text
JavaClassScopeClassifier
```

L'estrazione del classificatore dalla logica dell'inventario è stata verificata
rigenerando `java_class_inventory_raw.csv`: il file prima e dopo il refactoring
ha prodotto lo stesso SHA-256, confermando l'equivalenza completa della
classificazione.

Il catalogo definitivo dei fix è:

```text
isw2/datasets/fix_commit_catalog.csv
```

Risultati:

```text
Defect JIRA validi              : 1132
Candidate mappings RAW          : 1658
Fix mappings production         : 1222
Defect rappresentati            : 873
Fix commit distinti             : 1196

Duplicate mappings              : 0
Mappings senza ticket           : 0
Mappings senza production Java  : 0
Merge mappings                  : 0
Revert mappings                 : 0
IssueKey assente dal subject    : 0
```

Tutti i 1.222 mapping production contengono la relativa chiave JIRA
direttamente nel subject del commit.

## 14.3 Controllo temporale JIRA/Git

La relazione temporale tra ticket e fix commit viene mantenuta come
informazione diagnostica, ma non utilizzata come filtro automatico.

Distribuzione:

```text
Before ticket creation  : 5
Inside ticket lifecycle : 1102
After ticket resolution : 115
```

I controlli hanno mostrato che alcuni casi esterni all'intervallo
`CreatedDate → ResolutionDate` sono comunque associazioni JIRA/Git plausibili
o molto vicine temporalmente. Le date JIRA non vengono quindi usate per
eliminare automaticamente un fix commit già validato tramite chiave del ticket
e modifica a codice production.

---

# 15. Calcolo di NFIX

`NFIX` misura il numero cumulativo di **defect fixing distinti** che hanno
interessato una classe fino alla release osservata.

La definizione operativa adottata è:

```text
NFIX(class, release) =
    numero di IssueKey JIRA distinti
    associati a fix commit presenti nella storia della classe
    fino al commit della release
```

Di conseguenza, se lo stesso defect viene corretto attraverso più commit che
modificano la stessa classe, il defect contribuisce una sola unità:

```text
OPENJPA-X -> fix A -> Foo.java
OPENJPA-X -> fix B -> Foo.java

NFIX(Foo.java) += 1
```

mentre defect differenti contribuiscono separatamente.

## 15.1 Ricostruzione della storia della classe

Per ogni osservazione:

```text
(ReleaseIndex, Class)
```

la storia della classe viene ricostruita tramite:

```text
git -c diff.renameLimit=0 log <releaseCommit>
    --follow
    --no-merges
    --format=%H
    -- <Class>
```

`--follow` mantiene la continuità attraverso rename e move.

I commit della storia vengono intersecati con il catalogo dei fix commit; il
conteggio finale utilizza gli `IssueKey` distinti, non il numero di commit.

## 15.2 Pilot e validazione

Il calcolo è stato prima verificato sulla release:

```text
0.9.0
```

Risultati del pilot:

```text
NFIX observations       : 932
Observations NFIX > 0   : 5
Sum(NFIX)               : 5
Maximum NFIX            : 1
Duplicate keys          : 0
Negative NFIX           : 0
```

Per le cinque classi positive è stata eseguita una verifica indipendente delle
evidence Git/JIRA.

Risultato:

```text
Positive classes : 5
NFIX mismatches   : 0
```

## 15.3 FULL NFIX

Il calcolo è stato quindi esteso a tutte le osservazioni production.

Output:

```text
isw2/datasets/nfix_metrics.csv
```

Risultati:

```text
NFIX observations       : 12836
Observations NFIX > 0   : 3328
Observations NFIX = 0   : 9508
Sum(NFIX)               : 7523
Maximum NFIX            : 48
Duplicate keys          : 0
Negative NFIX           : 0
```

Sono presenti tutte le 12 release e la distribuzione delle righe per release
coincide esattamente con `java_class_inventory.csv`.

Per le classi che persistono con lo stesso path nelle release successive è
stata inoltre verificata la proprietà cumulativa:

```text
diminuzioni di NFIX : 0
```

Il massimo osservato è:

```text
NFIX = 48
```

per:

```text
openjpa-jdbc/src/main/java/org/apache/openjpa/jdbc/sql/DBDictionary.java
```

Il valore cresce progressivamente nella storia della classe e non deriva da un
singolo salto anomalo.

---

# 16. SZZ

Dopo la validazione del catalogo dei fix commit è stato applicato SZZ per
risalire ai commit candidati che hanno introdotto le linee successivamente
corrette.

## 16.1 Procedura

Per ogni fix commit production:

```text
fix commit
    ↓
parent commit
    ↓
git diff --find-renames --unified=0
    ↓
linee eliminate/modificate nei file Java PRODUCTION
    ↓
git blame sul parent
    ↓
bug-introducing commit
```

Le linee considerate sono quelle preesistenti che il fix elimina o modifica.

Un fix che aggiunge esclusivamente nuovo codice può quindi produrre
legittimamente:

```text
NO_EVIDENCE
```

poiché non esiste una linea precedente sulla quale applicare il blame.

L'implementazione principale utilizza:

```text
SzzEvidence
SzzAnalyzer
SzzCatalogGenerator
```

## 16.2 Pilot SZZ

Il pilot è stato eseguito su:

```text
OPENJPA-14
```

con fix commit:

```text
dcb47f0fe406c5d926c2d0e654b6690a15111cf1
```

Risultati:

```text
Evidence rows            : 4
Parent commits           : 1
Fixed production files   : 4
Blamed files             : 4
Bug-introducing commits  : 3
Total blamed lines       : 4
```

Una evidence è stata verificata manualmente su:

```text
openjpa-kernel/src/main/java/org/apache/openjpa/conf/ProductDerivations.java
```

Il fix modifica la linea 39 del parent e il `git blame` manuale restituisce:

```text
7a6d34e95f66e916e44b844b1ce6551ba8169539
```

che coincide esattamente con il BIC prodotto dall'analyzer.

Il ticket `OPENJPA-14` possiede inoltre un secondo fix commit production che
modifica `MetaDataRepository.java`; il caso ha confermato che `NFIX` deve
contare il defect una sola volta per classe, mentre SZZ deve analizzare
separatamente ciascun fix commit.

## 16.3 Safeguard sui fix dello stesso defect

Durante il primo FULL SZZ è stato rilevato un caso metodologicamente
importante: un commit già classificato come fix dello stesso `IssueKey` poteva
essere restituito da `git blame` come BIC di un fix successivo appartenente
alla stessa sequenza di correzione.

È stato quindi introdotto il seguente vincolo:

```text
un fix commit dello stesso IssueKey
non può essere utilizzato come BIC dello stesso defect
```

Il filtro ha escluso:

```text
Same-issue fix BIC rows skipped : 446
Same-issue blamed lines skipped : 2934
```

Dopo il filtro:

```text
Same-issue fix BIC remaining : 0
```

## 16.4 FULL SZZ

Gli output finali sono:

```text
isw2/datasets/szz_evidence.csv
isw2/results/szz/szz_fix_audit.csv
```

Risultati:

```text
Fix mappings processed          : 1222
Unique fix commits analyzed     : 1196
Cache reuses                    : 26

Mappings with evidence          : 943
Mappings without evidence       : 279

SZZ evidence rows               : 3748
Defects with evidence           : 766
Fix commits with evidence       : 921
Distinct fixed files            : 778
Bug-introducing commits         : 982
Total blamed lines              : 70830

Same-issue fix BIC rows skipped : 446
Same-issue blamed lines skipped : 2934
Same-issue fix BIC remaining    : 0
Validation failures             : 0
```

Sono stati verificati:

```text
Audit rows                      : 1222
Duplicati audit                 : 0
Duplicati evidence              : 0
SHA commit malformati           : 0
BlamedLineCount <= 0            : 0
File non Java production        : 0
Mismatch audit/evidence         : 0
Same-issue fix usato come BIC   : 0
```

L'audit mantiene anche i 279 mapping `NO_EVIDENCE`, evitando di confondere
l'assenza legittima di linee blameabili con un errore di elaborazione.

---

# 17. Proportion e intervalli effettivi

Dopo il FULL SZZ è stata determinata, per ciascun defect con evidence,
la porzione di storia del progetto nella quale il defect deve essere
considerato attivo.

La ricostruzione utilizza tre versioni concettuali:

```text
IV = Injected Version
OV = Opening Version
FV = Fixed Version
```

Per evitare di alterare le distanze temporali tra le versioni, questa fase
utilizza la timeline completa delle release JIRA rilasciate:

```text
Release universe : 42
```

e non soltanto le 36 release stabili utilizzate per selezionare il Dataset A.

Le 12 release del Dataset A corrispondono ai seguenti indici nella timeline RAW:

```text
1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 14, 18
```

---

## 17.1 Proportion Total

Per i defect per i quali sono noti e temporalmente coerenti `IV`, `OV` e `FV`
viene calcolato:

```text
P = (FV - IV) / (FV - OV)
```

con il caso:

```text
OV == FV
```

gestito utilizzando distanza pari a `1`, evitando la divisione per zero.

Sono considerati utilizzabili per il calcolo di `P_TOTAL` soltanto lifecycle
che rispettano:

```text
IV <= OV <= FV
IV < FV
```

Risultati:

```text
Usable defects for P_TOTAL : 621
P_TOTAL                    : 1.9688220484114205
P median                   : 1.0
P minimum                  : 1.0
P maximum                  : 25.0
```

Nel calcolo viene mantenuta la precisione completa di `P_TOTAL`; il valore
arrotondato è utilizzato soltanto a scopo descrittivo.

---

## 17.2 Determinazione della Fixed Version effettiva

Per i 766 defect che possiedono evidence SZZ, la `Fix Version` JIRA viene
utilizzata come fonte primaria della `FV`.

La coerenza viene verificata rispetto ai fix commit che producono evidence
SZZ: un fix è considerato allineato a una release quando il relativo commit
è raggiungibile dallo snapshot Git associato a quella release.

Quando non esiste una `Fix Version` JIRA utilizzabile, oppure quando la
`Fix Version` dichiarata non contiene nessuno dei fix commit SZZ del defect,
viene utilizzata come fallback la prima release RAW il cui snapshot contiene
almeno uno dei fix commit rilevanti.

Le sorgenti finali della `FV` sono:

```text
JIRA_FIX_VERSION
GIT_CONTAINMENT_NO_JIRA_FV
GIT_CONTAINMENT_JIRA_MISMATCH
```

Risultati:

```text
Defect con SZZ                         : 766

FV da JIRA                             : 696
FV da Git - nessuna FV JIRA usabile   : 36
FV da Git - mismatch JIRA/SZZ          : 34
FV unresolved                          : 0

Defect con ZERO evidence allineata     : 0
Defect con PARTIAL alignment           : 14
Defect con ALL fixes aligned           : 752

SZZ rows prima dell'allineamento       : 3748
SZZ rows dopo l'allineamento           : 3700
```

Nei 14 casi di allineamento parziale la `FV` effettiva rimane quella
determinata dalla procedura precedente, ma vengono utilizzate per quel
lifecycle soltanto le evidence appartenenti ai fix commit già contenuti
nello snapshot della `FV`.

In questo modo eventuali ulteriori fix dello stesso defect, entrati in
release successive, non vengono anticipati artificialmente.

---

## 17.3 Determinazione della Injected Version effettiva

L'`Opening Version` viene ricavata dalla data di creazione del ticket:

```text
OV =
    ultima release RAW con
    ReleaseDate <= CreatedDate
```

Quando JIRA fornisce una `Affected Version`, viene utilizzata come `IV`
osservata soltanto se il lifecycle è coerente:

```text
ObservedIV <= OV <= EffectiveFV
ObservedIV < EffectiveFV
```

Se questa condizione non è soddisfatta, oppure se manca una `Affected Version`
utilizzabile, viene applicato `Proportion Total`.

La stima viene calcolata come:

```text
distance = EffectiveFV - OV
```

con:

```text
distance = 1
```

quando `EffectiveFV == OV`.

Successivamente:

```text
LAV = EffectiveFV - distance * P_TOTAL
EffectiveIV = ceil(LAV)
```

Se il risultato precede l'inizio della storia osservata:

```text
EffectiveIV = 1
```

Per i ticket creati prima della prima release disponibile viene invece usata
esplicitamente la sorgente:

```text
PRE_FIRST_RELEASE
```

con:

```text
EffectiveIV = 1
```

senza classificare il caso come stima tramite Proportion.

Distribuzione finale sui 766 defect con SZZ:

```text
JIRA_AFFECTED_VERSION : 476
PROPORTION_TOTAL      : 284
PRE_FIRST_RELEASE     : 6
UNRESOLVED            : 0
```

Ulteriori controlli:

```text
Defect senza OV                    : 6
Observed IV incoerenti             : 174
Proportion IV clamped a release 1  : 11
```

---

## 17.4 Intervallo di vita del defect

Una volta determinati `EffectiveIV` ed `EffectiveFV`, il defect viene
considerato attivo nell'intervallo:

```text
[EffectiveIV, EffectiveFV)
```

quindi:

```text
EffectiveIV inclusa
EffectiveFV esclusa
```

La `FV` non è buggy perché rappresenta la release nella quale il fix è già
presente.

Tra i 766 defect con SZZ:

```text
Lifecycle che intersecano Dataset A : 461
Lifecycle esterni a Dataset A       : 305
```

I 461 lifecycle che intersecano almeno una delle 12 release costituiscono
la base temporale del labeling finale.

---

# 18. Determinazione della Bugginess

La `Bugginess` viene determinata combinando tre informazioni:

```text
SZZ                  -> identifica le classi coinvolte nel defect
Effective IV / FV    -> determina le release nelle quali il defect è attivo
Production inventory -> verifica che la classe esista nello snapshot
```

Per una release del Dataset A una classe può essere marcata:

```text
BUGGY = YES
```

quando esiste almeno un defect che:

```text
EffectiveIV <= RawReleaseIndex < EffectiveFV
```

e la classe associata all'evidence SZZ è presente tra le classi Java
`PRODUCTION` dello snapshot della release.

Il `BugIntroducingCommitId` rimane evidence utile per SZZ, ma la sua
raggiungibilità dalla release non viene utilizzata come un secondo filtro
temporale del lifecycle.

La temporalità del labeling è infatti determinata da `IV`, `OV`, `FV` e
`Proportion`, mentre SZZ identifica le classi associate al defect.

---

## 18.1 Mapping del path della classe

Il mapping utilizza come riferimento primario:

```text
BlamedFilePath
```

prodotto da SZZ.

Per ciascuna coppia defect/release appartenente al lifecycle viene prima
verificata la presenza esatta del path nell'inventario production.

Questo primo mapping ha prodotto:

```text
Candidate evidence-release pairs : 6833
Unique exact BUGGY pairs          : 2007
Defect con exact mapping          : 458
Defect senza exact mapping        : 3
```

I tre defect senza alcun exact mapping nel Dataset A sono stati verificati
direttamente sulla storia Git:

```text
OPENJPA-304
OPENJPA-311
OPENJPA-76
```

Nei relativi snapshot le classi indicate dall'evidence non erano ancora
presenti; non è stato quindi introdotto alcun mapping artificiale.

Non viene utilizzata una corrispondenza basata soltanto sul nome del file e
non viene accettata automaticamente una lineage ricostruita per similarità.

Un path storico alternativo viene accettato soltanto quando Git fornisce
un'evidenza esplicita di rename che collega direttamente il path storico al
`BlamedFilePath`, e il path storico appartiene all'inventario production
della release.

La verifica finale dei rename ha prodotto:

```text
Exact BUGGY pairs                          : 2007
Raw lineage candidate rows                 : 70
Direct explicit rename rows                : 4
Direct rename rows in production inventory : 4
Unique direct rename pairs                 : 4
New pairs rispetto agli exact              : 3

FINAL exact + direct rename BUGGY pairs    : 2010
```

I quattro mapping validati appartengono a `OPENJPA-896` e riguardano il rename:

```text
openjpa-slice/src/main/java/org/apache/openjpa/slice/jdbc/DistributedStoreManager.java
        ->
openjpa-slice/src/main/java/org/apache/openjpa/slice/jdbc/DistributedJDBCStoreManager.java
```

nelle release 7, 8, 9 e 10.

La coppia della release 9 era già supportata da un mapping exact; per questo
i quattro mapping di rename aggiungono soltanto tre nuove osservazioni al
set finale.

---

## 18.2 Labeling finale

L'output finale della fase è:

```text
isw2/datasets/bugginess_labels.csv
```

Colonne:

```text
ReleaseIndex
Version
CommitId
Class
BUGGY
```

Risultati complessivi:

```text
Observations : 12836
BUGGY=YES    : 2010
BUGGY=NO     : 10826
Buggy rate   : 15.66%
```

Distribuzione per release:

| Release | Versione | Classi | BUGGY=YES | BUGGY=NO | Buggy rate |
| ------: | -------- | -----: | --------: | -------: | ---------: |
| 1 | 0.9.0 | 932 | 63 | 869 | 6.76% |
| 2 | 0.9.6 | 949 | 100 | 849 | 10.54% |
| 3 | 0.9.7 | 948 | 158 | 790 | 16.67% |
| 4 | 1.0.0 | 996 | 123 | 873 | 12.35% |
| 5 | 1.0.1 | 1029 | 113 | 916 | 10.98% |
| 6 | 1.0.2 | 1058 | 136 | 922 | 12.85% |
| 7 | 1.1.0 | 1045 | 232 | 813 | 22.20% |
| 8 | 1.0.3 | 1050 | 234 | 816 | 22.29% |
| 9 | 1.2.0 | 1051 | 210 | 841 | 19.98% |
| 10 | 1.2.1 | 1185 | 350 | 835 | 29.54% |
| 11 | 1.2.2 | 1300 | 159 | 1141 | 12.23% |
| 12 | 2.0.0 | 1293 | 132 | 1161 | 10.21% |

Sono stati verificati:

```text
Righe finali                           : 12836
Chiavi (ReleaseIndex, Class) duplicate : 0
Classi extra rispetto all'inventario   : 0
Classi mancanti rispetto all'inventario: 0
BUGGY senza defect di supporto         : 0
```

---

## 18.3 Validazione dell'andamento tra le release

È stato eseguito un controllo aggiuntivo sull'andamento delle classi buggy,
in particolare sulla diminuzione osservata tra:

```text
R10 = 1.2.1
R11 = 1.2.2
```

Risultati:

```text
BUGGY R10 : 350
BUGGY R11 : 159

Retained R10 -> R11 : 100
Removed             : 250
Added               : 59
```

Per i 250 path che cessano di essere buggy è stato verificato il defect di
supporto:

```text
Removed supportati soltanto da defect usciti : 250
Removed ancora supportati da defect carried   : 0
Removed senza support mapping                  : 0
```

La diminuzione non deriva quindi da una perdita arbitraria di mapping:
le classi escono dal set BUGGY perché i defect che le supportavano terminano
prima della release successiva del Dataset A.

Tra R10 e R11:

```text
Active defects R10 : 96
Active defects R11 : 108
Carried            : 40
Entered            : 68
Exited             : 56
```

Poiché R10 e R11 corrispondono rispettivamente agli indici RAW 11 e 14,
49 dei 56 defect usciti hanno una `EffectiveFV` collocata in una release RAW
intermedia non appartenente al Dataset A.

---

## 18.4 Caso OPENJPA-896

Il principale contributore al picco della release 10 è:

```text
OPENJPA-896
```

con lifecycle:

```text
EffectiveIV = 7
EffectiveFV = 12
IV source   = JIRA_AFFECTED_VERSION
FV source   = JIRA_FIX_VERSION
```

Il defect contribuisce:

```text
R7  : 132 classi
R8  : 134 classi
R9  : 135 classi
R10 : 246 classi
```

e quindi da solo supporta 246 delle 350 osservazioni `BUGGY=YES` della
release 10.

Il relativo fix commit è:

```text
90b84c5d73db4d0d7fa3e5520358912961b59c43
```

con subject:

```text
OPENJPA-896. Setting eol-style:native and removing windows eol characters from source files.
```

L'audit ha confermato la natura trasversale del defect:

```text
File modificati                  : 1544
Java production                  : 264
Java test                        : 1280
Changed lines - diff normale     : 382166
Changed lines ignorando EOL      : 0

SZZ evidence rows                : 686
Distinct BIC                     : 244
SZZ blamed lines                 : 52128
```

Il fatto che il fix riguardi i caratteri di fine riga non viene utilizzato
come criterio di esclusione.

`OPENJPA-896` è mantenuto nell'analisi come qualsiasi altro defect valido:
la pipeline non applica filtri basati sulla natura funzionale, di
formattazione o di line-ending del problema.

L'audit EOL/whitespace è stato utilizzato esclusivamente per spiegare
l'elevato numero di classi coinvolte e non modifica `szz_evidence.csv` né
`bugginess_labels.csv`.

Il picco di R10 è quindi interpretabile come effetto di un defect
cross-cutting che interessa contemporaneamente un numero elevato di file,
non come un errore del conteggio finale.

---

# 19. Output disponibili

Gli output principali della Milestone 1 disponibili a questo punto sono:

```text
isw2/datasets/release_catalog_raw.csv
isw2/datasets/release_catalog.csv
isw2/datasets/java_class_inventory_raw.csv
isw2/datasets/java_class_inventory.csv
isw2/datasets/class_metrics.csv
isw2/datasets/sonar_smell_metrics.csv
isw2/datasets/class_metrics_with_smells.csv
isw2/datasets/defect_ticket_catalog_raw.csv
isw2/datasets/fix_commit_catalog.csv
isw2/datasets/nfix_metrics.csv
isw2/datasets/szz_evidence.csv
isw2/datasets/bugginess_labels.csv
```

Gli output diagnostici sono organizzati principalmente sotto:

```text
isw2/results/metrics/
isw2/results/sonar/
isw2/results/szz/
isw2/results/labeling/
```

Tra i principali audit della fase defect/labeling sono presenti:

```text
isw2/results/szz/szz_fix_audit.csv

isw2/results/labeling/bugginess_path_presence_diagnostic.csv
isw2/results/labeling/bugginess_direct_rename_valid.csv
isw2/results/labeling/bugginess_direct_rename_final_check.txt
isw2/results/labeling/bugginess_trend_diagnostic.txt
isw2/results/labeling/openjpa_896_audit.txt
```

Tutti i dataset vengono generati automaticamente dall'analyzer o dagli
script di estrazione, audit e validazione e non vengono corretti manualmente.

Il Dataset A finale non è ancora assemblato: restano da unire le metriche di
classe, `NSmells`, `NFIX` e `Bugginess` e da eseguire la validazione della
join finale.

---

# 20. Decisioni metodologiche

Le principali decisioni adottate nella Milestone 1 sono:

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

### Timeline per Proportion

La selezione del Dataset A utilizza le release stabili, mentre il calcolo
temporale di `Proportion`, `IV`, `OV` e `FV` utilizza tutte le 42 release JIRA
rilasciate, preservando le reali distanze tra le versioni.

### Identificazione dei file Java

I file vengono letti direttamente dall'albero Git tramite `git ls-tree`,
senza modificare la working copy principale.

### Classi production

Vengono utilizzate solamente le osservazioni classificate come `PRODUCTION`.

Sono esclusi test, esempi, sorgenti generati, parser source e altri file non
appartenenti al codice production.

La stessa classificazione viene riutilizzata per filtrare i fix commit
rilevanti per `NFIX` e SZZ.

### Metriche storiche

Le metriche storiche vengono calcolate cumulativamente:

```text
from release 0
```

### Revisioni

`NR` conta soltanto le revisioni che modificano effettivamente il contenuto
sorgente della classe.

Rename/move vengono seguiti per preservare la continuità della storia;
mode-only e rename puri senza modifica testuale non incrementano `NR`.

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
identificatore della classe.

### NSmells

`NSmells` rappresenta il numero di `CODE_SMELL` `OPEN` presenti nella classe
nello snapshot della specifica release.

Le issue `FIXED` non vengono conteggiate nella release corrente.

### Defect e fix commit

Sono considerati defect JIRA i ticket:

```text
Issue Type = Bug
Status     = Closed oppure Resolved
Resolution = Fixed
```

L'associazione a Git richiede la chiave `OPENJPA-*` nel subject del commit e
almeno una modifica a Java `PRODUCTION`.

Le date JIRA sono utilizzate come diagnostica e non come criterio rigido per
eliminare un fix commit già validato tramite ticket e modifica production.

### NFIX

`NFIX` rappresenta il numero cumulativo di `IssueKey` defect fixing distinti
che hanno interessato la classe fino alla release osservata.

Più fix commit dello stesso defect sulla stessa classe contribuiscono una
sola unità.

### SZZ

SZZ utilizza le linee eliminate/modificate dal fix e applica `git blame`
sul parent commit.

I fix add-only possono produrre legittimamente `NO_EVIDENCE`.

Un fix commit dello stesso `IssueKey` viene escluso dai possibili BIC dello
stesso defect.

Non viene applicato un filtro che escluda categorie di bug in base alla
natura del problema.

### Fixed Version

La `Fix Version` JIRA è la fonte primaria.

Git containment viene utilizzato come fallback quando manca una FV JIRA
utilizzabile oppure quando la FV dichiarata non contiene alcun fix commit SZZ
del defect.

### Injected Version

La `Affected Version` JIRA viene utilizzata quando forma un lifecycle
temporalmente coerente con `OV` e `FV`.

Negli altri casi viene utilizzato `Proportion Total`.

I ticket anteriori alla prima release vengono gestiti separatamente tramite
`PRE_FIRST_RELEASE`.

### Intervallo BUGGY

Un defect è attivo nell'intervallo:

```text
[EffectiveIV, EffectiveFV)
```

La release di fix è quindi esclusa.

### Mapping della classe

Il `BlamedFilePath` SZZ viene associato in modo exact all'inventario della
release.

Un path alternativo viene accettato soltanto in presenza di un rename Git
diretto e verificato verso una classe production presente nello snapshot.

Non vengono usati matching basati soltanto sul simple filename.

### Bug-introducing commit

La presenza del BIC nella storia della release viene mantenuta come
informazione diagnostica, ma non viene utilizzata come secondo filtro
temporale dopo la determinazione di `EffectiveIV` ed `EffectiveFV`.

### Tipologia del defect

Non vengono esclusi defect sulla base della loro natura.

Problemi cross-cutting, di configurazione, formattazione o line-ending
rimangono defect validi quando soddisfano i criteri JIRA adottati.

### Dataset

Gli output CSV vengono generati automaticamente e non vengono modificati
manualmente.

---

# 21. Stato Milestone 1

## Completato

* [x] Recupero release da JIRA
* [x] Generazione catalogo RAW delle release
* [x] Filtro release stabili
* [x] Verifica tag Git
* [x] Associazione release → commit
* [x] Validazione temporale dei commit
* [x] Selezione delle 12 release
* [x] Inventario e classificazione dei sorgenti Java
* [x] Generazione e validazione delle 12.836 osservazioni production
* [x] Calcolo e validazione delle metriche di classe
* [x] Calcolo e validazione di `NSmells`
* [x] Recupero e validazione dei defect ticket JIRA
* [x] Identificazione e validazione dei fix commit production
* [x] Calcolo e validazione di `NFIX`
* [x] Pilot e FULL SZZ
* [x] Safeguard sui fix commit dello stesso defect
* [x] Validazione finale di `szz_evidence.csv`
* [x] Calcolo di `Proportion Total`
* [x] Allineamento `FV` / fix commit / evidence SZZ
* [x] Determinazione di `EffectiveIV` ed `EffectiveFV`
* [x] Ricostruzione dei lifecycle `[IV,FV)`
* [x] Verifica dei lifecycle che intersecano Dataset A
* [x] Mapping exact delle classi buggy
* [x] Audit dei path SZZ assenti dagli snapshot
* [x] Validazione dei rename Git diretti
* [x] Generazione di `bugginess_labels.csv`
* [x] Validazione delle 12.836 label finali
* [x] Audit dell'andamento BUGGY tra le release
* [x] Verifica mirata del caso `OPENJPA-896`

## Prossimo step

* [ ] Join di `class_metrics_with_smells.csv`, `nfix_metrics.csv` e `bugginess_labels.csv`
* [ ] Aggiunta dell'identificativo `Project`
* [ ] Assemblaggio del Dataset A
* [ ] Validazione finale delle 12.836 osservazioni del Dataset A

Successivamente:

* [ ] Milestone 2 – Classification

Il presente documento verrà aggiornato con l'assemblaggio e la validazione
finale del Dataset A.

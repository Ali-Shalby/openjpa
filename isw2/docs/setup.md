# Setup e validazione della baseline

## 1. Repository

Il progetto è sviluppato a partire da un fork del repository ufficiale:

```text
https://github.com/apache/openjpa
```

Fork utilizzato:

```text
https://github.com/Ali-Shalby/openjpa
```

Sono configurati due remote:

```text
origin   → Ali-Shalby/openjpa
upstream → apache/openjpa
```

---

## 2. Baseline

La release scelta è:

```text
Apache OpenJPA 4.1.1
```

Tag:

```text
4.1.1
```

Commit della release:

```text
9d2f5f8c35691c3542606bea1d23b916bcbeda8d
```

Sono stati creati due branch:

```text
baseline-4.1.1
isw2-project
```

### `baseline-4.1.1`

Rappresenta la copia immutabile della release originale.

### `isw2-project`

Contiene tutto lo sviluppo relativo al progetto universitario.

---

## 3. Working copy

La working copy utilizzata è:

```text
C:\ISW2\openjpa
```

È stato scelto un percorso breve e non sincronizzato con OneDrive per ridurre possibili problemi con:

* file lock;
* percorsi Windows lunghi;
* file generati da Maven;
* strumenti esterni utilizzati nelle successive milestone.

---

## 4. Ambiente Java

OpenJPA 4.1.1 viene mantenuto compatibile con l'ambiente previsto dalla configurazione upstream.

Per la validazione della baseline viene utilizzato:

```text
Zulu JDK 11
```

Il nostro analyzer utilizza invece:

```text
JDK 21
```

L'analyzer è un progetto Maven indipendente e non modifica il reactor Maven originale di OpenJPA.

---

## 5. Problema LF / CRLF

Durante la prima build su Windows, Checkstyle ha segnalato numerose violazioni relative ai terminatori di riga.

Esempio:

```text
Expected line ending for file is LF(\n), but CRLF(\r\n) is detected.
```

L'analisi con:

```bash
git ls-files --eol
```

ha mostrato conversioni del tipo:

```text
i/lf → w/crlf
```

causate dalla configurazione Git:

```text
core.autocrlf=true
```

Per la working copy definitiva sono state utilizzate le impostazioni locali:

```text
core.autocrlf=false
core.eol=lf
```

La correzione riguarda esclusivamente l'ambiente locale e non modifica il contenuto della baseline.

---

## 6. Build della baseline

È stata eseguita:

```bash
mvn clean install -DskipTests
```

Risultato:

```text
BUILD SUCCESS
```

L'intero reactor Maven OpenJPA è stato compilato correttamente.

---

## 7. Test locali

L'esecuzione completa della suite originale su Windows ha evidenziato alcuni failure nel modulo:

```text
openjpa-persistence-jdbc
```

In una delle esecuzioni:

```text
Tests run: 3202
Failures: 14
Errors: 0
```

Sono stati inoltre eseguiti test mirati utilizzando Zulu JDK 11.

Alcuni failure continuavano a verificarsi nell'ambiente Windows.

Non è stata apportata alcuna modifica al codice o ai test originali per forzarne il superamento.

---

## 8. GitHub Actions

Per distinguere eventuali problemi della baseline da differenze dell'ambiente Windows è stata utilizzata la CI originale del progetto OpenJPA.

È stato creato un commit vuoto sul branch:

```text
isw2-project
```

per attivare il workflow GitHub Actions.

La CI ufficiale ha completato correttamente la build:

```text
SUCCESS
```

La baseline OpenJPA 4.1.1 viene pertanto considerata valida e riproducibile nell'ambiente CI upstream.

---

## 9. Analyzer ISW2

È stato creato un progetto Maven separato:

```text
isw2/analyzer/
```

con:

```text
isw2/analyzer/pom.xml
```

Il modulo utilizza Java 21.

Questa scelta permette di:

* non alterare il reactor Maven OpenJPA;
* isolare gli strumenti di analisi dal sistema studiato;
* utilizzare API Java moderne;
* eseguire indipendentemente le diverse attività delle milestone.

Il package principale segue la struttura:

```text
it.uniroma2.isw2.openjpa
```

---

## 10. Stato del setup

* [x] Repository forkato
* [x] `origin` configurato
* [x] `upstream` configurato
* [x] Baseline 4.1.1 fissata
* [x] Branch baseline creato
* [x] Branch di sviluppo creato
* [x] Problema LF/CRLF risolto
* [x] Build locale completata
* [x] Test locali analizzati
* [x] GitHub Actions validata
* [x] Analyzer Maven creato

Il setup iniziale è considerato completato.

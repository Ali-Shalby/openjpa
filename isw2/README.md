# Progetto ISW2 – Apache OpenJPA

Progetto sviluppato per il corso di **Ingegneria del Software 2**
Università degli Studi di Roma Tor Vergata – A.A. 2025/2026.

Il progetto open-source assegnato è **Apache OpenJPA**.

Repository upstream:

`https://github.com/apache/openjpa`

Tutto il materiale sviluppato specificamente per il progetto universitario è raccolto nella directory:

```text
isw2/
```

Il codice e la documentazione originali di Apache OpenJPA vengono mantenuti separati dal materiale ISW2.

---

## Baseline

La baseline scelta è:

```text
Apache OpenJPA 4.1.1
```

Tag Git:

```text
4.1.1
```

Branch principali:

```text
baseline-4.1.1   → copia immutabile della baseline
isw2-project     → sviluppo del progetto ISW2
```

La baseline è stata verificata sia tramite build locale sia tramite la CI GitHub Actions originale di OpenJPA.

Per maggiori dettagli:

[`docs/setup.md`](docs/setup.md)

---

## Struttura del progetto

```text
isw2/
├── README.md
├── analyzer/
├── datasets/
├── docs/
└── results/
```

### `analyzer/`

Progetto Maven indipendente contenente gli strumenti sviluppati per automatizzare le attività delle milestone.

### `datasets/`

Dataset e cataloghi generati automaticamente.

### `results/`

Risultati sperimentali delle analisi.

### `docs/`

Documentazione metodologica dettagliata.

---

## Parte Falessi

Il progetto comprende:

1. **Milestone 1 – Dataset Creation**
2. **Milestone 2 – Classification**
3. **Milestone 3 – What-if Analysis**
4. **Milestone 4 – Automated Refactoring**

La granularità utilizzata è la **classe Java**.

---

## Parte De Angelis

La parte di Software Testing verrà svolta su due classi OpenJPA e comprenderà:

* Category Partition;
* test manuali;
* random testing;
* test generati tramite LLM;
* generazione guidata dalla coverage;
* code coverage;
* mutation testing;
* miglioramento delle suite;
* reliability;
* confronto dei test sulle versioni refactored.

---

## Tecnologie principali

| Tecnologia      | Utilizzo                             |
| --------------- | ------------------------------------ |
| Java            | Analyzer e testing                   |
| Maven           | Build                                |
| Git / GitHub    | Versionamento e analisi della storia |
| GitHub Actions  | Continuous Integration               |
| Apache JIRA     | Release e ticket                     |
| Weka            | Machine Learning                     |
| SonarQube Cloud | Code smell                           |
| JUnit           | Testing                              |
| JaCoCo          | Coverage                             |
| PIT             | Mutation testing                     |
| Randoop         | Random test generation               |
| EvoSuite        | Coverage-based generation            |
| LLM / Copilot   | Test generation e refactoring        |

Gli strumenti vengono documentati nel dettaglio quando effettivamente introdotti.

---

## Stato di avanzamento

### Completato

* [x] Fork di Apache OpenJPA
* [x] Configurazione `origin` e `upstream`
* [x] Selezione della baseline OpenJPA 4.1.1
* [x] Creazione branch `baseline-4.1.1`
* [x] Creazione branch `isw2-project`
* [x] Validazione della baseline
* [x] Configurazione GitHub Actions
* [x] Creazione del modulo `isw2/analyzer`
* [x] Recupero delle release OpenJPA da JIRA
* [x] Generazione del catalogo RAW delle release
* [x] Identificazione delle release stabili
* [x] Associazione release → commit Git
* [x] Selezione delle release da utilizzare per il Dataset A

### In corso

* [ ] Identificazione delle classi Java delle release selezionate
* [ ] Definizione delle regole di inclusione/esclusione
* [ ] Calcolo delle metriche di classe
* [ ] Calcolo di `NSmells`
* [ ] Determinazione della `Bugginess`
* [ ] Generazione del Dataset A

### Successivamente

* [ ] Milestone 2 – Classification
* [ ] Milestone 3 – What-if Analysis
* [ ] Selezione delle due classi OpenJPA
* [ ] Software Testing – De Angelis
* [ ] Milestone 4 – Automated Refactoring

---

## Documentazione

* [Setup e baseline](docs/setup.md)
* [Milestone 1 – Dataset Creation](docs/milestone1.md)

La documentazione verrà aggiornata progressivamente durante lo sviluppo.

---

## Principi metodologici

Durante il progetto:

1. il materiale ufficiale del corso rappresenta la fonte primaria;
2. ogni fase viene verificata prima di procedere alla successiva;
3. i risultati vengono generati automaticamente quando possibile;
4. i dataset generati non vengono corretti manualmente;
5. le scelte metodologiche vengono documentate e motivate;
6. il branch `baseline-4.1.1` rimane immutabile;
7. repository di altri studenti vengono utilizzati soltanto come riferimento implementativo.

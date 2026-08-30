# ListIteratorWrapper C1 — Confronto delle suite automatiche

## Scopo

Questa sezione confronta le tre suite automatiche generate sulla variante **C1** di
`org.apache.openjpa.lib.util.collections.ListIteratorWrapper`:

- **T_RND** — Randoop
- **T_ES** — EvoSuite
- **T_LLM** — LLM

Il confronto è svolto a parità di cardinalità, con **N = 12 test per suite**, e considera:

- coverage;
- mutation score e test strength;
- dimensione della suite;
- codice di supporto/scaffolding;
- naming;
- densità di assertion;
- code smell Sonar;
- chiarezza e manutenibilità.

---

## Risultati C1

| Metrica | T_RND | T_ES | T_LLM |
|---|---:|---:|---:|
| Test | 12 | 12 | 12 |
| Line coverage | 58.82% | 88.24% | **100.00%** |
| Branch coverage | 40.48% | 88.10% | **97.62%** |
| Method coverage | 91.67% | 91.67% | **100.00%** |
| Mutanti totali | 51 | 51 | 51 |
| Mutanti uccisi | 9 | 33 | **49** |
| Mutanti sopravvissuti | 19 | 15 | **2** |
| Mutanti non coperti | 23 | 3 | **0** |
| Mutation score | 17.65% | 64.71% | **96.08%** |
| Test strength | 32.14% | 68.75% | **96.08%** |
| Logic LOC | 695 | 191 | **289** |
| Support LOC | 0 | 101 | **0** |
| Total LOC | 695 | 292 | **289** |
| Assertion-like statements | 231 | 16 | 79 |
| Assertion/test | 19.25 | 1.33 | 6.58 |
| Nomi opachi | 12 | 12 | **0** |
| Nomi descrittivi | 0% | 0% | **100%** |
| Sonar code smell | 154 | **0** | 1 |
| Smell nella logica | 154 | **0** | 1 |
| Smell nel supporto | 0 | **0** | 0 |
| Regole Sonar distinte | 7 | **0** | 1 |

Per T_LLM è stata inoltre verificata la stabilità tramite **5 esecuzioni consecutive**, tutte concluse con esito positivo.

---

## Analisi T_RND

Randoop raggiunge una line coverage del 58.82%, ma la branch coverage resta al 40.48%.
Il mutation score è pari al 17.65% e il test strength al 32.14%.

La suite è strutturalmente molto grande:

- 695 LOC;
- 231 assertion-like statements;
- 19.25 assertion per test;
- 12/12 nomi opachi.

SonarQube Cloud rileva **154 code smell**, tutti nella logica della suite, distribuiti su 7 regole.

### Regole Sonar — T_RND

| Regola | Issue | Maintainability severity | Clean Code attribute | Categoria |
|---|---:|---|---|---|
| `java:S1220` | 2 | LOW | MODULAR | ADAPTABLE |
| `java:S1481` | 13 | LOW | CLEAR | INTENTIONAL |
| `java:S1854` | 13 | MEDIUM | LOGICAL | INTENTIONAL |
| `java:S2133` | 1 | MEDIUM | EFFICIENT | INTENTIONAL |
| `java:S3415` | 18 | MEDIUM | LOGICAL | INTENTIONAL |
| `java:S3577` | 2 | LOW | IDENTIFIABLE | CONSISTENT |
| `java:S5785` | 105 | MEDIUM | CLEAR | INTENTIONAL |

La quantità di assertion è quindi molto elevata, ma non si traduce in un livello comparabile di mutation adequacy.

---

## Analisi T_ES

EvoSuite presenta un miglioramento netto rispetto a Randoop:

- line coverage: 88.24%;
- branch coverage: 88.10%;
- method coverage: 91.67%;
- mutation score: 64.71%;
- test strength: 68.75%.

La suite contiene:

- 191 LOC di logica;
- 101 LOC di scaffolding;
- 292 LOC complessive.

I 12 test hanno naming generato/opaco.

L'analisi SonarQube Cloud non rileva alcuna issue di maintainability:

- 0 code smell nella logica;
- 0 code smell nel supporto;
- 0 regole Sonar coinvolte.

EvoSuite è quindi la suite più pulita secondo Sonar per C1, pur mantenendo la dipendenza dallo scaffolding.

---

## Analisi T_LLM

T_LLM ottiene i valori più elevati di adequacy:

- line coverage: 100%;
- branch coverage: 97.62%;
- method coverage: 100%;
- mutation score: 96.08%;
- test strength: 96.08%.

Su 51 mutanti:

- 49 sono uccisi;
- 2 sopravvivono;
- 0 risultano non coperti.

La suite contiene 289 LOC, senza scaffolding dedicato.

Tutti i 12 test hanno nomi descrittivi.

SonarQube Cloud rileva **1 code smell**, nella logica:

| Regola | Issue | Maintainability severity | Clean Code attribute | Categoria |
|---|---:|---|---|---|
| `java:S1598` | 1 | HIGH | CONVENTIONAL | CONSISTENT |

T_LLM non è quindi completamente Sonar-clean su C1, ma presenta una sola issue a fronte dei migliori risultati di adequacy e di una struttura compatta e leggibile.

---

## Confronto di chiarezza

### Naming

- T_RND: 0% nomi descrittivi;
- T_ES: 0% nomi descrittivi;
- T_LLM: 100% nomi descrittivi.

T_LLM è quindi favorito per comprensibilità immediata dell'intento dei test.

### Assertion density

- T_RND: 19.25 assertion/test;
- T_ES: 1.33 assertion/test;
- T_LLM: 6.58 assertion/test.

Il caso Randoop mostra nuovamente che una densità elevata di assertion non implica automaticamente maggiore forza: nonostante 231 assertion-like statements, il mutation score resta molto inferiore a quello di ES e LLM.

---

## Confronto di manutenibilità

### Dimensione

- T_RND: 695 LOC;
- T_ES: 292 LOC, di cui 101 di supporto;
- T_LLM: 289 LOC, senza supporto dedicato.

T_LLM è quindi l'artefatto complessivamente più compatto, mentre Randoop è nettamente il più voluminoso.

### SonarQube Cloud

Per code smell:

1. T_ES: **0**
2. T_LLM: **1**
3. T_RND: **154**

Il risultato Sonar migliore appartiene a EvoSuite. T_LLM rimane molto vicino con una singola issue, mentre Randoop presenta un numero molto elevato di smell.

---

## Conclusione C1

Per ListIteratorWrapper C1:

- per **adequacy**, l'ordine osservato è `T_LLM > T_ES > T_RND`;
- per **chiarezza del naming**, T_LLM è nettamente favorito;
- per **Sonar smell**, EvoSuite ottiene il risultato migliore con 0 issue;
- per **dimensione complessiva**, T_LLM è leggermente più compatto di EvoSuite e molto più compatto di Randoop;
- per **assenza di scaffolding**, T_RND e T_LLM non richiedono supporto dedicato.

Nel complesso, **T_LLM presenta il profilo multidimensionale più forte su C1**, mentre **T_ES è la suite più pulita secondo SonarQube Cloud**.

---

# Delta C0 → C1

Il confronto seguente mostra come cambiano le suite automatiche passando dalla versione originale C0 alla variante C1.

> **Nota metodologica:** la popolazione PIT cambia da 52 mutanti in C0 a 51 in C1. I delta di mutation score, killed/survived/no-coverage sono quindi descrittivi e non costituiscono un confronto mutante-per-mutante.

## T_RND — C0 → C1

| Metrica | C0 | C1 | Delta |
|---|---:|---:|---:|
| Line coverage | 58.33% | 58.82% | +0.49 pp |
| Branch coverage | 47.50% | 40.48% | **-7.02 pp** |
| Method coverage | 100.00% | 91.67% | -8.33 pp |
| Killed | 6 | 9 | +3 |
| Survived | 19 | 19 | 0 |
| No coverage | 27 | 23 | -4 |
| Mutation score | 11.54% | 17.65% | +6.11 pp |
| Test strength | 24.00% | 32.14% | +8.14 pp |
| Logic LOC | 397 | 695 | **+298** |
| Assertion-like statements | 92 | 231 | **+139** |
| Assertion/test | 7.67 | 19.25 | +11.58 |
| Code smell | 65 | 154 | **+89** |
| Regole Sonar | 4 | 7 | +3 |

### Interpretazione

C1 produce con Randoop un modesto miglioramento del mutation score e del test strength, ma:

- peggiora la branch coverage;
- riduce la method coverage;
- aumenta di quasi 300 LOC;
- più che raddoppia le assertion;
- aumenta gli smell da 65 a 154.

Il refactoring non rende quindi la generazione Randoop complessivamente più semplice o manutenibile.

---

## T_ES — C0 → C1

| Metrica | C0 | C1 | Delta |
|---|---:|---:|---:|
| Line coverage | 83.33% | 88.24% | +4.91 pp |
| Branch coverage | 82.50% | 88.10% | +5.60 pp |
| Method coverage | 81.82% | 91.67% | +9.85 pp |
| Killed | 29 | 33 | +4 |
| Survived | 15 | 15 | 0 |
| No coverage | 8 | 3 | -5 |
| Mutation score | 55.77% | 64.71% | +8.94 pp |
| Test strength | 65.91% | 68.75% | +2.84 pp |
| Logic LOC | 186 | 191 | +5 |
| Support LOC | 100 | 101 | +1 |
| Total LOC | 286 | 292 | +6 |
| Assertion-like statements | 14 | 16 | +2 |
| Code smell | 1 | 0 | **-1** |

### Interpretazione

Per EvoSuite, C1 mostra un miglioramento coerente:

- coverage più alta;
- mutation score più alto;
- meno mutanti non coperti;
- dimensione quasi invariata;
- passaggio da 1 smell a 0.

Tra le tre tecniche, EvoSuite è quella per cui il passaggio C0→C1 appare più regolare sul piano strutturale e statico.

---

## T_LLM — C0 → C1

| Metrica | C0 | C1 | Delta |
|---|---:|---:|---:|
| Line coverage | 100.00% | 100.00% | 0.00 pp |
| Branch coverage | 92.50% | 97.62% | **+5.12 pp** |
| Method coverage | 100.00% | 100.00% | 0.00 pp |
| Killed | 47 | 49 | +2 |
| Survived | 5 | 2 | -3 |
| No coverage | 0 | 0 | 0 |
| Mutation score | 90.38% | 96.08% | +5.70 pp |
| Test strength | 90.38% | 96.08% | +5.70 pp |
| Logic LOC | 344 | 289 | **-55** |
| Assertion-like statements | 74 | 79 | +5 |
| Assertion/test | 6.17 | 6.58 | +0.41 |
| Code smell | 0 | 1 | +1 |
| Regole Sonar | 0 | 1 | +1 |

### Interpretazione

T_LLM mantiene il 100% di line e method coverage, aumenta branch coverage, mutation score e test strength e riduce la dimensione della suite di 55 LOC.

Il trade-off è la comparsa di una singola issue Sonar `java:S1598` con maintainability severity HIGH.

Quindi C1 migliora l'adequacy e la compattezza della suite LLM, ma non conserva il risultato Sonar-clean di C0.

---

## Sintesi C0 → C1

Il refactoring C1 non produce lo stesso effetto sulle tre tecniche:

- **Randoop**: aumenta mutation adequacy, ma peggiora alcune coverage metric e cresce fortemente in dimensione e smell;
- **EvoSuite**: migliora coverage e mutation adequacy mantenendo quasi invariata la dimensione e passando a 0 smell;
- **LLM**: migliora ulteriormente branch coverage e mutation adequacy, riduce le LOC, ma introduce una singola issue Sonar.

Questo risultato suggerisce che l'effetto del refactoring sulla generazione automatica dei test è **dipendente dalla tecnica**: C1 non rende uniformemente migliori o peggiori tutte le suite.

---

## Evidenze

```text
isw2/results/testing/automatic-suite-quality/list-iterator-wrapper/
├── c0/
│   ├── rnd/sonar-ci/
│   ├── es/sonar-ci/
│   ├── llm/sonar-ci/
│   └── automatic-suite-comparison.md
└── c1/
    ├── rnd/sonar-ci/
    ├── es/sonar-ci/
    └── llm/sonar-ci/
```

I dati strutturali comuni sono conservati in:

```text
isw2/results/testing/automatic-suite-quality/
├── structural-quality-baseline.csv
├── structural-quality-baseline.txt
└── structural-quality-protocol.txt
```

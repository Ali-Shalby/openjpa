# PCEnhancer – M4 C3

## Variante

Classe:

`org.apache.openjpa.enhance.PCEnhancer`

Variante:

`C3`

Ogni variante della M4 viene generata indipendentemente a partire da C0.

## Input forniti a Copilot

C3 è stata generata a partire da:

- sorgente originale C0 di `PCEnhancer`;
- code smell Sonar rilevati su C0;
- suite black-box congelata `T_BB`;
- suite control-flow congelata `T_CF`.

Input utilizzati:

- `raw/PCEnhancer-C0.txt`
- `raw/pcenhancer-sonar-smells.csv`
- `raw/pcenhancer-tbb-tcf-tests.txt`

Il file `raw/pcenhancer-sonar-metrics.csv` è conservato come evidenza locale,
ma non è stato fornito a Copilot.

La risposta testuale restituita da Copilot è conservata in:

- `raw/copilot-response.txt`

Il sorgente generato viene conservato senza correzioni manuali in:

- `PCEnhancer.java`

## Contesto sperimentale

Nel prompt è stato specificato esplicitamente che la variante C3 deve superare
integralmente tutti i test T_BB e T_CF forniti.

Non sono stati forniti:

- T_MT;
- T_RND;
- T_ES;
- T_LLM;
- risultati delle varianti C1 e C2;
- errori di compilazione delle varianti precedenti.

C3 è quindi indipendente dalle varianti precedenti e parte direttamente da C0.

## Compilazione

C3 è stata temporaneamente sostituita a C0 e compilata nel reale reactor Maven
del modulo `openjpa-kernel`.

Profilo di compilazione:

`mvn -pl openjpa-kernel -am -DskipTests compile`

Risultato:

- Maven exit code: `1`
- OpenJPA Parent POM: `SUCCESS`
- OpenJPA Utilities Library: `SUCCESS`
- OpenJPA Kernel: `FAILURE`
- Compilazione C3: `FAIL`

Il compilatore segnala il seguente errore:

`incompatible types: java.lang.String cannot be converted to java.lang.String[]`

Posizione riportata da javac:

- riga 2065

L'errore riguarda la costruzione di un `MethodNode`: il refactoring passa
una singola `String` come elenco delle eccezioni dichiarate, mentre il
costruttore richiede un array `String[]`.

Evidenza completa:

- `raw/compile.txt`

## Ripristino di C0

Al termine dell'esperimento il sorgente originale C0 è stato ripristinato.

SHA-256 del C0 ripristinato:

`F7D85F9663B68430521FBEDE824C273971BAEF790A3C5291A9F8E0B7BC64AC7A`

## Audit statico

Nel sorgente C3 sono inoltre presenti trasformazioni che richiederebbero
verifica comportamentale qualora la classe compilasse.

In particolare sono presenti auto-assegnazioni come:

- `addDefaultConstructor = addDefaultConstructor`
- `redefine = redefine`

Queste modifiche non vengono corrette manualmente, perché la variante viene
conservata come output sperimentale diretto prodotto da Copilot.

## Test post-hoc

Le suite T_BB e T_CF, pur essendo state fornite come vincolo esplicito del
refactoring, non possono essere eseguite sulla variante prodotta perché la
classe di produzione non compila.

Pertanto:

`T_BB post-hoc = NON ESEGUITA – prerequisito di compilazione non soddisfatto`

`T_CF post-hoc = NON ESEGUITA – prerequisito di compilazione non soddisfatto`

## Stato

`C3 = COMPILATION FAIL`

Il fallimento viene conservato come risultato sperimentale.

Non vengono effettuate correzioni manuali né ulteriori iterazioni con Copilot.

Il confronto Sonar e delle metriche di prodotto verrà svolto successivamente
in modo uniforme sulle varianti della M4.

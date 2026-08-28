# PCEnhancer – M4 C2

## Variante

Classe:

`org.apache.openjpa.enhance.PCEnhancer`

Variante:

`C2`

Ogni variante della M4 viene generata indipendentemente a partire da C0.

## Input forniti a Copilot

C2 è stata generata a partire da:

- sorgente originale C0 di `PCEnhancer`;
- code smell Sonar rilevati su C0;
- suite black-box congelata `T_BB`.

Input utilizzati:

- `raw/PCEnhancer-C0.txt`
- `raw/pcenhancer-sonar-smells.csv`
- `raw/pcenhancer-tbb-tests.txt`

Il file `raw/pcenhancer-sonar-metrics.csv` è conservato come evidenza locale,
ma non è stato fornito a Copilot come input.

La risposta testuale restituita da Copilot è conservata in:

- `raw/copilot-response.txt`

Il sorgente generato viene conservato senza correzioni manuali in:

- `PCEnhancer.java`

## Contesto sperimentale

La suite T_BB è stata fornita esclusivamente come vincolo comportamentale
durante il refactoring.

Non sono stati forniti:

- T_CF;
- T_MT;
- T_RND;
- T_ES;
- T_LLM;
- risultati della variante C1;
- errori di compilazione della variante C1.

C2 è quindi indipendente da C1 e parte direttamente da C0.

## Compilazione

C2 è stata temporaneamente sostituita a C0 e compilata nel reale reactor Maven
del modulo `openjpa-kernel`.

Profilo di compilazione:

`mvn -pl openjpa-kernel -am -DskipTests compile`

Risultato:

- Maven exit code: `1`
- OpenJPA Parent POM: `SUCCESS`
- OpenJPA Utilities Library: `SUCCESS`
- OpenJPA Kernel: `FAILURE`
- Compilazione C2: `FAIL`

Il compilatore segnala un errore in `PCEnhancer.java`:

`cannot find symbol: method isEmpty()`

La chiamata viene effettuata su un oggetto di tipo:

`org.apache.xbean.asm9.tree.InsnList`

Posizione riportata da javac:

- riga 1216

Il refactoring ha sostituito un controllo basato sulla dimensione della lista
di istruzioni con una chiamata a `InsnList.isEmpty()`, metodo non disponibile
nell'API effettivamente utilizzata dal progetto.

Evidenza completa:

- `raw/compile.txt`

## Ripristino di C0

Al termine dell'esperimento il sorgente originale è stato ripristinato.

SHA-256 del C0 ripristinato:

`F7D85F9663B68430521FBEDE824C273971BAEF790A3C5291A9F8E0B7BC64AC7A`

## Audit statico

Il sorgente C2 contiene inoltre alcune trasformazioni che richiederebbero
verifica comportamentale qualora la classe compilasse.

Tra queste sono presenti auto-assegnazioni introdotte dalla rinomina dei campi,
ad esempio:

- `addDefaultConstructor = addDefaultConstructor`
- `redefine = redefine`
- `subclass = subclass`
- `repos = repos`
- `meta = meta`

Queste modifiche non vengono corrette manualmente, poiché la variante viene
conservata come output sperimentale diretto di Copilot.

## Test post-hoc

La suite T_BB, pur essendo stata fornita a Copilot come vincolo durante il
refactoring, non può essere eseguita sulla variante prodotta perché la classe
di produzione non compila.

Pertanto:

`T_BB post-hoc = NON ESEGUITA – prerequisito di compilazione non soddisfatto`

## Stato

`C2 = COMPILATION FAIL`

Il fallimento viene mantenuto come risultato sperimentale.

Non vengono effettuate correzioni manuali né ulteriori iterazioni con Copilot.

Il confronto Sonar e delle metriche di prodotto verrà svolto successivamente
in modo uniforme sulle varianti della M4.

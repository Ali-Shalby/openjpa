# PCEnhancer – M4 C1

## Variante

Classe:

`org.apache.openjpa.enhance.PCEnhancer`

Variante:

`C1`

Ogni variante della M4 viene generata indipendentemente a partire da C0.

## Input forniti a Copilot

C1 è stata generata a partire dal sorgente originale di `PCEnhancer` e
dall'evidenza dei code smell Sonar rilevati su C0.

Nessuna suite di test è stata fornita come contesto per il refactoring.

Input utilizzati:

- `raw/PCEnhancer-C0.txt`
- `raw/pcenhancer-sonar-smells.csv`

Il sorgente generato da Copilot viene conservato senza correzioni manuali in:

- `PCEnhancer.java`

## Compilazione

C1 è stata temporaneamente sostituita a C0 e compilata all'interno del reale
reactor Maven di `openjpa-kernel`.

Profilo di compilazione:

`mvn -pl openjpa-kernel -am -DskipTests compile`

Risultato:

- Maven exit code: `1`
- OpenJPA Parent POM: `SUCCESS`
- OpenJPA Utilities Library: `SUCCESS`
- OpenJPA Kernel: `FAILURE`
- Compilazione C1: `FAIL`

Il compilatore segnala due errori in `PCEnhancer.java` causati da chiamate a
`InsnList.isEmpty()`, metodo non disponibile nella versione ASM/XBean
utilizzata da questa versione di OpenJPA.

Posizioni riportate da javac:

- riga 1084
- riga 1216

Al termine dell'esperimento il sorgente originale C0 è stato ripristinato.

SHA-256 del C0 ripristinato:

`F7D85F9663B68430521FBEDE824C273971BAEF790A3C5291A9F8E0B7BC64AC7A`

Evidenza completa:

- `raw/compile.txt`

## Audit statico

La C1 generata contiene inoltre modifiche di refactoring che richiederebbero
una verifica comportamentale nel caso in cui la compilazione avesse successo.

In particolare, la rinomina di alcuni campi ha introdotto auto-assegnazioni
come:

- `repos = repos`
- `meta = meta`
- `redefine = redefine`
- `subclass = subclass`
- `fail = fail`
- `dir = dir`
- `writer = writer`

Queste modifiche non vengono corrette manualmente, perché C1 viene trattata
come output sperimentale diretto prodotto nel contesto previsto per la variante.

## Test post-hoc

Le suite di test congelate non vengono eseguite su C1 perché la classe di
produzione non compila.

Pertanto, per questa variante, l'esecuzione dei test viene registrata come:

`NON ESEGUITA – prerequisito di compilazione non soddisfatto`

## Stato

`C1 = COMPILATION FAIL`

Il fallimento viene conservato come risultato sperimentale e non viene corretto
tramite ulteriori iterazioni con Copilot.

Il confronto dei code smell Sonar e delle metriche di prodotto verrà eseguito
in modo uniforme sulle varianti della M4.

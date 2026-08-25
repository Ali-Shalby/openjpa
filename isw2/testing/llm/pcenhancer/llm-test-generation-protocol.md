>>   "isw2/testing/llm/pcenhancer/prompts/P1-functional-analysis.txt" `
>>   "isw2/testing/llm/pcenhancer/prompts/P2-test-design-and-implementation.txt"
diff --git a/isw2/testing/llm/pcenhancer/prompts/P1-functional-analysis.txt b/isw2/testing/llm/pcenhancer/prompts/P1-functional-analysis.txt
new file mode 100644
index 000000000..e847c77a9
--- /dev/null
+++ b/isw2/testing/llm/pcenhancer/prompts/P1-functional-analysis.txt
@@ -0,0 +1,34 @@
+# P1 - Functional Analysis
+
+Sto svolgendo un esperimento universitario di Software Testing sulla classe:
+
+org.apache.openjpa.enhance.PCEnhancer
+
+Baseline: Apache OpenJPA 4.1.1.
+
+Analizza PCEnhancer dal punto di vista del testing utilizzando esclusivamente
+il production context fornito.
+
+Non utilizzare:
+- test nativi OpenJPA;
+- altre suite sperimentali;
+- risultati JaCoCo;
+- risultati PIT;
+- Web o fonti esterne.
+
+In questa fase non progettare ancora i 30 test e non scrivere codice Java.
+
+Descrivi:
+1. le principali responsabilità della classe;
+2. le funzionalità e i comportamenti osservabili rilevanti per il testing;
+3. gli input e gli stati che influenzano tali comportamenti;
+4. gli output, gli effetti e le eccezioni osservabili;
+5. i principali collaboratori production coinvolti;
+6. eventuali aspetti che rendono il testing più complesso;
+7. eventuali informazioni production mancanti o incertezze.
+
+Analizza comunque tutto ciò che è determinabile dal contesto disponibile.
+Se alcune informazioni dipendono da production code non fornito, segnalale
+soltanto nella sezione finale delle incertezze senza inventarne il contenuto.
+
+Non generare test Java.
diff --git a/isw2/testing/llm/pcenhancer/prompts/P2-test-design-and-implementation.txt b/isw2/testing/llm/pcenhancer/prompts/P2-test-design-and-implementation.txt
new file mode 100644
index 000000000..60d88a556
--- /dev/null
+++ b/isw2/testing/llm/pcenhancer/prompts/P2-test-design-and-implementation.txt
@@ -0,0 +1,42 @@
+# P2 - Test Design + Implementation
+
+Continua lo stesso esperimento sulla classe:
+
+org.apache.openjpa.enhance.PCEnhancer
+
+Baseline: Apache OpenJPA 4.1.1.
+
+Sulla base dell'analisi precedente e del production context disponibile,
+progetta e implementa esattamente 30 test case per PCEnhancer.
+
+Identifica i casi come:
+
+TLLM-001 ... TLLM-030
+
+Per ciascun caso descrivi sinteticamente:
+- comportamento considerato;
+- setup/input;
+- azione;
+- risultato atteso.
+
+Successivamente fornisci il codice Java completo della suite.
+
+Ambiente:
+- Java 21;
+- JUnit Jupiter;
+- Mockito disponibile ma non obbligatorio.
+
+Usa esclusivamente il production context fornito durante questa conversazione.
+
+Non utilizzare:
+- test nativi OpenJPA;
+- altre suite sperimentali;
+- risultati JaCoCo;
+- risultati PIT;
+- Web o fonti esterne.
+
+Devono essere rappresentati esattamente i 30 test case
+TLLM-001 ... TLLM-030.
+
+Puoi utilizzare liberamente fixture, setup condiviso e metodi helper:
+questi elementi non costituiscono test case aggiuntivi.

# PCEnhancer T_LLM - Context Manifest

Questo manifest descrive il contesto production reso disponibile all'LLM prima
e durante la generazione di `T_LLM`.

## Baseline

```text
Project              : Apache OpenJPA
Release              : 4.1.1
Target               : org.apache.openjpa.enhance.PCEnhancer
Suite                : T_LLM
N                    : 30 test case/scenari distinti
LLM client           : Microsoft Copilot
Interaction mode     : browser chat
Model                : GPT 5.6 Think Deeper
Model provider       : OpenAI
Copilot plan         : Copilot Chat (Basic)
Repository branch    : isw2-project
Repository HEAD      : fa2cf51186b00cd833338b20bdf2a2d3709ece75
```

`Repository HEAD` identifica lo stato del progetto precedente al commit di
preregistrazione `T_LLM`.

## Pre-P1 environment refinement

Dopo il commit iniziale di preregistrazione, ma prima di qualsiasi prompt P1,
l'accesso a Microsoft Copilot tramite l'account istituzionale ha esposto
esplicitamente il modello selezionabile. L'ambiente è stato quindi raffinato
pre-P1 registrando `GPT 5.6 Think Deeper` come modello effettivamente scelto.

Nessun prompt dell'esperimento era stato ancora inviato e il production context
non è stato modificato.

## Contesto iniziale autorizzato

Prima di P1 l'LLM riceve:

```text
openjpa-kernel/src/main/java/org/apache/openjpa/enhance/PCEnhancer.java
T_LLM-environment.txt
```

## Technical delivery adaptation P1-R1

La prima lettura dell'allegato `.java` da parte di Microsoft Copilot ha prodotto
una rappresentazione incompleta del sorgente. È stata quindi fornita una copia
byte-identica denominata `PCEnhancer.java.txt`.

```text
PCEnhancer.java SHA-256     : F7D85F9663B68430521FBEDE824C273971BAEF790A3C5291A9F8E0B7BC64AC7A
PCEnhancer.java.txt SHA-256 : F7D85F9663B68430521FBEDE824C273971BAEF790A3C5291A9F8E0B7BC64AC7A
Content match               : True
```

L'adattamento ha riguardato esclusivamente l'estensione del file e non il
contenuto production. La risposta P1 originale è conservata come evidence e
P1-R1 è registrato come repair tecnico di delivery.

## Contesto esplicitamente escluso

Non vengono resi disponibili all'LLM:

- test nativi OpenJPA;
- sorgenti `T_BB`;
- sorgenti `T_CF`;
- sorgenti `T_MT`;
- sorgenti `T_RND`;
- sorgenti `T_ES`;
- risultati JaCoCo;
- risultati PIT;
- survivor mutation;
- documentazione del progetto contenente risultati delle suite precedenti;
- ulteriori fonti esterne fornite per colmare informazioni mancanti.

## Context Completion C1

Dopo P1-R1, l'LLM ha indicato sette dipendenze production a necessità alta.
Sono state aggiunte prima di P2.

Ogni file è stato copiato nel contesto isolato con sola estensione `.txt`;
per tutti i file è stata verificata l'identità byte-a-byte mediante SHA-256.

| Step | File production aggiunto | Motivo dichiarato dall'LLM | SHA-256 |
|---|---|---|---|
| C1 | `org/apache/openjpa/enhance/PersistenceCapable.java` | Contratto generato, bit dei field flag, DESERIALIZED e firme persistence-capable. | `6AE2744BCFB66393AE9511CCB002364BE7C1DA5EF0B41511A73EBD9F93571DDC` |
| C1 | `org/apache/openjpa/enhance/StateManager.java` | Firme e semantica runtime delle deleghe generate da PCEnhancer. | `603A33B2AD4A721D1BA7E6140DEB4F57BF09E55B87CDA7ED6D65AA552541434F` |
| C1 | `org/apache/openjpa/meta/ClassMetaData.java` | Semantica di access type, identità, gerarchia PC, detachable state e metadata di classe. | `C697C807BE2622601A7C44734186071222A251BF5C5E65ED4932753584735987` |
| C1 | `org/apache/openjpa/meta/FieldMetaData.java` | Backing member, indici, management, PK/version, access type e metadata dei campi. | `11DE9DB8A3E3C0F3FA9FC7E7A176D6E4DCABDEA4839BCE55749031B6F63DB3CB` |
| C1 | `org/apache/openjpa/util/asm/AsmHelper.java` | Lookup ASM, riconoscimento istruzioni, tipi e serializzazione del bytecode. | `A74FAC2D107581E63AFD9C88569CF5129272ED642B822465A51DFF01611A4954` |
| C1 | `org/apache/openjpa/enhance/PCRegistry.java` | Semantica di register/newInstance e comportamento del controllo enhancement level. | `6E9BB0AD72A8158671B028583EF519E8A90FE1DC6F1CDDBA372454F5D5CE25D8` |
| C1 | `org/apache/openjpa/enhance/Reflection.java` | Risoluzione e accesso riflessivo a membri, gerarchie ed errori correlati. | `D4D151EFBA0DD5C3D0938746045701027FCB14CA712FB35781C1D423A66FD939` |

Tutti i confronti SOURCE/CONTEXT per C1 hanno restituito:

```text
MATCH : True
```

Per un limite dell'interfaccia Microsoft Copilot (massimo tre allegati per
messaggio), C1 è stato consegnato nella stessa conversazione in tre batch
(3 + 3 + 1), senza avviare P2 prima del batch finale.

## Context Completion C2

Dopo P2, l'LLM ha richiesto ulteriore production context necessario per
implementare in modo affidabile i 30 scenari senza inventare contratti.

Le richieste P2 corrispondono a 10 voci concettuali ma a 11 file fisici:
la voce relativa al subclassing comprende sia `PCSubclassValidator.java` sia
`DynamicPersistenceCapable.java`.

Il tipo restituito da `OpenJPAConfiguration.getDetachStateInstance()` è stato
identificato nel production code come `org.apache.openjpa.conf.DetachOptions`,
definito in `DetachOptions.java`.

Tutti gli 11 file sono stati copiati nel contesto isolato con sola estensione
`.txt`, senza modifiche di contenuto, e verificati mediante SHA-256.

| Step | File production aggiunto | Motivo dichiarato dall'LLM | SHA-256 |
|---|---|---|---|
| C2 | `org/apache/openjpa/meta/MetaDataRepository.java` | Richiesto in P2 per lookup metadata autorevoli, in particolare TLLM-004. | `1E9A34F6AC405A763A93B48FEDA2BF9A60728E15F07550B25FD088F404DC725E` |
| C2 | `org/apache/openjpa/util/asm/ClassNodeTracker.java` | Richiesto in P2 per costruzione, class loader, progetto e osservazione del writer. | `940C40C90F9F761DABE90D7212321A91FBFCC3ABC4121DDF9A98C9D6566CEF0F` |
| C2 | `org/apache/openjpa/util/asm/EnhancementProject.java` | Richiesto in P2 per caricamento di classi e sottoclassi synthetic. | `D5F230D344F276D4197C220795507BFABCAABC332B3359AF313FD1A73A4A1C68` |
| C2 | `org/apache/openjpa/util/asm/BytecodeWriter.java` | Richiesto in P2 per il contratto di scrittura osservato da TLLM-030. | `5DF42B85AA44EE4CEC4C254787909319587C4B57408A735490F5C50056967DA8` |
| C2 | `org/apache/openjpa/util/asm/RedefinedAttribute.java` | Richiesto in P2 per TLLM-007 e TLLM-008. | `8C1BCA957400EEEB13EA4B7F476D02A1370BC5922DA3C4B21F3226A450E502B5` |
| C2 | `org/apache/openjpa/enhance/RedefinitionHelper.java` | Richiesto in P2 per notifiche runtime e dirty check, incluso TLLM-026. | `697EE3F2EEEC002430B302D786382D6DAAF3FC7A00009728A62362F1D26E9E84` |
| C2 | `org/apache/openjpa/enhance/AttributeTranslator.java` | Richiesto in P2 per il contratto formale di TLLM-017. | `5938C99DC2C91757113EA59D324D8931D661CBC8319D84A6740E1AD81578A919` |
| C2 | `org/apache/openjpa/enhance/PCSubclassValidator.java` | Richiesto in P2 per validare l'ammissibilità del subclassing. | `A547DF6739A3163067B9B65B22CC044B63C888EE012C984C693397D2B7C71B80` |
| C2 | `org/apache/openjpa/enhance/DynamicPersistenceCapable.java` | Richiesto in P2 per il contratto della sottoclasse generated. | `E72E5035FB3AFB6C3C8B2C2D8C47A527A5AA1AA82BC13BD0EB2A1A025FA691E7` |
| C2 | `org/apache/openjpa/conf/OpenJPAConfiguration.java` | Richiesto in P2 per configurazione, repository, log, resolver, detach-state e OptimizeIdCopy. | `1B5EFF8684B96D39791AEEB8DD51737421D190FAEBA89F7CC239A4DD34E10809` |
| C2 | `org/apache/openjpa/conf/DetachOptions.java` | Identificato come tipo production restituito da OpenJPAConfiguration.getDetachStateInstance(). | `21923B0279B9FC0BBDD9612DEFF3B2B5A47857A16588B674B7D00BFCA7BC8FC3` |

Tutti i confronti SOURCE/CONTEXT per C2 hanno restituito:

```text
MATCH : True
```

C2 è registrato prima della consegna dei file a Copilot e prima di P3.

## Fingerprint del contesto iniziale

```text
PCEnhancer.java SHA-256 : F7D85F9663B68430521FBEDE824C273971BAEF790A3C5291A9F8E0B7BC64AC7A
Environment SHA-256     : 250C6EF4401AEABCF50B3E37C33DA4B12467E739FF21C69F214AE7CAA77E13A7
```

La copia iniziale di `PCEnhancer.java` usata nel contesto LLM è stata verificata
byte-identica al sorgente presente nel repository:

```text
Repository/Context match : True
```

## Freeze e aggiornamenti consentiti

Il contesto iniziale è stato congelato prima della prima interrogazione P1.

Gli aggiornamenti successivi sono ammessi soltanto come:
- technical delivery repair documentato;
- context completion esplicitamente richiesto dall'LLM e registrato prima
  dell'interrogazione successiva.

Non devono essere aggiunti file derivati da coverage, mutation analysis o suite
sperimentali precedenti.

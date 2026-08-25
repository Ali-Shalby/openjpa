# P1-R1 - Analisi funzionale di `org.apache.openjpa.enhance.PCEnhancer`

La seguente analisi sostituisce integralmente quella precedente. Considero `PCEnhancer.java.txt` come copia autorevole del sorgente production e utilizzo esclusivamente tale allegato insieme al contesto sperimentale già fornito, senza test OpenJPA, suite esterne, risultati JaCoCo/PIT o fonti Web.

Il nuovo allegato risulta leggibile in modo completo. Sono ora visibili il flusso di `run()`, i costruttori, la configurazione del bytecode, la generazione dei metodi persistence-capable, la gestione dell’identità, del detached state, della serializzazione, dell’externalizzazione, della clonazione, della scrittura e dell’esecuzione batch. [\[PCEnhancer.java | Txt\]](https://uniroma2-my.sharepoint.com/personal/ali_shalby_students_uniroma2_eu/Documents/File%20di%20Microsoft%20Copilot%20Chat/PCEnhancer.java.txt)

---

## 1. Inizializzazione statica e versione dell’enhancer

### 1. Comportamento o responsabilità osservabile

L’inizializzazione statica svolge due operazioni:

1. carica le implementazioni di `PCEnhancer.AuxiliaryEnhancer` tramite `Services`;
2. determina `ENHANCER_VERSION` leggendo `/META-INF/org.apache.openjpa.revision.properties` e convertendo la proprietà `openjpa.enhancer.revision` tramite `GitUtils.convertGitInfoToPCEnhancerVersion`.

Le implementazioni ausiliarie che producono qualunque `Throwable` durante l’istanziazione vengono ignorate. Se la revisione non può essere letta, convertita o produce un valore non positivo, la versione assume il valore di fallback `2`. [\[PCEnhancer.java | Txt\]](https://uniroma2-my.sharepoint.com/personal/ali_shalby_students_uniroma2_eu/Documents/File%20di%20Microsoft%20Copilot%20Chat/PCEnhancer.java.txt)

### 2. Input o stati rilevanti

- provider di `AuxiliaryEnhancer` individuabili dal class loader;
- successo o fallimento dell’istanziazione dei provider;
- presenza della risorsa revision properties;
- valore della proprietà `openjpa.enhancer.revision`;
- risultato della conversione Git.

### 3. Output, effetti o eccezioni osservabili

- popolamento dell’array statico `_auxEnhancers`;
- assegnazione di `ENHANCER_VERSION`;
- soppressione completa dei problemi di caricamento degli auxiliary enhancer;
- soppressione delle eccezioni durante il calcolo della versione;
- fallback deterministico a `2`.

### 4. Collaboratori production

- `Services`;
- `J2DoPrivHelper`;
- `GitUtils`;
- implementazioni di `AuxiliaryEnhancer`;
- risorsa `META-INF/org.apache.openjpa.revision.properties`.

### 5. Informazioni production mancanti

Per capire il solo flusso di `PCEnhancer` non sono indispensabili altri file. Per determinare concretamente:

- quali enhancer ausiliari possano essere caricati;
- come siano scoperti;
- come una revisione Git venga trasformata in versione numerica,

servono rispettivamente le implementazioni production di `AuxiliaryEnhancer`, i provider di servizio e `GitUtils.java`.

---

## 2. Costruzione, repository dei metadati e opzioni d’istanza

### 1. Comportamento o responsabilità osservabile

I costruttori accettano una classe, un `ClassMetaData` oppure un `ClassNodeTracker`. Il costruttore principale:

- conserva il progetto del tracker;
- usa inizialmente lo stesso tracker come tipo gestito e persistence-capable;
- acquisisce il log di enhancement;
- crea un nuovo `MetaDataRepository` se quello ricevuto è nullo;
- imposta sul repository creato `MetaDataModes.MODE_META`;
- cerca i metadati del tipo senza richiederne obbligatoriamente la presenza;
- legge dal repository l’opzione `OptimizeIdCopy`.

Il costruttore che riceve direttamente repository, tracker e metadati non effettua lookup aggiuntivi e non invoca `configureOptimizeIdCopy()`. [\[PCEnhancer.java | Txt\]](https://uniroma2-my.sharepoint.com/personal/ali_shalby_students_uniroma2_eu/Documents/File%20di%20Microsoft%20Copilot%20Chat/PCEnhancer.java.txt)

### 2. Input o stati rilevanti

- configurazione nulla o valida;
- tracker e progetto associato;
- repository fornito o assente;
- class loader esplicito o nullo;
- metadati trovati o assenti;
- opzioni:
  - aggiunta del costruttore predefinito;
  - ridefinizione;
  - creazione di sottoclasse;
  - enforcement delle restrizioni PROPERTY;
  - directory;
  - writer;
  - ottimizzazione della copia dell’identità.

### 3. Output, effetti o eccezioni osservabili

- inizializzazione dello stato interno;
- eventuale creazione e configurazione del repository;
- `_meta` può essere nullo, rappresentando un tipo persistence-aware;
- getter e setter espongono o modificano le opzioni;
- `setCreateSubclass` imposta `_subclass` e disabilita `_addVersionInitFlag`;
- eccezioni dei collaboratori non vengono intercettate dai costruttori.

### 4. Collaboratori production

- `OpenJPAConfiguration`;
- `MetaDataRepository`;
- `ClassMetaData`;
- `EnhancementProject`;
- `ClassNodeTracker`;
- `Log`.

### 5. Informazioni production mancanti

Per comprendere i valori restituiti e le eccezioni del lookup occorrono:

- `MetaDataRepository.java`, in particolare `getMetaData`, `setSourceMode` e la semantica del parametro `mustExist`;
- `OpenJPAConfiguration.java`, per `newMetaDataRepositoryInstance`, `getLog` e `getOptimizeIdCopy`;
- `ClassNodeTracker.java` ed `EnhancementProject.java`, per il ciclo di vita del bytecode.

---

## 3. Classificazione e orchestrazione di `run()`

### 1. Comportamento o responsabilità osservabile

`run()` applica una classificazione ordinata:

1. un enum restituisce `ENHANCE_NONE`;
2. un’interfaccia restituisce `ENHANCE_INTERFACE`;
3. un tipo che dichiara già direttamente l’interfaccia interna di `PersistenceCapable` restituisce `ENHANCE_NONE`;
4. gli altri tipi vengono configurati e sottoposti alla trasformazione degli accessi ai campi;
5. se `_meta` è nullo, il risultato è `ENHANCE_AWARE`;
6. se `_meta` è presente, vengono eseguite tutte le trasformazioni persistence-capable e il risultato è `ENHANCE_PC`.

Per accesso PROPERTY, prima della riscrittura vengono validati i metodi e raccolte le associazioni tra attributi e backing field. Nella modalità sottoclasse viene inoltre aggiunta la traduzione indice-attributo. Le violazioni vengono elaborate prima dell’enhancement strutturale. [\[PCEnhancer.java | Txt\]](https://uniroma2-my.sharepoint.com/personal/ali_shalby_students_uniroma2_eu/Documents/File%20di%20Microsoft%20Copilot%20Chat/PCEnhancer.java.txt)

### 2. Input o stati rilevanti

- flag ASM `ACC_ENUM` e `ACC_INTERFACE`;
- interfacce già dichiarate;
- presenza di metadati;
- accesso PROPERTY o mixed;
- modalità redefine/subclass;
- violazioni accumulate;
- contenuto dei metodi e accessi ai campi.

### 3. Output, effetti o eccezioni osservabili

- uno dei quattro status:
  - `ENHANCE_NONE = 0`;
  - `ENHANCE_AWARE = 2`;
  - `ENHANCE_INTERFACE = 4`;
  - `ENHANCE_PC = 8`;
- modifiche al bytecode;
- propagazione invariata di `OpenJPAException`;
- conversione delle altre `Exception` in `GeneralException`, con nome del tipo e messaggio originario;
- gli `Error` non vengono intercettati dal `catch (Exception)`.

### 4. Collaboratori production

- ASM `ClassNode` e `Opcodes`;
- `ClassMetaData`;
- `GeneralException`;
- `OpenJPAException`;
- tutte le routine interne di generazione.

### 5. Informazioni production mancanti

Il flusso proprio di `run()` è completamente determinabile. Per interpretare semanticamente gli status fuori da questa classe e gli effetti di alcune chiamate generate servono `PersistenceCapable`, `StateManager` e `PCRegistry`.

---

## 4. Ridefinizione e generazione della sottoclasse persistence-capable

### 1. Comportamento o responsabilità osservabile

`configureBCs()` viene eseguito una sola volta per istanza.

In modalità redefine:

- cerca negli attributi ASM un attributo sconosciuto con tipo `RedefinedAttribute.ATTR_TYPE`;
- se assente, aggiunge un nuovo `RedefinedAttribute`;
- se presente, imposta `_isAlreadyRedefined`.

In modalità subclass:

- costruisce `PCSubclassValidator` e invoca `assertCanSubclass()`;
- carica tramite il progetto la classe con nome sintetico;
- se la classe caricata ha ancora `java/lang/Object` come superclasse, la collega al tipo gestito, ne replica l’astrattezza e le aggiunge `DynamicPersistenceCapable`;
- altrimenti considera la sottoclasse già esistente e imposta `_isAlreadySubclassed`. [\[PCEnhancer.java | Txt\]](https://uniroma2-my.sharepoint.com/personal/ali_shalby_students_uniroma2_eu/Documents/File%20di%20Microsoft%20Copilot%20Chat/PCEnhancer.java.txt)

### 2. Input o stati rilevanti

- flag redefine e subclass;
- lista degli attributi ASM, anche nulla;
- attributo di ridefinizione già presente;
- metadati e validità della sottoclasse;
- superclasse iniziale del tracker sintetico;
- astrattezza del tipo gestito;
- precedente configurazione dell’istanza.

### 3. Output, effetti o eccezioni osservabili

- aggiunta dell’attributo di ridefinizione;
- creazione/configurazione del tracker della sottoclasse;
- dichiarazione di `DynamicPersistenceCapable`;
- aggiornamento dei due indicatori “already”;
- nessun effetto alle chiamate successive, dopo `_bcsConfigured = true`;
- propagazione delle eccezioni del validator o del progetto.

### 4. Collaboratori production

- `RedefinedAttribute`;
- `PCSubclassValidator`;
- `DynamicPersistenceCapable`;
- `EnhancementProject`;
- `ClassNodeTracker`.

### 5. Informazioni production mancanti

- `PCSubclassValidator.java`, necessario per conoscere le condizioni precise che impediscono il subclassing e le relative eccezioni;
- `RedefinedAttribute.java`, per il formato dell’attributo;
- `DynamicPersistenceCapable.java`, per il contratto aggiuntivo;
- `EnhancementProject.java`, per sapere se e come una classe sintetica già caricata venga recuperata.

---

## 5. Nomenclatura delle sottoclassi synthetic

### 1. Comportamento o responsabilità osservabile

Il nome della sottoclasse è costruito nel package di `PCEnhancer`, concatenando:

- package di `PCEnhancer`;
- nome interno del tipo gestito, con `/` sostituito da `$`;
- suffisso `$pcsubclass`.

`isPCSubclassName` verifica soltanto prefisso di package e suffisso. `toManagedTypeName` rimuove package e suffisso e sostituisce `$` con `.`. Il commento production riconosce espressamente che la conversione non è corretta per persistence-capable annidate. [\[PCEnhancer.java | Txt\]](https://uniroma2-my.sharepoint.com/personal/ali_shalby_students_uniroma2_eu/Documents/File%20di%20Microsoft%20Copilot%20Chat/PCEnhancer.java.txt)

### 2. Input o stati rilevanti

- nome normale o sintetico;
- nome nullo;
- nomi che soddisfano solo la forma lessicale;
- classi top-level o annidate.

### 3. Output, effetti o eccezioni osservabili

- nome sintetico;
- riconoscimento booleano;
- nome gestito ricostruito;
- `NullPointerException` per `isPCSubclassName(null)`, poiché il parametro viene dereferenziato direttamente;
- possibili falsi positivi lessicali;
- ricostruzione ambigua per tipi annidati.

### 4. Collaboratori production

- `ClassUtil`;
- `ClassNodeTracker`.

### 5. Informazioni production mancanti

Nessuna per il comportamento string-based. `ClassUtil.java` serve soltanto per verificare la determinazione esatta del package.

---

## 6. Validazione PROPERTY e individuazione dei backing field

### 1. Comportamento o responsabilità osservabile

Per accesso PROPERTY o mixed, `validateProperties()`:

- usa tutti i campi in modalità subclass, altrimenti solo quelli dichiarati;
- segnala come violazione fatale un backing member non rappresentato da `Method`, salvo mixed access;
- analizza il getter per trovare un campo restituito;
- cerca il setter lungo la gerarchia;
- se il setter manca, il getter restituisce un campo e non si opera in redefine, crea un setter sintetico privato che assegna direttamente quel campo;
- analizza il campo assegnato dal setter;
- registra le associazioni getter/setter, attributo e backing field;
- registra come non fatale il mismatch tra campo letto e campo scritto.

`findField` riconosce solo pattern bytecode semplici: metodo non statico, classe non interfaccia, accesso tramite `this`, `GETFIELD` per il getter o parametro locale 1 seguito da `PUTFIELD` per il setter. Ignora immediatamente `INSTANCEOF` o `CHECKCAST` fra accesso e ritorno. Se più occorrenze fanno riferimento a campi differenti, restituisce `null`. [\[PCEnhancer.java | Txt\]](https://uniroma2-my.sharepoint.com/personal/ali_shalby_students_uniroma2_eu/Documents/File%20di%20Microsoft%20Copilot%20Chat/PCEnhancer.java.txt)

### 2. Input o stati rilevanti

- accesso PROPERTY puro o mixed;
- backing member method o field;
- getter statico, astratto, di interfaccia o concreto;
- struttura delle istruzioni del getter e setter;
- setter assente;
- getter e setter riferiti allo stesso campo o a campi diversi;
- ridefinizione attiva;
- membri ereditati.

### 3. Output, effetti o eccezioni osservabili

- creazione delle mappe `_backingFields`, `_attrsToFields`, `_fieldsToAttrs`;
- eventuale setter sintetico;
- violazioni localizzate e deduplicate in un `HashSet`;
- ritorno `null` quando il pattern non è riconosciuto;
- `IllegalStateException` se la ricerca ricorsiva non trova campo o metodo prima di raggiungere una classe la cui superclasse è `Object`;
- `NoSuchElementException` se `AsmHelper.getMethodNode(...).get()` non trova il metodo atteso;
- possibile `NullPointerException` qualora la gerarchia raggiunga condizioni non contemplate dal controllo su `getSuperclass()`.

### 4. Collaboratori production

- `ClassMetaData`;
- `FieldMetaData`;
- `AccessCode`;
- `AsmHelper`;
- reflection Java;
- nodi ASM.

### 5. Informazioni production mancanti

- `ClassMetaData.java` e `FieldMetaData.java`, per definire con precisione mixed access, backing member, nomi getter/setter ed ereditarietà dei campi;
- `AsmHelper.java`, per la semantica di `isLoadInsn`, `isThisInsn` e `getMethodNode`;
- `AccessCode.java`, per le codifiche di accesso.

---

## 7. Aggregazione e gestione delle violazioni PROPERTY

### 1. Comportamento o responsabilità osservabile

`addViolation` crea il set su richiesta, aggiunge un messaggio localizzato e combina il parametro `fatal` con `_fail` tramite OR. Pertanto, una singola violazione fatale rende fatale l’intero insieme.

`processViolations` concatena i messaggi con il separatore di linea di sistema. Se `_fail` è vero, genera `UserException`; altrimenti registra un warning, se abilitato. [\[PCEnhancer.java | Txt\]](https://uniroma2-my.sharepoint.com/personal/ali_shalby_students_uniroma2_eu/Documents/File%20di%20Microsoft%20Copilot%20Chat/PCEnhancer.java.txt)

### 2. Input o stati rilevanti

- violazioni assenti o presenti;
- duplicati;
- fatalità della singola violazione;
- enforcement già impostato dall’utente;
- warning abilitato o disabilitato.

### 3. Output, effetti o eccezioni osservabili

- nessun effetto in assenza di violazioni;
- deduplicazione basata sull’uguaglianza dei messaggi;
- ordine dei messaggi non garantito, poiché è usato `HashSet`;
- `UserException` quando `_fail` è vero;
- warning unico aggregato altrimenti.

### 4. Collaboratori production

- `Localizer`;
- `J2DoPrivHelper`;
- `Log`;
- `UserException`.

### 5. Informazioni production mancanti

I file delle risorse di localizzazione sono necessari per conoscere il testo esatto dei messaggi. Non servono per determinare la logica di controllo.

---

## 8. Traduzione indice-attributo per sottoclassi PROPERTY

### 1. Comportamento o responsabilità osservabile

In modalità subclass, `addAttributeTranslation()` aggiunge `AttributeTranslator` e genera `pcAttributeIndexToFieldName(int)`.

- In accesso PROPERTY non mixed usa un `tableswitch` sull’intero intervallo dei campi e restituisce il backing field associato.
- In mixed access raccoglie gli indici dei soli campi PROPERTY e usa un `lookupswitch`.
- Nel ramo non mixed, un indice fuori intervallo genera `IllegalArgumentException`.
- Nel ramo mixed non viene aggiunto codice esplicito in corrispondenza del default label dopo lo switch. [\[PCEnhancer.java | Txt\]](https://uniroma2-my.sharepoint.com/personal/ali_shalby_students_uniroma2_eu/Documents/File%20di%20Microsoft%20Copilot%20Chat/PCEnhancer.java.txt)

### 2. Input o stati rilevanti

- accesso mixed o PROPERTY puro;
- zero, uno o più campi PROPERTY;
- mapping attributo-backing field disponibile o assente;
- indice valido o non valido.

### 3. Output, effetti o eccezioni osservabili

- aggiunta dell’interfaccia e del metodo;
- restituzione del nome del backing field;
- ritorno anticipato in mixed access senza campi PROPERTY;
- `IllegalArgumentException` nel default non mixed;
- il ramo mixed contiene una costruzione delicata: itera sui valori di `propFmds`, ma usa ciascun valore anche come indice di `propFmds` nell’espressione `propFmds.get(i)`. Se un indice PROPERTY non è valido come posizione nella lista compatta, durante l’enhancement può verificarsi `IndexOutOfBoundsException`;
- l’assenza di istruzioni al default mixed può produrre bytecode non completato per un indice non presente. L’effetto finale dipende dalla validazione e serializzazione ASM. [\[PCEnhancer.java | Txt\]](https://uniroma2-my.sharepoint.com/personal/ali_shalby_students_uniroma2_eu/Documents/File%20di%20Microsoft%20Copilot%20Chat/PCEnhancer.java.txt)

### 4. Collaboratori production

- `AttributeTranslator`;
- `ClassMetaData`;
- `FieldMetaData`;
- ASM.

### 5. Informazioni production mancanti

- `AttributeTranslator.java`, per il contratto pubblico del metodo;
- `AsmHelper.java`, per stabilire l’esito finale del bytecode incompleto quando viene serializzato.

---

## 9. Riscrittura e validazione degli accessi diretti ai campi

### 1. Comportamento o responsabilità osservabile

La classe scorre tutti i metodi non esclusi e cerca `GETFIELD` e `PUTFIELD`. Costruttori, inizializzatori statici e metodi esclusi da un auxiliary enhancer non vengono elaborati.

Per ogni accesso:

- risolve la classe proprietaria;
- trova il vero campo tramite `Reflection.findField`;
- recupera i metadati del tipo che dichiara il campo;
- rileva accessi illegittimi a backing field PROPERTY di altre gerarchie;
- registra accessi a backing field di un’altra proprietà della stessa classe;
- in enhancement ordinario FIELD sostituisce `GETFIELD`/`PUTFIELD` con `pcGet...`/`pcSet...` statici;
- in redefine inserisce chiamate a `RedefinitionHelper.accessingField` o `settingField`. [\[PCEnhancer.java | Txt\]](https://uniroma2-my.sharepoint.com/personal/ali_shalby_students_uniroma2_eu/Documents/File%20di%20Microsoft%20Copilot%20Chat/PCEnhancer.java.txt)

### 2. Input o stati rilevanti

- opcode e owner dell’istruzione;
- tipo owner caricabile o meno;
- campo persistente o non persistente;
- accesso FIELD o PROPERTY;
- proprietario nella stessa gerarchia o in una gerarchia esterna;
- backing-field mappings;
- modalità ordinaria, redefine o subclass;
- tipo primitivo, `String` o oggetto.

### 3. Output, effetti o eccezioni osservabili

- sostituzione delle istruzioni con `INVOKESTATIC`;
- aggiunta delle notifiche di accesso/mutazione;
- `UserException` per accesso diretto a proprietà appartenenti a una gerarchia esterna;
- violazione non fatale per backing field di un’altra proprietà locale;
- `ClassNotFoundException` o `NoSuchMethodException`;
- possibile `NullPointerException` nel ramo redefine se `owner` è nullo, perché il ramo usa `owner.getField(name)` senza un controllo aggiuntivo;
- il trattamento dell’old value nel ramo mutation dipende dalla consistenza dello stack e dai metodi ASM richiamati.

### 4. Collaboratori production

- `Reflection`;
- `MetaDataRepository`;
- `ClassMetaData`;
- `FieldMetaData`;
- `RedefinitionHelper`;
- `AsmHelper`;
- ASM.

### 5. Informazioni production mancanti

- `Reflection.java`, per la risoluzione esatta del campo;
- `RedefinitionHelper.java`, per gli effetti runtime delle notifiche;
- `AsmHelper.java`, per caricamento delle classi e gestione delle istruzioni;
- `ClassMetaData.java` e `FieldMetaData.java`, per lookup e indici.

---

## 10. Enhancement strutturale e campi synthetic

### 1. Comportamento o responsabilità osservabile

`enhanceClass`:

- aggiunge `PersistenceCapable`;
- genera `pcGetEnhancementContractVersion`;
- verifica il costruttore no-arg;
- se manca e l’aggiunta è disabilitata, genera `UserException`;
- se deve aggiungerlo, lo rende:
  - pubblico per tipi detachable;
  - privato per classi final;
  - protetto negli altri casi;
- il costruttore richiama il no-arg della superclasse;
- produce un warning quando modifica un normale tipo concreto, ma non per interfacce o sottoclassi generate.

`addFields` aggiunge gli array e riferimenti statici del protocollo, l’eventuale flag versione e, soltanto alla radice PC o in modalità subclass, `pcStateManager`. [\[PCEnhancer.java | Txt\]](https://uniroma2-my.sharepoint.com/personal/ali_shalby_students_uniroma2_eu/Documents/File%20di%20Microsoft%20Copilot%20Chat/PCEnhancer.java.txt)

### 2. Input o stati rilevanti

- costruttore no-arg presente o assente;
- opzione add-default-constructor;
- detachable;
- classe final;
- presenza di superclasse PC;
- campo versione;
- modalità subclass.

### 3. Output, effetti o eccezioni osservabili

- interfaccia e metodo versione aggiunti;
- costruttore eventualmente aggiunto;
- campi:
  - `pcInheritedFieldCount`;
  - `pcFieldNames`;
  - `pcFieldTypes`;
  - `pcFieldFlags`;
  - `pcPCSuperclass`;
  - `pcVersionInit`;
  - `pcStateManager`;
- `UserException` se il costruttore richiesto manca e non può essere aggiunto;
- un costruttore aggiunto può fare riferimento a un no-arg della superclasse non accessibile o inesistente. `PCEnhancer` non verifica direttamente tale condizione prima di emettere l’invocazione.

### 4. Collaboratori production

- `PersistenceCapable`;
- `ClassMetaData`;
- `ClassNodeTracker`;
- ASM;
- `Log`.

### 5. Informazioni production mancanti

- `PersistenceCapable.java`, per il significato completo dei campi e del contratto;
- `ClassMetaData.java`, per detachable, versione e gerarchia;
- `AsmHelper.java`, per la successiva validazione/serializzazione del bytecode.

---

## 11. Inizializzatore statico e registrazione nel registry

### 1. Comportamento o responsabilità osservabile

`addStaticInitializer` inizializza:

- conteggio dei campi ereditati;
- superclasse PC;
- nomi, tipi e flag dei campi dichiarati.

Registra poi il tipo con `PCRegistry.register`, passando classe descritta, array, superclasse PC, alias e un prototipo. L’alias è presente per tipi mapped o abstract, altrimenti è `null`. Per una classe abstract, anche il prototipo è `null`; per una concreta viene istanziato tramite costruttore no-arg.

Le istruzioni vengono inserite immediatamente prima dell’ultimo `RETURN` del `<clinit>` esistente o creato. [\[PCEnhancer.java | Txt\]](https://uniroma2-my.sharepoint.com/personal/ali_shalby_students_uniroma2_eu/Documents/File%20di%20Microsoft%20Copilot%20Chat/PCEnhancer.java.txt)

### 2. Input o stati rilevanti

- superclasse PC;
- modalità subclass;
- campi dichiarati;
- management, fetch group e primary key;
- mapped/abstract;
- inizializzatore statico preesistente;
- ultima istruzione del `<clinit>`.

### 3. Output, effetti o eccezioni osservabili

- valorizzazione dei campi statici;
- chiamata a `PCRegistry.register`;
- creazione di `<clinit>` se assente;
- `IllegalStateException` se l’ultima istruzione del `<clinit>` non è `RETURN`;
- per sottoclassi, inherited count inizializzato a zero anche in presenza di superclasse PC;
- la registrazione avviene dopo le istruzioni statiche originali, perché il blocco viene inserito prima del loro `RETURN` finale.

### 4. Collaboratori production

- `PCRegistry`;
- `PersistenceCapable`;
- `ClassMetaData`;
- `FieldMetaData`;
- `AsmHelper`.

### 5. Informazioni production mancanti

- `PCRegistry.java`, necessario per conoscere effetti, vincoli e duplicazioni della registrazione;
- `PersistenceCapable.java`, per i bit dei flag;
- `ClassMetaData.java` e `FieldMetaData.java`, per alias e classificazione dei campi.

---

## 12. Calcolo dei flag dei campi

### 1. Comportamento o responsabilità osservabile

`getFieldFlag` restituisce:

- `-1` per `MANAGE_NONE`;
- il bit `SERIALIZABLE` per primitivi o tipi `Serializable`;
- `CHECK_WRITE` per campi transazionali;
- `CHECK_WRITE | CHECK_READ` per campi non-PK fuori dal default fetch group;
- `MEDIATE_WRITE | MEDIATE_READ` negli altri casi. [\[PCEnhancer.java | Txt\]](https://uniroma2-my.sharepoint.com/personal/ali_shalby_students_uniroma2_eu/Documents/File%20di%20Microsoft%20Copilot%20Chat/PCEnhancer.java.txt)

### 2. Input o stati rilevanti

- tipo declared;
- management;
- primary key;
- default fetch group;
- serializzabilità.

### 3. Output, effetti o eccezioni osservabili

- byte di flag usato sia nella registrazione sia nella generazione degli accessor;
- nessuna eccezione esplicita;
- un `FieldMetaData` nullo produrrebbe `NullPointerException`.

### 4. Collaboratori production

- `FieldMetaData`;
- `PersistenceCapable`.

### 5. Informazioni production mancanti

`PersistenceCapable.java` è necessario per interpretare numericamente e semanticamente i bit. `FieldMetaData.java` serve per le costanti di management.

---

## 13. Accessor FIELD e PROPERTY

### 1. Comportamento o responsabilità osservabile

Per accesso FIELD vengono generati metodi statici finali `pcGet<field>` e `pcSet<field>`, mantenendo gran parte della visibilità del campo ma rimuovendo `transient` e `volatile`.

Per accesso PROPERTY ordinario:

- getter e setter originali vengono rinominati con prefisso `pc`;
- diventano `protected`;
- vengono creati wrapper con nome e firma originali;
- annotazioni visibili e signature generica vengono trasferite ai wrapper;
- le annotazioni invisibili non vengono trasferite dal metodo `moveAnnotations`.

Per le sottoclassi PROPERTY, se non si opera in redefine, vengono generati getter/setter che delegano ai metodi del tipo gestito e inseriscono notifiche di accesso e mutazione. [\[PCEnhancer.java | Txt\]](https://uniroma2-my.sharepoint.com/personal/ali_shalby_students_uniroma2_eu/Documents/File%20di%20Microsoft%20Copilot%20Chat/PCEnhancer.java.txt)

### 2. Input o stati rilevanti

- accesso FIELD o PROPERTY;
- campo e metodi presenti;
- annotazioni visibili/invisibili;
- firma generica;
- visibilità dell’accessor di superclasse;
- modalità subclass/redefine;
- getter `getX` o `isX`.

### 3. Output, effetti o eccezioni osservabili

- metodi aggiunti e originali rinominati;
- trasferimento delle sole annotazioni visibili;
- `NoSuchElementException` per campi, getter o setter attesi ma assenti;
- `UserException` se `setVisibilityToSuperMethod` non trova un accessor;
- la ricerca della corrispondenza in `setVisibilityToSuperMethod` confronta `MethodNode.parameters`, non il descriptor. Più overload con parametri metadata equivalenti o nulli possono quindi risultare indistinguibili;
- per getter di sottoclasse viene preferito `getX`, altrimenti viene usato `isX`, senza una verifica finale esplicita prima di emettere l’invocazione.

### 4. Collaboratori production

- `FieldMetaData`;
- `ClassMetaData`;
- `AsmHelper`;
- `RedefinitionHelper`;
- ASM.

### 5. Informazioni production mancanti

- `FieldMetaData.java`, per nomi e backing member;
- `AsmHelper.java`, per lookup dei metodi;
- `RedefinitionHelper.java`, per gli effetti delle notifiche.

---

## 14. Mediazione delle letture e scritture tramite `StateManager`

### 1. Comportamento o responsabilità osservabile

Il getter generato:

- restituisce direttamente il valore se il campo non richiede né check né mediazione in lettura;
- restituisce direttamente il valore se `pcStateManager` è nullo;
- altrimenti calcola l’indice assoluto, chiama `StateManager.accessingField(index)` e restituisce il valore.

Il setter:

- scrive direttamente se lo state manager è nullo;
- imposta `pcVersionInit` quando applicabile;
- altrimenti chiama `setting<Type>Field`, passando istanza, indice assoluto, valore corrente, valore nuovo e flag zero.

I tipi non primitivi diversi da `String` vengono mappati alle versioni `Object` dei metodi dello state manager. [\[PCEnhancer.java | Txt\]](https://uniroma2-my.sharepoint.com/personal/ali_shalby_students_uniroma2_eu/Documents/File%20di%20Microsoft%20Copilot%20Chat/PCEnhancer.java.txt)

### 2. Input o stati rilevanti

- flag del campo;
- state manager nullo o presente;
- indice relativo ed inherited count;
- tipo primitivo, `String` o oggetto;
- campo versione;
- accesso FIELD/PROPERTY e modalità di enhancement.

### 3. Output, effetti o eccezioni osservabili

- lettura o scrittura diretta;
- notifica di accesso;
- delega `setting...Field`;
- aggiornamento del flag versione;
- `NoSuchMethodException` se la firma attesa non esiste in `StateManager`;
- propagazione runtime delle eccezioni del manager.

### 4. Collaboratori production

- `StateManager`;
- `PersistenceCapable`;
- `FieldMetaData`;
- `AsmHelper`.

### 5. Informazioni production mancanti

`StateManager.java` è indispensabile per verificare le firme risolte per reflection e il significato degli argomenti. `PersistenceCapable.java` serve per i bit di controllo/mediazione.

---

## 15. Protocollo dei campi: clear, provide, replace e copy

### 1. Comportamento o responsabilità osservabile

`pcClearFields`:

- richiama prima la superclasse PC, salvo modalità subclass;
- azzera soltanto i campi `MANAGE_PERSISTENT`;
- usa i valori Java predefiniti per ogni categoria di tipo.

`pcProvideField` passa il valore corrente allo state manager tramite `provided<Type>Field`.

`pcReplaceField` riceve il nuovo valore tramite `replace<Type>Field`, effettua il cast per i riferimenti e lo assegna.

`pcCopyField` copia un campo da un’altra istanza. Le versioni plurali iterano sull’array di indici e delegano alle versioni singole. [\[PCEnhancer.java | Txt\]](https://uniroma2-my.sharepoint.com/personal/ali_shalby_students_uniroma2_eu/Documents/File%20di%20Microsoft%20Copilot%20Chat/PCEnhancer.java.txt)

### 2. Input o stati rilevanti

- zero o più campi;
- indici ereditati, dichiarati o fuori range;
- array di indici nullo, vuoto o popolato;
- stessa o diversa istanza;
- stesso o diverso state manager;
- state manager nullo;
- modalità subclass;
- tipo del campo.

### 3. Output, effetti o eccezioni osservabili

- reset, passaggio, sostituzione o copia dei valori;
- delega alla superclasse per un indice relativo negativo, se esiste;
- `IllegalArgumentException` per indice non valido o classe senza campi;
- `NullPointerException` per array di indici nullo, a causa di `ARRAYLENGTH`;
- nella copia plurale:
  - `IllegalArgumentException` se i due manager non coincidono;
  - `IllegalStateException` se il manager comune è nullo;
  - possibile `ClassCastException` sul parametro source;
- in modalità subclass, la sorgente viene convertita tramite `ImplHelper`.

### 4. Collaboratori production

- `StateManager`;
- `ImplHelper`;
- `PersistenceCapable`;
- `FieldMetaData`;
- gerarchia PC.

### 5. Informazioni production mancanti

- `StateManager.java`, per provide/replace;
- `ImplHelper.java`, per la conversione nella modalità subclass;
- `PersistenceCapable.java`, per il contratto pubblico;
- `ClassMetaData.java` e `FieldMetaData.java`, per ordine e indici.

---

## 16. Creazione di nuove istanze persistence-capable

### 1. Comportamento o responsabilità osservabile

Vengono generate due varianti di `pcNewInstance`, con o senza object id.

- Per classi abstract, il metodo genera `UserException`.
- Per classi concrete, crea un’istanza tramite costruttore no-arg.
- Se il parametro `clear` è vero, richiama `pcClearFields`.
- Imposta direttamente `pcStateManager`.
- Nella variante con oid richiama `pcCopyKeyFieldsFromObjectId`.
- Restituisce la nuova istanza. [\[PCEnhancer.java | Txt\]](https://uniroma2-my.sharepoint.com/personal/ali_shalby_students_uniroma2_eu/Documents/File%20di%20Microsoft%20Copilot%20Chat/PCEnhancer.java.txt)

### 2. Input o stati rilevanti

- classe abstract o concreta;
- state manager;
- flag clear;
- object id nullo o valido;
- costruttore no-arg valido;
- tipo di identità.

### 3. Output, effetti o eccezioni osservabili

- nuova istanza PC;
- campi eventualmente azzerati;
- manager installato;
- chiavi eventualmente copiate;
- `UserException` per classe abstract;
- propagazione di errori di costruzione, verifica, cast o copia dell’oid.

### 4. Collaboratori production

- `PersistenceCapable`;
- `StateManager`;
- metodi di copia dell’identità generati.

### 5. Informazioni production mancanti

`PersistenceCapable.java` è necessario per il contratto; `StateManager.java` per la semantica del manager. Il flusso emesso è altrimenti determinabile.

---

## 17. Metodi standard delegati allo `StateManager`

### 1. Comportamento o responsabilità osservabile

Alla radice della gerarchia PC, o in modalità subclass, vengono creati wrapper per:

- generic context;
- object id;
- deleted;
- dirty;
- new;
- persistent;
- transactional;
- serializing;
- dirty by field name;
- accesso allo state manager;
- versione;
- sostituzione dello state manager.

Senza manager, i wrapper restituiscono `false`, `null` o semplicemente terminano. Con manager, delegano al metodo corrispondente. `pcIsDirty`, quando non si opera in redefine, chiama prima `RedefinitionHelper.dirtyCheck(sm)`. [\[PCEnhancer.java | Txt\]](https://uniroma2-my.sharepoint.com/personal/ali_shalby_students_uniroma2_eu/Documents/File%20di%20Microsoft%20Copilot%20Chat/PCEnhancer.java.txt)

### 2. Input o stati rilevanti

- manager nullo o presente;
- ritorno boolean, reference o void;
- parametro stringa di `dirty`;
- modalità redefine;
- campo versione;
- manager sostitutivo.

### 3. Output, effetti o eccezioni osservabili

- valori di default senza manager;
- risultato della delega;
- valore locale della versione, boxed se primitivo, quando il manager è nullo;
- risultato `StateManager.getVersion()` quando presente;
- `pcReplaceStateManager` assegna direttamente il nuovo manager se quello corrente è nullo, altrimenti assegna il risultato di `replaceStateManager`;
- propagazione delle eccezioni del manager, inclusa l’eventuale `SecurityException`.

### 4. Collaboratori production

- `StateManager`;
- `RedefinitionHelper`;
- `FieldMetaData`;
- `PersistenceCapable`.

### 5. Informazioni production mancanti

- `StateManager.java`, per firme, effetti ed eccezioni;
- `RedefinitionHelper.java`, per `dirtyCheck`;
- `PersistenceCapable.java`, per il contratto dei wrapper.

---

## 18. Identità non applicativa e identità applicativa

### 1. Comportamento o responsabilità osservabile

Per identità non applicativa vengono generati:

- quattro metodi di copia chiavi no-op;
- due metodi di creazione oid che restituiscono `null`.

Per identità applicativa vengono generate varianti con e senza `FieldSupplier`/`FieldConsumer`, oltre ai metodi di creazione dell’oid. La copia:

- richiama eventualmente l’implementazione della superclasse;
- gestisce object id condivisi tramite wrapper `ObjectId`;
- usa accesso diretto quando pubblico;
- usa `Reflection` per membri non pubblici;
- gestisce relazioni PC estraendo l’object id;
- converte le single-field identity e i wrapper primitivi;
- può usare un costruttore pubblico dell’IdClass quando l’ottimizzazione è abilitata. [\[PCEnhancer.java | Txt\]](https://uniroma2-my.sharepoint.com/personal/ali_shalby_students_uniroma2_eu/Documents/File%20di%20Microsoft%20Copilot%20Chat/PCEnhancer.java.txt)

### 2. Input o stati rilevanti

- identity datastore o application;
- OpenJPA single-field identity;
- object id shared;
- classi id e relativi membri;
- accesso field/property;
- campi PK primitivi, wrapper, stringhe, date, big number, object o relazioni PC;
- state manager nullo o presente;
- gerarchia PC;
- embedded-only e PK astratte.

### 3. Output, effetti o eccezioni osservabili

- chiavi scritte nell’oid o nell’istanza;
- valori passati a supplier/consumer;
- nuovo oid o wrapper condiviso;
- `InternalException` per `pcCopyKeyFieldsToObjectId` con OpenJPA identity;
- `IllegalArgumentException` se la costruzione da stringa richiesta non è disponibile;
- ritorno anticipato dalla copia oid verso un campo relazione PC se lo state manager è nullo;
- cast e conversioni che possono generare `ClassCastException`;
- propagazione delle eccezioni di reflection.

### 4. Collaboratori production

- `FieldSupplier`;
- `FieldConsumer`;
- `ApplicationIds`;
- `Id` e tutte le classi concrete di id;
- `ObjectId`;
- `Reflection`;
- `StateManager`;
- `ClassMetaData`;
- `FieldMetaData`.

### 5. Informazioni production mancanti

Per una comprensione runtime affidabile servono:

- `FieldSupplier.java` e `FieldConsumer.java`, per le firme fetch/store;
- `ApplicationIds.java`, per `setAppId`;
- `Id.java`, `ObjectId.java` e le classi id concrete, per conversioni e costruttori;
- `Reflection.java`;
- `StateManager.java`, in particolare `getPCPrimaryKey`;
- `ClassMetaData.java` e `FieldMetaData.java`.

---

## 19. Ottimizzazione della copia dell’IdClass

### 1. Comportamento o responsabilità osservabile

L’ottimizzazione è abilitata dalla configurazione del repository. `optimizeIdCopy` accetta solo un insieme non vuoto di PK per cui:

- nessun campo è di tipo PC;
- il corrispondente field dell’IdClass esiste;
- il field non è pubblico;
- non esiste un setter pubblico.

Successivamente, `getIdClassConstructorParmOrder` analizza i costruttori pubblici dell’IdClass. Cerca un costruttore con lo stesso numero di parametri delle PK e ricostruisce l’ordine osservando `PUTFIELD` preceduti da load di parametri compatibili per tipo. [\[PCEnhancer.java | Txt\]](https://uniroma2-my.sharepoint.com/personal/ali_shalby_students_uniroma2_eu/Documents/File%20di%20Microsoft%20Copilot%20Chat/PCEnhancer.java.txt)

### 2. Input o stati rilevanti

- opzione OptimizeIdCopy;
- PK presenti o assenti;
- campi PC;
- visibilità dei field;
- setter pubblico, non pubblico o assente;
- costruttori pubblici e loro bytecode;
- assegnazioni dirette dei parametri ai campi;
- ordine e tipo dei parametri.

### 3. Output, effetti o eccezioni osservabili

- lista degli indici PK oppure `null`;
- ordine dei parametri oppure `null`;
- percorso ottimizzato o fallback riflessivo;
- costruttori che trasformano parametri, usano metodi o assegnano attraverso logica non riconosciuta non vengono considerati validi;
- un ordine viene accettato quando il numero delle corrispondenze raggiunge quello delle PK.

### 4. Collaboratori production

- `Reflection`;
- `AsmHelper`;
- `FieldMetaData`;
- bytecode dell’IdClass.

### 5. Informazioni production mancanti

- `Reflection.java`, per la ricerca di field e setter;
- `AsmHelper.java`, per lettura della classe, riconoscimento dei load e calcolo dell’indice parametro;
- `FieldMetaData.java`, per tipo e nome delle PK.

---

## 20. Detached state e `pcIsDetached`

### 1. Comportamento o responsabilità osservabile

L’enhancer genera getter e setter del detached state, con campo sintetico privato transient se necessario. Quando l’uso del detached state è disabilitato, il getter restituisce `null` e il setter è no-op.

`pcIsDetached` restituisce un `Boolean` ternario:

1. `FALSE` se il tipo non è detachable;
2. se esiste uno state manager, restituisce `TRUE` o `FALSE` secondo `sm.isDetached()`;
3. se il detached state esiste ed è diverso da `DESERIALIZED`, restituisce `TRUE`;
4. se il detached state è obbligatorio ma assente o deserialized, restituisce `FALSE`;
5. una versione non predefinita indica `TRUE`;
6. con versione predefinita, il flag `pcVersionInit` può distinguere `TRUE` da stato indeterminato;
7. una PK auto-generata non predefinita, e per stringhe anche non vuota, indica `TRUE`;
8. se il detached state non è definitivo, restituisce `null`;
9. in alcuni assetti senza detached state valido restituisce `FALSE`;
10. negli altri casi non determinabili restituisce `null`.

Se necessario viene generato `pcIsDetachedStateDefinitive`, inizialmente sempre `false`, modificabile dagli auxiliary enhancer. [\[PCEnhancer.java | Txt\]](https://uniroma2-my.sharepoint.com/personal/ali_shalby_students_uniroma2_eu/Documents/File%20di%20Microsoft%20Copilot%20Chat/PCEnhancer.java.txt)

### 2. Input o stati rilevanti

- detachable;
- manager nullo o presente;
- `usesDetachedState`: `TRUE`, `FALSE` o `null`;
- detached state nullo, ordinario o `DESERIALIZED`;
- versione e flag versione-init;
- PK con value strategy;
- PK ai valori predefiniti;
- stringa vuota;
- serializzabilità;
- transienza del detached state;
- risultato del metodo definitive.

### 3. Output, effetti o eccezioni osservabili

- `Boolean.TRUE`, `Boolean.FALSE` o `null`;
- aggiunta di campo e metodi detached;
- accesso diretto o riflessivo al campo configurato;
- possibili eccezioni di reflection;
- chiamata a un metodo definitive che può essere successivamente modificato da plugin.

### 4. Collaboratori production

- `ClassMetaData`;
- `FieldMetaData`;
- `StateManager`;
- `PersistenceCapable.DESERIALIZED`;
- configurazione detach-state;
- `Reflection`;
- `AuxiliaryEnhancer`.

### 5. Informazioni production mancanti

- `ClassMetaData.java`, per `usesDetachedState`, campo detached e detachable;
- `PersistenceCapable.java`, per `DESERIALIZED`;
- `StateManager.java`, per `isDetached`;
- classe restituita da `getDetachStateInstance()`, per la transienza;
- eventuali auxiliary enhancer che cambiano il risultato definitivo.

---

## 21. Serializzazione Java standard

### 1. Comportamento o responsabilità osservabile

La serializzazione standard viene ignorata se:

- il tipo usa externalizzazione detached;
- il tipo non è `Serializable`.

Per una sottoclasse generated non `Externalizable`, viene aggiunto `writeReplace`, che crea un’istanza del tipo gestito e vi copia tutti i campi metadata non transient.

Per enhancement ordinario:

- se manca `serialVersionUID`, tenta di preservare quello calcolato sul tipo non enhanced;
- se il calcolo fallisce, registra un warning e non aggiunge il campo;
- crea o modifica `writeObject`;
- crea o modifica `readObject`;
- `writeObject` chiama `pcSerializing`, esegue la serializzazione standard se il metodo è nuovo e, prima di ogni `RETURN`, azzera il detached state quando richiesto;
- `readObject` imposta `DESERIALIZED` per detached state synthetic e chiama `defaultReadObject` se il metodo è nuovo. [\[PCEnhancer.java | Txt\]](https://uniroma2-my.sharepoint.com/personal/ali_shalby_students_uniroma2_eu/Documents/File%20di%20Microsoft%20Copilot%20Chat/PCEnhancer.java.txt)

### 2. Input o stati rilevanti

- `Serializable`/`Externalizable`;
- modalità subclass;
- metodi custom presenti o assenti;
- `serialVersionUID` presente o assente;
- detached state synthetic;
- risultato di `pcSerializing`;
- campi transient.

### 3. Output, effetti o eccezioni osservabili

- campo `serialVersionUID`;
- metodi `writeObject`, `readObject` o `writeReplace`;
- copia dei campi non transient nella sostituzione della sottoclasse;
- warning se il serial UID non è accessibile;
- `IOException`, `ClassNotFoundException` e `ObjectStreamException` dichiarate dai metodi generati;
- in un metodo `writeObject` preesistente la logica viene inserita all’inizio e il cleanup prima di ogni `RETURN`.

### 4. Collaboratori production

- `StateManager`, tramite `pcSerializing`;
- `PersistenceCapable`;
- `ObjectStreamClass`;
- `Reflection`;
- `AsmHelper`;
- metadati.

### 5. Informazioni production mancanti

- `StateManager.java`, per la semantica di `serializing`;
- `PersistenceCapable.java`, per `DESERIALIZED`;
- `Reflection.java` e `AsmHelper.java`;
- `FieldMetaData.java`, per la classificazione transient.

---

## 22. Externalizzazione detach-on-serialize

### 1. Comportamento o responsabilità osservabile

`externalizeDetached()` è vero quando:

- il detached state è `ClassMetaData.SYNTHETIC`;
- il tipo è `Serializable`;
- la configurazione dichiara il detached state non transient.

In tal caso l’enhancer:

- rende pubblico il costruttore no-arg;
- dichiara `Externalizable` se necessario;
- rifiuta implementazioni custom incompatibili;
- identifica i campi unmanaged serializzabili;
- genera metodi per leggere e scrivere:
  - stato unmanaged;
  - detached state;
  - state manager serializzato come elemento separato;
  - campi managed;
  - campi ereditati.

Per oggetti, array e alcuni tipi mutabili, dopo la lettura notifica `StateManager.proxyDetachedDeserialized(index)`. [\[PCEnhancer.java | Txt\]](https://uniroma2-my.sharepoint.com/personal/ali_shalby_students_uniroma2_eu/Documents/File%20di%20Microsoft%20Copilot%20Chat/PCEnhancer.java.txt)

### 2. Input o stati rilevanti

- detached synthetic e non transient;
- costruttore no-arg e visibilità;
- metodi custom di serializzazione/externalizzazione;
- campi managed o unmanaged;
- transient/static/final;
- gerarchia detachable ed `Externalizable`;
- state manager nullo o presente;
- risultato di `writeDetached`.

### 3. Output, effetti o eccezioni osservabili

- costruttore reso pubblico, con warning;
- interfaccia `Externalizable`;
- `readExternal`, `writeExternal`, metodi fields e unmanaged;
- `UserException` per metodi custom rilevati;
- `IOException` e `ClassNotFoundException`;
- `RuntimeException` se un tipo unmanaged non può essere caricato;
- ritorno anticipato se `sm.writeDetached(out)` restituisce true;
- altrimenti scrittura del detached state e di un secondo oggetto nullo prima dei campi.

Sono inoltre osservabili due condizioni strutturali specifiche:

- il controllo di `writeObject` usa un descriptor con `ObjectOutput`, non con `ObjectOutputStream`;
- il controllo di `writeExternal` costruisce un descriptor con `ObjectInput`, non con `ObjectOutput`.

Pertanto, determinati metodi custom con la firma Java normalmente attesa potrebbero non essere riconosciuti da questi controlli letterali. [\[PCEnhancer.java | Txt\]](https://uniroma2-my.sharepoint.com/personal/ali_shalby_students_uniroma2_eu/Documents/File%20di%20Microsoft%20Copilot%20Chat/PCEnhancer.java.txt)

### 4. Collaboratori production

- configurazione detach-state;
- `ClassMetaData`;
- `FieldMetaData`;
- `StateManager`;
- `Reflection`;
- `AsmHelper`;
- API Java di externalizzazione.

### 5. Informazioni production mancanti

- classe del detach-state configuration;
- `ClassMetaData.java` e `FieldMetaData.java`;
- `StateManager.java`, per `writeDetached` e `proxyDetachedDeserialized`;
- `AsmHelper.java` e `Reflection.java`.

---

## 23. Clonazione

### 1. Comportamento o responsabilità osservabile

La clonazione viene modificata solo:

- per la radice PC;
- oppure per una sottoclasse generated.

Se manca `clone`, viene aggiunto soltanto quando il tipo gestito è `Cloneable` e la gerarchia soddisfa le condizioni previste. Il metodo generato chiama `super.clone()`.

Nel primo `INVOKESPECIAL clone` trovato, l’enhancer duplica il clone, lo converte al tipo PC e azzera `pcStateManager`. Un clone esistente con al massimo un’istruzione non viene modificato. [\[PCEnhancer.java | Txt\]](https://uniroma2-my.sharepoint.com/personal/ali_shalby_students_uniroma2_eu/Documents/File%20di%20Microsoft%20Copilot%20Chat/PCEnhancer.java.txt)

### 2. Input o stati rilevanti

- superclasse PC;
- modalità subclass;
- implementazione di `Cloneable`;
- superclasse diretta;
- metodo clone assente, vuoto o concreto;
- presenza di una chiamata `INVOKESPECIAL clone`;
- visibilità del metodo della superclasse.

### 3. Output, effetti o eccezioni osservabili

- clone aggiunto o modificato;
- state manager del clone posto a `null`;
- nessuna modifica nelle condizioni escluse;
- `UserException` possibile da `setVisibilityToSuperMethod` se il metodo corrispondente non viene trovato;
- `CloneNotSupportedException` dichiarata dal metodo generato.

### 4. Collaboratori production

- `AsmHelper`;
- metadati di gerarchia;
- `StateManager` come tipo del campo.

### 5. Informazioni production mancanti

`AsmHelper.java` serve per il lookup del clone. Non sono necessari altri contratti per comprendere l’azzeramento del manager.

---

## 24. Auxiliary enhancer

### 1. Comportamento o responsabilità osservabile

Gli auxiliary enhancer possono:

- escludere singoli metodi dalla riscrittura degli accessi;
- modificare il `ClassNode` dopo tutte le trasformazioni principali.

Costruttori e inizializzatori statici vengono sempre esclusi, indipendentemente dai plugin. `getAuxiliaryEnhancers()` restituisce direttamente l’array statico, senza copia difensiva. [\[PCEnhancer.java | Txt\]](https://uniroma2-my.sharepoint.com/personal/ali_shalby_students_uniroma2_eu/Documents/File%20di%20Microsoft%20Copilot%20Chat/PCEnhancer.java.txt)

### 2. Input o stati rilevanti

- provider caricati;
- metodo esaminato;
- decisione di ciascun plugin;
- bytecode e metadati finali;
- eventuale modifica esterna dell’array restituito.

### 3. Output, effetti o eccezioni osservabili

- metodo saltato appena un enhancer restituisce true;
- modifiche arbitrary production al bytecode;
- propagazione delle eccezioni prodotte da `run` o `skipEnhance`;
- la modifica dell’array restituito da `getAuxiliaryEnhancers()` modifica la struttura statica osservata dalle istanze successive.

### 4. Collaboratori production

- `Services`;
- implementazioni di `AuxiliaryEnhancer`;
- provider configuration.

### 5. Informazioni production mancanti

Sono necessarie tutte le implementazioni production di `AuxiliaryEnhancer` e i relativi provider per conoscere gli effetti reali effettuati dopo l’enhancement principale.

---

## 25. Scrittura del bytecode

### 1. Comportamento o responsabilità osservabile

`record()`:

- scrive prima il managed type se è distinto da `pc` e redefine è attivo;
- scrive sempre il tracker PC.

Per ogni tracker:

1. usa `BytecodeWriter` se presente;
2. altrimenti, senza directory, trova la risorsa `.class` e la sovrascrive;
3. altrimenti genera il percorso sotto la directory, crea le directory parent e scrive il bytecode.

Se il tracker non ha class loader, usa il context class loader del thread. [\[PCEnhancer.java | Txt\]](https://uniroma2-my.sharepoint.com/personal/ali_shalby_students_uniroma2_eu/Documents/File%20di%20Microsoft%20Copilot%20Chat/PCEnhancer.java.txt)

### 2. Input o stati rilevanti

- writer presente o assente;
- directory presente o assente;
- tracker gestito uguale o diverso da PC;
- redefine;
- class loader del tracker o context loader;
- risorsa della classe esistente o assente;
- directory parent.

### 3. Output, effetti o eccezioni osservabili

- chiamata a `BytecodeWriter.write`;
- sovrascrittura della classe originale;
- creazione della struttura di directory;
- `IOException`;
- possibile `NullPointerException` se anche il context class loader è nullo o se `getResource` restituisce `null`;
- il percorso della risorsa è passato a `URLDecoder.decode(String)` e poi a `FileOutputStream`;
- il risultato di `mkdirs()` non viene controllato.

### 4. Collaboratori production

- `BytecodeWriter`;
- `AsmHelper`;
- `ClassNodeTracker`;
- filesystem e class loader Java.

### 5. Informazioni production mancanti

- `BytecodeWriter.java`, per il contratto del writer;
- `AsmHelper.java`, per la serializzazione del `ClassNodeTracker`;
- `ClassNodeTracker.java`, per class loader e identità del tracker.

---

## 26. CLI ed esecuzione batch

### 1. Comportamento o responsabilità osservabile

`main` estrae le opzioni e stampa l’uso su standard error se il runner restituisce false.

La variante ad alto livello usa `Configurations.runAgainstAllAnchors`, crea una configurazione per anchor e la chiude in `finally`.

Il runner batch:

- risolve un class loader se assente;
- crea facoltativamente un temporary class loader;
- crea e configura un repository se necessario;
- senza argomenti usa tutti i tipi persistenti;
- con argomenti usa `ClassArgParser`;
- carica ciascuna classe in un `EnhancementProject`;
- configura l’enhancer;
- esegue `run`;
- non registra `ENHANCE_NONE` o `ENHANCE_INTERFACE`;
- registra `ENHANCE_AWARE` e `ENHANCE_PC`;
- raccoglie e infine registra i tipi persistence-aware;
- pulisce il progetto dopo ogni elemento. [\[PCEnhancer.java | Txt\]](https://uniroma2-my.sharepoint.com/personal/ali_shalby_students_uniroma2_eu/Documents/File%20di%20Microsoft%20Copilot%20Chat/PCEnhancer.java.txt)

### 2. Input o stati rilevanti

- argomenti nulli, vuoti o popolati;
- nomi classe o percorsi;
- directory;
- add-default-constructor;
- temporary class loader;
- enforce-property-restrictions;
- writer inserito nelle options;
- repository e loader forniti o assenti;
- classi duplicate, poiché gli argomenti vengono raccolti in `HashSet`.

### 3. Output, effetti o eccezioni osservabili

- booleano false se non vengono trovati tipi persistenti e la collection è `null`;
- booleano true dopo il completamento del ciclo;
- scrittura dei risultati aware e PC;
- log trace/info/warn;
- chiusura della configurazione creata internamente;
- `IOException`;
- eccezioni runtime o di enhancement propagate;
- l’opzione `enforcePropertyRestrictions` viene letta in `Flags`, ma nel ciclo mostrato non viene trasferita all’istanza tramite `setEnforcePropertyRestrictions`. Quindi, in questo specifico percorso batch, il valore del flag non modifica direttamente `_fail`.

### 4. Collaboratori production

- `Configurations`;
- `Options`;
- `OpenJPAConfigurationImpl`;
- `ClassArgParser`;
- `MetaDataRepository`;
- `EnhancementProject`;
- `BytecodeWriter`;
- class resolver e temporary class loader;
- `Log`.

### 5. Informazioni production mancanti

- `Configurations.java`, per anchor e interpretazione del booleano;
- `Options.java`, per parsing e proprietà residue;
- `ClassArgParser.java`, per formati accettati e fallimenti;
- `OpenJPAConfiguration.java`, per resolver e repository;
- `EnhancementProject.java`, per caricamento e `clear`.

---

## 27. Verifica del livello di enhancement

### 1. Comportamento o responsabilità osservabile

`checkEnhancementLevel`:

- restituisce false se classe o log sono nulli;
- richiede un’istanza al `PCRegistry`;
- restituisce false se il registry restituisce null;
- confronta `pcGetEnhancementContractVersion` con `ENHANCER_VERSION`;
- se la versione è inferiore, registra un messaggio info e restituisce true;
- per versione uguale o superiore restituisce false. [\[PCEnhancer.java | Txt\]](https://uniroma2-my.sharepoint.com/personal/ali_shalby_students_uniroma2_eu/Documents/File%20di%20Microsoft%20Copilot%20Chat/PCEnhancer.java.txt)

### 2. Input o stati rilevanti

- classe e log nulli o validi;
- tipo registrato o non registrato;
- versione inferiore, uguale o superiore;
- implementazione effettiva di `PersistenceCapable`.

### 3. Output, effetti o eccezioni osservabili

- booleano;
- log info soltanto per versione inferiore;
- possibili eccezioni di `PCRegistry.newInstance`;
- il Javadoc dichiara una `IllegalStateException` per classi non PC, ma il metodo non la produce direttamente.

### 4. Collaboratori production

- `PCRegistry`;
- `PersistenceCapable`;
- `Log`;
- `Localizer`.

### 5. Informazioni production mancanti

`PCRegistry.java` è indispensabile per stabilire se e quando una classe non PC genera `IllegalStateException`, restituisce null o produce un’altra conseguenza.

---

# Elenco sintetico delle aree funzionali individuate

1. Inizializzazione statica, plugin e versione dell’enhancer.
2. Costruzione, lookup dei metadati e configurazione.
3. Classificazione di enum, interfacce, tipi già enhanced, aware e PC.
4. Ridefinizione e creazione di sottoclassi.
5. Nomenclatura delle sottoclassi synthetic.
6. Validazione PROPERTY e backing field.
7. Aggregazione delle violazioni.
8. Traduzione indice-attributo.
9. Riscrittura di `GETFIELD` e `PUTFIELD`.
10. Enhancement strutturale e campi synthetic.
11. Inizializzatore statico e registrazione.
12. Calcolo dei field flags.
13. Accessor FIELD e PROPERTY.
14. Mediazione tramite `StateManager`.
15. Clear, provide, replace e copy fields.
16. Creazione di istanze persistence-capable.
17. Metodi standard e versione runtime.
18. Identità applicativa e non applicativa.
19. Ottimizzazione dell’IdClass.
20. Detached state e `pcIsDetached`.
21. Serializzazione Java standard.
22. Externalizzazione detach-on-serialize.
23. Clonazione.
24. Auxiliary enhancer.
25. Scrittura del bytecode.
26. CLI ed enhancement batch.
27. Controllo del livello di enhancement.

---

# Classi e file production aggiuntivi necessari

Il sorgente di `PCEnhancer` è ora completo e **non deve essere fornito nuovamente**. Le dipendenze seguenti sono necessarie solo per completare in modo affidabile la semantica dei collaboratori, non per ricostruire il controllo interno già visibile.

## Necessità alta

### `org/apache/openjpa/enhance/PersistenceCapable.java`

Serve per:

- firme esatte del contratto generato;
- valori dei bit `SERIALIZABLE`, `CHECK_READ`, `CHECK_WRITE`, `MEDIATE_READ`, `MEDIATE_WRITE`;
- valore e identità di `DESERIALIZED`;
- vincoli su metodi generate e object id.

### `org/apache/openjpa/enhance/StateManager.java`

Serve per:

- firme risolte dinamicamente da `getStateManagerMethod`;
- semantica di provide, replace, setting e accessing;
- `serializing`, `writeDetached`, `proxyDetachedDeserialized`;
- `getPCPrimaryKey`, `replaceStateManager`, `isDetached` e `getVersion`;
- eccezioni ed effetti runtime.

### `org/apache/openjpa/meta/ClassMetaData.java`

Serve per:

- access type e mixed access;
- gerarchia PC;
- identità;
- detachable e detached state;
- embedded/mapped/abstract;
- ordine dei campi;
- object id condiviso;
- alias e interface implementation.

### `org/apache/openjpa/meta/FieldMetaData.java`

Serve per:

- backing member;
- indici assoluti e relativi;
- management;
- PK, version, default fetch group;
- type code e object-id type code;
- access type;
- relazioni PC;
- value strategy e transient.

### `org/apache/openjpa/util/asm/AsmHelper.java`

Serve per:

- lookup di metodi;
- riconoscimento delle istruzioni;
- calcolo degli indici dei parametri;
- risoluzione dei tipi;
- caricamento e serializzazione dei `ClassNode`;
- comportamento su bytecode strutturalmente incompleto.

### `org/apache/openjpa/enhance/PCRegistry.java`

Serve per:

- effetti di `register`;
- comportamento di `newInstance`;
- condizione effettiva dell’eccezione documentata da `checkEnhancementLevel`;
- registrazioni duplicate o assenti.

### `org/apache/openjpa/enhance/Reflection.java`

Serve per:

- ricerca nella gerarchia;
- accesso a membri non pubblici;
- comportamento del parametro `mustExist`;
- eccezioni di get/set e accessor resolution.

## Necessità media, legata ad aree specifiche

### `org/apache/openjpa/enhance/PCSubclassValidator.java`

Necessario per conoscere tutte le condizioni che rendono un tipo non sottoclassabile.

### `org/apache/openjpa/enhance/RedefinitionHelper.java`

Necessario per gli effetti di access tracking, mutation tracking e dirty check.

### `org/apache/openjpa/enhance/FieldSupplier.java`

Necessario per le firme `fetch<Type>Field` usate nella copia verso l’oid.

### `org/apache/openjpa/enhance/FieldConsumer.java`

Necessario per le firme `store<Type>Field` usate nella copia dall’oid.

### `org/apache/openjpa/util/ApplicationIds.java`

Necessario per l’effetto di `setAppId` nel percorso ottimizzato.

### Classi production dell’identità

Plain Text

1

org/apache/openjpa/util/Id.java

2

org/apache/openjpa/util/ObjectId.java

3

org/apache/openjpa/util/ByteId.java

4

org/apache/openjpa/util/CharId.java

5

org/apache/openjpa/util/DateId.java

6

org/apache/openjpa/util/DoubleId.java

7

org/apache/openjpa/util/FloatId.java

8

org/apache/openjpa/util/IntId.java

9

org/apache/openjpa/util/LongId.java

10

org/apache/openjpa/util/ShortId.java

11

org/apache/openjpa/util/StringId.java

12

org/apache/openjpa/util/BigDecimalId.java

13

org/apache/openjpa/util/BigIntegerId.java

Mostra più linee

Sono necessarie se l’analisi successiva deve verificare costruttori, tipi restituiti da `getId` e comportamento delle conversioni.

### `org/apache/openjpa/util/ImplHelper.java`

Necessario per comprendere la conversione delle istanze nella copia dei campi in modalità subclass.

### `org/apache/openjpa/enhance/AttributeTranslator.java`

Necessario per il contratto del metodo di traduzione generato.

### `org/apache/openjpa/enhance/DynamicPersistenceCapable.java`

Necessario per il contratto aggiunto alle sottoclassi generated.

### Classe concreta restituita da `OpenJPAConfiguration.getDetachStateInstance()`

Necessaria per la semantica di `isDetachedStateTransient()`.

## Necessità specifica per esecuzione e output

Plain Text

1

org/apache/openjpa/util/asm/ClassNodeTracker.java

2

org/apache/openjpa/util/asm/EnhancementProject.java

3

org/apache/openjpa/util/asm/BytecodeWriter.java

4

org/apache/openjpa/util/asm/RedefinedAttribute.java

5

org/apache/openjpa/meta/MetaDataRepository.java

6

org/apache/openjpa/conf/OpenJPAConfiguration.java

7

org/apache/openjpa/lib/meta/ClassArgParser.java

8

org/apache/openjpa/lib/conf/Configurations.java

9

org/apache/openjpa/lib/util/Options.java

Mostra più linee

Questi file sono necessari per definire in modo completo caricamento, class loader, repository, parsing CLI, anchor, scrittura e serializzazione del bytecode.

## Plugin e risorse

- tutte le implementazioni production di `PCEnhancer.AuxiliaryEnhancer`;
- relativi provider sotto `META-INF/services`, se presenti;
- `META-INF/org.apache.openjpa.revision.properties`;
- `GitUtils.java`;
- risorse di localizzazione del package `org.apache.openjpa.enhance`.

Servono rispettivamente per effetti plugin, discovery, versione dell’enhancer e testo esatto dei messaggi.

---

# Aspetti non determinabili dal solo contesto disponibile

Dopo la lettura completa di `PCEnhancer`, restano non determinabili soltanto gli aspetti delegati ad altri componenti:

1. le regole complete con cui `PCSubclassValidator` ammette o rifiuta una sottoclasse;
2. le firme e la semantica runtime completa di `StateManager`;
3. i valori numerici dei bit dichiarati da `PersistenceCapable`;
4. la semantica completa di `PCRegistry.register` e `newInstance`;
5. il comportamento esatto della reflection helper su membri mancanti o non accessibili;
6. la semantica completa di metadati, mixed access, field indexes e detached state;
7. la validità finale prodotta da `AsmHelper.toByteArray` per strutture ASM problematiche;
8. il comportamento del caricamento di classi già presenti in `EnhancementProject`;
9. gli effetti reali degli auxiliary enhancer installati;
10. il contenuto esatto dei messaggi localizzati;
11. gli effetti delle classi concrete di object id;
12. parsing, anchor e proprietà residue del runner CLI;
13. eccezioni precise prodotte dai collaboratori;
14. il valore concreto di `ENHANCER_VERSION` nell’ambiente runtime, perché dipende dalla risorsa revision properties. In assenza o in caso di conversione non positiva, il sorgente stabilisce comunque il fallback `2`. [\[PCEnhancer.java | Txt\]](https://uniroma2-my.sharepoint.com/personal/ali_shalby_students_uniroma2_eu/Documents/File%20di%20Microsoft%20Copilot%20Chat/PCEnhancer.java.txt)

## Sintesi conclusiva

Con il nuovo allegato, la macro-specifica funzionale di `PCEnhancer` è ricostruibile in modo sostanzialmente completo. Le aree ancora aperte non dipendono più da parti mancanti del target, ma esclusivamente dai contratti e dagli effetti runtime dei collaboratori production elencati sopra. Non sono stati generati test Java né definiti i 30 casi finali.
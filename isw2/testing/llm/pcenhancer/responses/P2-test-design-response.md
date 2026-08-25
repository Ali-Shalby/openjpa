P2 - Test Design per PCEnhancer

Di seguito sono progettati esattamente 30 scenari distinti, senza codice Java. La selezione copre classificazione, configurazione, trasformazioni ASM, accesso ai campi, protocollo PersistenceCapable, identità, detached state, serializzazione, registrazione e output, usando soltanto il production context consegnato.

AsmHelper.java.txt, allegato con questo prompt, viene considerato il settimo file del completamento C1.

A. Classificazione e configurazione dell’enhancement
TLLM-001
Comportamento considerato: esclusione degli enum dall’enhancement.
Input e setup necessari: un ClassNodeTracker il cui ClassNode.access includa ACC_ENUM. Non sono necessari metadati.
Azione da eseguire: costruire PCEnhancer ed eseguire run().
Risultato atteso o osservabile: run() restituisce ENHANCE_NONE; non viene avviata la configurazione del bytecode e non vengono aggiunti interfacce, campi o metodi persistence-capable.
TLLM-002
Comportamento considerato: classificazione di un’interfaccia gestita.
Input e setup necessari: un tracker il cui ClassNode.access includa ACC_INTERFACE ma non ACC_ENUM.
Azione da eseguire: eseguire run().
Risultato atteso o osservabile: viene restituito ENHANCE_INTERFACE; nessuna trasformazione successiva viene applicata al ClassNode. La verifica dell’interfaccia precede il lookup operativo dei metadati nel corpo di run().
TLLM-003
Comportamento considerato: idempotenza rispetto a una classe che dichiara già PersistenceCapable.
Input e setup necessari: un normale ClassNode non enum e non interfaccia, con il nome interno di PersistenceCapable già presente in interfaces.
Azione da eseguire: eseguire run().
Risultato atteso o osservabile: restituzione di ENHANCE_NONE; il bytecode non viene nuovamente enhanced. Il controllo confronta direttamente i nomi interni ASM delle interfacce.
TLLM-004
Comportamento considerato: enhancement di una classe persistence-aware priva di metadati propri.
Input e setup necessari: classe non enum, non interfaccia e non già PersistenceCapable; repository configurato affinché getMetaData restituisca null; nel metodo della classe deve esserci un accesso a un campo persistente appartenente a un’altra classe per la quale il repository restituisce metadati FIELD.
Azione da eseguire: eseguire run().
Risultato atteso o osservabile: restituzione di ENHANCE_AWARE; l’accesso diretto al campo esterno viene riscritto con il relativo accessor statico, ma alla classe aware non vengono aggiunti il protocollo e i campi di PersistenceCapable.
Dipendenza production: per realizzare il repository senza inventarne il comportamento è necessario MetaDataRepository.java.
TLLM-005
Comportamento considerato: enhancement completo di una classe con metadati.
Input e setup necessari: classe concreta semplice, non già enhanced, con metadati validi e almeno un campo persistente FIELD.
Azione da eseguire: eseguire run().
Risultato atteso o osservabile: restituzione di ENHANCE_PC; il ClassNode dichiara PersistenceCapable e contiene almeno il metodo pcGetEnhancementContractVersion, i campi statici del protocollo, gli accessor e i metodi PC coordinati da addPCMethods.
TLLM-006
Comportamento considerato: wrapping delle eccezioni non OpenJPA.
Input e setup necessari: configurazione che permetta di raggiungere una collaborazione che produca una Exception non derivata da OpenJPAException, per esempio una firma StateManager non risolvibile in un ambiente controllato.
Azione da eseguire: eseguire run().
Risultato atteso o osservabile: viene generata GeneralException; la causa è l’eccezione originaria e il messaggio localizzato riceve il nome interno del tipo e il messaggio della causa. Le OpenJPAException, invece, non sono avvolte.
B. Ridefinizione, sottoclassi e costruttori
TLLM-007
Comportamento considerato: prima configurazione in modalità redefine.
Input e setup necessari: classe con metadati validi e senza attributo RedefinedAttribute; impostare setRedefine(true).
Azione da eseguire: eseguire run().
Risultato atteso o osservabile: viene aggiunto un RedefinedAttribute; isAlreadyRedefined() resta false. La serializzazione tramite AsmHelper riconosce tale attributo fra quelli supportati.
Dipendenza production: per controllare il tipo e il contenuto completo dell’attributo è necessario RedefinedAttribute.java.
TLLM-008
Comportamento considerato: rilevamento di una classe già ridefinita.
Input e setup necessari: classe con un attributo ASM sconosciuto il cui type corrisponda a RedefinedAttribute.ATTR_TYPE; modalità redefine attiva.
Azione da eseguire: eseguire run().
Risultato atteso o osservabile: isAlreadyRedefined() diventa true e non viene aggiunto un secondo attributo dello stesso tipo.
Dipendenza production: RedefinedAttribute.java è necessario per usare il valore autorevole di ATTR_TYPE.
TLLM-009
Comportamento considerato: rifiuto di una classe senza costruttore no-arg quando l’aggiunta automatica è disabilitata.
Input e setup necessari: classe PC concreta con soli costruttori parametrizzati; setAddDefaultConstructor(false).
Azione da eseguire: eseguire run().
Risultato atteso o osservabile: UserException durante enhanceClass; non viene aggiunto un costruttore sintetico.
TLLM-010
Comportamento considerato: aggiunta e visibilità del costruttore no-arg.
Input e setup necessari: tre varianti dello stesso scenario:
classe detachable;
classe non detachable e final;
classe non detachable e non final.
 Tutte prive di costruttore no-arg e con aggiunta automatica attiva.
Azione da eseguire: eseguire run() e ispezionare il metodo <init>()V.
Risultato atteso o osservabile: il costruttore aggiunto è rispettivamente pubblico, privato o protetto e invoca il costruttore no-arg della superclasse. Le varianti fanno parte dello stesso scenario parametrico sulla regola di visibilità.
C. Accesso PROPERTY e backing field
TLLM-011
Comportamento considerato: riconoscimento del backing field di un getter semplice.
Input e setup necessari: metodo non statico equivalente a una restituzione diretta di this.value; ClassNode contenente la sequenza ALOAD 0, GETFIELD, istruzione di ritorno coerente.
Azione da eseguire: invocare il comportamento package-visible getReturnedField.
Risultato atteso o osservabile: viene restituito il Field reflection corrispondente a value. AsmHelper.isThisInsn riconosce precisamente ALOAD sul local slot zero.
TLLM-012
Comportamento considerato: rifiuto di getter non riconducibili a un unico backing field.
Input e setup necessari: varianti con getter statico, getter dichiarato in interfaccia, getter che restituisce una costante e getter con più ritorni riferiti a campi differenti.
Azione da eseguire: invocare getReturnedField.
Risultato atteso o osservabile: restituzione di null per ciascuna variante, senza inventare una corrispondenza field-property.
TLLM-013
Comportamento considerato: riconoscimento del campo assegnato da un setter semplice.
Input e setup necessari: metodo non statico setValue(T) con istruzioni corrispondenti ad ALOAD 0, load del parametro nel local slot 1 e PUTFIELD.
Azione da eseguire: invocare getAssignedField.
Risultato atteso o osservabile: viene restituito il Field reflection assegnato dal setter.
TLLM-014
Comportamento considerato: creazione del setter sintetico quando PROPERTY ha solo un getter valido.
Input e setup necessari: metadati PROPERTY il cui backing member sia un getter che restituisce direttamente un campo; nessun setter nella gerarchia; redefine disabilitato.
Azione da eseguire: eseguire run() fino alla validazione delle proprietà.
Risultato atteso o osservabile: viene aggiunto un setter privato con nome determinato da FieldMetaData.getSetterName, descriptor coerente col tipo e assegnazione diretta al backing field.
TLLM-015
Comportamento considerato: violazione per mismatch fra getter e setter.
Input e setup necessari: proprietà con getter che legge il campo first e setter che assegna il parametro al campo second; enforcement inizialmente disabilitato e nessun’altra violazione fatale.
Azione da eseguire: eseguire run().
Risultato atteso o osservabile: la violazione viene registrata come non fatale e aggregata; l’enhancement prosegue e viene emesso un warning se il log abilita i warning. I mapping del getter e del setter sono comunque registrati rispetto ai campi individuati.
TLLM-016
Comportamento considerato: violazione fatale per backing member incompatibile con PROPERTY.
Input e setup necessari: metadati di classe PROPERTY non mixed e FieldMetaData il cui backing member sia un Field, non un Method.
Azione da eseguire: eseguire run().
Risultato atteso o osservabile: viene accumulata una violazione fatale e processViolations() genera UserException prima dell’enhancement strutturale. FieldMetaData.backingMember imposta normalmente l’accesso FIELD per un membro Field, quindi il setup deve rappresentare esplicitamente la configurazione metadata incoerente che il validator è destinato a intercettare.
TLLM-017
Comportamento considerato: traduzione degli indici PROPERTY in backing field nella sottoclasse.
Input e setup necessari: metadata PROPERTY non mixed con due proprietà, getter e setter validi e backing field differenti; setCreateSubclass(true).
Azione da eseguire: eseguire run() e ispezionare o eseguire il metodo generato pcAttributeIndexToFieldName.
Risultato atteso o osservabile: la sottoclasse dichiara AttributeTranslator; gli indici 0 e 1 restituiscono i rispettivi nomi dei backing field; un indice fuori intervallo segue il default che genera IllegalArgumentException.
Dipendenza production: AttributeTranslator.java è necessario per verificare formalmente il contratto dell’interfaccia.
D. Riscrittura degli accessi e accessor generati
TLLM-018
Comportamento considerato: sostituzione ordinaria di GETFIELD e PUTFIELD.
Input e setup necessari: classe con un campo persistente FIELD e un metodo ordinario che legge e scrive il campo; redefine e subclass disabilitati.
Azione da eseguire: eseguire run() e ispezionare le istruzioni del metodo.
Risultato atteso o osservabile: GETFIELD viene sostituito con INVOKESTATIC pcGet<nome> e PUTFIELD con INVOKESTATIC pcSet<nome>; gli accessi diretti originali non restano nel metodo.
TLLM-019
Comportamento considerato: esclusione di costruttore e inizializzatore statico dalla riscrittura.
Input e setup necessari: accessi a un campo persistente presenti in <init>, <clinit> e in un metodo ordinario.
Azione da eseguire: eseguire run() e confrontare i tre corpi.
Risultato atteso o osservabile: gli accessi in <init> e <clinit> non vengono riscritti; quelli nel metodo ordinario vengono elaborati.
TLLM-020
Comportamento considerato: comportamento del getter e setter FIELD con e senza state manager.
Input e setup necessari: classe enhanced con campo FIELD soggetto a mediazione; due istanze, una con pcStateManager == null e una con uno StateManager controllabile.
Azione da eseguire: invocare gli accessor generati per leggere e scrivere il campo.
Risultato atteso o osservabile: senza manager, lettura e scrittura avvengono direttamente; con manager, la lettura chiama accessingField e la scrittura chiama il metodo setting<Type>Field con istanza, indice assoluto, valore precedente, valore nuovo e SET_USER, il cui valore production è zero.
E. Campi registrati e protocollo PersistenceCapable
TLLM-021
Comportamento considerato: calcolo e registrazione dei flag dei campi.
Input e setup necessari: una classe con quattro categorie di campo:
MANAGE_NONE;
MANAGE_TRANSACTIONAL;
persistente non-PK fuori dal default fetch group;
persistente PK o nel default fetch group.
Azione da eseguire: effettuare l’enhancement, caricare la classe per attivare <clinit> e osservare i flag passati alla registrazione oppure il campo statico pcFieldFlags.
Risultato atteso o osservabile: i flag base sono rispettivamente -1, CHECK_WRITE, CHECK_WRITE | CHECK_READ, e MEDIATE_WRITE | MEDIATE_READ; per un tipo primitivo o serializzabile viene inoltre aggiunto SERIALIZABLE. I valori production sono CHECK_READ=1, MEDIATE_READ=2, CHECK_WRITE=4, MEDIATE_WRITE=8, SERIALIZABLE=16.
TLLM-022
Comportamento considerato: registrazione della classe concreta presso PCRegistry.
Input e setup necessari: classe concreta PC con almeno un campo dichiarato, alias determinabile e costruttore no-arg.
Azione da eseguire: effettuare l’enhancement, caricare e inizializzare la classe.
Risultato atteso o osservabile: PCRegistry contiene una entry per la classe; nomi e tipi dei campi registrati corrispondono ai campi dichiarati; newInstance usa il prototipo registrato e non restituisce null. La registrazione sostituisce un’eventuale entry precedente per la stessa Class.
TLLM-023
Comportamento considerato: creazione di una nuova istanza con clear e state manager.
Input e setup necessari: classe concreta enhanced con campi persistenti inizializzati dal costruttore a valori non predefiniti; prototipo registrato; state manager controllabile.
Azione da eseguire: richiedere una nuova istanza tramite PCRegistry.newInstance con clear=true.
Risultato atteso o osservabile: viene restituita una nuova istanza; i campi persistenti sono riportati ai valori Java predefiniti e il manager ricevuto è installato. PCRegistry delega al metodo pcNewInstance del prototipo.
TLLM-024
Comportamento considerato: provide e replace di un campo gestito.
Input e setup necessari: classe enhanced con almeno un campo primitivo e un campo reference; state manager controllabile che registri provided<Type>Field e restituisca valori da replace<Type>Field.
Azione da eseguire: invocare pcProvideField e pcReplaceField con indici validi.
Risultato atteso o osservabile: provide passa al manager istanza, indice assoluto e valore corrente; replace assegna al campo il valore restituito dal manager, applicando il cast per il riferimento. Le firme production sono presenti in StateManager.
TLLM-025
Comportamento considerato: validazione degli indici e delle precondizioni di pcCopyFields.
Input e setup necessari: due istanze enhanced della stessa classe e tre varianti:
state manager differenti;
stesso manager nullo;
stesso manager non nullo con indice valido.
Azione da eseguire: invocare pcCopyFields.
Risultato atteso o osservabile: nella prima variante viene generata IllegalArgumentException; nella seconda IllegalStateException; nella terza il valore indicato viene copiato dalla sorgente alla destinazione. Un indice singolo fuori intervallo deve raggiungere il default che genera IllegalArgumentException.
F. Stato, versione e identità
TLLM-026
Comportamento considerato: delega dei metodi standard e valore di default senza manager.
Input e setup necessari: istanza enhanced prima senza manager, poi con uno StateManager controllabile.
Azione da eseguire: invocare almeno pcIsDirty, pcIsPersistent, pcFetchObjectId, pcGetGenericContext e pcDirty.
Risultato atteso o osservabile: senza manager, i booleani restituiscono false, i riferimenti null e pcDirty non produce effetti. Con manager, ogni wrapper delega all’omonimo metodo production. pcIsDirty, quando redefine è falso, esegue anche RedefinitionHelper.dirtyCheck prima della delega.
Dipendenza production: RedefinitionHelper.java è necessario per osservare e interpretare il dirty check aggiuntivo.
TLLM-027
Comportamento considerato: metodi di identità no-op per una classe senza application identity.
Input e setup necessari: classe con ID_DATASTORE o comunque identity type diverso da ID_APPLICATION.
Azione da eseguire: effettuare l’enhancement e invocare i metodi di copia chiavi e creazione oid generati.
Risultato atteso o osservabile: le varianti di copia terminano senza modificare gli argomenti; entrambe le varianti pcNewObjectIdInstance restituiscono null.
TLLM-028
Comportamento considerato: decisione ternaria di pcIsDetached.
Input e setup necessari: un unico scenario parametrico con:
tipo non detachable;
tipo detachable con state manager che restituisce true o false;
tipo detachable senza manager e con detached state ordinario;
tipo detachable senza manager e stato non definitivo.
Azione da eseguire: effettuare l’enhancement e invocare pcIsDetached.
Risultato atteso o osservabile: rispettivamente:
Boolean.FALSE;
Boolean.TRUE o Boolean.FALSE secondo il manager;
Boolean.TRUE se lo stato è non nullo e diverso da PersistenceCapable.DESERIALIZED;
null quando le informazioni generate non permettono una decisione definitiva.
 PersistenceCapable documenta esplicitamente null come stato sconosciuto.
Dipendenza production: per configurare l’ultimo ramo in modo autorevole serve la classe restituita da OpenJPAConfiguration.getDetachStateInstance().
G. Serializzazione, clonazione e output
TLLM-029
Comportamento considerato: aggiunta e funzionamento del supporto alla serializzazione standard.
Input e setup necessari: classe concreta Serializable, non externalize-detached, senza serialVersionUID, writeObject o readObject; metadata con detached state synthetic.
Azione da eseguire: effettuare l’enhancement, ispezionare i membri generati e compiere un ciclo di serializzazione/deserializzazione.
Risultato atteso o osservabile: viene tentata l’aggiunta di serialVersionUID; vengono aggiunti writeObject e readObject; la scrittura chiama pcSerializing; dopo la lettura, il detached state viene posto all’oggetto sentinella PersistenceCapable.DESERIALIZED. Se il calcolo del UID fallisce, l’elemento osservabile previsto è un warning e l’assenza del campo aggiunto.
TLLM-030
Comportamento considerato: destinazione e cardinalità della scrittura del bytecode.
Input e setup necessari: enhancer completato e BytecodeWriter controllabile; due configurazioni:
enhancement ordinario, con managedType == pc;
sottoclasse distinta con redefine attivo, quindi managedType != pc.
Azione da eseguire: configurare il writer ed eseguire record().
Risultato atteso o osservabile: nella prima configurazione il writer riceve un tracker una sola volta; nella seconda riceve prima il managed type e poi il tracker PC. La presenza del writer ha precedenza sulla directory e sulla sovrascrittura della risorsa originale.
Dipendenza production: BytecodeWriter.java e ClassNodeTracker.java sono necessari per definire formalmente il contratto delle invocazioni e le informazioni osservabili dal writer.
Verifica finale
Conteggio
Numero totale dei test case/scenari: 30
Il conteggio riguarda gli scenari progettati, non il numero futuro di metodi Java, helper, fixture o invocazioni parametrizzate.
Sequenza degli identificatori

Sono presenti, senza salti e senza duplicazioni:

Plain Text
1
TLLM-001
2
TLLM-002
3
TLLM-003
4
TLLM-004
5
TLLM-005
6
TLLM-006
7
TLLM-007
8
TLLM-008
9
TLLM-009
10
TLLM-010
11
TLLM-011
12
TLLM-012
13
TLLM-013
14
TLLM-014
15
TLLM-015
16
TLLM-016
17
TLLM-017
18
TLLM-018
19
TLLM-019
20
TLLM-020
21
TLLM-021
22
TLLM-022
23
TLLM-023
24
TLLM-024
25
TLLM-025
26
TLLM-026
27
TLLM-027
28
TLLM-028
29
TLLM-029
30
TLLM-030
Mostra più linee
Dipendenze production ancora mancanti

I sette file prioritari consegnati consentono di definire la maggior parte degli oracoli:

PersistenceCapable.java;
StateManager.java;
PCRegistry.java;
ClassMetaData.java;
FieldMetaData.java;
Reflection.java;
AsmHelper.java.

Per implementare in modo affidabile tutti gli scenari sopra, senza inventare i contratti dei collaboratori, restano necessari:

MetaDataRepository.java
 Necessario soprattutto per TLLM-004 e per costruire lookup metadata autorevoli.

ClassNodeTracker.java
 Necessario per costruzione, class loader, progetto e osservazione del writer, inclusi TLLM-001, TLLM-030 e gran parte degli scenari ASM.

EnhancementProject.java
 Necessario per caricare classi e sottoclassi synthetic secondo il comportamento production.

BytecodeWriter.java
 Necessario per il contratto esatto di TLLM-030.

RedefinedAttribute.java
 Necessario per TLLM-007 e TLLM-008.

RedefinitionHelper.java
 Necessario per gli effetti runtime delle notifiche e per TLLM-026.

AttributeTranslator.java
 Necessario per il contratto formale di TLLM-017.

PCSubclassValidator.java e DynamicPersistenceCapable.java
 Necessari se l’implementazione degli scenari di sottoclasse deve validare anche ammissibilità e contratto della sottoclasse, anziché limitarsi alle trasformazioni direttamente visibili in PCEnhancer.

Classe production restituita da OpenJPAConfiguration.getDetachStateInstance()
 Necessaria per configurare con precisione i rami detached e externalization, in particolare TLLM-028.

OpenJPAConfiguration.java
 Necessario per costruire configurazione, repository, log, resolver, detach-state e OptimizeIdCopy usando soltanto API confermate.

Non è stato scritto codice Java e non sono stati utilizzati test nativi, altre suite sperimentali, dati JaCoCo/PIT, survivor mutation o fonti esterne.
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/* Mechanically integrated from EvoSuite seed-0 raw scaffolding. */

package it.uniroma2.isw2.openjpa.testing.pcenhancer.es;

import org.evosuite.runtime.annotation.EvoSuiteClassExclude;
import org.junit.BeforeClass;
import org.junit.Before;
import org.junit.After;
import org.junit.AfterClass;
import org.evosuite.runtime.sandbox.Sandbox;
import org.evosuite.runtime.sandbox.Sandbox.SandboxMode;

@EvoSuiteClassExclude
public class PCEnhancerEvoSuiteSeed0Scaffolding {

  @org.junit.Rule
  public org.evosuite.runtime.vnet.NonFunctionalRequirementRule nfr = new org.evosuite.runtime.vnet.NonFunctionalRequirementRule();

  private static final java.util.Properties defaultProperties = (java.util.Properties) java.lang.System.getProperties().clone();

  private org.evosuite.runtime.thread.ThreadStopper threadStopper =  new org.evosuite.runtime.thread.ThreadStopper (org.evosuite.runtime.thread.KillSwitchHandler.getInstance(), 3000);


  @BeforeClass
  public static void initEvoSuiteFramework() {
    org.evosuite.runtime.RuntimeSettings.className = "org.apache.openjpa.enhance.PCEnhancer";
    org.evosuite.runtime.GuiSupport.initialize();
    org.evosuite.runtime.RuntimeSettings.maxNumberOfThreads = 100;
    org.evosuite.runtime.RuntimeSettings.maxNumberOfIterationsPerLoop = 10000;
    org.evosuite.runtime.RuntimeSettings.mockSystemIn = true;
    org.evosuite.runtime.RuntimeSettings.sandboxMode = org.evosuite.runtime.sandbox.Sandbox.SandboxMode.RECOMMENDED;
    org.evosuite.runtime.sandbox.Sandbox.initializeSecurityManagerForSUT();
    org.evosuite.runtime.classhandling.JDKClassResetter.init();
    setSystemProperties();
    initializeClasses();
    org.evosuite.runtime.Runtime.getInstance().resetRuntime();
  }

  @AfterClass
  public static void clearEvoSuiteFramework(){
    Sandbox.resetDefaultSecurityManager();
    java.lang.System.setProperties((java.util.Properties) defaultProperties.clone());
  }

  @Before
  public void initTestCase(){
    threadStopper.storeCurrentThreads();
    threadStopper.startRecordingTime();
    org.evosuite.runtime.jvm.ShutdownHookHandler.getInstance().initHandler();
    org.evosuite.runtime.sandbox.Sandbox.goingToExecuteSUTCode();
    setSystemProperties();
    org.evosuite.runtime.GuiSupport.setHeadless();
    org.evosuite.runtime.Runtime.getInstance().resetRuntime();
    org.evosuite.runtime.agent.InstrumentingAgent.activate();
  }

  @After
  public void doneWithTestCase(){
    threadStopper.killAndJoinClientThreads();
    org.evosuite.runtime.jvm.ShutdownHookHandler.getInstance().safeExecuteAddedHooks();
    org.evosuite.runtime.classhandling.JDKClassResetter.reset();
    resetClasses();
    org.evosuite.runtime.sandbox.Sandbox.doneWithExecutingSUTCode();
    org.evosuite.runtime.agent.InstrumentingAgent.deactivate();
    org.evosuite.runtime.GuiSupport.restoreHeadlessMode();
  }

  public static void setSystemProperties() {

    java.lang.System.setProperties((java.util.Properties) defaultProperties.clone());
    java.lang.System.setProperty("user.dir", "C:\\ISW2\\openjpa\\isw2\\testing\\target\\evosuite-pcenhancer\\canonical-seed-0");
    java.lang.System.setProperty("java.io.tmpdir", "C:\\Users\\marco\\AppData\\Local\\Temp\\");
  }

  private static void initializeClasses() {
    org.evosuite.runtime.classhandling.ClassStateSupport.initializeClasses(PCEnhancerEvoSuiteSeed0Scaffolding.class.getClassLoader() ,
      "org.apache.openjpa.lib.util.StreamResourceBundleProvider",
      "org.apache.xbean.asm9.tree.TableSwitchInsnNode",
      "org.apache.openjpa.enhance.PersistenceCapable",
      "org.apache.openjpa.util.asm.ClassNodeTracker",
      "org.apache.openjpa.lib.util.ZipResourceBundleProvider",
      "org.apache.openjpa.enhance.PCEnhancer$Flags",
      "org.apache.openjpa.meta.MetaDataModes",
      "org.apache.openjpa.meta.QueryMetaData",
      "org.apache.openjpa.lib.util.concurrent.ConcurrentReferenceHashMap$Entry",
      "org.apache.xbean.asm9.ClassVisitor",
      "org.apache.openjpa.meta.ClassMetaData",
      "org.apache.openjpa.util.GeneralException",
      "org.apache.xbean.asm9.tree.FrameNode",
      "org.apache.openjpa.lib.util.SizedMap",
      "org.apache.openjpa.lib.conf.ProductDerivation",
      "org.apache.xbean.asm9.MethodVisitor",
      "org.apache.openjpa.lib.conf.ValueListener",
      "org.apache.xbean.asm9.ClassReader",
      "org.apache.openjpa.meta.MetaDataContext",
      "org.apache.openjpa.meta.MetaDataRepository",
      "org.apache.openjpa.lib.log.Log",
      "org.apache.xbean.asm9.tree.LineNumberNode",
      "org.apache.xbean.asm9.tree.InnerClassNode",
      "org.apache.xbean.asm9.tree.MethodInsnNode",
      "org.apache.openjpa.util.UserException",
      "org.apache.openjpa.lib.util.StringUtil",
      "org.apache.openjpa.enhance.PCEnhancer$AuxiliaryEnhancer",
      "org.apache.openjpa.lib.util.collections.AbstractReferenceMap$ReferenceStrength",
      "org.apache.xbean.asm9.tree.LocalVariableAnnotationNode",
      "org.apache.xbean.asm9.tree.IincInsnNode",
      "org.apache.openjpa.lib.util.SimpleResourceBundleProvider",
      "org.apache.xbean.asm9.tree.TryCatchBlockNode",
      "org.apache.openjpa.meta.XMLMetaData",
      "org.apache.xbean.asm9.tree.InvokeDynamicInsnNode",
      "org.apache.openjpa.lib.meta.SourceTracker",
      "org.apache.openjpa.enhance.PCRegistry",
      "org.apache.xbean.asm9.tree.Util",
      "org.apache.openjpa.util.asm.EnhancementClassLoader",
      "org.apache.xbean.asm9.tree.LocalVariableNode",
      "org.apache.xbean.asm9.tree.TypeInsnNode",
      "org.apache.openjpa.lib.util.J2DoPrivHelper",
      "org.apache.openjpa.lib.util.ReferenceMap",
      "org.apache.openjpa.lib.util.Services",
      "org.apache.openjpa.util.OpenJPAException",
      "org.apache.xbean.asm9.tree.InsnNode",
      "org.apache.openjpa.util.MetaDataException",
      "org.apache.openjpa.meta.XMLClassMetaData",
      "org.apache.xbean.asm9.tree.FieldNode",
      "org.apache.openjpa.lib.util.J2DoPrivHelper$38",
      "org.apache.openjpa.meta.NonPersistentMetaData",
      "org.apache.xbean.asm9.Opcodes",
      "org.apache.xbean.asm9.Type",
      "org.apache.xbean.asm9.Attribute",
      "org.apache.openjpa.meta.ValueMetaData",
      "org.apache.openjpa.lib.util.J2DoPrivHelper$5",
      "org.apache.xbean.asm9.Label",
      "org.apache.openjpa.lib.util.concurrent.ConcurrentMap",
      "org.apache.xbean.asm9.ModuleVisitor",
      "org.apache.openjpa.lib.util.git.GitUtils",
      "org.apache.openjpa.lib.util.J2DoPrivHelper$49",
      "org.apache.openjpa.lib.util.JavaVersions",
      "org.apache.openjpa.lib.conf.BootstrapException",
      "org.apache.openjpa.meta.FieldMetaData",
      "org.apache.openjpa.meta.MetaDataFactory",
      "org.apache.openjpa.lib.util.J2DoPrivHelper$40",
      "org.apache.openjpa.lib.util.Files",
      "org.apache.openjpa.util.asm.BytecodeWriter",
      "org.apache.openjpa.lib.conf.Configurable",
      "org.apache.xbean.asm9.tree.VarInsnNode",
      "org.apache.xbean.asm9.tree.TypeAnnotationNode",
      "org.apache.openjpa.lib.util.Closeable",
      "org.apache.openjpa.lib.log.NoneLogFactory$NoneLog",
      "org.apache.xbean.asm9.tree.LookupSwitchInsnNode",
      "org.apache.xbean.asm9.tree.AbstractInsnNode",
      "org.apache.openjpa.util.asm.RedefinedAttribute",
      "org.apache.openjpa.lib.util.J2DoPrivHelper$16",
      "org.apache.openjpa.lib.util.J2DoPrivHelper$15",
      "org.apache.xbean.asm9.TypePath",
      "org.apache.openjpa.lib.conf.Configuration",
      "org.apache.xbean.asm9.tree.MethodNode",
      "org.apache.openjpa.enhance.StateManager",
      "org.apache.openjpa.lib.util.ResourceBundleProvider",
      "org.apache.xbean.asm9.AnnotationVisitor",
      "org.apache.openjpa.lib.util.TypedProperties",
      "org.apache.openjpa.lib.util.ClassUtil",
      "org.apache.xbean.asm9.tree.ModuleNode",
      "org.apache.openjpa.util.InternalException",
      "org.apache.xbean.asm9.tree.FieldInsnNode",
      "org.apache.openjpa.enhance.FieldConsumer",
      "org.apache.xbean.asm9.MethodWriter",
      "org.apache.openjpa.meta.SequenceMetaData",
      "org.apache.xbean.asm9.tree.JumpInsnNode",
      "org.apache.openjpa.util.asm.BCClassWriter",
      "org.apache.openjpa.lib.util.ParseException",
      "org.apache.openjpa.lib.util.concurrent.ConcurrentReferenceHashSet",
      "org.apache.xbean.asm9.Context",
      "org.apache.xbean.asm9.tree.IntInsnNode",
      "org.apache.openjpa.lib.util.concurrent.ConcurrentReferenceHashMap",
      "org.apache.openjpa.lib.util.collections.MapBackedSet",
      "org.apache.openjpa.meta.XMLFieldMetaData",
      "org.apache.xbean.asm9.tree.UnsupportedClassVersionException",
      "org.apache.openjpa.enhance.PCRegistry$RegisterClassListener",
      "org.apache.xbean.asm9.FieldVisitor",
      "org.apache.openjpa.lib.xml.Commentable",
      "org.apache.openjpa.lib.conf.Configurations",
      "org.apache.openjpa.lib.util.Localizer",
      "org.apache.xbean.asm9.RecordComponentVisitor",
      "org.apache.openjpa.meta.Order",
      "org.apache.xbean.asm9.tree.RecordComponentNode",
      "org.apache.openjpa.enhance.FieldSupplier",
      "org.apache.xbean.asm9.tree.LabelNode",
      "org.apache.xbean.asm9.tree.MultiANewArrayInsnNode",
      "org.apache.openjpa.lib.conf.ProductDerivations",
      "org.apache.openjpa.lib.log.LogFactory",
      "org.apache.openjpa.util.asm.AsmHelper",
      "org.apache.openjpa.lib.util.MultiClassLoader",
      "org.apache.xbean.asm9.ClassWriter",
      "org.apache.xbean.asm9.tree.LdcInsnNode",
      "org.apache.openjpa.lib.conf.Configurations$Runnable",
      "org.apache.openjpa.conf.OpenJPAConfiguration",
      "org.apache.openjpa.enhance.PCEnhancer",
      "org.apache.openjpa.util.asm.EnhancementProject",
      "org.apache.openjpa.lib.util.Options",
      "org.apache.openjpa.meta.Extensions",
      "org.apache.xbean.asm9.tree.InsnList",
      "org.apache.openjpa.event.LifecycleEventManager$ListenerList",
      "org.apache.openjpa.lib.util.Localizer$Message",
      "org.apache.xbean.asm9.tree.ClassNode",
      "org.apache.openjpa.util.ExceptionInfo",
      "org.apache.openjpa.lib.log.NoneLogFactory",
      "org.apache.xbean.asm9.tree.AnnotationNode",
      "org.apache.openjpa.lib.util.Options$EmptyOptions"
    );
  }

  private static void resetClasses() {
    org.evosuite.runtime.classhandling.ClassResetter.getInstance().setClassLoader(PCEnhancerEvoSuiteSeed0Scaffolding.class.getClassLoader());

    org.evosuite.runtime.classhandling.ClassStateSupport.resetClasses(
      "org.apache.xbean.asm9.Type",
      "org.apache.openjpa.lib.util.SimpleResourceBundleProvider",
      "org.apache.openjpa.lib.util.StreamResourceBundleProvider",
      "org.apache.openjpa.lib.util.ZipResourceBundleProvider",
      "org.apache.openjpa.lib.util.Localizer",
      "org.apache.openjpa.lib.util.J2DoPrivHelper",
      "org.apache.openjpa.lib.util.J2DoPrivHelper$5",
      "org.apache.openjpa.lib.util.Services",
      "org.apache.openjpa.lib.util.J2DoPrivHelper$15",
      "org.apache.openjpa.lib.util.git.GitUtils",
      "org.apache.openjpa.enhance.PCEnhancer",
      "org.apache.openjpa.enhance.PCEnhancer$Flags",
      "org.apache.xbean.asm9.Attribute",
      "org.apache.openjpa.util.asm.RedefinedAttribute",
      "org.apache.openjpa.util.asm.AsmHelper",
      "org.apache.openjpa.lib.util.collections.AbstractReferenceMap$ReferenceStrength",
      "org.apache.openjpa.lib.util.concurrent.ConcurrentReferenceHashMap",
      "org.apache.openjpa.enhance.Reflection",
      "org.apache.openjpa.meta.AccessCode",
      "org.apache.openjpa.lib.util.J2DoPrivHelper$49",
      "org.apache.openjpa.lib.util.J2DoPrivHelper$16",
      "org.apache.openjpa.lib.util.MultiClassLoader",
      "org.apache.openjpa.lib.util.J2DoPrivHelper$40",
      "org.apache.openjpa.lib.util.Localizer$Message",
      "org.apache.openjpa.lib.conf.ProductDerivations",
      "org.apache.openjpa.lib.conf.Configurations",
      "org.apache.openjpa.lib.util.concurrent.ConcurrentReferenceHashSet",
      "org.apache.openjpa.lib.util.collections.MapBackedSet",
      "org.apache.openjpa.enhance.PCRegistry",
      "org.apache.openjpa.meta.MetaDataRepository",
      "org.apache.openjpa.event.LifecycleEventManager$ListenerList",
      "org.apache.openjpa.kernel.BrokerImpl",
      "org.apache.openjpa.util.asm.EnhancementProject",
      "org.apache.xbean.asm9.ClassReader",
      "org.apache.xbean.asm9.ClassVisitor",
      "org.apache.xbean.asm9.tree.ClassNode",
      "org.apache.xbean.asm9.Context",
      "org.apache.xbean.asm9.tree.Util",
      "org.apache.xbean.asm9.tree.InnerClassNode",
      "org.apache.xbean.asm9.FieldVisitor",
      "org.apache.xbean.asm9.tree.FieldNode",
      "org.apache.xbean.asm9.MethodVisitor",
      "org.apache.xbean.asm9.tree.MethodNode",
      "org.apache.xbean.asm9.tree.InsnList",
      "org.apache.xbean.asm9.Label",
      "org.apache.xbean.asm9.tree.AbstractInsnNode",
      "org.apache.xbean.asm9.tree.LabelNode",
      "org.apache.xbean.asm9.tree.LineNumberNode",
      "org.apache.xbean.asm9.tree.VarInsnNode",
      "org.apache.xbean.asm9.tree.MethodInsnNode",
      "org.apache.xbean.asm9.tree.InsnNode",
      "org.apache.xbean.asm9.tree.FieldInsnNode",
      "org.apache.xbean.asm9.tree.TypeInsnNode",
      "org.apache.xbean.asm9.tree.IntInsnNode",
      "org.apache.xbean.asm9.tree.LdcInsnNode",
      "org.apache.xbean.asm9.tree.JumpInsnNode",
      "org.apache.xbean.asm9.tree.FrameNode",
      "org.apache.xbean.asm9.tree.LocalVariableNode",
      "org.apache.xbean.asm9.tree.LookupSwitchInsnNode",
      "org.apache.xbean.asm9.tree.TryCatchBlockNode",
      "org.apache.xbean.asm9.Opcodes",
      "org.apache.xbean.asm9.tree.IincInsnNode",
      "org.apache.openjpa.util.asm.ClassNodeTracker",
      "org.apache.openjpa.util.asm.EnhancementClassLoader",
      "org.apache.openjpa.lib.util.J2DoPrivHelper$1",
      "org.apache.openjpa.lib.conf.ConfigurationImpl",
      "org.apache.openjpa.conf.OpenJPAConfigurationImpl",
      "org.apache.openjpa.lib.conf.Value",
      "org.apache.openjpa.lib.conf.ObjectValue",
      "org.apache.openjpa.lib.conf.PluginValue",
      "org.apache.openjpa.lib.util.StringUtil",
      "org.apache.openjpa.lib.conf.StringValue",
      "org.apache.openjpa.util.StoreFacadeTypeRegistry",
      "org.apache.openjpa.lib.util.concurrent.AbstractConcurrentEventManager",
      "org.apache.openjpa.event.BrokerFactoryEventManager",
      "org.apache.openjpa.conf.BrokerFactoryValue",
      "org.apache.openjpa.lib.util.ClassUtil",
      "org.apache.openjpa.kernel.exps.QueryExpressions",
      "org.apache.openjpa.lib.util.collections.AbstractHashedMap",
      "org.apache.openjpa.lib.util.collections.AbstractLinkedMap",
      "org.apache.openjpa.lib.util.collections.LinkedMap",
      "org.apache.openjpa.lib.util.collections.AbstractHashedMap$HashEntry",
      "org.apache.openjpa.lib.util.collections.AbstractLinkedMap$LinkEntry",
      "org.apache.openjpa.lib.util.OrderedMap",
      "org.apache.openjpa.kernel.StoreQuery",
      "org.apache.openjpa.util.ClassResolverImpl",
      "org.apache.openjpa.kernel.FinalizingBrokerImpl",
      "org.apache.openjpa.kernel.DelegatingBroker",
      "org.apache.openjpa.util.ImplHelper$1",
      "org.apache.openjpa.util.ImplHelper",
      "org.apache.openjpa.kernel.DelegatingExtent",
      "org.apache.openjpa.lib.util.TypedProperties",
      "org.apache.openjpa.lib.util.Options$EmptyOptions",
      "org.apache.openjpa.lib.util.Options",
      "org.apache.openjpa.meta.NoneMetaDataFactory",
      "org.apache.openjpa.lib.meta.ClassArgParser",
      "org.apache.openjpa.meta.Extensions",
      "org.apache.openjpa.meta.ClassMetaData",
      "org.apache.openjpa.meta.LifecycleMetaData",
      "org.apache.openjpa.kernel.AbstractBrokerFactory",
      "org.apache.openjpa.util.OpenJPAException",
      "org.apache.openjpa.util.UserException",
      "org.apache.openjpa.util.InvalidStateException",
      "org.apache.openjpa.util.NoTransactionException",
      "org.apache.openjpa.lib.util.J2DoPrivHelper$9",
      "org.apache.openjpa.kernel.jpql.JPQLParser",
      "org.apache.openjpa.kernel.AbstractStoreQuery",
      "org.apache.openjpa.kernel.exps.StringContains",
      "org.apache.openjpa.kernel.exps.WildcardMatch",
      "org.apache.openjpa.kernel.ExpressionStoreQuery",
      "org.apache.openjpa.kernel.exps.Val",
      "org.apache.openjpa.kernel.exps.Null",
      "org.apache.openjpa.kernel.exps.InMemoryExpressionFactory",
      "org.apache.openjpa.lib.log.JULLogFactory",
      "org.apache.openjpa.lib.log.JULLogFactory$JULLog",
      "org.apache.xbean.asm9.AnnotationVisitor",
      "org.apache.xbean.asm9.tree.AnnotationNode",
      "org.apache.openjpa.util.MetaDataException",
      "org.apache.xbean.asm9.RecordComponentVisitor",
      "org.apache.xbean.asm9.tree.RecordComponentNode",
      "org.apache.openjpa.kernel.StateManagerImpl",
      "org.apache.openjpa.kernel.PNewState",
      "org.apache.openjpa.kernel.PCleanState",
      "org.apache.openjpa.kernel.PDirtyState",
      "org.apache.openjpa.kernel.PDeletedState",
      "org.apache.openjpa.kernel.PNewDeletedState",
      "org.apache.openjpa.kernel.PNewProvisionalState",
      "org.apache.openjpa.kernel.PNonTransState",
      "org.apache.openjpa.kernel.PNonTransDirtyState",
      "org.apache.openjpa.kernel.PNonTransNewState",
      "org.apache.openjpa.kernel.PNonTransDeletedState",
      "org.apache.openjpa.kernel.HollowState",
      "org.apache.openjpa.kernel.TransientState",
      "org.apache.openjpa.kernel.TCleanState",
      "org.apache.openjpa.kernel.TDirtyState",
      "org.apache.openjpa.kernel.TLoadedState",
      "org.apache.openjpa.kernel.ECopyState",
      "org.apache.openjpa.kernel.ECleanState",
      "org.apache.openjpa.kernel.EDirtyState",
      "org.apache.openjpa.kernel.EDeletedState",
      "org.apache.openjpa.kernel.ENonTransState",
      "org.apache.openjpa.kernel.PNewFlushedDeletedState",
      "org.apache.openjpa.kernel.PNewFlushedDeletedFlushedState",
      "org.apache.openjpa.kernel.PDeletedFlushedState",
      "org.apache.openjpa.kernel.PCState",
      "org.apache.openjpa.kernel.TransferFieldManager",
      "org.apache.openjpa.kernel.SingleFieldManager",
      "org.apache.openjpa.lib.util.J2DoPrivHelper$38",
      "org.apache.openjpa.lib.util.JavaVersions",
      "org.apache.openjpa.lib.util.Files",
      "org.apache.xbean.asm9.ClassWriter",
      "org.apache.openjpa.util.asm.BCClassWriter",
      "org.apache.xbean.asm9.SymbolTable",
      "org.apache.xbean.asm9.ByteVector",
      "org.apache.xbean.asm9.Symbol",
      "org.apache.xbean.asm9.SymbolTable$Entry",
      "org.apache.xbean.asm9.AnnotationWriter",
      "org.apache.openjpa.kernel.jpql.JPQLExpressionBuilder$ParsedJPQL",
      "org.apache.openjpa.lib.util.StringDistance",
      "org.apache.xbean.asm9.tree.ParameterNode",
      "org.apache.openjpa.kernel.exps.CandidatePath",
      "org.apache.openjpa.meta.NonPersistentMetaData",
      "org.apache.openjpa.lib.log.NoneLogFactory$NoneLog",
      "org.apache.openjpa.util.InternalException",
      "org.apache.openjpa.lib.log.NoneLogFactory",
      "org.apache.openjpa.lib.log.LogFactoryImpl",
      "org.apache.openjpa.lib.log.AbstractLog",
      "org.apache.openjpa.lib.log.LogFactoryImpl$LogImpl",
      "org.apache.openjpa.kernel.exps.SubQ",
      "org.apache.openjpa.util.MultiLoaderClassResolver",
      "org.apache.openjpa.lib.util.J2DoPrivHelper$7",
      "org.apache.xbean.asm9.tree.ModuleOpenNode",
      "org.apache.xbean.asm9.ModuleVisitor",
      "org.apache.xbean.asm9.tree.ModuleNode",
      "org.apache.openjpa.kernel.FetchConfigurationImpl",
      "org.apache.openjpa.kernel.FetchConfigurationImpl$ConfigurationState",
      "org.apache.openjpa.kernel.DataCacheRetrieveMode",
      "org.apache.openjpa.kernel.DataCacheStoreMode",
      "org.apache.xbean.asm9.commons.JSRInlinerAdapter",
      "org.apache.openjpa.enhance.ReflectingPersistenceCapable",
      "org.apache.openjpa.kernel.DetachedValueStateManager",
      "org.apache.openjpa.meta.SequenceMetaData",
      "org.apache.openjpa.lib.util.concurrent.ConcurrentReferenceHashMap$4",
      "org.apache.openjpa.lib.util.concurrent.ConcurrentReferenceHashMap$WeakEntry",
      "org.apache.openjpa.lib.util.J2DoPrivHelper$11",
      "org.apache.openjpa.kernel.MethodStoreQuery",
      "org.apache.openjpa.kernel.QueryImpl",
      "org.apache.openjpa.lib.util.ReferenceHashSet$1",
      "org.apache.openjpa.lib.util.ReferenceHashSet",
      "org.apache.openjpa.lib.util.collections.AbstractReferenceMap",
      "org.apache.openjpa.lib.util.collections.ReferenceMap",
      "org.apache.openjpa.util.GeneralException",
      "org.apache.openjpa.lib.util.J2DoPrivHelper$14",
      "org.apache.openjpa.lib.log.LogFactoryAdapter",
      "org.apache.openjpa.lib.log.Log4JLogFactory",
      "org.apache.openjpa.kernel.exps.AbstractExpressionBuilder",
      "org.apache.openjpa.kernel.jpql.JPQLExpressionBuilder",
      "org.apache.openjpa.kernel.ExpressionStoreQuery$1",
      "org.apache.openjpa.kernel.exps.Context"
    );
  }
}

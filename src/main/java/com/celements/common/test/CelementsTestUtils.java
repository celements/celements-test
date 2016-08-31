package com.celements.common.test;

import static org.easymock.EasyMock.*;

import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.velocity.VelocityContext;
import org.easymock.IAnswer;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.manager.ComponentRepositoryException;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.test.MockConfigurationSource;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.PropertyClass;
import com.xpn.xwiki.store.XWikiStoreInterface;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiMessageTool;

public class CelementsTestUtils {

  public static final String EXECUTIONCONTEXT_KEY_MOCKS = "default_mocks";
  public static final String DEFAULT_DB = "xwikidb";
  public static final String DEFAULT_MAIN_WIKI = "xwikiWiki";
  public static final String DEFAULT_LANG = "de";

  private static ExecutionContext getExecutionContext() {
    return Utils.getComponent(Execution.class).getContext();
  }

  public static DefaultMocks getDefaultMocks() {
    DefaultMocks defaultMocks = (DefaultMocks) getExecutionContext().getProperty(
        EXECUTIONCONTEXT_KEY_MOCKS);
    if (defaultMocks == null) {
      defaultMocks = new DefaultMocks();
      getExecutionContext().setProperty(EXECUTIONCONTEXT_KEY_MOCKS, defaultMocks);
    }
    return defaultMocks;
  }

  public static <T> T createMockAndAddToDefault(Class<T> toMock) {
    return createMockAndAddToDefault(toMock, "default");
  }

  public static <T> T createMockAndAddToDefault(Class<T> toMock, String name) {
    T newMock = createMock(toMock);
    getDefaultMocks().put(getDescriptor(toMock, name), newMock);
    return newMock;
  }

  public static <T> T getMock(final Class<T> mockClass) {
    return getMock(mockClass, "default");
  }

  public static <T> T getMock(final Class<T> mockClass, String name) {
    return getDefaultMocks().get(getDescriptor(mockClass, name));
  }

  public static XWiki getWikiMock() {
    XWiki wikiMock = getContext().getWiki();
    if (wikiMock == null) {
      wikiMock = createMockAndAddToDefault(XWiki.class);
      getContext().setWiki(wikiMock);
    }
    return wikiMock;
  }

  @SuppressWarnings("unchecked")
  public static XWikiContext getContext() {
    XWikiContext context = (XWikiContext) getExecutionContext().getProperty(
        XWikiContext.EXECUTIONCONTEXT_KEY);
    if (context == null) {
      context = new XWikiContext();
      context.setDatabase(DEFAULT_DB);
      context.setMainXWiki(DEFAULT_MAIN_WIKI);
      context.setLanguage(DEFAULT_LANG);
      Locale locale = new Locale(context.getLanguage());
      ResourceBundle bundle = ResourceBundle.getBundle("ApplicationResources", locale);
      if (bundle == null) {
        bundle = ResourceBundle.getBundle("ApplicationResources");
      }
      XWikiMessageTool msg = new TestMessageTool(bundle, context);
      context.put("msg", msg);
      VelocityContext vcontext = ((VelocityContext) context.get("vcontext"));
      if (vcontext != null) {
        vcontext.put("msg", msg);
        vcontext.put("locale", locale);
      }
      Map<String, Object> gcontext = (Map<String, Object>) context.get("gcontext");
      if (gcontext != null) {
        gcontext.put("msg", msg);
        gcontext.put("locale", locale);
      }
      context.put(ComponentManager.class.getName(), Utils.getComponentManager());
      getExecutionContext().setProperty(XWikiContext.EXECUTIONCONTEXT_KEY, context);
    }
    return context;
  }

  public static XWikiStoreInterface getStoreMock() throws ComponentRepositoryException {
    XWikiStoreInterface storeMock = getMock(XWikiStoreInterface.class);
    if (storeMock == null) {
      storeMock = registerComponentMock(XWikiStoreInterface.class);
      expect(getWikiMock().getStore()).andReturn(storeMock).anyTimes();
    }
    return storeMock;
  }

  public static TestMessageTool getMessageToolStub() {
    XWikiMessageTool msgTool = getContext().getMessageTool();
    if (msgTool instanceof TestMessageTool) {
      return (TestMessageTool) msgTool;
    }
    return null;
  }

  public static XWikiDocument createDocMock(DocumentReference docRef) {
    XWikiDocument docMock = createMockAndAddToDefault(XWikiDocument.class);
    expect(docMock.getDocumentReference()).andReturn(docRef).anyTimes();
    return docMock;
  }

  /**
   * @deprecated instead use expectNewBaseObject(DocumentReference)
   */
  @Deprecated
  public static BaseClass expectNewBaseObject(AbstractBridgedComponentTestCase testCase,
      final DocumentReference classRef) throws XWikiException {
    return expectNewBaseObject(classRef);
  }

  public static BaseClass expectNewBaseObject(final DocumentReference classRef)
      throws XWikiException {
    BaseClass bClass = createBaseClassMock(classRef);
    expect(bClass.newCustomClassInstance(same(getContext()))).andAnswer(new IAnswer<BaseObject>() {

      @Override
      public BaseObject answer() throws Throwable {
        BaseObject bObj = new BaseObject();
        bObj.setXClassReference(classRef);
        return bObj;
      }
    }).anyTimes();
    return bClass;
  }

  /**
   * @deprecated instead use expectPropertyClass(DocumentReference, String, PropertyClass)
   */
  @Deprecated
  public static BaseClass expectPropertyClass(AbstractBridgedComponentTestCase testCase,
      DocumentReference classRef, String fieldName, PropertyClass propClass) throws XWikiException {
    return expectPropertyClass(classRef, fieldName, propClass);
  }

  public static BaseClass expectPropertyClass(DocumentReference classRef, String fieldName,
      PropertyClass propClass) throws XWikiException {
    BaseClass bClass = createBaseClassMock(classRef);
    expectPropertyClass(bClass, fieldName, propClass);
    return bClass;
  }

  public static BaseClass expectPropertyClass(BaseClass bClass, String fieldName,
      PropertyClass propClass) {
    expect(bClass.get(eq(fieldName))).andReturn(propClass).anyTimes();
    return bClass;
  }

  public static BaseClass expectPropertyClasses(DocumentReference classRef,
      Map<String, PropertyClass> fieldMap) throws XWikiException {
    BaseClass bClass = createBaseClassMock(classRef);
    expectPropertyClasses(bClass, fieldMap);
    return bClass;
  }

  public static BaseClass expectPropertyClasses(BaseClass bClass,
      Map<String, PropertyClass> fieldMap) {
    for (String fieldName : fieldMap.keySet()) {
      expectPropertyClass(bClass, fieldName, fieldMap.get(fieldName));
    }
    return bClass;
  }

  /**
   * @deprecated instead use createBaseClassMock(DocumentReference)
   */
  @Deprecated
  public static BaseClass createBaseClassMock(AbstractBridgedComponentTestCase testCase,
      DocumentReference classRef) throws XWikiException {
    return createBaseClassMock(classRef);
  }

  public static BaseClass createBaseClassMock(DocumentReference classRef) throws XWikiException {
    BaseClass bClass = createMockAndAddToDefault(BaseClass.class);
    expect(getWikiMock().getXClass(eq(classRef), same(getContext()))).andReturn(bClass).anyTimes();
    expect(bClass.getXClassReference()).andReturn(classRef).anyTimes();
    return bClass;
  }

  public static void setConfigSrcProperty(String key, Object value) {
    ((MockConfigurationSource) Utils.getComponent(ConfigurationSource.class)).setProperty(key,
        value);
  }

  public static void registerComponentMocks(Class<?>... roles) throws ComponentRepositoryException {
    for (Class<?> role : roles) {
      registerComponentMock(role);
    }
  }

  public static <T> T registerComponentMock(Class<T> role) throws ComponentRepositoryException {
    return registerComponentMock(role, "default");
  }

  public static <T> T registerComponentMock(Class<T> role, String hint)
      throws ComponentRepositoryException {
    return registerComponent(role, hint, createMockAndAddToDefault(role, hint));
  }

  /**
   * @deprecated instead use {@link #registerComponent(Class, String, Object)} due to misleading
   *             naming
   */
  @Deprecated
  public static <T> T registerComponentMock(Class<T> role, String hint, T componentMock)
      throws ComponentRepositoryException {
    return registerComponent(role, hint, componentMock);
  }

  public static <T> T registerComponent(Class<T> role, String hint, T component)
      throws ComponentRepositoryException {
    Utils.getComponentManager().registerComponent(getDescriptor(role, hint), component);
    return component;
  }

  public static <T> ComponentDescriptor<T> getDescriptor(Class<T> role, String hint) {
    DefaultComponentDescriptor<T> descriptor = new DefaultComponentDescriptor<>();
    descriptor.setRole(role);
    descriptor.setRoleHint(hint);
    return descriptor;
  }

  public static void replayDefault(Object... mocks) {
    replay(getDefaultMocks().getAll().toArray());
    replay(mocks);
  }

  public static void verifyDefault(Object... mocks) {
    verify(getDefaultMocks().getAll().toArray());
    verify(mocks);
  }

}

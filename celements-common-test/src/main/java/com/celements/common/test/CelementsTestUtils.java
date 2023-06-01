package com.celements.common.test;

import static com.google.common.base.Preconditions.*;
import static org.easymock.EasyMock.*;

import java.util.Map;
import java.util.Map.Entry;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.easymock.EasyMock;
import org.xwiki.component.descriptor.ComponentRole;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.manager.ComponentRepositoryException;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.test.MockConfigurationSource;

import com.google.common.base.Strings;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.ListClass;
import com.xpn.xwiki.objects.classes.PropertyClass;
import com.xpn.xwiki.store.XWikiStoreInterface;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiMessageTool;

public final class CelementsTestUtils {

  private CelementsTestUtils() {}

  /**
   * @deprecated since 6.0 instead use {@link AbstractBaseComponentTest#getDefaultMocks}
   */
  @Deprecated
  public static CelDefaultMocks getDefaultMocks() {
    return Utils.getComponent(CelDefaultMocks.class);
  }

  /**
   * @deprecated since 6.0 instead use {@link AbstractBaseComponentTest#createDefaultMock}
   */
  @Deprecated
  public static <T> T createMockAndAddToDefault(final Class<T> toMock) {
    T newMock = EasyMock.createMock(toMock);
    getDefaultMocks().add(newMock);
    return newMock;
  }

  /**
   * @deprecated since 6.0 instead use {@link AbstractBaseComponentTest#getMock}
   */
  @Deprecated
  public static <T> T getMock(final Class<T> mockClass) {
    return getDefaultMocks().get(mockClass);
  }

  /**
   * @deprecated since 6.0 instead use {@link AbstractBaseComponentTest#getMock(XWiki.class)}
   */
  @Deprecated
  public static XWiki getWikiMock() {
    XWiki wikiMock = getDefaultMocks().get(XWiki.class);
    if (wikiMock == null) {
      wikiMock = createMockAndAddToDefault(XWiki.class);
      getContext().setWiki(wikiMock);
    }
    return wikiMock;
  }

  /**
   * @deprecated since 6.0 instead use {@link AbstractComponentTest#getXContext()}
   */
  @Deprecated
  public static XWikiContext getContext() {
    return (XWikiContext) Utils.getComponent(Execution.class).getContext()
        .getProperty(XWikiContext.EXECUTIONCONTEXT_KEY);
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

  public static BaseClass expectNewBaseObject(final DocumentReference classRef)
      throws XWikiException {
    BaseClass bClass = createBaseClassMock(classRef);
    expect(bClass.newCustomClassInstance(same(getContext()))).andAnswer(() -> {
      BaseObject bObj = new BaseObject();
      bObj.setXClassReference(classRef);
      return bObj;
    }).anyTimes();
    return bClass;
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
    for (Entry<String, PropertyClass> fieldEntry : fieldMap.entrySet()) {
      expectPropertyClass(bClass, fieldEntry.getKey(), fieldEntry.getValue());
    }
    return bClass;
  }

  public static @NotNull BaseClass adjustDefaultListSeparator(@NotNull BaseClass bClass,
      @NotEmpty String fieldName) {
    return adjustListSeparator(bClass, fieldName, "|");
  }

  public static @NotNull BaseClass adjustListSeparator(@NotNull BaseClass bClass,
      @NotEmpty String fieldName, @NotEmpty String separator) {
    checkNotNull(bClass);
    checkArgument(!Strings.isNullOrEmpty(fieldName));
    checkArgument(!Strings.isNullOrEmpty(separator));
    ((ListClass) bClass.get(fieldName)).setSeparators(separator);
    return bClass;
  }

  public static BaseClass createBaseClassMock(DocumentReference classRef) throws XWikiException {
    BaseClass bClass = createMockAndAddToDefault(BaseClass.class);
    expect(getWikiMock().getXClass(eq(classRef), same(getContext()))).andReturn(bClass).anyTimes();
    expect(bClass.getXClassReference()).andReturn(classRef).anyTimes();
    return bClass;
  }

  public static void setConfigSrcProperty(String key, Object value) {
    Utils.getComponent(MockConfigurationSource.class).setProperty(key, value);
  }

  /**
   * @deprecated since 6.0 instead use {@link AbstractBaseComponentTest#registerComponentMocks}
   */
  @Deprecated
  public static void registerComponentMocks(Class<?>... roles) throws ComponentRepositoryException {
    for (Class<?> role : roles) {
      registerComponentMock(role);
    }
  }

  /**
   * @deprecated since 6.0 instead use {@link AbstractBaseComponentTest#registerComponentMock}
   */
  @Deprecated
  public static <T> T registerComponentMock(Class<T> role) throws ComponentRepositoryException {
    return registerComponentMock(role, ComponentRole.DEFAULT_HINT);
  }

  /**
   * @deprecated since 6.0 instead use {@link AbstractBaseComponentTest#registerComponentMock}
   */
  @Deprecated
  public static <T> T registerComponentMock(Class<T> role, String hint)
      throws ComponentRepositoryException {
    return registerComponentMock(role, hint, createMockAndAddToDefault(role));
  }

  /**
   * @deprecated since 6.0 instead use {@link AbstractBaseComponentTest#registerComponentMock}
   */
  @Deprecated
  @SuppressWarnings("unchecked")
  public static <T> T registerComponentMock(Class<T> role, String hint, T instance)
      throws ComponentRepositoryException {
    DefaultComponentDescriptor<T> descriptor = new DefaultComponentDescriptor<>();
    descriptor.setRole(role);
    descriptor.setRoleHint(hint);
    if (instance != null) {
      descriptor.setImplementation((Class<T>) instance.getClass());
    }
    Utils.getComponent(ComponentManager.class).registerComponent(descriptor, instance);
    return instance;
  }

  /**
   * @deprecated since 6.0 instead use {@link AbstractBaseComponentTest#replayDefault}
   */
  @Deprecated
  public void replayDefault(Object... mocks) {
    getDefaultMocks().stream().forEach(EasyMock::replay);
    EasyMock.replay(mocks);
  }

  /**
   * @deprecated since 6.0 instead use {@link AbstractBaseComponentTest#verifyDefault}
   */
  @Deprecated
  public void verifyDefault(Object... mocks) {
    getDefaultMocks().stream().forEach(EasyMock::verify);
    EasyMock.verify(mocks);
  }

  /**
   * @deprecated since 6.0 instead use {@link AbstractBaseComponentTest#resetDefault}
   */
  @Deprecated
  public void resetDefault(Object... mocks) {
    getDefaultMocks().stream().forEach(EasyMock::reset);
    EasyMock.reset(mocks);
  }

}

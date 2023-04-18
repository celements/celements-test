package com.celements.common.test;

import static com.google.common.base.Preconditions.*;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.xwiki.component.descriptor.ComponentRole;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.manager.ComponentRepositoryException;

import com.celements.spring.context.CelSpringContext;

public final class CelementsSpringTestUtil {

  public static final String DEFAULT_MOCKS_BEAN_NAME = "celementsTestDefaultMocks";

  private static CelSpringContext context;

  private CelementsSpringTestUtil() {}

  public static CelSpringContext getContext() {
    checkState(context != null);
    return context;
  }

  static void setContext(CelSpringContext ctx) {
    checkState(context == null);
    context = ctx;
  }

  static CelSpringContext removeContext() {
    CelSpringContext ret = getContext();
    resetDefault(); // let's reset the mocks here to avoid memory leaks into the ClassLoader
    getDefaultMocks().clear();
    context = null;
    return ret;
  }

  @SuppressWarnings("unchecked")
  public static List<Object> getDefaultMocks() {
    try {
      return getContext().getBean(DEFAULT_MOCKS_BEAN_NAME, List.class);
    } catch (NoSuchBeanDefinitionException exc) {
      List<Object> mocks = new ArrayList<>();
      context.getBeanFactory().registerSingleton(DEFAULT_MOCKS_BEAN_NAME, mocks);
      return mocks;
    }
  }

  public static <T> T createDefaultMock(final Class<T> toMock) {
    T newMock = EasyMock.createMock(toMock);
    getDefaultMocks().add(newMock);
    return newMock;
  }

  public static <T> T getMock(final Class<T> mockClass) {
    return getDefaultMocks().stream()
        .filter(mockClass::isInstance)
        .map(mockClass::cast)
        .findFirst()
        .orElse(null);
  }

  public static void registerComponentMocks(Class<?>... roles) throws ComponentRepositoryException {
    for (Class<?> role : roles) {
      registerComponentMock(role);
    }
  }

  public static <T> T registerComponentMock(Class<T> role) throws ComponentRepositoryException {
    return registerComponentMock(role, ComponentRole.DEFAULT_HINT);
  }

  public static <T> T registerComponentMock(Class<T> role, String hint)
      throws ComponentRepositoryException {
    return registerComponent(role, hint, createDefaultMock(role));
  }

  @SuppressWarnings("unchecked")
  public static <T> T registerComponent(Class<T> role, String hint, T instance)
      throws ComponentRepositoryException {
    DefaultComponentDescriptor<T> descriptor = new DefaultComponentDescriptor<>();
    descriptor.setRole(role);
    descriptor.setRoleHint(hint);
    if (instance != null) {
      descriptor.setImplementation((Class<T>) instance.getClass());
    }
    getContext().getBean(ComponentManager.class).registerComponent(descriptor, instance);
    return instance;
  }

  public static void replayDefault(Object... mocks) {
    getDefaultMocks().forEach(EasyMock::replay);
    EasyMock.replay(mocks);
  }

  public static void verifyDefault(Object... mocks) {
    getDefaultMocks().forEach(EasyMock::verify);
    EasyMock.verify(mocks);
  }

  public static void resetDefault(Object... mocks) {
    getDefaultMocks().forEach(EasyMock::reset);
    EasyMock.reset(mocks);
  }

}

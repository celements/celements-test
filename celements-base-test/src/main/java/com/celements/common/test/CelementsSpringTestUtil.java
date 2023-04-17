package com.celements.common.test;

import static com.google.common.base.Preconditions.*;
import static org.easymock.EasyMock.*;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.easymock.EasyMock;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.xwiki.component.descriptor.ComponentRole;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.manager.ComponentRepositoryException;

import com.celements.spring.context.CelSpringContext;

public final class CelementsSpringTestUtil {

  private static final ThreadLocal<CelSpringContext> CONTEXT = new ThreadLocal<>();

  private static final String PREFIX = "com.celements.common.test.Mock|||";

  private CelementsSpringTestUtil() {}

  public static CelSpringContext getContext() {
    CelSpringContext ctx = CONTEXT.get();
    checkState(ctx != null);
    return ctx;
  }

  static void setContext(CelSpringContext ctx) {
    checkState(CONTEXT.get() == null);
    CONTEXT.set(ctx);
  }

  static CelSpringContext removeContext() {
    CelSpringContext removed = CONTEXT.get();
    if (removed != null) {
      resetDefault(); // let's reset the mocks here to avoid memory leaks into the ClassLoader
    }
    CONTEXT.remove();
    return removed;
  }

  public static Stream<Object> streamDefaultMocks() {
    ConfigurableListableBeanFactory factory = getContext().getBeanFactory();
    Spliterator<String> beanNameSpliter = Spliterators.spliteratorUnknownSize(
        factory.getBeanNamesIterator(), Spliterator.ORDERED);
    return StreamSupport.stream(beanNameSpliter, false)
        .filter(name -> name.startsWith(PREFIX))
        .map(factory::getBean);
  }

  public static <T> T createDefaultMock(final Class<T> toMock) {
    T newMock = EasyMock.createMock(toMock);
    String beanName = PREFIX + toMock.getClass().getName();
    getContext().getBeanFactory().registerSingleton(beanName, newMock);
    return newMock;
  }

  public static <T> T getMock(final Class<T> mockClass) {
    return streamDefaultMocks()
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
    streamDefaultMocks().forEach(EasyMock::replay);
    replay(mocks);
  }

  public static void verifyDefault(Object... mocks) {
    streamDefaultMocks().forEach(EasyMock::verify);
    EasyMock.verify(mocks);
  }

  public static void resetDefault(Object... mocks) {
    streamDefaultMocks().forEach(EasyMock::reset);
    EasyMock.reset(mocks);
  }

}

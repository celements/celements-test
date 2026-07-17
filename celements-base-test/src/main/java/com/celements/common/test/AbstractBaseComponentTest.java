package com.celements.common.test;

import static com.google.common.base.Preconditions.*;

import java.util.List;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;
import org.xwiki.component.descriptor.ComponentRole;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.manager.ComponentRepositoryException;

import com.celements.spring.context.CelSpringContext;

/**
 * Prepares the Spring testing environment.
 */
public abstract class AbstractBaseComponentTest {

  private ConfigurableApplicationContext context;
  private SpringContextCache.ContextLease contextLease;

  @Before
  public final void setUpSpring() throws Exception {
    checkState(context == null);
    contextLease = SpringContextCache.acquire(this::initializeSpringContext);
    context = contextLease.context();
  }

  private ConfigurableApplicationContext initializeSpringContext() throws Exception {
    context = createSpringContext();
    context.getEnvironment().setActiveProfiles("test");
    beforeSpringContextRefresh();
    context.refresh();
    return context;
  }

  /**
   * Entry point for initialising a different spring context.
   */
  protected ConfigurableApplicationContext createSpringContext() throws Exception {
    return new CelSpringContext(
        new GenerationAwareBeanFactory(),
        new FullyQualifiedAnnotationBeanNameGenerator(),
        List.of());
  }

  /**
   * Entry point for handling logic pre context refresh.
   */
  protected void beforeSpringContextRefresh() throws Exception {}

  @After
  public final void tearDownSpring() throws Exception {
    try {
      resetDefault();
      getDefaultMocks().clear();
    } finally {
      var lease = contextLease;
      context = null;
      contextLease = null;
      if (lease != null) {
        lease.close();
      }
    }
  }

  public ConfigurableApplicationContext getSpringContext() {
    checkState(context != null);
    return context;
  }

  public ConfigurableListableBeanFactory getBeanFactory() {
    checkState(context != null);
    return context.getBeanFactory();
  }

  public ComponentManager getComponentManager() {
    return getBeanFactory().getBean(ComponentManager.class);
  }

  public CelDefaultMocks getDefaultMocks() {
    return getBeanFactory().getBean(CelDefaultMocks.class);
  }

  public <T> T createDefaultMock(final Class<T> toMock) {
    T newMock = EasyMock.createMock(toMock);
    getDefaultMocks().add(newMock);
    return newMock;
  }

  public <T> T getMock(final Class<T> mockClass) {
    return getDefaultMocks().get(mockClass);
  }

  public void registerComponentMocks(Class<?>... roles) throws ComponentRepositoryException {
    for (Class<?> role : roles) {
      registerComponentMock(role);
    }
  }

  public <T> T registerComponentMock(Class<T> role) throws ComponentRepositoryException {
    return registerComponentMock(role, ComponentRole.DEFAULT_HINT);
  }

  public <T> T registerComponentMock(Class<T> role, String hint)
      throws ComponentRepositoryException {
    return registerComponentMock(role, hint, createDefaultMock(role));
  }

  @SuppressWarnings("unchecked")
  public <T> T registerComponentMock(Class<T> role, String hint, T instance)
      throws ComponentRepositoryException {
    DefaultComponentDescriptor<T> descriptor = new DefaultComponentDescriptor<>(role, hint);
    if (instance != null) {
      descriptor.setImplementation((Class<T>) instance.getClass());
    }
    getComponentManager().registerComponent(descriptor, instance);
    return instance;
  }

  public void replayDefault(Object... mocks) {
    getDefaultMocks().stream().forEach(EasyMock::replay);
    EasyMock.replay(mocks);
  }

  public void verifyDefault(Object... mocks) {
    getDefaultMocks().stream().forEach(EasyMock::verify);
    EasyMock.verify(mocks);
  }

  public void resetDefault(Object... mocks) {
    getDefaultMocks().stream().forEach(EasyMock::reset);
    EasyMock.reset(mocks);
  }

}

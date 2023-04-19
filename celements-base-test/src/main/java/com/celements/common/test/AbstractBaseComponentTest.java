package com.celements.common.test;

import static com.google.common.base.Preconditions.*;

import java.util.List;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.xwiki.component.descriptor.ComponentRole;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.manager.ComponentRepositoryException;
import org.xwiki.component.spring.XWikiSpringConfig;

import com.celements.spring.CelSpringConfig;
import com.celements.spring.context.CelSpringContext;
import com.google.common.collect.ImmutableList;

/**
 * Prepares the Spring testing environment.
 */
public abstract class AbstractBaseComponentTest {

  private CelSpringContext context;

  @Before
  public final void setUpSpring() throws Exception {
    checkState(context == null);
    context = new CelSpringContext(getSpringConfigs());
  }

  /**
   * Entry point for adding additional configs like {@link CelSpringConfig}.
   */
  protected List<Class<?>> getSpringConfigs() {
    return ImmutableList.of(XWikiSpringConfig.class, CelSpringConfig.class);
  }

  @After
  public final void tearDownSpring() throws Exception {
    resetDefault(); // let's reset the mocks here to avoid memory leaks into the ClassLoader
    getDefaultMocks().clear();
    getSpringContext().close();
    context = null;
  }

  public CelSpringContext getSpringContext() {
    checkState(context != null);
    return context;
  }

  public ComponentManager getComponentManager() {
    return getSpringContext().getBean(ComponentManager.class);
  }

  public CelDefaultMocks getDefaultMocks() {
    return getSpringContext().getBean(CelDefaultMocks.class);
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
    return registerComponent(role, hint, createDefaultMock(role));
  }

  @SuppressWarnings("unchecked")
  public <T> T registerComponent(Class<T> role, String hint, T instance)
      throws ComponentRepositoryException {
    DefaultComponentDescriptor<T> descriptor = new DefaultComponentDescriptor<>();
    descriptor.setRole(role);
    descriptor.setRoleHint(hint);
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

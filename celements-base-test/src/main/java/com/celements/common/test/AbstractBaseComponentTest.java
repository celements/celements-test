package com.celements.common.test;

import static com.google.common.base.Preconditions.*;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.springframework.context.support.GenericApplicationContext;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.container.ApplicationContext;
import org.xwiki.container.Container;

import com.celements.spring.CelSpringConfig;
import com.celements.spring.XWikiSpringConfig;
import com.celements.spring.context.CelSpringContext;
import com.google.common.collect.ImmutableList;

public abstract class AbstractBaseComponentTest {

  private GenericApplicationContext springCtx;

  @Before
  public void setUp() throws Exception {
    checkState(springCtx == null);
    springCtx = new CelSpringContext(getSpringConfigs());
    springCtx.getBean(Container.class)
        .setApplicationContext(new TestXWikiApplicationContext());
  }

  /**
   * Entry point for adding additional configs like {@link CelSpringConfig}.
   */
  protected List<Class<?>> getSpringConfigs() {
    return ImmutableList.of(XWikiSpringConfig.class, CelSpringConfig.class);
  }

  @After
  public void tearDown() throws Exception {
    getSpringContext().close();
    springCtx = null;
  }

  public GenericApplicationContext getSpringContext() {
    checkState(springCtx != null);
    return springCtx;
  }

  public ComponentManager getComponentManager() {
    return getSpringContext().getBean(ComponentManager.class);
  }

  public static class TestXWikiApplicationContext implements ApplicationContext {

    @Override
    public URL getResource(String resourceName) throws MalformedURLException {
      if (resourceName.contains("xwiki.properties")) {
        return this.getClass().getClassLoader().getResource("xwiki.properties");
      }
      throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getResourceAsStream(String resourceName) {
      throw new UnsupportedOperationException();
    }

    @Override
    public File getTemporaryDirectory() {
      throw new UnsupportedOperationException();
    }
  }

}

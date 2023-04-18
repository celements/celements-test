package com.celements.common.test;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.spring.XWikiSpringConfig;

import com.celements.spring.CelSpringConfig;
import com.celements.spring.context.CelSpringContext;
import com.google.common.collect.ImmutableList;

public abstract class AbstractBaseComponentTest {

  @Before
  public final void setUpSpring() throws Exception {
    CelSpringContext springCtx = new CelSpringContext(getSpringConfigs());
    CelementsSpringTestUtil.setContext(springCtx);
  }

  /**
   * Entry point for adding additional configs like {@link CelSpringConfig}.
   */
  protected List<Class<?>> getSpringConfigs() {
    return ImmutableList.of(XWikiSpringConfig.class, CelSpringConfig.class);
  }

  @After
  public final void tearDownSpring() throws Exception {
    CelementsSpringTestUtil.removeContext()
        .close();
  }

  public CelSpringContext getSpringContext() {
    return CelementsSpringTestUtil.getContext();
  }

  public ComponentManager getComponentManager() {
    return getSpringContext().getBean(ComponentManager.class);
  }

}

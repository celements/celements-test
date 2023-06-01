/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.celements.common.test;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.velocity.VelocityContext;
import org.junit.After;
import org.junit.Before;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.xwiki.component.manager.ComponentRepositoryException;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.container.ApplicationContext;
import org.xwiki.container.Container;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.context.ExecutionContextManager;
import org.xwiki.test.MockConfigurationSource;

import com.celements.servlet.CelSpringWebContext;
import com.google.common.collect.ImmutableList;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.util.XWikiStubContextProvider;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiMessageTool;

/**
 * Extension of {@link AbstractBaseComponentTest} which prepares the Spring and XWiki testing
 * environment.
 */
public abstract class AbstractComponentTest extends AbstractBaseComponentTest {

  public static final String DEFAULT_DB = "xwikidb";
  public static final String DEFAULT_MAIN_WIKI = "xwikiWiki";
  public static final String DEFAULT_LANG = "de";

  @Override
  protected ConfigurableWebApplicationContext createSpringContext() {
    return new CelSpringWebContext();
  }

  @Override
  public ConfigurableWebApplicationContext getSpringContext() {
    return (ConfigurableWebApplicationContext) super.getSpringContext();
  }

  @Override
  protected void beforeSpringContextRefresh() {
    getSpringContext().setServletContext(new MockServletContext());
  }

  @Before
  public final void setUpXWiki() throws Exception {
    Utils.setComponentManager(getComponentManager());
    getBeanFactory().getBean(Container.class)
        .setApplicationContext(new TestXWikiApplicationContext());
    registerComponentMock(XWikiStubContextProvider.class, "default", () -> {
      XWikiContext context = new XWikiContext();
      context.setDatabase(DEFAULT_DB);
      context.setMainXWiki(DEFAULT_MAIN_WIKI);
      context.setLanguage(DEFAULT_LANG);
      return context;
    });
    registerMockConfigSource();
    ExecutionContext execCtx = new ExecutionContext();
    getBeanFactory().getBean(Execution.class).setContext(execCtx);
    getBeanFactory().getBean(ExecutionContextManager.class).initialize(execCtx);
    XWikiContext xwikiContext = getXContext();
    setLocaleAndMsgTool(xwikiContext, execCtx);
    xwikiContext.setWiki(createDefaultMock(XWiki.class));
  }

  protected void registerMockConfigSource() throws ComponentRepositoryException {
    MockConfigurationSource cfgSrc = new MockConfigurationSource();
    for (String hint : getConfigSourceHints()) {
      registerComponentMock(ConfigurationSource.class, hint, cfgSrc);
    }
  }

  protected List<String> getConfigSourceHints() {
    return ImmutableList.of("default", "all", "wiki", "fromwiki", "allproperties", "properties",
        "xwikiproperties", "celementsproperties");
  }

  private XWikiContext setLocaleAndMsgTool(XWikiContext context, ExecutionContext execCtx) {
    Locale locale = new Locale(context.getLanguage());
    ResourceBundle bundle = ResourceBundle.getBundle("ApplicationResources", locale);
    if (bundle == null) {
      bundle = ResourceBundle.getBundle("ApplicationResources");
    }
    XWikiMessageTool msg = new TestMessageTool(bundle, context);
    context.put("msg", msg);
    context.put("locale", locale);
    VelocityContext vcontext = (VelocityContext) execCtx.getProperty("velocityContext");
    if (vcontext != null) {
      vcontext.put("msg", msg);
      vcontext.put("locale", locale);
    }
    return context;
  }

  @After
  public final void tearDownXWiki() throws Exception {
    try {
      getXContext().clear();
      getXContext().setWiki(null);
      getBeanFactory().getBean(Execution.class).removeContext();
    } finally {
      Utils.setComponentManager(null);
    }
  }

  public MockConfigurationSource getConfigurationSource() {
    return getBeanFactory().getBean(MockConfigurationSource.class);
  }

  public XWikiContext getXContext() {
    return (XWikiContext) getBeanFactory().getBean(Execution.class).getContext()
        .getProperty(XWikiContext.EXECUTIONCONTEXT_KEY);
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

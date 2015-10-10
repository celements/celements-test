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

import static org.easymock.EasyMock.*;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.manager.ComponentRepositoryException;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.environment.Environment;
import org.xwiki.environment.internal.ServletEnvironment;
import org.xwiki.test.jmock.AbstractComponentTestCase;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiMessageTool;

/**
 * Extension of {@link org.xwiki.test.AbstractComponentTestCase} that sets up a bridge
 * between the new Execution Context and the old XWikiContext. This allows code that uses
 * XWikiContext to be tested using this Test Case class.
 * 
 * @version: AbstractBridgedComponentTestCase.java fpichler copied from
 *           AbstractBridgedComponentTestCase.java
 */
public abstract class AbstractBridgedComponentTestCase extends AbstractComponentTestCase {

  private static Logger LOGGER = LoggerFactory.getLogger(
      AbstractBridgedComponentTestCase.class);

  private XWikiContext context;
  private XWiki wikiMock;
  private ServletContext servletContextMock;
  private Set<Object> defaultMocks = new HashSet<>();
  // private DocumentAccessBridge mockDocumentAccessBridge;
  // private DocumentNameSerializer mockDocumentNameSerializer;
  // private DocumentNameFactory mockDocumentNameFactory;

  public XWiki getWikiMock() {
    return wikiMock;
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();

    // Statically store the component manager in {@link Utils} to be able to access it
    // without the context.
    Utils.setComponentManager(getComponentManager());

    this.context = (XWikiContext) getExecutionContext().getProperty("xwikicontext");
    if (this.context == null) {
      this.context = new XWikiContext();

      this.context.setDatabase("xwikidb");
      this.context.setMainXWiki("xwikiWiki");
      wikiMock = createMockAndAddToDefault(XWiki.class);
      context.setWiki(wikiMock);
      getExecutionContext().setProperty("xwikicontext", this.context);

      // initialize the Component Manager so that components can be looked up
      getContext().put(ComponentManager.class.getName(), getComponentManager());
    }
    servletContextMock = createMockAndAddToDefault(ServletContext.class);
    ServletEnvironment environment = (ServletEnvironment) getComponentManager(
      ).getInstance(Environment.class);
    environment.setServletContext(servletContextMock);
    expect(servletContextMock.getResourceAsStream(eq(
      "/WEB-INF/cache/infinispan/config.xml"))).andReturn(null).anyTimes();
    expect(servletContextMock.getAttribute(eq("javax.servlet.context.tempdir"))
      ).andReturn(new File(System.getProperty("java.io.tmpdir"))).anyTimes();
  }

  private ExecutionContext getExecutionContext() throws Exception {
    return ((Execution)getComponentManager().getInstance(Execution.class)).getContext();
  }

  @After
  public void tearDown() throws Exception {
    defaultMocks.clear();
    Utils.setComponentManager(null);
    super.tearDown();
  }

  @SuppressWarnings("unchecked")
  public XWikiContext getContext() {
    if (this.context.getLanguage() == null) {
      this.context.setLanguage("de");
    }
    if (this.context.get("msg") == null) {
      Locale locale = new Locale(this.context.getLanguage());
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
    }
    return this.context;
  }

  public TestMessageTool getMessageToolStub() {
    XWikiMessageTool msgTool = getContext().getMessageTool();
    if (msgTool instanceof TestMessageTool) {
      return (TestMessageTool) msgTool;
    }
    return null;
  }

  public class TestMessageTool extends XWikiMessageTool {

    private Map<String, String> injectedMessages = new HashMap<>();

    public TestMessageTool(ResourceBundle bundle, XWikiContext context) {
      super(bundle, context);
    }

    public void injectMessage(String key, String value) {
      injectedMessages.put(key, value);
    }

    public void injectMessage(String key, List<?> params, String value) {
      injectedMessages.put(key + StringUtils.join(params, ","), value);
    }

    @Override
    public String get(String key) {
      if (injectedMessages.containsKey(key)) {
        return injectedMessages.get(key);
      } else {
        LOGGER.error("TestMessageTool missing the key '" + key + "'.");
        return super.get(key);
      }
    }

    @Override
    public String get(String key, List<?> params) {
      String paramsStr = StringUtils.join(params, ",");
      if (injectedMessages.containsKey(key + paramsStr)) {
        return injectedMessages.get(key + paramsStr);
      } else {
        LOGGER.error("TestMessageTool missing the key '" + key + "' for params '"
            + paramsStr + "'.");
        return super.get(key, params);
      }
    }

  }

  public <T> T registerComponentMock(Class<T> role) throws ComponentRepositoryException {
    return registerComponentMock(role, "default");
  }

  public <T> T registerComponentMock(Class<T> role, String hint
      ) throws ComponentRepositoryException {
    DefaultComponentDescriptor<T> descriptor = new DefaultComponentDescriptor<T>();
    descriptor.setRole(role);
    descriptor.setRoleHint(hint);
    T componentMock = createMock(role);
    Utils.getComponentManager().registerComponent(descriptor, componentMock);
    defaultMocks.add(componentMock);
    return componentMock;
  }

  public <T> T createMockAndAddToDefault(final Class<T> toMock) {
    T newMock = createMock(toMock);
    defaultMocks.add(newMock);
    return newMock;
  }

  protected void replayDefault(Object... mocks) {
    replay(defaultMocks.toArray());
    replay(mocks);
  }

  protected void verifyDefault(Object... mocks) {
    verify(defaultMocks.toArray());
    verify(mocks);
  }

}

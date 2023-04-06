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

import static com.celements.common.test.CelementsTestUtils.*;

import org.junit.After;
import org.junit.Before;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.context.ExecutionContextManager;
import org.xwiki.test.MockConfigurationSource;

import com.xpn.xwiki.web.Utils;

/**
 * Extension of {@link AbstractBaseComponentTest} which prepares the {@link ExecutionContext} and
 * {@link MockConfigurationSource} and can be used together with {@link CelementsTestUtils}
 */
public abstract class AbstractComponentTest extends AbstractBaseComponentTest {

  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();
    ExecutionContext execCtx = new ExecutionContext();
    springCtx.getBean(Execution.class).setContext(execCtx);
    springCtx.getBean(ExecutionContextManager.class).initialize(execCtx);
    Utils.setComponentManager(getComponentManager());
    getWikiMock();
  }

  @Override
  protected void registerComponents() throws Exception {
    MockConfigurationSource cfgSrc = new MockConfigurationSource();
    registerComponentMock(ConfigurationSource.class, "default", cfgSrc);
    registerComponentMock(ConfigurationSource.class, "all", getConfigurationSource());
    registerComponentMock(ConfigurationSource.class, "wiki", getConfigurationSource());
    registerComponentMock(ConfigurationSource.class, "fromwiki", getConfigurationSource());
    registerComponentMock(ConfigurationSource.class, "allproperties", getConfigurationSource());
    registerComponentMock(ConfigurationSource.class, "xwikiproperties", cfgSrc);
    registerComponentMock(ConfigurationSource.class, "celementsproperties", cfgSrc);
    for (HintedComponent hc : this.getClass().getAnnotation(ComponentList.class).value()) {
      registerComponentMock(hc.clazz(), hc.hint());
    }
  }

  @Override
  @After
  public void tearDown() throws Exception {
    springCtx.getBean(Execution.class).removeContext();
    Utils.setComponentManager(null);
    getDefaultMocks().clear();
    super.tearDown();
  }

  public MockConfigurationSource getConfigurationSource() {
    return springCtx.getBean(MockConfigurationSource.class);
  }

}

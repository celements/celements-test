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

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.manager.ComponentRepositoryException;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.context.ExecutionContextManager;
import org.xwiki.test.MockConfigurationSource;

import com.google.common.collect.ImmutableList;
import com.xpn.xwiki.web.Utils;

/**
 * Extension of {@link AbstractBaseComponentTest} which prepares the {@link ExecutionContext} and
 * {@link MockConfigurationSource} and can be used together with {@link CelementsTestUtils}
 */
public abstract class AbstractComponentTest extends AbstractBaseComponentTest {

  @Before
  @Override
  public final void setUp() throws Exception {
    super.setUp();
    Utils.setComponentManager(getComponentManager());
    registerMockConfigSource();
    ExecutionContext execCtx = new ExecutionContext();
    getSpringContext().getBean(Execution.class).setContext(execCtx);
    getSpringContext().getBean(ExecutionContextManager.class).initialize(execCtx);
    CelementsTestUtils.getWikiMock();
  }

  protected void registerMockConfigSource() throws ComponentRepositoryException {
    MockConfigurationSource cfgSrc = new MockConfigurationSource();
    DefaultComponentDescriptor<ConfigurationSource> descriptor = new DefaultComponentDescriptor<>();
    descriptor.setRole(ConfigurationSource.class);
    descriptor.setImplementation(MockConfigurationSource.class);
    for (String hint : getConfigSourceHints()) {
      descriptor.setRoleHint(hint);
      getComponentManager().registerComponent(descriptor, cfgSrc);
    }
  }

  protected List<String> getConfigSourceHints() {
    return ImmutableList.of("default", "all", "wiki", "fromwiki", "allproperties",
        "xwikiproperties", "celementsproperties");
  }

  @After
  @Override
  public final void tearDown() throws Exception {
    try {
      // let's reset the mocks here to avoid memory leaks into the ClassLoader
      CelementsTestUtils.resetDefault();
      CelementsTestUtils.getDefaultMocks().clear();
      CelementsTestUtils.getContext().clear();
      CelementsTestUtils.getContext().setWiki(null);
      getSpringContext().getBean(Execution.class).removeContext();
    } finally {
      Utils.setComponentManager(null);
      super.tearDown();
    }
  }

  public MockConfigurationSource getConfigurationSource() {
    return getSpringContext().getBean(MockConfigurationSource.class);
  }

}

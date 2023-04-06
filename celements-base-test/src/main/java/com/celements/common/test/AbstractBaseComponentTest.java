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

import org.junit.After;
import org.junit.Before;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.container.ApplicationContext;
import org.xwiki.container.Container;

import com.celements.spring.CelSpringConfig;
import com.celements.spring.context.CelSpringContext;
import com.google.common.collect.ImmutableList;

public abstract class AbstractBaseComponentTest {

  protected CelSpringContext springCtx;

  @Before
  public void setUp() throws Exception {
    this.springCtx = new CelSpringContext(getAdditionalSpringConfigs());
    registerComponents();
    springCtx.getBean(Container.class)
        .setApplicationContext(new TestXWikiApplicationContext());
  }

  /**
   * Entry point for adding additional configs like {@link CelSpringConfig}.
   */
  protected List<Class<?>> getAdditionalSpringConfigs() {
    return ImmutableList.of();
  }

  /**
   * Register custom/mock components
   */
  protected void registerComponents() throws Exception {}

  @After
  public void tearDown() throws Exception {
    this.springCtx = null;
  }

  public ComponentManager getComponentManager() {
    return springCtx.getBean(ComponentManager.class);
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

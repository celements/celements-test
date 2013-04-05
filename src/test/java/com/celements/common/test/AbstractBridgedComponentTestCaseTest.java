package com.celements.common.test;
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
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.cache.CacheManager;
import org.xwiki.cache.config.CacheConfiguration;
import org.xwiki.cache.eviction.LRUEvictionConfiguration;
import org.xwiki.environment.Environment;
import org.xwiki.model.reference.EntityReferenceSerializer;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.render.XWikiRenderingEngine;
import com.xpn.xwiki.web.Utils;

public class AbstractBridgedComponentTestCaseTest
    extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private XWiki xwiki;

  @Before
  public void setUp_TestAbstractBridgedComponentTestCase() throws Exception {
    context = getContext();
    xwiki = getWikiMock();
  }

  @Test
  public void testDefaultMocks() {
    assertNotNull(context);
    assertNotNull(xwiki);
    assertSame(context.getWiki(), xwiki);
  }

  @Test
  public void testOnceLoadComponentManager() throws Exception {
    replayDefault();
    assertNotNull(getComponentManager());
    assertNotNull(Utils.getComponent(EntityReferenceSerializer.TYPE_STRING));
    verifyDefault();
  }

  @Test
  public void testServletEnvironment_setup() throws Exception {
    replayDefault();
    Environment environment = getComponentManager().getInstance(Environment.class);
    assertNotNull(environment);
    assertNotNull(environment.getTemporaryDirectory());
    verifyDefault();
  }

  @Test
  public void testServletEnvironment_get_infinispan_cache_config() throws Exception {
    replayDefault();
    Environment environment = getComponentManager().getInstance(Environment.class);
    assertNotNull(environment);
    assertNull(environment.getResourceAsStream("/WEB-INF/cache/infinispan/config.xml"));
    verifyDefault();
  }

  @Test
  public void testInitCache() throws Exception {
    CacheConfiguration configuration = new CacheConfiguration();
    configuration.setConfigurationId("xwiki.renderingcache");
    LRUEvictionConfiguration lru = new LRUEvictionConfiguration();
    lru.setMaxEntries(100);
    configuration.put(LRUEvictionConfiguration.CONFIGURATIONID, lru);
    replayDefault();
    assertNotNull(Utils.getComponent(CacheManager.class).createNewCache(configuration));
    verifyDefault();
  }

  @Test
  public void test() {
    XWikiRenderingEngine renderingEngine = createMockAndAddToDefault(
        XWikiRenderingEngine.class);
    expect(xwiki.getRenderingEngine()).andReturn(renderingEngine).anyTimes();
    expect(renderingEngine.interpretText(eq("link"), same(context.getDoc()),
        same(context))).andReturn("rendered link");
    replayDefault();
    assertNotNull(xwiki.getRenderingEngine());
    assertEquals("renderingEngine schould get set to replay by replayDefault because it"
        + " is created with createMockAndAddToDefault.", "rendered link",
        xwiki.getRenderingEngine().interpretText("link",
        context.getDoc(), context));
    verifyDefault();
  }

}

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
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.environment.Environment;
import org.xwiki.model.reference.EntityReferenceSerializer;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.Utils;

public class TestAbstractBridgedComponentTestCase
    extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private XWiki xwiki;

  @Before
  public void setUp_TestAbstractBridgedComponentTestCase() throws Exception {
    context = getContext();
    xwiki = getWikiMock();
  }

  @Test
  public void testOnceLoadComponentManager() throws Exception {
    replayAll();
    assertNotNull(getComponentManager());
    assertNotNull(Utils.getComponent(EntityReferenceSerializer.TYPE_STRING));
    verifyAll();
  }

  @Test
  public void testServletEnvironment_setup() throws Exception {
    replayAll();
    Environment environment = getComponentManager().getInstance(Environment.class);
    assertNotNull(environment);
    assertNotNull(environment.getTemporaryDirectory());
    verifyAll();
  }

  @Test
  public void testServletEnvironment_get_infinispan_cache_config() throws Exception {
    replayAll();
    Environment environment = getComponentManager().getInstance(Environment.class);
    assertNotNull(environment);
    assertNull(environment.getResourceAsStream("/WEB-INF/cache/infinispan/config.xml"));
    verifyAll();
  }


  private void replayAll(Object ... mocks) {
    replayDefault(mocks);
  }

  private void verifyAll(Object ... mocks) {
    verifyDefault(mocks);
  }
}

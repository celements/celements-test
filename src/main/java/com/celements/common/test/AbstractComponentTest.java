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
import org.xwiki.test.AbstractComponentTestCase;

import com.xpn.xwiki.web.Utils;

/**
 * Extension of {@link org.xwiki.test.AbstractComponentTestCase} which can be used
 * together with {@link CelementsTestUtils}
 */
public abstract class AbstractComponentTest extends AbstractComponentTestCase {

  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();

    // Statically store the component manager in {@link Utils} to be able to access it
    // without the context.
    Utils.setComponentManager(getComponentManager());

    // initialize celements configuration source mock
    activateCelConfigSource(getCelConfigSourceMock());

    // initialize context and wiki mock
    getWikiMock();
  }

  @After
  @Override
  public void tearDown() throws Exception {
    getDefaultMocks().clear();
    Utils.setComponentManager(null);
    super.tearDown();
  }

}

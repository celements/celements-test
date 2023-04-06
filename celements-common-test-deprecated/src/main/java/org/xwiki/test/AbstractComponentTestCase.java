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
package org.xwiki.test;

import static com.celements.common.test.CelementsTestUtils.*;

import org.xwiki.component.manager.ComponentRepositoryException;

import com.celements.common.test.AbstractComponentTest;
import com.celements.common.test.CelementsTestUtils;

/**
 * Tests which needs to have XWiki Components set up should extend this class which makes the
 * Component Manager
 * available. Use this class for JUnit 4.x tests. For JUnit 3.x tests use
 * {@link AbstractXWikiComponentTestCase}
 * instead.
 *
 * Since XWiki 2.2M1 you should prefer using {@link org.xwiki.test.AbstractMockingComponentTestCase}
 * instead.
 *
 * @deprecated since 6.0
 */
@Deprecated
public class AbstractComponentTestCase extends AbstractComponentTest {

  /**
   * @deprecated instead use {@link CelementsTestUtils#registerComponentMock(Class, String)}
   */
  @Deprecated
  public <T> T registerMockComponent(Class<T> role, String hint)
      throws ComponentRepositoryException {
    return registerComponentMock(role, hint);
  }

  /**
   * @deprecated instead use {@link CelementsTestUtils#registerComponentMock(Class)}
   */
  @Deprecated
  public <T> T registerMockComponent(Class<T> role) throws ComponentRepositoryException {
    return registerComponentMock(role);
  }

}

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

import org.xwiki.component.manager.ComponentRepositoryException;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;

/**
 * Extension of {@link org.xwiki.test.AbstractComponentTestCase} that sets up a bridge
 * between the new Execution Context and the old XWikiContext. This allows code that uses
 * XWikiContext to be tested using this Test Case class.
 *
 * @version: AbstractBridgedComponentTestCase.java fpichler copied from
 *           AbstractBridgedComponentTestCase.java
 * @deprecated instead use {@link AbstractComponentTest} together with static import
 *             {@link CelementsTestUtils}
 */
@Deprecated
public abstract class AbstractBridgedComponentTestCase extends AbstractComponentTest {

  public XWiki getWikiMock() {
    return CelementsTestUtils.getWikiMock();
  }

  public XWikiContext getContext() {
    return CelementsTestUtils.getContext();
  }

  public TestMessageTool getMessageToolStub() {
    return CelementsTestUtils.getMessageToolStub();
  }

  public <T> T registerComponentMock(Class<T> role) throws ComponentRepositoryException {
    return CelementsTestUtils.registerComponentMock(role);
  }

  public <T> T registerComponentMock(Class<T> role, String hint)
      throws ComponentRepositoryException {
    return CelementsTestUtils.registerComponentMock(role, hint);
  }

  public <T> T createMockAndAddToDefault(final Class<T> toMock) {
    return CelementsTestUtils.createMockAndAddToDefault(toMock);
  }

  protected void replayDefault(Object... mocks) {
    replay(CelementsTestUtils.getDefaultMocks().toArray());
    replay(mocks);
  }

  protected void verifyDefault(Object... mocks) {
    verify(CelementsTestUtils.getDefaultMocks().toArray());
    verify(mocks);
  }

}

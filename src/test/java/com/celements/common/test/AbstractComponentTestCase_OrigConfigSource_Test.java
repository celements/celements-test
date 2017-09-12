package com.celements.common.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.configuration.ConfigurationSource;

import com.xpn.xwiki.web.Utils;

@ComponentList({ @HintedComponent(clazz = ConfigurationSource.class,
    hint = "celementsproperties") })
public class AbstractComponentTestCase_OrigConfigSource_Test extends AbstractComponentTest {

  @Before
  public void prepare() throws Exception {
  }

  @Test
  public void test_getCelConfigSource_mock() {
    try {
      Utils.getComponentManager().lookup(ConfigurationSource.class, "celementsproperties");
      fail("expecting no celements config source component registered");
    } catch (ComponentLookupException cLExp) {
      // expected
    }
  }

}

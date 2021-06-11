package com.celements.common.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.configuration.ConfigurationSource;

import com.xpn.xwiki.web.Utils;

public class AbstractComponentTestCase_MockConfigSource_Test extends AbstractComponentTest {

  @Before
  public void prepare() throws Exception {
  }

  @Test
  public void test_getCelConfigSource_mock() {
    ConfigurationSource testSource = Utils.getComponent(ConfigurationSource.class,
        "celementsproperties");
    assertNotNull(testSource.getClass());
    assertNotSame(ConfigurationSource.class, testSource.getClass());
  }

}

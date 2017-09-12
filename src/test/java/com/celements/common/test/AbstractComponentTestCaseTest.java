package com.celements.common.test;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.configuration.ConfigurationSource;

import com.xpn.xwiki.web.Utils;

public class AbstractComponentTestCaseTest extends AbstractComponentTest {

  @Before
  public void prepare() throws Exception {
  }

  @Test
  public void test_getCelConfigSource_mock() {
    ConfigurationSource testSource = Utils.getComponent(ConfigurationSource.class,
        "celementsproperties");
    assertSame(getCelConfigSourceMock(), testSource);
  }

  @Test
  public void test_getCelConfigSource_origSource() {
    ConfigurationSource testSourceMock = Utils.getComponent(ConfigurationSource.class,
        "celementsproperties");
    activateCelConfigSource(getCelConfigSourceOriginal());
    ConfigurationSource testSourceOrig = Utils.getComponent(ConfigurationSource.class,
        "celementsproperties");
    assertNotSame(testSourceMock, testSourceOrig);
  }

}

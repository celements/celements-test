package com.celements.common.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.XWikiMessageTool;

public class TestMessageTool extends XWikiMessageTool {

  private final Logger LOGGER = LoggerFactory.getLogger(TestMessageTool.class);

  private Map<String, String> injectedMessages = new HashMap<>();

  public TestMessageTool(ResourceBundle bundle, XWikiContext context) {
    super(bundle, context);
  }

  public void injectMessage(String key, String value) {
    injectedMessages.put(key, value);
  }

  public void injectMessage(String key, List<?> params, String value) {
    injectedMessages.put(key + StringUtils.join(params, ","), value);
  }

  @Override
  public String get(String key) {
    if (injectedMessages.containsKey(key)) {
      return injectedMessages.get(key);
    } else {
      LOGGER.error("TestMessageTool missing the key '" + key + "'.");
      return super.get(key);
    }
  }

  @Override
  public String get(String key, List<?> params) {
    String paramsStr = StringUtils.join(params, ",");
    if (injectedMessages.containsKey(key + paramsStr)) {
      return injectedMessages.get(key + paramsStr);
    } else {
      LOGGER.error("TestMessageTool missing the key '" + key + "' for params '"
          + paramsStr + "'.");
      return super.get(key, params);
    }
  }

}
package com.celements.common.test.service;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

@Component
public class TestService implements ITestServiceRole {

  public static final Object OBJ = new Object();

  @Requirement("injected")
  private ITestServiceRole injectedTestService;

  @Override
  public ITestServiceRole getInjectedComponent() {
    return injectedTestService;
  }

  @Override
  public Object getObj() {
    return OBJ;
  }

}

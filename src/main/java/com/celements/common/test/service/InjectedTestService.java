package com.celements.common.test.service;

import org.xwiki.component.annotation.Component;

@Component("injected")
public class InjectedTestService implements ITestServiceRole {

  public static final Object OBJ = new Object();

  @Override
  public ITestServiceRole getInjectedComponent() {
    return null;
  }

  @Override
  public Object getObj() {
    return OBJ;
  }

}

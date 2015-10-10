package com.celements.common.test.service;

import javax.inject.Inject;
import javax.inject.Named;

import org.xwiki.component.annotation.Component;

@Component
public class TestService implements ITestServiceRole {

  public static final Object OBJ = new Object();

  @Inject
  @Named("injected")
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

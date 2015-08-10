package com.celements.common.test.service;

import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface ITestServiceRole {

  public ITestServiceRole getInjectedComponent();

  public Object getObj();

}

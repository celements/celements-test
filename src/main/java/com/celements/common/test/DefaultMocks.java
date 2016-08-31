package com.celements.common.test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.xwiki.component.descriptor.ComponentDescriptor;

public class DefaultMocks {

  private Map<ComponentDescriptor<?>, Object> mocks = new HashMap<>();

  public <T> void put(ComponentDescriptor<T> descr, T mock) {
    mocks.put(descr, mock);
  }

  public <T> T get(ComponentDescriptor<T> descr) {
    return descr.getRole().cast(mocks.get(descr));
  }

  public Collection<Object> getAll() {
    return mocks.values();
  }

  public void clear() {
    mocks.clear();
  }

}

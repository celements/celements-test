package com.celements.common.test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.annotation.concurrent.NotThreadSafe;

import org.springframework.stereotype.Component;

@NotThreadSafe
@Component
public class CelDefaultMocks {

  private final List<Object> mocks = new ArrayList<>();

  void add(Object mock) {
    mocks.add(mock);
  }

  void clear() {
    mocks.clear();
  }

  public Stream<Object> stream() {
    return mocks.stream();
  }

  public <T> T get(final Class<T> mockClass) {
    return stream()
        .filter(mockClass::isInstance)
        .map(mockClass::cast)
        .findFirst()
        .orElse(null);
  }
}

package com.celements.common.test.generation;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import org.springframework.beans.factory.config.BeanDefinition;

final class BeanGenerationState {

  private final Set<String> singletons = new LinkedHashSet<>();
  private final Map<String, Optional<BeanDefinition>> originalBeanDefinitions = new LinkedHashMap<>();
  private final Map<String, Optional<String>> originalAliases = new LinkedHashMap<>();

  private boolean reusable = true;

  Set<String> singletons() {
    return singletons;
  }

  Map<String, Optional<BeanDefinition>> originalBeanDefinitions() {
    return originalBeanDefinitions;
  }

  Map<String, Optional<String>> originalAliases() {
    return originalAliases;
  }

  boolean reusable() {
    return reusable;
  }

  void invalidate() {
    reusable = false;
  }

  void recordSingleton(String beanName) {
    singletons.add(beanName);
  }

  void recordBeanDefinition(String beanName, Supplier<Optional<BeanDefinition>> beanDefinition) {
    originalBeanDefinitions.computeIfAbsent(beanName, ignored -> beanDefinition.get());
  }

  void recordAlias(String alias, Supplier<Optional<String>> beanName) {
    originalAliases.computeIfAbsent(alias, ignored -> beanName.get());
  }
}

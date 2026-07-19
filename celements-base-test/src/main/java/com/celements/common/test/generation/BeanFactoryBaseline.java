package com.celements.common.test.generation;

import java.util.List;
import java.util.Set;

record BeanFactoryBaseline(Set<String> singletons, List<String> eagerGenerationBeans) {

  BeanFactoryBaseline(Set<String> singletons, List<String> eagerGenerationBeans) {
    this.singletons = Set.copyOf(singletons);
    this.eagerGenerationBeans = List.copyOf(eagerGenerationBeans);
  }

  boolean containsSingleton(String beanName) {
    return singletons.contains(beanName);
  }
}

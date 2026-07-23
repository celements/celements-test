package com.celements.common.test.generation;

import static java.util.stream.Collectors.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.AbstractApplicationContext;

final class EagerGenerationBeanCollector {

  private final ConfigurableListableBeanFactory beanFactory;

  EagerGenerationBeanCollector(ConfigurableListableBeanFactory beanFactory) {
    this.beanFactory = beanFactory;
  }

  List<String> collect() {
    Set<String> kernelBeans = collectKernelBeans();
    return Stream.of(beanFactory.getSingletonNames())
        .filter(beanName -> !kernelBeans.contains(beanName))
        .toList();
  }

  private Set<String> collectKernelBeans() {
    Set<String> kernelBeans = Stream.of(beanFactory.getSingletonNames())
        .filter(this::isKernelSingleton)
        .collect(toCollection(LinkedHashSet::new));
    boolean changed;
    do {
      changed = kernelBeans.addAll(kernelBeans.stream()
          .flatMap(name -> Stream.of(beanFactory.getDependenciesForBean(name)))
          .filter(beanFactory::containsSingleton)
          .toList());
    } while (changed);
    return kernelBeans;
  }

  private boolean isKernelSingleton(String beanName) {
    if (!beanFactory.containsBeanDefinition(beanName)) {
      return true;
    }
    BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
    Object singleton = beanFactory.getSingleton(beanName);
    return isInfrastructureBean(beanDefinition, singleton)
        || isContextInfrastructure(beanName);
  }

  private boolean isInfrastructureBean(BeanDefinition beanDefinition, Object singleton) {
    return (beanDefinition.getRole() == BeanDefinition.ROLE_INFRASTRUCTURE)
        || (singleton instanceof BeanFactoryPostProcessor)
        || (singleton instanceof BeanPostProcessor);
  }

  private boolean isContextInfrastructure(String beanName) {
    return AbstractApplicationContext.MESSAGE_SOURCE_BEAN_NAME.equals(beanName)
        || AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME.equals(beanName)
        || AbstractApplicationContext.LIFECYCLE_PROCESSOR_BEAN_NAME.equals(beanName);
  }
}

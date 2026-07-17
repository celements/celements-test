package com.celements.common.test;

import static java.util.stream.Collectors.*;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.Scope;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.convert.ConversionService;

import com.celements.spring.context.XWikiShimBeanFactory;

/**
 * A test-only bean factory which can discard all beans and component registrations made during a
 * test while retaining the refreshed Spring container.
 */
public class GenerationAwareBeanFactory extends XWikiShimBeanFactory {

  private final LinkedHashSet<String> generationSingletons = new LinkedHashSet<>();
  private final Map<String, Optional<BeanDefinition>> originalBeanDefs = new LinkedHashMap<>();
  private final Map<String, Optional<String>> originalAliases = new LinkedHashMap<>();

  private Set<String> baselineSingletons = Set.of();
  private List<String> eagerGenerationBeans = List.of();
  private boolean baselineSealed;
  private boolean generationActive;
  private boolean restoring;
  private boolean reusable = true;

  /**
   * Classifies the singletons created by the context refresh and seals the container baseline.
   */
  public synchronized void sealBaseline() {
    requireState(!baselineSealed, "bean factory baseline is already sealed");
    requireState(!generationActive, "cannot seal the baseline during a generation");
    eagerGenerationBeans = findEagerGenerationBeans();
    destroySingletons(eagerGenerationBeans);
    baselineSingletons = Set.of(getSingletonNames());
    baselineSealed = true;
  }

  /**
   * Starts tracking singleton creation and bean registry changes for one test.
   */
  public synchronized void beginGeneration() {
    requireState(baselineSealed, "bean factory baseline has not been sealed");
    requireState(!generationActive, "a bean generation is already active");
    requireState(reusable, "bean factory is no longer reusable");
    generationSingletons.clear();
    originalBeanDefs.clear();
    originalAliases.clear();
    generationActive = true;
    eagerGenerationBeans.forEach(this::getBean);
  }

  /**
   * Destroys the current generation and restores the bean registry to its refreshed baseline.
   *
   * @return whether the containing application context is safe to cache for another test
   */
  public synchronized boolean endGeneration() {
    requireState(generationActive, "no bean generation is active");
    restoring = true;
    try {
      destroySingletons(List.copyOf(generationSingletons));
      generationSingletons.forEach(this::clearMergedBeanDefinition);
      originalBeanDefs.forEach(this::restoreBeanDefinition);
      originalAliases.forEach(this::restoreAlias);
      clearMetadataCache();
      validateBaselineSingletons();
      return reusable;
    } finally {
      resetGenerationTracking();
    }
  }

  @Override
  protected synchronized void addSingleton(String beanName, Object singletonObject) {
    super.addSingleton(beanName, singletonObject);
    if (isTracking()) {
      trackSingleton(beanName);
    }
  }

  @Override
  public synchronized void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
    if (isTracking()) {
      rememberBeanDefinition(beanName);
      protectBaselineSingleton(beanName);
    }
    super.registerBeanDefinition(beanName, beanDefinition);
  }

  @Override
  public synchronized void removeBeanDefinition(String beanName) {
    if (isTracking()) {
      rememberBeanDefinition(beanName);
      protectBaselineSingleton(beanName);
    }
    super.removeBeanDefinition(beanName);
  }

  @Override
  public synchronized void registerAlias(String name, String alias) {
    if (isTracking()) {
      rememberAlias(alias);
    }
    super.registerAlias(name, alias);
  }

  @Override
  public synchronized void removeAlias(String alias) {
    if (isTracking()) {
      rememberAlias(alias);
    }
    super.removeAlias(alias);
  }

  @Override
  public synchronized void destroySingleton(String beanName) {
    if (isTracking()) {
      protectBaselineSingleton(beanName);
    }
    super.destroySingleton(beanName);
  }

  @Override
  public synchronized void destroySingletons() {
    if (isTracking()) {
      reusable = false;
    }
    super.destroySingletons();
  }

  @Override
  public synchronized void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
    invalidateForInfrastructureMutation();
    super.addBeanPostProcessor(beanPostProcessor);
  }

  @Override
  public synchronized void registerResolvableDependency(
      Class<?> dependencyType,
      Object autowiredValue) {
    invalidateForInfrastructureMutation();
    super.registerResolvableDependency(dependencyType, autowiredValue);
  }

  @Override
  public synchronized void registerScope(String scopeName, Scope scope) {
    invalidateForInfrastructureMutation();
    super.registerScope(scopeName, scope);
  }

  @Override
  public synchronized void setConversionService(ConversionService conversionService) {
    invalidateForInfrastructureMutation();
    super.setConversionService(conversionService);
  }

  private boolean isTracking() {
    return generationActive && !restoring;
  }

  private void trackSingleton(String beanName) {
    if (baselineSingletons.contains(beanName)) {
      reusable = false;
    } else {
      generationSingletons.add(beanName);
    }
  }

  private void rememberBeanDefinition(String beanName) {
    originalBeanDefs.computeIfAbsent(beanName, this::getOriginalBeanDefinition);
  }

  private Optional<BeanDefinition> getOriginalBeanDefinition(String beanName) {
    return Optional.of(beanName)
        .filter(this::containsBeanDefinition)
        .map(this::getBeanDefinition)
        .map(this::copyBeanDefinition);
  }

  private BeanDefinition copyBeanDefinition(BeanDefinition beanDefinition) {
    return switch (beanDefinition) {
      case AbstractBeanDefinition beanDef -> beanDef.cloneBeanDefinition();
      default -> new GenericBeanDefinition(beanDefinition);
    };
  }

  private void rememberAlias(String alias) {
    originalAliases.computeIfAbsent(alias, this::getOriginalAlias);
  }

  private Optional<String> getOriginalAlias(String alias) {
    return Optional.of(alias)
        .filter(this::isAlias)
        .map(this::canonicalName);
  }

  private void protectBaselineSingleton(String beanName) {
    if (baselineSingletons.contains(beanName)) {
      reusable = false;
    }
  }

  private void invalidateForInfrastructureMutation() {
    if (isTracking()) {
      reusable = false;
    }
  }

  private List<String> findEagerGenerationBeans() {
    Set<String> kernelBeans = findKernelSingletons();
    return Stream.of(getSingletonNames())
        .filter(beanName -> !kernelBeans.contains(beanName))
        .toList();
  }

  private Set<String> findKernelSingletons() {
    Set<String> kernelBeans = Stream.of(getSingletonNames())
        .filter(this::isKernelSingleton)
        .collect(toCollection(LinkedHashSet::new));
    boolean kernelExpanded;
    do {
      kernelExpanded = addKernelDependencies(kernelBeans);
    } while (kernelExpanded);
    return kernelBeans;
  }

  private boolean addKernelDependencies(Set<String> kernelBeans) {
    return kernelBeans.addAll(kernelBeans.stream()
        .flatMap(beanName -> Stream.of(getDependenciesForBean(beanName)))
        .filter(this::containsSingleton)
        .toList());
  }

  private void destroySingletons(List<String> beanNames) {
    beanNames.reversed().stream()
        .filter(this::containsSingleton)
        .forEach(super::destroySingleton);
  }

  private void restoreBeanDefinition(String beanName, Optional<BeanDefinition> origBeanDef) {
    if (containsBeanDefinition(beanName)) {
      super.removeBeanDefinition(beanName);
    }
    origBeanDef.ifPresent(def -> super.registerBeanDefinition(beanName, def));
  }

  private void restoreAlias(String alias, Optional<String> originalName) {
    if (isAlias(alias)) {
      super.removeAlias(alias);
    }
    originalName.ifPresent(name -> super.registerAlias(name, alias));
  }

  private void validateBaselineSingletons() {
    if (!baselineSingletons.stream().allMatch(this::containsSingleton)) {
      reusable = false;
    }
  }

  private void resetGenerationTracking() {
    generationActive = false;
    restoring = false;
    generationSingletons.clear();
    originalBeanDefs.clear();
    originalAliases.clear();
  }

  private boolean isKernelSingleton(String beanName) {
    if (!containsBeanDefinition(beanName)) {
      return true;
    }
    BeanDefinition beanDefinition = getBeanDefinition(beanName);
    Object singleton = getSingleton(beanName);
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

  private void requireState(boolean expression, String message) {
    if (!expression) {
      throw new IllegalStateException(message);
    }
  }
}

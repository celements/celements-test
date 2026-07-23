package com.celements.common.test.generation;

import static com.google.common.base.Preconditions.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.Scope;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.core.convert.ConversionService;

import com.celements.spring.context.XWikiShimBeanFactory;

/**
 * A test-only bean factory which can discard all beans and component registrations made during a
 * test while retaining the refreshed Spring container.
 */
public class GenerationAwareBeanFactory extends XWikiShimBeanFactory {

  private BeanFactoryBaseline baseline;
  private BeanGenerationState state;

  /**
   * Classifies the singletons created by the context refresh and seals the container baseline.
   */
  public synchronized void sealBaseline() {
    checkState(baseline == null, "bean factory baseline is already sealed");
    checkState(!hasGeneration(), "cannot seal the baseline during a generation");
    List<String> eagerGenerationBeans = new EagerGenerationBeanCollector(this).collect();
    destroySingletons(eagerGenerationBeans);
    baseline = new BeanFactoryBaseline(Set.of(getSingletonNames()), eagerGenerationBeans);
  }

  /**
   * Starts recording singleton creation and bean registry changes for one test.
   */
  public synchronized void beginGeneration() {
    checkState(baseline != null, "bean factory baseline has not been sealed");
    checkState(!hasGeneration(), "a bean generation is already active");
    state = new BeanGenerationState();
    baseline.eagerGenerationBeans().forEach(this::getBean);
  }

  /**
   * Destroys the current generation and restores the bean registry to its refreshed baseline.
   *
   * @return whether the containing application context is safe to cache for another test
   */
  public synchronized boolean endGeneration() {
    checkState(hasGeneration(), "no bean generation is active");
    var gen = state;
    state = null; // clear generation before destroying singletons to avoid recording them
    destroySingletons(List.copyOf(gen.singletons()));
    gen.singletons().forEach(this::clearMergedBeanDefinition);
    gen.originalBeanDefinitions().forEach(this::restoreBeanDefinition);
    gen.originalAliases().forEach(this::restoreAlias);
    clearMetadataCache();
    if (!baseline.singletons().stream().allMatch(this::containsSingleton)) {
      gen.invalidate();
    }
    return gen.reusable();
  }

  @Override
  protected synchronized void addSingleton(String beanName, Object singletonObject) {
    super.addSingleton(beanName, singletonObject);
    if (hasGeneration()) {
      recordSingleton(beanName);
    }
  }

  @Override
  public synchronized void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
    if (hasGeneration()) {
      recordBeanDefinition(beanName);
      invalidateIfBaseline(beanName);
    }
    super.registerBeanDefinition(beanName, beanDefinition);
  }

  @Override
  public synchronized void removeBeanDefinition(String beanName) {
    if (hasGeneration()) {
      recordBeanDefinition(beanName);
      invalidateIfBaseline(beanName);
    }
    super.removeBeanDefinition(beanName);
  }

  @Override
  public synchronized void registerAlias(String name, String alias) {
    if (hasGeneration()) {
      recordAlias(alias);
    }
    super.registerAlias(name, alias);
  }

  @Override
  public synchronized void removeAlias(String alias) {
    if (hasGeneration()) {
      recordAlias(alias);
    }
    super.removeAlias(alias);
  }

  @Override
  public synchronized void destroySingleton(String beanName) {
    if (hasGeneration()) {
      invalidateIfBaseline(beanName);
    }
    super.destroySingleton(beanName);
  }

  @Override
  public synchronized void destroySingletons() {
    if (hasGeneration()) {
      state.invalidate();
    }
    super.destroySingletons();
  }

  @Override
  public synchronized void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
    invalidate();
    super.addBeanPostProcessor(beanPostProcessor);
  }

  @Override
  public synchronized void registerResolvableDependency(
      Class<?> dependencyType,
      Object autowiredValue) {
    invalidate();
    super.registerResolvableDependency(dependencyType, autowiredValue);
  }

  @Override
  public synchronized void registerScope(String scopeName, Scope scope) {
    invalidate();
    super.registerScope(scopeName, scope);
  }

  @Override
  public synchronized void setConversionService(ConversionService conversionService) {
    invalidate();
    super.setConversionService(conversionService);
  }

  private boolean hasGeneration() {
    return state != null;
  }

  private void recordSingleton(String beanName) {
    if (baseline.containsSingleton(beanName)) {
      state.invalidate();
    } else {
      state.recordSingleton(beanName);
    }
  }

  private void recordBeanDefinition(String beanName) {
    state.recordBeanDefinition(beanName, () -> Optional.of(beanName)
        .filter(this::containsBeanDefinition)
        .map(this::getBeanDefinition)
        .map(this::copyBeanDefinition));
  }

  private BeanDefinition copyBeanDefinition(BeanDefinition beanDefinition) {
    return switch (beanDefinition) {
      case AbstractBeanDefinition beanDef -> beanDef.cloneBeanDefinition();
      default -> new GenericBeanDefinition(beanDefinition);
    };
  }

  private void recordAlias(String alias) {
    state.recordAlias(alias, () -> Optional.of(alias)
        .filter(this::isAlias)
        .map(this::canonicalName));
  }

  private void invalidateIfBaseline(String beanName) {
    if (baseline.containsSingleton(beanName)) {
      invalidate();
    }
  }

  private void invalidate() {
    if (hasGeneration()) {
      state.invalidate();
    }
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
}

package com.celements.common.test;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.core.Ordered;

/**
 * {@link BeanFactoryPostProcessor} to set lazy-init on bean definitions that are not
 * {@link LazyInitializationExcludeFilter excluded} and have not already had a value
 * explicitly set.
 * <p>
 * Note that {@link SmartInitializingSingleton SmartInitializingSingletons} are
 * automatically excluded from lazy initialization to ensure that their
 * {@link SmartInitializingSingleton#afterSingletonsInstantiated() callback method} is
 * invoked.
 */
public final class LazyInitBeanFactoryPostProcessor implements BeanFactoryPostProcessor, Ordered {

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
      throws BeansException {
    for (String beanName : beanFactory.getBeanDefinitionNames()) {
      BeanDefinition beanDef = beanFactory.getBeanDefinition(beanName);
      if ((beanDef instanceof AbstractBeanDefinition)
          && (((AbstractBeanDefinition) beanDef).getLazyInit() == null)) {
        beanDef.setLazyInit(true);
      }
    }
  }

}

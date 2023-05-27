package com.celements.common.test;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.core.Ordered;

public final class TestLazyInitBeanFactoryPostProcessor
    implements BeanFactoryPostProcessor, Ordered {

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

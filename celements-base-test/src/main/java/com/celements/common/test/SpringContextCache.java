package com.celements.common.test;

import static com.celements.common.MoreObjectsCel.*;

import java.util.concurrent.locks.ReentrantLock;

import org.springframework.context.ConfigurableApplicationContext;

import com.celements.common.lambda.LambdaExceptionUtil.ThrowingSupplier;

final class SpringContextCache {

  static final String REUSE_CONTEXT_PROP = "celements.test.reuseContext";

  private static final ReentrantLock TEST_METHOD_LOCK = new ReentrantLock();

  private static ConfigurableApplicationContext ctx;

  static {
    var thread = new Thread(SpringContextCache::close, "celements-test-spring-context-close");
    Runtime.getRuntime().addShutdownHook(thread);
  }

  private SpringContextCache() {}

  static boolean isActive() {
    return (ctx != null) && ctx.isActive();
  }

  static ContextLease acquire(ContextCreator contextCreator) throws Exception {
    TEST_METHOD_LOCK.lock();
    ConfigurableApplicationContext context = null;
    try {
      if (!isCacheEnabled()) {
        return new ContextLease(contextCreator.get(), null);
      }
      context = getOrCreateContext(contextCreator);
      var bf = tryCast(context.getBeanFactory(), GenerationAwareBeanFactory.class).orElse(null);
      if (bf != null) {
        beginGeneration(context, bf);
      }
      return new ContextLease(context, bf);
    } catch (Exception | Error exc) {
      evict(context);
      TEST_METHOD_LOCK.unlock();
      throw exc;
    }
  }

  private static boolean isCacheEnabled() {
    return Boolean.parseBoolean(System.getProperty(REUSE_CONTEXT_PROP, Boolean.TRUE.toString()));
  }

  private static ConfigurableApplicationContext getOrCreateContext(ContextCreator contextCreator)
      throws Exception {
    if (!isActive()) {
      close();
    }
    return (ctx != null) ? ctx : contextCreator.get();
  }

  private static GenerationAwareBeanFactory beginGeneration(
      ConfigurableApplicationContext context, GenerationAwareBeanFactory beanFactory) {
    if (ctx == null) {
      beanFactory.sealBaseline();
      ctx = context;
    }
    beanFactory.beginGeneration();
    return beanFactory;
  }

  private static synchronized void close() {
    try {
      if (isActive()) {
        ctx.close();
      }
    } finally {
      ctx = null;
    }
  }

  private static void evict(ConfigurableApplicationContext context) {
    if (ctx == context) {
      close();
    }
  }

  interface ContextCreator extends ThrowingSupplier<ConfigurableApplicationContext, Exception> {}

  record ContextLease(
      ConfigurableApplicationContext context,
      GenerationAwareBeanFactory beanFactory) {

    void close() {
      try {
        if (beanFactory != null) {
          endGeneration(context);
        } else if (context.isActive()) {
          context.close();
        }
      } finally {
        TEST_METHOD_LOCK.unlock();
      }
    }

    private void endGeneration(ConfigurableApplicationContext context) {
      boolean keep = false;
      try {
        if (context.isActive()) {
          keep = beanFactory.endGeneration();
        }
      } finally {
        if (!keep) {
          evict(context);
        }
      }
    }
  }
}

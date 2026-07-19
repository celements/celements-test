package com.celements.common.test;

import static com.celements.common.MoreObjectsCel.*;
import static java.util.Objects.*;

import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.context.ConfigurableApplicationContext;

import com.celements.common.lambda.LambdaExceptionUtil.ThrowingSupplier;
import com.celements.common.test.generation.GenerationAwareBeanFactory;

final class SpringContextCache {

  static final String REUSE_CONTEXT_PROP = "celements.test.reuseContext";

  private final ReentrantLock lock = new ReentrantLock();
  private final boolean reuse;

  private ConfigurableApplicationContext ctx;

  SpringContextCache() {
    reuse = Boolean.parseBoolean(System.getProperty(REUSE_CONTEXT_PROP, Boolean.TRUE.toString()));
    var thread = new Thread(this::evict, "celements-test-spring-context-close");
    Runtime.getRuntime().addShutdownHook(thread);
  }

  Lease acquire(ThrowingSupplier<ConfigurableApplicationContext, Exception> creator)
      throws Exception {
    lock.lock();
    try {
      boolean create = (ctx == null);
      ctx = create ? requireNonNull(creator.get()) : ctx;
      begin(create);
      return new Lease(ctx);
    } catch (Exception | Error exc) {
      try {
        evict();
      } catch (Exception | Error evictExc) {
        exc.addSuppressed(evictExc);
      }
      throw exc;
    }
  }

  private Optional<GenerationAwareBeanFactory> getBeanFactory() {
    return tryCast(ctx.getBeanFactory(), GenerationAwareBeanFactory.class);
  }

  private void begin(boolean created) {
    getBeanFactory().filter(bf -> reuse).ifPresent(bf -> {
      if (created) {
        bf.sealBaseline();
      }
      bf.beginGeneration();
    });
  }

  private void end() {
    boolean reusable = false;
    try {
      reusable = reuse && getBeanFactory()
          .map(GenerationAwareBeanFactory::endGeneration)
          .orElse(false);
    } finally {
      if (!reusable) {
        evict();
      } else {
        lock.unlock();
      }
    }
  }

  private void evict() {
    try {
      if (ctx != null) {
        ctx.close();
      }
    } finally {
      ctx = null;
      if (lock.isHeldByCurrentThread()) {
        lock.unlock();
      }
    }
  }

  final class Lease implements AutoCloseable {

    private final ConfigurableApplicationContext context;

    private Lease(ConfigurableApplicationContext context) {
      this.context = requireNonNull(context);
    }

    ConfigurableApplicationContext context() {
      return context;
    }

    @Override
    public void close() {
      end();
    }
  }
}

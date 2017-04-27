package com.celements.common.test;

import static java.text.MessageFormat.format;
import static org.junit.Assert.*;

/**
 * asserts that an {@link Exception} is thrown within {@link #execute()}. note that the evaluation
 * is already done in the constructor to assure correct usage.
 */
public abstract class ExceptionAsserter {

  private final Class<? extends Exception> token;
  private final Exception instance;
  private final String message;

  public ExceptionAsserter(Class<? extends Exception> token, String message) {
    this.token = token;
    this.instance = null;
    this.message = message;
    evaluate();
  }

  public ExceptionAsserter(Exception exception, String message) {
    this.token = exception.getClass();
    this.instance = exception;
    this.message = message;
    evaluate();
  }

  private void evaluate() {
    try {
      execute();
      fail(format("expecting [{0}]: {1}", token.getSimpleName(), message));
    } catch (Exception exc) {
      if (instance != null) {
        assertSame(instance, exc);
      } else {
        assertTrue(format("expected exeception type [{0}] but was [{1}]", token.getSimpleName(),
            exc.getClass().getSimpleName()), exc.getClass().isAssignableFrom(token));
      }
    }
  }

  protected abstract void execute() throws Exception;

}

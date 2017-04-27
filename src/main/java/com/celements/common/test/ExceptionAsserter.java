package com.celements.common.test;

import static com.google.common.base.Preconditions.*;
import static java.text.MessageFormat.format;
import static org.junit.Assert.*;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * asserts that an {@link Exception} of type T is thrown within {@link #execute()}. for further
 * asserts use {@link #getThrownException()}. note that the evaluation is already done while
 * constructing to assure correct usage.
 *
 * @param <T>
 */
public abstract class ExceptionAsserter<T extends Exception> {

  private final Class<T> token;
  private final T expected;
  private final String message;
  private final T exception;

  public ExceptionAsserter(@NotNull Class<T> token) {
    this(token, "");
  }

  public ExceptionAsserter(@NotNull Class<T> token, @Nullable String message) {
    this(token, null, message);
  }

  public ExceptionAsserter(@NotNull Class<T> token, @Nullable T expected) {
    this(token, expected, "");
  }

  public ExceptionAsserter(@NotNull Class<T> token, @Nullable T expected,
      @Nullable String message) {
    this.token = checkNotNull(token);
    this.expected = expected;
    this.message = message;
    this.exception = checkNotNull(evaluate());
  }

  public @NotNull T getThrownException() {
    return exception;
  }

  private @NotNull T evaluate() {
    try {
      execute();
      fail(format("expecting [{0}]: {1}", token.getSimpleName(), message));
      return null; // never happens
    } catch (Exception exc) {
      if (expected != null) {
        assertSame(expected, exc);
      } else {
        assertTrue(format("expected exeception type [{0}] but was [{1}]", token.getSimpleName(),
            exc.getClass().getSimpleName()), exc.getClass().isAssignableFrom(token));
      }
      return token.cast(exc);
    }
  }

  protected abstract void execute() throws Exception;

}

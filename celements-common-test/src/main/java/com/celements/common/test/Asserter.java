package com.celements.common.test;

import javax.validation.constraints.NotNull;

public interface Asserter<T> {

  public @NotNull T evaluate();

}

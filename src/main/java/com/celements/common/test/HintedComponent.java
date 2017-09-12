package com.celements.common.test;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.xwiki.configuration.ConfigurationSource;

@Documented
@Retention(RUNTIME)
@Target({ ANNOTATION_TYPE })
public @interface HintedComponent {

  Class<ConfigurationSource> clazz();

  String hint();

}

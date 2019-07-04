package org.col.es.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.col.es.mapping.ESDataType;

/**
 * Forces the Java property with this annotation to be mapped to the specified ES data type. No check is done whether that makes sense.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface MapToType {

  ESDataType value();

}
package com.blackrook.engine.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation on {@link Component}s that signify the constructor for this
 * component (rather than using the default). The classes in the parameters
 * should be singletons that the engine should instantiate.
 * @author Matthew Tropiano
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.CONSTRUCTOR)
public @interface ComponentConstructor
{

}

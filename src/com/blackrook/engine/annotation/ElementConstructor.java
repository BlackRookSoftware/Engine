package com.blackrook.engine.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.blackrook.commons.logging.Logger;

/**
 * Annotation on {@link Element}s that signify the constructor for this
 * component (rather than using the default). The classes in the parameters
 * should be singletons that the engine should instantiate. If the type is {@link Logger},
 * a logger designated for the class is created.
 * @author Matthew Tropiano
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.CONSTRUCTOR)
public @interface ElementConstructor
{

}

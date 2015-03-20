package com.blackrook.engine.annotation.component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.blackrook.engine.EnginePool;
import com.blackrook.engine.EnginePool.PoolPolicy;

/**
 * Annotation for classes that instantiated as pooled objects for the Engine.
 * This annotation only has an effect on classes annotated with {@link Component}. 
 * Constructors {@link ComponentConstructor} annotations are called for each object to create the pool.
 * Pooled objects cannot have commands or variables associated with them.
 * @author Matthew Tropiano
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Pooled
{
	/**
	 * This number is the initial amount of elements in the pool.
	 */
	int value();

	/**
	 * The amount to expand the pool by if its policy dictates
	 * that it needs to expand.
	 */
	int expansion() default EnginePool.EXPAND_DOUBLE;
	
	/**
	 * The pool policy to use. Uses {@link PoolPolicy#SENSIBLE} by default.
	 * @see PoolPolicy
	 */
	PoolPolicy policy() default PoolPolicy.SENSIBLE;

}

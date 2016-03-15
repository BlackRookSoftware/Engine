package com.blackrook.engine.roles;

import com.blackrook.engine.annotation.element.Ordering;

/**
 * This interface describes an object that should receive input events.
 * <p>
 * The Engine uses something called <b>input codes</b> to define actions taken so that the component
 * that is supposed to listen for device actions can broadcast filtered events, or automate them.
 * <p>
 * Input codes should be defined by the layer that reads device input.
 * A good strategy for handling heterogeneous input systems like GUIs are to have those handle system input,
 * and then if the input wasn't handled, pass it along to the Engine.
 * <p>
 * The {@link Ordering} annotation can influence invocation order on this type of object.
 * @author Matthew Tropiano
 */
public interface EngineInputListener extends EngineInputHandler
{

}

package com.blackrook.engine.roles;

import java.util.Properties;

import com.blackrook.engine.Engine;
import com.blackrook.engine.annotation.element.Ordering;


/**
 * This is a component that has methods invoked on it when persistent settings are saved and loaded.
 * <p>
 * The {@link Ordering} annotation can influence invocation order on this type of object.
 * @author Matthew Tropiano
 */
public interface EngineSettingsListener
{
	/**
	 * Puts this element's user-specific settings into the provided {@link Properties} object.
	 * Invoked when {@link Engine} has {@link Engine#saveSettings()} called on it.
	 * @param settings the Properties object to store settings into.
	 */
	public void onSaveUserSettings(Properties settings);
	
	/**
	 * Puts this element's global settings into the provided {@link Properties} object.
	 * Invoked when {@link Engine} has {@link Engine#saveSettings()} called on it.
	 * @param settings the Properties object to store settings into.
	 */
	public void onSaveGlobalSettings(Properties settings);
	
	/**
	 * Sets this element's user-specific settings by reading them from the provided {@link Properties} object.
	 * Invoked when {@link Engine} has {@link Engine#loadSettings()} called on it.
	 * @param settings the Properties object to read settings from.
	 */
	public void onLoadUserSettings(Properties settings);
	
	/**
	 * Sets this element's global settings by reading them from the provided {@link Properties} object.
	 * Invoked when {@link Engine} has {@link Engine#loadSettings()} called on it.
	 * @param settings the Properties object to read settings from.
	 */
	public void onLoadGlobalSettings(Properties settings);
	
}

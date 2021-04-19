/*******************************************************************************
 * Copyright (c) 2016-2021 Black Rook Software
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Lesser Public License v2.1 which accompanies this 
 * distribution, and is available at 
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
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

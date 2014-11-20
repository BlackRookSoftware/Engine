package com.blackrook.engine.components;

import java.util.Arrays;

import com.blackrook.commons.Common;
import com.blackrook.engine.Engine;
import com.blackrook.engine.EngineConsole;
import com.blackrook.engine.EngineConsoleManager;
import com.blackrook.engine.EngineConsoleManager.CVARMapping;
import com.blackrook.engine.annotation.CCMD;
import com.blackrook.engine.annotation.Component;
import com.blackrook.engine.annotation.ComponentConstructor;

/**
 * 
 * @author Matthew Tropiano
 */
@Component
public final class EngineCommonCommands
{
	/** Engine reference. */
	private Engine engine;
	/** Engine reference. */
	private EngineConsole console;
	/** Console manager. */
	private EngineConsoleManager consoleManager;
	
	@ComponentConstructor
	public EngineCommonCommands(Engine engine, EngineConsole console, EngineConsoleManager consoleManager)
	{
		this.engine = engine;
		this.console = console;
		this.consoleManager = consoleManager;
	}
	
	@CCMD(description = "Tells the engine to shut down.")
	public void quit(int status)
	{
		engine.shutDown(status);
	}
	
	@CCMD(description = "Echos a line to the console out.")
	public void echo(String text)
	{
		console.println(text);
	}
	
	@CCMD(description = "Dumps all console variables to console.")
	public void cvarList(String text)
	{
		String[] variables = consoleManager.getVariableNames();
		
		Arrays.sort(variables);

		int i = 0;
		for (String var : variables)
		{
			CVARMapping mapping = consoleManager.getVariableDefinition(var);
			console.printfln(
				"%s%s %s\t\t%s",  
				mapping.isArchived() ? "A" : "-", 
				mapping.isReadOnly() ? "R" : "-", 
				var, 
				mapping.getDescription()
			);
			i++;
		}
		
		console.printfln("count %d", i);

		
	}
	
	@CCMD(description = "Lists all console commands.")
	public void cmdList(String prefix)
	{
		String[] commands = null;
		if (Common.isEmpty(prefix))
			commands = consoleManager.getCommandNames();
		else
			commands = consoleManager.getCommandNamesForPrefix(prefix);
		
		Arrays.sort(commands);
		
		int i = 0;
		for (String cmd : commands)
		{
			console.printfln("%s\t\t%s", cmd, consoleManager.getCommandDefinition(cmd).getDescription());
			i++;
		}
		
		console.printfln("count %d", i);
	}
}

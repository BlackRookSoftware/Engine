package com.blackrook.engine;

import java.util.Arrays;

import com.blackrook.commons.Common;
import com.blackrook.engine.EngineConsoleManager.CVARMapping;
import com.blackrook.engine.annotation.element.CCMD;
import com.blackrook.engine.annotation.element.CVAR;

/**
 * The common engine variables and commands.
 * @author Matthew Tropiano
 */
public final class EngineCommon
{
	/** Engine reference. */
	private Engine engine;
	/** Engine reference. */
	private EngineConsole console;
	/** Console manager. */
	private EngineConsoleManager consoleManager;
	
	public EngineCommon(Engine engine, EngineConsole console, EngineConsoleManager consoleManager)
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
		int maxlen = consoleManager.getVariableLongestLength();
		int i = 0;
		for (String var : variables)
		{
			CVARMapping mapping = consoleManager.getVariableDefinition(var);
			console.printfln(
				"%s%s%s %-"+maxlen+"s %s",  
				mapping.isArchived() ? "A" : "-", 
				mapping.isGlobal() ? "G" : "-", 
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
		int maxlen = consoleManager.getCommandLongestLength();

		int i = 0;
		for (String cmd : commands)
		{
			console.printfln("%-"+maxlen+"s %s", cmd, consoleManager.getCommandDefinition(cmd).getDescription());
			i++;
		}
		
		console.printfln("count %d", i);
	}
	
	@CVAR(value = "java_version", description = "JVM version.")
	public String getJavaVersion()
	{
		return System.getProperty("java.version");
	}
	
	@CVAR(value = "java_vendor", description = "JVM vendor.")
	public String getJavaVendor()
	{
		return System.getProperty("java.vendor");
	}

	@CVAR(value = "java_vm_name", description = "JVM name.")
	public String getJavaVMName()
	{
		return System.getProperty("java.vm.name");
	}

	@CVAR(value = "java_classpath", description = "JVM current classpath.")
	public String getJavaClasspath()
	{
		return System.getProperty("java.class.path");
	}

	@CVAR(value = "java_libpath", description = "JVM native library path.")
	public String getJavaLibraryPath()
	{
		return System.getProperty("java.library.path");
	}

	@CVAR(value = "os_name", description = "OS name.")
	public String getOSName()
	{
		return System.getProperty("os.name");
	}

	@CVAR(value = "os_version", description = "OS version name.")
	public String getOSVersion()
	{
		return System.getProperty("os.version");
	}

	@CVAR(value = "os_arch", description = "OS architecture type.")
	public String getOSArchitecture()
	{
		return System.getProperty("os.arch");
	}

	@CVAR(value = "workdir", description = "Working directory path.")
	public String getWorkingDirectory()
	{
		return Common.WORK_DIR;
	}

	@CVAR(value = "appdata", description = "Application data directory path.")
	public String getApplicationDataDirectory()
	{
		return Common.APP_DIR;
	}

	@CVAR(value = "homedir", description = "Home directory path.")
	public String getHomeDirectory()
	{
		return Common.HOME_DIR;
	}

}

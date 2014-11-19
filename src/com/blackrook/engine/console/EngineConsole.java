package com.blackrook.engine.console;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

import com.blackrook.commons.Common;
import com.blackrook.commons.CommonTokenizer;
import com.blackrook.commons.list.List;
import com.blackrook.engine.Engine;
import com.blackrook.engine.EngineConfig;
import com.blackrook.engine.annotation.CCMD;
import com.blackrook.engine.annotation.Component;
import com.blackrook.engine.annotation.ComponentConstructor;
import com.blackrook.engine.console.EngineConsoleManager.CCMDMapping;
import com.blackrook.engine.exception.ConsoleCommandInvocationException;

/**
 * The console itself.
 * @author Matthew Tropiano
 */
@Component
public class EngineConsole extends JFrame
{
	private static final long serialVersionUID = 3854911727580406755L;
	
	/** Text area for scrollable window. */
	private JTextArea textArea;
	/** The scroll bars for the window. */
	private JScrollPane scrollPane;
	/** The entry field. */
	private JTextField entryField;

	/** Desktop toolkit. */
	private Toolkit toolkit;	
	/** Console manager. */
	private EngineConsoleManager consoleManager;
	/** Command history. */
	private List<String> commandHistory;
	/** Command history index. */
	private int commandIndex;
	
	/**
	 * Creates the console.
	 * @param engine the engine instance.
	 * @param config the configuration.
	 */
	@ComponentConstructor
	public EngineConsole(Engine engine, EngineConfig config, EngineConsoleManager manager)
	{
		super();
		
		String windowTitle = config.getApplicationName() + (config.getApplicationVersion() == null ? "" : " v"+config.getApplicationVersion());
		setTitle(windowTitle);
		setIconImage(config.getApplicationIcon());
		setVisible(false);
	
		toolkit = Toolkit.getDefaultToolkit();
		commandHistory = new List<String>(50);

		consoleManager = manager;
		
		scrollPane = createScrollPane(textArea = createTextArea());
		entryField = createEntryField();
		
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		
		JPanel base = new JPanel();
		base.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		base.setLayout(new BorderLayout());
		
		base.add(scrollPane, BorderLayout.CENTER);
		base.add(entryField, BorderLayout.SOUTH);
		
		contentPane.add(base, BorderLayout.CENTER);
		contentPane.setPreferredSize(new Dimension(640, 480));
		
		addWindowFocusListener(new WindowFocusListener()
		{
			@Override
			public void windowLostFocus(WindowEvent arg0)
			{
				// Nothing.
			}
			
			@Override
			public void windowGainedFocus(WindowEvent arg0)
			{
				entryField.requestFocus();
			}
		});
		
		pack();
		setVisible(true);
	}
	
	// Creates the text area.
	private JTextArea createTextArea()
	{
		JTextArea out = new JTextArea();
		out.setEditable(false);
		out.setDisabledTextColor(Color.BLACK);
		out.setLineWrap(true);
		return out;
	}
	
	// Creates the scroll pane.
	private JScrollPane createScrollPane(JTextArea textarea)
	{
		JScrollPane out = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		out.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		return out;
	}

	// creates the entry field.
	private JTextField createEntryField()
	{
		final JTextField field = new JTextField();
		
		field.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		field.setFocusTraversalKeysEnabled(false);

		// focus.
		field.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(FocusEvent arg0)
			{
				field.selectAll();
			}
		});
		
		// key listening.
		field.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent event)
			{
				// autocomplete.
				if (event.getKeyCode() == KeyEvent.VK_TAB)
				{
					int cpos = field.getCaretPosition();
					String selected = field.getSelectedText();
					String prefix = (!Common.isEmpty(selected) ? selected : field.getText().substring(0, Math.max(cpos, 0))).trim();
					if (!Common.isEmpty(prefix))
					{
						String[] cmds = consoleManager.getCommandNamesForPrefix(prefix);
						if (Common.isEmpty(cmds))
						{
							println("NOTICE: No possible completions for input.");
							toolkit.beep();
						}
						else if (cmds.length == 1)
						{
							field.setText(cmds[0]);
						}
						else
						{
							println("Possible completions:");
							for (String cmd : cmds)
								print(cmd + " ");
							println();
						}
					}
					
					event.consume();
				}

				// send command.
				else if (event.getKeyCode() == KeyEvent.VK_ENTER)
				{
					String command = field.getText();
					field.setText("");
					parseCommand(command);
					commandHistory.add(command);
					commandIndex = -1;
				}

				// history back.
				else if (event.getKeyCode() == KeyEvent.VK_UP)
				{
					if (commandIndex < 0)
					{
						commandIndex = commandHistory.size();
						if (commandIndex > 0)
						{
							field.setText(commandHistory.getByIndex(commandIndex - 1));
							commandIndex--;
						}
					}
					else
					{
						field.setText(commandHistory.getByIndex(commandIndex));
					}
				}

				// history forward.
				else if (event.getKeyCode() == KeyEvent.VK_DOWN)
				{
					if (commandIndex >= 0)
					{
						commandIndex = commandHistory.size();
						if (commandIndex > 0)
						{
							field.setText(commandHistory.getByIndex(commandIndex - 1));
							commandIndex--;
						}
					}
				}
				
			}
			
			public void keyTyped(KeyEvent event) 
			{
				commandIndex = -1;
			}
			
		});
		
		return field;
	}

	/**
	 * Sends a command.
	 */
	private void parseCommand(String commandString)
	{
		char[] cmdchars = commandString.toCharArray();
		StringBuilder sb = new StringBuilder();
		
		final int STATE_INIT = 0;
		final int STATE_INQUOTE = 1;
		final int STATE_INQUOTE_ESCAPE = 2;
		int state = STATE_INIT;
		
		for (int i = 0; i < cmdchars.length; i++)
		{
			char c = cmdchars[i];
			
			switch (state)
			{
				case STATE_INIT:
				{
					if (c == '"')
					{
						sb.append(c);
						state = STATE_INQUOTE;
					}
					else if (c == ';')
					{
						sendCommand(sb.toString());
						sb.delete(0, sb.length());
					}
					else
						sb.append(c);
				}
				break;
					
				case STATE_INQUOTE:
				{
					if (c == '"')
					{
						sb.append(c);
						state = STATE_INIT;
					}
					else if (c == '\\')
					{
						sb.append(c);
						state = STATE_INQUOTE_ESCAPE;
					}
					else
						sb.append(c);					
				}
				break;

				case STATE_INQUOTE_ESCAPE:
				{
					sb.append(c);
					state = STATE_INIT;
				}
				break;
				
			}
			
		}

		if (sb.length() > 0)
			sendCommand(sb.toString());
		
	}
	
	// Send command.
	private void sendCommand(String commandString)
	{
		if (Common.isEmpty(commandString))
			return;
			
		CommonTokenizer tokenizer = new CommonTokenizer(commandString);
		String cmd = tokenizer.nextToken();
		List<String> argList = new List<String>();
		while (tokenizer.hasMoreTokens())
			argList.add(tokenizer.nextToken());
		
		String[] args = new String[argList.size()];
		argList.toArray(args);
		
		Object out = null;
		try {
			
			if (consoleManager.containsCommand(cmd))
				out = consoleManager.callCommand(cmd, (Object[])args);
			else if (consoleManager.containsAlias(cmd))
				parseCommand(consoleManager.getAlias(cmd));
			else if (consoleManager.containsVariable(cmd))
			{
				if (args.length == 0)
					println(cmd + " is " + String.valueOf(consoleManager.getVariable(cmd)));
				else
				{
					consoleManager.setVariable(cmd, args[0]);
					println(cmd + " set to " + String.valueOf(consoleManager.getVariable(cmd)));
				}
			}
			else
				println("ERROR: " + cmd + " is not a command, alias, or variable.");
		
		} catch (ConsoleCommandInvocationException ex) {

			println("ERROR: " + ex.getMessage());
			CCMDMapping mapping = consoleManager.getCommandDefinition(cmd);
			String[] usage = mapping.getUsage();
			
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < usage.length; i++)
			{
				sb.append('[').append(usage[i]).append(']');
				if (i < usage.length - 1)
					sb.append(' ');
			}
			println("Usage: " + cmd + " " + sb.toString());

		} catch (Exception ex) {
			println("EXCEPTION: " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}
		
		if (out != null)
			println(String.valueOf(out));
	}
	
	
	
	/**
	 * Prints a message to the console.
	 * @param object the message to print (see {@link String#valueOf(Object)}).
	 */
	public void print(Object object)
	{
		textArea.append(String.valueOf(object));
	}
	
	/**
	 * Prints a formatted message to the console.
	 * @param formatting the format text (see {@link String#format(String, Object...)}).
	 * @param args the message to print (see {@link String#valueOf(Object)}).
	 */
	public void printf(String formatting, Object ... args)
	{
		textArea.append(String.format(formatting, args));
	}
	
	/**
	 * Prints a message to the console with a newline appended to it.
	 * @param object the message to print (see {@link String#valueOf(Object)}).
	 */
	public void println(Object object)
	{
		print(String.valueOf(object) + '\n');
	}
	
	/**
	 * Prints a newline.
	 */
	public void println()
	{
		print('\n');
	}
	
	
	/**
	 * Prints a formatted message to the console with a newline appended to it.
	 * @param formatting the format text (see {@link String#format(String, Object...)}).
	 * @param args the message to print (see {@link String#valueOf(Object)}).
	 */
	public void printfln(String formatting, Object ... args)
	{
		printf(formatting + '\n', args);
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
			printfln("%s\t\t%s", cmd, consoleManager.getCommandDefinition(cmd).getDescription());
			i++;
		}
		
		printfln("count %d", i);
	}
	
}

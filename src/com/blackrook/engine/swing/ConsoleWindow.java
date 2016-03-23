/*******************************************************************************
 * Copyright (c) 2016 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.engine.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

import com.blackrook.commons.Common;
import com.blackrook.commons.list.List;
import com.blackrook.engine.Engine;
import com.blackrook.engine.EngineConfig;
import com.blackrook.engine.EngineConsole;

/**
 * The console itself.
 * @author Matthew Tropiano
 */
public class ConsoleWindow extends JFrame
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
	private EngineConsole consoleManager;
	/** Command history. */
	private List<String> commandHistory;

	/** Command history index. */
	private int commandIndex;
	
	/**
	 * Creates the console.
	 * @param engine the engine instance.
	 * @param config the configuration.
	 * @param console the engine console.
	 */
	public ConsoleWindow(Engine engine, EngineConfig config, EngineConsole console)
	{
		super();
		
		String windowTitle = config.getApplicationName() + (config.getApplicationVersion() == null ? "" : " v"+config.getApplicationVersion()) + " Console";
		setTitle(windowTitle);
		setIconImage(config.getApplicationIcon());
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setVisible(false);
		
		toolkit = Toolkit.getDefaultToolkit();
		commandHistory = new List<String>(50);

		consoleManager = console;
		
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
		
		Rectangle maxwin = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		
		contentPane.setPreferredSize(new Dimension(maxwin.width/2, maxwin.height/2));
		
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
		
	}
	
	// Creates the text area.
	private JTextArea createTextArea()
	{
		JTextArea out = new JTextArea();
		out.setEditable(false);
		out.setDisabledTextColor(Color.BLACK);
		out.setLineWrap(true);
		out.setFont(new Font("Courier", Font.PLAIN, 12));
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
						String[] vars = consoleManager.getVariableNamesForPrefix(prefix);
						if (Common.isEmpty(cmds) && Common.isEmpty(vars))
						{
							println("NOTICE: No possible completions for input.");
							toolkit.beep();
						}
						else if (cmds.length == 1)
						{
							field.setText(cmds[0]);
						}
						else if (vars.length == 1)
						{
							field.setText(vars[0]);
						}
						else
						{
							if (!Common.isEmpty(cmds))
							{
								println("Commands:");
								for (String c : cmds)
									print(c + " ");
								println();
							}
							if (!Common.isEmpty(vars))
							{
								println("\nVariables:");
								for (String v : vars)
									print(v + " ");
								println();
							}
						}
					}
					
					event.consume();
				}

				// send command.
				else if (event.getKeyCode() == KeyEvent.VK_ENTER)
				{
					String command = field.getText();
					field.setText("");
					consoleManager.parseCommand(command);
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
	 * Prints a message to the console.
	 * @param object the message to print (see {@link String#valueOf(Object)}).
	 */
	public void print(Object object)
	{
		textArea.append(String.valueOf(object));
		textArea.setCaretPosition(textArea.getText().length());
	}
	
	/**
	 * Prints a formatted message to the console.
	 * @param formatting the format text (see {@link String#format(String, Object...)}).
	 * @param args the message to print (see {@link String#valueOf(Object)}).
	 */
	public void printf(String formatting, Object ... args)
	{
		textArea.append(String.format(formatting, args));
		textArea.setCaretPosition(textArea.getText().length());
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

}

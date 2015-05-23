/*******************************************************************************
 * Copyright (c) 2009-2014 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.engine.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import org.omg.CORBA.portable.ApplicationException;

import com.blackrook.commons.Common;
import com.blackrook.engine.EngineConfig;


/**
 * A dialog window for end-user program crashes that, if the JVM 
 * and platform supports it, presents the opportunity for the user
 * to open their E-mail client and send an E-mail to the developer.
 * @author Matthew Tropiano
 */
public class ProgramCrashDialog extends JDialog
{
	private static final long serialVersionUID = -6446066521771267843L;

	/** E-mail address. */ 
	private String eMailAddress;
	/** E-mail subject. */ 
	private String eMailSubject;
	/** E-mail body prefix. */ 
	private String eMailBodyPrefix;
	
	/** Exception reference. */
	private Throwable throwableRef;
	
	/** Text area for the crash text. */
	private JTextArea bodyArea;
	
	/**
	 * Creates a new crash dialog.
	 * @param title Window title.
	 * @param titleIcon Window title icon. Can be null.
	 * @param image An image to put on the left side. Can be null.
	 * @param heading Dialog heading (big text).
	 * @param text A message detailing what happened.
	 * @param closeButtonText The close button text.
	 * @param exception The exception itself.
	 * @param logFiles the set of log files to add to the output.
	 */
	public ProgramCrashDialog(String title, Image titleIcon, Icon image, String heading, String text, String closeButtonText, Throwable exception, File... logFiles)
	{
		setModal(true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle(title);
		setIconImage(titleIcon);

		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		
		JPanel temp;
		
		temp = createImagePanel(image);
		if (temp != null)
			contentPane.add(temp, BorderLayout.WEST);
		
		// TODO: add other crap.
		
		
		contentPane.setPreferredSize(new Dimension(640,480));
		setResizable(true);
		pack();
	}

	/**
	 * Creates the image panel placed to the west of the main dialog content.
	 * @param icon the image to add.
	 * @return a new panel for placement, or null if icon is null.
	 */
	protected JPanel createImagePanel(Icon icon)
	{
		if (icon == null)
			return null;
		
		JPanel out = new JPanel();
		out.setLayout(new BorderLayout());
		
		JLabel label = new JLabel();
		label.setIcon(icon);
		label.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		
		out.add(label, BorderLayout.NORTH);
		return out;
	}
	
	/**
	 * Creates the heading label panel.
	 * @param text the label text.
	 * @return the new panel.
	 */
	protected JPanel createHeadingPanel(String text)
	{
		JPanel out = new JPanel();
		out.setLayout(new BorderLayout());
		out.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		JLabel label = new JLabel("<h1>"+text+"</h1>");
		out.add(label, BorderLayout.CENTER);
		return out;
	}
	
	/**
	 * Creates the text label panel.
	 * @param text the label text.
	 * @return the new panel.
	 */
	protected JPanel createTextPanel(String text)
	{
		JPanel out = new JPanel();
		out.setLayout(new BorderLayout());
		out.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		JLabel label = new JLabel(text);
		out.add(label, BorderLayout.CENTER);
		return out;
	}
	
	/**
	 * Creates the close button.
	 * @param dialog the parent dialog.
	 * @param text the button text.
	 * @return a new button.
	 */
	protected JButton createCloseButton(final JDialog dialog, String text)
	{
		return new JButton(new AbstractAction(text)
		{
			private static final long serialVersionUID = 5641944497201023651L;

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				dialog.setVisible(false);
			}
		});
	}
	
}

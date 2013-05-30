/*
 * Copyright 2012 maxstrauch
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package helpers;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

/**
 * A simple form layout wrapper using the GridBagLayout
 */
public class SimpleForm extends JPanel {

	/**
	 * ID for Java(TM) Object Serialization Specification
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constraint for right field
	 */
	private GridBagConstraints lastConstraints = null;

	/**
	 * Constraint for middle field
	 */
	private GridBagConstraints middleConstraints = null;

	/**
	 * Constraint for left label field
	 */
	private GridBagConstraints labelConstraints = null;

	/**
	 * A empty border
	 */
	private EmptyBorder border;

	/**
	 * Containing {@link JPanel}
	 */
	private JPanel holder;
	
	/**
	 * Constructs a new form object
	 */
	public SimpleForm() {
		super(new BorderLayout());
		holder = new JPanel(new GridBagLayout());
		holder.setOpaque(false);
		setOpaque(false);
		add(holder, BorderLayout.NORTH);
		border = new EmptyBorder(new JTextArea().getInsets().top, 0, 0, 0);
		lastConstraints = new GridBagConstraints();
		lastConstraints.fill = GridBagConstraints.HORIZONTAL;
		lastConstraints.anchor = GridBagConstraints.NORTHWEST;
		lastConstraints.weightx = 1.0;
		lastConstraints.gridwidth = GridBagConstraints.REMAINDER;
		lastConstraints.insets = new Insets(2, 2, 2, 2);
		middleConstraints = (GridBagConstraints) lastConstraints.clone();
		middleConstraints.weightx = 0.0;
		middleConstraints.gridwidth = GridBagConstraints.RELATIVE;
		labelConstraints = (GridBagConstraints) lastConstraints.clone();
		labelConstraints.weightx = 0.0;
		labelConstraints.gridwidth = 1;
	}

	/**
	 * Adds a last field
	 * 
	 * @param c
	 *            Component to add
	 */
	public SimpleForm addLastField(Component c) {
		GridBagLayout gbl = (GridBagLayout) holder.getLayout();
		gbl.setConstraints(c, lastConstraints);
		holder.add(c);
		return this;
	}
	
	/**
	 * Adds all given components in a horizontal row
	 * and adds this row as last fields
	 * 
	 * @param c The components to add
	 */
	public SimpleForm addMultiField(Component...c) {
		JPanel hor = new JPanel(new GridLayout(1, c.length));
		for (Component component : c)
				hor.add(component);
		return addLastField(hor);
	}

	/**
	 * Adds a label
	 * 
	 * @param c
	 *            Label component to add
	 */
	public SimpleForm addLabel(Component c) {
		GridBagLayout gbl = (GridBagLayout) holder.getLayout();
		gbl.setConstraints(c, labelConstraints);
		holder.add(c);
		return this;
	}

	/**
	 * Adds a normal label
	 * 
	 * @param s
	 *            Label text
	 * @return The added label
	 */
	public SimpleForm addLabel(String s) {
		addLabel(s, true);
		return this;
	}

	/**
	 * Adds a normal label
	 * 
	 * @param s
	 *            Label text
	 * @param fill
	 *            True if label should be centered
	 * @return The added label
	 */
	public SimpleForm addLabel(String s, boolean fill) {
		JLabel c = new JLabel("<html>" + s + "</html>");
		if (fill) {
			labelConstraints.fill = GridBagConstraints.VERTICAL;
		} else {
			labelConstraints.fill = GridBagConstraints.NONE;
			c.setBorder(border);
		}
		addLabel(c);
		
		return this;
	}

	/**
	 * Adds a middle component
	 * 
	 * @param c
	 *            Middle component
	 */
	public SimpleForm addMiddleField(Component c) {
		GridBagLayout gbl = (GridBagLayout) getLayout();
		gbl.setConstraints(c, middleConstraints);
		holder.add(c);
		
		return this;
	}

	/**
	 * Adds one component on a row
	 * 
	 * @param c
	 *            Component
	 */
	public SimpleForm addSpan(Component c) {
		GridBagConstraints colspanCons = new GridBagConstraints();
		colspanCons.fill = GridBagConstraints.HORIZONTAL;
		colspanCons.anchor = GridBagConstraints.CENTER;
		colspanCons.insets = new Insets(4, 0, 4, 4);
		colspanCons.gridwidth = GridBagConstraints.REMAINDER;
		GridBagLayout gbl2 = (GridBagLayout) holder.getLayout();
		gbl2.setConstraints(c, colspanCons);
		holder.add(c);
		
		return this;
	}

	/**
	 * Adds a separator line with title
	 * 
	 * @param title
	 *            Title of separator
	 */
	public SimpleForm addSeperator(String title) {
		GridBagLayout gbl = new GridBagLayout();
		JPanel p = new JPanel(gbl);
		p.setOpaque(false);

		GridBagConstraints rightCons = new GridBagConstraints();
		rightCons.fill = GridBagConstraints.HORIZONTAL;
		rightCons.anchor = GridBagConstraints.CENTER;
		rightCons.weightx = 1.0;
		rightCons.gridwidth = GridBagConstraints.REMAINDER;

		GridBagConstraints leftCons = new GridBagConstraints();
		leftCons.fill = GridBagConstraints.HORIZONTAL;
		leftCons.anchor = GridBagConstraints.CENTER;
		leftCons.gridwidth = GridBagConstraints.RELATIVE;
		leftCons.insets = new Insets(0, 0, 0, 4);

		p.add(new JLabel("<html><b>" + title + "</b></html>"), leftCons);
		p.add(new JSeparator(), rightCons);
		addSpan(p);
		return this;
	}

}
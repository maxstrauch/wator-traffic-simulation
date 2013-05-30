/*
 * Copyright 2013 maxstrauch
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
package simulation.traffic.motorway;

import helpers.SimpleForm;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class MotorwaySimulationGui extends JPanel implements ActionListener {
 
	private static final long serialVersionUID = 1L;
	
    private JButton stepButton, newButton;
    
    private JLabel statisticalData;

    private JSlider slider;
    
    private JSpinner length, carLinger;
    
    private JToggleButton autoToggle;
    
    private JScrollPane scroll;
    
    private Timer timer;

    private MotorwaySimulation motorway;
    
    public MotorwaySimulationGui() {
        super(new BorderLayout());
        initGui();
    }
    
    /**
     * Creates the GUI components and layouts them
     */
    private void initGui() {
		// Create the component buttons
		stepButton = new JButton("Step");
		stepButton.setActionCommand("step");
		stepButton.addActionListener(this);
		
		autoToggle = new JToggleButton("Run");
		autoToggle.setActionCommand("auto");
		autoToggle.addActionListener(this);
		
		statisticalData = new JLabel("0");
		statisticalData.setPreferredSize(stepButton.getPreferredSize());
		
		slider = new JSlider(JSlider.HORIZONTAL, 2, 20, 2);
		slider.setMajorTickSpacing(2);
		slider.setSnapToTicks(true);
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		slider.setValue(2);
		
		newButton = new JButton("Create new motorway");
		newButton.setActionCommand("new");
		newButton.addActionListener(this);
		
		length = new JSpinner(new SpinnerNumberModel(42, 2, 1000, 1));
		carLinger = new JSpinner(new SpinnerNumberModel(.15, 0, 1, 0.05));
		carLinger.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				motorway.setCarLinger((double) carLinger.getValue());
			}
		});
		
		// Set initial values
		setEnabled(false, stepButton, autoToggle, slider, carLinger);
		
		// Lay-out components
		setLayout(new BorderLayout(4, 4));
		setBorder(new EmptyBorder(4, 4, 4, 4));

		scroll = new JScrollPane();
		scroll.setPreferredSize(new Dimension(700, 200));
		add(scroll, BorderLayout.CENTER);
        
        JPanel controls = new JPanel(new GridLayout(1, 2, 8, 8));
        controls.add(new SimpleForm()
        	.addSeperator("Creation")
        	.addMultiField(length, newButton)
        	.addSeperator("Parameters")
        	.addLabel("Car linger:")
        	.addLastField(carLinger)
	    	.addSeperator("Statistics")
	    	.addLastField(statisticalData));
        
        controls.add(new SimpleForm()
        	.addSeperator("Simulation")
	    	.addLastField(slider)
	    	.addMultiField(autoToggle, stepButton));
        
        add(controls, BorderLayout.SOUTH);
    }
   
    
    /**
     * Invoked when the user presses some button
     */
    public void actionPerformed(ActionEvent evt) {
    	String cmd = evt.getActionCommand();
    	
    	if ("new".equals(cmd)) {
    		// Create a new motorway
    		motorway = new MotorwaySimulation((int) length.getValue());
    		
    		scroll.getViewport().removeAll();
    		scroll.getViewport().add(motorway);
    		
    		motorway.setCarLinger((double) carLinger.getValue());
    		
    		setEnabled(true, stepButton, autoToggle, slider, carLinger);
    	}
    	
    	if ("step".equals(evt.getActionCommand())) {
    		// Make a step
    		motorway.step();
    		
    		// Set statistical data
    		statisticalData.setText(motorway.getGeneration() + ". gen, " +
    				motorway.getAverageVelocity() + " avg v");
    	}
    	
    	if ("auto".equals(evt.getActionCommand())) {
    		if (autoToggle.isSelected()) {
    			timer = new Timer(1000/slider.getValue(), this);
    			timer.setActionCommand("step");
    			timer.start();
    		} else {
    			timer.stop();
    			timer = null;
    		}
    		
    		// Disable other components
    		setEnabled(!autoToggle.isSelected(), stepButton, slider, 
    				newButton, length, carLinger, motorway);
    		
    		// Manage caption
    		autoToggle.setText(autoToggle.isSelected() ? "Stop" : "Run");
    	}
    }
    
    public static final void setEnabled(boolean enabled, JComponent...components) {
    	for (JComponent component : components)
			component.setEnabled(enabled);
    }
    
}
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
package simulation.traffic.highway;
 

import helpers.SimpleForm;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
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

import simulation.traffic.highway.HighwayCell.Car;
import simulation.traffic.highway.HighwayCell.TrafficLight;

public class HighwaySimulationGui extends JPanel implements ActionListener {
 
	private static final long serialVersionUID = 1L;
	
    private JButton stepButton, newButton, placeRandom;
    
    private JLabel statisticalData;

    private JSlider slider;
    
    private Timer timer;

    private HighwaySimulation road;
    
    private JSpinner length, carLinger, redProb, redTime;
    
    private JToggleButton autoToggle, placeCar, placeTrafficLight, placeNothing;
    
    private ButtonGroup placeGroup;
    
    public HighwaySimulationGui() {
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
		
		placeRandom = new JButton("Place random");
		placeRandom.setActionCommand("random");
		placeRandom.addActionListener(this);
		
		statisticalData = new JLabel("0");
		
		slider = new JSlider(JSlider.HORIZONTAL, 2, 20, 2);
		slider.setMajorTickSpacing(2);
		slider.setSnapToTicks(true);
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		slider.setValue(2);

		length = new JSpinner(new SpinnerNumberModel(42, 2, 1000, 1));

		newButton = new JButton("Create new highway");
		newButton.addActionListener(this);
		
		placeCar = new JToggleButton("Car");
		placeCar.setActionCommand("placeCar");
		placeCar.addActionListener(this);
		
		carLinger = new JSpinner(new SpinnerNumberModel(.15, 0, 1, 0.05));
		carLinger.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				road.setCarLinger((double) carLinger.getValue());
			}
		});
		
		placeTrafficLight = new JToggleButton("Light");
		placeTrafficLight.setActionCommand("placeLight");
		placeTrafficLight.addActionListener(this);
		
		redProb = new JSpinner(new SpinnerNumberModel(.25, 0, 1, 0.05));
		redProb.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				road.setTrafficLightRedProb((double) redProb.getValue());
			}
		});
		
		redTime = new JSpinner(new SpinnerNumberModel(2, 1, 1000, 1));
		redTime.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				road.setTrafficLightRedTime((int) redTime.getValue());
			}
		});
		
		placeNothing = new JToggleButton("Clear");
		placeNothing.setActionCommand("place");
		placeNothing.addActionListener(this);

		placeGroup = new ButtonGroup();
		placeGroup.add(placeCar);
		placeGroup.add(placeTrafficLight);
		placeGroup.add(placeNothing);
		placeCar.setSelected(true);
		
		// Set initial values
		setEnabled(false, stepButton, autoToggle, placeCar, placeTrafficLight, 
				placeNothing, placeRandom, slider, carLinger, redProb, redTime);
		
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
        	.addSeperator("Place")
        	.addMultiField(placeCar, placeTrafficLight, placeNothing)
        	.addSeperator("Parameters")
        	.addLabel("Car linger:")
        	.addLastField(carLinger)
        	.addLabel("Red probability/time:")
        	.addMultiField(redProb, redTime));
        
        controls.add(new SimpleForm()
	    	.addSeperator("Simulation")
	    	.addLastField(slider)
	    	.addMultiField(autoToggle, stepButton)
	    	.addSeperator("Statistics")
	    	.addLastField(statisticalData));
        
        add(controls, BorderLayout.SOUTH);
    }
    
    JScrollPane scroll;
    
    
    /**
     * Invoked when the user presses some button
     */
    public void actionPerformed(ActionEvent evt) {
    	String cmd = evt.getActionCommand();
    	
    	if (cmd.startsWith("place")) {
    		if (cmd.startsWith("placeCar"))
    			road.notifyPlaceModeChanged(Car.class);
    		else if (cmd.startsWith("placeLight"))
    			road.notifyPlaceModeChanged(TrafficLight.class);
    		else
    			road.notifyPlaceModeChanged(null);
    	}
    	
    	if (evt.getSource() == newButton) {
    		// Create a new model
    		road = new HighwaySimulation((int) length.getValue());
    		
    		// Add it to the view
    		scroll.getViewport().removeAll();
    		scroll.getViewport().add(road);
    		
    		// Enable editor components
    		setEnabled(true, stepButton, autoToggle, placeCar, placeTrafficLight, 
    				placeNothing, placeRandom, slider, carLinger, redProb, redTime);
    		
    		// De-select all place buttons
    		placeCar.setSelected(true);
    		road.notifyPlaceModeChanged(
    				placeCar.isSelected() ? Car.class :
    					(placeTrafficLight.isSelected() ? TrafficLight.class : null));
    		road.setCarLinger((double) carLinger.getValue());
    		road.setTrafficLightRedProb((double) redProb.getValue());
    		road.setTrafficLightRedTime((int) redTime.getValue());
    	}
    	
    	if ("step".equals(evt.getActionCommand())) {
    		road.step();
    		
    		// Set statistical data
    		statisticalData.setText(road.getGeneration() + ". gen, " +
    				road.getAverageVelocity() + " avg v");
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
    		setEnabled(!autoToggle.isSelected(), stepButton, placeCar, 
    				placeTrafficLight, placeNothing, placeRandom, slider, 
    				carLinger, redProb, redTime, road, newButton, length);
    		
    		// Manage caption
    		autoToggle.setText(autoToggle.isSelected() ? "Stop" : "Run");
    	}
    }
    
    public static final void setEnabled(boolean enabled, JComponent...components) {
    	for (JComponent component : components)
			component.setEnabled(enabled);
    }
    
}
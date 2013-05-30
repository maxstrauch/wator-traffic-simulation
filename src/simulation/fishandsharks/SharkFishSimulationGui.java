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
package simulation.fishandsharks;

import helpers.SimpleForm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import simulation.fishandsharks.Ocean.Cell;
import simulation.fishandsharks.Ocean.Fish;
import simulation.fishandsharks.Ocean.Shark;

public class SharkFishSimulationGui extends JPanel implements ActionListener {
 
	private static final long serialVersionUID = 1L;
	
    private JButton stepButton, newButton, placeRandom;
    
    private JLabel generationCnt, fishCnt, sharkCnt;

    private JSlider slider;
    
    private SharkFishModel gol;
    
    private Timer timer;
 
    public SharkFishSimulationGui() {
        super(new BorderLayout());
        initGui();
    }
    
    private JSpinner rows, cols, newbornFish, newbornShark;
    
    private JToggleButton autoToggle, placeFish, placeShark, placeNothing;
    
    private PopulationChart populationDiagram;
    
    private AgeDistributionChart popVar;
    
    private ButtonGroup placeGroup;
    
    private ChangeListener cycleListener = new ChangeListener() {
		
		@Override
		public void stateChanged(ChangeEvent e) {
			gol.notifyRecycleChanged((int) newbornFish.getValue(),
					(int) newbornShark.getValue());
		}
	};
    
    
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
		
		generationCnt = new JLabel("0");
		fishCnt = new JLabel("0");
		sharkCnt = new JLabel("0");
		
		slider = new JSlider(JSlider.HORIZONTAL, 2, 20, 2);
		slider.setMajorTickSpacing(2);
		slider.setSnapToTicks(true);
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		slider.setValue(2);
		
		rows = new JSpinner(new SpinnerNumberModel(42, 2, 120, 1));
		cols = new JSpinner(new SpinnerNumberModel(42, 2, 120, 1));
		
		newbornFish = new JSpinner(new SpinnerNumberModel(2, 1, 10, 1));
		newbornFish.addChangeListener(cycleListener);
		newbornShark = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
		newbornShark.addChangeListener(cycleListener);

		newButton = new JButton("Create new field");
		newButton.addActionListener(this);
		
		placeFish = new JToggleButton("Place fish");
		placeFish.setActionCommand("placeFish");
		placeFish.addActionListener(this);
		
		placeShark = new JToggleButton("Place shark");
		placeShark.setActionCommand("placeShark");
		placeShark.addActionListener(this);
		
		placeNothing = new JToggleButton("Place empty cell");
		placeNothing.setActionCommand("place");
		placeNothing.addActionListener(this);
		
		placeGroup = new ButtonGroup();
		placeGroup.add(placeFish);
		placeGroup.add(placeShark);
		placeGroup.add(placeNothing);
		placeFish.setSelected(true);
		
		populationDiagram = new PopulationChart(new Color[] {Cell.FISH, 
				Cell.SHARK, Cell.OCEAN_DARK});
		
		popVar = new AgeDistributionChart(new Color[] {Cell.FISH, Cell.SHARK});
		
		
		// Set initial values
		setEnabled(false, stepButton, autoToggle, placeFish, placeShark, 
				placeNothing, placeRandom, slider, newbornFish, newbornShark);
		
		// Lay-out components
        setLayout(new BorderLayout(4, 4));
        setBorder(new EmptyBorder(4, 4, 4, 4));
        
        scroll = new JScrollPane();
        scroll.setPreferredSize(new Dimension(600, 600));
        
        add(scroll, BorderLayout.CENTER);
        
        add(new SimpleForm()
        	.addSeperator("Creation")
        	.addMultiField(rows, new JLabel("x", SwingConstants.CENTER), cols)
    		.addLastField(newButton)
        	.addSeperator("Parameters")
        	.addMultiField(placeFish, placeShark)
        	.addLastField(placeNothing)
        	.addLastField(placeRandom)
        	.addLabel("# G for fish recycle:")
        	.addLastField(newbornFish)
        	.addLabel("# G for shark recycle:")
        	.addLastField(newbornShark)
        	.addSeperator("Simulation")
        	.addLastField(slider)
        	.addMultiField(autoToggle, stepButton)
        	.addSeperator("Statistics")
        	.addLabel("Year / G:")
        	.addLastField(generationCnt)
        	.addLabel("# of fishes:")
        	.addLastField(fishCnt)
        	.addLabel("# of sharks:")
        	.addLastField(sharkCnt)
        	.addLastField(populationDiagram)
        	.addLastField(popVar), BorderLayout.EAST);
    }
    
    JScrollPane scroll;
    
    
    /**
     * Invoked when the user presses some button
     */
    public void actionPerformed(ActionEvent evt) {
    	String cmd = evt.getActionCommand();
    	
    	if (cmd.startsWith("place")) {
    		if (cmd.startsWith("placeFish"))
    			gol.notifyPlaceModeChanged(Fish.class);
    		else if (cmd.startsWith("placeShark"))
    			gol.notifyPlaceModeChanged(Shark.class);
    		else
    			gol.notifyPlaceModeChanged(null);
    	}
    	
    	if (evt.getSource() == newButton) {
    		// Create a new model
    		gol = new SharkFishModel((int) rows.getValue(), 
    				(int) cols.getValue());
    		
    		// Add it to the view
    		scroll.getViewport().removeAll();
    		scroll.getViewport().add(gol);
    		
    		// Enable editor components
    		setEnabled(true, stepButton, autoToggle, placeFish, 
    				placeShark, placeNothing, slider, placeRandom, 
    				newbornFish, newbornShark);
    		
    		// De-select all place buttons
    		placeFish.setSelected(true);
    		gol.notifyPlaceModeChanged(Fish.class);
    	}

    	if ("step".equals(evt.getActionCommand())) {
    		// Perform a step
    		gol.step();
    		
    		// Update the statistical data
    		generationCnt.setText(String.valueOf(gol.getGeneration()));
    		
    		// Update statistical data
    		populationDiagram.addData(gol.getFishCount(), 
    				gol.getSharkCount(), gol.getEmptyCount());
    		fishCnt.setText(String.valueOf(gol.getFishCount()));
    		sharkCnt.setText(String.valueOf(gol.getSharkCount()));
    		
    		popVar.setData(gol.getAgeDistribution());
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
    		setEnabled(!autoToggle.isSelected(), stepButton, placeFish, 
    				placeRandom, placeShark, placeNothing, slider, 
    				newButton, cols, rows, gol, newbornFish, newbornShark);
    		
    		// Manage caption
    		autoToggle.setText(autoToggle.isSelected() ? "Stop" : "Run");
    	}
    	
    	if ("random".equals(cmd)) {
    		JSpinner fishes = new JSpinner(new SpinnerNumberModel(.1, 0.01, .5, .01));
    		JSpinner sharks = new JSpinner(new SpinnerNumberModel(.05, 0.01, .5, .01));
    		
    		// Ask the user for creation details
    		int retVal = JOptionPane.showConfirmDialog(this, new SimpleForm()
    				.addLastField(new JLabel("Fill the ocean randomly with"))
    				.addLabel("% of fishes:").addLastField(fishes)
    				.addLabel("% of sharks:").addLastField(sharks), 
    			"Random fill", JOptionPane.OK_CANCEL_OPTION);
    		
    		if (retVal == JOptionPane.OK_OPTION)
    			gol.fillOceanRandomly((double) fishes.getValue(), 
    					(double) sharks.getValue());
    	}
    }
    
    public static final void setEnabled(boolean enabled, JComponent...components) {
    	for (JComponent component : components)
			component.setEnabled(enabled);
    }
    
}
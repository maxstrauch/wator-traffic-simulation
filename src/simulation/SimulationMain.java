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
package simulation;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

import simulation.fishandsharks.SharkFishSimulationGui;
import simulation.traffic.TrafficSimulation;

public class SimulationMain {
	
    /**
     * Create the GUI and show it
     */
    private static void createAndShowGUI() {
        // Create and set up the window
        JFrame frame = new JFrame("Wa-Tor & traffic simulation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        // Create and set up the content pane
        JTabbedPane tab = new JTabbedPane();
        tab.addTab("Wa-Tor (Shark-fish-simulation)", 
        		new SharkFishSimulationGui());

        tab.addTab(TrafficSimulation.getName(), 
        		TrafficSimulation.getGUI());
       
        frame.setContentPane(tab);
 
        // Display the window
        frame.pack();
        frame.setVisible(true);
    }
 
    public static void main(String[] args) {
    	// Some LaF stuff ...
    	System.setProperty("com.apple.mrj.application." +
    			"apple.menu.about.name", "Wa-Tor & traffic simulation");
    	try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			// Ignore
		}
    	
        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
    
}

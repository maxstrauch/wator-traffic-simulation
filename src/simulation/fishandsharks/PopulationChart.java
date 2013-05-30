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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JComponent;

import simulation.fishandsharks.Ocean.Cell;

public class PopulationChart extends JComponent {

	private static final long serialVersionUID = 1L;

	private static final int WIDTH = 2;
	
	private Bar[] bars;
	private Color[] colors;
	
	private int i;
	
	public PopulationChart(Color[] colors) {
		
		this.colors = colors;
		
		setPreferredSize(new Dimension(200, 100));
		setMinimumSize(getPreferredSize());
		setMaximumSize(getPreferredSize());
		
		bars = new Bar[getPreferredSize().width/WIDTH];
		i = 0;
	}
	
	public synchronized void addData(int...values) {
		if (values.length != colors.length)
			throw new IllegalArgumentException();
		
		bars[i] = new Bar();
		bars[i].parts = values;
		bars[i].total = 0;
		for (int value : values)
			bars[i].total += value;
		
		
		i = (i+1)%bars.length;
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		int h = getHeight();

		g.setColor(Cell.OCEAN_LIGHT);
	 	g.fillRect(0, 0, getWidth(), h);
		
		for (int i = 0; i < bars.length; i++) {
			if (bars[i] == null)
				continue;
		
			int x0 = 0, h0;
			for (int j = 0; j < bars[i].parts.length; j++) {
				h0 = (int) Math.round(bars[i].relative(j)*h);
				g.setColor(colors[j]);
				g.fillRect(i*WIDTH, x0, WIDTH, h0);
				x0 += h0;
			}
		}

		// Draw scanner line and border
		g.setColor(Color.black);
		g.drawLine(i*WIDTH, 0, i*WIDTH, h);
		g.drawRect(0, 0, getWidth()-1, getHeight()-1);
	}
	
	
	private class Bar {
		
		int total;
		int[] parts;
		
		private float relative(int i) {
			return ((float) parts[i])/((float) total);
		}
		
	}
	
}

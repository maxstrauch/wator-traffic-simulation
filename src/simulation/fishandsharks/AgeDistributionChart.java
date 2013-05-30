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
import java.awt.Rectangle;
import java.util.Map;

import javax.swing.JComponent;

import simulation.fishandsharks.Ocean.Cell;

public class AgeDistributionChart extends JComponent {

	private static final long serialVersionUID = 1L;

	private static final int BORDER = 5;
	
	private Color[] colors;
	
	private Map<Integer, int[]> data;
	
	private int[] biggest;
	
	public AgeDistributionChart(Color[] colors) {
		setPreferredSize(new Dimension(201, 150));
		setMinimumSize(getPreferredSize());
		setMaximumSize(getPreferredSize());
		this.colors = colors;
	}
	
	public void setData(Map<Integer, int[]> data) {
		this.data = data;
		this.biggest = null;
		
		for (Integer key : data.keySet()) {
			int[] dataEntry = data.get(key);
			
			if (dataEntry.length != 2)
				throw new IllegalArgumentException();
			
			if (biggest == null) {
				biggest = dataEntry.clone();
			} else {
				
				for (int i = 0; i < dataEntry.length; i++) {
					if (dataEntry[i] > biggest[i]) {
						biggest[i] = dataEntry[i];
					}
				}
				
			}
			
		}
		
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		int w = getWidth(), h = getHeight();
		
		g.setColor(Cell.OCEAN_LIGHT);
	 	g.fillRect(0, 0, w, h);
		
		Rectangle[] dims = {
				new Rectangle(2*BORDER+1, BORDER, w-3*BORDER, (h-2*BORDER)/2),
				new Rectangle(0, 0, 0, 0)
		};
		dims[1] = new Rectangle(dims[0].x, dims[0].y+dims[0].height+1, 
				dims[0].width, -1);
		
		if (data != null) {
			int wWidth = (int) Math.round(Math.floor(((float) dims[0].width)/
					((float) data.size())));
			wWidth = wWidth < 1 ? 1 : wWidth;
			
			int x0 = 0, h0, cnt = 0;
			for (Integer age : data.keySet()) {
				int[] entry = data.get(age);
				
				if (cnt == data.size()-1) {
					wWidth = dims[0].width-x0;
					if (wWidth < 1)
						continue;
				}

				for (int i = 0; i < entry.length; i++) {
					g.setColor(colors[i]);
					h0 = Math.round((((float) entry[i])/((float) biggest[i]))*dims[0].height);
					g.fillRect(dims[0].x+x0, dims[i].height < 0 ? dims[i].y : 
						dims[i].y+dims[i].height-h0, wWidth, h0);
				}
				
				x0 += wWidth;
				cnt++;
			}
		}
		
		g.setColor(Color.black);
		g.drawLine(BORDER, BORDER, BORDER+5, BORDER);
		g.drawLine(BORDER, h-BORDER, BORDER+5, h-BORDER);
		g.drawLine(BORDER+5, BORDER, BORDER+5, h-BORDER);
		g.drawLine(BORDER, h/2, w-BORDER, h/2);
		g.drawRect(0, 0, w-1, h-1);
	}
	
}

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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;

import simulation.traffic.highway.HighwayCell;

public class MotorwaySimulation extends JComponent implements MouseListener {

	private static final long serialVersionUID = 1L;
	
	private static final int SIZE = 14, IS = 10, VB = 10, HB = 20, SB = 2, SBD = 2;
	
	public static final int FAST_LANE = 0, NORMAL_LANE = 1;
	
	private MotorwayCar[][] road;
	
	private int generation;
	
	private Dimension size, canvasSize;
	
	private Rectangle[] clickAngles;
	
	private double linger, averageVelocity;
	
	public MotorwaySimulation(int length) {
		road = new MotorwayCar[2][length];
		generation = 0;
		clickAngles = new Rectangle[length*2];
		for (int i = 0; i < clickAngles.length; i++)
			clickAngles[i] = new Rectangle();
		addMouseListener(this);
	}
	
	private MotorwayCar getCell(int lane, int p) {
		while (p < 0)
			p += length();
		return road[lane][p%length()];
	}
	
	private boolean isEmpty(int lane, int p) {
		return getCell(lane, p) == null;
	}
	
	private void setCell(int lane, int p, MotorwayCar c) {
		while (p < 0)
			p += length();
		road[lane][p%length()] = c;
	}

	private MotorwayCar elementOf(boolean prev, int lane, int pos) {
		int i = 0;
		MotorwayCar c;
		
		while (i < length()) {
			
			if ((c = getCell(lane, prev ? pos-1-i : pos+1+i)) != null) {
				return c;
			}
			
			i++;
		}
		
		return null;
	}

	private int indexOf(boolean prev, int lane, int pos) {
		int i = 0;
		while (i < length()) {
			if (getCell(lane, prev ? pos-1-i : pos+1+i) != null)
				return i;
			i++;
		}
		return Integer.MAX_VALUE;
	}

	public int length() {
		return road[0].length;
	}
	
	public int getGeneration() {
		return generation;
	}
	
	public int getLaneCount() {
		return road.length;
	}
	
	public double getAverageVelocity() {
		return Math.round(averageVelocity*100d)/100d;
	}
	
	public void setCarLinger(double linger) {
		this.linger = linger;
	}
	
	public void step() {
		MotorwayCar c;
		double tmpAvgV = 0, items = 0;
		
		// Update positions
		for (int i = 0; i < length(); i++) {
			c = getCell(FAST_LANE, i);
			if (c != null)
				c.update(this, i, FAST_LANE, generation);
			
			c = getCell(NORMAL_LANE, i);
			if (c != null)
				c.update(this, i, NORMAL_LANE, generation);
		}
		
		// Collect statistical data
		for (int i = 0; i < road.length; i++) {
			for (int j = 0; j < road[i].length; j++) {
				if (road[i][j] != null) {
					tmpAvgV += road[i][j].v;
					items++;
				}
			}
		}
		
		averageVelocity = (averageVelocity+(tmpAvgV/items))/2;
		
		repaint();
		generation++;
	}
	
	@Override
	public String toString() {
		String ln1 = "", ln2 = "";
		for (int i = 0; i < road[0].length; i++) {
			ln1 += road[0][i] == null ? "." : road[0][i];
			ln2 += road[1][i] == null ? "." : road[1][i];
		}
		return ln1 + "\n" + ln2;
	}

	public int calculateSize(int width, int height) {
		// Update size only on window resize
		if (canvasSize == null)
			canvasSize = new Dimension(width, height);
		else if (canvasSize.width == width && canvasSize.height == height)
			return (canvasSize.width-2*VB)/(SIZE+2);
		else
			canvasSize = new Dimension(width, height);
		
		int w = getWidth();
		int newH = (int) Math.ceil(((SIZE+2)*length()+2*VB)/(double) w);
		setSize(w, newH*SIZE+(newH+1)*HB);
		int h = getHeight();
		size = new Dimension(w, h);
		super.setSize(size);
		setPreferredSize(size);
		setMinimumSize(getPreferredSize());
		setMaximumSize(getPreferredSize());
		return (canvasSize.width-2*VB)/(SIZE+2);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		// Set the canvas size if not calculated yet
		int itemsPerRow = calculateSize(getWidth(), getHeight());
		int i = 0, y0 = HB, streetWidth, xo, yo, cc = 0;
		
		while (i < length()) {
			streetWidth = Math.min(itemsPerRow, length()-i)*(SIZE+2)+VB;
			
			// Draw the street
			g.setColor(Color.gray);
			g.fillRect(VB/2, y0-(SB+HB/4), streetWidth, 8+2*SIZE+HB/2+2*SB);
			
			// Draw the street side
			g.setColor(Color.white);
			g.fillRect(VB/2, y0-(SB+HB/4)+SBD, streetWidth, 2);
			
			// Draw the middle line
			xo = VB/2;
			yo = y0+SIZE+HB/4-SBD;
			while (xo <= streetWidth) {
				g.fillRect(xo, yo, IS, 2);
				xo += 2*IS;
			}
			
			g.fillRect(VB/2, y0+8+2*SIZE+HB/4-SBD, streetWidth, 2);
			
			for (int j = 0; j < itemsPerRow; j++) {
				// Draw the background
				g.setColor(Color.gray.brighter());
				
				clickAngles[cc].x = VB+j*(SIZE+2);
				clickAngles[cc].y = y0;
				clickAngles[cc].width = SIZE;
				clickAngles[cc].height = SIZE;
				g.fillRect(clickAngles[cc].x, clickAngles[cc].y,
						clickAngles[cc].width, clickAngles[cc].height);
				cc++;
				
				// Draw the cars
				MotorwayCar car = getCell(FAST_LANE, i);
				if (car != null) {
					xo = VB+j*(SIZE+2)+(SIZE-IS)/2;
					yo = y0+(SIZE-IS)/2;
					
					g.setColor(Color.blue);
					g.fillRect(xo, yo, IS, IS);
					
					g.setColor(Color.black);
					g.fillRect(xo, yo, (IS/5)*car.v, IS);
				}
				
				g.setColor(Color.gray.brighter());
				
				clickAngles[cc].x = VB+j*(SIZE+2);
				clickAngles[cc].y = y0+SIZE+8;
				clickAngles[cc].width = SIZE;
				clickAngles[cc].height = SIZE;
				g.fillRect(clickAngles[cc].x, clickAngles[cc].y,
						clickAngles[cc].width, clickAngles[cc].height);
				cc++;
				
				car = getCell(NORMAL_LANE, i);
				if (car != null) {
					xo = VB+j*(SIZE+2)+(SIZE-IS)/2;
					yo = y0+8+(SIZE-IS)/2+SIZE;
					
					g.setColor(Color.blue);
					g.fillRect(xo, yo, IS, IS);
					
					g.setColor(Color.black);
					g.fillRect(xo, yo, (IS/5)*car.v, IS);
				}
				
				i++;
				if (i >= length())
					break;
			}
			
			y0 += 2*SIZE + HB + HB/2;
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (!isEnabled())
			return;
		
		Point p = e.getPoint();
		for (int i = 0; i < clickAngles.length; i++) {
			if (clickAngles[i].contains(p)) {
				int lane = i%2, pos = i/2;
				MotorwayCar cell = getCell(lane, pos);
				
				if (cell == null) {
					// Place a new cell
					setCell(lane, pos, new MotorwayCar(0, generation));
					break;
				} else {
					cell.v = cell.v + 1;
					if (cell.v > MotorwayCar.MAX_V)
						setCell(lane, pos, null);
				}
				
			}
		}
		
		repaint();
	}

	@Override
	public void mousePressed(MouseEvent e) { }

	@Override
	public void mouseReleased(MouseEvent e) { }

	@Override
	public void mouseEntered(MouseEvent e) { }

	@Override
	public void mouseExited(MouseEvent e) { }
	
	public class MotorwayCar {
		
		public static final int MIN_V = 0, MAX_V = 5;
		
		private int v, gen;
		
		public MotorwayCar(int v, int gen) {
			this.v = v;
			this.gen = gen;
		}

		public void accelerate() {
			v = (v+1)%(MAX_V+1);
		}
		
		public void update(MotorwaySimulation road, int pos, int lane, int generation) {
			if (generation != gen)
				return;
			MotorwayCar c;
			int changeLane = lane;
			
			// Rule 1: speed up
			v = Math.min(v+1, MAX_V);
			
			// Rule 2: collision detection
			if (HighwayCell.decision(linger))
				v = Math.max(v-1, MIN_V);
			
			// Rule 3: Overtaking
			int distanceToNext = road.indexOf(false, lane, pos);
			if (distanceToNext < v) {
				c = road.elementOf(true, FAST_LANE, pos);
				
				if (
						lane == NORMAL_LANE && 
						road.isEmpty(FAST_LANE, pos) &&
						road.indexOf(false, FAST_LANE, pos) >= v &&
						road.indexOf(true, FAST_LANE, pos) >= (c == null ? 0 : c.v)
				) {
					changeLane = FAST_LANE;
				} else {
					v = Math.min(v, distanceToNext);
				}
			}
			
			// Rule 4: Slow down
			c = road.elementOf(true, NORMAL_LANE, pos);
			if (lane == changeLane && lane == FAST_LANE &&
					road.isEmpty(NORMAL_LANE, pos) &&
					road.indexOf(true, NORMAL_LANE, pos) >= (c == null ? 0 : c.v)
			) {
				changeLane = NORMAL_LANE;
				v = Math.min(v, road.indexOf(false, NORMAL_LANE, pos));
			}

			// Rule 5: execution (movement
			road.setCell(lane, pos, null);
			road.setCell(changeLane, pos+v, this);
			
			gen = generation+1;
		}
		
		public String toString() {
			return String.valueOf(v);
		}
		
	}
	
}

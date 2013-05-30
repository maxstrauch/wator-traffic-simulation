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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;

import simulation.traffic.highway.HighwayCell.Car;
import simulation.traffic.highway.HighwayCell.TrafficLight;

public class HighwaySimulation extends JComponent implements MouseListener {

	private static final long serialVersionUID = 1L;
	
	private static final int SIZE = 14, IS = 10, VB = 10, HB = 20, SB = 2, SBD = 2;
	
	private HighwayCell[][] road;
	
	private int generation;
	
	private Dimension size, canvasSize;
	
	private Rectangle[] clickAngles;
	
	private Class<? extends HighwayCell> newType;
	
	private double carLinger, redLightProb, averageVelocity;

	private int redTime;
	
	public HighwaySimulation(int length) {
		road = new HighwayCell[2][length];
		generation = 0;
		clickAngles = new Rectangle[length];
		for (int i = 0; i < clickAngles.length; i++)
			clickAngles[i] = new Rectangle();
		addMouseListener(this);
	}
	
	private HighwayCell getCell(int p) {
		return road[0][p%road[0].length];
	}
	
	private TrafficLight getTrafficLight(int p) {
		return (TrafficLight) road[1][p%road[0].length];
	}
	
	public void setCell(int p, HighwayCell c) {
		road[c instanceof Car ? 0 : 1][p%road[0].length] = c;
	}
	
	public int getGeneration() {
		return generation;
	}
	
	public double getAverageVelocity() {
		return Math.round(averageVelocity*100d)/100d;
	}
	
	public int getDistanceToNext(int cp) {
		int p = 0;
		HighwayCell c, trafficLight;
		
		while (p < road[0].length) {
			c = getCell((cp+1)+p);
			trafficLight = getTrafficLight((cp+1)+p);
			
			if (c != null || (trafficLight != null && trafficLight.isSolid()))
				break;
			
			p++;
		}
		
		return p;
	}
	
	public int length() {
		return road[0].length;
	}
	
	public void notifyPlaceModeChanged(Class<? extends HighwayCell> newType) {
		this.newType = newType;
	}
	
	public void setCarLinger(double carLinger) {
		this.carLinger = carLinger;
	}
	
	public void setTrafficLightRedProb(double redLightProb) {
		System.out.println("SET: " + redLightProb);
		this.redLightProb = redLightProb;
	}
	
	public void setTrafficLightRedTime(int redTime) {
		this.redTime = redTime;
	}
	
	public double getCarLinger() {
		return carLinger;
	}
	
	public double getTrafficLightRedProb() {
		System.out.println(redLightProb);
		return redLightProb;
	}
	
	public int getTrafficLightRedTime() {
		return redTime;
	}
	
	public void step() {
		HighwaySimulation tmp = new HighwaySimulation(length());
		tmp.carLinger = carLinger;
		tmp.redLightProb = redLightProb;
		tmp.redTime = redTime;
		double tmpAvgV = 0, items = 0;
		
		for (int i = 0; i < road[0].length; i++) {
			if (road[0][i] != null)
				road[0][i].update(tmp, i, getDistanceToNext(i), generation);
			
			if (road[1][i] != null)
				road[1][i].update(tmp, i, -1, generation);
		}
		
		// Create statistics
		for (int i = 0; i < length(); i++) {
			if (road[0][i] != null) {
				tmpAvgV += road[0][i].getVelocity();
				items++;
			}
		}
		
		averageVelocity = (averageVelocity+(tmpAvgV/items))/2;
		
		road = tmp.road;
		generation++;
		repaint();
	}
	
	@Override
	public String toString() {
		String ln1 = "", ln2 = "";
		for (int i = 0; i < road[0].length; i++) {
			ln1 += road[0][i] == null ? "." : road[0][i];
			ln2 += road[1][i] == null ? " " : road[1][i];
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
		int i = 0, y0 = HB, streetWidth, xo, yo;
		
		while (i < length()) {
			streetWidth = Math.min(itemsPerRow, length()-i)*(SIZE+2)+VB;
			
			// Draw the street
			g.setColor(Color.gray);
			g.fillRect(VB/2, y0-(SB+HB/4), streetWidth, SIZE+HB/2+2*SB);
			
			// Draw the street side
			g.setColor(Color.white);
			g.fillRect(VB/2, y0-(SB+HB/4)+SBD, streetWidth, 2);
			g.fillRect(VB/2, y0+SIZE+HB/4-SBD, streetWidth, 2);
			
			for (int j = 0; j < itemsPerRow; j++) {
				// Draw the traffic light
				TrafficLight tl = getTrafficLight(i);
				if (tl != null)
					g.setColor(tl.isSolid() ? Color.red : Color.green);
				else
					g.setColor(Color.gray.brighter());
				
				clickAngles[i].x = VB+j*(SIZE+2);
				clickAngles[i].y = y0;
				clickAngles[i].width = SIZE;
				clickAngles[i].height = SIZE;
				
				g.fillRect(clickAngles[i].x, clickAngles[i].y,
						clickAngles[i].width, clickAngles[i].height);
				
				// Draw the cars
				HighwayCell car = getCell(i);
				if (car != null) {
					xo = VB+j*(SIZE+2)+(SIZE-IS)/2;
					yo = y0+(SIZE-IS)/2;
					
					g.setColor(Color.blue);
					g.fillRect(xo, yo, IS, IS);
					
					g.setColor(Color.black);
					g.fillRect(xo, yo, (IS/5)*car.getVelocity(), IS);
				}
				
				i++;
				if (i >= length())
					break;
			}
			
			y0 += SIZE + HB;
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (!isEnabled())
			return;
		
		Point p = e.getPoint();
		for (int i = 0; i < clickAngles.length; i++) {
			if (clickAngles[i].contains(p)) {
				HighwayCell cell = getCell(i);
				
				if (cell == null) {
					// Place a new cell
					try {
						setCell(i, newType == null ? null : newType.newInstance());
					} catch (Exception e2) {
						System.out.println("Error creating new cell: " + e2);
					}
					break;
				}
				
				if (cell instanceof Car) {
					((Car) cell).accelerate();
					break;
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
	
}

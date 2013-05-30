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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import javax.swing.JComponent;

import simulation.fishandsharks.Ocean.Cell;
import simulation.fishandsharks.Ocean.Fish;
import simulation.fishandsharks.Ocean.Shark;

public class SharkFishModel extends JComponent implements MouseListener {

	private static final long serialVersionUID = 1L;
	
	private static final int BOX_SIZE = 10;

	private Ocean ocean;
	
	private int generation, fishCnt, sharkCnt, emptyCnt, 
		fishRebornCycle, sharkRebornCycle;

	private int x0, y0;
	
	private Class<? extends Cell> newType;
	
	private Map<Integer, int[]> ageDistribution;

	public SharkFishModel(int width, int height) {
		ocean = new Ocean(width, height);
		generation = fishCnt = sharkCnt = 0;
		emptyCnt = width*height;
		setPreferredSize(new Dimension(BOX_SIZE*width, BOX_SIZE*height));
		setMinimumSize(getPreferredSize());
		setMaximumSize(getPreferredSize());
		addMouseListener(this);
		
		fishRebornCycle = 2;
		sharkRebornCycle = 3;
		
		ageDistribution = new TreeMap<Integer, int[]>();
	}
	
	public int getGeneration() {
		return generation;
	}
	
	public int getFishCount() {
		return fishCnt;
	}
	
	public int getSharkCount() {
		return sharkCnt;
	}
	
	public int getEmptyCount() {
		return emptyCnt;
	}
	
	
	public void place(int x, int y, Cell c) {
		ocean.setField(x, y, c);
	}
	
	public Map<Integer, int[]> getAgeDistribution() {
		return ageDistribution;
	}

	public void notifyPlaceModeChanged(Class<? extends Cell> type) {
		this.newType = type;
	}
	
	public void notifyRecycleChanged(int fishCycle, int sharkCycle) {
		this.fishRebornCycle = fishCycle;
		this.sharkRebornCycle = sharkCycle;
	}
	
	public void fillOceanRandomly(double fishes, double sharks) {
		Class<? extends Cell> b4 = newType;
		Random r = new Random();
		int w = ocean.getWidth(), h = ocean.getHeight();
		
		// Create random fishes
		newType = Fish.class;
		for (int i = 0; i < Math.round(w * h * fishes); i++)
			ocean.setField(r.nextInt(w), r.nextInt(h), getNewCellInstance());

		// Create random sharks
		newType = Shark.class;
		for (int i = 0; i < Math.round(w * h * sharks); i++)
			ocean.setField(r.nextInt(w), r.nextInt(h), getNewCellInstance());
		
		// Repaint and reset everything
		newType = b4;
		repaint();
	}
	
	private Cell getNewCellInstance() {
		if (newType == null)
			return null;
		else
			try {
				Cell c = newType.newInstance();
				c.setGeneration(generation);
				return c;
			} catch (Exception e1) {
				System.out.println("Failed to place new cell: " + e1);
			}
		return null;
	}

	public void step() {
		long start = System.nanoTime();
		
		for (int y = 0; y < ocean.getHeight(); y++) {
			for (int x = 0; x < ocean.getWidth(); x++) {
				Cell c = ocean.getField(x, y);
				if (c != null && c.isPending(generation))
					c.update(ocean, x, y, generation, 
							fishRebornCycle, sharkRebornCycle);
			}
		}
		
		// Statistics
		emptyCnt = fishCnt = sharkCnt = 0;
		ageDistribution.clear();
		
		for (int y = 0; y < ocean.getHeight(); y++) {
			for (int x = 0; x < ocean.getWidth(); x++) {
				
				Cell c = ocean.getField(x, y);
				
				if (c == null)
					emptyCnt++;
				else if (c instanceof Fish)
					fishCnt++;
				else if (c instanceof Shark)
					sharkCnt++;
			
				if (c != null) {
					int[] spAge = ageDistribution.get(c.getAge());
					
					if (spAge == null)
						spAge = new int[2];
					
					if (c instanceof Fish)
						spAge[0]++;
					else 
						spAge[1]++;
					
					ageDistribution.put(c.getAge(), spAge);
					
				}
				
			}
		}
		
		generation++;
		
		start = System.nanoTime()-start;
		System.out.println("Step: " + start + " ns = " + (start*1e-9));
		
		repaint();
	}
	
	@Override
	public String toString() {
		return ocean.toString();
	}

	@Override
	public void paint(Graphics g) {
		// Set the base offset so that the ocean is displayed
		// in the middle of the view port
		x0 = (getWidth()-ocean.getWidth()*BOX_SIZE)/2;
		y0 = (getHeight()-ocean.getHeight()*BOX_SIZE)/2;
		x0 = x0 < 0 ? 0 : x0;
		y0 = y0 < 0 ? 0 : y0;
		
		// Paint the ocean
		g.setColor(Cell.OCEAN_DARK);
		g.fillRect(0, 0, getWidth(), getHeight());
		
		// Paint all cells
		for (int y = 0; y < ocean.getHeight(); y++) {
			for (int x = 0; x < ocean.getWidth(); x++) {
				Cell c = ocean.getField(x, y);
				
				if (c == null) {
					// The empty cell
					g.setColor(Cell.OCEAN_LIGHT);
					g.fillRect(x0+x*BOX_SIZE+2, y0+y*BOX_SIZE+2, 
							BOX_SIZE-4, BOX_SIZE-4);
				} else {
					// A fish or a shark
					g.setColor(c.getColor());
					
					if (c instanceof Shark && ((Shark) c).lifeIndex > 1)
						g.fillRect(x0+x*BOX_SIZE+1, y0+y*BOX_SIZE+1, 
								BOX_SIZE-2, BOX_SIZE-2);
					else
						g.fillRect(x0+x*BOX_SIZE+2, y0+y*BOX_SIZE+2, 
								BOX_SIZE-4, BOX_SIZE-4);
				}
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (!isEnabled())
			return;
		
		// Get coordinates on field
		int x = (e.getX()-x0)/BOX_SIZE, y = (e.getY()-y0)/BOX_SIZE;
		if (x < 0 || y < 0 || x >= ocean.getWidth() || y >= ocean.getHeight())
			return;
			
		// Place a new object and repaint
		place(x, y, getNewCellInstance());
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

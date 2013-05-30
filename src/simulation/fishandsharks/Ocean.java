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
import java.awt.Point;
import java.util.Arrays;


public class Ocean {

	public static final Point[] NEIGHBOUR_MASK = {
			new Point(0, -1), new Point(1, 0), new Point(0, 1), new Point(-1, 0)
	};
		
	private Cell[][] ocean;
	private int width, height;
	
	public Ocean(int width, int height) {
		if (width < 1 || height < 1)
			throw new IllegalArgumentException();
		
		this.ocean = new Cell[height][width];
		this.width = width;
		this.height = height;
		
		for (int j = 0; j < ocean.length; j++) {
			for (int i = 0; i < ocean[j].length; i++) {
				ocean[j][i] = null;
			}
		}
	}
	
	public Cell setField(int x, int y, Cell value) {
		return ocean[(height+(y%height))%height][(width+(x%width))%width] = value;
	}

	public Cell getField(int x, int y) {
		return ocean[(height+(y%height))%height][(width+(x%width))%width];
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		
		for (int j = 0; j < ocean.length; j++) {
			for (int i = 0; i < ocean[j].length; i++)
				buf.append(ocean[j][i] == null ? "." : ocean[j][i]);
			buf.append('\n');
		}
		
		return buf.toString();
	}

	public Point[] getFreeNeighbours(int x, int y) {
		Point[] freeCells = {};
		Cell cell;
		
		for (int i = 0; i < NEIGHBOUR_MASK.length; i++) {
			Point mask = NEIGHBOUR_MASK[i];
			cell = getField(x+mask.x, y+mask.y);
			if (cell == null) {
				freeCells = Arrays.copyOf(freeCells, freeCells.length+1);
				freeCells[freeCells.length-1] = new Point(x+mask.x, y+mask.y);
			}
		}
		
		return freeCells;
	}
	
	public Point[] getSharkNeighbours(int x, int y) {
		Point[] freeCells = {};
		Cell cell;
		
		for (int i = 0; i < NEIGHBOUR_MASK.length; i++) {
			Point mask = NEIGHBOUR_MASK[i];
			cell = getField(x+mask.x, y+mask.y);
			
			if (cell instanceof Shark) {
				freeCells = Arrays.copyOf(freeCells, freeCells.length+1);
				freeCells[freeCells.length-1] = new Point(x+mask.x, y+mask.y);
			}
		}
		
		return freeCells;
	}
	
	public Point[] getFishNeighbours(int x, int y) {
		Point[] freeCells = {};
		Cell cell;
		
		for (int i = 0; i < NEIGHBOUR_MASK.length; i++) {
			Point mask = NEIGHBOUR_MASK[i];
			cell = getField(x+mask.x, y+mask.y);
			
			if (cell instanceof Fish) {
				freeCells = Arrays.copyOf(freeCells, freeCells.length+1);
				freeCells[freeCells.length-1] = new Point(x+mask.x, y+mask.y);
			}
		}
		
		return freeCells;
	}
	
	public static abstract class Cell {
		
		public static final Color OCEAN_LIGHT = new Color(0xE5F9FF),
				OCEAN_DARK = new Color(0xB3ECFF),
				FISH = new Color(0x3BEB00),
				SHARK = new Color(0xFF571F);
		
		private int time, age;
		
		public Cell() {
			time = 0;
			age = 0;
		}
		
		public void update(Ocean o, int x, int y, int generation, 
				int fishCycle, int sharkCycle) {
			internalUpdate(o, x, y, generation, fishCycle, sharkCycle);
			time++;
			age++;
		}
		
		public void setGeneration(int time) {
			this.time = time;
		}
		
		public boolean isPending(int generation) {
			return generation == time;
		}
		
		public int getAge() {
			return age;
		}
		
		protected abstract void internalUpdate(Ocean o, int x, int y, 
				int generation, int fishCycle, int sharkCycle);
	
		public abstract Color getColor();
		
	}
	
	
	public static class Fish extends Cell {
		
		@Override
		protected void internalUpdate(Ocean o, int x, int y, int generation, 
				int fishCycle, int sharkCycle) {
			Point[] freeNeighbors;
			
			// Rule 1:
			// A fish moves randomly to a neighbor field if it's free and
			// he has regenerated from the last time
			freeNeighbors = o.getFreeNeighbours(x, y);
			if (freeNeighbors.length > 0 && generation%fishCycle == 0) {
				// Get the new cell
				Point newCell = getRandomly(freeNeighbors);
				
				// Move this object to the new position
				o.setField(x, y, null);
				x = newCell.x;
				y = newCell.y;
				o.setField(x, y, this);
			}
			
			// Rule 2:
			// On a neighbor field which is empty a new fish is born
			freeNeighbors = o.getFreeNeighbours(x, y);
			if (freeNeighbors.length > 0) {
				// Get the new cell
				Point newCell = getRandomly(freeNeighbors);
				
				// Create the new fish
				o.setField(newCell.x, newCell.y, new Fish()).time =
						super.time + 1;
			}
		}
		
		@Override
		public Color getColor() {
			return Cell.FISH;
		}
		
		@Override
		public String toString() {
			return "f";
		}
		
	}
	
	public static class Shark extends Cell {
		
		public int lifeIndex = 2;
		
		@Override
		protected void internalUpdate(Ocean o, int x, int y, int generation, 
				int fishCycle, int sharkCycle) {
			Point[] fishNeighbors;
			Point[] freeNeighbors;
			
			// Rule 1:
			// A shark eats all fishes in neighborhood
			fishNeighbors = o.getFishNeighbours(x, y);
			for (Point fish : fishNeighbors)
				o.setField(fish.x, fish.y, null);
			
			// Rule 2:
			// If the shark eats nothing he moves to a free cell
			freeNeighbors = o.getFreeNeighbours(x, y);
			if (fishNeighbors.length < 1 && freeNeighbors.length > 0) {
				// Get the new cell
				Point newCell = getRandomly(freeNeighbors);
				
				// Move this object to the new position
				o.setField(x, y, null);
				x = newCell.x;
				y = newCell.y;
				o.setField(x, y, this);
			}
			
			// Rule 3:
			// On a neighbor field which is empty a new shark is born after
			// regeneration
			freeNeighbors = o.getFreeNeighbours(x, y);
			if (freeNeighbors.length > 0 && generation%sharkCycle == 0) {
				// Get the new cell
				Point newCell = getRandomly(freeNeighbors);
				
				// Create the new fish
				o.setField(newCell.x, newCell.y, new Shark()).time =
						super.time + 1;
			}
			
			// Rule 4:
			// A shark dies if he has nothing to eat two times
			if (fishNeighbors.length < 1)
				lifeIndex--;
			if (lifeIndex < 1) {
				// Remove this shark
				o.setField(x, y, null);
			}
		}
		
		@Override
		public Color getColor() {
			return Cell.SHARK;
		}
		
		
		@Override
		public String toString() {
			return lifeIndex == 2 ? "S" : (lifeIndex == 1 ? "s" : "-");
		}
		
	}
	
	public static <T> T getRandomly(T[] arr) {
		int index = (int) Math.round(Math.random()*Math.random()*2*arr.length)%arr.length;
		return arr[index];
	}
	
}

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

public abstract class HighwayCell {

	public static final int MIN_V = 0, MAX_V = 5;
	
	public static final Color[] index = {
		Color.gray, Color.yellow, Color.orange, Color.red, Color.green, Color.black
	};
	
	public Color getVelocityColor() {
		return index[getVelocity()];
	}

	public abstract boolean isSolid();
	
	public abstract void update(HighwaySimulation road, int pos, int d, int generation);
	
	public abstract int getVelocity();
	
	public static class Car extends HighwayCell {
		
		private int v;
		
		public Car() {
			v = 0;
		}
		
		@Override
		public int getVelocity() {
			return v;
		}
		
		@Override
		public boolean isSolid() {
			return true;
		}

		public void accelerate() {
			v = (v+1)%(MAX_V+1);
		}
		
		@Override
		public void update(HighwaySimulation road, int pos, int d, int generation) {
			// Rule 1: speed up
			v = Math.min(v+1, MAX_V);
			
			// Rule 2: collision detection
			v = Math.min(v, d);
			
			// Rule 3: normal linger
			if (decision(road.getCarLinger()))
				v = Math.max(v-1, MIN_V);
			
			// Rule 4: move car
			road.setCell(pos+v, this);
		}
		
		@Override
		public String toString() {
			return String.valueOf(v);
		}
		
	}
	
	public static class TrafficLight extends HighwayCell {

		private boolean isGreen;
		
		private int eta;
		
		public TrafficLight() {
			this.isGreen = true;
		}
		
		@Override
		public int getVelocity() {
			return 0;
		}
		
		@Override
		public boolean isSolid() {
			return !isGreen;
		}

		@Override
		public void update(HighwaySimulation road, int pos, int d, int generation) {
			if (eta > 0)
				eta--;
			
			if (eta == 0) {
				eta = 0;
				isGreen = true;
			}
			
			System.out.println("TFL update");
			
			// Check if good to make red
			if (decision(road.getTrafficLightRedProb()) && eta == 0) {
				eta = road.getTrafficLightRedTime();
				isGreen = false;
			}
			
			// Set the traffic light on the same location
			road.setCell(pos, this);
		}
		
		@Override
		public String toString() {
			return isGreen ? "G" : "R";
		}

	}
	
	public static boolean decision(double probability) {
		return Math.random() <= probability;
	}
	
}

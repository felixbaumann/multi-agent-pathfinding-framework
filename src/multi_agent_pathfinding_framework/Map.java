/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package multi_agent_pathfinding_framework;

import java.util.HashSet;

/* Standard representation of a map consisting of directed edges,
 * possibly obstacles that can be displayed in the simulation
 * and possibly parking spots for agents. */
public class Map {

	/* Directed edges [x1, y1, x2, y2] pointing from (x1, y1) to (x2, y2).
	 * For an undirected edge, add two edges, one in each directions. */
	public HashSet<Edge> edges;
	
	/* Dimensions of the map [x, y].
	 * Holds [-1, -1] if not specified in the file. */
	public int[] dimensions;
	
	/* Obstacles described by their coordinates [x, y]. */
	public HashSet<Position> obstacles;
	
	/* Parking spots described by their coordinates [x, y]. */
	public HashSet<Position> parkingSpots;

	/* Creates a standard map from a yamlClassicMap loaded from a classic
	 * file. */
	public Map(YamlClassicMap yamlClassicMap) throws CorruptedFileException {
		
		this.edges = importEdges(yamlClassicMap.edges);
		
		this.dimensions = importDimensions(yamlClassicMap.dimensions());

		this.obstacles = importPositions(yamlClassicMap.obstacles());
	}
	
	/* Creates a standard map from a yamlDynamicMap loaded from a dynamic
	 * file. */
	public Map(YamlDynamicMap yamlDynamicMap) throws CorruptedFileException {
		
		this.edges = importEdges(yamlDynamicMap.edges);
		
		this.dimensions = importDimensions(yamlDynamicMap.dimensions());

		this.obstacles = importPositions(yamlDynamicMap.obstacles());
		
		this.parkingSpots = importPositions(yamlDynamicMap.parkingSpots());
	}
	
	
	/* Creates edge objects from a set of coordinate arrays. */
	private HashSet<Edge> importEdges(HashSet<int[]> edges)
			throws CorruptedFileException {
		
        HashSet<Edge> edgeSet = new HashSet<Edge>();
		
		for (int[] edge : edges) {
			
			if (edge.length != 4) { throw new CorruptedFileException(
					"One of the edges does not consist of 4 values"
					+ " [x1, y1, x2, y2]."); }
			
			edgeSet.add(new Edge(new Position(edge[0], edge[1]),
							     new Position(edge[2], edge[3])));

	    }
	    return edgeSet;
	}
	
	
	/* Returns given dimensions if the array size fits. */
	private int[] importDimensions(int[] dimensions)
			throws CorruptedFileException {
		
		if (dimensions.length == 2) {
			return dimensions;
		}
		else {
			throw new CorruptedFileException("The dimensions specified"
					+ " in the file are not two values (x, y).");
		}
	}
	
	
	/* Creates position objects from a set of coordinate arrays. */
	private HashSet<Position> importPositions(HashSet<int[]> positions)
			throws CorruptedFileException {
		
		HashSet<Position> positionSet = new HashSet<Position>();
		
		if (positions != null) {
			for (int[] position : positions) {
				
				if (position.length != 2) { throw new CorruptedFileException(
						"One of the obstacles or parking spots does not"
						+ "consist of 2 values [x, y]."); }
				
				positionSet.add(new Position(position[0], position[1]));
			}
		}
	
		return positionSet;
	}
	
	
	/* Returns a copy of this map with the same values for all fields.
	 * The returned map is identical to but independend from this one. */
	public Map deepCopy() {
		
		return new Map(edges, dimensions, obstacles, parkingSpots);
	}
	
	/* Constructor for deep copy only. */
	public Map(HashSet<Edge> edges,
			    int dimensions[],
			    HashSet<Position> obstacles,
			    HashSet<Position> parkingSpots) {
		
		this.edges = new HashSet<Edge>();
		
		for (Edge edge : edges) { this.edges.add(edge); }
		
		
		this.dimensions = new int[]{ dimensions[0], dimensions[1] };
    
		
		this.obstacles = new HashSet<Position>();
			
		for (Position position : obstacles) { this.obstacles.add(position); }
		    
		
		if (parkingSpots != null) {
			
		    this.parkingSpots = new HashSet<Position>();
			
		    for (Position position : parkingSpots) {
			
			    this.parkingSpots.add(position);
		    }
		}
	}
}

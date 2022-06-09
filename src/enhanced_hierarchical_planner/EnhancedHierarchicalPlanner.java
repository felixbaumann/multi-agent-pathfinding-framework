/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package enhanced_hierarchical_planner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.TimeoutException;

import multi_agent_pathfinding_framework.A_STAR;
import multi_agent_pathfinding_framework.Agent;
import multi_agent_pathfinding_framework.CommonPlan;
import multi_agent_pathfinding_framework.Edge;
import multi_agent_pathfinding_framework.Map;
import multi_agent_pathfinding_framework.Plan;
import multi_agent_pathfinding_framework.Position;
import multi_agent_pathfinding_framework.Scenario;
import multi_agent_pathfinding_framework.Timeout;

/* Hierarchical Multi-Agent Path Planner based on the one proposed by
 * 
 * Han Zhang, Mingze Yao, Ziang Liu, Jiaoyang Li, Lucas Terr,
 * Shao-Hung Chan, T. K. Satish Kumar and Sven Koenig in
 * "A Hierarchical Approach to Multi-Agent Path Finding"
 * in "Proceedings of the 4th ICAPS Workshop on Hierarchical Planning
 * (HPlan 2021)".
 */
public class EnhancedHierarchicalPlanner {
	
	private Map map;
	
	/* Map will be divided into regions. */
	private ArrayList<Region> regions;
	
	/* Graph with Regions as nodes, RegionEdges as edges. */
	private RegionContainer graph;
	
	/* Number of regions in a row. */
	private int horizontalCount;
	
	/* Number of regions in a column. */
	private int verticalCount;
	
	/* Number of positions in a region horizontally. */
	private int horizontalRegionSize;
	
	/* Number of positions in a region vertically. */
	private int verticalRegionSize;
	
	/* This map maps regions to the traversals that traverse the respective
	 * region. Conflicts can only occur among traversals of the same list. */
	private HashMap<Region, ArrayList<Traversal>> traversals;

	
	/* There's no need for creating a new HierarchicalPlanner for every
	 * scenario. Instead, call this initialization function every time a new
	 * scenario is supposed to be processed. Afterwards create a CommonPlan
	 * by calling hierarchicalPlanning(). */
	public void initialize(Scenario scenario) {
		
		this.map = scenario.map();
		this.regions = createRegions(map);
		this.map.edges = directRegionBorders(map);		
	}

	
	/* Divide the map into several rectangular regions.
	 * The regions are chosen such that the horizontal number of regions is
	 * about as large as the number of horizontal positions in a region
	 * (same for vertically).
	 * The last region may be smaller, respectively.
	 * 
	 * For instance, an 11 x 18 map is divided into 4 regions horizontally of
	 * sizes (3, 3, 3, 2) and into 5 regions vertically of sizes
	 * (4, 4, 4, 4, 2). */
	private ArrayList<Region> createRegions(Map map) {
					
		/* 1. Figure out the size and number of regions. */		
		computeRegionStats();
						
		/* 2. Create empty regions and a RegionGraph.*/		
		ArrayList<Region> regions = initializeRegions();
		
		this.graph = new RegionContainer(regions,
									 	 horizontalCount,
									 	 horizontalRegionSize,
									 	 verticalRegionSize);
						
		/* 3. Process each edge of the map and either 
		 *    add it to the respective regions internal edge set or
		 *    to the region graph. */
		insertEdges(regions);
				
		return regions;
	}
	
	
	/* Computes the number of regions both horizontally and vertically
	 * as well as the size of those regions. */
	private void computeRegionStats() {
		
		/* Number of regions. */
		horizontalCount
		    = (int) Math.ceil(Math.sqrt(map.dimensions[0]));
		
		verticalCount
		    = (int) Math.ceil(Math.sqrt(map.dimensions[1]));
		
		/* Size of a single region.
		 * Double-cast necessary since Java truncates an int/int-division. */
		horizontalRegionSize
		    = (int) Math.ceil((double) map.dimensions[0] / horizontalCount);
		
		verticalRegionSize
		    = (int) Math.ceil((double) map.dimensions[1] / verticalCount);		
	}
	
	
	/* Process all edges of the map. Each of them is either an internal edge 
	 * of a region or an edge between regions. In the latter case,
	 * create a RegionEdge for the RegionGraph. */
	private void insertEdges(ArrayList<Region> regions) {
		
		for (Edge edge : map.edges) {			
			
		    Region source = regions.get(regionIndex(edge.source));
			
			Region target = regions.get(regionIndex(edge.target));					
			
			/* Edge within a single region. */
			if (source.equals(target)) { source.addInternalEdge(edge); }
			
			/* Edge between two distinct regions. */
			else { graph.addBorderEdge(edge); }
		}		
	}
	
	
	/* This function prunes the edge set of the map according to the chosen
	 * distribution in regions. The hierarchical planer assumes that edges
	 * between regions are directed. However, when considering an undirected
	 * scenario, the issue comes up that regions are not defined before
	 * executing this planner while undirecting the scenario takes place way
	 * before that. Thus, this function will remove edges from the map that
	 * have been created during undirecting and now happen to lie on region
	 * borders. */
	private HashSet<Edge> directRegionBorders(Map map) {		
		
		/* Both edges on borders and within regions. */
		HashSet<Edge> mapEdges = new HashSet<Edge>();
		
		/* Only edges on borders. */
		HashSet<Edge> borderEdges = new HashSet<Edge>();
		
		/* Reconsider the validity of each edge on the map: */
		for (Edge edge : map.edges) {
	
			/* Edges within regions remain untouched. */
			if (!graph.isBorderEdge(edge)) { mapEdges.add(edge); }
			
			/* Edges on a border that have been created via copy are not
			 * supposed to exist. */
			else if (!edge.copy) {
				
				mapEdges.add(edge);
				borderEdges.add(edge);
			}				
		}		
		
		/* Update the container. */
		graph.borderEdges = borderEdges;
		
		return mapEdges;
	}
	
	
	/* Returns the index of the region the given position belongs to. 
	 * Region indices are given as 
	 *  -----------------------
	 * |      |      |     |   |
	 * |   8  |   9  |  10 |11 |
	 * |      |      |     |   |
	 *  -----------------------
	 * |      |      |     |   |
	 * |   4  |   5  |  6  | 7 |
	 * |      |      |     |   |
	 *  -----------------------
	 * |   0  |   1  |  2  | 3 |
	 *  -----------------------
	 *  
	 *  With the origin of the coordinate system being in the left lower
	 *  corner.
	 *  
	 *  If the position is out of bounds, the function will still return an
	 *  index. One that doesn't make much sense though.  
	 */
	public int regionIndex(Position position) {
		
		/* Find the horizontal region index h such that
		 * horizontalSize * h < position.x < horizontalSize * (h + 1).
		 * Remember that Java truncates the fractional part of the result
		 * of any int/int division. */
		int horizontalRegion = position.x / horizontalRegionSize;
		
		/* Find the vertical region index v such that
		 * verticalSize * v < position.y < verticalSize * (v + 1). */
		int verticalRegion = position.y / verticalRegionSize;
	 	
		return verticalRegion * horizontalCount + horizontalRegion;
	}
	
	
	/* Create a list of empty regions. */
	private ArrayList<Region> initializeRegions() {
		
		ArrayList<Region> regions = new ArrayList<Region>();		
		
        for (int verticalIndex = 0;
        	 verticalIndex < verticalCount;
        	 verticalIndex++) {
        	
        	for (int horizontalIndex = 0;
        		 horizontalIndex < horizontalCount;
        		 horizontalIndex++) {
			
        		int regionIndex
        		    = verticalIndex * horizontalCount + horizontalIndex;
        	
        		/* There's a separate coordinate system, containing each
        		 * region as a single position. This allows to compute
        		 * the Manhattan distance as heuristic for the high-level
        		 * search. */
        		Position highLevelPosition
        		    = new Position(horizontalIndex, verticalIndex);
        		
        		int minX = horizontalIndex * horizontalRegionSize;
        		int minY = verticalIndex * verticalRegionSize;
        		int maxX = (horizontalIndex + 1) * horizontalRegionSize - 1;
        		int maxY = (verticalIndex + 1) * verticalRegionSize - 1;
        
			    regions.add(new Region(regionIndex,
			    		               highLevelPosition,
			    		               minX, minY, maxX, maxY));
        	}
		}        
        return regions;
	}
	
	
	/* Given a list of high level plans, this function creates a hashmap which
	 * contains all the regions of the entire map as keys and the traversals
	 * of the respective region as values. This makes the traversals of a
	 * given region easily accessible. */
	private HashMap<Region, ArrayList<Traversal>> createRegionTraversalMap(
			ArrayList<EnhancedHighLevelPlan> plans) {
		
		HashMap<Region, ArrayList<Traversal>> map
		    = new HashMap<Region, ArrayList<Traversal>>();
		
		for (Region region : regions) {
			
			map.put(region, new ArrayList<Traversal>());
		}		
		
		for (EnhancedHighLevelPlan plan : plans) {
			
			for (Traversal traversal : plan.plan) {
														
				map.get(traversal.region).add(traversal);
			}			
		}		
		return map;
	}
 
	
	/* Main function that controls the actual hierarchical planning process.
	 */
	public CommonPlan enhancedHierarchicalPlanning(Scenario scenario,
												   int timeHorizon, 
												   long runtimeLimit) 
												   throws TimeoutException {
		
		/* Divides the given map into regions. */
		initialize(scenario);
		
		/* Cancel the process if it exceeds the runtime limit. */
		Timeout.checkForTimeout(runtimeLimit);
		
		/* First, find a high-level plan. That means a region sequence that
		 * leads each agent to its goal region. Since multiple agents can be
		 * in the same region, they usually don't block each other. Since it
		 * takes more than 1 time step to traverse a region, ignore the
		 * timesteps in this planning part. However, the high-level plan must
		 * not exceed the time horizon since then the low-level plan will
		 * necessarily do so as well.
		 * Eventually, this high-level plan contains for each agent an
		 * independent list of regions. */
		ArrayList<EnhancedHighLevelPlan> highLevelPlans
		    = computeHighLevelPlans(scenario, timeHorizon);	
			
		
		/* Scenario unsolvable. For at least one agent no highLevelPlan was
		 * found. */
		if (highLevelPlans == null) { return null; }
						
		/* This map lists all existing traversals for a given region. */
		this.traversals = createRegionTraversalMap(highLevelPlans);
		
        timeLoop:
		for (int time = 0; time <= makespan(highLevelPlans); time++) {			    
			
			while (true) {
				
			    /* Cancel the process if it exceeds the runtime limit. */
				Timeout.checkForTimeout(runtimeLimit);
				
				/* Find an arbitrary region in which a conflict occurs at the
				 * current time. */
				Region region = findConflictRegion(highLevelPlans, time);
				
				/* No more conflicts at the current time. */
				if (region == null) { continue timeLoop; }		
				
				/* Fix the conflict by using CBS on all traversals of the
				 * respective region starting at the current time.
				 * CBS will only change conflicting plans.  */
				boolean failure = fixConflictsInRegion(highLevelPlans,
						                               traversals.get(region),
						                               scenario.agents(),
						                               time,
						                               timeHorizon,
						                               runtimeLimit);				
				if (failure) { return null; }			
			}
		}
		
		/* Planning successful.
		 * 
		 * Combine the merged plans to a CommonPlan. */		
		CommonPlan commonPlan = new CommonPlan();		
		
		for (EnhancedHighLevelPlan highLevelPlan : highLevelPlans) {		
			
			Plan plan = highLevelPlan.lowLevelPlan;					
			
			if (plan == null) { return null; }			
            
			commonPlan.addPlan(plan);					
		}
		return commonPlan;
	}
	
	
	/* Run CBS to fix any conflicts in the region the given
	 * traversals belong to. Do not touch plans at times earlier
	 * than the start time given as 'time'. */
	private boolean fixConflictsInRegion(
			ArrayList<EnhancedHighLevelPlan> highLevelPlans,
			ArrayList<Traversal> traversals,
			Agent[] agents,
			int time,
			int timeHorizon,
			long runtimeLimit) throws TimeoutException {
		
		/* perform CBS to solve conflicts within the plans of the given
		 * traversals. */
		ConflictBasedSearch cbs = new ConflictBasedSearch();
		
		/* This CommonPlan contains one plan for each traversal,
		 * not for each agent. */
		CommonPlan localPlans  = cbs.conflictBasedSearch(
				traversals,				
				time,
				timeHorizon,
				runtimeLimit);
		
		/* Failure. */
		if (localPlans == null) { return true; };
		
		/* For each traversal of this region... */
		for (int index = 0; index < traversals.size(); index++) {			
			
			Traversal traversal = traversals.get(index);
			
			EnhancedHighLevelPlan highLevelPlan = traversal.highLevelPlan;
			
            /* Write the new plan back into the highLevelPlan. */
			highLevelPlan.insertPlan(traversal, localPlans.get(index));
				
			/* Write the new plan into the traversal. */
			traversal.setPlan(localPlans.get(index));
		}
		
		/* Success. */
		return false;
	}

	
	/* Find an arbitrary conflict that occurs at the current time and returns
	 * the region in which it takes place.
	 * This is linear in the number of agents. */
	private Region findConflictRegion(ArrayList<EnhancedHighLevelPlan> plans,
			                      int time) {
				
		/* Vertex conflicts. */
		HashSet<Position> positions = new HashSet<Position>();
		
		/* Edge conflicts. */
		HashSet<Edge> edges = new HashSet<Edge>();
		
		for (EnhancedHighLevelPlan plan : plans) {
			
			Position position = plan.lowLevelPlan.position(time, true);
			
			Position nextPos = plan.lowLevelPlan.position(time + 1, true);
			
			Edge edge = new Edge(position, nextPos);
			
			/* Check whether someone else already claimed this position or
			 * the edge in the opposite direction. */
			if (positions.contains(position)
				|| edges.contains(new Edge(nextPos, position))) {
												
				return regions.get(regionIndex(position));
			}
			
			/* Otherwise, claim them for the current plan. */
			positions.add(position);
			
			edges.add(edge);
		}			
		return null;				
	}
	
	
	/* Returns the length of the longest given HighLevelPlan.
	 * That is the makespan of the given (potential) solution. */
	private int makespan(ArrayList<EnhancedHighLevelPlan> plans) {
		
		int max = 0;
		
		for (EnhancedHighLevelPlan plan : plans) {
					
			max = Math.max(max, plan.lowLevelPlan.length());
		}
		return max;
	}
		
	
	/* Computes high level plans for all agents. */
	public ArrayList<EnhancedHighLevelPlan> computeHighLevelPlans (
			Scenario scenario, int timeHorizon) {
		
		ArrayList<EnhancedHighLevelPlan> highLevelPlans
		    = new ArrayList<EnhancedHighLevelPlan>();		
		
		for (Agent agent : scenario.agents()) {
			
			EnhancedHighLevelPlan plan = computeHighLevelPlan(
					scenario, agent); 
			
			/* For the current agent no HighLevelPlan could be found.
			 * The algorithm won't solve this scenario. Return null. */
			if (plan == null) { return null; }					
			
			highLevelPlans.add(plan);
		}				
		return highLevelPlans;
	}
	
	
	/* Finds a high level plan (region sequence) for the first target of the
	 * task of a single agent. */
	private EnhancedHighLevelPlan computeHighLevelPlan(Scenario scenario,
											   		   Agent agent) {
		
		/* Identify the agent's start region. */
		Position start = agent.position();

		/* Identify the agent's goal region. */
		Position goal = agent.task().targets()[0];				
		
		/* Positions sequence from start to goal, not including start. */
		ArrayList<Position> positions = new A_STAR().AStar(map, start, goal);
		
		if (positions == null) { return null; }
		
		positions.add(0, start);
		
		return new EnhancedHighLevelPlan(graph, agent, positions);
	}
}
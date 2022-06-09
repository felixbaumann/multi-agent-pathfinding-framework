/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package multi_agent_pathfinding_framework;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import cooperative_a_star.CA_STAR;
import token_passing.TokenPassing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/* General test class. */
public class _Test {

	@Test
	public void testAStar()
		throws IOException, CorruptedFileException, TimeoutException {
		
		/* * * Test case: Classic_01.yaml * * */
		
		Parameters parameters = new Parameters(MAPFAlgorithm.CA_STAR,
				Paths.get("testCases/Classic_01.yaml"),
				Paths.get("testCases/output.yaml"),
				100,
				300,
				100,
				0);
		
		FileReader fileReader = new FileReader(parameters);
		
		Scenario scenario = fileReader.processFile(0);
		
		testMinCostAStar(scenario,
						 new Position(0, 0),
						 new Position(4, 4), 10);
		
		long now = System.nanoTime();
		
		testCooperativeAStar(scenario, 20, 100, now + 60000000000L, 11);
		
		
		/* * * Test case: Classic_02.yaml * * */
		
		parameters = new Parameters(MAPFAlgorithm.CA_STAR,
				Paths.get("testCases/Classic_02.yaml"),
				Paths.get("testCases/output.yaml"),
				100,
				300,
				100,
				0);
		
		fileReader = new FileReader(parameters);
		
		scenario = fileReader.processFile(0);
		
		assertEquals(scenario.agents().length, 1);
		assertEquals(scenario.agents()[0].name(), "agent0");
		
		assertEquals(scenario.map().dimensions[0], 9);
		assertEquals(scenario.map().dimensions[1], 5);
		
		assertEquals(scenario.map().edges.size(), 32);
		assertEquals(scenario.map().obstacles.size(), 24);
		
		assertTrue(scenario.map().edges.contains(new Edge(
			new Position(3, 1),
			new Position(2, 1))));
		
		assertTrue(scenario.map().obstacles.contains(new Position(4, 4)));
		
		testMinCostAStar(scenario, new Position(1, 1), new Position(2, 1), 5);	
		testMinCostAStar(scenario, new Position(7, 3), new Position(7, 1), 6);
		testMinCostAStar(scenario, new Position(5, 2), new Position(1, 3), 7);		
		
		now = System.nanoTime();
		
		testCooperativeAStar(scenario, 20, 100, now + 60000000000L, 6);
	}

	/* Just check whether the token passing algorithm yields a non-empty
	 * common plan. Checking whether this is an actual conflict-free plan
	 * that solves the scenario is somewhat complicated. */
	@Test
	public void testTokenPassing() throws IOException,
	    CorruptedFileException, TimeoutException, DistanceTableException {
		
		for (int index = 1; index < 11; index++) {
		
			String indexString
			    = (index < 10) ? "0" + index : String.valueOf(index);
			
			System.out.print("\nNow testing Dynamic scenario #"
			    + indexString + "\n");
			
			Parameters parameters = new Parameters(MAPFAlgorithm.TokenPassing,
					Paths.get("testCases/Dynamic_" + indexString + ".yaml"),
					Paths.get("testCases/output.yaml"),
					400,
					300,
					1,
					0);
			
	        FileReader fileReader = new FileReader(parameters);
			
			Scenario scenario = fileReader.processFile(0);
			
			long now = System.nanoTime();
			
			TokenPassing tokenPassing
			    = new TokenPassing(scenario, now + 300000000000L);
			
			now = System.nanoTime();
			
			CommonPlan plan = tokenPassing.tokenPassing(
					parameters.timeHorizon(), now + 300000000000L);
			
			assertTrue(plan.planLength() > 0);
		}
	}
	
	public void testMinCostAStar(Scenario scenario,
								 Position start,
								 Position goal,
								 int expectedCost) {
		
		A_STAR star = new A_STAR();
		int cost = star.minCost(scenario.map(), start, goal);
		assertEquals(expectedCost, cost);
	}
	
	
	/* Use a lot of trials to make the risk for an assertion error even though
	 * the algorithm works fine very low. */
	public void testCooperativeAStar(Scenario scenario,
									 int timeHorizon,									 
									 int trialLimit,
									 long runtimeLimit,
									 int expectedPlanLength)
											 throws TimeoutException {
		
		CA_STAR cooperativeAStar = new CA_STAR();
		CommonPlan plan = cooperativeAStar.runCooperativeAStar(
				scenario, timeHorizon, trialLimit, runtimeLimit);
		
		assertEquals(expectedPlanLength, plan.planLength());
	}
	
	
	
	@Test
	public void testTrueDistances() {
		
		/* -------------------- Map 1, Target 1 ------------------- */
		HashSet<Edge> edges = new HashSet<Edge>();
		edges.add(new Edge(new Position(1, 0), new Position(0, 0)));
		edges.add(new Edge(new Position(1, 1), new Position(0, 1)));
		edges.add(new Edge(new Position(2, 1), new Position(1, 1)));
		edges.add(new Edge(new Position(0, 0), new Position(0, 1)));
		edges.add(new Edge(new Position(0, 2), new Position(0, 1)));
		edges.add(new Edge(new Position(1, 0), new Position(1, 1)));
		edges.add(new Edge(new Position(2, 1), new Position(2, 2)));
		
		int dimensions[] = {3, 3};
		
		Map map = new Map(edges, dimensions,
						  new HashSet<Position>(),
						  new HashSet<Position>());
		
		TrueDistances trueDistances = new TrueDistances();
		
		Position target = new Position(0, 1);
				
		HashMap<Edge, Integer> distances
		    = trueDistances.trueDistances(map, target);
		
		assertTrue(distances.get(new Edge(new Position(0, 1), target)) == 0);		
		assertTrue(distances.get(new Edge(new Position(0, 2), target)) == 1);
		assertTrue(distances.get(new Edge(new Position(0, 0), target)) == 1);
		assertTrue(distances.get(new Edge(new Position(1, 1), target)) == 1);
		assertTrue(distances.get(new Edge(new Position(1, 0), target)) == 2);
		assertTrue(distances.get(new Edge(new Position(2, 1), target)) == 2);
				
		assertFalse(distances.containsKey(
				new Edge(new Position(2, 2), target)));
		
		/* -------------------- Map 1, Target 2 ------------------- */
		
		target = new Position(1, 1);
		
		distances = trueDistances.trueDistances(map, target);
		
		assertTrue(distances.get(new Edge(new Position(1, 1), target)) == 0);		
		assertTrue(distances.get(new Edge(new Position(1, 0), target)) == 1);
		assertTrue(distances.get(new Edge(new Position(2, 1), target)) == 1);
		
		assertFalse(distances.containsKey(
				new Edge(new Position(2, 2), target)));
		
		assertFalse(distances.containsKey(
				new Edge(new Position(0, 0), target)));
		
		assertFalse(distances.containsKey(
				new Edge(new Position(0, 1), target)));
		
		assertFalse(distances.containsKey(
				new Edge(new Position(0, 2), target)));
		
		/* -------------------- Map 2, Target 1 ------------------- */
		
		edges = new HashSet<Edge>();
		edges.add(new Edge(new Position(0, 1), new Position(1, 1)));
		edges.add(new Edge(new Position(1, 1), new Position(0, 1)));
		edges.add(new Edge(new Position(1, 1), new Position(2, 1)));
		edges.add(new Edge(new Position(2, 0), new Position(1, 0)));
		edges.add(new Edge(new Position(1, 0), new Position(1, 1)));
		edges.add(new Edge(new Position(2, 1), new Position(2, 0)));
		
		dimensions[1] = 2;
		
		map = new Map(edges, dimensions,
					  new HashSet<Position>(),
					  new HashSet<Position>());
		
		target = new Position(0, 1);
		
		distances = trueDistances.trueDistances(map, target);
		
		assertTrue(distances.get(new Edge(new Position(0, 1), target)) == 0);
		assertTrue(distances.get(new Edge(new Position(1, 1), target)) == 1);
		assertTrue(distances.get(new Edge(new Position(1, 0), target)) == 2);
		assertTrue(distances.get(new Edge(new Position(2, 0), target)) == 3);
		assertTrue(distances.get(new Edge(new Position(2, 1), target)) == 4);
	}
}

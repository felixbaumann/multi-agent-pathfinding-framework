/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package runtime_replanner;

import org.junit.Test;

import multi_agent_pathfinding_framework.Agent;
import multi_agent_pathfinding_framework.AgentOrder;
import multi_agent_pathfinding_framework.CommonPlan;
import multi_agent_pathfinding_framework.CorruptedFileException;
import multi_agent_pathfinding_framework.FileReader;
import multi_agent_pathfinding_framework.ParameterReader;
import multi_agent_pathfinding_framework.Parameters;
import multi_agent_pathfinding_framework.Plan;
import multi_agent_pathfinding_framework.Position;
import multi_agent_pathfinding_framework.Scenario;
import multi_agent_pathfinding_framework.TimedPosition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertFalse;

/* General test class. */
public class _TestRuntimeReplanner {
	
	@Test
	public void testClaimContainer() {
		
		Agent agent1 = new Agent(new Position(0, 0));
		Agent agent2 = new Agent(new Position(1, 1));
		Agent agent3 = new Agent(new Position(2, 2));		
		Agent agents[] = {agent1, agent2, agent3};
		
		ClaimContainer container = new ClaimContainer(agents);		

		ClaimPosition claim1 = new ClaimPosition(new Position(1, 1));
		ClaimPosition claim2 = new ClaimPosition(new Position(2, 2));
		ClaimPosition claim3 = new ClaimPosition(new Position(3, 3));

		ClaimEdge claim4 = new ClaimEdge(new Position(4, 4),
										 new Position(4, 5));
		ClaimEdge claim5 = new ClaimEdge(new Position(5, 5),
										 new Position(5, 6));
		
		assertTrue(container.claims.size() == 0);
		assertFalse(container.contains(null));
		assertFalse(container.contains(claim1));				
		
		container.addClaims(agent1, claim1, null);				
		assertTrue(container.contains(claim1));
		assertTrue(container.claims.size() == 1);
		
		/* Replace a claim. */
		container.addClaims(agent1, claim2, claim4);
		
		assertTrue(container.contains(claim2));
		assertTrue(container.contains(claim4));
		assertFalse(container.contains(claim1));
		assertTrue(container.claims.size() == 2);
		
		/* Remove a claim. */
		container.addClaims(agent2, claim3, claim5);
		container.removeClaims(agent2);
		assertFalse(container.contains(claim5));
		assertTrue(container.claims.size() == 2);
		
		assertTrue(container.contains(new ClaimEdge(new Position(4, 4),
													new Position(4, 5))));
		assertFalse(container.contains(new ClaimEdge(new Position(5, 5),
													 new Position(5, 6))));
		
		container.addClaims(agents[2], null, null);
		assertFalse(container.contains(null));
	}
	

	@Test
	public void test() throws IOException, 
							  CorruptedFileException,
							  TimeoutException {
		
		/* Test a specific file. */
		String[] args1 = { "RuntimeReplanner",
				"D:/Bibliotheken/Masterstudium/Masterarbeit/testCases/JUnit"
				+ "/RuntimeReplanner/Classic_02.yaml",
				"D:/Bibliotheken/Masterstudium/Masterarbeit/testCases/JUnit"
				+ "/RuntimeReplanner/test_output.yaml",
				"100",
				"120",
				"1",
				"0"};
		
		long runtimeLimit = System.nanoTime() + 60000000000L;
		
		Parameters parameters = new ParameterReader().readParameters(args1);
		
		FileReader fileReader = new FileReader(parameters);
		
		AlternatingRuntimeReplanner planner
			= new AlternatingRuntimeReplanner();
		
		/* Classic_02.yaml */
		Scenario scenario = fileReader.processFile(0);
		
		testGetAlternatives(scenario);
		
		CommonPlan plans = testIndependentPlans(planner,
												scenario,
												parameters.timeHorizon(),
												true);
		
		AgentOrder order = new AgentOrder(scenario.agents());
		
		/* Since there's only a single agent in this example, it should follow
		 * its plan without any interruptions. */
		Position[] locations;
		
		for (TimedPosition timedPosition : plans.commonPlan().get(0).plan()) {
		    
			if (timedPosition.t == 0) continue;
			
			/* Do a step. */
			locations = testStep(planner, scenario, plans, order,
								 timedPosition.t - 1, runtimeLimit);
			
			/* Check whether this step led to the position proposed
			 * by the plan. */
			assertEquals(locations[0], timedPosition.position());						
		}
		
		
		/* Test a scenario where an agent has to wait. */
		String[] args2 = { "RuntimeReplanner",
				"D:/Bibliotheken/Masterstudium/Masterarbeit/testCases/JUnit"
				+ "/RuntimeReplanner/Classic_104_Simple.yaml",
				"D:/Bibliotheken/Masterstudium/Masterarbeit/testCases/JUnit"
				+ "/RuntimeReplanner/test_output.yaml",
				"10",
				"120",
				"1",
				"0"};
		
		runtimeLimit = System.nanoTime() + 60000000000L;
		
		parameters = new ParameterReader().readParameters(args2);
		
		fileReader = new FileReader(parameters);
		
		scenario = fileReader.processFile(0);
		
		plans = planner.independentPlans(scenario, parameters.timeHorizon(),
										 runtimeLimit);
		
		order = new AgentOrder(scenario.agents());

		/* Step 0 -> 1 */
		planner.attemptStep(scenario, plans, order, 0, runtimeLimit);
		
		assertEquals(plans.get(0).plan().get(1), new TimedPosition(1, 1, 1));
		assertEquals(plans.get(1).plan().get(1), new TimedPosition(0, 1, 1));
		
		/* Step 1 -> 2 */
		planner.attemptStep(scenario, plans, order, 1, runtimeLimit);
		
		assertEquals(plans.get(0).plan().get(2), new TimedPosition(1, 2, 2));
		assertEquals(plans.get(1).plan().get(2), new TimedPosition(1, 1, 2));
		
		/* Step 2 -> 3 */
		planner.attemptStep(scenario, plans, order, 2, runtimeLimit);
		
		assertEquals(plans.get(0).plan().get(3), new TimedPosition(1, 3, 3));
		assertEquals(plans.get(1).plan().get(3), new TimedPosition(1, 2, 3));


		/* Test a scenario where an agent has to step out of the way. */
		String[] args3 = { "RuntimeReplanner",
				"D:/Bibliotheken/Masterstudium/Masterarbeit/testCases/JUnit"
				+ "/RuntimeReplanner/Classic_105_Simple.yaml",
				"D:/Bibliotheken/Masterstudium/Masterarbeit/testCases/JUnit"
				+ "/RuntimeReplanner/test_output.yaml",
				"10",
				"120",
				"1",
				"0"};
		
		runtimeLimit = System.nanoTime() + 60000000000L;
		
		parameters = new ParameterReader().readParameters(args3);
		
		fileReader = new FileReader(parameters);
		
		scenario = fileReader.processFile(0);
		
		plans = planner.independentPlans(scenario, parameters.timeHorizon(),
										 runtimeLimit);
				
		order = new AgentOrder(scenario.agents());				
		
		/* Step 0 -> 1 */
		planner.attemptStep(scenario, plans, order, 0, runtimeLimit);
		assertEquals(plans.get(0).position(1, true), new Position(2, 1));	
		
		/* Agent1 has to step out of the way, instead of following its plan.
		 */
		assertEquals(plans.get(1).position(1, true), new Position(2, 0));
		
		/* Step 1 -> 2 */
		planner.attemptStep(scenario, plans, order, 1, runtimeLimit);		
		assertEquals(plans.get(0).position(2, true), new Position(2, 2));		
		assertEquals(plans.get(1).position(2, true), new Position(2, 1));
				
		/* Step 2 -> 3 */
		planner.attemptStep(scenario, plans, order, 2, runtimeLimit);		
		assertEquals(plans.get(0).position(3, true), new Position(2, 2));		
		assertEquals(plans.get(1).position(3, true), new Position(1, 1));
				
		/* Step 3 -> 4 */
		planner.attemptStep(scenario, plans, order, 3, runtimeLimit);		
		assertEquals(plans.get(0).position(4, true), new Position(2, 2));		
		assertEquals(plans.get(1).position(4, true), new Position(0, 1));
				
		/* Step 4 -> 5 */
		planner.attemptStep(scenario, plans, order, 4, runtimeLimit);		
		assertEquals(plans.get(0).position(5, true), new Position(2, 2));		
		assertEquals(plans.get(1).position(5, true), new Position(0, 0));


		/* Test all files in the directory. Order not guaranteed. */
		
		String[] newArgs = { "RuntimeReplanner",
			"D:/Bibliotheken/Masterstudium/Masterarbeit/testCases/JUnit"
			+ "/RuntimeReplanner/",
			"D:/Bibliotheken/Masterstudium/Masterarbeit/testCases/JUnit"
			+ "/RuntimeReplanner/test_output.yaml",
			"200",
			"120",
			"1",
			"0"};
		
		parameters = new ParameterReader().readParameters(newArgs);
		
		fileReader = new FileReader(parameters);				
		
		for (int index = 0; index < fileReader.fileCount(); index++) {
			
			scenario = fileReader.processFile(index);

			testIndependentPlans(planner, scenario, parameters.timeHorizon(),
								 true);
		}		
	}
	
	
	private void testGetAlternatives(Scenario scenario) {				
		
		Agent agent = scenario.agents()[0];
		
		ClaimContainer claims = new ClaimContainer(scenario.agents());
		
		Position position = agent.position();
		
		AlternatingRuntimeReplanner planner
			= new AlternatingRuntimeReplanner();
		
		ArrayList<Position> alternatives
		    = planner.getAlternatives(scenario, position, 0, claims, agent);
		
		/* Not moving keeps us at distance 1 from the goal. */
		assertTrue(alternatives.get(1).equals(position));
		
		/* The only possible step brings us to distance 2 from the goal. */
		assertTrue(alternatives.get(0).equals(new Position(1, 2)));				
		
		
		/* Different start position. */
		position = new Position(7, 2);
		
		alternatives
	        = planner.getAlternatives(scenario, position, 0, claims, agent);
		
		assertTrue(alternatives.get(0).equals(new Position(7, 1)));
		assertTrue(alternatives.get(1).equals(new Position(7, 3)));
		assertTrue(alternatives.get(2).equals(new Position(7, 2)));	
		
		/* Add a claim ruling out one of the alternatives. */
		claims.addClaims(agent, new ClaimPosition(new Position(7, 2)), null);
		
		alternatives
            = planner.getAlternatives(scenario, position, 0, claims, agent);
	
	    assertTrue(alternatives.get(0).equals(new Position(7, 1)));
	    assertTrue(alternatives.get(1).equals(new Position(7, 3)));
	    
	    
	    /* Replace the claim by an edge claim ruling out a different
	     * alternative. */
	    claims.removeClaims(agent);
	    claims.addClaims(agent, new ClaimPosition(new Position(9, 9)),
	    	new ClaimEdge(new Position(7, 1), new Position(7, 2)));
	    
	    alternatives
            = planner.getAlternatives(scenario, position, 0, claims, agent);
	    	   
		assertTrue(alternatives.get(0).equals(new Position(7, 3)));
		assertTrue(alternatives.get(1).equals(new Position(7, 2)));
	}
	
	
	private CommonPlan testIndependentPlans(
				AlternatingRuntimeReplanner planner,
				Scenario scenario,
				int timeHorizon,
				boolean solvable) throws TimeoutException {				
		
		long runtimeLimit = System.nanoTime() + 60000000000L;
		
		CommonPlan plans
			= planner.independentPlans(scenario, timeHorizon, runtimeLimit);
		
		/* The common plan is supposed to be null iff the instance is
		 * unsolvable. */
		assertTrue(solvable ^ (plans == null));
		
		if (plans != null) {
		    
			/* Makespan should not exceed the time horizon. */
			assertTrue(plans.planLength() <= timeHorizon);
			
			/* Each plan should start at the respective agent's start
			 * position. */
			for (Plan plan : plans.commonPlan()) {
				
				assertEquals(plan.plan().get(0).position(),
						     plan.agent.position());
			}
			
			int makespan = plans.planLength();
					
			/* Since the scenario was solved, all agents should be at their
			 * goals at the end of the common plan. */
			assertTrue(planner.allGoalsReached(plans, makespan));
			
			/* There should not be any unnecessary time steps at the end of
			 * the plan. Meaning, one time step before the end of the common
			 * plan not all agents may have already reached their goal. */
			if (makespan > 1) {
				
				assertFalse(planner.allGoalsReached(plans, makespan - 2));
			}
		}		
		return plans;
	}


	/* Returns the positions of all agents after the step starting at the
	 * given time in the given scenario following the given plans. 
	 * 
	 * 'time' is the moment after which the step takes place. So the function
	 * will return the positions at 'time + 1' */
    private Position[] testStep(AlternatingRuntimeReplanner planner,
    							Scenario scenario,
    							CommonPlan plans,
    							AgentOrder order, 
    							int time, 
    							long runtimeLimit) throws TimeoutException {
    	
    	Position locations[] = new Position[scenario.agents().length];
        
        for (int index = 0; index < locations.length; index++) {
        	
        	locations[index] = null;
        }

		ClaimContainer claims = new ClaimContainer(scenario.agents());
		
		planner.step(scenario, plans, order, time, 0, claims, locations,
					 runtimeLimit);
		
		return locations;
    }
    
    
    @Test
    public void testRuntimeReplanner() throws IOException,
    										  CorruptedFileException,
    										  TimeoutException {
    	
    	/* Test a specific file. */
		String[] args1 = { "RuntimeReplanner",
				"D:/Bibliotheken/Masterstudium/Masterarbeit/testCases/JUnit"
				+ "/RuntimeReplanner/Classic_105_Simple.yaml",
				"D:/Bibliotheken/Masterstudium/Masterarbeit/testCases/JUnit"
				+ "/RuntimeReplanner/test_output.yaml",
				"100",
				"120",
				"1",
				"0"};
		
		Parameters parameters = new ParameterReader().readParameters(args1);
		
		FileReader fileReader = new FileReader(parameters);
		
		Scenario scenario = fileReader.processFile(0);		
		
		AlternatingRuntimeReplanner planner
			= new AlternatingRuntimeReplanner();
		
		long runtimeLimit = System.nanoTime() + 60000000000L;
		
		CommonPlan plans 
			= planner.alternatingRuntimeReplanner(scenario,
			  								      parameters.timeHorizon(),
			  								      1,
			  								      runtimeLimit);
		
		assertEquals(plans.commonPlan().get(0).plan().get(0),
				     new TimedPosition(1, 1, 0));
		
		assertEquals(plans.commonPlan().get(0).plan().get(1), 
					 new TimedPosition(2, 1, 1));
		
		assertEquals(plans.commonPlan().get(0).plan().get(2), 
					 new TimedPosition(2, 2, 2));
		
		assertEquals(plans.commonPlan().get(1).plan().get(0),
					 new TimedPosition(2, 1, 0));
		
		assertEquals(plans.commonPlan().get(1).plan().get(1), 
				 	 new TimedPosition(2, 0, 1));
		
		assertEquals(plans.commonPlan().get(1).plan().get(2), 
					 new TimedPosition(2, 1, 2));
		
		assertEquals(plans.commonPlan().get(1).plan().get(3), 
					 new TimedPosition(1, 1, 3));
		
		assertEquals(plans.commonPlan().get(1).plan().get(4), 
					 new TimedPosition(0, 1, 4));
		
		assertEquals(plans.commonPlan().get(1).plan().get(5), 
					 new TimedPosition(0, 0, 5));
		
		
		/* Test a specific file. */
		String[] args2 = { "RuntimeReplanner",
				"D:/Bibliotheken/Masterstudium/Masterarbeit/testCases/JUnit"
				+ "/RuntimeReplanner/Classic_106_Simple.yaml",
				"D:/Bibliotheken/Masterstudium/Masterarbeit/testCases/JUnit"
				+ "/RuntimeReplanner/test_output.yaml",
				"100",
				"120",
				"1",
				"0"};
		
		parameters = new ParameterReader().readParameters(args2);
		
		fileReader = new FileReader(parameters);
		
		scenario = fileReader.processFile(0);					
		
		runtimeLimit = System.nanoTime() + 60000000000L;
		
		plans = planner.alternatingRuntimeReplanner(scenario,
													parameters.timeHorizon(),
													1, 
													runtimeLimit);
    	
		assertEquals(plans.commonPlan().get(0).plan().get(0), 
					 new TimedPosition(1, 3, 0));
		
		assertEquals(plans.commonPlan().get(0).plan().get(1), 
					 new TimedPosition(2, 3, 1));
		
		assertEquals(plans.commonPlan().get(0).plan().get(2), 
					 new TimedPosition(3, 3, 2));
		
		
		assertEquals(plans.commonPlan().get(1).plan().get(0), 
					 new TimedPosition(2, 1, 0));
		
		assertEquals(plans.commonPlan().get(1).plan().get(1), 
					 new TimedPosition(1, 1, 1));
		
		assertEquals(plans.commonPlan().get(1).plan().get(2), 
					 new TimedPosition(2, 1, 2));
		
		assertEquals(plans.commonPlan().get(1).plan().get(3), 
					 new TimedPosition(2, 2, 3));
		
		assertEquals(plans.commonPlan().get(1).plan().get(4), 
					 new TimedPosition(2, 3, 4));
		
		assertEquals(plans.commonPlan().get(1).plan().get(5), 
					 new TimedPosition(1, 3, 5));
		
		assertEquals(plans.commonPlan().get(1).plan().get(6), 
					 new TimedPosition(0, 3, 6));
		
		assertEquals(plans.commonPlan().get(1).plan().get(7), 
					 new TimedPosition(0, 2, 7));
		
		
		assertEquals(plans.commonPlan().get(2).plan().get(0), 
					 new TimedPosition(2, 2, 0));
		
		assertEquals(plans.commonPlan().get(2).plan().get(1), 
					 new TimedPosition(2, 1, 1));
		
		assertEquals(plans.commonPlan().get(2).plan().get(2), 
					 new TimedPosition(2, 0, 2));
		
		assertEquals(plans.commonPlan().get(2).plan().get(3), 
					 new TimedPosition(3, 0, 3));
    }
}
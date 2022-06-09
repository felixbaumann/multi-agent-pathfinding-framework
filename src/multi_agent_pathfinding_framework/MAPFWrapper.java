/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package multi_agent_pathfinding_framework;

import cooperative_a_star.CA_STAR;
import enhanced_hierarchical_planner.EnhancedHierarchicalPlanner;
import runtime_replanner.AlternatingRuntimeReplanner;
import runtime_replanner.RuntimeReplanner;
import token_passing.TokenPassing;
import traffic_simulator.TrafficSimulator;

import java.util.concurrent.TimeoutException;

/* This class decides which multi-agent pathfinding algorithm to run on
 * the given scenario. */
public class MAPFWrapper {		
	
	/* Tries to solve a given multi-agent pathfining scenario using
	 * the algorithm specified in the parameters. */
	public CommonPlan mapf(Scenario scenario, Parameters parameters,
			               long timeout) throws TimeoutException,
												DistanceTableException {
		
		/* Identify the chosen MAPF algorithm. */
		MAPFAlgorithm algorithm = parameters.algorithm();
		
		/* Run cooperative A* on the scenario and return the common plan.
		 * Might be null. */
		if (algorithm.equals(MAPFAlgorithm.CA_STAR)) {
			
			CA_STAR ca_star = new CA_STAR();
			
			return ca_star.runCooperativeAStar(scenario,
					parameters.timeHorizon(), parameters.trialLimit(),
					timeout);
		}
		
		/* Run regular token passing (without task swaps) and return the
		 * common plan. Might be null. */
		else if (algorithm.equals(MAPFAlgorithm.TokenPassing)) {
		
			TokenPassing tokenPassing = new TokenPassing(scenario, timeout);
			
			return tokenPassing.tokenPassing(parameters.timeHorizon(),
					                         timeout);
		}
		
		/* Run enhanced hierarchical multi-agent pathfinding and return the
		 * common plan. Might be null. */
		else if (algorithm.equals(
				MAPFAlgorithm.EnhancedHierarchicalPlanner)) {
			
			return new EnhancedHierarchicalPlanner()
					.enhancedHierarchicalPlanning(scenario,
				parameters.timeHorizon(), timeout);
		}
		
		/* Run a simple runtime replanner and return the common plan.
		 * Might be null. */
		else if (algorithm.equals(MAPFAlgorithm.RuntimeReplanner)) {
						
			return new RuntimeReplanner().runtimeReplanner(scenario,
					parameters.timeHorizon(), parameters.trialLimit(),
					timeout);
		}
		
		/* Run a runtime replanner that supports dynamic graphs and return
		 * the common plan. Might be null. */
		else if (algorithm.equals(
				MAPFAlgorithm.AlternatingRuntimeReplanner)) {
			
			return new AlternatingRuntimeReplanner()
					.alternatingRuntimeReplanner(scenario,
					parameters.timeHorizon(), parameters.trialLimit(),
					timeout);
		}
		
		/* Run a traffic simulator. */		
		else if (algorithm.equals(MAPFAlgorithm.TrafficSimulator)) {
			
			return new TrafficSimulator().trafficSimulation(scenario,
					   parameters.timeHorizon(), timeout);
		}
		
		return new CommonPlan();
	}
}

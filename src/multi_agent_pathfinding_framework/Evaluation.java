/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package multi_agent_pathfinding_framework;

import java.util.HashSet;
import java.util.concurrent.TimeoutException;

/* This class wraps the entire pathfinding process which allows to time it and
 * furthermore evaluate the plan quality. 
 * It requires a scenario as well as parameters specifying the pathfinding
 * algorithm.
 * In case the planning is successful, it offers the common plan as well as
 * some quality measures including the planning time. */
public class Evaluation {
	
	/* The plans computed by the pathfinding algorithm specified in the
	 * parameters on the (un)directed static graph as well as the dynamic
	 * graph. May be null in case the planning is unsuccessful. */
	private CommonPlan commonPlanUndirected = null;
	private CommonPlan commonPlanDirected = null;
	private CommonPlan commonPlanDynamic = null;
	
	/* Describes how long the pathfinding algorithm ran on the directed,
	 * the undirected and on the dynamic graph, respectively. */
	private long planningTimeUndirected = -1;
	private long planningTimeDirected = -1;
	private long planningTimeDynamic = -1;
	
	/* Makespan is the length of the longest individual plan of the common
	 * plan. In context of MAPD problems, it's the point in time when the last
	 * package is delivered. */
	private int makespanUndirected = -1;
	private int makespanDirected = -1;
	private int makespanDynamic = -1;
	
	/* Flowtime is the sum of costs, i.e. the sum of the individual plan
	 * lengths of the common plan. */
	private int flowtimeUndirected = -1;
	private int flowtimeDirected = -1;
	private int flowtimeDynamic = -1;
	
	/* Average over all tasks: time span between the task coming up and the
	 * task being completed.
	 * In regular MAPF settings this is the average plan length. */
	private int serviceTimeUndirected = -1;
	private int serviceTimeDirected = -1;	
	private int serviceTimeDynamic = -1;
	
	
	public Evaluation(Scenario directedScenario, Parameters parameters)
			throws InvalidPlanException, DistanceTableException {				

		/* Static graph with bidirectional edges. */
		Scenario undirectedScenario
		    = createUndirectedScenario(directedScenario, 0);
		
		/* Dynamic graph with unidirectional edges. */
		Scenario dynamicScenario
		    = createUndirectedScenario(directedScenario,
		    		parameters.directionChangeFrequency());
		
		/* Runs a Multi-Agent Pathfinding algorithm according to the choice in
		 * the parameters. */
		MAPFWrapper mapfWrapper = new MAPFWrapper();
		
		/* Used to validate the resulting common plans. That means, check
		 * that the plans don't contain conflicts, jumps in time or space,
		 * respect obstacles and start and end in the locations specified by
		 * the scenario. */
		Validator validator = new Validator();
		
		/* Used to time the process. */
		long timeStart;
		
		/* Moment when the process is cancelled in case it has not been
		 * successful already. */
		long timeout;
		
		/* UNDIRECTED SCENARIO. */		
		
		timeStart = System.nanoTime();
		timeout = timeStart + (((long) parameters.runtimeLimit()) * 1000000000);
		
		
		try {
			
			commonPlanUndirected = mapfWrapper.mapf(undirectedScenario, parameters, timeout);
		
		}
		/* Plan is initially null anyway which is desired in case of
		 * a timeout or memory error. */
		catch (TimeoutException exception) {}
		catch (java.lang.OutOfMemoryError error) {}
		
		
		planningTimeUndirected = System.nanoTime() - timeStart;

		if (commonPlanUndirected != null) {
			
			makespanUndirected = commonPlanUndirected.planLength();
			
			flowtimeUndirected = commonPlanUndirected.sumOfCosts();

			if (classicScenario(parameters.algorithm())) {
			
				/* In a classic scenario the service time is simply
				 * the average plan length. */
				serviceTimeUndirected
				    = flowtimeUndirected / undirectedScenario.agents().length;
				
			    /* In case of an invalid plan throws an exception that
			     * specifies the issue. */
			    validator.validateClassicScenario(undirectedScenario,
											      commonPlanUndirected);
			}
			
			else if (dynamicScenario(parameters.algorithm())) {
				
				serviceTimeUndirected
				    = computeServiceTime(undirectedScenario);
				
				/* In case of an invalid plan throws an exception that
				 * specifies the issue. */
			    validator.validateDynamicScenario(undirectedScenario,
											      commonPlanUndirected);
			}
		}
		
				
		/* DIRECTED SCENARIO. */
		
		timeStart = System.nanoTime();
		timeout = timeStart + (((long) parameters.runtimeLimit()) * 1000000000);
		
		try {
			
		    commonPlanDirected = mapfWrapper.mapf(directedScenario,
		    			                          parameters, timeout);   
	    }
		/* Plan is initially null anyway which is desired in case of
		 * a timeout or memory error. */
		catch (TimeoutException exception) {}
		catch (java.lang.OutOfMemoryError error) {}
		catch (java.lang.StackOverflowError error) {}
	
		planningTimeDirected = System.nanoTime() - timeStart;

		if (commonPlanDirected != null) {
			
            makespanDirected = commonPlanDirected.planLength();
			
			flowtimeDirected = commonPlanDirected.sumOfCosts();
			
            if (classicScenario(parameters.algorithm())) {
				
            	/* In a classic scenario the service time is simply
				 * the average plan length. */
				serviceTimeDirected
				    = flowtimeDirected / directedScenario.agents().length;
				
            	/* In case of an invalid plan throws an exception that 
				 * specifies the issue. */
				validator.validateClassicScenario(directedScenario,
												  commonPlanDirected);
			}
            
            else if (dynamicScenario(parameters.algorithm())) {
				
            	serviceTimeDirected
			        = computeServiceTime(directedScenario);
            	
				/* In case of an invalid plan throws an exception that specifies
			     * the issue. */
			    validator.validateDynamicScenario(directedScenario,
											      commonPlanDirected);
			}
		}

		
		/* DYNAMIC SCENARIO. */
		
		/* Parameters specify that no dynamic scenario is wanted. */
		if (parameters.directionChangeFrequency() == 0) return;
		
		/* CA* and the AlternatingRuntimeReplanner are the only algorithms
		 * up to now that support dynamic graphs. */
		if (! (parameters.algorithm().equals(MAPFAlgorithm.CA_STAR)
			|| parameters.algorithm().equals(
					MAPFAlgorithm.AlternatingRuntimeReplanner))) { return; }
		
		timeStart = System.nanoTime();
		timeout = timeStart + (((long) parameters.runtimeLimit()) * 1000000000);
		
		try {

			commonPlanDynamic = mapfWrapper.mapf(dynamicScenario,
												 parameters, timeout);
		}		
	
		/* Plan is initially null anyway which is desired in case of
		 * a timeout or memory error. */
		catch (TimeoutException exception) {}
		catch (java.lang.OutOfMemoryError error) {}
		catch (java.lang.NullPointerException exception) {}
		catch (java.lang.ArrayIndexOutOfBoundsException exception) {}
		
		planningTimeDynamic = System.nanoTime() - timeStart;

		if (commonPlanDynamic != null) {
			
            makespanDynamic = commonPlanDynamic.planLength();
			
			flowtimeDynamic = commonPlanDynamic.sumOfCosts();

			if (classicScenario(parameters.algorithm())) {
				
				/* In a classic scenario the service time is simply
				 * the average plan length. */
				serviceTimeDynamic
				    = flowtimeDynamic / dynamicScenario.agents().length; 
				
				/* In case of an invalid plan throws an exception that 
				 * specifies the issue. */
				validator.validateClassicScenario(dynamicScenario,
												  commonPlanDynamic);
			}
			
            else if (dynamicScenario(parameters.algorithm())) {
				
            	serviceTimeDynamic
			        = computeServiceTime(dynamicScenario);
            	
				/* In case of an invalid plan throws an exception that specifies
			     * the issue. */
			    validator.validateDynamicScenario(dynamicScenario,
											      commonPlanDynamic);
			}
		}
	}
	
	
	/* Just checks whether the given algorithm solves classic scenarios. */
	private boolean classicScenario(MAPFAlgorithm algorithm) {
		
		return (algorithm.equals(
				MAPFAlgorithm.RuntimeReplanner) ||
			algorithm.equals(
				MAPFAlgorithm.AlternatingRuntimeReplanner) ||
			algorithm.equals(
				MAPFAlgorithm.EnhancedHierarchicalPlanner) ||
			algorithm.equals(
				MAPFAlgorithm.CA_STAR) ||
			algorithm.equals(
			    MAPFAlgorithm.TrafficSimulator));		
	}
	
	
	/* Just checks whether the given algorithm solves dynamic scenarios. */
	private boolean dynamicScenario(MAPFAlgorithm algorithm) {
		
		return algorithm.equals(MAPFAlgorithm.TokenPassing);
	}
	
	
	/* Given a scenario with a directed map, creates a new scenario with an
	 * undirected map by copying each edge and point the copy in the opposite
	 * direction if it doesn't exist already. */
	private Scenario createUndirectedScenario(Scenario directedScenario,
											  int edgeDirectionChangeFreq) {
				
		/* Create a deep copy of the map and then add edges such that for
		 * every edge (v1, v2) there's an edge (v2, v1) but no duplicates.
		 * */
		Map copiedMap = directedScenario.map().deepCopy();
		
		undirectMap(copiedMap);
			
		/* Create a new agent array containing deep copies of all agents. */
		Agent copiedAgents[] = new Agent[directedScenario.agents().length];
		
		for (int index = 0; index < directedScenario.agents().length;
			 index++) {
			
			Agent agent =  directedScenario.agents()[index];
			
			copiedAgents[index] = agent.deepCopy();
		}
		
		
		/* Create a new task set containing deep copies of all tasks. */
		HashSet<Task> copiedTasks = new HashSet<Task>();
		
		for (Task task : directedScenario.tasks()) {
			
			copiedTasks.add(task.deepCopy());
		}				
		
		return new Scenario(copiedMap, copiedAgents, copiedTasks,
							edgeDirectionChangeFreq);
	}
	
	
	/* Add edges such that for every edge (v1, v2) there's an edge
	 * (v2, v1) but no duplicates. */
	private void undirectMap(Map map) {
		
		HashSet<Edge> combinedEdges = new HashSet<Edge>();
		
		for (Edge edge: map.edges) {
			
			combinedEdges.add(edge);
			
		    combinedEdges.add(new Edge(edge.target, edge.source, true));	
		}
		
		map.edges = combinedEdges;
	}

		
	/* Returns the average time it took to execute a task,
	 * that is from the availability to the delivery time. */
	private int computeServiceTime(Scenario scenario) {
		
		int serviceTimeSum = 0;
		
		for (Task task : scenario.tasks()) {
			
			serviceTimeSum += (
					task.completionTime() - task.availabilityTime());
		}
		
		return serviceTimeSum / scenario.tasks().size();		
	}
	
	
	public CommonPlan commonPlanDirected() { return commonPlanDirected; }
	
	public CommonPlan commonPlanUndirected() { return commonPlanUndirected; }
	
	public CommonPlan commonPlanDynamic() { return commonPlanDynamic; }
	
	public long planningTimeDirected() { return planningTimeDirected; }
	
	public long planningTimeUndirected() { return planningTimeUndirected; }
	
	public long planningTimeDynamic() { return planningTimeDynamic; }
	
	public int makespanDirected() { return makespanDirected; }
	
	public int makespanUndirected() { return makespanUndirected; }
	
	public int makespanDynamic() { return makespanDynamic; }
	
	public int flowtimeDirected() { return flowtimeDirected; }
	
	public int flowtimeUndirected() { return flowtimeUndirected; }
	
	public int flowtimeDynamic() { return flowtimeDynamic; }
	
    public int serviceTimeDirected() { return serviceTimeDirected; }
    
    public int serviceTimeUndirected() { return serviceTimeUndirected; }
    
    public int serviceTimeDynamic() { return serviceTimeDynamic; }
}

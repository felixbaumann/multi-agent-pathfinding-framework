/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package multi_agent_pathfinding_framework;

import java.util.HashSet;

/* This class validates a given CommonPlan with respect to a given scenario
 * by searching for conflicts, making sure all moves are legal and checking
 * whether the plan achieves each agent's goal. */
public class Validator {

	/* Validates a scenario in which each agent has exactly one specific task.
	 * No task swapping allowed. */
	public void validateClassicScenario(Scenario scenario,
										CommonPlan commonPlan)
										throws InvalidPlanException {
		
		/* There should be exactly one plan for each agent. */
		if (scenario.agents().length != commonPlan.planCount()) {
			
			exception("\n\nNumber of plans does not match. "
					+ "There should be " 
					+ String.valueOf(scenario.agents().length) 
					+ " plans but there are " 
					+ String.valueOf(commonPlan.planCount()));
		}
		
		for (Plan plan : commonPlan.commonPlan()) {
			
			if (plan == null) {
				
				exception("\n\nSome individual plan is null.\n");
			}
			
			Agent agent = plan.agent;
			
			Position start = agent.position();
			
			Position goal = agent.task().targets()[0];
			
			/* Plans are only allowed to be empty if the agent already starts
			 * at its goal. */
			if (plan.length() == 0 && !start.equals(goal)) {			
					
				exception("\n\nEmpty individual plan for agent "
					+ plan.agent.name() + " even though start and "
					+ "goal are different.\n");
			
			}
						
			/* A plan should start at the agent's initial position. */
			if (!plan.plan().get(0).equals(
					new TimedPosition(start, 0))) {
				
				exception("\n\n" + plan.agent.name()
						+ "'s individual plan starts "
						+ "with " + plan.plan().get(0).print()
						+ " even though the agent's initial position is"
						+ new TimedPosition(start, 0).print()
						+ ".\n");
			}
			
			/* A plan should end at the agent's goal. */
			if (!plan.lastTimedPosition(0).position().equals(goal)) {
				
				exception("\n\n" + plan.agent.name()
						+ "'s individual plan ends at "
						+ plan.lastTimedPosition(0).position().print()
						+ " even though his goal position is " + goal.print()
						+ "\n.");
			}
														
			/* None of the positions in the plan should hold an obstacle. 
			 * Note that this may also indicate a flawed map. */
			obstacleClash(plan, scenario.map().obstacles);

			/* Neither leaving out time steps nor multiple positions for the
			 * same time are allowed. */
			timeConsistency(plan);

			/* There should be an edge between each TimedPosition and its
			 * successor (unless they refer to the same position). */
			edgeMissing(plan, scenario.mapManager);							
		}		

        conflicts(commonPlan);       	
	}
	
	
	/* Validates a scenario where the assignment of tasks to agents is free.
	 * An agent is allowed to perform multiple tasks consecutively. */
	public void validateDynamicScenario(
			Scenario scenario,
			CommonPlan commonPlan)
			throws InvalidPlanException {
		
		
		/* There should be exactly one plan for each agent. */
		if (scenario.agents().length != commonPlan.planCount()) {
			
			exception("\n\nNumber of plans does not match. "
					+ "There should be " 
					+ String.valueOf(scenario.agents().length) 
					+ " plans but there are " 
					+ String.valueOf(commonPlan.planCount()));
		}
		
		for (Plan plan : commonPlan.commonPlan()) {
			
			if (plan == null) {
				
				exception("\n\nSome individual plan is null.\n");
			}
			
			/* None of the positions in the plan should hold an obstacle. 
			 * Note that this may also indicate a flawed map. */
			obstacleClash(plan, scenario.map().obstacles);

			/* Neither leaving out time steps nor multiple positions for the
			 * same time are allowed. */
			timeConsistency(plan);

			/* There should be an edge between each TimedPosition and its
			 * successor (unless they refer to the same position). */
			edgeMissing(plan, scenario.mapManager);	
		}
		
		conflicts(commonPlan);
		
		/* Each task has to be completed by an arbitrary agent. */
		taskLoop:
		for (Task task : scenario.tasks()) {
			
			for (Plan plan : commonPlan.commonPlan()) {
				
				if (planCompletesTask(plan, task)) {
					
					continue taskLoop;
				}
			}
		    
		    throw new InvalidPlanException("" + task.print());
		}

		/* All tasks completed by some agent. */
	}
	
	
	/* This function checks whether a given plan completes a given task. */
	private boolean planCompletesTask(Plan plan, Task task) {
		
		int target = 0;
		
		int numberOfTargets = task.targets().length;
		
		/* Go over the entire plan... */
		for (TimedPosition timedPosition : plan.plan()) {
			
			/* Does the current position in the plan match the current target?
			 */
			if (timedPosition.position().equals(task.targets()[target])) {
				
				/* Then we're now looking for a match for the next target. */
				target++;
				
				/* If this was the last target, the plan indeed completes
				 * the task. */
				if (target == numberOfTargets) { return true; }
			}
			/* The current position is not a target. */
		}
		
		/* The plan ended but not all targets were reached. */
		return false;		
	}
	
	
	/* Returns true if at least one of the positions noted in the plan is
	 * an obstacle. */
	private void obstacleClash(Plan plan, HashSet<Position> obstacles)
			throws InvalidPlanException {
		
		for (TimedPosition timedPosition : plan.plan()) {
			
			if (obstacles.contains(timedPosition.position())) {
				
				exception("\n\n" + plan.agent.name()
						+ " steps on an obstacle at "
						+ timedPosition.print()
						+ ".\n");
			}
		}
	}
	
	
	/* A plan should start at time 0 and each following TimedPosition should
	 * have an incremented time stamp. */
	private void timeConsistency(Plan plan) throws InvalidPlanException {
		
		for (int time = 0; time < plan.length(); time++) {
			
			if (plan.plan().get(time).t != time) {
				
				exception("\n\nTime inconsistency in " + plan.agent.name()
					+ "'s plan: For time " + String.valueOf(time)
					+ " the TimedPosition " + plan.plan().get(time).print()
					+ " is noted.\n\n" + plan.print() + "\n");
			}
		}
	}
	
	
	/* Analyzes the plan with respect to the existence of edges allowing
	 * the steps between any position and its follower. */
	private void edgeMissing(Plan plan, MapManager mapManager)
			throws InvalidPlanException {
		
		for (int index = 0; index < plan.length() - 1; index++) {
			
			Position position = plan.plan().get(index).position();
			
			Position successor = plan.plan().get(index + 1).position();
			
			/* For wait operations no edge is needed. */
			if (position.equals(successor)) continue;			
			
			Edge edge = new Edge(position, successor);
			
			/* Check whether an edge from position to successor exists at
			 * the current time (index). */
			if (!mapManager.passagePermitted(new TimedEdge(index, edge))) {
				
				exception("\n\n" + plan.agent.name()
						+ "'s plan includes using the edge "
						+ edge.print() + " at time " + index
						+ " which does not exist on the map.\n");
			}
		}
	}
	
	
	/* Checks whether there is a node- or edge conflict in the given
	 * CommonPlan. */
	private void conflicts(CommonPlan commonPlan) throws InvalidPlanException {		
		
		/* Node conflicts. */
		for (int time = 0; time < commonPlan.planLength(); time++) {
			
			HashSet<Position> positions = new HashSet<Position>();
			
			/* If the same position shows up in multiple plans for the same
			 * time, the positions set will be smaller than expected and thus
			 * reveal this conflict. */
			for (Plan plan : commonPlan.commonPlan()) {
				
				positions.add(plan.position(time, true));
			}
			
			if (positions.size() != commonPlan.planCount())  {
				
				exception("\n\nThere's a node conflict at time "
						+ String.valueOf(time) + " in the common plan.\n");
			}
		}
		
		/* Edge conflicts. */		
		for (int time = 0; time < commonPlan.planLength() - 1; time++) {
			
			HashSet<Edge> edges = new HashSet<Edge>();
			
			/* Counts how many agents move in this time step. */
			int movements = 0;
			
			/* For each movement, add the respecitve edge as well as an edge
			 * in the opposite direction to a set. Without conflicts, there
			 * will be twice as many edges in the set as there are movements.
			 * However, in case of an edge conflict, the set will be smaller
			 * revealing said conflict. */
			for (Plan plan : commonPlan.commonPlan()) {
				
				Position position = plan.position(time, true);
				
				Position successor = plan.position(time + 1, true);
				
				/* Without movement there can't be an edge conflict. */
				if (position.equals(successor)) continue;
				
				movements++;
				
				edges.add(new Edge(position, successor));
				
				edges.add(new Edge(successor, position));				
			}
			
			if (edges.size() != movements * 2) {
			
				exception("\n\nThere's an edge conflict between time "
					+ String.valueOf(time) + " and "
					+ String.valueOf(time + 1) + " in the common plan.\n");	
			}	
		}		
	}
	
	
	private void exception(String message) throws InvalidPlanException {
	
		throw new InvalidPlanException(message);
	}
}

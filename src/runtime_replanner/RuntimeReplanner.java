/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package runtime_replanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeoutException;

import multi_agent_pathfinding_framework.A_STAR;
import multi_agent_pathfinding_framework.Scenario;
import multi_agent_pathfinding_framework.TimedEdge;
import multi_agent_pathfinding_framework.CommonPlan;
import multi_agent_pathfinding_framework.Plan;
import multi_agent_pathfinding_framework.Agent;
import multi_agent_pathfinding_framework.AgentOrder;
import multi_agent_pathfinding_framework.TimedPosition;
import multi_agent_pathfinding_framework.Timeout;
import multi_agent_pathfinding_framework.Edge;
import multi_agent_pathfinding_framework.Position;


/* This class represents a planning approach that does not consider conflicts
 * that are more than one time step ahead. Instead, each agent follows its
 * own shortest way to the goal while ignoring everyone else.
 * To avoid crashes, in each time step agents announce one after the other
 * which action they want to take. If this does not conflict with previous
 * announcements, the action is tentatively permitted. However, if this action
 * does conflict, the agent has to move out of the way by announcing a
 * different action that does not conflict with the previous ones.
 * When moving out of the way, actions are preferred that minimize the
 * Manhattan distance to the goal.
 * If an agent is incapable of taking any legal action at all, backtracking
 * is initiated and the previous agent has to choose a different action.
 * Once every agent found a legal action, they are executed and the planning
 * proceeds with the next time step.
 * Note that it's not possible to undo actions once the actions of a time step
 * are executed. Thus, the appearance of a deadlock dooms the entire planning
 * process. */
public class RuntimeReplanner {

	/* Main function.
	 * 
	 * timeHorizon	limits the makespan (longest individual plan) of the
	 * 				resulting common plan.
	 * 
	 * trialLimit	limits how often the planning process may be executed.
	 * 				This may be useful, since an unsuccessful process does not
	 * 				rule out, that a different agent order may lead to
	 * 				a successful process.
	 * 
	 * runtimeLimit limits the runtime of the planning process.
	 *              If no CommonPlan has been found once the limit is
	 *              exceeded, the planning failed and a plan of null is
	 *              assumed.
	 */
	public CommonPlan runtimeReplanner(Scenario scenario,
									   int timeHorizon,
									   int trialLimit,
									   long runtimeLimit)
											   throws TimeoutException {
		
		/* First, each agent plans on its own ignoring all the others.. */
		CommonPlan originalPlans
		    = independentPlans(scenario, timeHorizon, runtimeLimit);
		
		/* If at least one agent can't even find a plan if it were the only
		 * agent on the map, there's no point in trying to solve conflicts. */
		if (originalPlans == null) { return null; }						
		
		/* Consider multiple random agent orders if necessary.
		 * Once an order yields a valid common plan, this plan is returned
		 * without considering additional orders. */
		AgentOrder order = new AgentOrder(scenario.agents());
		
		for (int trial = 0; trial < trialLimit; trial++) {		
		
			CommonPlan plans = originalPlans.deepCopy();
			
			/* Deal with conflicts as they come up. Ignore any potential
			 * problems coming up multiple time steps in the future. */
			for (int time = 0; time < timeHorizon; time++) {
				
				/* Cancel the process if it exceeds the runtime limit. */
				Timeout.checkForTimeout(runtimeLimit);				
				
				if (allGoalsReached(plans, time)) { return plans; }
	
				/* This function returns null if each agent was able to take
				 * some legal action. Useful ones are preferred but not
				 * mandatory.
				 * A result different from null represents a deadend.
				 * No matter what the agents do, they can't take a legal step
				 * anymore.
				 * If agents have to move out of other agents' ways, there
				 * respective plans are adjusted accordingly. */
				if (attemptStep(scenario, plans, order, time,
						        runtimeLimit) != null) { 

					break;
				}
			}
			
			/* Since planning was unsuccessful, let's try a different order of
			 * agents. */
			order = shuffleAgentOrder(order);
		}
		
		/* None of the agent orders used was successful. */
		return null;
	}
	
	
	/* Use simple A* to compute a plan for each agent ignoring the other
	 * agents. */
	protected CommonPlan independentPlans(Scenario scenario,
										  int timeHorizon,
										  long runtimeLimit)
												  throws TimeoutException {
		
		A_STAR star = new A_STAR();
		
		CommonPlan plans = new CommonPlan();
		
		for (Agent agent : scenario.agents()) {
			
			Position start = agent.position();
			
			Position goal = agent.task().targets()[0];		
			
			/* Find a sequence of positions leading to the goal.
			 * This sequence does not include the start position! */
			ArrayList<Position> positions
			    = star.AStar(scenario.map(), start, goal);			
			
			/* If one of the agents can't even find a plan ignoring all the
			 * others, the whole scenario is doomed. */
			if (positions == null) { return null; }
			
			/* Plans always start with the target of the first step.
			 * However, we'd like to include the initial position. */
			positions.add(0, start);
			
			/* Transform the position sequence to a plan which is
			 * a TimedPosition sequence, here starting at time 0. */
			plans.addPlan(new Plan(agent, positions, 0));
		}
		return plans;
	}
	
	
	/* Check whether each agent is at its goal position at the given time
	 * according to the given plans. */
	protected boolean allGoalsReached(CommonPlan plans, int time) {
		
		for (Plan plan : plans.commonPlan()) { 
		
			Position goal = plan.agent.task().targets()[0];
			
			Position target = plan.position(time, true);
			
			if (!goal.equals(target)) { return false; }		
		}		
		return true;
	}

	
	/* Deal with the given time step by letting every agent do its planned
	 * step or if blocked, trying out every other possible action favoring
	 * those with smaller Manhattan distance.
	 * Then includes a replanning process after the given time, cuts the plans
	 * after this time and appends the replanned ones. */
	protected Agent attemptStep(Scenario scenario,
								CommonPlan plans,
								AgentOrder order,
								int time,
								long runtimeLimit) throws TimeoutException {	
		
		/* Cancel the process if it exceeds the runtime limit. */
		Timeout.checkForTimeout(runtimeLimit);
		
		/* Stores the positions of the agents after this step.
		 * Positions are ordered according to the agents' ids,
		 * not the given agentOrder. */
        Position locations[] = new Position[scenario.agents().length];
        
        for (int index = 0; index < locations.length; index++) {
        	
        	locations[index] = null;
        }

		ClaimContainer claims = new ClaimContainer(scenario.agents());
		
		/* Hopefully null (if each agent found a legal action). */
		Agent incapableAgent
		    = step(scenario, plans, order, time, 0, claims, locations,
		    	   runtimeLimit);
		
		/* If there's no incapable Agent, replan for all those who were unable
		 * to do their planned step and had to go out of the way instead. */
		if (incapableAgent == null) {
			
			A_STAR star = new A_STAR();
			
			/* Check which agents are at their planned positions. */
			for (int index = 0; index < locations.length; index++) {
				
				Plan plan = plans.get(index);
				
				Position actualLocation = locations[index];
				
				/* Check whether the calculated step (target stored in
				 * locations) will bring the agent off track of its plan.
				 * This would mean that at time + 1 the agent is not where
				 * it's expected to be. */
				if (agentOffTrack(actualLocation, plan, time)) {					

					/* Remove the future part of the plan since the agent is
					 * off track. */
					plan.cutAfter(time);
					
					/* The agent may have reached its goal long ago so his
					 * plan ended and he did not move up to now.
					 * Thus, when adding the new actualLocation, there may or
					 * may not be a gap in time in his plan during which he
					 * simply stayed at his goal position.
					 * To keep the plan complete, the following function call
					 * adds the missing TimedPositions to the plan up to
					 * (and including) time with the latest available
					 * position. */
					plan.fillUp(time);
					
					/* Add the calculated step to move out of the way of some
					 * other agent. */
					plan.add(new TimedPosition(actualLocation, time + 1));
					
					/* Replan after the given time.
					 * This sequence does not include the start position! */
					ArrayList<Position> positions
					    = star.AStar(scenario.map(),
					    		     actualLocation,
					    		     plan.agent.task().targets()[0]);
					
					/* Add the new part of the plan. */
					plan.add(new Plan(plan.agent, positions, time + 2));
				}
			}								 			
		}

		/* Return the incapable agent or return null to inform about a
		 * successful step. */
		return incapableAgent;
	}		
	
	
	/* Tries to perform a single step for a given agent in the given scenario.
	 * The method calls itself recursively to consider a step for the next
	 * agent. If unsuccessful, it backtracks and considers a different step.
	 * Steps according to the plans are prefered. If not possible,
	 * steps with smaller Manhattan distance to the agent's goal are prefered
	 * over those with a larger distance.
	 * 
	 * If planning fails, returns the agent who is unable to take any legal
	 * action at all. Otherwise returns null.*/
	protected Agent step(Scenario scenario,
			 		     CommonPlan plans,
			 		     AgentOrder order,
			 		     int time,
					     int orderIndex,
					     ClaimContainer claims,
					     Position locations[],
					     long runtimeLimit) throws TimeoutException {
		
		/* Cancel the process if it exceeds the runtime limit. */
		Timeout.checkForTimeout(runtimeLimit);
		
		int agentId = order.getAgentIdByOrderIndex(orderIndex);
		
		Plan plan = plans.getPlanByAgentId(agentId);
		
		Position positionNow = plan.position(time, true);
		
		Position positionNext = plan.position(time + 1, true);
		
		ClaimPosition claimPos = new ClaimPosition(positionNext);
		
		ClaimEdge claimEdge = positionNow.equals(
			positionNext) ? null : new ClaimEdge(positionNow, positionNext);
		
		Agent incapableAgent = plan.agent;
		
		/* Make the agent perform its next step according to its plan
		 * if no claims on edge or position where uttered by a previous agent.
		 */
		if (claims.noClaimsOn(claimPos, claimEdge)) {

			claims.addClaims(plan.agent, claimPos, claimEdge);
			
			locations[scenario.indexOfAgent(agentId)] = positionNext;
			
			/* This is the last agent and it's able to do its step. */
			if (orderIndex + 1 == locations.length) return null;
			
			/* There are more agents. Let the next one try to take a step. */
			incapableAgent = step(scenario, plans, order, time,
								  orderIndex + 1, claims, locations,
								  runtimeLimit);
			
			/* If none of the following agents have an issue, we're done here
			 * for now. */
			if (incapableAgent == null) return null;
		}

		
		/* So the agent was not allowed to do its planned step. Either because
		 * of claims by previous agents or because of resulting incapabilities
		 * of following agents. */
		
		
		/* Consider all alternatives if necessary. */
		ArrayList<Position> alternatives = getAlternatives(scenario,
														   positionNow,
														   time,
														   claims,
														   plan.agent);		
		
		for (Position alternative : alternatives) {
		
			/* Release any claims this agent may have already made for
			 * a previous alternative or the planned step. */
			claims.removeClaims(plan.agent);
			
			positionNext = alternative;
			
			claimPos = new ClaimPosition(positionNext);
			
			claimEdge = positionNow.equals(
				positionNext) ? null : new ClaimEdge(positionNow,
													 positionNext);
			
			/* Make the agent perform the alternative step if allowed to
			 * do so. */
			if (claims.noClaimsOn(claimPos, claimEdge)) {

				claims.addClaims(plan.agent, claimPos, claimEdge);
				
				locations[scenario.indexOfAgent(agentId)] = positionNext;
				
				/* This is the last agent in the given order and it's able
				 * to do its step. */
				if (orderIndex + 1 == locations.length) return null;
				
				/* There are more agents. Let the next one try a step. */
				incapableAgent = step(scenario, plans, order, time,
									  orderIndex + 1, claims, locations,
									  runtimeLimit);
				
				/* If none of the following agents have an issue,
				 * we're done here for now. */
				if (incapableAgent == null) return null;			
			}
		}		
		
		/* Since we have to backtrack even further, the current agent should
		 * release its claims for the last alternative. Maybe he'll be able
		 * to get his first preference after all depending on what his
		 * predecessors choose now. */
		claims.removeClaims(plan.agent);
				
		return incapableAgent;								
	}
	
	
	/* The agent is not able to reach the position noted for the given time in
	 * the given plan. However, it may either wait or move in any of the other
	 * directions. Consider all these options, rank them by the Manhattan
	 * distance of their target to the agent's goal and return the ranked
	 * list. */
	protected ArrayList<Position> getAlternatives(Scenario scenario,
												  Position positionNow,
												  int time,
												  ClaimContainer claims,
												  Agent agent) {	
				
		ArrayList<Position> candidates = new ArrayList<Position>();
		
		/* Right. */
		candidates.add(new Position(positionNow.x + 1, positionNow.y));
		
		/* Left. */
		candidates.add(new Position(positionNow.x - 1, positionNow.y));
		
		/* Up. */
		candidates.add(new Position(positionNow.x, positionNow.y + 1));
		
		/* Down. */
		candidates.add(new Position(positionNow.x, positionNow.y - 1));		
		
		ArrayList<Position> alternatives = new ArrayList<Position>();
		
		for (Position candidate : candidates) {
			
			/* Make sure there's an edge leading to the candidate.  */
			if (!scenario.mapManager.passagePermitted(
				new TimedEdge(time, new Edge(positionNow, candidate)))) {
					
				continue;					
			}
									
			/* Make sure the candidate is not already claimed by a previous
			 * agent. */
			if (claims.contains(new ClaimPosition(candidate))) { 
				
				continue;
			}
			
			if (claims.contains(new ClaimEdge(positionNow, candidate))) { 
				
				continue;
			}
			
			/* The candidate is a valid option for the agent to move out of
			 * the way of other agents. */
			alternatives.add(candidate);			
		}						
		
		/* Sort the options the agent can move to in the next step with
		 * respect to to their distance to the goal. */
		alternatives.sort(
			new ManhattanDistanceComparator(agent.task().targets()[0]));
		
		
		/* Waiting may be an option as well. We prefer movement though to
		 * avoid the case where everyone is waiting forever. */
		if (!claims.contains(new ClaimPosition(positionNow))) { 
			
			alternatives.add(positionNow);
		}
		
		return alternatives;				
	}

	
	/* Checks whether the given location fits the next position on
	 * the plan. */
	protected boolean agentOffTrack(Position actualLocation,
								  Plan plan,
								  int time) {
		
		return !actualLocation.equals(plan.position(time + 1, true));
	}
	
	
	/* Creates a new agent order by copying followed by shuffling the copy. */
	protected AgentOrder shuffleAgentOrder(AgentOrder agentOrder) { 
	
		ArrayList<Integer> order = agentOrder.getCopyAsList();
		
		Collections.shuffle(order);
		
		return new AgentOrder(order);
	}
}

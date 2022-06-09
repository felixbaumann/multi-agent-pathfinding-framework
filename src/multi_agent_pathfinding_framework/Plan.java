/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package multi_agent_pathfinding_framework;

import java.util.ArrayList;

/* A plan can be expressed as a sequence of timed positions.
 * Starting with the initial position of the agent, followed by a neighboring
 * position in each time step until finally reaching a goal position. */
public class Plan {

	private ArrayList<TimedPosition> plan
	    = new ArrayList<TimedPosition>();
	
	public final Agent agent;
	
	public Plan(Agent agent) { this.agent = agent; }
	
	
	public Plan(Agent agent, ArrayList<TimedPosition> positions) {
		
		this.agent = agent;
		this.plan = positions;		
	}
	
	
	/* Create a plan from a sequence of positions starting at the given time.
	 */
	public Plan(Agent agent, ArrayList<Position> positions, int startTime) {
		
		this.agent = agent;		
		
		for (int index = 0; index < positions.size(); index++) {
			
			add(new TimedPosition(positions.get(index), startTime + index));			
		}
	}
	
	
	/* Creates a deep copy of this plan.
	 * Changes on the copy have no effect on this plan. */
	public Plan deepCopy() {
		
		Plan copy = new Plan(this.agent);
		
		for (TimedPosition timedPosition : plan) {
			
			copy.add(timedPosition);
		}		
		return copy;
	}
	
	
	public ArrayList<TimedPosition> plan() { return plan; }		
	
	
	/* Returns the last timed position of the positional plan. Should always
	 * exist since the constructor always adds the start position of an agent
	 * to the plan. */
	public TimedPosition lastTimedPosition(int currentTime) {
		
		int lastPositionIndex = plan.size() -1 ;
		
		if (lastPositionIndex >= 0) {
		
		    return plan.get(lastPositionIndex);
		}
		
		/* If the plan happens to be empty, return the current position and
		 * time of the agent. */
		return new TimedPosition(agent.position(), currentTime);
	}
	
	
	/* Returns the agents position at the given time according to the plan.
	 * If the plan ends before the given time, the restingAssumption decides
	 * on whether to assume the agent remains at the last position of his
	 * plan or not.*/
	public Position position(int time, boolean restingAssumption) {
		
		if (plan.size() == 0) { return null; }
		
		int startTime = plan.get(0).t;
		
		/* Time before the beginning of the plan requested. */
		if (time < startTime) { return null; }				

		int endTime = plan.get(plan.size() - 1).t;
		
		/* Time after the end of the plan requested.  */
		if (time > endTime) {
			
			/* Out of range. Assume the agent doesn't move once the plan ends. */
			if (restingAssumption) {
				
				return plan.get(plan.size() - 1).position();
			}
			
			/* Out of range without restingAssumption. No information about the
			 * agent's whereabouts at the given time. */
			else { return null; }
		}		
		
		/* Find the timed position with the correct time. */
		return plan.get(time - startTime).position();						
	}
	
	
	/* Action plan length. */
	public int length() { return plan.size(); }		
	
	
	/* Append a single action to the current plan. */
	public void add(TimedPosition position) {
		
		this.plan.add(position);		
	}
	
	
	/* Append a whole plan to this current plan. */
	public void add(Plan plan) {
		
		if (plan == null) { return; }
		
		for (TimedPosition position : plan.plan()) {
			
			add(position);
		}
	}
	
	
	/* Removes the last timed position from the plan.
	 * Use the boolean exceptTimeZero to specify that the last position
	 * must only be removed if it refers to a time other than 0. */
	public void removeLastPos(boolean exceptTimeZero) {
	
		if (exceptTimeZero && plan.get(plan.size() - 1).t == 0) { return; }
		
		plan.remove(plan.size() - 1);		
	}

	
	/* Checks whether this plan does not contain any TimedPositions so far. */
	public boolean empty() { return plan.size() == 0; }
	
	
	/* Returns the time of the first TimedPosition in this plan.
	 * If there is none, indicate this by returning -1. */
	public int startTime() {
		
		if (length() > 0) { return plan.get(0).t; }
		
		return -1;
	}
	
	
	/* Remove all TimedPositions from the end of the plan that have a time
	 * stamp later than the given time. */
	public void cutAfter(int time) {
		
		ArrayList<TimedPosition> newPlan
		    = new ArrayList<TimedPosition>();
		
		/* Stop after the specified time or once the end of the plan is
		 * reached. */
		for (int index = 0; index < Math.min(time + 1, plan.size());
			 index++) {
			
			newPlan.add(plan.get(index));
		}		
		plan = newPlan;
	}
	
	
	/* If the plan ends before the given time, this function fills it up with
	 * TimedPositions for each missing timestep at the end assuming the agent
	 * will stay where the plan led him. */
	public void fillUp(int time) {
		
		/* There's no filling up possible if the plan is already complete up
		 * to the given time */
		if (time < 0 || length() > time) { return; }
		
		/* An empty plan can still be filled up by assuming the agent's
		 * initial position. */
		Position position = empty() ? agent.position() : lastTimedPosition(
				90000).position();									
		
		for (int timeStep = length(); timeStep <= time; timeStep++) {
			
			add(new TimedPosition(position, timeStep));
		}
	}
	
	
	/* If an agent is delayed starting at a certain time,
	 * he can postpone all his action by a fixed delay.*/
	public void delay(int delay, int index) {
		
		int length = length();
		
		while (index < length) {
			
			TimedPosition old = plan.get(index);
			
			plan.set(index, new TimedPosition(old.position(), old.t + delay));
			
			index++;			
		}
	}
	
	
	public String print() {
		
		String string = "\nAgent " + agent.id() + ": ";
		
		for (TimedPosition timedPosition : plan) {
			
			string += (timedPosition.print() + ", ");
		}		
		return string;
	}
}

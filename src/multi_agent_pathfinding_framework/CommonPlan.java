/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package multi_agent_pathfinding_framework;

import java.util.ArrayList;

/* An instance of this class combines several plans - one for each agent. */
public class CommonPlan {

	private ArrayList<Plan> plans = new ArrayList<Plan>();
	
	
	/* Returns the length of the longest individual plan in this common plan.
	 * Also called makespan.
	 * Returns -1 if this common plan does not contain any individual plans at
	 * all. */
	public int planLength() {
		
		int max = -1;
		
		for (Plan plan : plans) {
			
			max = Math.max(max, plan.length());
		}		
		return max; 
	}
	
	
	/* Returns the sum of the lengths of the inidivual plans.
	 * Also called the flowtime.
	 * Returns 0 if this common plan does not contain any individual plans at
	 * all.*/
	public int sumOfCosts() {
		
		int sum = 0;
		
		for (Plan plan : plans) {
			
			sum += plan.length();
		}		
		return sum;
	}
	
	
	public ArrayList<Plan> commonPlan() { return plans; }
	
	public void addPlan(Plan plan) { plans.add(plan); }
	
	public Plan get(int index) { return plans.get(index); }
	
	public Plan getPlanByAgentId(int id) {
		
		for (Plan plan : plans) {
			
			if (plan.agent.id() == id) { return plan; }			
		}
		
		return null;
	}
	
	public void set(int index, Plan plan) { plans.set(index, plan); }
	
	
	/* Return true if any of the agents did not get a plan. */
	public boolean isIncomplete() {
		
		for (Plan plan : plans) {
			
			if (plan == null) { return true; }
		}		
		return false;
	}
	
	
	/* Returns the number of individual plans in this common plan. */
	public int planCount() { return plans.size(); } 
	
	
	/* Create a deep copy of this plan. Changes on the copy will not have
	 * an effect on this plan. */
	public CommonPlan deepCopy() {
		
		CommonPlan copy = new CommonPlan();
		
		for (Plan plan : plans) {
			
			copy.addPlan(plan.deepCopy());
		}
		return copy;		
	}
	
	
	public String print() {
		
		String string = "\nCommonPlan:";
		
		for (Plan plan : plans) {
			
			string += plan.print();
		}		
		return string;		
	}		
}

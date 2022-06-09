/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package enhanced_hierarchical_planner;

import java.util.ArrayList;
import java.util.HashSet;

import multi_agent_pathfinding_framework.CommonPlan;
import multi_agent_pathfinding_framework.Plan;

/* Instances of this class represent nodes in a constraint tree.
 *
 * A node has a parent node unless it's the root in which case the parent
 * is null.
 * Furthermore, a node has an associated set of vertex- and edge constraints.
 * The solution (plans) of this node have to be true to those constraints.
 * They may still have conflicts though. If this is the case, two children
 * of this node are created with the same constraints but an additional one
 * that resolves one of these conflicts. Each child gets a different
 * constraint representing the two ways to resolve the conflict.  
 */
public class ConstraintTreeNode implements Comparable<ConstraintTreeNode> {

	public final ConstraintTreeNode parent;
	
	public HashSet<VertexConstraint> vertexConstraints;
	
	public HashSet<EdgeConstraint> edgeConstraints;
	
	private final ArrayList<Traversal> traversals;
	
	public CommonPlan solution = new CommonPlan();
	
	/* Sum of costs of all the plans in the solution. */
	public int cost = 0;
	
	
	public ConstraintTreeNode(ConstraintTreeNode parent,
							  Constraint newConstraint,
							  ArrayList<Traversal> traversals) {
		
		this.parent = parent;		
		
		this.traversals = traversals;
		
		/* Root node. */
		if (parent == null) { createRoot(); }
		
		/* Child of an existing node. */
		else { createChild(parent, newConstraint); }
	}
	
	
	/* Create the root node of the constraint tree.
	 * It contains no constraints at all. */
	private void createRoot() {
		
		vertexConstraints = new HashSet<VertexConstraint>();
		edgeConstraints = new HashSet<EdgeConstraint>();
	}
	
	
	/* Create a child node in the constraint tree.
	 * Such a node inherits all constraints of its parent node
	 * and gets an additional constraint. */
	private void createChild(ConstraintTreeNode parent,
			 				 Constraint newConstraint) {
		
		/* Copy the constraints of the parent and add the given new one. */
		vertexConstraints
		    = copyVertexConstraints(parent.vertexConstraints);
		edgeConstraints
		    = copyEdgeConstraints(parent.edgeConstraints);
		
		if (newConstraint instanceof VertexConstraint) {
			
			vertexConstraints.add((VertexConstraint) newConstraint);
		}
		
		else {
			
			edgeConstraints.add((EdgeConstraint) newConstraint);
		}
		
		/* Copy the solution of the parent.*/
		solution = deepCopy(parent.solution);		
	}
	
	
	/* Create a deep copy of the given common plan so plans can be changed
	 * in the children nodes without having an effect on each other. */
	private CommonPlan deepCopy(CommonPlan solution) {
		
		CommonPlan deepCopy = new CommonPlan();
		
		for (Plan plan : solution.commonPlan()) {
			
			deepCopy.addPlan(plan.deepCopy());
		}		
		return deepCopy;
	}
	
	
	/* Create a new HashSet with the same constraints as the given one. */
	private HashSet<VertexConstraint> copyVertexConstraints(
			HashSet<VertexConstraint> constraints) {
		
		HashSet<VertexConstraint> copy = new HashSet<VertexConstraint>();
		
		for (VertexConstraint constraint : constraints) {
			
			copy.add(constraint);
		}
		return copy;
	}
	
	
	/* Create a new HashSet with the same constraints as teh given one. */
	private HashSet<EdgeConstraint> copyEdgeConstraints(
			HashSet<EdgeConstraint> constraints) {
				
		HashSet<EdgeConstraint> copy = new HashSet<EdgeConstraint>();
		
		for (EdgeConstraint constraint : constraints) {
			
			copy.add(constraint);
		}
		return copy;
	}
	

	@Override
	public int compareTo(ConstraintTreeNode otherNode) {

		return Integer.compare(this.cost, otherNode.cost);
	}
}

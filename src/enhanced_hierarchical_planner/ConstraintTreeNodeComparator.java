/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package enhanced_hierarchical_planner;

import java.util.Comparator;


/* This is a comparator that compares two nodes of the constraint tree with
 * respect to their costs. The cost of such a node is the sum of costs of
 * all the plans in the solution of this node.
 * This comparator hence allows to choose the most promising node for
 * expansion. */
public class ConstraintTreeNodeComparator 
    implements Comparator<ConstraintTreeNode> {
	
	@Override
	public int compare(ConstraintTreeNode node1,
			           ConstraintTreeNode node2) {

		Integer cost1 = node1.cost;
		Integer cost2 = node2.cost;
		
		return cost1.compareTo(cost2);
	}
}

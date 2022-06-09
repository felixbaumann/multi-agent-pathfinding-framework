package multi_agent_pathfinding_framework;

/* Indicates that a table lookup failed. A distance table contains
 * distances between given positions. If a lookup failed the table is
 * incomplete or a position is requested that should not be necessary. */
public class DistanceTableException extends Exception {

	private static final long serialVersionUID = -413311272056372740L;

	public DistanceTableException(String errorMessage) {
		
		super(errorMessage);
	}
}

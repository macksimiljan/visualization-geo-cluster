package representation;

/**
 * Edges. 
 * 
 * @author MM
 *
 */
public class InputEdge {
	
	/** ID of the source vertex. */
	public Long source;
	/** ID of the target vertex. */
	public Long target;
	/** Trigram similarity between source and target vertex. */
	public Double trigramSim;
	/** ... */
	public Double aggsimValue;
	
	/** Constructor.*/
	public InputEdge() {
		this.source = null;
		this.target = null;
		this.trigramSim = null;
		this.aggsimValue = null;
	}

}

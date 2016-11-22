package representation;

import java.util.HashSet;
import java.util.Set;

/**
 * Cluster representative.
 * 
 * @author MM
 *
 */
public class ClusterRepresentative extends Vertex {
	
	/** Types of the cluster, e.g. Settlement. */
	public Set<String> typeIntern;
	/** Simple type of the cluster. */
	public String simpleType;	
	/** Ontologies which have a vertex within the cluster. */
	public Set<String> ontologies;
	/** IDs of the vertices contained the cluster. */
	public Set<Long> clusteredVertexIds;

	/** Constructor. */
	public ClusterRepresentative() {
		this.typeIntern = new HashSet<String>();
		this.ontologies = new HashSet<String>();
		this.clusteredVertexIds = new HashSet<Long>();		
	}
}

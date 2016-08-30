package representation;

import java.util.HashSet;
import java.util.Set;

public class ClusterRepresentative extends Vertex {
	
	public Set<String> typeIntern;
	public Set<String> ontologies;
	public Set<Long> clusteredVertexIds;

	
	public ClusterRepresentative() {
		this.typeIntern = new HashSet<String>();
		this.ontologies = new HashSet<String>();
		this.clusteredVertexIds = new HashSet<Long>();		
	}
}

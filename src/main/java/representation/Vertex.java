package representation;

import java.util.HashSet;
import java.util.Set;

/** 
 * Vertex.
 * 
 * @author MM
 *
 */
public class Vertex {

	/** Vertex ID. */
	public Long id;
	/** Types of this vertex, e.g. Settlement.*/
	public Set<String> typeInternInput;
	/** ... */
	public Float vertexAggSimValue;
	/** ... */
	public Long ccId;
	/** Longitude value of this vertex. */
	public Double lon;
	/** Latitude value of this vertex. */
	public Double lat;
	/** Label of this vertex, e.g. name of the city.*/
	public String label;
	/** URL of this vertex. */
	public String url;
	/** Ontology from which the vertex has been extracted.*/
	public String ontology;
	/** Intern processing note. Added during generation of the output file for visualisation. */
	public String processingNote;
	
	/** Constructor.*/
	public Vertex() {
		this.id = null;
		this.typeInternInput = new HashSet<String>();
		this.vertexAggSimValue = null;
		this.ccId = null;
		this.lon = null;
		this.lat = null;
		this.label = null;
		this.url = null;
		this.ontology = null;
		this.processingNote = null;
	}
	
	

	@Override
	public String toString() {
		return "Vertex [id=" + id + ", lon=" + lon + ", lat=" + lat + ", label=" + label + ", processingNote: " + processingNote + "]";
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Vertex other = (Vertex) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	
}

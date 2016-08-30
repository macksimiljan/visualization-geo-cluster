package representation;

import java.util.HashSet;
import java.util.Set;

public class Vertex {

	public Long id;
	public Set<String> typeInternInput;
	public Float vertexAggSimValue;
	public Long ccId;
	public Double lon;
	public Double lat;
	public String label;
	public String url;
	public String ontology;
	
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
	}
	
	

	@Override
	public String toString() {
		return "Vertex [id=" + id + ", lon=" + lon + ", lat=" + lat + ", label=" + label + "]";
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

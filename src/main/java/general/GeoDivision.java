package general;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import representation.Region;
import representation.Vertex;

/**
 * Classifier for geo-locations to regions.
 *
 * @author MM
 *
 */
public class GeoDivision {
	
	/** Mapping from region to vertices. */
	private Map<Region, Set<Vertex>> verticesPerRegion;
	
	/**
	 * Creates a new classifier for geo-locations.
	 * @param regions List of regions for classifying geo-locations.
	 */
	public GeoDivision(List<Region> regions) {
		this.verticesPerRegion = new HashMap<Region, Set<Vertex>>();
		
		for (Region region : regions) {
			Set<Vertex> s = new HashSet<Vertex>();
			verticesPerRegion.put(region, s);
		}
	}
	
	/**
	 * @param region Region.
	 * @return Vertices of a given region.
	 */
	public Set<Vertex> getVertices(Region region) {
		return verticesPerRegion.get(region);
	}
	
	/**
	 * @return all regions which are mapped from.
	 */
	public Set<Region> getRegions() {
		return verticesPerRegion.keySet();
	}
	
	/**
	 * Adds vertices to the regions they belong to.
	 * @param vertices Set of vertices.
	 */
	public void addVertices(Set<Vertex> vertices) {
		for (Vertex v : vertices) {
			for (Region region : verticesPerRegion.keySet()) {
				if (region.contains(v.lon, v.lat)) {
					Set<Vertex> val = verticesPerRegion.get(region);
					val.add(v);
					verticesPerRegion.put(region, val);
					break;
				}
			}
		}
	}
	
	
	

}

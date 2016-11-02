package general;

import java.util.ArrayList;
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
	 * @return Six default regions which overlapping cover the world.
	 */
	public static List<Region> initDefaultRegions() {
		/*
		 * distance between two latitudes: 110km
		 * distance between two longitudes: 110km (equator), 75km (middle latitudes), 10km (pole)
		 * 
		 * see: http://www.kowoma.de/gps/geo/laengenbreitengrad.htm
		 * see: http://www.kowoma.de/gps/spiele/confluence.htm
		 * 
		 * intersection points of the regions:
		 * 	Atlantic ocean:	lat = 15, lon = -30
		 * 	Arabic sea:		lat = 15, lon = 65
		 * 	Pacific ocean:	lat = 15, lon = -170
		 * 
		 * overlap: 5 long-/latitudes
		 * 
		 */
		List<Region> regions = new ArrayList<Region>();
		
		Region europe = new Region(90, (15-5), (-30-5), (65+5), "europe");
		Region africa = new Region((15+5), -90, (-30-5), (65+5), "africa");
		
		Region asia = new Region(90, (5-5), (65-5), (-170+5), "asia");
		Region australia = new Region((5+5), -90, (65-5), (-170+5), "australia");
		
		Region northAm = new Region(90, (15-5), (-170-5), (-30+5), "northAm");
		Region southAm = new Region((15+5), -90, (-170-5), (-30+5), "southAm");
			
		regions.add(europe);
		regions.add(asia);
		regions.add(northAm);
		regions.add(southAm);
		regions.add(africa);
		regions.add(australia);
		
		return regions;
	}	
	
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
			System.out.println("\t\t>>"+v.label+": "+v.lat+", "+v.lon);
			for (Region region : verticesPerRegion.keySet()) {
				System.out.println("\t\t\t>>"+region.label+": "+region);
				if (region.contains(v.lon, v.lat)) {
					System.out.println("\t\t\t\t>>contained");
					Set<Vertex> val = verticesPerRegion.get(region);
					val.add(v);
					verticesPerRegion.put(region, val);
				}
			}
		}
	}
	
	
	

}

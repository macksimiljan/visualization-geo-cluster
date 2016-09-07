package general;

import java.util.List;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import representation.ClusterRepresentative;
import representation.InputEdge;
import representation.Vertex;

/**
 * Builder for GeoJSON objects and arrays.
 * 
 * @author MM
 *
 */
public class GeoJsonBuilder {
	
	/** Colors of the clusters. */
	final private static String[] COLORS = {"SeaGreen", "Chocolate", "LightSeaGreen", "MediumBlue", "FireBrick", "DodgerBlue", "Orchid"};
	/** Current color index. */
	private static int currColorIndex = 0;
	/** Icon of geo-points within uMap. */
	final private static String ICON = "Drop";
	/** Big edge weight. */
	final private static String LINE_WEIGHT_BIG = "10";
	/** Small edge weight. */
	final private static String LINE_WEIGHT_SMALL = "4";
	/** Edge opacity. */
	final private static String LINE_OPACITY = "0.8";
	
	
	/**
	 * Builds a GeoJSON array as the representation of a cluster.
	 * @param c Cluster representative.
	 * @param set Vertices within the cluster.
	 * @return Cluster as GeoJSON array.
	 */
	@SuppressWarnings("unchecked")
	public JSONArray buildCluster(ClusterRepresentative c, Set<Vertex> set) {
		JSONArray cluster = new JSONArray();
		currColorIndex = ++currColorIndex % COLORS.length;
		
		// add points
		cluster.add(buildPointAsFeature(c, COLORS[currColorIndex]));
		for (Vertex v : set) {
			if (v.lat == null || v.lon == null)
				v.processingNote = "No GeoCoordinates!";
			v.lat = (v.lat == null) ? c.lat : v.lat;
			v.lon = (v.lon == null) ? c.lon : v.lon;
			
			cluster.add(buildPointAsFeature(v, COLORS[currColorIndex]));
		}
		
		// add lines
		cluster.add(buildMultiLineStringAsFeature(buildCoordinatesForCluster(c, set), COLORS[currColorIndex], LINE_WEIGHT_SMALL));
		
		return cluster;
	}
	
	/**
	 * Builds a GeoJSON object as the representation of the original links between geo-objects.
	 * @param dict Vertex dictionary.
	 * @param edges Original links.
	 * @return Original links as GeoJSON object.
	 */
	@SuppressWarnings("unchecked")
	public JSONObject buildOldStructure(VertexDict dict, Set<InputEdge> edges) {
		JSONArray coordinates = new JSONArray();
		for (InputEdge edge : edges) {
			Vertex start = dict.getVertexById(edge.source);
			Vertex target = dict.getVertexById(edge.target);
			JSONArray point0 = new JSONArray();
			point0.add(start.lon);
			point0.add(start.lat);
			JSONArray point1 = new JSONArray();
			point1.add(target.lon);
			point1.add(target.lat);
			
			JSONArray line = new JSONArray();
			line.add(point0);
			line.add(point1);
			
			if (point0.get(0) != null && point0.get(1) != null
					&& point1.get(0) != null && point1.get(1) != null) {
				coordinates.add(line);
			}
			else 
				System.err.println("One edge point has no coordinates: start "+start+", end "+target);
		}
		
		return buildMultiLineStringAsFeature(coordinates, "Gray", LINE_WEIGHT_BIG);
	}
	
	/**
	 * Builds a GeoJSON point as a feature. 
	 * @param v Vertex as the point.
	 * @param color Color of the point.
	 * @return GeoJSON point as a feature.
	 */
	@SuppressWarnings("unchecked")
	public JSONObject buildPointAsFeature(Vertex v, String color) {
		JSONObject obj = new JSONObject();		
		obj.put("type", "Feature");
		
		JSONObject geometry = new JSONObject();
		geometry.put("type", "Point");
		JSONArray coordinates = new JSONArray();
		coordinates.add(v.lon);
		coordinates.add(v.lat);
		geometry.put("coordinates", coordinates);
		obj.put("geometry", geometry);
		
		JSONObject properties = new JSONObject();
		properties.put("name", v.label);
		String description = "ID: "+v.id+"\nType: "+v.typeInternInput+"\nOntology: "+v.ontology;
		description = (v.processingNote == null) ? description : description+"\nProcessing Note: "+v.processingNote; 
		properties.put("description", description);
		JSONObject storageOptions = new JSONObject();
		storageOptions.put("color", color);
		storageOptions.put("iconClass", ICON);
		properties.put("_storage_options", storageOptions);
		obj.put("properties", properties);
		
		return obj;	
	}
	
	/**
	 * Builds a GeoJSON point as a feature.
	 * @param c Cluster representative.
	 * @param color Color of the cluster representative.
	 * @return GeoJSON point as a feature.
	 */
	@SuppressWarnings("unchecked")
	public JSONObject buildPointAsFeature(ClusterRepresentative c, String color) {
		JSONObject obj = new JSONObject();		
		obj.put("type", "Feature");
		
		JSONObject geometry = new JSONObject();
		geometry.put("type", "Point");
		JSONArray coordinates = new JSONArray();
		coordinates.add(c.lon);
		coordinates.add(c.lat);
		geometry.put("coordinates", coordinates);
		obj.put("geometry", geometry);
		
		JSONObject properties = new JSONObject();
		properties.put("name", c.label);
		properties.put("description", "**Cluster Representative**\nID: "+c.id+"\nType: "+c.typeIntern);		
		JSONObject storageOptions = new JSONObject();
		storageOptions.put("color", color);
		storageOptions.put("iconClass", ICON);
		properties.put("_storage_options", storageOptions);
		obj.put("properties", properties);
		
		return obj;	
	}
	
	/**
	 * Builds a feature collection from a list of features.
	 * @param features Features as JSON objects.
	 * @return Feature collection.
	 */
	@SuppressWarnings("unchecked")
	public JSONObject buildFeatureCollection(List<JSONObject> features) {
		JSONObject obj = new JSONObject();
		obj.put("type", "FeatureCollection");
		JSONArray array = new JSONArray();
		array.addAll(features);
		obj.put("features", array);
		
		return obj;
	}
	
	/**
	 * Builds JSON array containing the links between each cluster vertex to the representative.
	 * @param start Cluster representative.
	 * @param targets Vertices within the cluster.
	 * @return JSON array for coordinates of MultiLine object.
	 */
	@SuppressWarnings("unchecked")
	private JSONArray buildCoordinatesForCluster(Vertex start, Set<Vertex> targets) {
		JSONArray coordinates = new JSONArray();		
		
		JSONArray point0 = new JSONArray();
		point0.add(start.lon);
		point0.add(start.lat);
		if (point0.get(0) != null && point0.get(1) != null) {		
			for (Vertex target : targets) {
				JSONArray point1 = new JSONArray();
				point1.add(target.lon);
				point1.add(target.lat);
				
				JSONArray line = new JSONArray();
				line.add(point0);
				line.add(point1);
				if (point1.get(0)!= null && point1.get(1) != null)
					coordinates.add(line); // point within the cluster has coordinates: everything fine
				else {
					System.err.println("Point within the cluster has no coordinates: "+target);
				}
			}
		} else 
			System.err.println("Cluster representative has no coordinates: "+start);
		
		return coordinates;
	}
	
	/**
	 * Builds a MultiLineString object as a feature.
	 * @param coordinates Coordinates of the MultiLineString.
	 * @param color Color of the lines.
	 * @param lineWeight Weight of the lines.
	 * @return MultiLineString object.
	 */
	@SuppressWarnings("unchecked")
	public JSONObject buildMultiLineStringAsFeature(JSONArray coordinates, String color, String lineWeight) {
		JSONObject obj = new JSONObject();
		obj.put("type", "Feature");
		
		JSONObject geometry = new JSONObject();
		geometry.put("type", "MultiLineString");
		
		if (coordinates.size() == 0) {
			obj.put("geometry", null);
		} else {
			geometry.put("coordinates", coordinates);	
			obj.put("geometry", geometry);
		}
		
		JSONObject properties = new JSONObject();
		JSONObject storageOptions = new JSONObject();
		storageOptions.put("color", color);
		storageOptions.put("weight", lineWeight);
		storageOptions.put("opacity", LINE_OPACITY);
		properties.put("_storage_options", storageOptions);
		obj.put("properties", properties);
		
		return obj;
	}

}

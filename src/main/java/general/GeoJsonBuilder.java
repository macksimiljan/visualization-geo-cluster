package general;

import java.util.List;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import representation.ClusterRepresentative;
import representation.InputEdge;
import representation.Vertex;

public class GeoJsonBuilder {
	
	final private static String[] COLORS = {"SeaGreen", "Chocolate", "LightSeaGreen", "MediumBlue", "FireBrick", "DodgerBlue", "Orchid"};
	private static int currColorIndex = 0;
	final private static String ICON = "Drop";
	final private static String LINE_WEIGHT_BIG = "10";
	final private static String LINE_WEIGHT_SMALL = "4";
	final private static String LINE_OPACITY = "0.8";
	
	
	@SuppressWarnings("unchecked")
	public JSONArray buildCluster(ClusterRepresentative c, Set<Vertex> set) {
		JSONArray cluster = new JSONArray();
		currColorIndex = ++currColorIndex % COLORS.length;
		
		// add points
		cluster.add(buildPointAsFeature(c.lon, c.lat, c.label, COLORS[currColorIndex]));
		for (Vertex v : set) {
			cluster.add(buildPointAsFeature(v.lon, v.lat, v.label, COLORS[currColorIndex]));
		}
		
		// add lines
		cluster.add(buildMultiLineStringAsFeature(buildCoordinatesForCluster(c, set), COLORS[currColorIndex], LINE_WEIGHT_SMALL));
		
		return cluster;
	}
	
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
		}
		
		return buildMultiLineStringAsFeature(coordinates, "Gray", LINE_WEIGHT_BIG);
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject buildPointAsFeature(Double lon, Double lat, String name, String color) {
		JSONObject obj = new JSONObject();		
		obj.put("type", "Feature");
		
		JSONObject geometry = new JSONObject();
		geometry.put("type", "Point");
		JSONArray coordinates = new JSONArray();
		coordinates.add(lon);
		coordinates.add(lat);
		geometry.put("coordinates", coordinates);
		obj.put("geometry", geometry);
		
		JSONObject properties = new JSONObject();
		properties.put("name", name);
		JSONObject storageOptions = new JSONObject();
		storageOptions.put("color", color);
		storageOptions.put("iconClass", ICON);
		properties.put("_storage_options", storageOptions);
		obj.put("properties", properties);
		
		return obj;	
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject buildFeatureCollection(List<JSONObject> features) {
		JSONObject obj = new JSONObject();
		obj.put("type", "FeatureCollection");
		JSONArray array = new JSONArray();
		array.addAll(features);
		obj.put("features", array);
		
		return obj;
	}
	
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
				if(point1.get(0)!= null && point1.get(1) != null)
				coordinates.add(line);		
			}
		}		
		return coordinates;
	}
	
	
	@SuppressWarnings("unchecked")
	public JSONObject buildMultiLineStringAsFeature(JSONArray coordinates, String color, String lineWeight) {
		JSONObject obj = new JSONObject();
		obj.put("type", "Feature");
		
		JSONObject geometry = new JSONObject();
		geometry.put("type", "MultiLineString");
		
		geometry.put("coordinates", coordinates);		
		obj.put("geometry", geometry);
		
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

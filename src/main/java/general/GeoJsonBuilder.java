package general;

import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class GeoJsonBuilder {
	
	@SuppressWarnings("unchecked")
	public JSONObject buildPointAsFeature(Double lon, Double lat, String name) {
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

}

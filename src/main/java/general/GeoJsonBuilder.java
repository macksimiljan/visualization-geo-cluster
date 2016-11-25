package general;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import dict.VertexDict;
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
	
	/** Log4j Logger */
	public static Logger log = Logger.getLogger(GeoJsonBuilder.class);
	
	/** Current color index. */
	private static int currColorIndex = 0;
	
	/** Color of a vertex (except representative) is determined by its type. */
	private final boolean colorByVertexType;
	/** Color of a vertex is determined by the type of its representative. */
	private final boolean colorByRepresType;
	/** No cluster representative is printed to the geojson output. */
	private final boolean noRepresentative;
	/** Only cluster representatives are printed to the geojson output. */
	private final boolean onlyRepresentative;
	
	public GeoJsonBuilder(boolean colorByVertexType, boolean colorByRepresType, boolean noRepresentative, boolean onlyRepresentative) {
		this.colorByVertexType = colorByVertexType;
		this.colorByRepresType = colorByRepresType;
		if (noRepresentative && onlyRepresentative) {
			noRepresentative = false;
			onlyRepresentative = false;
		}
		this.noRepresentative = noRepresentative;
		this.onlyRepresentative = onlyRepresentative;
	}
	
	/**
	 * Builds a GeoJSON array as the representation of a cluster.
	 * @param c Cluster representative.
	 * @param set Vertices within the cluster.
	 * @return Cluster as GeoJSON array.
	 * @throws IllegalArgumentException If the coordinates of at least one vertex are illegal.
	 */
	@SuppressWarnings("unchecked")
	public JSONArray buildCluster(ClusterRepresentative c, Set<Vertex> set) throws IllegalArgumentException {
		JSONArray cluster = new JSONArray();
		currColorIndex = ++currColorIndex % ViewConstants.COLORS.length;
		
		// check for illegal values
		if (c.lat == null || c.lon == null) {
			// cluster representative has no coordinates
			for (Vertex v : set) { // randomly selecting coordinates from one other vertex
				if (v.lat == null || v.lon == null) {
					continue;
				} else {
					c.lat = v.lat;
					c.lon = v.lon;
					c.processingNote = "No GeoCoordinates!";
					break;
				}
			}
			// no vertex within the cluster has coordinates
			if (c.lat == null && c.lon == null)
				throw new IllegalArgumentException("No coordinates for the cluster representative: "+c);			
		}		
		if (c.lat != null && c.lon != null && (c.lat > 180 || c.lat < -180 || c.lon > 180 || c.lon < -180))
			throw new IllegalArgumentException("Illegal coordinates: "+c);
		
		String colorRepr = ViewConstants.COLORS[currColorIndex];
		if (colorByRepresType)
			colorRepr = ViewConstants.getTypeColor(c.typeIntern);
		// 1: add cluster representative to cluster
		if (!noRepresentative && c.lat != null && c.lon != null)
			cluster.add(buildPointAsFeature(c, colorRepr));
		
		for (Vertex v : set) {
			if (v.lat == null || v.lon == null)
				v.processingNote = "No GeoCoordinates!";
			v.lat = (v.lat == null) ? c.lat : v.lat;
			v.lon = (v.lon == null) ? c.lon : v.lon;
			
			if (v.lat != null && v.lon != null && (v.lat > 180 || v.lat < -180 || v.lon > 180 || v.lon < -180))
				throw new IllegalArgumentException("Illegal coordinates: "+v);
			
			String colorVertex = ViewConstants.COLORS[currColorIndex];
			if (colorByVertexType)
				colorVertex = ViewConstants.getTypeColor(v.typeInternInput);
			else if (colorByRepresType)
				colorVertex = colorRepr;
			// 2: add vertices to cluster
			if (!onlyRepresentative)
				cluster.add(buildPointAsFeature(v, colorVertex));
		}
		
		// 3: add lines
		if (!onlyRepresentative)
			cluster.add(buildMultiLineStringAsFeature(
				buildCoordinatesForCluster(c, set), colorRepr,ViewConstants.LINE_WEIGHT_SMALL));
		
		return cluster;
	}
	
	
	
	/**
	 * Builds a GeoJSON object as the representation of the original links between geo-objects.
	 * @param dict Vertex dictionary.
	 * @param edges Original links.
	 * @return Original links as GeoJSON object.
	 * @throws IllegalArgumentException If some vertex has illegal coordinates.
	 */
	@SuppressWarnings("unchecked")
	@Deprecated
	public JSONObject buildOldStructure(VertexDict dict, Set<InputEdge> edges) throws IllegalArgumentException {
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
				
				if (start.lat > 180 || start.lat < -180 || start.lon > 180 || start.lon < -180)
					throw new IllegalArgumentException("Illegal coordinates: "+start);
				if (target.lat > 180 || target.lat < -180 || target.lon > 180 || target.lon < -180)
					throw new IllegalArgumentException("Illegal coordinates: "+target);
				
				coordinates.add(line);
				
			}
			else 
				log.debug("One edge point has no coordinates: start "+start+", end "+target);
		}
		
		return buildMultiLineStringAsFeature(coordinates, ViewConstants.COLOR_ORIGINAL, ViewConstants.LINE_WEIGHT_BIG);
	}
	
	/**
	 * Builds the original link structure with similarity values on the edges.
	 * @param dict Vertex dictionary.
	 * @param edges Set of input edges for the original structure.
	 * @return The original links.
	 */
	@SuppressWarnings("unchecked")
	public JSONArray buildOldStructureWithSimVal(VertexDict dict, Set<InputEdge> edges) {
		JSONArray a = new JSONArray();
		
		for (InputEdge edge : edges) {
			Vertex start = dict.getVertexById(edge.source);
			Vertex target = dict.getVertexById(edge.target);
			JSONArray point0 = new JSONArray();
			point0.add(start.lon);
			point0.add(start.lat);
			JSONArray point1 = new JSONArray();
			point1.add(target.lon);
			point1.add(target.lat);
			
			JSONArray coordinates = new JSONArray();
			if (point0.get(0) != null && point0.get(1) != null
					&& point1.get(0) != null && point1.get(1) != null) {				

				if (start.lat > 180 || start.lat < -180 || start.lon > 180 || start.lon < -180)
					log.error("Illegal coordinates: "+start);
				else if (target.lat > 180 || target.lat < -180 || target.lon > 180 || target.lon < -180)
					log.error("Illegal coordinates: "+target);
				else {
					coordinates.add(point0);
					coordinates.add(point1);
				}			
			}
			else 
				log.debug("One edge point has no coordinates: start "+start+", end "+target);
			
			a.add(buildLineStringAsFeature(coordinates, "aggSimValue: "+edge.aggsimValue, ViewConstants.COLOR_ORIGINAL, ViewConstants.LINE_WEIGHT_BIG));
		}
		
		return a;
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
		String description = "ID: "+v.id+"\nType: "+v.typeInternInput+"\nOntology: "+v.ontology+"\nccId: "+v.ccId;
		description = (v.processingNote == null) ? description : description+"\nProcessing Note: "+v.processingNote; 
		properties.put("description", description);
		JSONObject storageOptions = new JSONObject();
		storageOptions.put("color", color);
		storageOptions.put("iconClass", ViewConstants.ICON);
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
		properties.put("name", "R: "+c.label);
		properties.put("description", "**Cluster Representative**\nID: "+c.id+"\nType: "+c.typeIntern+"\nVertices: "+c.clusteredVertexIds);		
		JSONObject storageOptions = new JSONObject();
		storageOptions.put("color", color);
		storageOptions.put("iconClass", ViewConstants.ICON_REPR);
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
					log.debug("Point within the cluster has no coordinates: "+target);
				}
			}
		} else 
			log.debug("Cluster representative has no coordinates: "+start);
		
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
		storageOptions.put("opacity", ViewConstants.LINE_OPACITY);
		properties.put("_storage_options", storageOptions);
//		properties.put("name", (color.equals(COLOR_ORIGINAL)) ? "original link" : "new cluster link");
		obj.put("properties", properties);
		
		return obj;
	}
	
	/**
	 * @param coordinates Coordinates of the LineString.
	 * @param description Description for the line.
	 * @param color Color of the line.
	 * @param lineWeight Weight of the line.
	 * @return LineString.
	 */
	@SuppressWarnings("unchecked")
	public JSONObject buildLineStringAsFeature(JSONArray coordinates, String description, String color, String lineWeight) {
		JSONObject obj = new JSONObject();
		obj.put("type", "Feature");
		
		JSONObject geometry = new JSONObject();
		geometry.put("type", "LineString");
		
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
		storageOptions.put("opacity", ViewConstants.LINE_OPACITY);
		properties.put("_storage_options", storageOptions);
		properties.put("name", (color.equals(ViewConstants.COLOR_ORIGINAL)) ? "original link" : "new cluster link");
		properties.put("description", description);
		obj.put("properties", properties);
		
		return obj;
	}

}

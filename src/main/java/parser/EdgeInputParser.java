package parser;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import representation.InputEdge;

/**
 * Parses data in 'edgeInput.geojson'.
 *
 * @author MM
 *
 */
public class EdgeInputParser {
	
	/**
	 * Parses a string representing a JSON object as an InputEdge object.
	 * @param line JSON object of an edge.
	 * @return Edge.
	 */
	public InputEdge parseLine(String line) {
		JSONParser parser = new JSONParser();
		InputEdge e = new InputEdge();
		
		try {
			JSONObject object = (JSONObject)parser.parse(line);
			e.source = (Long)object.get("source");
			e.target = (Long)object.get("target");
			JSONObject data = (JSONObject)object.get("data");
			try {
				e.trigramSim = (Double)data.get("trigramSim");
			} catch(Exception exc) {
				if (data.get("trigramSim") != null) {
					e.trigramSim = ((Long)data.get("trigramSim")).doubleValue();
				}
			}
			try {
				e.aggsimValue = (Double)data.get("aggSimValue");
			} catch(Exception exc) {
				if (data.get("aggSimValue") != null) {
					e.aggsimValue = ((Long)data.get("aggSimValue")).doubleValue();
				}
			}			
 		} catch (ParseException exc) {
			exc.printStackTrace();
		}
		
		return e;
	}

}

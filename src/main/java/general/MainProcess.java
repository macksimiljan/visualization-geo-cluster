package general;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;

import parser.MergedClusterParser;
import representation.ClusterRepresentative;
import representation.Vertex;

/**
 * Manages the visualization of clustered geo-coordinates.
 * 
 * @author moeller
 *
 */
public class MainProcess {
	
	public static void main(String[] args) {
		final String vertexFileLoc = "./src/main/resources/vertexInput.geojson";
		final String clusterFileLoc = "./src/main/resources/small_mergedCluster.geojson";
		final String outLoc = "./src/main/resources/out.json";
		
		System.out.println("--- start ---");
		System.out.println("vertexFileLoc: "+vertexFileLoc+"\nclusterFileLoc: "+clusterFileLoc);
		
		try (BufferedReader readerCluster = new BufferedReader(new FileReader(clusterFileLoc));
				PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outLoc)));) {
			System.out.println("Creating vertex dictionary ... ");
			VertexDict dict = new VertexDict(vertexFileLoc);
			
			System.out.println("Iterating through clusters ... ");
			GeoJsonBuilder geo = new GeoJsonBuilder();
			List<JSONObject> features = new ArrayList<JSONObject>();
			MergedClusterParser parserCluster = new MergedClusterParser();
			String line = null;
			while ( (line = readerCluster.readLine()) != null) {
				ClusterRepresentative cluster = parserCluster.parseLine(line);
				features.add(geo.buildPointAsFeature(cluster.lon, cluster.lat, cluster.label));
				for (Long id : cluster.clusteredVertexIds) {
					if (id == cluster.id)
						continue;
					Vertex v= dict.getVertexById(id);
					features.add(geo.buildPointAsFeature(v.lon, v.lat, v.label));
				}
			}
			JSONObject featureCollection = geo.buildFeatureCollection(features);
			
			System.out.println("Writing to "+outLoc+" ... ");
			featureCollection.writeJSONString(writer);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("--- end ---");
	}

}

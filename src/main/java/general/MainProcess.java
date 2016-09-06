package general;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONObject;

import parser.MergedClusterParser;
import representation.ClusterRepresentative;
import representation.InputEdge;
import representation.Vertex;

/**
 * Manages the visualization of clustered geo-coordinates.
 * 
 * @author MM
 *
 */
public class MainProcess {
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		// file locations
		final String vertexFileLoc = "./src/main/resources/vertexInput.geojson";
		final String edgeFileLoc = "./src/main/resources/edgeInput.geojson";
		final String clusterFileLoc = "./src/main/resources/small_mergedCluster.geojson";
		final String newClustersLoc = "./src/main/resources/layer2.json";
		final String oldClustersLoc = "./src/main/resources/layer1.json";
		
		System.out.println("--- start ---");
		System.out.println("vertexFileLoc: "+vertexFileLoc+"\nclusterFileLoc: "+clusterFileLoc);
		
		// read mergedCluster.geojson
		try (BufferedReader readerCluster = new BufferedReader(new FileReader(clusterFileLoc));
				PrintWriter writerCluster = new PrintWriter(new BufferedWriter(new FileWriter(newClustersLoc)));
				PrintWriter writerOldStructure = new PrintWriter(new BufferedWriter(new FileWriter(oldClustersLoc)));) {
			// create vertex and edge dictionary
			System.out.println("Creating vertex dictionary ... ");
			VertexDict dictVertex = new VertexDict(vertexFileLoc);
			System.out.println("Creating edge ditionary ... ");
			EdgeDict dictEdge = new EdgeDict(edgeFileLoc);
			
			System.out.println("Iterating through clusters ... ");
			GeoJsonBuilder geo = new GeoJsonBuilder();
			List<JSONObject> featuresNew = new ArrayList<JSONObject>();
			List<JSONObject> featuresOld = new ArrayList<JSONObject>();
			MergedClusterParser parserCluster = new MergedClusterParser();
			
			String line = null;
			
			while ( (line = readerCluster.readLine()) != null) {
				
				// for each cluster get representative and other cluster nodes
				ClusterRepresentative clusterRepr = parserCluster.parseLine(line);				
				Set<InputEdge> edges = new HashSet<InputEdge>();
				try {
					edges.addAll(dictEdge.getEdgeByStartId(clusterRepr.id));
				} catch (Exception e) {
					System.err.println(e.getMessage());
				}
				
				Set<Vertex> otherNodes = new HashSet<Vertex>();
				for (Long id : clusterRepr.clusteredVertexIds) {
					Vertex v = dictVertex.getVertexById(id);
					otherNodes.add(v);					
					try {
						edges.addAll(dictEdge.getEdgeByStartId(id));
					} catch (Exception e) {
						System.err.println(e.getMessage());
					}					
				}
							
				// build geojson objects of new clusters
				featuresNew.addAll(geo.buildCluster(clusterRepr, otherNodes));
				
				// build geojson object for the old structure
				featuresOld.add(geo.buildOldStructure(dictVertex, edges));
				
			}
			
			// save geojson objects of new clusters
			JSONObject featureCollection = geo.buildFeatureCollection(featuresNew);
			System.out.println("|features| = "+featuresNew.size());			
			System.out.println("Writing to "+newClustersLoc+" ... ");
			featureCollection.writeJSONString(writerCluster);
			
			// save gepjson objects of old structure
			featureCollection = geo.buildFeatureCollection(featuresOld);
			System.out.println("Writing to "+oldClustersLoc+" ... ");
			featureCollection.writeJSONString(writerOldStructure);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("--- end ---");
	}

}

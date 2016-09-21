package general;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import dict.EdgeDict;
import dict.RepresentativeDict;
import dict.VertexDict;
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
	
	/** Log4j Logger */
	public static Logger log = Logger.getLogger(MainProcess.class);
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		// file locations
		final String vertexFileLoc = "./src/main/resources/vertexInput.geojson";
		final String edgeFileLoc = "./src/main/resources/edgeInput.geojson";
		final String clusterFileLoc = "./src/main/resources/mergedCluster.geojson";
		final String newClustersLoc = "./src/main/resources/layer2.json";
		final String originalLinksLoc = "./src/main/resources/layer1.json";
		final int subsetSize = 100;
		
		log.info("--- start ---");
		log.info("vertexFileLoc: "+vertexFileLoc);
		log.info("clusterFileLoc: "+clusterFileLoc);
		
		// read mergedCluster.geojson
		try (PrintWriter writerCluster = new PrintWriter(new BufferedWriter(new FileWriter(newClustersLoc)));
				PrintWriter writerOriginalLinks = new PrintWriter(new BufferedWriter(new FileWriter(originalLinksLoc)));) {
			
			// create dictionaries
			log.info("Creating vertex dictionary ... ");
			VertexDict dictVertex = new VertexDict(vertexFileLoc);
			log.info("Creating edge ditionary ... ");
			EdgeDict dictEdge = new EdgeDict(edgeFileLoc);
			log.info("Creating merged cluster dictionary ... ");
			RepresentativeDict dictRepresentative = new RepresentativeDict(clusterFileLoc);
			
			// get a particular number of original cluster IDs
			log.info("Selecting "+subsetSize+" original clusters ... ");
			Set<Long> ccIds = dictVertex.getAllCcIds();
			ccIds = selectSubsetOfCcIds(ccIds, subsetSize);
						
			// get the vertex IDs of these cluster IDs
			// and check whether they are a cluster representative
			Set<Long> vertexIds = new HashSet<Long>();
			for (Long ccId : ccIds) {
				vertexIds.addAll(dictVertex.getVertexIdsByCcId(ccId));				
			}
			Set<ClusterRepresentative> representatives = new HashSet<ClusterRepresentative>();
			for (Long vertexId : vertexIds) {
				ClusterRepresentative r = dictRepresentative.getRepresentativeById(vertexId);
				if (r != null)
					representatives.add(r);
			}
			
			// construct GeoJSON objects of the original links
			log.info("Constructing original links ... ");
			GeoJsonBuilder geo = new GeoJsonBuilder();
			List<JSONObject> features = new ArrayList<JSONObject>();
			Set<InputEdge> edges = dictEdge.getEdgesByVertexIds(vertexIds);
			try {
				features.addAll(geo.buildOldStructureWithSimVal(dictVertex, edges));
			} catch (Exception e) {
				log.error(e.getMessage());
			}
			
			// save GeoJSON objects of old structure
			log.info("|features original links| = "+features.size());	
			log.info("Writing to "+originalLinksLoc+" ... ");
			writerOriginalLinks.println("{\n\"type\": \"FeatureCollection\",\n\"features\": [");
			for (int i = 0; i < features.size(); i++) {
				features.get(i).writeJSONString(writerOriginalLinks);
				if (i != features.size() - 1)
					writerOriginalLinks.print(",");
				writerOriginalLinks.println();
			}
			writerOriginalLinks.print("]\n}");
			
			// construct GeoJSON objects for each representative
			log.info("Iterating through cluster representatives (#: "+representatives.size()+") ... ");			
			features = new ArrayList<JSONObject>();		
			for (ClusterRepresentative clusterRepr : representatives) {				
				// build GeoJSON objects of new clusters
				Set<Vertex> otherNodes = new HashSet<Vertex>();
				for (Long id : clusterRepr.clusteredVertexIds) {
					Vertex v = dictVertex.getVertexById(id);
					otherNodes.add(v);
				}
				try {
					features.addAll(geo.buildCluster(clusterRepr, otherNodes));
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			}
			
			// save GeoJSON objects of new clusters
			log.info("|features new clusters| = "+features.size());		
			log.info("Writing to "+newClustersLoc+" ... ");
			writerCluster.println("{\n\"type\": \"FeatureCollection\",\n\"features\": [");
			for (int i = 0; i < features.size(); i++) {
				features.get(i).writeJSONString(writerCluster);
				if (i != features.size() - 1)
					writerCluster.print(",");
				writerCluster.println();
			}
			writerCluster.print("]\n}");
			
			
			
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		
		log.info("--- end ---");
	}
	
	/**
	 * Selects a subset of original clusters given by its ccID.
	 * @param ccIds IDs if the original link clusters.
	 * @param size Subset size.
	 * @return Subset of ccIds.
	 */
	private static Set<Long> selectSubsetOfCcIds(Set<Long> ccIds, int size) {		
		Set<Long> subset = new HashSet<Long>();
		if (size < 1)
			return subset;
		
		int i = 1;
		for (Long ccId : ccIds) {
			subset.add(ccId);
			if (++i > size)
				break;
		}
		
		return subset;
	}

}

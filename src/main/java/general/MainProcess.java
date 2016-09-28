package general;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
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
		
		// 0. load properties
		Properties properties = new Properties();
		BufferedInputStream stream;
		try {
			stream = new BufferedInputStream(new FileInputStream("./src/main/resources/config.properties"));
			properties.load(stream);
			stream.close();
		} catch (IOException e) {
			log.error("Could not load property file.");
			System.exit(1);
		}		
		
		/** input: vertices. */
		String vertexFileLoc = properties.getProperty("vertexFileLoc");
		/** input: edges. */
		String edgeFileLoc = properties.getProperty("edgeFileLoc");
		/** input: merged clusters. */
		String clusterFileLoc = properties.getProperty("clusterFileLoc");
		/** output: clusters layer. */
		String newClustersLoc = properties.getProperty("newClustersLoc");
		/** output: original links layer. */
		String originalLinksLoc = properties.getProperty("originalLinksLoc");
		/** parameter: subset size of original clusters. */
		int subsetSize = Integer.parseInt(properties.getProperty("subsetSize"));
		/** parameter: subset of ccIds. */
		String[] givenCcIds = properties.getProperty("ccIds").split("\\s*,\\s*");
		/** parameter: true iff only clusters with less than 4 nodes are printed. */
		boolean onlyInterestingClusters = Boolean.parseBoolean(properties.getProperty("onlyInterestingClusters"));
		
		
		log.info("--- start ---");
					
		// 1. create dictionaries
		VertexDict dictVertex = null;
		EdgeDict dictEdge = null;
		RepresentativeDict dictRepresentative = null;
		try {
			log.info("Creating vertex dictionary ... ");
			dictVertex = new VertexDict(vertexFileLoc);
			log.info("Creating edge ditionary ... ");
			dictEdge = new EdgeDict(edgeFileLoc);
			log.info("Creating merged cluster dictionary ... ");
			dictRepresentative = new RepresentativeDict(clusterFileLoc);
		} catch (Exception e) {
			log.error("Error while loading the dictionaries.");
			log.debug(e.getMessage());
			System.exit(1);
		}
			
		// 2.1 get a particular number of original cluster IDs
		log.info("Selecting "+subsetSize+" original clusters ... ");
		Set<Long> ccIds = dictVertex.getAllCcIds();
		ccIds = selectSubsetOfCcIds(ccIds, subsetSize, givenCcIds); 
		
		// 2.2 get the vertex IDs of these cluster IDs
		// 	   and determine the new clusters of these vertices
		Set<Long> vertexIds = new HashSet<Long>();
		for (Long ccId : ccIds) {
			vertexIds.addAll(dictVertex.getVertexIdsByCcId(ccId));				
		}
		Set<ClusterRepresentative> representatives = new HashSet<ClusterRepresentative>();
		for (Long vertexId : vertexIds) {
			ClusterRepresentative r = dictRepresentative.getRepresentativeByItsVertexId(vertexId);
			if (r != null)
				representatives.add(r);
		}
			
		// 3.1 construct GeoJSON objects of the original links
		log.info("Constructing original links ... ");
		GeoJsonBuilder geo = new GeoJsonBuilder();
		List<JSONObject> features = new ArrayList<JSONObject>();
		Set<InputEdge> edges = dictEdge.getEdgesByVertexIds(vertexIds);
		try {
			features.addAll(geo.buildOldStructureWithSimVal(dictVertex, edges));
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		
		// 3.2 save GeoJSON objects of old structure
		try (PrintWriter writerOriginalLinks = new PrintWriter(new BufferedWriter(new FileWriter(originalLinksLoc)));) {
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
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		
		// 4.1 construct GeoJSON objects for each representative
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
				// check whether cluster is interesting
				if (onlyInterestingClusters) {
					if (otherNodes.size() < 4)
						features.addAll(geo.buildCluster(clusterRepr, otherNodes));
				} else {
					features.addAll(geo.buildCluster(clusterRepr, otherNodes));
				}
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}		
		
		// 4.2 save GeoJSON objects of new clusters
		try (PrintWriter writerCluster = new PrintWriter(new BufferedWriter(new FileWriter(newClustersLoc)));) {
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
	 * @param givenCcIds Alternatively to 'size' ccIDs can be explicitly specified.
	 * @return Subset of ccIds.
	 */
	private static Set<Long> selectSubsetOfCcIds(Set<Long> ccIds, int size, String[] givenCcIds) {	
		Set<Long> subset = new HashSet<Long>();
		
		if (givenCcIds.length == 1 && givenCcIds[0].equals("null")) {		
			if (size == 0)
				return subset;
			
			if (size < 0)
				return ccIds;
			
			int i = 1;
			for (Long ccId : ccIds) {
				subset.add(ccId);
				if (++i > size)
					break;
			}
		} else {
			for (int i=0; i < givenCcIds.length; i++) {
				Long ccId = Long.parseLong(givenCcIds[i]);
				subset.add(ccId);
			}
		}
		
		return subset;
	}
	
}

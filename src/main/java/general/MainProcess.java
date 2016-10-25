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
import java.util.Random;
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
		String givenCcIds = properties.getProperty("ccIds");
		/** parameter: true iff only clusters with less than 4 nodes are printed. */
		boolean onlyInterestingClusters = Boolean.parseBoolean(properties.getProperty("onlyInterestingClusters").trim());
		/** parameter: subset size of original clusters. */
		int interestingSize = Integer.parseInt(properties.getProperty("interestingSize"));
		/** parameter: true iff color of a vertex (except representative) is determined by its type */
		boolean colorByVertexType = Boolean.parseBoolean(properties.getProperty("colorByVertexType").trim());
		/** parameter: true iff no cluster representative is printed to the geojson output */
		boolean noRepresentative = Boolean.parseBoolean(properties.getProperty("noRepresentative").trim());
		/** parameter: true iff color of a vertex is determined by the type of its representative */
		boolean colorByRepresType = Boolean.parseBoolean(properties.getProperty("colorByRepresType").trim());
		/** parameter: true iff only cluster representatives are printed to the geojson output */
		boolean onlyRepresentative = Boolean.parseBoolean(properties.getProperty("onlyRepresentative").trim());
		
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
			e.printStackTrace();
			log.debug(e.getMessage());
			System.exit(1);
		}
			
		// 2.1 get a particular number of original cluster IDs
		log.info((givenCcIds.equals("null")) ? 
				"Selecting "+subsetSize+" original clusters ... " : "Selecting original clusters according to specification ... ");
		Set<Long> ccIds = dictVertex.getAllCcIds();
		ccIds = selectSubsetOfCcIds(ccIds, subsetSize, givenCcIds); 
		
		// 2.2 get the vertex IDs of these cluster IDs
		// 	   and determine the new clusters of these vertices
		Set<Long> vertexIds = new HashSet<Long>();
		for (Long ccId : ccIds) {
			try {
				vertexIds.addAll(dictVertex.getVertexIdsByCcId(ccId));
			} catch (NullPointerException e) {
				log.error("No ccID "+ccId+" exists!");
			}
		}
		Set<ClusterRepresentative> representatives = new HashSet<ClusterRepresentative>();
		for (Long vertexId : vertexIds) {
			ClusterRepresentative r = dictRepresentative.getRepresentativeByItsVertexId(vertexId);
			if (r != null)
				representatives.add(r);
		}
			
		// 3.1 construct GeoJSON objects of the original links
		log.info("Constructing original links ... ");
		GeoJsonBuilder geo = new GeoJsonBuilder(colorByVertexType, colorByRepresType, noRepresentative, onlyRepresentative);
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
				if (onlyInterestingClusters && interestingSize < 0) {
					// check whether cluster is interesting; default case
					if (otherNodes.size() < 4)
						features.addAll(geo.buildCluster(clusterRepr, otherNodes));
				} else if (onlyInterestingClusters && interestingSize >= 0) {
					// check whether a cluster has an interesting size
					if (otherNodes.size() == interestingSize)
						features.addAll(geo.buildCluster(clusterRepr, otherNodes));
				} else {
					// we are interested in all clusters!
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
	private static Set<Long> selectSubsetOfCcIds(Set<Long> ccIds, int size, String givenCcIds) {
		Set<Long> subset = new HashSet<Long>();
		String[] array = givenCcIds.split("\\s*,\\s*");
		
		if (array.length == 1 && array[0].trim().equals("null")) {		
			if (size == 0)
				return subset;
			
			if (size < 0)
				return ccIds;
			
			List<Long> listCcId = new ArrayList<Long>(ccIds);
			Random randomGenerator = new Random();
			while (subset.size() < size) {				
				int randomIndex = randomGenerator.nextInt(listCcId.size());
				subset.add(listCcId.get(randomIndex));
			}
		} else {
			for (int i=0; i < array.length; i++) {
				Long ccId = Long.parseLong(array[i].trim());
				subset.add(ccId);
			}
		}
		
		return subset;
	}
	
}

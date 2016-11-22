package general;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import representation.Region;
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
	
	/** input: vertices. */
	private static String vertexFileLoc;
	/** input: edges. */
	private static String edgeFileLoc;
	/** input: merged clusters. */
	private static String clusterFileLoc;
	/** output: clusters layer. */
	private static String newClustersLoc;
	/** output: original links layer. */
	private static String originalLinksLoc;
	/** parameter: subset size of original clusters. */
	private static int subsetSize;
	/** parameter: subset of ccIds. */
	private static String givenCcIds;
	/** parameter: true iff only clusters with less than 4 nodes are printed. */
	private static boolean onlyInterestingClusters;
	/** parameter: subset size of original clusters. */
	private static int interestingSize;
	/** parameter: true iff color of a vertex (except representative) is determined by its type */
	private static boolean colorByVertexType;
	/** parameter: true iff no cluster representative is printed to the geojson output */
	private static boolean noRepresentative;
	/** parameter: true iff color of a vertex is determined by the type of its representative */
	private static boolean colorByRepresType;
	/** parameter: true iff only cluster representatives are printed to the geojson output */
	private static boolean onlyRepresentative;
	/** parameter: true iff partitions the data into different regions; each region is a file */
	private static boolean partition;
	
	
	
	public static void main(String[] args) {
		
		// 0. load properties
		log.info("Load properites ... ");
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
		
		vertexFileLoc = properties.getProperty("vertexFileLoc");
		edgeFileLoc = properties.getProperty("edgeFileLoc");
		clusterFileLoc = properties.getProperty("clusterFileLoc");
		newClustersLoc = properties.getProperty("newClustersLoc");
		originalLinksLoc = properties.getProperty("originalLinksLoc");
		subsetSize = Integer.parseInt(properties.getProperty("subsetSize"));
		givenCcIds = properties.getProperty("ccIds");
		onlyInterestingClusters = Boolean.parseBoolean(properties.getProperty("onlyInterestingClusters").trim());
		interestingSize = Integer.parseInt(properties.getProperty("interestingSize"));
		colorByVertexType = Boolean.parseBoolean(properties.getProperty("colorByVertexType").trim());
		noRepresentative = Boolean.parseBoolean(properties.getProperty("noRepresentative").trim());
		colorByRepresType = Boolean.parseBoolean(properties.getProperty("colorByRepresType").trim());
		onlyRepresentative = Boolean.parseBoolean(properties.getProperty("onlyRepresentative").trim());
		partition = Boolean.parseBoolean(properties.getProperty("partition").trim());
		
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
		// 	   and determine the new clusters of these vertices,
		//     for partition: vertex IDs and representatives are mapped to regions
		Set<Long> vertexIds = new HashSet<Long>();
		for (Long ccId : ccIds) {
			try {
				vertexIds.addAll(dictVertex.getVertexIdsByCcId(ccId));
			} catch (NullPointerException e) {
				log.error("No ccID "+ccId+" exists!");
			}
		}
		// partition containers
		Map<Region, String> filesForOriginalLinks = new HashMap<Region, String>();
		Map<Region, String> filesForNewClusters = new HashMap<Region, String>();
		Map<Region, Set<Long>> partitionVertexIds = new HashMap<Region, Set<Long>>();
		Map<Region, Set<ClusterRepresentative>> partitionRepresentatives = new HashMap<Region, Set<ClusterRepresentative>>();
		GeoDivision division = new GeoDivision(GeoDivision.initDefaultRegions());
		Region world = new Region(90, -90, 180, -180, "");
		// iterate through the vertex ID set
		log.info("Iterate through vertex IDs. Partition: "+partition);
		for (Long vertexId : vertexIds) {
			ClusterRepresentative r = dictRepresentative.getRepresentativeByItsVertexId(vertexId);			
			// check for partition
			if (partition) {
				// get regions of the current representative
				Set<Region> regions = division.determineRegion(r);
				// add the vertex ID to the corresponding regions
				for (Region region : regions) {
					Set<Long> valV = partitionVertexIds.get(region);
					if (valV != null) {
						valV.add(vertexId);						
					} else {
						valV = new HashSet<Long>();
						valV.add(vertexId);
					}
					partitionVertexIds.put(region, valV);
					// add the representative to the corresponding region
					Set<ClusterRepresentative> valR = partitionRepresentatives.get(region);
					if (valR != null) {
						valR.add(r);						
					} else {
						valR = new HashSet<ClusterRepresentative>();
						valR.add(r);
					}
					if (r != null)
						partitionRepresentatives.put(region, valR);
				}				
			} else {
				// no partition
				// add vertex ID
				if (partitionVertexIds.keySet().size() == 0) {
					Set<Long> valV = new HashSet<Long>();
					valV.add(vertexId);
					partitionVertexIds.put(world, valV);	
				} else {
					Set<Long> valV = partitionVertexIds.get(world);
					valV.add(vertexId);
					partitionVertexIds.put(world, valV);
				}
				// add cluster representative
				if (partitionRepresentatives.keySet().size() == 0) {					
					Set<ClusterRepresentative> valR = new HashSet<ClusterRepresentative>();
					valR.add(r);
					if (r != null)
						partitionRepresentatives.put(world, valR);
				} else {
					Set<ClusterRepresentative> valR = partitionRepresentatives.get(world);
					valR.add(r);
					if (r != null)
						partitionRepresentatives.put(world, valR);
				}
			}
		}		
		
		// modify files: add region name and cluster size to the file name
		String prefixOriginal = originalLinksLoc.substring(0, originalLinksLoc.lastIndexOf('.'));
		String suffixOriginal = originalLinksLoc.substring(originalLinksLoc.lastIndexOf('.'));
		String prefixClusters = newClustersLoc.substring(0, newClustersLoc.lastIndexOf('.'));
		String suffixClusters = newClustersLoc.substring(newClustersLoc.lastIndexOf('.'));
		
		for (Region region : partitionRepresentatives.keySet()) {
			String infix = "_"+region.label;
			if (onlyInterestingClusters)
				infix += (interestingSize < 0) ? "_lt4" : "_"+interestingSize;
			filesForOriginalLinks.put(region, prefixOriginal+infix+suffixOriginal);
			filesForNewClusters.put(region, prefixClusters+infix+suffixClusters);
		}			
		
		for (Region region : partitionRepresentatives.keySet()) {
			log.info("Current region: "+region.label+", #representatives: "+partitionRepresentatives.get(region).size());
			// 4. create and save original links
			manageOriginalLinks(dictEdge, dictVertex, partitionVertexIds.get(region), filesForOriginalLinks.get(region));
			// 5. create and save new clusters
			manageNewClusters(dictVertex, partitionRepresentatives.get(region), filesForNewClusters.get(region));
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
	
	@SuppressWarnings("unchecked")
	private static void manageOriginalLinks(EdgeDict dictEdge, VertexDict dictVertex, Set<Long> vertexIds, String loc) {
		// 1 construct GeoJSON objects of the original links
		log.info("Constructing original links ... ");
		GeoJsonBuilder geo = new GeoJsonBuilder(colorByVertexType, colorByRepresType, noRepresentative, onlyRepresentative);
		List<JSONObject> features = new ArrayList<JSONObject>();
		Set<InputEdge> edges = dictEdge.getEdgesByVertexIds(vertexIds);
		try {
			features.addAll(geo.buildOldStructureWithSimVal(dictVertex, edges));
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		
		// 2 save GeoJSON objects of old structure
		try (PrintWriter writerOriginalLinks = new PrintWriter(new BufferedWriter(new FileWriter(loc)));) {
			log.info("|features original links| = "+features.size());	
			log.info("Writing to "+loc+" ... ");
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
	}
	
	@SuppressWarnings("unchecked")
	private static void manageNewClusters(VertexDict dictVertex, Set<ClusterRepresentative> representatives, String loc) {
		// 1 construct GeoJSON objects for each representative
		log.info("Iterating through cluster representatives (#: "+representatives.size()+") ... ");	
		GeoJsonBuilder geo = new GeoJsonBuilder(colorByVertexType, colorByRepresType, noRepresentative, onlyRepresentative);
		List<JSONObject> features = new ArrayList<JSONObject>();		
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
		
		// 2 save GeoJSON objects of new clusters
		try (PrintWriter writerCluster = new PrintWriter(new BufferedWriter(new FileWriter(loc)));) {
			log.info("|features new clusters| = "+features.size());		
			log.info("Writing to "+loc+" ... ");
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
	}
	
}

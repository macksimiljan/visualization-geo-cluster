package dict;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import parser.MergedClusterParser;
import representation.ClusterRepresentative;

public class RepresentativeDict {
	
	/** Log4j Logger */
	public static Logger log = Logger.getLogger(RepresentativeDict.class);
	
	/** The merged cluster file. */
	final private File file;
	/** The dictionary: merged cluster id to representative. */
	private Map<Long, ClusterRepresentative> dict;
	/** Another dictionary: cluster vertex id to its representative. */
	private Map<Long, ClusterRepresentative> dictV;
	
	public RepresentativeDict(String mergedClusterFileLoc) throws IOException {
		this.file = new File(mergedClusterFileLoc);
		if (!this.file.exists())
			throw new FileNotFoundException("Could not find file "+mergedClusterFileLoc);
		
		dict = new HashMap<Long, ClusterRepresentative>();
		dictV = new HashMap<Long, ClusterRepresentative>();
				
		this.loadDict();
	}
	
	/**
	 * Return cluster representative by its ID.
	 * @param id Representative ID.
	 * @return Cluster representative.
	 */
	public ClusterRepresentative getRepresentativeById(Long id) {
		return dict.get(id);
	}
	
	/**
	 * Returns the cluster representative of a cluster by one of the cluster vertices.
	 * @param vertexId Vertex ID.
	 * @return Cluster representative.
	 */
	public ClusterRepresentative getRepresentativeByItsVertexId(Long vertexId) {
		return dictV.get(vertexId);
	}
	
	/**
	 * Return all IDs of cluster representatives.
	 * @return IDs of representatives.
	 */
	public Set<Long> getAllIds() {
		return dict.keySet();
	}
	
	private void loadDict() throws FileNotFoundException, IOException {
		try(BufferedReader reader = new BufferedReader(new FileReader(file));) {
			MergedClusterParser parser = new MergedClusterParser();
			String line = null;
			while ( (line = reader.readLine()) != null) {
				ClusterRepresentative r = parser.parseLine(line);
				
				// fill dictionary
				ClusterRepresentative prev = dict.put(r.id, r);
				if (prev != null)
					log.error("Duplicated ID for representative ID: "+r.id+"!");
				// fill the other dictionary
				for (Long vertexId : r.clusteredVertexIds) {
					prev = dictV.put(vertexId, r);
					if (prev != null)
						log.error("Vertex, id="+vertexId+", is part of two clusters!");
				}
			}
		}
	}

}

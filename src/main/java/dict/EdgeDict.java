package dict;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import parser.EdgeInputParser;
import representation.InputEdge;

/**
 * Dictionary for edges.
 * 
 * @author MM
 *
 */
public class EdgeDict {
	
	/** Log4j Logger */
	public static Logger log = Logger.getLogger(EdgeDict.class);
	
	/** The edge file. */
	final private File file;
	/** The dictionary. */
	private Map<Long, Set<InputEdge>> dict;
	
	/**
	 * Constructor.
	 * @param edgeFileLoc Location of the edge file.
	 * @throws IOException If file could not be found.
	 */
	public EdgeDict(String edgeFileLoc) throws IOException {
		this.file = new File(edgeFileLoc);
		if (!this.file.exists())
			throw new FileNotFoundException("Could not find file "+edgeFileLoc);
		
		dict = new HashMap<Long, Set<InputEdge>>();
		
		this.loadDict();
	}
	
	/**
	 * Returns edges for a given vertex ID as start and end point, respectively.
	 * @param id Start point ID.
	 * @return Set of edges.
	 * @throws NullPointerException If the dictionary contains no edges for that start ID.
	 */
	public Set<InputEdge> getEdgeByStartId(Long id) throws NullPointerException {
		Set<InputEdge> edges = dict.get(id);
		if (edges == null)
			throw new NullPointerException("There is no edge starting with ID "+id+"!");
		
		return dict.get(id);
	}
	
	/**
	 * Returns IDs of the start vertices of all edges.
	 * @return Set of vertex IDs.
	 */
	public Set<Long> getAllIds() {
		return dict.keySet();
	}
	
	/**
	 * Returns edges starting from one of the vertices represented by the given IDs.
	 * @param vertexIds Start point IDs.
	 * @return Set of edges.
	 */
	public Set<InputEdge> getEdgesByVertexIds(Set<Long> vertexIds) {
		Set<InputEdge> edges = new HashSet<InputEdge>();
		
		for (Long id : vertexIds) {
			try {
				edges.addAll(this.getEdgeByStartId(id));
			} catch (Exception e) {
				log.debug(e.getMessage());
			}
		}
		
		return edges;
	}
	
	/**
	 * Loads the dictionary.
	 * @throws FileNotFoundException If file path is wrong.
	 * @throws IOException If an error occurs while reading the file.
	 */
	private void loadDict() throws FileNotFoundException, IOException {
		try(BufferedReader reader = new BufferedReader(new FileReader(file));) {
			EdgeInputParser parser = new EdgeInputParser();
			String line = null;
			while ( (line = reader.readLine()) != null) {
				InputEdge edge = parser.parseLine(line);
				// from source to target
				Set<InputEdge> val = null;
				if (dict.containsKey(edge.source)) {
					val = dict.get(edge.source);
					val.add(edge);
				} else {
					val = new HashSet<InputEdge>();
					val.add(edge);					
				}
				dict.put(edge.source, val);
				// from target to source
				if (dict.containsKey(edge.target)) {
					val = dict.get(edge.target);
					val.add(edge);
				} else {
					val = new HashSet<InputEdge>();
					val.add(edge);					
				}
				dict.put(edge.target, val);
			}
		}
	}

}

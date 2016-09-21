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

import parser.VertexInputParser;
import representation.Vertex;

/**
 * Dictionary for vertices.
 * 
 * @author MM
 *
 */
public class VertexDict {
	
	/** The vertex file. */
	final private File file;
	/** The dictionary: vertexId to Vertex. */
	private Map<Long, Vertex> dict;
	/** Another dictionary: original cluster Id ccID to set of original cluster vertices.*/
	private Map<Long, Set<Long>> dictCcId;

	/**
	 * Constructor.
	 * @param vertexFileLoc Location of the file for input vertices.
	 * @throws IOException If there is an error while loading vertices from file.
	 */
	public VertexDict(String vertexFileLoc) throws IOException {
		this.file = new File(vertexFileLoc);
		if (!this.file.exists())
			throw new FileNotFoundException("Could not find file "+vertexFileLoc);
		
		dict = new HashMap<Long, Vertex>();
		dictCcId = new HashMap<Long, Set<Long>>();
		
		this.loadDicts();
	}
	
	/**
	 * Returns a vertex due to its ID.
	 * @param id Vertex ID.
	 * @return Vertex.
	 */
	public Vertex getVertexById(Long id) {
		return dict.get(id);
	}
	
	public Set<Long> getVertexIdsByCcId(Long ccId) {
		return dictCcId.get(ccId);
	}
	
	public Set<Long> getAllCcIds() {
		return dictCcId.keySet();
	}
	
	/**
	 * Returns all vertex IDs.
	 * @return Set of vertex IDs.
	 */
	public Set<Long> getAllIds() {
		return dict.keySet();
	}
	
	/**
	 * Loads the input vertex dictionary.
	 * @throws FileNotFoundException If file path is wrong.
	 * @throws IOException If an error occurs while reading the file.
	 */
	private void loadDicts() throws FileNotFoundException, IOException {
		try(BufferedReader reader = new BufferedReader(new FileReader(file));) {
			VertexInputParser parser = new VertexInputParser();
			String line = null;
			while ( (line = reader.readLine()) != null) {
				Vertex vertex = parser.parseLine(line);
				
				// fill dictionary
				Vertex prev = dict.put(vertex.id, vertex);
				if (prev != null)
					throw new IOException("Duplicated ID!");
				
				// fill second dictionary
				Long ccId = vertex.ccId;
				if (dictCcId.containsKey(ccId)) {
					Set<Long> val = dictCcId.get(ccId);
					val.add(vertex.id);
					dictCcId.put(ccId, val);
				} else {
					Set<Long> val = new HashSet<Long>();
					val.add(vertex.id);
					dictCcId.put(ccId, val);
				}
				
			}
		}
	}
}

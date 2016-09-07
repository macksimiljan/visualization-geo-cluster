package general;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
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
	/** The dictionary. */
	private Map<Long, Vertex> dict;

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
		
		this.loadDict();
	}
	
	/**
	 * Returns a vertex due to its ID.
	 * @param id Vertex ID.
	 * @return Vertex.
	 */
	public Vertex getVertexById(Long id) {
		return dict.get(id);
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
	private void loadDict() throws FileNotFoundException, IOException {
		try(BufferedReader reader = new BufferedReader(new FileReader(file));) {
			VertexInputParser parser = new VertexInputParser();
			String line = null;
			while ( (line = reader.readLine()) != null) {
				Vertex vertex = parser.parseLine(line);
				Vertex prev = dict.put(vertex.id, vertex);
				if (prev != null)
					throw new IOException("Duplicated ID!");
			}
		}
	}
}

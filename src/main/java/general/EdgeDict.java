package general;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import parser.EdgeInputParser;
import representation.InputEdge;

public class EdgeDict {
	
	final private File file;
	private Map<Long, Set<InputEdge>> dict;
	
	public EdgeDict(String edgeFileLoc) throws IOException {
		this.file = new File(edgeFileLoc);
		if (!this.file.exists())
			throw new FileNotFoundException("Could not find file "+edgeFileLoc);
		
		dict = new HashMap<Long, Set<InputEdge>>();
		
		this.loadDict();
	}
	
	public Set<InputEdge> getEdgeByStartId(Long id) throws NullPointerException {
		Set<InputEdge> edges = dict.get(id);
		if (edges == null)
			throw new NullPointerException("There is no edge starting with ID "+id+"!");
		
		return dict.get(id);
	}
	
	public Set<Long> getAllIds() {
		return dict.keySet();
	}
	
	public Set<InputEdge> getEdgesByVertexIds(Set<Long> vertexIds) {
		Set<InputEdge> edges = new HashSet<InputEdge>();
		
		for (Long id : vertexIds) {
			edges.addAll(this.getEdgeByStartId(id));
		}
		
		return edges;
	}
	
	private void loadDict() throws FileNotFoundException, IOException {
		try(BufferedReader reader = new BufferedReader(new FileReader(file));) {
			EdgeInputParser parser = new EdgeInputParser();
			String line = null;
			while ( (line = reader.readLine()) != null) {
				InputEdge edge = parser.parseLine(line);
				
				Set<InputEdge> val = null;
				if (dict.containsKey(edge.source)) {
					val = dict.get(edge.source);
					val.add(edge);
				} else {
					val = new HashSet<InputEdge>();
					val.add(edge);					
				}
				dict.put(edge.source, val);
			}
		}
	}

}

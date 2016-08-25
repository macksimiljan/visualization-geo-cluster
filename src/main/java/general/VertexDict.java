package general;

import java.util.HashMap;
import java.util.Map;

public class VertexDict {
	
	private Map<Long, Vertex> dict;

	public VertexDict() {
		dict = new HashMap<Long, Vertex>();
		this.loadDict();
	}
	
	public Vertex getVertexById(Long id) {
		return dict.get(id);
	}
	
	private void loadDict() {
		//TODO;
	}
}

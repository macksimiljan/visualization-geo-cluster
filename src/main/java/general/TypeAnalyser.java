package general;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import parser.MergedClusterParser;
import parser.VertexInputParser;
import representation.ClusterRepresentative;
import representation.Vertex;

public class TypeAnalyser {

	private Map<Set<String>, Integer> typeMap;
	private int emptyType, uniqueType, multiType;
	
	public TypeAnalyser() {
		this.typeMap = new HashMap<Set<String>, Integer>();
		this.emptyType = 0;
		this.uniqueType = 0;
		this.multiType = 0;
	}
	
	public void addType(Set<String> type) {
		if(this.typeMap.containsKey(type)) {
			int val = typeMap.get(type);
			val++;
			typeMap.put(type, val);
		} else {
			typeMap.put(type, 1);
		}
		
		if (type.size() == 0)
			this.emptyType++;
		else if (type.size() == 1)
			this.uniqueType++;
		else if (type.size() > 1)
			this.multiType++;
		else
			System.err.println("Something went wrong: Illegal type size!");
	}
	 
	public void printTypeDistrib() {
		System.out.println("--------------------");
		for (Set<String> type : typeMap.keySet()) {
			System.out.println(type+":\t"+typeMap.get(type));
		}
		System.out.println("--------------------");
		System.out.println("#emptyType: "+this.emptyType+", #uniqueType: "+this.uniqueType+", #multiType: "+this.multiType);
		System.out.println("--------------------");
	}
	
	public Set<Set<String>> getTypes() {
		return this.typeMap.keySet();
	}
	
	public void reset() {
		this.typeMap.clear();
		this.emptyType = 0;
		this.uniqueType = 0;
		this.multiType = 0;
	}
	
	public static void main(String[] args) {
		TypeAnalyser ta = new TypeAnalyser();
		String fileV = "./src/main/resources/input/vertexInput.geojson";
		String fileR = "./src/main/resources/input/mergedCluster.geojson";
		
		try(BufferedReader reader = new BufferedReader(new FileReader(fileV));) {
			VertexInputParser parser = new VertexInputParser();
			String line = null;
			
			while ( (line = reader.readLine()) != null) {
				Vertex vertex = parser.parseLine(line);
				Set<String> type = vertex.typeInternInput;
				ta.addType(type);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Vertices:");
		ta.printTypeDistrib();
		
		ta.reset();
		try(BufferedReader reader = new BufferedReader(new FileReader(fileR));) {
			MergedClusterParser parser = new MergedClusterParser();
			String line = null;
			while ( (line = reader.readLine()) != null) {
				ClusterRepresentative r = parser.parseLine(line);
				Set<String> type = r.typeIntern;
				ta.addType(type);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Representatives:");
		ta.printTypeDistrib();
	}

}

package general;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import parser.MergedClusterParser;
import representation.ClusterRepresentative;

public class Script {

	public static void main(String[] args) {
		Map<Long, Set<Long>> newIds = new HashMap<>();
		Map<Long, Set<String>> newOntolgoies = new HashMap<>();
		Map<Long, Set<String>> newTypeInterns = new HashMap<>();
		
		System.out.println("read /home/max/Dropbox/eval/additionalNodes.json");
		try (BufferedReader r = new BufferedReader(new FileReader("/home/max/Dropbox/eval/additionalNodes.json"))) {
			String line;
			MergedClusterParser parser = new MergedClusterParser();
			while ((line = r.readLine()) != null) {
				ClusterRepresentative cluster = parser.parseLine(line);
				newIds.put(cluster.id, cluster.clusteredVertexIds);
				newOntolgoies.put(cluster.id, cluster.ontologies);
				newTypeInterns.put(cluster.id, cluster.typeIntern);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("read /home/max/Dropbox/eval/combinedSettlements.json");
		Set<String> evalData = new HashSet<String>();
		String line = null;
		
		try (BufferedReader r = new BufferedReader(new FileReader("/home/max/Dropbox/eval/combinedSettlements.json"))) {		
			MergedClusterParser parser = new MergedClusterParser();
			while ((line = r.readLine()) != null) {
				if (!line.startsWith("{\"id\":"))
					continue;
				
				line = (line.endsWith(",")) ? line.substring(0,line.length()-1) : line;
				ClusterRepresentative cluster = parser.parseLine(line);
				Set<Long> clusteredVertexIds = cluster.clusteredVertexIds;
				boolean isWritten = false;
				for (Long id : clusteredVertexIds) {
					if (newIds.containsKey(id)) {
						Set<Long> v = new HashSet<Long>();
						v.addAll(cluster.clusteredVertexIds);
						v.addAll(newIds.get(id));
						cluster.ontologies.addAll(newOntolgoies.get(id));
						cluster.typeIntern.addAll(newTypeInterns.get(id));
						
						String nodes = "[", ontologies = "[", typeInternString = "[";
						for (Long vertexId : v)
							nodes += vertexId+",";
						for (String ontology : cluster.ontologies)
							ontologies += "\""+ontology+"\",";
						for (String type : cluster.typeIntern) {
							 if (!type.equals("no_type"))
								typeInternString += "\""+type+"\",";
						}
							
						
						nodes = nodes.substring(0, nodes.length()-1)+"]";
						typeInternString = (typeInternString.length() > 1) ? typeInternString.substring(0, typeInternString.length()-1)+"]" : typeInternString+"]";
						ontologies = ontologies.substring(0, ontologies.length()-1)+"]";
						evalData.add(cluster.label.toLowerCase()+"_#####_"+"{\"id\":"+cluster.id+",\"data\":{\"label\":\""+cluster.label+"\",\"clusteredVertices\":"+nodes+",\"ontologies\":"+ontologies+",\"typeIntern\":"+typeInternString+",\"simpleType\":\"Settlement\"}}");
						
						isWritten = true;
					}
				}
				if (!isWritten) {
					// write cluster without modifications
					evalData.add(cluster.label.toLowerCase()+"_#####_"+line);
				}
			}
		} catch (Exception e) {
			System.err.println(line);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("write ...");
		List<String> list = new ArrayList<String>(evalData);
		Collections.sort(list);
		try (PrintWriter w = new PrintWriter(new BufferedWriter(new FileWriter("/home/max/Dropbox/eval/output.json")))) {
			for (String line1 : list)
				w.println(line1.substring(line1.indexOf("_#####_")+"_#####_".length()));
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}

}

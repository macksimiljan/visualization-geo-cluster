package general;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import Tools.IO.IOTools;
import dict.VertexDict;
import parser.MergedClusterParser;
import representation.ClusterRepresentative;
import representation.Vertex;

public class PerfectStandardGuide {

	public static void main(String[] args) {
		System.out.println("Choose a region: ");
		System.out.println("\t 1 = africa");
		System.out.println("\t 2 = asia");
		System.out.println("\t 3 = australia");
		System.out.println("\t 4 = europe");
		System.out.println("\t 5 = north america");
		System.out.println("\t 6 = south america");
		System.out.println("\t 7 = no coordinates");		
		int c = IOTools.readInteger(" >> ");
		
		String path = "./src/main/resources/eval/";
		String[] files = {"eval_africa.json", "eval_asia.json", "eval_australia.json", "eval_europe.json", 
				"eval_northAm.json", "eval_southAm.json", "eval_noCoordinates.json"};
		String file = files[c-1];
		String commentFile = "comment_"+file;
		
		try (BufferedReader r = new BufferedReader(new FileReader(path+file));
				PrintWriter commentWriter =  new PrintWriter(new BufferedWriter(new FileWriter(path+commentFile, true)));
				PrintWriter out =  new PrintWriter(new BufferedWriter(new FileWriter(path+"mod_"+file, true)));) {
			MergedClusterParser parser = new MergedClusterParser();
			String line;
			while ((line = r.readLine()) != null) {
				ClusterRepresentative cluster = parser.parseLine(line);
				System.out.println();
				System.out.println("###########################################");
				System.out.println("label:\t\t"+cluster.label+"\nsimple type:\t"+cluster.simpleType+"\nnodes:\t\t"+cluster.clusteredVertexIds);
				System.out.println("-------------------------------");
				
				boolean printCluster = true;
				char evalCluster = 'y';
				while (evalCluster == 'y') {
					// actions
					Character[] actions = {'n', 't', 'a', 'c', 'e', 'd', 'l'};
					Set<Character> actionSet = new HashSet<Character>(Arrays.asList(actions));
					char action = 'x';			
					while (!actionSet.contains(action)) {
						System.out.println("Actions:");
						System.out.println("\t n = no changes");
						System.out.println("\t t = change simple type");
						System.out.println("\t a = add nodes");
						System.out.println("\t l = less nodes: split or remove");
						System.out.println("\t c = add comment");
						System.out.println("\t e = save and exit");
						System.out.println("\t d = already evaluated; if not: delete this cluster");
						action = IOTools.readChar(" >> ");
					}					
					
					switch (action) {
					case 'n':
						evalCluster = 'n';
						continue;
					case 't':
						System.out.println("types: "+cluster.typeIntern);
						System.out.println("simple type: "+cluster.simpleType);
						String type = IOTools.readString(" >> new simple type: ").trim();
						cluster.simpleType = type;
						break;
					case 'a':
						System.out.println("vertices: "+cluster.clusteredVertexIds);
						char repeat = 'y';
						while (repeat == 'y') {
							long id = IOTools.readLong(" >> additional vertex ID: ");
							cluster.clusteredVertexIds.add(id);
							do {
								repeat = IOTools.readChar(" >> another ID? [y/n] ");
							} while (repeat != 'y' && repeat != 'n');
						}
						// set smallest vertex ID as cluster ID
						cluster.id = getSmallestId(cluster.clusteredVertexIds);
						System.out.println("types: "+cluster.typeIntern);
						repeat = IOTools.readChar(" >> Add another type? [y/n] ");
						while (repeat == 'y') {
							String addType = IOTools.readLine(" >> additional type: ").trim();
							cluster.typeIntern.add(addType);
							do {
								repeat = IOTools.readChar(" >> another type? [y/n] ");
							} while (repeat != 'y' && repeat != 'n');
						}
						repeat = IOTools.readChar(" >> Add another ontology? [y/n] ");
						while (repeat == 'y') {
							String addOntology = IOTools.readString(" >> additional ontology: ").trim();
							cluster.ontologies.add(addOntology);
							do {
								repeat = IOTools.readChar(" >> another ontology? [y/n] ");
							} while (repeat != 'y' && repeat != 'n');
						}
						break;
					case 'l':
						System.out.println("vertices: "+cluster.clusteredVertexIds);
						Set<Long> keptVertices = new HashSet<Long>();
						Set<Long> newClusterVertices = new HashSet<Long>();
						for (Long id : cluster.clusteredVertexIds) {
							System.out.println("\t vertex: "+id);
							char removeNode;
							do {
								removeNode = IOTools.readChar("\t >> remove this node? [y/n] ");
							} while (removeNode != 'y' && removeNode != 'n');
							if (removeNode == 'y') {
								char toNewCluster;
								do {
									toNewCluster = IOTools.readChar("\t >> removed node to a new cluster? [y/n] ");
								} while (toNewCluster != 'y' && toNewCluster != 'n');
								if (toNewCluster == 'y')
									// add removed node to a new cluster
									newClusterVertices.add(id);
							} else
								// keep the node
								keptVertices.add(id);
						}
						if (cluster.clusteredVertexIds.size() > keptVertices.size())
							cluster = createRepresentative(keptVertices);
						if (newClusterVertices.size() > 0) {
							// create the new cluster
							ClusterRepresentative newRepresentative = createRepresentative(newClusterVertices);
							out.println(MergedClusterParser.printClusterRepresentative(newRepresentative));
						}
						break;
					case 'c':
						String comment = IOTools.readLine(" >> comment: ");
						commentWriter.println(cluster.id+"\t"+cluster.label+"\t"+comment);
						break;
					case 'e':
						commentWriter.close();
						out.close();
						System.exit(1);
					case 'd':
						printCluster = false;
						evalCluster = 'n';
						continue;
					}
					
					do {
						evalCluster = IOTools.readChar(" >> is there still anything to do? [y/n] ");
					} while (evalCluster != 'y' && evalCluster != 'n');
				}
				
				if (printCluster)
					out.println(MergedClusterParser.printClusterRepresentative(cluster));
				
				System.out.println("###########################################");
			}			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private static Long getSmallestId(Set<Long> clusteredVertexIds) {
		List<Long> sort = new ArrayList<Long>(clusteredVertexIds);
		Collections.sort(sort);
		return sort.get(0);
	}
	
	private static ClusterRepresentative createRepresentative(Set<Long> vertexIds) {
		ClusterRepresentative newRepresentative = new ClusterRepresentative();
		newRepresentative.id = getSmallestId(vertexIds);
		newRepresentative.clusteredVertexIds = vertexIds;
		try {
			VertexDict vertexDict = new VertexDict("./src/main/resources/input/vertexInput.geojson");
			Vertex representativeVertex = vertexDict.getVertexById(newRepresentative.id);
			newRepresentative.label = representativeVertex.label;
			newRepresentative.lat = representativeVertex.lat;
			newRepresentative.lon = representativeVertex.lon;
			Set<String> ontologies = new HashSet<String>();
			Set<String> types = new HashSet<String>();
			for (Long vId : vertexIds) {
				Vertex v = vertexDict.getVertexById(vId);
				ontologies.add(v.ontology);
				for (String type : v.typeInternInput)
					if (!type.equals("no_type"))
						types.add(type); //TODO: for split: how to determine typeIntern set?
			}
			newRepresentative.ontologies = ontologies;
			newRepresentative.typeIntern = types;
			System.out.println("types of the cluster ("+newRepresentative.label+", "+newRepresentative.id+"): "+types);
			newRepresentative.simpleType = IOTools.readString("\t >> simple type: ").trim();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return newRepresentative;
	}

}

package general;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Tools.IO.IOTools;
import parser.MergedClusterParser;
import representation.ClusterRepresentative;

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
		
		String path = "/home/max/git/visualization-geo-cluster/src/main/resources/layers/";
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
				System.out.println("label:\t\t"+cluster.label+"\nsimple type:\t"+cluster.simpleType);
				System.out.println("-------------------------------");
				
				char evalCluster = 'y';
				while (evalCluster == 'y') {
					// actions
					
					char action = 'n';					
					System.out.println("Actions:");
					System.out.println("\t n = no changes");
					System.out.println("\t t = change simple type");
					System.out.println("\t a = add nodes");
					System.out.println("\t c = add comment");
					System.out.println("\t e = save and exit");
					System.out.println("\t d = already evaluated; if not: delete this cluster");
					action = IOTools.readChar(" >> ");
						
					
					
					switch (action) {
					case 'n':
						out.println(line);
						continue;
					case 't':
						System.out.println("simple type: "+cluster.simpleType);
						String type = IOTools.readString(" >> new simple type: ");
						cluster.simpleType = type;
						break;
					case 'a':
						System.out.println("vertices: "+cluster.clusteredVertexIds);
						char repeat = 'y';
						while (repeat == 'y') {
							long id = IOTools.readLong(" >> additional vertex ID: ");
							cluster.clusteredVertexIds.add(id);
							repeat = IOTools.readChar(" >> another ID? [y/n] ");
						}
						// set smallest vertex ID as cluster ID
						List<Long> sort = new ArrayList<Long>(cluster.clusteredVertexIds);
						Collections.sort(sort);
						cluster.id = sort.get(0);
						System.out.println("types: "+cluster.typeIntern);
						repeat = IOTools.readChar(" >> Add another type? [y/n] ");
						while (repeat == 'y') {
							String addType = IOTools.readString(" >> additional type: ");
							cluster.typeIntern.add(addType);
							repeat = IOTools.readChar(" >> another type? [y/n] ");
						}
						repeat = IOTools.readChar(" >> Add another ontology? [y/n] ");
						while (repeat == 'y') {
							String addOntology = IOTools.readString(" >> additional ontology: ");
							cluster.ontologies.add(addOntology);
							repeat = IOTools.readChar(" >> another ontology? [y/n] ");
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
						continue;
					}
					
					evalCluster = IOTools.readChar(" >> is there still anything to do? [y/n] ");
				}
				
				out.println(MergedClusterParser.printClusterRepresentative(cluster));
				
				System.out.println("###########################################");
			}			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}

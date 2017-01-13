package general;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import parser.MergedClusterParser;
import representation.ClusterRepresentative;

public class Test {

	public static void main(String[] args) {
		Set<String> set = new HashSet<String>();
		Set<String> set2 = new HashSet<String>();
		MergedClusterParser parser = new MergedClusterParser();
		
		String line = null;
		try (BufferedReader r = new BufferedReader(new FileReader("/home/max/Dropbox/eval/output.json"))) {		
			while ((line = r.readLine()) != null) {
				if (!line.startsWith("{\"id\":"))
					continue;
				
				ClusterRepresentative cluster = parser.parseLine(line);
				set.add(cluster.label);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("|output|="+set.size());
		
		try (BufferedReader r = new BufferedReader(new FileReader("/home/max/Dropbox/eval/combinedSettlements.json"))) {		
			while ((line = r.readLine()) != null) {
				if (!line.startsWith("{\"id\":"))
					continue;
				
				line = (line.endsWith(",")) ? line.substring(0,line.length()-1) : line;
				ClusterRepresentative cluster = parser.parseLine(line);
				set2.add(cluster.label);
			}
		} catch (Exception e) {
			System.err.println("line: "+line);
			e.printStackTrace();
		}
		System.out.println("|combinedSettlements|="+set2.size());
		
		set.removeAll(set2);
		
		int i = 1;
		for (String line1 : set) {
			System.out.println(i+" "+line1);
			i++;
		}

	}

}

package parser;

import java.io.File;

import general.ClusterRepresentative;

public class MergedClusterParser {
	
	final File file;
	
	
	
	
	public MergedClusterParser(String fileLoc) throws IllegalArgumentException {
		File f = new File(fileLoc);
		if (!f.exists())
			throw new IllegalArgumentException("The specified file "+fileLoc+" does not exist!");
		this.file = f;
	}
	
	public MergedClusterParser() {
		this.file = null;
	}
	
	public ClusterRepresentative parseLine(String line) {
		
	}

}

package general;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class ViewConstants {
	
	/** Colors of the clusters. */
//	final private static String[] COLORS = {"SeaGreen", "Chocolate", "LightSeaGreen", 
//			"MediumBlue", "FireBrick", "DodgerBlue", "Orchid", 
//			"Tomato", "Teal", "MediumPurple", "PowderBlue"};
	final public static String[] COLORS = { "Navy", "Blue", "Green", "DarkCyan", "DeepSkyBlue", 
			"SteelBlue", "RoyalBlue", "MediumSeaGreen", "SeaGreen", "LightSeaGreen",
			"CornflowerBlue", "CadetBlue" };
	
	/** Icon of geo-points within uMap. */
	final public static String ICON = "Circle"; //"Drop";
	/** Big edge weight. */
	final public static String LINE_WEIGHT_BIG = "10";
	/** Small edge weight. */
	final public static String LINE_WEIGHT_SMALL = "4";
	/** Edge opacity. */
	final public static String LINE_OPACITY = "0.8";
	/** Original link color. */
	final public static String COLOR_ORIGINAL = "Gold";
	
	final private static Map<String, String> TYPE2COLOR = new HashMap<String, String>();
	static {
		TYPE2COLOR.put("emptyType", "LightGrey");
		TYPE2COLOR.put("multiType", "Black");
		TYPE2COLOR.put("[BodyOfWater]", "Blue");
		TYPE2COLOR.put("[Park]", "Lime");
		TYPE2COLOR.put("[Settlement]", "Orange");
		TYPE2COLOR.put("[Island]", "Green");
		TYPE2COLOR.put("[AdministrativeRegion]", "DarkGoldenRod");
		TYPE2COLOR.put("[School]", "Fuchsia");
		TYPE2COLOR.put("[Country]", "Tan");
		TYPE2COLOR.put("[ArchitecturalStructure]", "DarkViolet");
		TYPE2COLOR.put("[Mountain]", "DarkRed");
	}
	
	public static String getTypeColor(Set<String> type) throws IllegalArgumentException {
		if (type.size() == 0 || (type.size() == 1 && type.toString().equals("[no_type]")))
			return TYPE2COLOR.get("emptyType");
		else if (type.size() > 1)
			return TYPE2COLOR.get("multiType");
		else {
			String color = TYPE2COLOR.get(type.toString());
			if (color != null)
				return color;
			else 
				throw new IllegalArgumentException("Unexpected type! '"+type+"' not contained in  type-color map.");
		}
			
	}

}
